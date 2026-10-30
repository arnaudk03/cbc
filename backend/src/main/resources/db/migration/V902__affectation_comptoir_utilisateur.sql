-- Affectation des utilisateurs terrain à un comptoir de rattachement.
-- Un agent collecteur / chef de comptoir ne peut consulter et saisir que les
-- collectes de son comptoir. Les autres profils conservent un accès global.
ALTER TABLE utilisateurs
    ADD COLUMN IF NOT EXISTS comptoir_id BIGINT;

ALTER TABLE utilisateurs
    DROP CONSTRAINT IF EXISTS fk_utilisateurs_comptoir;

ALTER TABLE utilisateurs
    ADD CONSTRAINT fk_utilisateurs_comptoir
        FOREIGN KEY (comptoir_id) REFERENCES comptoirs (id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_utilisateurs_comptoir
    ON utilisateurs (comptoir_id);
