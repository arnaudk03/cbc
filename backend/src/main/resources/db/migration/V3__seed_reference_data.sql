-- =============================================================================
-- V3 : Données de référence système
-- Projet CBC SONAGESS
-- Rôles, permissions et découpage territorial officiel uniquement.
-- Les données de démonstration sont chargées par le profil dev (db/demo).
-- =============================================================================

-- ------------------------------------------------------------------ Rôles
INSERT INTO roles (code, libelle, description) VALUES
    ('ADMIN', 'Administrateur système', 'Administration complète de la plateforme'),
    ('DCT', 'Directeur de la Collecte et de la Transformation', 'Validation finale des collectes et des prix'),
    ('SCP', 'Service Contrôle et Qualité', 'Examen des collectes et contrôles qualité'),
    ('DFC_DIRECTEUR', 'Directeur Financier et Comptable', 'Supervision financière et budgétaire'),
    ('SFB_INITIATEUR', 'SFB Initiateur', 'Initiation des ordres de paiement'),
    ('SFB_CHEF', 'Chef Service Financier et Budgétaire', 'Validation 2FA des paiements'),
    ('COMPTABILITE', 'Comptabilité', 'Suivi comptable des opérations'),
    ('DGSA', 'Directeur Général des Services Administratifs', 'Supervision des stocks et logistique'),
    ('SGS', 'Service Gestion des Stocks', 'Gestion des transferts et réceptions'),
    ('MAGASINIER', 'Magasinier', 'Réception physique des marchandises'),
    ('AGENT_COLLECTEUR', 'Agent collecteur', 'Saisie des collectes bord champ'),
    ('CHEF_COMPTOIR', 'Chef de comptoir', 'Tenue des fiches de stock du comptoir'),
    ('DPL', 'Direction de la Production et de la Logistique', 'Planification logistique'),
    ('CONSULTATION', 'Consultation', 'Accès en lecture seule aux tableaux de bord')
ON CONFLICT (code) DO NOTHING;

-- ------------------------------------------------------------ Permissions
INSERT INTO permissions (code, libelle, module) VALUES
    ('COLLECTE_SAISIR', 'Saisir une collecte bord champ', 'COLLECTE'),
    ('COLLECTE_SYNCHRONISER', 'Synchroniser les collectes terrain', 'COLLECTE'),
    ('COLLECTE_EXAMINER', 'Examiner une collecte (SCP)', 'COLLECTE'),
    ('COLLECTE_VALIDER', 'Valider une collecte et émettre le BRBC', 'COLLECTE'),
    ('COLLECTE_REJETER', 'Rejeter une collecte', 'COLLECTE'),
    ('PAIEMENT_INITIER', 'Initier un ordre de paiement', 'PAIEMENT'),
    ('PAIEMENT_VALIDER_2FA', 'Valider un paiement par 2FA', 'PAIEMENT'),
    ('PAIEMENT_REJETER', 'Rejeter un ordre de paiement', 'PAIEMENT'),
    ('STOCK_TENIR_FSJ', 'Tenir la fiche de stock journalière', 'STOCK'),
    ('STOCK_TRANSFERT_CREER', 'Créer un bordereau d''enlèvement', 'STOCK'),
    ('STOCK_RECEPTIONNER', 'Réceptionner un transfert en magasin', 'STOCK'),
    ('PARAMETRAGE_LIRE', 'Consulter le paramétrage', 'PARAMETRAGE'),
    ('PARAMETRAGE_GERER', 'Gérer le paramétrage', 'PARAMETRAGE'),
    ('PRIX_CREER', 'Proposer un prix garanti', 'PARAMETRAGE'),
    ('PRIX_APPROUVER', 'Approuver un prix garanti', 'PARAMETRAGE'),
    ('CAMPAGNE_GERER', 'Gérer les campagnes', 'PARAMETRAGE'),
    ('COMPTOIR_GERER', 'Gérer les comptoirs', 'PARAMETRAGE'),
    ('PRODUCTEUR_GERER', 'Gérer les producteurs', 'PARAMETRAGE'),
    ('UTILISATEUR_GERER', 'Gérer les utilisateurs', 'SECURITE'),
    ('DASHBOARD_LIRE', 'Consulter le tableau de bord', 'DASHBOARD'),
    ('AUDIT_LIRE', 'Consulter le journal d''audit', 'SECURITE'),
    ('NOTIFICATION_LIRE', 'Consulter ses notifications', 'NOTIFICATION')
ON CONFLICT (code) DO NOTHING;

-- ------------------------------------------------- Liaisons rôle / permission
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'COLLECTE_EXAMINER', 'COLLECTE_VALIDER', 'COLLECTE_REJETER', 'PRIX_APPROUVER',
    'PARAMETRAGE_LIRE', 'DASHBOARD_LIRE', 'AUDIT_LIRE', 'NOTIFICATION_LIRE')
WHERE r.code = 'DCT'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'COLLECTE_EXAMINER', 'COLLECTE_REJETER', 'PARAMETRAGE_LIRE',
    'DASHBOARD_LIRE', 'NOTIFICATION_LIRE')
WHERE r.code = 'SCP'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('PRIX_CREER', 'PARAMETRAGE_LIRE', 'DASHBOARD_LIRE')
WHERE r.code = 'DFC_DIRECTEUR'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('PAIEMENT_INITIER', 'PARAMETRAGE_LIRE', 'NOTIFICATION_LIRE')
WHERE r.code = 'SFB_INITIATEUR'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'PAIEMENT_INITIER', 'PAIEMENT_VALIDER_2FA', 'PAIEMENT_REJETER',
    'DASHBOARD_LIRE', 'NOTIFICATION_LIRE')
WHERE r.code = 'SFB_CHEF'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('PARAMETRAGE_LIRE', 'DASHBOARD_LIRE', 'NOTIFICATION_LIRE')
WHERE r.code = 'COMPTABILITE'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'STOCK_TRANSFERT_CREER', 'DASHBOARD_LIRE', 'PARAMETRAGE_LIRE', 'NOTIFICATION_LIRE')
WHERE r.code = 'DGSA'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'STOCK_TRANSFERT_CREER', 'STOCK_RECEPTIONNER',
    'DASHBOARD_LIRE', 'PARAMETRAGE_LIRE', 'NOTIFICATION_LIRE')
WHERE r.code = 'SGS'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('STOCK_RECEPTIONNER', 'PARAMETRAGE_LIRE', 'NOTIFICATION_LIRE')
WHERE r.code = 'MAGASINIER'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'COLLECTE_SAISIR', 'COLLECTE_SYNCHRONISER', 'PARAMETRAGE_LIRE', 'NOTIFICATION_LIRE')
WHERE r.code = 'AGENT_COLLECTEUR'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'STOCK_TENIR_FSJ', 'STOCK_TRANSFERT_CREER', 'PARAMETRAGE_LIRE', 'NOTIFICATION_LIRE')
WHERE r.code = 'CHEF_COMPTOIR'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'STOCK_TRANSFERT_CREER', 'DASHBOARD_LIRE', 'PARAMETRAGE_LIRE')
WHERE r.code = 'DPL'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('DASHBOARD_LIRE', 'AUDIT_LIRE', 'PARAMETRAGE_LIRE')
WHERE r.code = 'CONSULTATION'
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- Localités
-- Données administratives officielles du Burkina Faso
-- Source : OCHA Burkina Faso / HDX (COD-AB valide au 01/08/2025)
-- 1 pays -> 17 régions -> 47 provinces -> 351 communes -> subdivisions
WITH country AS (
    INSERT INTO localities (id, code, name, type, parent_id, status, created_at, updated_at)
    VALUES (gen_random_uuid(), 'BF', 'Burkina Faso', 'COUNTRY', NULL, 'ACTIVE', NOW(), NOW())
    RETURNING id AS country_id
)

-- =============================================================================
-- COUCHE 2 : 17 RÉGIONS (nouvelles dénominations endogènes)
-- =============================================================================
, regions AS (
    INSERT INTO localities (id, code, name, type, parent_id, status, created_at, updated_at)
    VALUES
        (gen_random_uuid(), 'R_BNK', 'Bankui', 'REGION', (SELECT country_id FROM country), 'ACTIVE', NOW(), NOW()),
        (gen_random_uuid(), 'R_DJR', 'Djôrô', 'REGION', (SELECT country_id FROM country), 'ACTIVE', NOW(), NOW()),
        (gen_random_uuid(), 'R_GLM', 'Goulmou', 'REGION', (SELECT country_id FROM country), 'ACTIVE', NOW(), NOW()),
        (gen_random_uuid(), 'R_GRK', 'Guiriko', 'REGION', (SELECT country_id FROM country), 'ACTIVE', NOW(), NOW()),
        (gen_random_uuid(), 'R_KDG', 'Kadiogo', 'REGION', (SELECT country_id FROM country), 'ACTIVE', NOW(), NOW()),
        (gen_random_uuid(), 'R_KLS', 'Kuilsé', 'REGION', (SELECT country_id FROM country), 'ACTIVE', NOW(), NOW()),
        (gen_random_uuid(), 'R_LTK', 'Liptako', 'REGION', (SELECT country_id FROM country), 'ACTIVE', NOW(), NOW()),
        (gen_random_uuid(), 'R_NDO', 'Nando', 'REGION', (SELECT country_id FROM country), 'ACTIVE', NOW(), NOW()),
        (gen_random_uuid(), 'R_NKB', 'Nakambé', 'REGION', (SELECT country_id FROM country), 'ACTIVE', NOW(), NOW()),
        (gen_random_uuid(), 'R_NZN', 'Nazinon', 'REGION', (SELECT country_id FROM country), 'ACTIVE', NOW(), NOW()),
        (gen_random_uuid(), 'R_OBR', 'Oubri', 'REGION', (SELECT country_id FROM country), 'ACTIVE', NOW(), NOW()),
        (gen_random_uuid(), 'R_SRB', 'Sirba', 'REGION', (SELECT country_id FROM country), 'ACTIVE', NOW(), NOW()),
        (gen_random_uuid(), 'R_SOM', 'Soum', 'REGION', (SELECT country_id FROM country), 'ACTIVE', NOW(), NOW()),
        (gen_random_uuid(), 'R_SRO', 'Sourou', 'REGION', (SELECT country_id FROM country), 'ACTIVE', NOW(), NOW()),
        (gen_random_uuid(), 'R_TNY', 'Tannounyan', 'REGION', (SELECT country_id FROM country), 'ACTIVE', NOW(), NOW()),
        (gen_random_uuid(), 'R_TPA', 'Tapoa', 'REGION', (SELECT country_id FROM country), 'ACTIVE', NOW(), NOW()),
        (gen_random_uuid(), 'R_YDG', 'Yaadga', 'REGION', (SELECT country_id FROM country), 'ACTIVE', NOW(), NOW())
    RETURNING id, code, name
)

