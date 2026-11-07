-- Correction ponctuelle de la réception du BEBC-2026-D5AA0BEE.
-- Maïs blanc : 8 sacs reçus ; Niébé : 9 sacs reçus.
-- Totaux : 17 sacs / 850 kg ; écarts : -3 sacs / -150 kg ; statut MANQUANT.

BEGIN;

UPDATE bebc_lignes_enlevement l
SET nombre_sacs_recus = 8,
    quantite_recue_kg = ROUND((8 * l.colisage_kg)::numeric, 2)::double precision
FROM speculations s, bordereaux_enlevement_bebc b
WHERE l.speculation_id = s.id
  AND l.bordereau_enlevement_id = b.id
  AND b.numero_bebc = 'BEBC-2026-D5AA0BEE'
  AND s.code = 'MAIS_BLANC';

UPDATE bebc_lignes_enlevement l
SET nombre_sacs_recus = 9,
    quantite_recue_kg = ROUND((9 * l.colisage_kg)::numeric, 2)::double precision
FROM speculations s, bordereaux_enlevement_bebc b
WHERE l.speculation_id = s.id
  AND l.bordereau_enlevement_id = b.id
  AND b.numero_bebc = 'BEBC-2026-D5AA0BEE'
  AND s.code = 'NIEBE';

UPDATE magasins_centraux m
SET stock_actuel_tonnes = GREATEST(0, COALESCE(m.stock_actuel_tonnes, 0) - 0.15)
FROM bordereaux_reception_magasin r
JOIN bordereaux_enlevement_bebc b ON b.id = r.bordereau_enlevement_id
WHERE b.numero_bebc = 'BEBC-2026-D5AA0BEE'
  AND r.tonnage_decharge_kg = 1000.0
  AND m.id = r.magasin_central_id;

UPDATE bordereaux_reception_magasin r
SET nombre_sacs_recus = 17,
    tonnage_decharge_kg = 850.0,
    ecart_sacs = -3,
    ecart_poids_kg = -150.0,
    statut_conformite = 'MANQUANT'
FROM bordereaux_enlevement_bebc b
WHERE b.id = r.bordereau_enlevement_id
  AND b.numero_bebc = 'BEBC-2026-D5AA0BEE';

UPDATE bordereaux_enlevement_bebc
SET statut = 'ECART_CONSTATE'
WHERE numero_bebc = 'BEBC-2026-D5AA0BEE';

INSERT INTO journal_audit (
    matricule_utilisateur,
    action,
    entite_concernee,
    entite_id,
    resultat,
    details
)
SELECT
    'SYSTEM',
    'CORRECTION_RECEPTION',
    'BordereauReceptionMagasin',
    r.id,
    'SUCCES',
    'Correction rétroactive BEBC-2026-D5AA0BEE : 17 sacs / 850 kg (écart -3 sacs / -150 kg)'
FROM bordereaux_reception_magasin r
JOIN bordereaux_enlevement_bebc b ON b.id = r.bordereau_enlevement_id
WHERE b.numero_bebc = 'BEBC-2026-D5AA0BEE'
  AND NOT EXISTS (
      SELECT 1
      FROM journal_audit j
      WHERE j.action = 'CORRECTION_RECEPTION'
        AND j.details LIKE '%BEBC-2026-D5AA0BEE%'
  );

COMMIT;
