-- V904 : un seul BRBC pour les collectes multi-spéculations d'un producteur.
-- Chaque ligne de collecte conserve son propre numéro de lot, son prix et son montant.
-- Les BRBC historiques d'un même producteur / comptoir / campagne / jour sont regroupés.

-- 1. Colonnes de ligne et nouveau rattachement au BRBC
ALTER TABLE collectes_bord_champ
    ADD COLUMN IF NOT EXISTS quantite_kg DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS prix_unitaire_kg DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS montant_net_cfa DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS bordereau_id BIGINT;

UPDATE collectes_bord_champ c
SET quantite_kg = COALESCE(
        (SELECT f.poids_net_total_kg FROM fiches_pesee_fp f WHERE f.collecte_id = c.id),
        b.quantite_kg
    ),
    prix_unitaire_kg = b.prix_unitaire_applique,
    montant_net_cfa = b.montant_net_payer_cfa,
    bordereau_id = b.id
FROM bordereaux_reception_brbc b
WHERE b.collecte_id = c.id;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM collectes_bord_champ WHERE bordereau_id IS NULL) THEN
        RAISE EXCEPTION 'Migration BRBC : une collecte ne possède pas de bordereau de réception.';
    END IF;
END $$;

-- 2. Table de correspondance ancien BRBC -> BRBC maître (plus petit identifiant)
CREATE TEMP TABLE brbc_regroupement ON COMMIT DROP AS
SELECT b.id AS old_id,
       MIN(b.id) OVER (
           PARTITION BY c.producteur_id, c.comptoir_id, c.campagne_id,
                        c.date_heure_collecte::date
       ) AS master_id
FROM bordereaux_reception_brbc b
JOIN collectes_bord_champ c ON c.id = b.collecte_id;

-- Sécurité : ne pas fusionner des BRBC déjà payés de façon incompatible
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM brbc_regroupement m
        JOIN ordres_paiement o ON o.bordereau_reception_id = m.old_id
        WHERE m.old_id <> m.master_id
    ) THEN
        RAISE EXCEPTION 'Regroupement BRBC impossible : un BRBC secondaire possède déjà un ordre de paiement.';
    END IF;

    IF EXISTS (
        WITH statuts AS (
            SELECT c.bordereau_id,
                   MIN(CASE c.statut
                       WHEN 'REJETEE' THEN 0
                       WHEN 'SYNCHRONISEE' THEN 1
                       WHEN 'EXAMINEE_SCP' THEN 2
                       WHEN 'VALIDEE_DCT' THEN 3
                   END) AS rang
            FROM collectes_bord_champ c
            GROUP BY c.bordereau_id
        )
        SELECT 1
        FROM statuts s
        JOIN ordres_paiement o ON o.bordereau_reception_id = s.bordereau_id
        WHERE s.rang < 3
    ) THEN
        RAISE EXCEPTION 'Regroupement BRBC impossible : un BRBC payé possède des lignes non validées.';
    END IF;
END $$;

-- 3. Réaffectation des lignes au BRBC maître
UPDATE collectes_bord_champ c
SET bordereau_id = m.master_id
FROM brbc_regroupement m
WHERE c.bordereau_id = m.old_id
  AND m.old_id <> m.master_id;

-- 4. Agrégats du BRBC maître
UPDATE bordereaux_reception_brbc b
SET quantite_kg = t.total_quantite,
    montant_net_payer_cfa = t.total_montant,
    prix_unitaire_applique = CASE
        WHEN t.total_quantite > 0
            THEN ROUND((t.total_montant / t.total_quantite)::numeric, 2)
        ELSE b.prix_unitaire_applique
    END
FROM (
    SELECT bordereau_id,
           SUM(quantite_kg) AS total_quantite,
           SUM(montant_net_cfa) AS total_montant
    FROM collectes_bord_champ
    GROUP BY bordereau_id
) t
WHERE b.id = t.bordereau_id;

-- 5. Harmonisation des statuts : le BRBC est examiné / validé en bloc
WITH statuts AS (
    SELECT c.bordereau_id,
           MIN(CASE c.statut
               WHEN 'REJETEE' THEN 0
               WHEN 'SYNCHRONISEE' THEN 1
               WHEN 'EXAMINEE_SCP' THEN 2
               WHEN 'VALIDEE_DCT' THEN 3
           END) AS rang
    FROM collectes_bord_champ c
    GROUP BY c.bordereau_id
)
UPDATE collectes_bord_champ c
SET statut = CASE s.rang
                 WHEN 0 THEN 'REJETEE'
                 WHEN 1 THEN 'SYNCHRONISEE'
                 WHEN 2 THEN 'EXAMINEE_SCP'
                 ELSE 'VALIDEE_DCT'
             END,
    examinateur_id = CASE WHEN s.rang <= 1 THEN NULL ELSE c.examinateur_id END,
    date_examen = CASE WHEN s.rang <= 1 THEN NULL ELSE c.date_examen END,
    validateur_id = CASE WHEN s.rang <= 2 THEN NULL ELSE c.validateur_id END,
    date_validation = CASE WHEN s.rang <= 2 THEN NULL ELSE c.date_validation END