-- =============================================================================
-- COUCHE 3 : 47 PROVINCES
-- =============================================================================
, provinces AS (
    INSERT INTO localities (id, code, name, type, parent_id, status, created_at, updated_at)
    SELECT gen_random_uuid(), code, name, 'PROVINCE', region_id, 'ACTIVE', NOW(), NOW()
    FROM (
        VALUES
            -- Région BANKUI (3 provinces)
            ('P_BLM', 'Balé', (SELECT id FROM regions WHERE code = 'R_BNK')),
            ('P_BNW', 'Banwa', (SELECT id FROM regions WHERE code = 'R_BNK')),
            ('P_MOH', 'Mouhoun', (SELECT id FROM regions WHERE code = 'R_BNK')),
            -- Région DJÔRÔ (4 provinces)
            ('P_BGR', 'Bougouriba', (SELECT id FROM regions WHERE code = 'R_DJR')),
            ('P_IOB', 'Ioba', (SELECT id FROM regions WHERE code = 'R_DJR')),
            ('P_NBL', 'Noumbiel', (SELECT id FROM regions WHERE code = 'R_DJR')),
            ('P_PON', 'Poni', (SELECT id FROM regions WHERE code = 'R_DJR')),
            -- Région GOULMOU (2 provinces)
            ('P_GOM', 'Gourma', (SELECT id FROM regions WHERE code = 'R_GLM')),
            ('P_KPG', 'Kompienga', (SELECT id FROM regions WHERE code = 'R_GLM')),
            -- Région GUIRIKO (3 provinces)
            ('P_HOU', 'Houet', (SELECT id FROM regions WHERE code = 'R_GRK')),
            ('P_KND', 'Kénédougou', (SELECT id FROM regions WHERE code = 'R_GRK')),
            ('P_TUY', 'Tuy', (SELECT id FROM regions WHERE code = 'R_GRK')),
            -- Région KADIOGO (1 province)
            ('P_KAD', 'Kadiogo', (SELECT id FROM regions WHERE code = 'R_KDG')),
            -- Région KUILSÉ (3 provinces)
            ('P_BAM', 'Bam', (SELECT id FROM regions WHERE code = 'R_KLS')),
            ('P_NMT', 'Namentenga', (SELECT id FROM regions WHERE code = 'R_KLS')),
            ('P_SBT', 'Sandbondtenga', (SELECT id FROM regions WHERE code = 'R_KLS')),
            -- Région LIPTAKO (3 provinces)
            ('P_OUD', 'Oudalan', (SELECT id FROM regions WHERE code = 'R_LTK')),
            ('P_SEN', 'Séno', (SELECT id FROM regions WHERE code = 'R_LTK')),
            ('P_YGH', 'Yagha', (SELECT id FROM regions WHERE code = 'R_LTK')),
            -- Région NAKAMBÉ (3 provinces)
            ('P_BLG', 'Boulgou', (SELECT id FROM regions WHERE code = 'R_NKB')),
            ('P_KPL', 'Koulpelogo', (SELECT id FROM regions WHERE code = 'R_NKB')),
            ('P_KRT', 'Kourittenga', (SELECT id FROM regions WHERE code = 'R_NKB')),
            -- Région NANDO (4 provinces)
            ('P_BLK', 'Boulkiemdé', (SELECT id FROM regions WHERE code = 'R_NDO')),
            ('P_SNG', 'Sanguié', (SELECT id FROM regions WHERE code = 'R_NDO')),
            ('P_SSL', 'Sissili', (SELECT id FROM regions WHERE code = 'R_NDO')),
            ('P_ZIR', 'Ziro', (SELECT id FROM regions WHERE code = 'R_NDO')),
            -- Région NAZINON (3 provinces)
            ('P_BZG', 'Bazèga', (SELECT id FROM regions WHERE code = 'R_NZN')),
            ('P_NHR', 'Nahouri', (SELECT id FROM regions WHERE code = 'R_NZN')),
            ('P_ZOW', 'Zoundwéogo', (SELECT id FROM regions WHERE code = 'R_NZN')),
            -- Région OUBRI (3 provinces)
            ('P_BSS', 'Bassitenga', (SELECT id FROM regions WHERE code = 'R_OBR')),
            ('P_GZG', 'Ganzourgou', (SELECT id FROM regions WHERE code = 'R_OBR')),
            ('P_KWG', 'Kourwéogo', (SELECT id FROM regions WHERE code = 'R_OBR')),
            -- Région SIRBA (2 provinces)
            ('P_GNG', 'Gnagna', (SELECT id FROM regions WHERE code = 'R_SRB')),
            ('P_KMJ', 'Komondjari', (SELECT id FROM regions WHERE code = 'R_SRB')),
            -- Région SOUM (2 provinces)
            ('P_DGD', 'Djelgodji', (SELECT id FROM regions WHERE code = 'R_SOM')),
            ('P_KRP', 'Karo-Peli', (SELECT id FROM regions WHERE code = 'R_SOM')),
            -- Région SOUROU (3 provinces)
            ('P_KSN', 'Koosin', (SELECT id FROM regions WHERE code = 'R_SRO')),
            ('P_NYL', 'Nayala', (SELECT id FROM regions WHERE code = 'R_SRO')),
            ('P_SRO', 'Sourou', (SELECT id FROM regions WHERE code = 'R_SRO')),
            -- Région TANNOUNYAN (2 provinces)
            ('P_COM', 'Comoé', (SELECT id FROM regions WHERE code = 'R_TNY')),
            ('P_LRB', 'Léraba', (SELECT id FROM regions WHERE code = 'R_TNY')),
            -- Région TAPOA (2 provinces)
            ('P_DYM', 'Dyamongou', (SELECT id FROM regions WHERE code = 'R_TPA')),
            ('P_TPA', 'Tapoa', (SELECT id FROM regions WHERE code = 'R_TPA')),
            -- Région YAADGA (4 provinces)
            ('P_LRM', 'Loroum', (SELECT id FROM regions WHERE code = 'R_YDG')),
            ('P_PSR', 'Passoré', (SELECT id FROM regions WHERE code = 'R_YDG')),
            ('P_YTG', 'Yatenga', (SELECT id FROM regions WHERE code = 'R_YDG')),
            ('P_ZDM', 'Zondoma', (SELECT id FROM regions WHERE code = 'R_YDG'))
    ) AS p(code, name, region_id)
    RETURNING id, code, name, parent_id
)

