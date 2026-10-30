-- =============================================================================
-- V2 : Compléments de schéma nécessaires aux workflows de validation
-- Projet CBC SONAGESS - Octobre 2026
-- =============================================================================

-- Circuit d'examen SCP et de validation DCT de la collecte
ALTER TABLE collectes_bord_champ
    ADD COLUMN IF NOT EXISTS motif_rejet TEXT,
    ADD COLUMN IF NOT EXISTS examinateur_id BIGINT REFERENCES utilisateurs(id),
    ADD COLUMN IF NOT EXISTS date_examen TIMESTAMP,
    ADD COLUMN IF NOT EXISTS validateur_id BIGINT REFERENCES utilisateurs(id),
    ADD COLUMN IF NOT EXISTS date_validation TIMESTAMP;

-- Approbation préalable des prix garantis par la Direction (invariant métier)
ALTER TABLE prix_garanties
    ADD COLUMN IF NOT EXISTS statut VARCHAR(30) DEFAULT 'EN_ATTENTE_APPROBATION',
    ADD COLUMN IF NOT EXISTS approuve_par BIGINT REFERENCES utilisateurs(id),
    ADD COLUMN IF NOT EXISTS date_approbation TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_prix_garanties_lookup
    ON prix_garanties (campagne_id, speculation_id, locality_id, date_effet);

-- Traçabilité de l'exécution des paiements mobile money
ALTER TABLE ordres_paiement
    ADD COLUMN IF NOT EXISTS date_confirmation TIMESTAMP,
    ADD COLUMN IF NOT EXISTS reference_operateur VARCHAR(100),
    ADD COLUMN IF NOT EXISTS motif_rejet TEXT;

-- Dates logistiques du bordereau d'enlèvement
ALTER TABLE bordereaux_enlevement_bebc
    ADD COLUMN IF NOT EXISTS date_enlevement TIMESTAMP,
    ADD COLUMN IF NOT EXISTS date_arrivee_prevue DATE;

CREATE INDEX IF NOT EXISTS idx_collectes_statut ON collectes_bord_champ (statut);
CREATE INDEX IF NOT EXISTS idx_collectes_campagne ON collectes_bord_champ (campagne_id);
CREATE INDEX IF NOT EXISTS idx_collectes_comptoir ON collectes_bord_champ (comptoir_id);
CREATE INDEX IF NOT EXISTS idx_paiements_statut ON ordres_paiement (statut);
CREATE INDEX IF NOT EXISTS idx_bebc_statut ON bordereaux_enlevement_bebc (statut);

-- Magasin central de destination du transfert
ALTER TABLE bordereaux_enlevement_bebc
    ADD COLUMN IF NOT EXISTS magasin_central_destination_id BIGINT REFERENCES magasins_centraux(id);
