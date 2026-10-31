# Guide d'Intégration Mobile Flutter · Module Collecte Bord Champ (CBC)

Ce document détaille les APIs REST conçues spécifiquement pour le développement de l'application **Mobile Flutter** (tablettes et smartphones Android durcis déployés sur les comptoirs de collecte SONAGESS au Burkina Faso).

---

## 1. Architecture & Principes Fondamentaux

* **Mode Hors-Ligne (Offline-First)** : Les agents de collecte opèrent souvent dans des zones à faible couverture réseau ou sans internet. L'application Flutter doit enregistrer les producteurs, pesées et bordereaux dans une base de données locale (SQLite / Floor / Drift ou Isar).
* **Idempotence & Résilience** : Chaque bordereau généré hors-ligne porte un `clientSyncId` (UUID v4 généré par Flutter). En cas de coupure réseau pendant une synchronisation, un renvoi du même lot ne créera **aucun doublon** sur le serveur.
* **Résolution d'identifiants temporaires** : Lorsqu'un producteur est créé sur le terrain hors-ligne, Flutter lui attribue un `clientTempId` (ex: `PROD-TEMP-UUID`). Les collectes du même lot référencent ce `clientTempId`. Le serveur crée le producteur, enregistre le BRBC et renvoie la correspondance `clientTempId -> serverId` pour mise à jour de la base locale SQLite.
* **Sécurité & Traçabilité** :
  * Authentification standard `Bearer JWT` (via `/auth/login`).
  * Les rôles autorisés sont `AGENT_COLLECTEUR`, `CHEF_COMPTOIR`, `ADMIN`.
  * Horodatage GPS (`latitudeGps`, `longitudeGps`), signature tactile (`signatureProducteurToken`, `signatureAgentToken`), et numéro de série tablette (`tabletteImei`).

---

## 2. Synthèse des Endpoints Dédiés Mobile

| Méthode | URL | Description | Rôles |
| :--- | :--- | :--- | :--- |
| `GET` | `/collectes/mobile/bootstrap` | Amorçage hors-ligne (campagne, comptoir, spéculations, barèmes, prix, producteurs) | `AGENT_COLLECTEUR`, `CHEF_COMPTOIR`, `ADMIN` |
| `POST` | `/collectes/mobile/sync` | Synchronisation par lot (batch sync) des collectes et producteurs hors-ligne | `AGENT_COLLECTEUR`, `CHEF_COMPTOIR`, `ADMIN` |
| `GET` | `/collectes/mobile/resume-journalier` | Synthèse quotidienne de l'agent (tonnages, montants, compteurs de statuts) | `AGENT_COLLECTEUR`, `CHEF_COMPTOIR`, `ADMIN` |
| `GET` | `/collectes/mobile/mes-collectes` | Historique paginé des collectes de l'agent (suivi validation DCT / paiement) | `AGENT_COLLECTEUR`, `CHEF_COMPTOIR`, `ADMIN` |
| `GET` | `/collectes/mobile/brbc/{id}/ticket` | Données prêtes pour impression thermique Bluetooth ESC/POS (58mm / 80mm) | `AGENT_COLLECTEUR`, `CHEF_COMPTOIR`, `ADMIN` |
| `POST` | `/collectes/mobile/producteurs` | Enrôlement direct d'un producteur depuis le terrain (quand le réseau est présent) | `AGENT_COLLECTEUR`, `CHEF_COMPTOIR`, `ADMIN` |

---

## 3. Détail des Endpoints & Formats JSON

### 3.1. `GET /collectes/mobile/bootstrap`
Permet à la tablette de télécharger l'ensemble du référentiel nécessaire pour fonctionner de manière autonome pendant plusieurs jours sur le terrain.