-- =============================================================================
-- COUCHE 4 : 351 COMMUNES
-- Toutes listées par province avec codes uniques au format C_{PROVINCE_CODE}_{NUM}
-- =============================================================================
, communes AS (
    INSERT INTO localities (id, code, name, type, parent_id, status, created_at, updated_at)
        SELECT gen_random_uuid(), code, name, 'COMMUNE', province_id, 'ACTIVE', NOW(), NOW()
        FROM (
            VALUES
                -- =====================================================================
                -- Région BANKUI (3 provinces, 23 communes)
                -- =====================================================================
                -- Province du Mouhoun (7 communes)
                ('C_MOH_01', 'Bondokuy', (SELECT id FROM provinces WHERE code = 'P_MOH')),
                ('C_MOH_02', 'Dédougou', (SELECT id FROM provinces WHERE code = 'P_MOH')),
                ('C_MOH_03', 'Douroula', (SELECT id FROM provinces WHERE code = 'P_MOH')),
                ('C_MOH_04', 'Kona', (SELECT id FROM provinces WHERE code = 'P_MOH')),
                ('C_MOH_05', 'Ouarkoye', (SELECT id FROM provinces WHERE code = 'P_MOH')),
                ('C_MOH_06', 'Safané', (SELECT id FROM provinces WHERE code = 'P_MOH')),
                ('C_MOH_07', 'Tchériba', (SELECT id FROM provinces WHERE code = 'P_MOH')),
                -- Province du Balé (10 communes)
                ('C_BLM_01', 'Bagassi', (SELECT id FROM provinces WHERE code = 'P_BLM')),
                ('C_BLM_02', 'Bana', (SELECT id FROM provinces WHERE code = 'P_BLM')),
                ('C_BLM_03', 'Boromo', (SELECT id FROM provinces WHERE code = 'P_BLM')),
                ('C_BLM_04', 'Fara', (SELECT id FROM provinces WHERE code = 'P_BLM')),
                ('C_BLM_05', 'Oury', (SELECT id FROM provinces WHERE code = 'P_BLM')),
                ('C_BLM_06', 'Pa', (SELECT id FROM provinces WHERE code = 'P_BLM')),
                ('C_BLM_07', 'Pompoï', (SELECT id FROM provinces WHERE code = 'P_BLM')),
                ('C_BLM_08', 'Poura', (SELECT id FROM provinces WHERE code = 'P_BLM')),
                ('C_BLM_09', 'Siby', (SELECT id FROM provinces WHERE code = 'P_BLM')),
                ('C_BLM_10', 'Yaho', (SELECT id FROM provinces WHERE code = 'P_BLM')),
                -- Province du Banwa (6 communes)
                ('C_BNW_01', 'Balavé', (SELECT id FROM provinces WHERE code = 'P_BNW')),
                ('C_BNW_02', 'Kouka', (SELECT id FROM provinces WHERE code = 'P_BNW')),
                ('C_BNW_03', 'Sami', (SELECT id FROM provinces WHERE code = 'P_BNW')),
                ('C_BNW_04', 'Sanaba', (SELECT id FROM provinces WHERE code = 'P_BNW')),
                ('C_BNW_05', 'Solenzo', (SELECT id FROM provinces WHERE code = 'P_BNW')),
                ('C_BNW_06', 'Tansila', (SELECT id FROM provinces WHERE code = 'P_BNW')),
                
                -- =====================================================================
                -- Région DJÔRÔ (4 provinces, 28 communes)
                -- =====================================================================
                -- Province de la Bougouriba (5 communes)
                ('C_BGR_01', 'Diébougou', (SELECT id FROM provinces WHERE code = 'P_BGR')),
                ('C_BGR_02', 'Dolo', (SELECT id FROM provinces WHERE code = 'P_BGR')),
                ('C_BGR_03', 'Gbondjigui', (SELECT id FROM provinces WHERE code = 'P_BGR')),
                ('C_BGR_04', 'Nioronioro', (SELECT id FROM provinces WHERE code = 'P_BGR')),
                ('C_BGR_05', 'Tiankoura', (SELECT id FROM provinces WHERE code = 'P_BGR')),
                -- Province de l'Ioba (8 communes)
                ('C_IOB_01', 'Dano', (SELECT id FROM provinces WHERE code = 'P_IOB')),
                ('C_IOB_02', 'Dissihn', (SELECT id FROM provinces WHERE code = 'P_IOB')),
                ('C_IOB_03', 'Guéguéré', (SELECT id FROM provinces WHERE code = 'P_IOB')),
                ('C_IOB_04', 'Koper', (SELECT id FROM provinces WHERE code = 'P_IOB')),
                ('C_IOB_05', 'Niégo', (SELECT id FROM provinces WHERE code = 'P_IOB')),
                ('C_IOB_06', 'Oronkua', (SELECT id FROM provinces WHERE code = 'P_IOB')),
                ('C_IOB_07', 'Ouéssa', (SELECT id FROM provinces WHERE code = 'P_IOB')),
                ('C_IOB_08', 'Zambo', (SELECT id FROM provinces WHERE code = 'P_IOB')),
                -- Province du Noumbiel (5 communes)
                ('C_NBL_01', 'Batié', (SELECT id FROM provinces WHERE code = 'P_NBL')),
                ('C_NBL_02', 'Boussoukoula', (SELECT id FROM provinces WHERE code = 'P_NBL')),
                ('C_NBL_03', 'Kpéré', (SELECT id FROM provinces WHERE code = 'P_NBL')),
                ('C_NBL_04', 'Legmoin', (SELECT id FROM provinces WHERE code = 'P_NBL')),
                ('C_NBL_05', 'Midebdo', (SELECT id FROM provinces WHERE code = 'P_NBL')),
                -- Province du Poni (10 communes)
                ('C_PON_01', 'Bouroum-Bouroum', (SELECT id FROM provinces WHERE code = 'P_PON')),
                ('C_PON_02', 'Bousséra', (SELECT id FROM provinces WHERE code = 'P_PON')),
                ('C_PON_03', 'Djigouè', (SELECT id FROM provinces WHERE code = 'P_PON')),
                ('C_PON_04', 'Gaoua', (SELECT id FROM provinces WHERE code = 'P_PON')),
                ('C_PON_05', 'Gbomblora', (SELECT id FROM provinces WHERE code = 'P_PON')),
                ('C_PON_06', 'Kampti', (SELECT id FROM provinces WHERE code = 'P_PON')),
                ('C_PON_07', 'Loropéni', (SELECT id FROM provinces WHERE code = 'P_PON')),
                ('C_PON_08', 'Malba', (SELECT id FROM provinces WHERE code = 'P_PON')),
                ('C_PON_09', 'Nako', (SELECT id FROM provinces WHERE code = 'P_PON')),
                ('C_PON_10', 'Périgban', (SELECT id FROM provinces WHERE code = 'P_PON')),
                
                -- =====================================================================
                -- Région GOULMOU (2 provinces, 9 communes)
                -- =====================================================================
                -- Province du Gourma (6 communes)
                ('C_GOM_01', 'Diabo', (SELECT id FROM provinces WHERE code = 'P_GOM')),
                ('C_GOM_02', 'Diapangou', (SELECT id FROM provinces WHERE code = 'P_GOM')),
                ('C_GOM_03', 'Fada N''Gourma', (SELECT id FROM provinces WHERE code = 'P_GOM')),
                ('C_GOM_04', 'Matiacoali', (SELECT id FROM provinces WHERE code = 'P_GOM')),
                ('C_GOM_05', 'Tibga', (SELECT id FROM provinces WHERE code = 'P_GOM')),
                ('C_GOM_06', 'Yamba', (SELECT id FROM provinces WHERE code = 'P_GOM')),
                -- Province de la Kompienga (3 communes)
                ('C_KPG_01', 'Kompienga', (SELECT id FROM provinces WHERE code = 'P_KPG')),
                ('C_KPG_02', 'Madjoari', (SELECT id FROM provinces WHERE code = 'P_KPG')),
                ('C_KPG_03', 'Pama', (SELECT id FROM provinces WHERE code = 'P_KPG')),
                
                -- =====================================================================
                -- Région GUIRIKO (3 provinces, 33 communes)
                -- =====================================================================
                -- Province du Houet (13 communes)
                ('C_HOU_01', 'Bama', (SELECT id FROM provinces WHERE code = 'P_HOU')),
                ('C_HOU_02', 'Bobo-Dioulasso', (SELECT id FROM provinces WHERE code = 'P_HOU')),
                ('C_HOU_03', 'Dandé', (SELECT id FROM provinces WHERE code = 'P_HOU')),
                ('C_HOU_04', 'Faramana', (SELECT id FROM provinces WHERE code = 'P_HOU')),
                ('C_HOU_05', 'Fô', (SELECT id FROM provinces WHERE code = 'P_HOU')),
                ('C_HOU_06', 'Karangasso-Sambla', (SELECT id FROM provinces WHERE code = 'P_HOU')),
                ('C_HOU_07', 'Karangasso-Viguè', (SELECT id FROM provinces WHERE code = 'P_HOU')),
                ('C_HOU_08', 'Koundougou', (SELECT id FROM provinces WHERE code = 'P_HOU')),
                ('C_HOU_09', 'Léna', (SELECT id FROM provinces WHERE code = 'P_HOU')),
                ('C_HOU_10', 'Padéma', (SELECT id FROM provinces WHERE code = 'P_HOU')),
                ('C_HOU_11', 'Péni', (SELECT id FROM provinces WHERE code = 'P_HOU')),
                ('C_HOU_12', 'Satiri', (SELECT id FROM provinces WHERE code = 'P_HOU')),
                ('C_HOU_13', 'Toussiana', (SELECT id FROM provinces WHERE code = 'P_HOU')),
                -- Province du Kénédougou (13 communes)
                ('C_KND_01', 'Banzon', (SELECT id FROM provinces WHERE code = 'P_KND')),
                ('C_KND_02', 'Djigouèra', (SELECT id FROM provinces WHERE code = 'P_KND')),
                ('C_KND_03', 'Kangala', (SELECT id FROM provinces WHERE code = 'P_KND')),
                ('C_KND_04', 'Kayan', (SELECT id FROM provinces WHERE code = 'P_KND')),
                ('C_KND_05', 'Koloko', (SELECT id FROM provinces WHERE code = 'P_KND')),
                ('C_KND_06', 'Kourinion', (SELECT id FROM provinces WHERE code = 'P_KND')),
                ('C_KND_07', 'Kourouma', (SELECT id FROM provinces WHERE code = 'P_KND')),
                ('C_KND_08', 'Morolaba', (SELECT id FROM provinces WHERE code = 'P_KND')),
                ('C_KND_09', 'N''Dorola', (SELECT id FROM provinces WHERE code = 'P_KND')),
                ('C_KND_10', 'Orodara', (SELECT id FROM provinces WHERE code = 'P_KND')),
                ('C_KND_11', 'Samôgôyiri', (SELECT id FROM provinces WHERE code = 'P_KND')),
                ('C_KND_12', 'Samorogouan', (SELECT id FROM provinces WHERE code = 'P_KND')),
                ('C_KND_13', 'Sindo', (SELECT id FROM provinces WHERE code = 'P_KND')),
                -- Province du Tuy (7 communes)
                ('C_TUY_01', 'Békuy', (SELECT id FROM provinces WHERE code = 'P_TUY')),
                ('C_TUY_02', 'Béréba', (SELECT id FROM provinces WHERE code = 'P_TUY')),
                ('C_TUY_03', 'Boni', (SELECT id FROM provinces WHERE code = 'P_TUY')),
                ('C_TUY_04', 'Founzan', (SELECT id FROM provinces WHERE code = 'P_TUY')),
                ('C_TUY_05', 'Houndé', (SELECT id FROM provinces WHERE code = 'P_TUY')),
                ('C_TUY_06', 'Koti', (SELECT id FROM provinces WHERE code = 'P_TUY')),
                ('C_TUY_07', 'Koumbia', (SELECT id FROM provinces WHERE code = 'P_TUY')),
                
                -- =====================================================================
                -- Région KADIOGO (1 province, 7 communes)
                -- =====================================================================
                ('C_KAD_01', 'Komki-Ipala', (SELECT id FROM provinces WHERE code = 'P_KAD')),
                ('C_KAD_02', 'Komsilga', (SELECT id FROM provinces WHERE code = 'P_KAD')),
                ('C_KAD_03', 'Koubri', (SELECT id FROM provinces WHERE code = 'P_KAD')),
                ('C_KAD_04', 'Ouagadougou', (SELECT id FROM provinces WHERE code = 'P_KAD')),
                ('C_KAD_05', 'Pabré', (SELECT id FROM provinces WHERE code = 'P_KAD')),
                ('C_KAD_06', 'Saaba', (SELECT id FROM provinces WHERE code = 'P_KAD')),
                ('C_KAD_07', 'Tanghin-Dassouri', (SELECT id FROM provinces WHERE code = 'P_KAD')),
                
                -- =====================================================================
                -- Région KUILSÉ (3 provinces, 28 communes)
                -- =====================================================================
                -- Province du Bam (9 communes)
                ('C_BAM_01', 'Bourzanga', (SELECT id FROM provinces WHERE code = 'P_BAM')),
                ('C_BAM_02', 'Guibaré', (SELECT id FROM provinces WHERE code = 'P_BAM')),
                ('C_BAM_03', 'Kongoussi', (SELECT id FROM provinces WHERE code = 'P_BAM')),
                ('C_BAM_04', 'Nasséré', (SELECT id FROM provinces WHERE code = 'P_BAM')),
                ('C_BAM_05', 'Rollo', (SELECT id FROM provinces WHERE code = 'P_BAM')),
                ('C_BAM_06', 'Rouko', (SELECT id FROM provinces WHERE code = 'P_BAM')),
                ('C_BAM_07', 'Sabcé', (SELECT id FROM provinces WHERE code = 'P_BAM')),
                ('C_BAM_08', 'Tikaré', (SELECT id FROM provinces WHERE code = 'P_BAM')),
                ('C_BAM_09', 'Zimtenga', (SELECT id FROM provinces WHERE code = 'P_BAM')),
                -- Province du Namentenga (8 communes)
                ('C_NMT_01', 'Boala', (SELECT id FROM provinces WHERE code = 'P_NMT')),
                ('C_NMT_02', 'Boulsa', (SELECT id FROM provinces WHERE code = 'P_NMT')),
                ('C_NMT_03', 'Bouroum', (SELECT id FROM provinces WHERE code = 'P_NMT')),
                ('C_NMT_04', 'Dargo', (SELECT id FROM provinces WHERE code = 'P_NMT')),
                ('C_NMT_05', 'Nagbingou', (SELECT id FROM provinces WHERE code = 'P_NMT')),
                ('C_NMT_06', 'Tougouri', (SELECT id FROM provinces WHERE code = 'P_NMT')),
                ('C_NMT_07', 'Yalgo', (SELECT id FROM provinces WHERE code = 'P_NMT')),
                ('C_NMT_08', 'Zéguédéguin', (SELECT id FROM provinces WHERE code = 'P_NMT')),
                -- Province du Sandbondtenga (11 communes)
                ('C_SBT_01', 'Barsalogho', (SELECT id FROM provinces WHERE code = 'P_SBT')),
                ('C_SBT_02', 'Boussouma', (SELECT id FROM provinces WHERE code = 'P_SBT')),
                ('C_SBT_03', 'Dablo', (SELECT id FROM provinces WHERE code = 'P_SBT')),
                ('C_SBT_04', 'Kaya', (SELECT id FROM provinces WHERE code = 'P_SBT')),
                ('C_SBT_05', 'Korsimoro', (SELECT id FROM provinces WHERE code = 'P_SBT')),
                ('C_SBT_06', 'Mané', (SELECT id FROM provinces WHERE code = 'P_SBT')),
                ('C_SBT_07', 'Namissiguima', (SELECT id FROM provinces WHERE code = 'P_SBT')),
                ('C_SBT_08', 'Pensa', (SELECT id FROM provinces WHERE code = 'P_SBT')),
                ('C_SBT_09', 'Pibaoré', (SELECT id FROM provinces WHERE code = 'P_SBT')),
                ('C_SBT_10', 'Pissila', (SELECT id FROM provinces WHERE code = 'P_SBT')),
                ('C_SBT_11', 'Ziga', (SELECT id FROM provinces WHERE code = 'P_SBT')),
                
                -- =====================================================================
                -- Région LIPTAKO (3 provinces, 17 communes)
                -- =====================================================================
                -- Province de l'Oudalan (5 communes)
                ('C_OUD_01', 'Déou', (SELECT id FROM provinces WHERE code = 'P_OUD')),
                ('C_OUD_02', 'Gorom-Gorom', (SELECT id FROM provinces WHERE code = 'P_OUD')),
                ('C_OUD_03', 'Markoye', (SELECT id FROM provinces WHERE code = 'P_OUD')),
                ('C_OUD_04', 'Oursi', (SELECT id FROM provinces WHERE code = 'P_OUD')),
                ('C_OUD_05', 'Tinakoff', (SELECT id FROM provinces WHERE code = 'P_OUD')),
                -- Province du Séno (6 communes)
                ('C_SEN_01', 'Bani', (SELECT id FROM provinces WHERE code = 'P_SEN')),
                ('C_SEN_02', 'Dori', (SELECT id FROM provinces WHERE code = 'P_SEN')),
                ('C_SEN_03', 'Falangountou', (SELECT id FROM provinces WHERE code = 'P_SEN')),
                ('C_SEN_04', 'Gorgadji', (SELECT id FROM provinces WHERE code = 'P_SEN')),
                ('C_SEN_05', 'Sampelga', (SELECT id FROM provinces WHERE code = 'P_SEN')),
                ('C_SEN_06', 'Seytenga', (SELECT id FROM provinces WHERE code = 'P_SEN')),
                -- Province du Yagha (6 communes)
                ('C_YGH_01', 'Boundoré', (SELECT id FROM provinces WHERE code = 'P_YGH')),
                ('C_YGH_02', 'Mansila', (SELECT id FROM provinces WHERE code = 'P_YGH')),
                ('C_YGH_03', 'Sebba', (SELECT id FROM provinces WHERE code = 'P_YGH')),
                ('C_YGH_04', 'Solhan', (SELECT id FROM provinces WHERE code = 'P_YGH')),
                ('C_YGH_05', 'Tankougounadié', (SELECT id FROM provinces WHERE code = 'P_YGH')),
                ('C_YGH_06', 'Titabé', (SELECT id FROM provinces WHERE code = 'P_YGH')),
                
                -- =====================================================================
                -- Région NAKAMBÉ (3 provinces, 30 communes)
                -- =====================================================================
                -- Province du Boulgou (12 communes – une supprimée)
                ('C_BLG_01', 'Bagré', (SELECT id FROM provinces WHERE code = 'P_BLG')),
                ('C_BLG_02', 'Bané', (SELECT id FROM provinces WHERE code = 'P_BLG')),
                ('C_BLG_03', 'Béguédo', (SELECT id FROM provinces WHERE code = 'P_BLG')),
                ('C_BLG_04', 'Bissiga', (SELECT id FROM provinces WHERE code = 'P_BLG')),
                ('C_BLG_05', 'Bittou', (SELECT id FROM provinces WHERE code = 'P_BLG')),
                -- ('C_BLG_06', 'Boussouma', ...) supprimé
                ('C_BLG_07', 'Garango', (SELECT id FROM provinces WHERE code = 'P_BLG')),
                ('C_BLG_08', 'Komtoèga', (SELECT id FROM provinces WHERE code = 'P_BLG')),
                ('C_BLG_09', 'Niaogho', (SELECT id FROM provinces WHERE code = 'P_BLG')),
                ('C_BLG_10', 'Tenkodogo', (SELECT id FROM provinces WHERE code = 'P_BLG')),
                ('C_BLG_11', 'Zabré', (SELECT id FROM provinces WHERE code = 'P_BLG')),
                ('C_BLG_12', 'Zoaga', (SELECT id FROM provinces WHERE code = 'P_BLG')),
                ('C_BLG_13', 'Zonsé', (SELECT id FROM provinces WHERE code = 'P_BLG')),
                -- Province du Koulpelogo (8 communes)
                ('C_KPL_01', 'Comin-Yanga', (SELECT id FROM provinces WHERE code = 'P_KPL')),
                ('C_KPL_02', 'Dourtenga', (SELECT id FROM provinces WHERE code = 'P_KPL')),
                ('C_KPL_03', 'Lalgaye', (SELECT id FROM provinces WHERE code = 'P_KPL')),
                ('C_KPL_04', 'Ouargaye', (SELECT id FROM provinces WHERE code = 'P_KPL')),
                ('C_KPL_05', 'Sangha', (SELECT id FROM provinces WHERE code = 'P_KPL')),
                ('C_KPL_06', 'Soudougui', (SELECT id FROM provinces WHERE code = 'P_KPL')),
                ('C_KPL_07', 'Yargatenga', (SELECT id FROM provinces WHERE code = 'P_KPL')),
                ('C_KPL_08', 'Yondé', (SELECT id FROM provinces WHERE code = 'P_KPL')),
                -- Province du Kourittenga (9 communes)
                ('C_KRT_01', 'Andemtenga', (SELECT id FROM provinces WHERE code = 'P_KRT')),
                ('C_KRT_02', 'Baskouré', (SELECT id FROM provinces WHERE code = 'P_KRT')),
                ('C_KRT_03', 'Dialgaye', (SELECT id FROM provinces WHERE code = 'P_KRT')),
                ('C_KRT_04', 'Gounghin', (SELECT id FROM provinces WHERE code = 'P_KRT')),
                ('C_KRT_05', 'Kando', (SELECT id FROM provinces WHERE code = 'P_KRT')),
                ('C_KRT_06', 'Koupèla', (SELECT id FROM provinces WHERE code = 'P_KRT')),
                ('C_KRT_07', 'Pouytenga', (SELECT id FROM provinces WHERE code = 'P_KRT')),
                ('C_KRT_08', 'Tansobentenga', (SELECT id FROM provinces WHERE code = 'P_KRT')),
                ('C_KRT_09', 'Yargo', (SELECT id FROM provinces WHERE code = 'P_KRT')),
                
                -- =====================================================================
                -- Région NANDO (4 provinces, 38 communes)
                -- =====================================================================
                -- Province du Boulkiemdé (15 communes)
                ('C_BLK_01', 'Bingo', (SELECT id FROM provinces WHERE code = 'P_BLK')),
                ('C_BLK_02', 'Imasgo', (SELECT id FROM provinces WHERE code = 'P_BLK')),
                ('C_BLK_03', 'Kindi', (SELECT id FROM provinces WHERE code = 'P_BLK')),
                ('C_BLK_04', 'Kokologo', (SELECT id FROM provinces WHERE code = 'P_BLK')),
                ('C_BLK_05', 'Koudougou', (SELECT id FROM provinces WHERE code = 'P_BLK')),
                ('C_BLK_06', 'Nanoro', (SELECT id FROM provinces WHERE code = 'P_BLK')),
                ('C_BLK_07', 'Nandiala', (SELECT id FROM provinces WHERE code = 'P_BLK')),
                ('C_BLK_08', 'Pella', (SELECT id FROM provinces WHERE code = 'P_BLK')),
                ('C_BLK_09', 'Poa', (SELECT id FROM provinces WHERE code = 'P_BLK')),
                ('C_BLK_10', 'Ramongo', (SELECT id FROM provinces WHERE code = 'P_BLK')),
                ('C_BLK_11', 'Sabou', (SELECT id FROM provinces WHERE code = 'P_BLK')),
                ('C_BLK_12', 'Siglé', (SELECT id FROM provinces WHERE code = 'P_BLK')),
                ('C_BLK_13', 'Soaw', (SELECT id FROM provinces WHERE code = 'P_BLK')),
                ('C_BLK_14', 'Sourgou', (SELECT id FROM provinces WHERE code = 'P_BLK')),
                ('C_BLK_15', 'Thiou', (SELECT id FROM provinces WHERE code = 'P_BLK')),
                -- Province du Sanguié (10 communes)
                ('C_SNG_01', 'Dassa', (SELECT id FROM provinces WHERE code = 'P_SNG')),
                ('C_SNG_02', 'Didyr', (SELECT id FROM provinces WHERE code = 'P_SNG')),
                ('C_SNG_03', 'Godyr', (SELECT id FROM provinces WHERE code = 'P_SNG')),
                ('C_SNG_04', 'Kordié', (SELECT id FROM provinces WHERE code = 'P_SNG')),
                ('C_SNG_05', 'Kyon', (SELECT id FROM provinces WHERE code = 'P_SNG')),
                ('C_SNG_06', 'Pouni', (SELECT id FROM provinces WHERE code = 'P_SNG')),
                ('C_SNG_07', 'Réo', (SELECT id FROM provinces WHERE code = 'P_SNG')),
                ('C_SNG_08', 'Ténado', (SELECT id FROM provinces WHERE code = 'P_SNG')),
                ('C_SNG_09', 'Zamo', (SELECT id FROM provinces WHERE code = 'P_SNG')),
                ('C_SNG_10', 'Zawara', (SELECT id FROM provinces WHERE code = 'P_SNG')),
                -- Province du Sissili (7 communes)
                ('C_SSL_01', 'Biéha', (SELECT id FROM provinces WHERE code = 'P_SSL')),
                ('C_SSL_02', 'Boura', (SELECT id FROM provinces WHERE code = 'P_SSL')),
                ('C_SSL_03', 'Léo', (SELECT id FROM provinces WHERE code = 'P_SSL')),
                ('C_SSL_04', 'Nébiélianayou', (SELECT id FROM provinces WHERE code = 'P_SSL')),
                ('C_SSL_05', 'Niambouri', (SELECT id FROM provinces WHERE code = 'P_SSL')),
                ('C_SSL_06', 'Silly', (SELECT id FROM provinces WHERE code = 'P_SSL')),
                ('C_SSL_07', 'Tô', (SELECT id FROM provinces WHERE code = 'P_SSL')),
                -- Province du Ziro (6 communes)
                ('C_ZIR_01', 'Bakata', (SELECT id FROM provinces WHERE code = 'P_ZIR')),
                ('C_ZIR_02', 'Bougnounou', (SELECT id FROM provinces WHERE code = 'P_ZIR')),
                ('C_ZIR_03', 'Dalô', (SELECT id FROM provinces WHERE code = 'P_ZIR')),
                ('C_ZIR_04', 'Gaô', (SELECT id FROM provinces WHERE code = 'P_ZIR')),
                ('C_ZIR_05', 'Kassou', (SELECT id FROM provinces WHERE code = 'P_ZIR')),
                ('C_ZIR_06', 'Sapouy', (SELECT id FROM provinces WHERE code = 'P_ZIR')),
                
                -- =====================================================================
                -- Région NAZINON (3 provinces, 19 communes)
                -- =====================================================================
                -- Province du Bazèga (7 communes)
                ('C_BZG_01', 'Doulougou', (SELECT id FROM provinces WHERE code = 'P_BZG')),
                ('C_BZG_02', 'Gaongo', (SELECT id FROM provinces WHERE code = 'P_BZG')),
                ('C_BZG_03', 'Ipelcé', (SELECT id FROM provinces WHERE code = 'P_BZG')),
                ('C_BZG_04', 'Kayao', (SELECT id FROM provinces WHERE code = 'P_BZG')),
                ('C_BZG_05', 'Kombissiri', (SELECT id FROM provinces WHERE code = 'P_BZG')),
                ('C_BZG_06', 'Saponé', (SELECT id FROM provinces WHERE code = 'P_BZG')),
                ('C_BZG_07', 'Toécé', (SELECT id FROM provinces WHERE code = 'P_BZG')),
                -- Province du Nahouri (5 communes)
                ('C_NHR_01', 'Guiaro', (SELECT id FROM provinces WHERE code = 'P_NHR')),
                ('C_NHR_02', 'Pô', (SELECT id FROM provinces WHERE code = 'P_NHR')),
                ('C_NHR_03', 'Tiébélé', (SELECT id FROM provinces WHERE code = 'P_NHR')),
                ('C_NHR_04', 'Zecco', (SELECT id FROM provinces WHERE code = 'P_NHR')),
                ('C_NHR_05', 'Ziou', (SELECT id FROM provinces WHERE code = 'P_NHR')),
                -- Province du Zoundwéogo (7 communes)
                ('C_ZOW_01', 'Béré', (SELECT id FROM provinces WHERE code = 'P_ZOW')),
                ('C_ZOW_02', 'Bindé', (SELECT id FROM provinces WHERE code = 'P_ZOW')),
                ('C_ZOW_03', 'Gogo', (SELECT id FROM provinces WHERE code = 'P_ZOW')),
                ('C_ZOW_04', 'Gomboussougou', (SELECT id FROM provinces WHERE code = 'P_ZOW')),
                ('C_ZOW_05', 'Guiba', (SELECT id FROM provinces WHERE code = 'P_ZOW')),
                ('C_ZOW_06', 'Manga', (SELECT id FROM provinces WHERE code = 'P_ZOW')),
                ('C_ZOW_07', 'Nobéré', (SELECT id FROM provinces WHERE code = 'P_ZOW')),
                
                -- =====================================================================
                -- Région OUBRI (3 provinces, 20 communes)
                -- =====================================================================
                -- Province du Bassitenga (7 communes)
                ('C_BSS_01', 'Absouya', (SELECT id FROM provinces WHERE code = 'P_BSS')),
                ('C_BSS_02', 'Dapélogo', (SELECT id FROM provinces WHERE code = 'P_BSS')),
                ('C_BSS_03', 'Loumbila', (SELECT id FROM provinces WHERE code = 'P_BSS')),
                ('C_BSS_04', 'Nagréongo', (SELECT id FROM provinces WHERE code = 'P_BSS')),
                ('C_BSS_05', 'Ourgou-Manega', (SELECT id FROM provinces WHERE code = 'P_BSS')),
                ('C_BSS_06', 'Ziniaré', (SELECT id FROM provinces WHERE code = 'P_BSS')),
                ('C_BSS_07', 'Zitenga', (SELECT id FROM provinces WHERE code = 'P_BSS')),
                -- Province du Ganzourgou (8 communes)
                ('C_GZG_01', 'Boudry', (SELECT id FROM provinces WHERE code = 'P_GZG')),
                ('C_GZG_02', 'Kogho', (SELECT id FROM provinces WHERE code = 'P_GZG')),
                ('C_GZG_03', 'Méguet', (SELECT id FROM provinces WHERE code = 'P_GZG')),
                ('C_GZG_04', 'Mogtédo', (SELECT id FROM provinces WHERE code = 'P_GZG')),
                ('C_GZG_05', 'Salogo', (SELECT id FROM provinces WHERE code = 'P_GZG')),
                ('C_GZG_06', 'Zam', (SELECT id FROM provinces WHERE code = 'P_GZG')),
                ('C_GZG_07', 'Zorgho', (SELECT id FROM provinces WHERE code = 'P_GZG')),
                ('C_GZG_08', 'Zoungou', (SELECT id FROM provinces WHERE code = 'P_GZG')),
                -- Province du Kourwéogo (5 communes)
                ('C_KWG_01', 'Boussé', (SELECT id FROM provinces WHERE code = 'P_KWG')),
                ('C_KWG_02', 'Laye', (SELECT id FROM provinces WHERE code = 'P_KWG')),
                ('C_KWG_03', 'Niou', (SELECT id FROM provinces WHERE code = 'P_KWG')),
                ('C_KWG_04', 'Sourgoubila', (SELECT id FROM provinces WHERE code = 'P_KWG')),
                ('C_KWG_05', 'Toéghin', (SELECT id FROM provinces WHERE code = 'P_KWG')),
                
                -- =====================================================================
                -- Région SIRBA (2 provinces, 10 communes)
                -- =====================================================================
                -- Province de la Gnagna (7 communes)
                ('C_GNG_01', 'Bilanga', (SELECT id FROM provinces WHERE code = 'P_GNG')),
                ('C_GNG_02', 'Bogandé', (SELECT id FROM provinces WHERE code = 'P_GNG')),
                ('C_GNG_03', 'Coalla', (SELECT id FROM provinces WHERE code = 'P_GNG')),
                ('C_GNG_04', 'Liptougou', (SELECT id FROM provinces WHERE code = 'P_GNG')),
                ('C_GNG_05', 'Mani', (SELECT id FROM provinces WHERE code = 'P_GNG')),
                ('C_GNG_06', 'Pièla', (SELECT id FROM provinces WHERE code = 'P_GNG')),
                ('C_GNG_07', 'Thion', (SELECT id FROM provinces WHERE code = 'P_GNG')),
                -- Province de la Komondjari (3 communes)
                ('C_KMJ_01', 'Bartiébougou', (SELECT id FROM provinces WHERE code = 'P_KMJ')),
                ('C_KMJ_02', 'Foutouri', (SELECT id FROM provinces WHERE code = 'P_KMJ')),
                ('C_KMJ_03', 'Gayéri', (SELECT id FROM provinces WHERE code = 'P_KMJ')),
                
                -- =====================================================================
                -- Région SOUM (2 provinces, 15 communes – une supprimée)
                -- =====================================================================
                -- Province du Djelgodji (11 communes)
                ('C_DGD_01', 'Djibo', (SELECT id FROM provinces WHERE code = 'P_DGD')),
                ('C_DGD_02', 'Baraboulé', (SELECT id FROM provinces WHERE code = 'P_DGD')),
                ('C_DGD_03', 'Diguel', (SELECT id FROM provinces WHERE code = 'P_DGD')),
                ('C_DGD_04', 'Don', (SELECT id FROM provinces WHERE code = 'P_DGD')),
                ('C_DGD_05', 'Dorbel', (SELECT id FROM provinces WHERE code = 'P_DGD')),
                ('C_DGD_06', 'Gassam', (SELECT id FROM provinces WHERE code = 'P_DGD')),
                ('C_DGD_07', 'Koutougou', (SELECT id FROM provinces WHERE code = 'P_DGD')),
                ('C_DGD_08', 'Nassoumbou', (SELECT id FROM provinces WHERE code = 'P_DGD')),
                ('C_DGD_09', 'Pobé-Mengao', (SELECT id FROM provinces WHERE code = 'P_DGD')),
                ('C_DGD_10', 'Tongomayel', (SELECT id FROM provinces WHERE code = 'P_DGD')),
                -- ('C_DGD_11', 'Zambo', ...) supprimé
                -- Province du Karo-Peli (4 communes – une supprimée)
                ('C_KRP_01', 'Arbinda', (SELECT id FROM provinces WHERE code = 'P_KRP')),
                ('C_KRP_02', 'Béléhédé', (SELECT id FROM provinces WHERE code = 'P_KRP')),
                ('C_KRP_03', 'Kelbo', (SELECT id FROM provinces WHERE code = 'P_KRP')),
                ('C_KRP_04', 'Kéré', (SELECT id FROM provinces WHERE code = 'P_KRP')),
                -- ('C_KRP_05', 'Koumbri', ...) supprimé
                
                -- =====================================================================
                -- Région SOUROU (3 provinces, 21 communes – une supprimée)
                -- =====================================================================
                -- Province du Koosin (9 communes)
                ('C_KSN_01', 'Nouna', (SELECT id FROM provinces WHERE code = 'P_KSN')),
                ('C_KSN_02', 'Barani', (SELECT id FROM provinces WHERE code = 'P_KSN')),
                ('C_KSN_03', 'Bomborokui', (SELECT id FROM provinces WHERE code = 'P_KSN')),
                ('C_KSN_04', 'Djibasso', (SELECT id FROM provinces WHERE code = 'P_KSN')),
                ('C_KSN_05', 'Dokui', (SELECT id FROM provinces WHERE code = 'P_KSN')),
                ('C_KSN_06', 'Doumbala', (SELECT id FROM provinces WHERE code = 'P_KSN')),
                ('C_KSN_07', 'Kombori', (SELECT id FROM provinces WHERE code = 'P_KSN')),
                ('C_KSN_08', 'Madouba', (SELECT id FROM provinces WHERE code = 'P_KSN')),
                ('C_KSN_09', 'Sono', (SELECT id FROM provinces WHERE code = 'P_KSN')),
                -- Province du Nayala (5 communes – une supprimée)
                ('C_NYL_01', 'Toma', (SELECT id FROM provinces WHERE code = 'P_NYL')),
                ('C_NYL_02', 'Gassan', (SELECT id FROM provinces WHERE code = 'P_NYL')),
                ('C_NYL_03', 'Gossina', (SELECT id FROM provinces WHERE code = 'P_NYL')),
                ('C_NYL_04', 'Kougny', (SELECT id FROM provinces WHERE code = 'P_NYL')),
                -- ('C_NYL_05', 'Tougan', ...) supprimé
                ('C_NYL_06', 'Yaba', (SELECT id FROM provinces WHERE code = 'P_NYL')),
                -- Province du Sourou (7 communes)
                ('C_SRO_01', 'Tougan', (SELECT id FROM provinces WHERE code = 'P_SRO')),
                ('C_SRO_02', 'Di', (SELECT id FROM provinces WHERE code = 'P_SRO')),
                ('C_SRO_03', 'Gomboro', (SELECT id FROM provinces WHERE code = 'P_SRO')),
                ('C_SRO_04', 'Kassoum', (SELECT id FROM provinces WHERE code = 'P_SRO')),
                ('C_SRO_05', 'Kiembara', (SELECT id FROM provinces WHERE code = 'P_SRO')),
                ('C_SRO_06', 'Lankoué', (SELECT id FROM provinces WHERE code = 'P_SRO')),
                ('C_SRO_07', 'Toéni', (SELECT id FROM provinces WHERE code = 'P_SRO')),
                
                -- =====================================================================
                -- Région TANNOUNYAN (2 provinces, 16 communes)
                -- =====================================================================
                -- Province de la Comoé (9 communes)
                ('C_COM_01', 'Banfora', (SELECT id FROM provinces WHERE code = 'P_COM')),
                ('C_COM_02', 'Bérégadougou', (SELECT id FROM provinces WHERE code = 'P_COM')),
                ('C_COM_03', 'Mangodara', (SELECT id FROM provinces WHERE code = 'P_COM')),
                ('C_COM_04', 'Moussodougou', (SELECT id FROM provinces WHERE code = 'P_COM')),
                ('C_COM_05', 'Niangoloko', (SELECT id FROM provinces WHERE code = 'P_COM')),
                ('C_COM_06', 'Ouo', (SELECT id FROM provinces WHERE code = 'P_COM')),
                ('C_COM_07', 'Sidéradougou', (SELECT id FROM provinces WHERE code = 'P_COM')),
                ('C_COM_08', 'Soubakaniédougou', (SELECT id FROM provinces WHERE code = 'P_COM')),
                ('C_COM_09', 'Tiéfora', (SELECT id FROM provinces WHERE code = 'P_COM')),
                -- Province de la Léraba (7 communes)
                ('C_LRB_01', 'Sindou', (SELECT id FROM provinces WHERE code = 'P_LRB')),
                ('C_LRB_02', 'Dakoro', (SELECT id FROM provinces WHERE code = 'P_LRB')),
                ('C_LRB_03', 'Douna', (SELECT id FROM provinces WHERE code = 'P_LRB')),
                ('C_LRB_04', 'Kankalaba', (SELECT id FROM provinces WHERE code = 'P_LRB')),
                ('C_LRB_05', 'Loumana', (SELECT id FROM provinces WHERE code = 'P_LRB')),
                ('C_LRB_06', 'Niankorodougou', (SELECT id FROM provinces WHERE code = 'P_LRB')),
                ('C_LRB_07', 'Wolonkoto', (SELECT id FROM provinces WHERE code = 'P_LRB')),
                
                -- =====================================================================
                -- Région TAPOA (1 province, 8 communes – les 4 de Dyamongou sont supprimées)
                -- =====================================================================
                ('C_TPA_01', 'Diapaga', (SELECT id FROM provinces WHERE code = 'P_TPA')),
                ('C_TPA_02', 'Bottou', (SELECT id FROM provinces WHERE code = 'P_TPA')),
                ('C_TPA_03', 'Kantari', (SELECT id FROM provinces WHERE code = 'P_TPA')),
                ('C_TPA_04', 'Logobou', (SELECT id FROM provinces WHERE code = 'P_TPA')),
                ('C_TPA_05', 'Namounou', (SELECT id FROM provinces WHERE code = 'P_TPA')),
                ('C_TPA_06', 'Partiaga', (SELECT id FROM provinces WHERE code = 'P_TPA')),
                ('C_TPA_07', 'Tambaga', (SELECT id FROM provinces WHERE code = 'P_TPA')),
                ('C_TPA_08', 'Tansarga', (SELECT id FROM provinces WHERE code = 'P_TPA')),
                
                -- =====================================================================
                -- Région YAADGA (4 provinces, 31 communes)
                -- =====================================================================
                -- Province du Loroum (4 communes)
                ('C_LRM_01', 'Titao', (SELECT id FROM provinces WHERE code = 'P_LRM')),
                ('C_LRM_02', 'Banh', (SELECT id FROM provinces WHERE code = 'P_LRM')),
                ('C_LRM_03', 'Ouindigui', (SELECT id FROM provinces WHERE code = 'P_LRM')),
                ('C_LRM_04', 'Sollé', (SELECT id FROM provinces WHERE code = 'P_LRM')),
                -- Province du Passoré (11 communes)
                ('C_PSR_01', 'Yako', (SELECT id FROM provinces WHERE code = 'P_PSR')),
                ('C_PSR_02', 'Arbolle', (SELECT id FROM provinces WHERE code = 'P_PSR')),
                ('C_PSR_03', 'Bagaré', (SELECT id FROM provinces WHERE code = 'P_PSR')),
                ('C_PSR_04', 'Bokin', (SELECT id FROM provinces WHERE code = 'P_PSR')),
                ('C_PSR_05', 'Gomponsom', (SELECT id FROM provinces WHERE code = 'P_PSR')),
                ('C_PSR_06', 'Kirsi', (SELECT id FROM provinces WHERE code = 'P_PSR')),
                ('C_PSR_07', 'Lâ-Todin', (SELECT id FROM provinces WHERE code = 'P_PSR')),
                ('C_PSR_08', 'Pilimpikou', (SELECT id FROM provinces WHERE code = 'P_PSR')),
                ('C_PSR_09', 'Samba', (SELECT id FROM provinces WHERE code = 'P_PSR')),
                ('C_PSR_10', 'Tibin', (SELECT id FROM provinces WHERE code = 'P_PSR')),
                ('C_PSR_11', 'Zogoré', (SELECT id FROM provinces WHERE code = 'P_PSR')),
                -- Province du Yatenga (11 communes)
                ('C_YTG_01', 'Ouahigouya', (SELECT id FROM provinces WHERE code = 'P_YTG')),
                ('C_YTG_02', 'Barga', (SELECT id FROM provinces WHERE code = 'P_YTG')),
                ('C_YTG_03', 'Kaïn', (SELECT id FROM provinces WHERE code = 'P_YTG')),
                ('C_YTG_04', 'Kalsaka', (SELECT id FROM provinces WHERE code = 'P_YTG')),
                ('C_YTG_05', 'Kossouka', (SELECT id FROM provinces WHERE code = 'P_YTG')),
                ('C_YTG_06', 'Koumbri', (SELECT id FROM provinces WHERE code = 'P_YTG')),
                ('C_YTG_07', 'Namissiguima', (SELECT id FROM provinces WHERE code = 'P_YTG')),
                ('C_YTG_08', 'Oula', (SELECT id FROM provinces WHERE code = 'P_YTG')),
                ('C_YTG_09', 'Rambo', (SELECT id FROM provinces WHERE code = 'P_YTG')),
                ('C_YTG_10', 'Séguénéga', (SELECT id FROM provinces WHERE code = 'P_YTG')),
                ('C_YTG_11', 'Tangaye', (SELECT id FROM provinces WHERE code = 'P_YTG')),
                -- Province du Zondoma (5 communes)
                ('C_ZDM_01', 'Gourcy', (SELECT id FROM provinces WHERE code = 'P_ZDM')),
                ('C_ZDM_02', 'Bassi', (SELECT id FROM provinces WHERE code = 'P_ZDM')),
                ('C_ZDM_03', 'Boussou', (SELECT id FROM provinces WHERE code = 'P_ZDM')),
                ('C_ZDM_04', 'Léboudougou', (SELECT id FROM provinces WHERE code = 'P_ZDM')),
                ('C_ZDM_05', 'Tougo', (SELECT id FROM provinces WHERE code = 'P_ZDM'))
                
        ) AS c(code, name, province_id)
    RETURNING id, code, parent_id
),

