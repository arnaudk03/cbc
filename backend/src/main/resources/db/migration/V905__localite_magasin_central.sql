ALTER TABLE magasins_centraux
    ADD COLUMN locality_id UUID;

ALTER TABLE magasins_centraux
    ADD CONSTRAINT fk_magasins_centraux_localite
        FOREIGN KEY (locality_id) REFERENCES localities (id) ON DELETE SET NULL;

CREATE INDEX idx_magasins_centraux_localite ON magasins_centraux (locality_id);

UPDATE magasins_centraux m
SET locality_id = (
    SELECT l.id
    FROM localities l
    WHERE l.type = 'COMMUNE'
      AND lower(l.name) = lower(m.ville)
    ORDER BY l.code
    LIMIT 1
)
WHERE m.locality_id IS NULL;