**Exemple de Réponse (200 OK) :**
```json
{
  "statut": "OK",
  "message": "Référentiels hors-ligne chargés pour la tablette mobile.",
  "donnees": {
    "agent": {
      "id": 12,
      "matricule": "AGT-2026-0042",
      "nomComplet": "Moussa OUEDRAOGO",
      "telephone": "+22670123456",
      "email": "moussa.ouedraogo@sonagess.bf",
      "comptoirId": 3,
      "comptoirNom": "Comptoir Central de Dédougou",
      "comptoirCode": "CMP-DED-01",
      "roles": ["AGENT_COLLECTEUR"]
    },
    "campagne": {
      "id": 1,
      "code": "CAMP-2026",
      "libelle": "Campagne Agricole 2026",
      "dateDebut": "2026-10-01",
      "dateFin": "2027-03-31",
      "statut": "OUVERTE"
    },
    "comptoir": {
      "id": 3,
      "code": "CMP-DED-01",
      "nom": "Comptoir Central de Dédougou",
      "localityId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "localiteNom": "Dédougou",
      "regionNom": "Boucle du Mouhoun",
      "provinceNom": "Mouhoun",
      "capaciteStockageKg": 500000.0,
      "actif": true
    },
    "speculations": [
      {
        "id": 1,
        "code": "MAIS_B",
        "nom": "Maïs Blanc",
        "seuilHumiditeMax": 14.0,
        "seuilImpureteMax": 2.0,
        "active": true
      },
      {
        "id": 2,
        "code": "SORGHO_B",
        "nom": "Sorgho Blanc",
        "seuilHumiditeMax": 13.0,
        "seuilImpureteMax": 2.0,
        "active": true
      }
    ],
    "prixGarantis": [
      {
        "id": 10,
        "speculationId": 1,
        "speculationCode": "MAIS_B",
        "prixUnitaireKg": 190.0,
        "dateEffet": "2026-10-01",
        "statut": "APPROUVE"
      },
      {
        "id": 11,
        "speculationId": 2,
        "speculationCode": "SORGHO_B",
        "prixUnitaireKg": 175.0,
        "dateEffet": "2026-10-01",
        "statut": "APPROUVE"
      }
    ],
    "producteurs": [
      {
        "id": 101,
        "nomPrenom": "Alassane SAWADOGO",
        "telephoneMobileMoney": "+22676001122",
        "operateurMobile": "SANK",
        "cnibNumero": "B12345678",
        "cnibDateDelivrance": "2024-05-12",
        "cnibLieuDelivrance": "Dédougou",
        "localityId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "localityNom": "Dédougou",
        "adressePrecision": "Secteur 3, près du magasin SONAGESS"
      }
    ],
    "modesPaiement": ["MOBILE_MONEY", "ESPECES", "VIREMENT_BANCAIRE"],
    "operateursMobileMoney": ["SANK", "CORIS", "MOOV", "LIGDICASH", "ORANGE"],
    "resumeAujourdhui": {
      "date": "2026-10-31",
      "nombreBordereaux": 4,
      "nombreLots": 6,
      "quantiteTotaleKg": 12450.0,
      "montantTotalCfa": 2365500.0,
      "parSpeculation": [
        { "code": "MAIS_B", "nom": "Maïs Blanc", "quantiteKg": 8000.0, "montantCfa": 1520000.0 },
        { "code": "SORGHO_B", "nom": "Sorgho Blanc", "quantiteKg": 4450.0, "montantCfa": 845500.0 }
      ],
      "synchronisees": 2,
      "examineesScp": 3,
      "valideesDct": 1,
      "rejetees": 0
    },
    "horodatageServeur": "2026-10-31T12:00:00"
  }
}
```

---

### 3.2. `POST /collectes/mobile/sync`
Permet la transmission par lot des collectes et des producteurs créés hors-ligne.

**Exemple de Corps de Requête :**
```json
{
  "tabletteImei": "861234567890123",
  "tabletteNumeroSerie": "TAB-SONAGESS-004",
  "nouveauxProducteurs": [
    {
      "clientTempId": "TEMP-PROD-998877",
      "nomPrenom": "Issouf KABORE",
      "telephoneMobileMoney": "+22670998877",
      "operateurMobile": "CORIS",
      "cnibNumero": "B98765432",
      "cnibDateDelivrance": "2025-01-15",
      "cnibLieuDelivrance": "Koudougou",
      "localityId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "adressePrecision": "Village de Kari"
    }
  ],
  "bordereaux": [
    {
      "clientSyncId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
      "referenceLocale": "BRBC-LOC-001",
      "campagneId": 1,
      "comptoirId": 3,
      "clientTempProducteurId": "TEMP-PROD-998877",
      "modePaiement": "MOBILE_MONEY",
      "dateHeureCollecte": "2026-10-31T10:15:30",
      "latitudeGps": 12.4632,
      "longitudeGps": -3.4601,
      "observationsReceptionnaire": "Sacs propres, pesée certifiée sur bascule électronique",
      "signatureProducteurToken": "data:image/png;base64,iVBORw0KGgo...",
      "signatureAgentToken": "data:image/png;base64,iVBORw0KGgo...",
      "lignes": [
        {
          "clientLigneId": "LIGNE-UUID-01",
          "speculationId": 1,
          "nombreSacs": 20,
          "poidsBrutTotalKg": 2020.0,
          "tareEmballageKg": 20.0,
          "quantiteKg": 2000.0,
          "tauxHumidite": 12.5,
          "tauxImpuretes": 1.2,
          "grainsAvaries": 0.5,
          "conforme": true,
          "decisionAgent": "ACCEPTE",
          "motifRejet": null
        }
      ]
    }
  ]
}
```