-- ---------------------------------------------------------------------
-- 5. ARRONDISSEMENTS (Ouaga: 12, Bobo: 7)
-- ---------------------------------------------------------------------

    ouaga_arrondissements AS (
        INSERT INTO localities (id, code, name, type, parent_id, status, created_at, updated_at)
        SELECT gen_random_uuid(), 'ARR_OUA_'||num, 'Arrondissement '||num, 'ARRONDISSEMENT', (SELECT id FROM communes WHERE code = 'C_KAD_04'),
        'ACTIVE', NOW(), NOW()
        FROM generate_series(1, 12) num
        RETURNING id, code, parent_id
    ), 
    bobo_arrondissements AS (
        INSERT INTO localities (id, code, name, type, parent_id, status, created_at, updated_at)
        SELECT gen_random_uuid(), 'ARR_BOB_'||num, 'Arrondissement '||num, 'ARRONDISSEMENT', (SELECT id FROM communes WHERE code = 'C_HOU_02'),
        'ACTIVE', NOW(), NOW()
        FROM generate_series(1, 7) num
        RETURNING id, code, parent_id
    ),

-- ---------------------------------------------------------------------
-- 6. Secteurs de Ouagadougou (55 secteurs)
-- ---------------------------------------------------------------------
ouaga_secteurs AS (
    INSERT INTO localities (id, code, name, type, parent_id, status, created_at, updated_at)
    SELECT gen_random_uuid(), code, name, 'SECTOR', (SELECT id FROM communes WHERE code = 'C_KAD_04'), 'ACTIVE', NOW(), NOW()
    FROM (
        VALUES
            ('SEC_OUA_01', 'Secteur 1 – Bilbalogo'), ('SEC_OUA_02', 'Secteur 2 – Dapoya'),
            ('SEC_OUA_03', 'Secteur 3 – Gounghin'), ('SEC_OUA_04', 'Secteur 4 – Paspanga'),
            ('SEC_OUA_05', 'Secteur 5 – Koulouba'), ('SEC_OUA_06', 'Secteur 6 – Saint Léon'),
            ('SEC_OUA_07', 'Secteur 7 – Ouidi'), ('SEC_OUA_08', 'Secteur 8 – Larlé'),
            ('SEC_OUA_09', 'Secteur 9 – Nemnin'), ('SEC_OUA_10', 'Secteur 10 – Dapoya 2'),
            ('SEC_OUA_11', 'Secteur 11 – Kologh Naba'), ('SEC_OUA_12', 'Secteur 12 – Kienbaoghin'),
            ('SEC_OUA_13', 'Secteur 13 – Zongo'), ('SEC_OUA_14', 'Secteur 14 – Tampouy'),
            ('SEC_OUA_15', 'Secteur 15 – Kilwin'), ('SEC_OUA_16', 'Secteur 16 – Naababpougo'),
            ('SEC_OUA_17', 'Secteur 17 – Somgandé'), ('SEC_OUA_18', 'Secteur 18 – Zone industrielle'),
            ('SEC_OUA_19', 'Secteur 19 – Nioko 2'), ('SEC_OUA_20', 'Secteur 20 – Tanghin'),
            ('SEC_OUA_21', 'Secteur 21 – Zogona'), ('SEC_OUA_22', 'Secteur 22 – Wemtenga'),
            ('SEC_OUA_23', 'Secteur 23 – Dagnoën'), ('SEC_OUA_24', 'Secteur 24 – Kalgondin'),
            ('SEC_OUA_25', 'Secteur 25 – Cissin'), ('SEC_OUA_26', 'Secteur 26 – Kouritenga'),
            ('SEC_OUA_27', 'Secteur 27 – Pissy'), ('SEC_OUA_28', 'Secteur 28 – Nagrin'),
            ('SEC_OUA_29', 'Secteur 29 – Yaoghin'), ('SEC_OUA_30', 'Secteur 30 – Sandogo'),
            ('SEC_OUA_31', 'Secteur 31 – Kankasin'), ('SEC_OUA_32', 'Secteur 32 – Boassa'),
            ('SEC_OUA_33', 'Secteur 33 – Bassinko'), ('SEC_OUA_34', 'Secteur 34 – Silmiougou'),
            ('SEC_OUA_35', 'Secteur 35 – Gantin'), ('SEC_OUA_36', 'Secteur 36 – Dar-es-Salam'),
            ('SEC_OUA_37', 'Secteur 37 – Bangpooré'), ('SEC_OUA_38', 'Secteur 38 – Larlé Wéogo'),
            ('SEC_OUA_39', 'Secteur 39 – Marcoussis'), ('SEC_OUA_40', 'Secteur 40 – Silmiyiri'),
            ('SEC_OUA_41', 'Secteur 41 – Kossodo'), ('SEC_OUA_42', 'Secteur 42 – Wayalghin'),
            ('SEC_OUA_43', 'Secteur 43 – Godin'), ('SEC_OUA_44', 'Secteur 44 – Nioko 1'),
            ('SEC_OUA_45', 'Secteur 45 – Dassosgho'), ('SEC_OUA_46', 'Secteur 46 – Dassasgo'),
            ('SEC_OUA_47', 'Secteur 47 – Yemtenga'), ('SEC_OUA_48', 'Secteur 48 – Karpala'),
            ('SEC_OUA_49', 'Secteur 49 – Balkuy'), ('SEC_OUA_50', 'Secteur 50 – Lanoayiri'),
            ('SEC_OUA_51', 'Secteur 51 – Dayongo'), ('SEC_OUA_52', 'Secteur 52 – Patte d’oie'),
            ('SEC_OUA_53', 'Secteur 53 – Ouaga 2000'), ('SEC_OUA_54', 'Secteur 54 – Trame d’accueil'),
            ('SEC_OUA_55', 'Secteur 55 – Zagtouli')
    ) AS s(code, name)
    RETURNING id, code, name
)

