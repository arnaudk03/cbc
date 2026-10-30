-- =============================================================================
-- V900 : Données de démonstration pour le profil DEV uniquement
-- Comptes, campagnes, prix, comptoirs, magasins, producteurs et spéculations.
-- Ne jamais charger ce fichier en production.
-- =============================================================================

-- ---------------------------------------------------------------- Utilisateurs
INSERT INTO utilisateurs (matricule, nom, prenom, email, telephone, mot_de_passe_hash, actif)
VALUES
    ('ADM001', 'SAWADOGO', 'Abdoul', 'admin@sonagess.bf', '+22670000001',
     '$2b$10$I.fN6/QMvXUJgTHF7Vq/yerXZmrHoNTT9MNPJ4nxDx4tcyhhzgFEG', TRUE),
    ('DCT001', 'OUEDRAOGO', 'Awa', 'dct@sonagess.bf', '+22670000002',
     '$2b$10$5SIRI7ycoZ3LGus7OIQH0uPjMwSsTg4uSnV27McqK0IGDwHq5I6dK', TRUE),
    ('SCP001', 'KABORE', 'Idrissa', 'scp@sonagess.bf', '+22670000003',
     '$2b$10$5SIRI7ycoZ3LGus7OIQH0uPjMwSsTg4uSnV27McqK0IGDwHq5I6dK', TRUE),
    ('DFC001', 'TRAORE', 'Aminata', 'dfc@sonagess.bf', '+22670000004',
     '$2b$10$5SIRI7ycoZ3LGus7OIQH0uPjMwSsTg4uSnV27McqK0IGDwHq5I6dK', TRUE),
    ('SFB001', 'TRAORE', 'Salif', 'chef.sfb@sonagess.bf', '+22670000005',
     '$2b$10$5SIRI7ycoZ3LGus7OIQH0uPjMwSsTg4uSnV27McqK0IGDwHq5I6dK', TRUE),
    ('SFB002', 'COMPAORE', 'Mariam', 'sfb@sonagess.bf', '+22670000006',
     '$2b$10$5SIRI7ycoZ3LGus7OIQH0uPjMwSsTg4uSnV27McqK0IGDwHq5I6dK', TRUE),
    ('CPT0001', 'ZONGO', 'Pascal', 'comptable@sonagess.bf', '+22670000007',
     '$2b$10$5SIRI7ycoZ3LGus7OIQH0uPjMwSsTg4uSnV27McqK0IGDwHq5I6dK', TRUE),
    ('DGS001', 'NACOULMA', 'Bintou', 'dgsa@sonagess.bf', '+22670000008',
     '$2b$10$5SIRI7ycoZ3LGus7OIQH0uPjMwSsTg4uSnV27McqK0IGDwHq5I6dK', TRUE),
    ('SGS001', 'BARRO', 'Noufou', 'sgs@sonagess.bf', '+22670000009',
     '$2b$10$5SIRI7ycoZ3LGus7OIQH0uPjMwSsTg4uSnV27McqK0IGDwHq5I6dK', TRUE),
    ('MAG001', 'ILBOUDO', 'Adama', 'magasinier@sonagess.bf', '+22670000010',
     '$2b$10$5SIRI7ycoZ3LGus7OIQH0uPjMwSsTg4uSnV27McqK0IGDwHq5I6dK', TRUE),
    ('AGT001', 'SAWADOGO', 'Fatoumata', 'agent@sonagess.bf', '+22670000011',
     '$2b$10$5SIRI7ycoZ3LGus7OIQH0uPjMwSsTg4uSnV27McqK0IGDwHq5I6dK', TRUE),
    ('CMP001', 'KOUANDA', 'Issa', 'chef.comptoir@sonagess.bf', '+22670000012',
     '$2b$10$5SIRI7ycoZ3LGus7OIQH0uPjMwSsTg4uSnV27McqK0IGDwHq5I6dK', TRUE),
    ('DPL001', 'OUATTARA', 'Harouna', 'dpl@sonagess.bf', '+22670000013',
     '$2b$10$5SIRI7ycoZ3LGus7OIQH0uPjMwSsTg4uSnV27McqK0IGDwHq5I6dK', TRUE),
    ('CON001', 'SAWADOGO', 'Rasmané', 'consultation@sonagess.bf', '+22670000014',
     '$2b$10$5SIRI7ycoZ3LGus7OIQH0uPjMwSsTg4uSnV27McqK0IGDwHq5I6dK', TRUE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO utilisateur_roles (utilisateur_id, role_id)
SELECT u.id, r.id
FROM utilisateurs u
JOIN roles r ON r.code = CASE u.email
    WHEN 'admin@sonagess.bf' THEN 'ADMIN'
    WHEN 'dct@sonagess.bf' THEN 'DCT'
    WHEN 'scp@sonagess.bf' THEN 'SCP'
    WHEN 'dfc@sonagess.bf' THEN 'DFC_DIRECTEUR'
    WHEN 'chef.sfb@sonagess.bf' THEN 'SFB_CHEF'
    WHEN 'sfb@sonagess.bf' THEN 'SFB_INITIATEUR'
    WHEN 'comptable@sonagess.bf' THEN 'COMPTABILITE'
    WHEN 'dgsa@sonagess.bf' THEN 'DGSA'
    WHEN 'sgs@sonagess.bf' THEN 'SGS'
    WHEN 'magasinier@sonagess.bf' THEN 'MAGASINIER'
    WHEN 'agent@sonagess.bf' THEN 'AGENT_COLLECTEUR'
    WHEN 'chef.comptoir@sonagess.bf' THEN 'CHEF_COMPTOIR'
    WHEN 'dpl@sonagess.bf' THEN 'DPL'
    WHEN 'consultation@sonagess.bf' THEN 'CONSULTATION'
END
WHERE u.email IN (
    'admin@sonagess.bf', 'dct@sonagess.bf', 'scp@sonagess.bf', 'dfc@sonagess.bf',
    'chef.sfb@sonagess.bf', 'sfb@sonagess.bf', 'comptable@sonagess.bf', 'dgsa@sonagess.bf',
    'sgs@sonagess.bf', 'magasinier@sonagess.bf', 'agent@sonagess.bf',
    'chef.comptoir@sonagess.bf', 'dpl@sonagess.bf', 'consultation@sonagess.bf')
ON CONFLICT DO NOTHING;

INSERT INTO two_factor_auth (utilisateur_id, active, type_2fa)
SELECT u.id, TRUE, 'SMS'
FROM utilisateurs u
WHERE u.email IN ('admin@sonagess.bf', 'dct@sonagess.bf', 'chef.sfb@sonagess.bf')
ON CONFLICT (utilisateur_id) DO NOTHING;

-- -------------------------------------------------------------- Spéculations
INSERT INTO speculations (code, nom, seuil_humidite_max, seuil_impurete_max, active) VALUES
    ('MAIS_BLANC', 'Maïs blanc', 14.0, 2.0, TRUE),
    ('MAIS_JAUNE', 'Maïs jaune', 14.0, 2.0, TRUE),
    ('SORGHO_BLANC', 'Sorgho blanc', 13.5, 2.5, TRUE),
    ('SORGHO_ROUGE', 'Sorgho rouge', 13.5, 2.5, TRUE),
    ('MIL', 'Mil', 12.5, 2.0, TRUE),
    ('NIEBE', 'Niébé', 12.0, 1.5, TRUE),
    ('RIZ_PADDY', 'Riz paddy', 14.0, 2.0, TRUE),
    ('COTON_GRAINE', 'Coton graine', 10.0, 3.0, TRUE),
    ('ARACHIDE', 'Arachide', 9.0, 2.0, TRUE)
ON CONFLICT (code) DO NOTHING;

-- ----------------------------------------------------------------- Campagnes
INSERT INTO campagnes (code, libelle, date_debut, date_fin, objectif_total_tonnes, statut) VALUES
    ('CAMP-2025-2026', 'Campagne agricole 2025-2026', '2025-11-01', '2026-06-30', 500000.0, 'CLOTUREE'),
    ('CAMP-2026-2027', 'Campagne agricole 2026-2027', '2026-10-01', '2027-06-30', 530000.0, 'OUVERTE')
ON CONFLICT (code) DO NOTHING;

-- Objectifs nationaux de la campagne ouverte
INSERT INTO objectifs_campagne (campagne_id, speculation_id, locality_id, objectif_tonnes,
                                budget_previsionnel_cfa, seuil_alerte_pct)
SELECT c.id, s.id, NULL, o.objectif, o.budget, 80.0
FROM campagnes c
JOIN (VALUES
    ('MAIS_BLANC', 160000.0, 32000000000.0),
    ('MAIS_JAUNE', 10000.0, 2000000000.0),
    ('SORGHO_BLANC', 120000.0, 22800000000.0),
    ('SORGHO_ROUGE', 40000.0, 7600000000.0),
    ('MIL', 70000.0, 17500000000.0),
    ('NIEBE', 40000.0, 14000000000.0),
    ('RIZ_PADDY', 30000.0, 8250000000.0),
    ('COTON_GRAINE', 40000.0, 14000000000.0),
    ('ARACHIDE', 20000.0, 7000000000.0)
) AS o(code, objectif, budget) ON TRUE
JOIN speculations s ON s.code = o.code
WHERE c.code = 'CAMP-2026-2027'
  AND NOT EXISTS (
      SELECT 1 FROM objectifs_campagne oc
      WHERE oc.campagne_id = c.id AND oc.speculation_id = s.id AND oc.locality_id IS NULL
  );

-- Prix garantis nationaux approuvés pour la campagne ouverte
INSERT INTO prix_garanties (campagne_id, speculation_id, locality_id, prix_unitaire_kg,
                            date_effet, statut, approuve_par, date_approbation)
SELECT c.id, s.id, NULL, p.prix, DATE '2026-10-01', 'APPROUVE', u.id, TIMESTAMP '2026-09-25 10:00:00'
FROM campagnes c
JOIN (VALUES
    ('MAIS_BLANC', 200.0),
    ('MAIS_JAUNE', 200.0),
    ('SORGHO_BLANC', 190.0),
    ('SORGHO_ROUGE', 190.0),
    ('MIL', 250.0),
    ('NIEBE', 350.0),
    ('RIZ_PADDY', 275.0),
    ('COTON_GRAINE', 350.0),
    ('ARACHIDE', 350.0)
) AS p(code, prix) ON TRUE
JOIN speculations s ON s.code = p.code
JOIN utilisateurs u ON u.email = 'admin@sonagess.bf'
WHERE c.code = 'CAMP-2026-2027'
  AND NOT EXISTS (
      SELECT 1 FROM prix_garanties pg
      WHERE pg.campagne_id = c.id AND pg.speculation_id = s.id AND pg.locality_id IS NULL
  );

-- ------------------------------------------------------------------ Comptoirs
INSERT INTO comptoirs (code, nom, type, locality_id, latitude_gps, longitude_gps,
                       capacite_stockage_kg, actif)
SELECT c.code, c.nom, 'FIXE', l.id, c.lat, c.lon, c.capacite, TRUE
FROM (VALUES
    ('CPT-OUA-01', 'Comptoir central de Ouagadougou', 'C_KAD_04', 12.3667, -1.5167, 120000.0),
    ('CPT-OUA-02', 'Comptoir de Ouaga 2000', 'C_KAD_04', 12.3, -1.45, 60000.0),
    ('CPT-BOB-01', 'Comptoir central de Bobo-Dioulasso', 'C_HOU_02', 11.1833, -4.2833, 100000.0),
    ('CPT-KDG-01', 'Comptoir de Koudougou', 'C_BLK_05', 12.25, -2.3667, 80000.0),
    ('CPT-DED-01', 'Comptoir de Dédougou', 'C_MOH_02', 12.4667, -3.4667, 60000.0),
    ('CPT-BAN-01', 'Comptoir de Banfora', 'C_COM_01', 10.6333, -4.7667, 60000.0),
    ('CPT-OUH-01', 'Comptoir de Ouahigouya', 'C_YTG_01', 13.5833, -2.4167, 50000.0),
    ('CPT-FNG-01', 'Comptoir de Fada N''Gourma', 'C_GOM_03', 12.0667, 0.3667, 50000.0),
    ('CPT-KAY-01', 'Comptoir de Kaya', 'C_SBT_04', 13.0833, -1.0833, 50000.0),
    ('CPT-TEN-01', 'Comptoir de Tenkodogo', 'C_BLG_10', 11.7833, -0.3667, 40000.0),
    ('CPT-GAO-01', 'Comptoir de Gaoua', 'C_PON_04', 10.3333, -3.1833, 40000.0)
) AS c(code, nom, locality_code, lat, lon, capacite)
JOIN localities l ON l.code = c.locality_code
ON CONFLICT (code) DO NOTHING;

-- ------------------------------------------------------------------ Magasins
INSERT INTO magasins_centraux (code_magasin, nom, ville, capacite_totale_tonnes,
                               stock_actuel_tonnes, actif) VALUES
    ('MAG-OUA', 'Magasin central de Ouagadougou', 'Ouagadougou', 80000.0, 0.0, TRUE),
    ('MAG-BOB', 'Magasin central de Bobo-Dioulasso', 'Bobo-Dioulasso', 50000.0, 0.0, TRUE),
    ('MAG-DED', 'Magasin central de Dédougou', 'Dédougou', 30000.0, 0.0, TRUE)
ON CONFLICT (code_magasin) DO NOTHING;

-- --------------------------------------------------------------- Producteurs
INSERT INTO producteurs (nom_prenom, telephone_mobile_money, operateur_mobile, cnib_numero,
                         cnib_date_delivrance, cnib_lieu_delivrance, locality_id, adresse_precision)
SELECT p.nom, p.tel, p.operateur, p.cnib, DATE '2020-01-15', p.lieu_cnib, l.id, p.adresse
FROM (VALUES
    ('OUEDRAOGO Bila', '+22676000001', 'SANK', 'B123456789', 'Ouagadougou', 'C_KAD_04', 'Secteur 30'),
    ('SAWADOGO Awa', '+22676000002', 'CORIS', 'B223456789', 'Koudougou', 'C_BLK_05', 'Quartier administratif'),
    ('KABORE Salif', '+22676000003', 'MOOV', 'B323456789', 'Bobo-Dioulasso', 'C_HOU_02', 'Secteur 12'),
    ('TRAORE Mariam', '+22676000004', 'LIGDICASH', 'B423456789', 'Dédougou', 'C_MOH_02', 'Centre-ville'),
    ('ZONGO Issa', '+22676000005', 'SANK', 'B523456789', 'Kaya', 'C_SBT_04', 'Quartier Marché'),
    ('COMPAORE Fatoumata', '+22676000006', 'CORIS', 'B623456789', 'Fada N''Gourma', 'C_GOM_03', 'Secteur 3'),
    ('NACOULMA Adama', '+22676000007', 'MOOV', 'B723456789', 'Banfora', 'C_COM_01', 'Quartier Résidentiel'),
    ('ILBOUDO Bintou', '+22676000008', 'LIGDICASH', 'B823456789', 'Ouahigouya', 'C_YTG_01', 'Secteur 5')
) AS p(nom, tel, operateur, cnib, lieu_cnib, locality_code, adresse)
JOIN localities l ON l.code = p.locality_code
WHERE NOT EXISTS (
    SELECT 1 FROM producteurs existing WHERE existing.cnib_numero = p.cnib
);
