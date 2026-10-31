-- V909 : Support synchronisation hors-ligne pour application mobile Flutter
ALTER TABLE bordereaux_reception_brbc
    ADD COLUMN IF NOT EXISTS client_sync_id VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_brbc_client_sync_id
    ON bordereaux_reception_brbc (client_sync_id)
    WHERE client_sync_id IS NOT NULL;

ALTER TABLE collectes_bord_champ
    ADD COLUMN IF NOT EXISTS client_ligne_id VARCHAR(100);