-- 6.2 Secteurs de Bobo-Dioulasso (33)
, bobo_secteurs AS (
    INSERT INTO localities (id, code, name, type, parent_id, status, created_at, updated_at)
    SELECT gen_random_uuid(), code, name, 'SECTOR', (SELECT id FROM communes WHERE code = 'C_HOU_02'), 'ACTIVE', NOW(), NOW()
    FROM (
        VALUES
            ('SEC_BOB_01', 'Secteur 1 – Konsa'), ('SEC_BOB_02', 'Secteur 2 – Dioulasso'),
            ('SEC_BOB_03', 'Secteur 3 – Tounouma'), ('SEC_BOB_04', 'Secteur 4 – Do'),
            ('SEC_BOB_05', 'Secteur 5 – Lafiabougou'), ('SEC_BOB_06', 'Secteur 6 – Farakoba'),
            ('SEC_BOB_07', 'Secteur 7 – Yeguéresso'), ('SEC_BOB_08', 'Secteur 8 – Koué'),
            ('SEC_BOB_09', 'Secteur 9 – Nasso'), ('SEC_BOB_10', 'Secteur 10 – Badara'),
            ('SEC_BOB_11', 'Secteur 11 – Barani'), ('SEC_BOB_12', 'Secteur 12 – Dafinso'),
            ('SEC_BOB_13', 'Secteur 13 – Doufiguisso'), ('SEC_BOB_14', 'Secteur 14 – Kimidougou'),
            ('SEC_BOB_15', 'Secteur 15 – Moukoma'), ('SEC_BOB_16', 'Secteur 16 – Panamasso'),
            ('SEC_BOB_17', 'Secteur 17 – Santidougou'), ('SEC_BOB_18', 'Secteur 18 – Kékelésso'),
            ('SEC_BOB_19', 'Secteur 19 – Kouentou'), ('SEC_BOB_20', 'Secteur 20 – Léguéma'),
            ('SEC_BOB_21', 'Secteur 21 – Moussobadougou'), ('SEC_BOB_22', 'Secteur 22 – Noumousso'),
            ('SEC_BOB_23', 'Secteur 23 – Koloko'), ('SEC_BOB_24', 'Secteur 24 – Koroma'),
            ('SEC_BOB_25', 'Secteur 25 – Kotédougou'), ('SEC_BOB_26', 'Secteur 26 – Lo'),
            ('SEC_BOB_27', 'Secteur 27 – Niamadougou'), ('SEC_BOB_28', 'Secteur 28 – Ouangolodougou'),
            ('SEC_BOB_29', 'Secteur 29 – Zambélédougou'), ('SEC_BOB_30', 'Secteur 30 – Diéridougou'),
            ('SEC_BOB_31', 'Secteur 31 – Guimbé'), ('SEC_BOB_32', 'Secteur 32 – Kokorowé'),
            ('SEC_BOB_33', 'Secteur 33 – Wahablé')
    ) AS s(code, name)
    RETURNING id, code, name
)
-- =============================================================================
-- 7. QUARTIERS RÉELS (type VILLAGE) rattachés aux secteurs
--    - Pour Ouagadougou : 80 quartiers répartis dans les 55 secteurs
--    - Pour Bobo-Dioulasso : 36 villages répartis dans les 33 secteurs
-- =============================================================================

