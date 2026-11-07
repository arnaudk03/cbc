ALTER TABLE bordereaux_reception_magasin
    ADD COLUMN IF NOT EXISTS ecart_sacs INTEGER NOT NULL DEFAULT 0;

ALTER TABLE bebc_lignes_enlevement
    ADD COLUMN IF NOT EXISTS nombre_sacs_recus INTEGER;

ALTER TABLE bebc_lignes_enlevement
    ADD COLUMN IF NOT EXISTS quantite_recue_kg DOUBLE PRECISION;

UPDATE bordereaux_reception_magasin r
SET ecart_sacs = r.nombre_sacs_recus - b.nombre_sacs
FROM bordereaux_enlevement_bebc b
WHERE b.id = r.bordereau_enlevement_id;

UPDATE bebc_lignes_enlevement l
SET nombre_sacs_recus = r.nombre_sacs_recus,
    quantite_recue_kg = ROUND((r.nombre_sacs_recus * l.colisage_kg)::numeric, 2)::double precision
FROM bordereaux_reception_magasin r
WHERE l.bordereau_enlevement_id = r.bordereau_enlevement_id
  AND (SELECT COUNT(*) FROM bebc_lignes_enlevement l2
       WHERE l2.bordereau_enlevement_id = r.bordereau_enlevement_id) = 1;
