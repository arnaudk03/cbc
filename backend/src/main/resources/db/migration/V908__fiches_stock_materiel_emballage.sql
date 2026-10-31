-- Fiches de stock du matériel d'emballage : référentiel, en-tête, lignes.
-- Les anciennes fiches plates sont reprises puis supprimées.

CREATE TABLE IF NOT EXISTS materiels_emballages (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    libelle VARCHAR(150) NOT NULL,
    categorie VARCHAR(30) NOT NULL DEFAULT 'AUTRE',
    unite VARCHAR(20) NOT NULL DEFAULT 'UNITE',
    seuil_alerte INT NOT NULL DEFAULT 0,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS fiches_stock_materiel (
    id BIGSERIAL PRIMARY KEY,
    numero_fiche VARCHAR(50) UNIQUE NOT NULL,
    comptoir_id BIGINT NOT NULL REFERENCES comptoirs (id) ON DELETE RESTRICT,
    date_fiche DATE NOT NULL,
    statut VARCHAR(20) NOT NULL DEFAULT 'BROUILLON',
    observations TEXT,
    cree_par_id BIGINT REFERENCES utilisateurs (id) ON DELETE SET NULL,
    valide_par_id BIGINT REFERENCES utilisateurs (id) ON DELETE SET NULL,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valide_le TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_fiches_stock_materiel_comptoir
    ON fiches_stock_materiel (comptoir_id, date_fiche DESC);

CREATE TABLE IF NOT EXISTS lignes_fiche_stock_materiel (
    id BIGSERIAL PRIMARY KEY,
    fiche_id BIGINT NOT NULL REFERENCES fiches_stock_materiel (id) ON DELETE CASCADE,
    materiel_id BIGINT NOT NULL REFERENCES materiels_emballages (id) ON DELETE RESTRICT,
    stock_initial INT NOT NULL DEFAULT 0,
    entrees INT NOT NULL DEFAULT 0,
    sorties INT NOT NULL DEFAULT 0,
    stock_physique INT,
    ecart INT,
    stock_final INT NOT NULL,
    reference_doc VARCHAR(50),
    observation VARCHAR(255)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_ligne_fiche_materiel
    ON lignes_fiche_stock_materiel (fiche_id, materiel_id);

INSERT INTO materiels_emballages (code, libelle, categorie, unite, seuil_alerte)
VALUES
    ('SACS_JUTE_NEUFS', 'Sacs de jute neufs', 'SACS', 'SAC', 100),
    ('BACHES', 'Bâches', 'BACHES', 'UNITE', 10),
    ('PRODUITS_PHYTO', 'Produits phytosanitaires', 'PRODUITS_PHYTO', 'BIDON', 5)
ON CONFLICT (code) DO NOTHING;

INSERT INTO materiels_emballages (code, libelle, categorie, unite, seuil_alerte)
SELECT DISTINCT
       upper(replace(trim(designation_materiel), ' ', '_')),
       initcap(replace(lower(designation_materiel), '_', ' ')),
       CASE
           WHEN designation_materiel ILIKE '%SAC%' THEN 'SACS'
           WHEN designation_materiel ILIKE '%BACH%' THEN 'BACHES'
           WHEN designation_materiel ILIKE '%PHYTO%' THEN 'PRODUITS_PHYTO'
           ELSE 'AUTRE'
       END,
       CASE
           WHEN designation_materiel ILIKE '%SAC%' THEN 'SAC'
           ELSE 'UNITE'
       END,
       0
FROM fiches_stock_materiel_emballages
WHERE designation_materiel IS NOT NULL
ON CONFLICT (code) DO NOTHING;

INSERT INTO fiches_stock_materiel (numero_fiche, comptoir_id, date_fiche, statut,
                                   observations, cree_le)
SELECT 'FSME-HIST-' || LPAD(f.id::text, 5, '0'),
       f.comptoir_id,
       COALESCE(f.cree_le::date, CURRENT_DATE),
       'VALIDEE',
       'Fiche historique reprise lors de la migration',
       f.cree_le
FROM fiches_stock_materiel_emballages f
WHERE NOT EXISTS (
    SELECT 1 FROM fiches_stock_materiel x
    WHERE x.numero_fiche = 'FSME-HIST-' || LPAD(f.id::text, 5, '0')
);

INSERT INTO lignes_fiche_stock_materiel (fiche_id, materiel_id, stock_initial,
                                         entrees, sorties, stock_physique, ecart,
                                         stock_final, reference_doc)
SELECT fiche.id,
       materiel.id,
       GREATEST(0, f.stock_emballages - COALESCE(f.entrees_emballages, 0)
           + COALESCE(f.sorties_emballages, 0)),
       COALESCE(f.entrees_emballages, 0),
       COALESCE(f.sorties_emballages, 0),
       f.stock_emballages,
       0,
       f.stock_emballages,
       f.reference_doc
FROM fiches_stock_materiel_emballages f
JOIN fiches_stock_materiel fiche
    ON fiche.numero_fiche = 'FSME-HIST-' || LPAD(f.id::text, 5, '0')
JOIN materiels_emballages materiel
    ON materiel.code = upper(replace(trim(f.designation_materiel), ' ', '_'))
WHERE NOT EXISTS (
    SELECT 1 FROM lignes_fiche_stock_materiel l
    WHERE l.fiche_id = fiche.id
      AND l.materiel_id = materiel.id
);

DROP TABLE IF EXISTS fiches_stock_materiel_emballages;

INSERT INTO permissions (code, libelle, module) VALUES
    ('EMBALLAGE_GERER', 'Gérer les fiches de stock du matériel d''emballage', 'STOCK'),
    ('EMBALLAGE_LIRE', 'Consulter les fiches de stock du matériel d''emballage', 'STOCK')
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('EMBALLAGE_GERER', 'EMBALLAGE_LIRE')
WHERE r.code IN ('ADMIN', 'DCT', 'CHEF_COMPTOIR')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code = 'EMBALLAGE_LIRE'
WHERE r.code IN ('DGSA', 'SGS', 'DPL', 'AGENT_COLLECTEUR', 'CONSULTATION')
ON CONFLICT DO NOTHING;