**Exemple de Réponse (200 OK) :**
```json
{
  "statut": "OK",
  "message": "Synchronisation terminée : 1 succès, 0 échec(s) sur 1 bordereau(x).",
  "donnees": {
    "horodatageServeur": "2026-10-31T12:05:12",
    "producteursMap": {
      "TEMP-PROD-998877": 105
    },
    "resultats": [
      {
        "clientSyncId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
        "referenceLocale": "BRBC-LOC-001",
        "statut": "SYNCHRONISE",
        "bordereauId": 45,
        "numeroBrbc": "BRBC-20261031-7F8A9C12",
        "quantiteTotaleKg": 2000.0,
        "montantTotalCfa": 380000.0,
        "montantEnLettres": "Trois cent quatre-vingt mille francs CFA",
        "lignes": [
          {
            "clientLigneId": "LIGNE-UUID-01",
            "collecteId": 78,
            "referenceLot": "LOT-CAMP-2026-20261031120512-3D4E5F6A",
            "speculationCode": "MAIS_B",
            "quantiteKg": 2000.0,
            "prixUnitaireKg": 190.0,
            "montantNetCfa": 380000.0
          }
        ],
        "messageErreur": null
      }
    ],
    "totalRecus": 1,
    "totalSucces": 1,
    "totalErreurs": 0
  }
}
```

---

### 3.3. `GET /collectes/mobile/brbc/{bordereauId}/ticket`
Fournit l'ensemble des données pré-formatées pour l'impression directe sur imprimante thermique Bluetooth (ESC/POS 58mm ou 80mm).

**Exemple de Réponse :**
```json
{
  "statut": "OK",
  "message": "Ticket de pesée généré pour impression thermique mobile.",
  "donnees": {
    "enteteRepublique": "BURKINA FASO\nUnite - Progres - Justice",
    "enteteSociete": "SONAGESS - Stock de Securite Alimentaire",
    "titreDocument": "BORDEREAU DE RECEPTION BORD CHAMP",
    "numeroBrbc": "BRBC-20261031-7F8A9C12",
    "dateEmission": "31/10/2026 10:15",
    "comptoirNom": "Comptoir Central de Dédougou",
    "regionNom": "Boucle du Mouhoun",
    "provinceNom": "Mouhoun",
    "agentMatricule": "AGT-2026-0042",
    "agentNom": "Moussa OUEDRAOGO",
    "producteurNom": "Issouf KABORE",
    "producteurTelephone": "+22670998877 (CORIS)",
    "producteurCnib": "B98765432",
    "modePaiement": "MOBILE_MONEY",
    "lignes": [
      {
        "speculationNom": "Maïs Blanc",
        "speculationCode": "MAIS_B",
        "referenceLot": "LOT-CAMP-2026-20261031120512-3D4E5F6A",
        "nombreSacs": 20,
        "poidsBrutKg": 2020.0,
        "tareKg": 20.0,
        "poidsNetKg": 2000.0,
        "tauxHumidite": 12.5,
        "tauxImpuretes": 1.2,
        "prixUnitaireKg": 190.0,
        "montantNetCfa": 380000.0,
        "conformite": "CONFORME"
      }
    ],
    "totalPoidsBrutKg": 2020.0,
    "totalTareKg": 20.0,
    "totalPoidsNetKg": 2000.0,
    "montantTotalCfa": 380000.0,
    "montantEnLettres": "Trois cent quatre-vingt mille francs CFA",
    "observations": "Sacs propres, pesée certifiée",
    "mentionsLegales": "Document officiel SONAGESS emis sur bascule certifiee. Reglement securise Mobile Money apres validation DCT.",
    "qrCodePayload": "SONAGESS|BRBC:BRBC-20261031-7F8A9C12|PROD:Issouf KABORE|TEL:+22670998877|NET_KG:2000.0|MONTANT_CFA:380000.0|DATE:2026-10-31"
  }
}
```

---

## 4. Recommandations pour l'Implémentation Flutter

1. **State Management & Offline Storage** :
   * Recommandé : **Drift** (SQLite typé) ou **Isar** pour la persistence locale.
   * Modèle local `BordereauEntity` avec champs : `clientSyncId` (String), `isSynced` (bool), `dateCreationLocal`, `syncAttemptCount`.
2. **Gestion de la connectivité** :
   * Utiliser le package `connectivity_plus`.
   * Dès que l'appareil passe en statut `ConnectivityResult.wifi` ou `ConnectivityResult.mobile`, déclencher un worker en tâche de fond (ou via `workmanager`) pour appeler `POST /collectes/mobile/sync`.
3. **Signature tactile** :
   * Package `signature` Flutter : capturer le dessin tactile sur l'écran de la tablette et l'exporter en chaîne Base64 PNG.
4. **Impression Bluetooth** :
   * Packages `blue_thermal_printer` ou `esc_pos_utils`.
   * Utiliser directement le payload JSON fourni par `/collectes/mobile/brbc/{id}/ticket` pour imprimer le ticket avec le QR code généré.

