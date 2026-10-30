-- Affectation des profils terrain de démonstration au comptoir de Bobo-Dioulasso
-- (CPT-BOB-01), afin d'illustrer le cloisonnement des collectes par comptoir.
UPDATE utilisateurs
SET comptoir_id = (SELECT id FROM comptoirs WHERE code = 'CPT-BOB-01')
WHERE email IN ('agent@sonagess.bf', 'chef.comptoir@sonagess.bf');
