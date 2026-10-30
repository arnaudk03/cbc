CREATE TABLE bebc_lignes_enlevement (
    id BIGSERIAL PRIMARY KEY,
    bordereau_enlevement_id BIGINT NOT NULL REFERENCES bordereaux_enlevement_bebc(id) ON DELETE CASCADE,
    speculation_id BIGINT NOT NULL REFERENCES speculations(id) ON DELETE RESTRICT,
    nombre_sacs INTEGER NOT NULL CHECK (nombre_sacs > 0),
    colisage_kg DOUBLE PRECISION NOT NULL CHECK (colisage_kg > 0),
    quantite_kg DOUBLE PRECISION NOT NULL CHECK (quantite_kg > 0),
    CONSTRAINT uk_bebc_ligne_speculation UNIQUE (bordereau_enlevement_id, speculation_id)
);

CREATE INDEX idx_bebc_lignes_bordereau ON bebc_lignes_enlevement (bordereau_enlevement_id);
CREATE INDEX idx_bebc_lignes_speculation ON bebc_lignes_enlevement (speculation_id);