-- 7.1 Quartiers de Ouagadougou (rattachement aux secteurs)
, ouaga_quartiers AS (
    INSERT INTO localities (id, code, name, type, parent_id, status, created_at, updated_at)
    SELECT gen_random_uuid(), code, name, 'VILLAGE', parent_id, 'ACTIVE', NOW(), NOW()
    FROM (
        VALUES
        -- Exemples : Bilbalogo dans Secteur 1, Dapoya dans Secteur 2, etc.
        ('QRT_OUA_01', 'Bilbalogo', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_01')),
        ('QRT_OUA_02', 'Saint Léon', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_06')),
        ('QRT_OUA_03', 'Zangouettin', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_01')),
        ('QRT_OUA_04', 'Tiedpalogo', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_01')),
        ('QRT_OUA_05', 'Koulouba', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_05')),
        ('QRT_OUA_06', 'Kamsonghin', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_01')),
        ('QRT_OUA_07', 'Samandin', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_01')),
        ('QRT_OUA_08', 'Gounghin Sud', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_03')),
        ('QRT_OUA_09', 'Gandin', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_01')),
        ('QRT_OUA_10', 'Kouritenga', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_26')),
        ('QRT_OUA_11', 'Mankougoudou', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_01')),
        ('QRT_OUA_12', 'Paspanga', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_04')),
        ('QRT_OUA_13', 'Ouidi', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_07')),
        ('QRT_OUA_14', 'Larlé', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_08')),
        ('QRT_OUA_15', 'Kologh Naba', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_11')),
        ('QRT_OUA_16', 'Dapoya 2', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_10')),
        ('QRT_OUA_17', 'Nemnin', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_09')),
        ('QRT_OUA_18', 'Niogsin', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_02')),
        ('QRT_OUA_19', 'Hamdalaye', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_07')),
        ('QRT_OUA_20', 'Gounghin Nord', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_03')),
        ('QRT_OUA_21', 'Baoghin', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_01')),
        ('QRT_OUA_22', 'Camp militaire', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_12')),
        ('QRT_OUA_23', 'Naababpougo', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_16')),
        ('QRT_OUA_24', 'Kienbaoghin', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_12')),
        ('QRT_OUA_25', 'Zongo', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_13')),
        ('QRT_OUA_26', 'Koumdayonré', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_12')),
        ('QRT_OUA_27', 'Nonsin', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_14')),
        ('QRT_OUA_28', 'Rimkièta', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_12')),
        ('QRT_OUA_29', 'Tampouy', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_14')),
        ('QRT_OUA_30', 'Kilwin', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_15')),
        ('QRT_OUA_31', 'Tanghin', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_20')),
        ('QRT_OUA_32', 'Sambin barrage', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_20')),
        ('QRT_OUA_33', 'Somgandé', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_17')),
        ('QRT_OUA_34', 'Zone industrielle', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_18')),
        ('QRT_OUA_35', 'Nioko 2', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_19')),
        ('QRT_OUA_36', 'Bendogo', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_20')),
        ('QRT_OUA_37', 'Toukin', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_20')),
        ('QRT_OUA_38', 'Zogona', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_21')),
        ('QRT_OUA_39', 'Wemtenga', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_22')),
        ('QRT_OUA_40', 'Dagnoën', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_23')),
        ('QRT_OUA_41', 'Ronsin', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_21')),
        ('QRT_OUA_42', 'Kalgondin', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_24')),
        ('QRT_OUA_43', 'Cissin', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_25')),
        ('QRT_OUA_44', 'Pissy', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_27')),
        ('QRT_OUA_45', 'Nagrin', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_28')),
        ('QRT_OUA_46', 'Yaoghin', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_29')),
        ('QRT_OUA_47', 'Sandogo', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_30')),
        ('QRT_OUA_48', 'Kankasin', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_31')),
        ('QRT_OUA_49', 'Boassa', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_32')),
        ('QRT_OUA_50', 'Zaghtouli', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_55')),
        ('QRT_OUA_51', 'Zongo Nabitenga', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_33')),
        ('QRT_OUA_52', 'Sogpèlcé', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_33')),
        ('QRT_OUA_53', 'Bissighin', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_33')),
        ('QRT_OUA_54', 'Bassinko', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_33')),
        ('QRT_OUA_55', 'Dar-es-Salam', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_36')),
        ('QRT_OUA_56', 'Silmiougou', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_34')),
        ('QRT_OUA_57', 'Gantin', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_35')),
        ('QRT_OUA_58', 'Bangpooré', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_37')),
        ('QRT_OUA_59', 'Larlé Wéogo', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_38')),
        ('QRT_OUA_60', 'Marcoussis', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_39')),
        ('QRT_OUA_61', 'Silmiyiri', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_40')),
        ('QRT_OUA_62', 'Wob Riguéré', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_40')),
        ('QRT_OUA_63', 'Ouapassi', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_40')),
        ('QRT_OUA_64', 'Kossodo', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_41')),
        ('QRT_OUA_65', 'Wayalghin', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_42')),
        ('QRT_OUA_66', 'Godin', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_43')),
        ('QRT_OUA_67', 'Nioko 1', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_44')),
        ('QRT_OUA_68', 'Dassosgho', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_45')),
        ('QRT_OUA_69', 'Taabtenga', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_44')),
        ('QRT_OUA_70', 'Dassasgo', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_46')),
        ('QRT_OUA_71', 'Yemtenga', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_47')),
        ('QRT_OUA_72', 'Karpala', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_48')),
        ('QRT_OUA_73', 'Balkuy', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_49')),
        ('QRT_OUA_74', 'Lanoayiri', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_50')),
        ('QRT_OUA_75', 'Dayongo', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_51')),
        ('QRT_OUA_76', 'Ouidtenga', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_50')),
        ('QRT_OUA_77', 'Patte d’oie', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_52')),
        ('QRT_OUA_78', 'Ouaga 2000', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_53')),
        ('QRT_OUA_79', 'Trame d’accueil de Ouaga 2000', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_54')),
        ('QRT_OUA_80', 'Zagtouli', (SELECT id FROM ouaga_secteurs WHERE code = 'SEC_OUA_55'))
    ) AS q(code, name, parent_id)
    RETURNING id
)

-- 7.2 Villages de Bobo-Dioulasso (rattachement aux secteurs)
, bobo_villages AS (
    INSERT INTO localities (id, code, name, type, parent_id, status, created_at, updated_at)
    SELECT gen_random_uuid(), code, name, 'VILLAGE', parent_id, 'ACTIVE', NOW(), NOW()
    FROM (
        VALUES
        ('VIL_BOB_01', 'Dafinso', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_12')),
        ('VIL_BOB_02', 'Doufiguisso', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_13')),
        ('VIL_BOB_03', 'Kimidougou', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_14')),
        ('VIL_BOB_04', 'Moukoma', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_15')),
        ('VIL_BOB_05', 'Panamasso', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_16')),
        ('VIL_BOB_06', 'Santidougou', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_17')),
        ('VIL_BOB_07', 'Kékelésso', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_18')),
        ('VIL_BOB_08', 'Kouentou', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_19')),
        ('VIL_BOB_09', 'Léguéma', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_20')),
        ('VIL_BOB_10', 'Moussobadougou', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_21')),
        ('VIL_BOB_11', 'Noumousso', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_22')),
        ('VIL_BOB_12', 'Koloko', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_23')),
        ('VIL_BOB_13', 'Koroma', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_24')),
        ('VIL_BOB_14', 'Kotédougou', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_25')),
        ('VIL_BOB_15', 'Lo', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_26')),
        ('VIL_BOB_16', 'Niamadougou', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_27')),
        ('VIL_BOB_17', 'Ouangolodougou', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_28')),
        ('VIL_BOB_18', 'Zambélédougou', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_29')),
        ('VIL_BOB_19', 'Diéridougou', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_30')),
        ('VIL_BOB_20', 'Guimbé', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_31')),
        ('VIL_BOB_21', 'Kokorowé', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_32')),
        ('VIL_BOB_22', 'Wahablé', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_33')),
        ('VIL_BOB_23', 'Farakoba', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_06')),
        ('VIL_BOB_24', 'Yeguéresso', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_07')),
        ('VIL_BOB_25', 'Koué', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_08')),
        ('VIL_BOB_26', 'Nasso', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_09')),
        ('VIL_BOB_27', 'Badara', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_10')),
        ('VIL_BOB_28', 'Barani', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_11')),
        ('VIL_BOB_29', 'Lafiabougou', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_05')),
        ('VIL_BOB_30', 'Konsa', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_01')),
        ('VIL_BOB_31', 'Dioulasso', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_02')),
        ('VIL_BOB_32', 'Tounouma', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_03')),
        ('VIL_BOB_33', 'Do', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_04')),
        ('VIL_BOB_34', 'Tondogosso', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_07')),
        ('VIL_BOB_35', 'Tondoura', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_07')),
        ('VIL_BOB_36', 'Touareg', (SELECT id FROM bobo_secteurs WHERE code = 'SEC_BOB_02'))
    ) AS v(code, name, parent_id)
    RETURNING id
)
SELECT count(*) FROM localities;

-- Fermeture transitive de la hiérarchie territoriale
WITH RECURSIVE hierarchy_cte AS (
    SELECT id AS ancestor_id, id AS descendant_id, 0 AS depth
    FROM localities
    UNION ALL
    SELECT l.parent_id, h.descendant_id, h.depth + 1
    FROM localities l
    JOIN hierarchy_cte h ON l.id = h.ancestor_id
    WHERE l.parent_id IS NOT NULL
)
INSERT INTO locality_hierarchies (ancestor_id, descendant_id, depth)
SELECT ancestor_id, descendant_id, depth FROM hierarchy_cte
ON CONFLICT DO NOTHING;
