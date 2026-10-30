-- =============================================================================
-- PROJET COLLECTE BORD CHAMP (CBC) - SONAGESS
-- MIGRATION INITIALE FLYWAY V1: SCHEMA RELATIONNEL GLOBAL
-- Standard: PostgreSQL 14+, DGTD / DCSI / ANPTIC
-- =============================================================================

-- Extension UUID pour les identifiants territoriaux
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================================================
-- 1. MODULE SÉCURITÉ & HABILITATIONS (RBAC, 2FA, AUDIT)
-- =============================================================================

CREATE TABLE IF NOT EXISTS utilisateurs (
    id BIGSERIAL PRIMARY KEY,
    matricule VARCHAR(50) UNIQUE NOT NULL,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    telephone VARCHAR(30) UNIQUE NOT NULL,
    mot_de_passe_hash VARCHAR(255) NOT NULL,
    actif BOOLEAN DEFAULT TRUE,
    compte_bloque BOOLEAN DEFAULT FALSE,
    tentatives_echouees INT DEFAULT 0,
    date_derniere_connexion TIMESTAMP,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modifie_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    libelle VARCHAR(100) NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS permissions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) UNIQUE NOT NULL,
    libelle VARCHAR(150) NOT NULL,
    module VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS utilisateur_roles (
    utilisateur_id BIGINT REFERENCES utilisateurs(id) ON DELETE CASCADE,
    role_id BIGINT REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (utilisateur_id, role_id)
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS two_factor_auth (
    id BIGSERIAL PRIMARY KEY,
    utilisateur_id BIGINT UNIQUE REFERENCES utilisateurs(id) ON DELETE CASCADE,
    active BOOLEAN DEFAULT FALSE,
    secret_key_totp VARCHAR(100),
    dernier_code_otp VARCHAR(10),
    date_expiration_otp TIMESTAMP,
    type_2fa VARCHAR(20) DEFAULT 'SMS' -- 'SMS', 'TOTP', 'EMAIL'
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    utilisateur_id BIGINT REFERENCES utilisateurs(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    date_expiration TIMESTAMP NOT NULL,
    revoque BOOLEAN DEFAULT FALSE,
    adresse_ip_creation VARCHAR(45),
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tablette_devices (
    id BIGSERIAL PRIMARY KEY,
    utilisateur_id BIGINT REFERENCES utilisateurs(id) ON DELETE SET NULL,
    numero_serie VARCHAR(100) UNIQUE NOT NULL,
    imei VARCHAR(50) UNIQUE,
    modele VARCHAR(100),
    cle_chiffrement_locale VARCHAR(255),
    statut VARCHAR(30) DEFAULT 'ACTIVE', -- 'ACTIVE', 'VERROUILLEE', 'MAINTENANCE'
    date_derniere_synchro TIMESTAMP,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS journal_audit (
    id BIGSERIAL PRIMARY KEY,
    utilisateur_id BIGINT REFERENCES utilisateurs(id) ON DELETE SET NULL,
    matricule_utilisateur VARCHAR(50),
    action VARCHAR(100) NOT NULL,
    entite_concernee VARCHAR(100),
    entite_id BIGINT,
    date_action TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    adresse_ip VARCHAR(45),
    user_agent TEXT,
    resultat VARCHAR(20) DEFAULT 'SUCCES', -- 'SUCCES', 'ECHEC'
    details TEXT
);

-- =============================================================================
-- 2. MODULE DÉCOUPAGE TERRITORIAL OFFICIEL DU BURKINA FASO (CLOSURE TABLE)
-- =============================================================================

CREATE TABLE IF NOT EXISTS localities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(150) NOT NULL,
    type VARCHAR(30) NOT NULL, -- 'COUNTRY', 'REGION', 'PROVINCE', 'COMMUNE', 'ARRONDISSEMENT', 'SECTOR', 'VILLAGE'
    parent_id UUID REFERENCES localities(id) ON DELETE RESTRICT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS locality_hierarchies (
    ancestor_id UUID REFERENCES localities(id) ON DELETE CASCADE,
    descendant_id UUID REFERENCES localities(id) ON DELETE CASCADE,
    depth INT NOT NULL,
    PRIMARY KEY (ancestor_id, descendant_id)
);

CREATE INDEX IF NOT EXISTS idx_locality_type ON localities(type);
CREATE INDEX IF NOT EXISTS idx_hierarchy_ancestor ON locality_hierarchies(ancestor_id);
CREATE INDEX IF NOT EXISTS idx_hierarchy_descendant ON locality_hierarchies(descendant_id);

CREATE TABLE IF NOT EXISTS perimetres_intervention (
    id BIGSERIAL PRIMARY KEY,
    utilisateur_id BIGINT REFERENCES utilisateurs(id) ON DELETE CASCADE,
    locality_id UUID REFERENCES localities(id) ON DELETE CASCADE,
    type_perimetre VARCHAR(30) NOT NULL, -- 'NATIONAL', 'REGION', 'PROVINCE', 'COMMUNE'
    libelle_zone VARCHAR(150),
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 3. MODULE RÉFÉRENTIELS & CAMPAGNES AGRICOLES
-- =============================================================================

CREATE TABLE IF NOT EXISTS campagnes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL, -- ex: 'CAMP-2025-2026'
    libelle VARCHAR(150) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    objectif_total_tonnes DOUBLE PRECISION DEFAULT 0.0,
    statut VARCHAR(30) DEFAULT 'OUVERTE', -- 'PLANIFIEE', 'OUVERTE', 'CLOTUREE'
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS speculations (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL, -- ex: 'MAIS_BLANC', 'RIZ_PADDY', 'NIEBE'
    nom VARCHAR(100) NOT NULL,
    seuil_humidite_max DOUBLE PRECISION NOT NULL,
    seuil_impurete_max DOUBLE PRECISION NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS objectifs_campagne (
    id BIGSERIAL PRIMARY KEY,
    campagne_id BIGINT NOT NULL REFERENCES campagnes(id) ON DELETE CASCADE,
    speculation_id BIGINT NOT NULL REFERENCES speculations(id) ON DELETE RESTRICT,
    locality_id UUID REFERENCES localities(id) ON DELETE SET NULL, -- NULL = National, renseigné = Régional
    objectif_tonnes DOUBLE PRECISION NOT NULL,
    budget_previsionnel_cfa DOUBLE PRECISION DEFAULT 0.0,
    seuil_alerte_pct DOUBLE PRECISION DEFAULT 80.0,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS prix_garanties (
    id BIGSERIAL PRIMARY KEY,
    campagne_id BIGINT NOT NULL REFERENCES campagnes(id) ON DELETE CASCADE,
    speculation_id BIGINT NOT NULL REFERENCES speculations(id) ON DELETE RESTRICT,
    locality_id UUID REFERENCES localities(id) ON DELETE SET NULL, -- NULL = National
    prix_unitaire_kg DOUBLE PRECISION NOT NULL,
    date_effet DATE NOT NULL,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS comptoirs (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    nom VARCHAR(150) NOT NULL,
    type VARCHAR(50) DEFAULT 'FIXE', -- 'FIXE', 'MOBILE'
    locality_id UUID NOT NULL REFERENCES localities(id) ON DELETE RESTRICT,
    latitude_gps DOUBLE PRECISION,
    longitude_gps DOUBLE PRECISION,
    capacite_stockage_kg DOUBLE PRECISION DEFAULT 50000.0,
    actif BOOLEAN DEFAULT TRUE,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS producteurs (
    id BIGSERIAL PRIMARY KEY,
    nom_prenom VARCHAR(150) NOT NULL,
    telephone_mobile_money VARCHAR(30) NOT NULL,
    operateur_mobile VARCHAR(30) NOT NULL, -- 'SANK', 'CORIS', 'MOOV', 'LIGDICASH'
    cnib_numero VARCHAR(50) NOT NULL,
    cnib_date_delivrance DATE,
    cnib_lieu_delivrance VARCHAR(100),
    locality_id UUID REFERENCES localities(id) ON DELETE SET NULL,
    adresse_precision TEXT,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 4. MODULE COLLECTE BORD CHAMP & CONTRÔLE QUALITÉ
-- =============================================================================

CREATE TABLE IF NOT EXISTS collectes_bord_champ (
    id BIGSERIAL PRIMARY KEY,
    reference_lot VARCHAR(60) UNIQUE NOT NULL,
    campagne_id BIGINT NOT NULL REFERENCES campagnes(id) ON DELETE RESTRICT,
    comptoir_id BIGINT NOT NULL REFERENCES comptoirs(id) ON DELETE RESTRICT,
    producteur_id BIGINT NOT NULL REFERENCES producteurs(id) ON DELETE RESTRICT,
    speculation_id BIGINT NOT NULL REFERENCES speculations(id) ON DELETE RESTRICT,
    agent_id BIGINT NOT NULL REFERENCES utilisateurs(id) ON DELETE RESTRICT,
    date_heure_collecte TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    statut VARCHAR(30) DEFAULT 'SYNCHRONISEE', -- 'ENREGISTREE', 'SYNCHRONISEE', 'EXAMINEE_SCP', 'VALIDEE_DCT', 'REJETEE'
    latitude_gps DOUBLE PRECISION,
    longitude_gps DOUBLE PRECISION,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS controles_qualite (
    id BIGSERIAL PRIMARY KEY,
    collecte_id BIGINT UNIQUE NOT NULL REFERENCES collectes_bord_champ(id) ON DELETE CASCADE,
    taux_humidite DOUBLE PRECISION NOT NULL,
    taux_impuretes DOUBLE PRECISION NOT NULL,
    grains_avaries DOUBLE PRECISION DEFAULT 0.0,
    conforme BOOLEAN NOT NULL,
    decision_agent VARCHAR(30) NOT NULL,
    motif_rejet TEXT,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS fiches_pesee_fp (
    id BIGSERIAL PRIMARY KEY,
    collecte_id BIGINT UNIQUE NOT NULL REFERENCES collectes_bord_champ(id) ON DELETE CASCADE,
    numero_fiche VARCHAR(50) UNIQUE NOT NULL,
    direction_regionale VARCHAR(100),
    direction_provinciale VARCHAR(100),
    site_comptoir VARCHAR(100),
    nombre_sacs INT NOT NULL,
    poids_brut_total_kg DOUBLE PRECISION NOT NULL,
    tare_emballage_kg DOUBLE PRECISION NOT NULL,
    poids_net_total_kg DOUBLE PRECISION NOT NULL,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bordereaux_reception_brbc (
    id BIGSERIAL PRIMARY KEY,
    collecte_id BIGINT UNIQUE NOT NULL REFERENCES collectes_bord_champ(id) ON DELETE CASCADE,
    numero_brbc VARCHAR(50) UNIQUE NOT NULL,
    date_emission TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    quantite_kg DOUBLE PRECISION NOT NULL,
    prix_unitaire_applique DOUBLE PRECISION NOT NULL,
    montant_net_payer_cfa DOUBLE PRECISION NOT NULL,
    montant_en_lettres TEXT NOT NULL,
    mode_paiement VARCHAR(30) DEFAULT 'MOBILE_MONEY', -- 'MOBILE_MONEY', 'ESPECES', 'VIREMENT'
    observations_receptionnaire TEXT,
    signature_producteur_token VARCHAR(255),
    signature_agent_token VARCHAR(255),
    statut_validation VARCHAR(30) DEFAULT 'EN_ATTENTE_EXAMEN',
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 5. MODULE PAIEMENT & FINANCES
-- =============================================================================

CREATE TABLE IF NOT EXISTS ordres_paiement (
    id BIGSERIAL PRIMARY KEY,
    bordereau_reception_id BIGINT UNIQUE NOT NULL REFERENCES bordereaux_reception_brbc(id) ON DELETE RESTRICT,
    reference_transaction VARCHAR(100) UNIQUE NOT NULL,
    montant_paye_cfa DOUBLE PRECISION NOT NULL,
    operateur VARCHAR(30) NOT NULL, -- 'SANK', 'CORIS', 'MOOV', 'LIGDICASH'
    telephone_destination VARCHAR(30) NOT NULL,
    initiateur_id BIGINT NOT NULL REFERENCES utilisateurs(id),
    validateur_2fa_id BIGINT REFERENCES utilisateurs(id),
    date_initiation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_validation_2fa TIMESTAMP,
    statut VARCHAR(30) DEFAULT 'INITIE', -- 'INITIE', 'VALIDE_2FA', 'ENVOYE_OPERATEUR', 'CONFIRME', 'ECHEC'
    message_retour_operateur TEXT,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 6. MODULE GESTION DES STOCKS, EMBALLAGES & LOGISTIQUE (TRANSFERTS)
-- =============================================================================

CREATE TABLE IF NOT EXISTS fiches_stock_journalieres_fsj (
    id BIGSERIAL PRIMARY KEY,
    comptoir_id BIGINT NOT NULL REFERENCES comptoirs(id) ON DELETE RESTRICT,
    date_fiche DATE NOT NULL,
    nature_operation VARCHAR(50) NOT NULL,
    entree_sacs INT DEFAULT 0,
    entree_poids_kg DOUBLE PRECISION DEFAULT 0.0,
    entree_valeur_cfa DOUBLE PRECISION DEFAULT 0.0,
    sortie_sacs INT DEFAULT 0,
    sortie_poids_kg DOUBLE PRECISION DEFAULT 0.0,
    sortie_valeur_cfa DOUBLE PRECISION DEFAULT 0.0,
    stock_final_sacs INT NOT NULL,
    stock_final_poids_kg DOUBLE PRECISION NOT NULL,
    stock_final_valeur_cfa DOUBLE PRECISION NOT NULL,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS fiches_stock_materiel_emballages (
    id BIGSERIAL PRIMARY KEY,
    comptoir_id BIGINT NOT NULL REFERENCES comptoirs(id) ON DELETE RESTRICT,
    designation_materiel VARCHAR(100) NOT NULL, -- 'SACS_JUTE_NEUFS', 'BACHES', 'PRODUITS_PHYTO'
    reference_doc VARCHAR(50),
    entrees_emballages INT DEFAULT 0,
    sorties_emballages INT DEFAULT 0,
    stock_emballages INT NOT NULL,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS magasins_centraux (
    id BIGSERIAL PRIMARY KEY,
    code_magasin VARCHAR(50) UNIQUE NOT NULL,
    nom VARCHAR(150) NOT NULL,
    ville VARCHAR(100) NOT NULL,
    capacite_totale_tonnes DOUBLE PRECISION NOT NULL,
    stock_actuel_tonnes DOUBLE PRECISION DEFAULT 0.0,
    actif BOOLEAN DEFAULT TRUE,
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bordereaux_enlevement_bebc (
    id BIGSERIAL PRIMARY KEY,
    numero_bebc VARCHAR(50) UNIQUE NOT NULL,
    comptoir_depart_id BIGINT NOT NULL REFERENCES comptoirs(id) ON DELETE RESTRICT,
    direction VARCHAR(100),
    localite VARCHAR(100),
    magasin_depart VARCHAR(100),
    beneficiaire VARCHAR(150),
    objet_enlevement VARCHAR(150),
    colisage VARCHAR(100),
    nombre_sacs INT NOT NULL,
    tonnage_enleve_kg DOUBLE PRECISION NOT NULL,
    nom_transporteur VARCHAR(150) NOT NULL,
    numero_vehicule VARCHAR(50) NOT NULL,
    signature_transporteur_token VARCHAR(255),
    statut VARCHAR(30) DEFAULT 'EN_TRANSIT', -- 'EN_TRANSIT', 'LIVRE', 'ECART_CONSTATE'
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bordereaux_reception_magasin (
    id BIGSERIAL PRIMARY KEY,
    bordereau_enlevement_id BIGINT UNIQUE NOT NULL REFERENCES bordereaux_enlevement_bebc(id) ON DELETE RESTRICT,
    magasin_central_id BIGINT NOT NULL REFERENCES magasins_centraux(id) ON DELETE RESTRICT,
    magasinier_id BIGINT NOT NULL REFERENCES utilisateurs(id) ON DELETE RESTRICT,
    numero_reception VARCHAR(50) UNIQUE NOT NULL,
    date_dechargement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    nombre_sacs_recus INT NOT NULL,
    tonnage_decharge_kg DOUBLE PRECISION NOT NULL,
    ecart_poids_kg DOUBLE PRECISION DEFAULT 0.0,
    statut_conformite VARCHAR(30) DEFAULT 'CONFORME', -- 'CONFORME', 'MANQUANT', 'EXCEDENT'
    cree_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 7. MODULE NOTIFICATIONS MULTICANALES (SMS, PUSH, EMAIL, IN-APP)
-- =============================================================================

CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    canal VARCHAR(20) NOT NULL, -- 'SMS', 'PUSH_MOBILE', 'EMAIL', 'IN_APP'
    type_notification VARCHAR(50) NOT NULL, -- 'CONFIRMATION_BRBC', 'PAIEMENT_MOBILE_RECU', 'REJET_DOSSIER_COLLECTE', etc.
    destinataire_contact VARCHAR(150) NOT NULL,
    titre VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    statut VARCHAR(20) DEFAULT 'EN_ATTENTE', -- 'EN_ATTENTE', 'ENVOYE', 'DISTRIBUE', 'ECHEC', 'LU'
    utilisateur_id BIGINT REFERENCES utilisateurs(id) ON DELETE SET NULL,
    producteur_id BIGINT REFERENCES producteurs(id) ON DELETE SET NULL,
    collecte_id BIGINT REFERENCES collectes_bord_champ(id) ON DELETE SET NULL,
    ordre_paiement_id BIGINT REFERENCES ordres_paiement(id) ON DELETE SET NULL,
    bordereau_enlevement_id BIGINT REFERENCES bordereaux_enlevement_bebc(id) ON DELETE SET NULL,
    reference_externe_gateway VARCHAR(150),
    tentatives_envoi INT DEFAULT 0,
    erreur_message TEXT,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_envoi TIMESTAMP,
    date_lecture TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notification_destinataire ON notifications(destinataire_contact);
CREATE INDEX IF NOT EXISTS idx_notification_statut ON notifications(statut);