FROM statuts s
WHERE c.bordereau_id = s.bordereau_id;

-- Aligner l'examinateur sur les groupes examinés (rang 2)
UPDATE collectes_bord_champ c
SET examinateur_id = e.examinateur_id,
    date_examen = e.date_examen
FROM (
    SELECT DISTINCT ON (bordereau_id)
           bordereau_id, examinateur_id, date_examen
    FROM collectes_bord_champ
    WHERE examinateur_id IS NOT NULL
    ORDER BY bordereau_id, date_examen DESC
) e
WHERE c.bordereau_id = e.bordereau_id
  AND c.statut = 'EXAMINEE_SCP';

-- Propager le motif de rejet aux lignes du même BRBC
UPDATE collectes_bord_champ c
SET motif_rejet = m.motif
FROM (
    SELECT DISTINCT ON (bordereau_id) bordereau_id, motif_rejet AS motif
    FROM collectes_bord_champ
    WHERE statut = 'REJETEE' AND motif_rejet IS NOT NULL
    ORDER BY bordereau_id, date_validation DESC NULLS LAST, date_examen DESC NULLS LAST
) m
WHERE c.bordereau_id = m.bordereau_id
  AND c.statut = 'REJETEE'
  AND c.motif_rejet IS NULL;

-- 6. Statut de validation du BRBC
UPDATE bordereaux_reception_brbc b
SET statut_validation = CASE
    WHEN EXISTS (SELECT 1 FROM collectes_bord_champ c
                 WHERE c.bordereau_id = b.id AND c.statut = 'REJETEE') THEN 'REJETE'
    WHEN EXISTS (SELECT 1 FROM collectes_bord_champ c
                 WHERE c.bordereau_id = b.id AND c.statut = 'SYNCHRONISEE') THEN 'EN_ATTENTE_EXAMEN'
    WHEN EXISTS (SELECT 1 FROM collectes_bord_champ c
                 WHERE c.bordereau_id = b.id AND c.statut = 'EXAMINEE_SCP') THEN 'EXAMINE_SCP'
    ELSE 'VALIDE_DCT'
END;

-- Un BRBC non validé ne doit pas conserver un numéro officiel
UPDATE bordereaux_reception_brbc b
SET numero_brbc = 'BRBC-REG-' || b.id
WHERE b.id IN (
    SELECT c.bordereau_id
    FROM collectes_bord_champ c
    WHERE c.statut <> 'VALIDEE_DCT'
    GROUP BY c.bordereau_id
)
AND b.numero_brbc NOT LIKE 'BRBC-PRV-%'
AND b.numero_brbc NOT LIKE 'BRBC-REG-%';

-- 7. Libellé du montant en lettres (les DTO le recalculent à la lecture)
UPDATE bordereaux_reception_brbc b
SET montant_en_lettres = 'Montant total du BRBC : ' || b.montant_net_payer_cfa::bigint || ' francs CFA'
FROM (
    SELECT bordereau_id
    FROM collectes_bord_champ
    GROUP BY bordereau_id
    HAVING COUNT(*) > 1
) t
WHERE b.id = t.bordereau_id;

-- 8. Suppression des BRBC secondaires (aucun paiement, garanti ci-dessus)
DELETE FROM bordereaux_reception_brbc b
USING brbc_regroupement m
WHERE b.id = m.old_id
  AND m.old_id <> m.master_id;

-- 9. Nouvelle relation BRBC -> plusieurs collectes
ALTER TABLE bordereaux_reception_brbc DROP COLUMN collecte_id;

ALTER TABLE collectes_bord_champ
    ALTER COLUMN quantite_kg SET NOT NULL,
    ALTER COLUMN prix_unitaire_kg SET NOT NULL,
    ALTER COLUMN montant_net_cfa SET NOT NULL,
    ALTER COLUMN bordereau_id SET NOT NULL;

ALTER TABLE collectes_bord_champ
    ADD CONSTRAINT fk_collectes_bordereau
    FOREIGN KEY (bordereau_id) REFERENCES bordereaux_reception_brbc(id) ON DELETE RESTRICT;

CREATE INDEX IF NOT EXISTS idx_collectes_bordereau_id
    ON collectes_bord_champ(bordereau_id);
