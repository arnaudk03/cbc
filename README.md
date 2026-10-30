# CBC - Centrale d'Achat des Céréales (SONAGESS)
## Dématérialisation de la Collecte Bord Champ (CBC)

Système d'information et de gestion pour la dématérialisation de la collecte des céréales bord champ par la **SONAGESS** (Société Nationale de Gestion du Stock de Sécurité alimentaire du Burkina Faso), dans le cadre de l'Offensive Agropastorale et Halieutique (OAPH).

---

## 🏗️ Architecture du Projet

Le projet est structuré sous forme d'un monorepo comprenant deux parties principales :
- **`backend/`** : API REST développée avec **Java / Spring Boot** (Spring Data JPA, Spring Security / JWT, Flyway, PostgreSQL, Maven).
- **`frontend/`** : Application web monopage développée avec **Angular 19** (TypeScript, SCSS, Angular Material / UI components).
- **`docs/`** : Documentation technique, spécifications fonctionnelles et diagrammes UML de conception.

---

## 👥 Rôles et Acteurs du Système

Le système gère plusieurs profils utilisateurs avec des droits et habilitations spécifiques :

1. **Administrateur (`ADMIN`)** :
   - Gestion des utilisateurs et de leurs affectations aux comptoirs.
   - Configuration des référentiels (campagnes, spéculations, prix garantis, comptoirs, magasins centraux, localités).
   - Consultation des journaux d'audit et du système.

2. **Chef de Comptoir / Gestionnaire de Collecte** :
   - Supervision des opérations de collecte bord champ (BRBC - Bon de Réception Bord Champ).
   - Validation des bons de collecte et suivi des volumes par spéculation.

3. **Magasinier / Gestionnaire de Stock** :
   - Réception physique des céréales dans les magasins centraux ou régionaux.
   - Émission et gestion des BEBC (Bon d'Entrée en Magasin Central) et suivi des mouvements de stock.

4. **Comptable / Agent Financier** :
   - Suivi et validation des ordres de paiement (OP) liés aux collectes validées.
   - Gestion des règlements aux producteurs (modes de paiement : virement, espèces, mobile money).

5. **Producteur / Groupement de Producteurs** :
   - Enregistrement dans le référentiel des producteurs.
   - Participation aux campagnes de collecte aux prix garantis fixés par l'État.

---

## 📊 Diagrammes de Conception (UML)

La conception détaillée est documentée dans le dossier `docs/conception/`. Le répertoire `docs/conception/diagrams/` contient les diagrammes PlantUML (`.mmd`), ainsi que leurs exports graphiques (`.svg`, `.png`) :

- **Diagramme de Cas d'Utilisation** (`1_usecase`) : Vue d'ensemble des interactions des acteurs avec le système CBC.
- **Diagrammes de Classes** (`2_classes`, `2a`, `2b`, `2c`) :
  - Modèle métier global.
  - Sécurité & Administration.
  - Collecte & Paiement.
  - Stocks & Logistique.
- **Diagrammes de Séquence** :
  - Authentification et Sécurité (`seq_auth`)
  - Processus de Collecte Bord Champ (`3_seq_collecte`)
  - Validation des bons de collecte (`4_seq_validation`)
  - Circuit de Paiement (`5_seq_paiement`)
  - Transfert et Mouvement de Stock (`6_seq_transfert`)

Vous pouvez visualiser l'ensemble des diagrammes via la page interactive de visualisation : `docs/conception/visualisation_diagrammes.html`.

---

## ⚙️ Prérequis techniques

Assurez-vous d'avoir installé les outils suivants sur votre poste de développement :
- **Java JDK 17** ou supérieur
- **Maven 3.8+** (ou utilisation du wrapper Maven inclus)
- **Node.js 18+** et **npm** (ou yarn / pnpm)
- **Angular CLI 19+**
- **PostgreSQL 14+** (ou instance de base de données compatible)

---

## 🚀 Instructions de Lancement

### 1. Configuration de la Base de Données
Créez une base de données PostgreSQL nommée `cbc_db` :
```sql
CREATE DATABASE cbc_db;
```

### 2. Lancement du Backend (Spring Boot)
Ouvrez un terminal dans le dossier `backend/` :
```cd backend```

Configurez vos paramètres de connexion si nécessaire dans `src/main/resources/application.yml` (ou via des variables d'environnement).

Lancez l'application avec Maven :
```bash
# Sous Linux / macOS
./mvnw spring-boot:run

# Sous Windows
mvnw.cmd spring-boot:run
```
L'API sera accessible sur `http://localhost:8080/api/v1`. La documentation Swagger OpenAPI est disponible sur `http://localhost:8080/api/v1/swagger-ui.html`.

### 3. Lancement du Frontend (Angular)
Ouvrez un second terminal dans le dossier `frontend/` :
```bash
cd frontend
```

Installez les dépendances Node.js :
```bash
npm install
```

Lancez le serveur de développement Angular :
```bash
npm start
```
L'application web sera accessible dans votre navigateur sur `http://localhost:4200`.

---

## 📄 Documentation et Références
- **Cahier des Charges** : `Cahier_des_charge.md`
- **Dossier Technique de Conception (DTC)** : `Document_Technique_de_Conception_CBC_SONAGESS_V1.docx`
- **Dossier Conceptuel UML** : `docs/conception/Dossier_Conceptuel_UML.md`
