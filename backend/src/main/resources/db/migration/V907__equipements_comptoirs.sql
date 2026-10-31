-- Parc d'équipements durables affectés aux comptoirs.
-- Les tablettes deviennent un sous-type d'équipement (tablette_devices).
-- Les consommables (sacs, bâches, produits phyto) restent dans les fiches de stock.

CREATE TABLE IF NOT EXISTS equipements (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    type VARCHAR(30) NOT NULL,
    designation VARCHAR(150) NOT NULL,
    numero_serie VARCHAR(100) UNIQUE,
    marque VARCHAR(100),
    modele VARCHAR(100),
    etat VARCHAR(30) NOT NULL DEFAULT 'BON',
    statut VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE',
    date_acquisition DATE,
    valeur_cfa DOUBLE PRECISION,
    observation TEXT,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_equipements_type ON equipements (type);
CREATE INDEX IF NOT EXISTS idx_equipements_statut ON equipements (statut);

CREATE TABLE IF NOT EXISTS affectations_equipement (
    id BIGSERIAL PRIMARY KEY,
    equipement_id BIGINT NOT NULL REFERENCES equipements (id) ON DELETE CASCADE,
    comptoir_id BIGINT NOT NULL REFERENCES comptoirs (id) ON DELETE RESTRICT,
    utilisateur_id BIGINT REFERENCES utilisateurs (id) ON DELETE SET NULL,
    date_debut TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_fin TIMESTAMP,
    motif VARCHAR(255),
    affecte_par_id BIGINT REFERENCES utilisateurs (id) ON DELETE SET NULL,
    retire_par_id BIGINT REFERENCES utilisateurs (id) ON DELETE SET NULL,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_affectation_equipement_active
    ON affectations_equipement (equipement_id)
    WHERE date_fin IS NULL;

CREATE INDEX IF NOT EXISTS idx_affectations_equipement_comptoir
    ON affectations_equipement (comptoir_id)
    WHERE date_fin IS NULL;

CREATE INDEX IF NOT EXISTS idx_affectations_equipement_utilisateur
    ON affectations_equipement (utilisateur_id)
    WHERE date_fin IS NULL;

-- Reprise des tablettes existantes comme équipements de type TABLETTE.
ALTER TABLE tablette_devices
    ADD COLUMN IF NOT EXISTS equipement_id BIGINT;

INSERT INTO equipements (code, type, designation, numero_serie, modele, etat, statut, observation)
SELECT 'TAB-' || LPAD(t.id::text, 6, '0'),
       'TABLETTE',
       COALESCE(NULLIF(t.modele, ''), 'Tablette ' || t.numero_serie),
       t.numero_serie,
       t.modele,
       'BON',
       CASE
           WHEN t.statut IN ('MAINTENANCE', 'VERROUILLEE') THEN t.statut
           ELSE 'DISPONIBLE'
       END,
       'Reprise de la tablette ' || t.numero_serie
FROM tablette_devices t
WHERE t.equipement_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM equipements e
      WHERE e.numero_serie = t.numero_serie
  );

UPDATE tablette_devices t
SET equipement_id = e.id
FROM equipements e
WHERE t.equipement_id IS NULL
  AND e.type = 'TABLETTE'
  AND e.numero_serie = t.numero_serie;

INSERT INTO affectations_equipement (equipement_id, comptoir_id, utilisateur_id, date_debut, motif)
SELECT t.equipement_id,
       u.comptoir_id,
       u.id,
       COALESCE(t.date_derniere_synchro, t.cree_le, CURRENT_TIMESTAMP),
       'Reprise de l''affectation existante'
FROM tablette_devices t
JOIN utilisateurs u ON u.id = t.utilisateur_id
WHERE t.equipement_id IS NOT NULL
  AND u.comptoir_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM affectations_equipement a
      WHERE a.equipement_id = t.equipement_id
        AND a.date_fin IS NULL
  );

UPDATE equipements e
SET statut = 'AFFECTE'
WHERE EXISTS (
    SELECT 1 FROM affectations_equipement a
    WHERE a.equipement_id = e.id
      AND a.date_fin IS NULL
);

ALTER TABLE tablette_devices
    ALTER COLUMN equipement_id SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_tablette_equipement
    ON tablette_devices (equipement_id);

ALTER TABLE tablette_devices
    DROP CONSTRAINT IF EXISTS tablette_devices_equipement_id_fkey;

ALTER TABLE tablette_devices
    ADD CONSTRAINT tablette_devices_equipement_id_fkey
        FOREIGN KEY (equipement_id) REFERENCES equipements (id) ON DELETE CASCADE;

ALTER TABLE tablette_devices DROP COLUMN IF EXISTS utilisateur_id;
ALTER TABLE tablette_devices DROP COLUMN IF EXISTS numero_serie;
ALTER TABLE tablette_devices DROP COLUMN IF EXISTS modele;
ALTER TABLE tablette_devices DROP COLUMN IF EXISTS statut;
ALTER TABLE tablette_devices DROP COLUMN IF EXISTS cree_le;

-- Permissions du parc d'équipements.
INSERT INTO permissions (code, libelle, module) VALUES
    ('EQUIPEMENT_GERER', 'Gérer le parc d''équipements et les affectations', 'EQUIPEMENT'),
    ('EQUIPEMENT_LIRE', 'Consulter le parc d''équipements', 'EQUIPEMENT')
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('EQUIPEMENT_GERER', 'EQUIPEMENT_LIRE')
WHERE r.code IN ('ADMIN', 'DCT')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code = 'EQUIPEMENT_LIRE'
WHERE r.code IN ('DGSA', 'SGS', 'DPL', 'CHEF_COMPTOIR', 'AGENT_COLLECTEUR', 'CONSULTATION')
ON CONFLICT DO NOTHING;
