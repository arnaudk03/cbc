+-------------------------------+---+---------------------+
| **MINISTÈRE DE                |   | **BURKINA FASO**    |
| L\'AGRICULTURE, DE L'EAU DES  |   |                     |
| RESSOURCES ANIMALES ET        |   | **La Patrie ou la   |
| HALIEUTIQUES(MAERAH)**        |   | mort,**             |
|                               |   |                     |
| **\-\-\-\-\-\-\-\-\-\-\-\--** |   | **nous Vaincrons**  |
|                               |   |                     |
| **SECRÉTARIAT GÉNÉRAL**       |   |                     |
|                               |   |                     |
| **\-\-\-\-\-\-\-\-\-\-\-\--** |   |                     |
|                               |   |                     |
| **SOCIÉTÉ NATIONALE DE        |   |                     |
| GESTION DU STOCK**            |   |                     |
|                               |   |                     |
| **DE SÉCURITÉ ALIMENTAIRE     |   |                     |
| (SONAGESS)**                  |   |                     |
+:=============================:+===+=====================+

**CAHIER DES CHARGES**

> **(ANALYSE)**

**DÉMATÉRIALISATION DE LA COLLECTE BORD CHAMP**

**Table des matières**

[Introduction [3](#introduction)](#introduction)

[I - Présentation du projet
[3](#i---présentation-du-projet)](#i---présentation-du-projet)

[I.1 - Contexte [3](#i.1---contexte)](#i.1---contexte)

[I.2 -- Objectifs [4](#i.2-objectifs)](#i.2-objectifs)

[I.3 - Livrables attendus
[4](#i.3---livrables-attendus)](#i.3---livrables-attendus)

[II - Présentation du domaine
[5](#ii---présentation-du-domaine)](#ii---présentation-du-domaine)

[II.1 - Environnement réglementaire
[5](#ii.1---environnement-réglementaire)](#ii.1---environnement-réglementaire)

[II.2 - Environnement technologique
[5](#ii.2---environnement-technologique)](#ii.2---environnement-technologique)

[II.3 - Parties prenantes
[5](#ii.3---parties-prenantes)](#ii.3---parties-prenantes)

[II.4 - Périmètre du projet
[6](#ii.4---périmètre-du-projet)](#ii.4---périmètre-du-projet)

[III - Spécifications fonctionnelles
[6](#iii---spécifications-fonctionnelles)](#iii---spécifications-fonctionnelles)

[III.1 - Fonctionnalités générales
[6](#iii.1---fonctionnalités-générales)](#iii.1---fonctionnalités-générales)

[III.2 - Fonctionnalités spécifiques
[9](#iii.2---fonctionnalités-spécifiques)](#iii.2---fonctionnalités-spécifiques)

[IV - Transfert de compétences
[17](#iv---transfert-de-compétences)](#iv---transfert-de-compétences)

[V - Conduite du changement
[18](#v---conduite-du-changement)](#v---conduite-du-changement)

[VI - Gestion des risques
[18](#vi---gestion-des-risques)](#vi---gestion-des-risques)

[VII - Modalités d\'intervention
[19](#vii---modalités-dintervention)](#vii---modalités-dintervention)

[VII.1 - Durée de la mission et calendrier de mise en œuvre
[19](#vii.1---durée-de-la-mission-et-calendrier-de-mise-en-œuvre)](#vii.1---durée-de-la-mission-et-calendrier-de-mise-en-œuvre)

[VII.2 - Gouvernance [19](#vii.2---gouvernance)](#vii.2---gouvernance)

[VIII - Les livrables
[19](#viii---les-livrables)](#viii---les-livrables)

[IX - Autres [20](#ix---autres)](#ix---autres)

[IX.1 - Points à valider avant le lancement
[20](#ix.1---points-à-valider-avant-le-lancement)](#ix.1---points-à-valider-avant-le-lancement)

[IX.2 - Documents de référence
[20](#ix.2---documents-de-référence)](#ix.2---documents-de-référence)

[IX.3 - Sigles [21](#ix.3---sigles)](#ix.3---sigles)

[Annexe : matrice des droits
[22](#annexe-matrice-des-droits)](#annexe-matrice-des-droits)

# Introduction

La SONAGESS, érigée en centrale d\'achats dans le cadre de l\'Offensive
agropastorale et halieutique (OAPH), achète directement aux producteurs
les produits agro-sylvo-pastoraux qui constituent les stocks de
souveraineté alimentaire et de régulation. Cette « collecte bord champ »
repose aujourd\'hui sur des supports papier : fiche de qualité, fiche de
pesée, bordereau de réception bord champ (BRBC) en cinq volets, états de
paiement, bordereaux d\'enlèvement et de réception.

La rencontre de concertation sur la digitalisation de la collecte bord
champ et la présentation « Initiative collecte bord champ : défis et
perspectives » ont retenu le déploiement d\'une plateforme numérique
comme prérequis du lancement de la campagne 2026.

**En résumé :**

- **Une seule application, développée sur mesure** : une application
  mobile pour les agents de terrain (fonctionne sans réseau) et une
  application web pour les directions, reliées à la même base de
  données.

- **Elle couvre tout le circuit** : contrôle qualité, pesée, bordereau
  de réception bord champ, validation par le Service collecte et
  production puis la DCT, paiement par la DFC, comptabilisation,
  transfert vers les magasins, statistiques.

- **Elle suit les procédures officielles** décrites dans les fiches de
  workflow SONAGESS_2026_001 (collecte), SONAGESS_2026_002 (paiement) et
  SONAGESS_2026_003 (transfert de stocks).

- **Gains attendus** : paiement en 7 jours ouvrables au lieu de 21 ;
  transfert en 9 jours au lieu de 15 ; situation des stocks et des
  paiements disponibles chaque jour.

Le présent cahier des charges spécifie les exigences, les
caractéristiques et les contraintes de cette plateforme. Il suit le plan
du guide d\'élaboration de cahier des charges de la Direction générale
de la transformation digitale : (I) présentation du projet ; (II)
présentation du domaine ; (III) spécifications fonctionnelles ; (V)
support et maintenance ; (VI) transfert de compétences ; (VII) conduite
du changement ; (VIII) gestion des risques ; (IX) modalités
d\'intervention ; (X) livrables ; (XI) autres. Les spécifications
techniques (IV) sont traitées dans la partie Conception du cahier des
charges.

# I - Présentation du projet

## I.1 - Contexte

La campagne 2025-2026 a permis de collecter **315 264 tonnes au 31
juillet 2026, soit 59,5 % de l\'objectif national de 530 000 tonnes**,
avec plus de 265 agents dans 12 régions et des paiements digitaux via
Sank Money, LigdiCash, Coris Money et Moov Money.

  -----------------------------------------------
  **Spéculation**              **Collecté au
                               31/07/2026 (t)**
  ---------------------------- ------------------
  Riz paddy                    137 875,2

  Maïs                         114 202,1

  Niébé                        31 391,2

  Sorgho                       13 336,2

  Soja                         8 773,1

  Mil                          6 276,1

  Blé, NBC, sésame             3 410,1

  Total                        315 264
  -----------------------------------------------

*Source : « Initiative collecte bord champ : défis et perspectives ».*

Le bilan relève quatre ruptures : une **collecte dispersée en plein
air** (surcoûts, risques de vol), une **dégradation de la qualité**, une
**saturation logistique** (16 camions, retards de kits) et des
**ruptures de trésorerie** qui retardent les paiements. La phase 2
restructure la collecte autour de **comptoirs fixes** : 19 hubs
principaux (plus de 5 000 t), 78 comptoirs secondaires (1 000 à 4 999 t)
et 178 comptoirs intermédiaires, la collecte itinérante restant possible
dans les micro-zones. Les cibles 2026 sont de 300 000 t, 389 camions, 7
millions d\'emballages, 16 vanneuses et 304 agents équipés d\'un kit de
collecte.

## I.2 -- Objectifs

## 

**Objectif général** :

> L'objectif global du projet est de concevoir et mettre en œuvre une
> plateforme sécurisée et évolutive couvrant, la Gestion de la collecte,
> la Comptabilité, le Transfert des stocks, la Statistique et le
> Paramétrage.

**Objectifs spécifiques** :

Conformément au présent cahier de charges, il s'agit spécifiquement de :

> • concevoir une plateforme sécurisée et évolutive ;
>
> • Déployer la future plateforme ;
>
> • former les utilisateurs sur l'utilisation de ladite plateforme ;
>
> • élaborer et mettre en œuvre un plan de vulgarisation de la
> plateforme ;
>
> • organiser une cérémonie de lancement officiel.

## I.3 - Livrables attendus

Les livrables suivants sont attendus à l'issue du projet :

• la note de cadrage ;

• le cahier de charges ;

• les fiches de description des workflows ;

• le dossier technique de conception ;

• les manuels d'utilisation ;

• les manuels de déploiement et d'exploitation ;

• le guide du développeur ;

• les code sources ;

• le plan de vulgarisation ;

• le rapport d'audit de sécurité ;

• le rapport de validation de tests fonctionnels ;

• le rapport de tests de performance ;

• l'avis de non objection de l'ANSSI ;

• le lien de la plateforme en ligne.

# II - Présentation du domaine

## II.1 - Environnement réglementaire

  ---------------------------------------------------------------
  **Texte**                                 **Objet utile au
                                            projet**
  ----------------------------------------- ---------------------
  Loi n°025-99/AN du 16 novembre 1999 et    Réglementation
  décrets n°2000-189 à 2000-192 du 17 mai   générale des sociétés
  2000                                      à capitaux publics

  Décret                                    Missions de la
  n°2024-0979/PRES/PM/MDICAPME/MARAH/MEFP   SONAGESS
  du 22 août 2024                           (constitution des
                                            stocks, achats au
                                            prix minimum garanti,
                                            collecte bord champ)

  Délibération n°2025-020/MARAH/CA/SONAGESS Organigramme de la
                                            SONAGESS

  Manuel de procédures spécifique de la     Procédures C.1 à C.6,
  collecte bord champ et de la              supports de gestion
  transformation (octobre 2025)             (annexes 1 à 11)

  Fiches de description de workflow         Procédures de
  SONAGESS_2026_001, SONAGESS_2026_002 et   collecte, de paiement
  SONAGESS_2026_003 (octobre 2026)          et de transfert de
                                            stocks à
                                            dématérialiser

  Loi n°005-2024/ALT du 20 avril 2024 et    Commande publique
  textes d\'application                     (recrutement du
                                            prestataire,
                                            acquisition du
                                            matériel)

  Arrêté n°2023-215/MEFP/SG/DGI             Facture normalisée

  Droit comptable OHADA (SYSCOHADA)         Comptabilisation des
                                            achats, avances et
                                            stocks

  La loi n°001-2021/AN du 30 mars 2021      Traitement des
  portant protection des personnes à        données des
  l'égard du traitement des données à       producteurs
  caractère personnel qui encadre le        (identité, CNIB,
  traitement des données à caractère        numéros de paiement)
  personnel relatives aux communications    
  électroniques                             

  Le décret                                 Exigences du
  n°2024-0922/PRES/PM/MTDPCE/MFPTPS/MEFP du développement des
  02 Aout 2024 portant modalités de mise en plateformes de
  exploitation des plateformes numériques   l'administration
  de l'administration publique de service   publique
  qui fixe les conditions de mise en        
  exploitation des plateformes de           
  l'administration                          

  Référentiel général de sécurité, guide    Exigences de sécurité
  d\'intégration de la sécurité, documents  (partie Conception,
  de l\'ANSSI                               section 9)
  ---------------------------------------------------------------

## 

## II.2 - Environnement technologique

- **Existant** : supports papier ; paiements digitaux via plusieurs
  opérateurs sans plateforme centrale ; reporting manuel de fin de
  journée.

- **Solution retenue** : une application unique(web et mobile)
  développée sur mesure. La collecte se fait sur tablettes Android avec
  l\'application mobile SONAGESS, qui peut fonctionner hors ligne.

- **Paiement** : API de décaissement des opérateurs mobile money (Sank
  Money, LigdiCash, Coris Money, Moov Money, Orange Money) ou d\'un
  agrégateur.

- **Connectivité** : couverture 3G/4G variable selon les comptoirs ; la
  plateforme doit tolérer l\'absence de réseau.

- **Systèmes à interfacer** : logiciel comptable de la DFC.

## II.3 - Parties prenantes

Le dispositif comprend trois niveaux : **central** (Direction générale :
planification, suivi, contrôle), **régional** (points focaux SONAGESS,
directions régionales et provinciales du ministère en charge de
l\'agriculture) et **opérationnel** (comptoirs : agents collecteurs,
agents payeurs, gardiens, transporteurs). Un comité de suivi
ministère--SONAGESS pilote l\'ensemble.

  -------------------------------------------------------------
  **Partie prenante**   **Rôle dans le       **Utilisation de
                        projet**             la plateforme**
  --------------------- -------------------- ------------------
  Direction générale,   Maître d\'ouvrage,   Tableau de bord,
  CODIR(Conseil de      arbitrages           approbations
  Direction)                                 au-delà des seuils

  DCT (Direction de la  Métier principal,    Examen (SCP) et
  collecte et de la     porteur de la        validation (DCT)
  transformation) et    procédure            des documents de
  Service collecte et   SONAGESS_2026_001,   collecte,
  production (SCP)      propriétaire du      paramétrage, suivi
                        produit              

  DFC : directeur,      Métier finances,     Tri des pièces,
  Service des finances  porteur de la        initiation et
  et du budget (SFB),   procédure            validation des
  service de la         SONAGESS_2026_002    paiements, prise
  comptabilité                               en charge,
                                             lettrage,
                                             comptabilisation

  DGSA et Service de la Porteur de la        Magasins de
  gestion des stocks    procédure            destination,
  (SGS)                 SONAGESS_2026_003    demande de
                                             transfert, suivi
                                             des stocks reçus

  DPL, DMP              Logistique ;         Programmes et
                        recrutement de       états de transport
                        transporteurs privés 

  Direction des         Pilotage de la       Validation des
  systèmes              dématérialisation    fiches de
  d\'information du     des procédures       workflow,
  ministère en charge                        accompagnement
  de l\'agriculture                          

  Service de            Maître d\'œuvre      Administration,
  l\'informatique et de interne,             support
  la digitalisation     exploitation         

  DAI, contrôle interne Contrôle             Consultation,
                                             journal d\'audit

  Points focaux,        Relais terrain       Consultation des
  directions régionales                      données de leur
  et provinciales du                         zone
  ministère, chefs ZATA                      
  / UATA                                     

  Agents collecteurs,   Utilisateurs terrain Application mobile
  chefs de comptoir,                         
  agents payeurs                             

  Producteurs et        Bénéficiaires        Reçus SMS,
  coopératives                               paiements

  Opérateurs de         Prestataires         API de paiement ;
  paiement,                                  ordres de
  transporteurs                              transport

  Comité de suivi       Pilotage de la       Rapports
  ministère--SONAGESS   campagne             

  Autres structures     Appui technique      Developpement,
  (ANSSI, ANPTIC, DGTD,                      ANO, Avis
  INSD)                                      technique et mise
                                             en ligne
  -------------------------------------------------------------

## 

## II.4 - Périmètre du projet

La plateforme couvre les **cinq modules** retenus par la SONAGESS :
paramétrage, collecte, paiement et comptabilité, transfert des stocks et
statistique. Elle est développée **sur mesure, comme une seule
application** : l\'application mobile des agents et l\'application web
des directions partagent les mêmes données, les mêmes comptes et les
mêmes règles.

**Hors périmètre** : la planification budgétaire initiale (procédure
C.1), la comptabilité générale de la société et la passation des
marchés. La plateforme exporte vers les outils de la DFC sans les
remplacer.

# III - Spécifications fonctionnelles

Les spécifications fonctionnelles déterminent ce que la plateforme doit
faire, sans préjuger de la manière de l\'implémenter.

## III.1 - Fonctionnalités générales

La procédure est décrite en cinq workflows, au format des fiches de
description de workflow de la Direction des systèmes d\'information. Les
workflows 1, 2 et 4 reprennent les fiches officielles SONAGESS_2026_001
(gestion de la collecte), SONAGESS_2026_002 (paiement et comptabilité)
et SONAGESS_2026_003 (transfert de stocks). Les durées sont des cibles à
confirmer après mesure des délais réels.

![circuit circuit](media/image1.png){width="6.458333333333333in"
height="3.21875in"}

*Figure 1 -- De la collecte au paiement*

### 

### 

### WF1 - Gestion de la collecte (SONAGESS_2026_001)

Direction porteuse : DCT ; service porteur : Service collecte et
production (SCP).

  ----------------------------------------------------------------------------------------------------
  **N°**   **Acteur /   **Tâches           **Pièces        **Réponses     **Durée    **Contraintes**
           service**    effectuées**       attendues**     possibles /    cible**    
                                                           suite**                   
  -------- ------------ ------------------ --------------- -------------- ---------- -----------------
  1        Agent        Échantillonne et   Fiche de        Peser ou       Immédiat   Kit de collecte
           collecteur   analyse le produit qualité         rejeter                   étalonné
                        ; remplit la fiche                                           
                        de qualité dans                                              
                        l\'application ;                                             
                        notifie au                                                   
                        producteur la                                                
                        conformité ou le                                             
                        rejet                                                        

  2        Agent        Supervise le       Document de     Réception ou   Immédiat   ---
           collecteur   nettoyage et le    dénombrement,   rejet                     
                        conditionnement    fiche de pesée                            
                        (ensachage) ;                                                
                        dénombre les sacs                                            
                        et constate le                                               
                        poids total par                                              
                        pesées                                                       

  3        Agent        Établit le BRBC,   BRBC, fiche de  Dossier        1 j        Stabilité de la
           collecteur   le fait co-signer  pesée, fiche    transmis                  plateforme,
                        par le producteur  journalière des                           réseau (envoi
                        sur l\'écran,      stocks                                    différé possible)
                        remet une copie                                              
                        (impression ou                                               
                        SMS), met à jour                                             
                        la fiche                                                     
                        journalière des                                              
                        stocks et transmet                                           
                        le tout                                                      
                        électroniquement à                                           
                        la DCT                                                       

  4        Plateforme   Contrôle           Dossier de      Dossier        Immédiat   ---
                        automatiquement le collecte        contrôlé ou               
                        dossier (doublons,                 signalé                   
                        GPS, poids, prix,                                            
                        étalonnage) et                                               
                        signale les                                                  
                        anomalies                                                    

  5        Chef du      Examine les        BRBC, fiches    Transmission   1 j        Disponibilité des
           Service      documents (fiche   journalière et  au DCT ou                 acteurs
           collecte et  de qualité, BRBC,  mensuelle des   rejet                     
           production   fiches de stock),  stocks                                    
           (SCP)        vérifie                                                      
                        l\'exactitude des                                            
                        informations,                                                
                        transmet au DCT ou                                           
                        rejette avec motif                                           
                        (retour sur la                                               
                        tablette)                                                    

  6        DCT          Valide les         Fiche de        Bordereau de   1 j        Disponibilité des
                        documents de       qualité, fiche  transmission à            acteurs
                        gestion            de pesée, BRBC, la DFC ou                 
                        (numérotation      fiches de stock rejet                     
                        officielle du                                                
                        BRBC) et les                                                 
                        transmet à la DFC                                            
                        pour paiement :                                              
                        état récapitulatif                                           
                        des états de                                                 
                        paiement et                                                  
                        demande de                                                   
                        déblocage                                                    
  ----------------------------------------------------------------------------------------------------

### 

### WF2 - Paiement et comptabilisation des spéculations, de la manutention et du gardiennage (SONAGESS_2026_002)

Direction porteuse : DFC ; service porteur : Service des finances et du
budget (SFB).

  ------------------------------------------------------------------------------------------------------
  **N°**   **Acteur /     **Tâches         **Pièces         **Réponses       **Durée   **Contraintes**
           service**      effectuées**     attendues**      possibles /      cible**   
                                                            suite**                    
  -------- -------------- ---------------- ---------------- ---------------- --------- -----------------
  1        Directeur DFC  Réceptionne le   Bordereau de     Transmission au  1 j       Stabilité de la
                          bordereau de la  transmission     SFB ou rejet à             plateforme,
                          DCT ; génère le  DCT, demande de  la DCT                     disponibilité des
                          bordereau de     déblocage,                                  acteurs
                          transmission des pièces                                      
                          pièces acceptées justificatives                              
                          au chef du SFB                                               
                          et le bordereau                                              
                          de rejet des                                                 
                          pièces rejetées                                              
                          à la DCT                                                     

  2        Chef du SFB    Réceptionne ;    Bordereau DFC,   Transmission à   1 j       Idem
                          transmet les     pièces           l\'agent                   
                          pièces acceptées                  initiateur ou              
                          à l\'agent                        rejet                      
                          initiateur ;                                                 
                          renvoie les                                                  
                          rejets au                                                    
                          directeur                                                    

  3        Agent          Réceptionne ;    Bordereau SFB,   Bordereau des    1 j       Idem
           initiateur du  initie le        pièces           paiements                  
           paiement (SFB) paiement des                      initiés ou rejet           
                          pièces acceptées                                             
                          vers                                                         
                          l\'opérateur de                                              
                          la zone (ou                                                  
                          l\'agent payeur                                              
                          pour les                                                     
                          espèces) ;                                                   
                          renvoie les                                                  
                          rejets                                                       

  4        Chef du SFB    Vérifie les      Paiements        Rapport de       1 j       Idem
                          éléments, valide initiés          paiement ou                
                          le paiement                       rejet                      
                          (second facteur)                                             
                          ; la plateforme                                              
                          transmet les                                                 
                          ordres à                                                     
                          l\'opérateur ;                                               
                          génère le                                                    
                          rapport de                                                   
                          paiement pour la                                             
                          comptabilité                                                 

  5        Chef du        Vérifie, valide  Rapport de       Bordereau de     1 j       Idem
           service de la  et prend en      paiement, pièces prise en charge            
           comptabilité   charge                            ou rejet                   
                          l\'opération ;                                               
                          renvoie les                                                  
                          rejets au SFB                                                

  6        Chef du        Réceptionne les  Pièces           État             1 j       Idem
           service de la  pièces           justificatives   récapitulatif              
           comptabilité   justificatives                    des opérations             
                          et lettre les                     lettrées                   
                          opérations                                                   

  7        Chef du        Comptabilise     Opérations       Écritures        1 j       Idem
           service de la  l\'opération     lettrées         comptabilisées             
           comptabilité   dans le logiciel                                             
                          comptable                                                    
                          (export de la                                                
                          plateforme)                                                  
  ------------------------------------------------------------------------------------------------------

### 

### WF3 - Paiement en espèces

  -----------------------------------------------------------------------------------------------------
  **N°**   **Acteur /     **Tâches        **Pièces       **Réponses    **Durée        **Contraintes**
           service**      effectuées**    attendues**    possibles /   cible**        
                                                         suite**                      
  -------- -------------- --------------- -------------- ------------- -------------- -----------------
  1        Plateforme     Après           BRBC validé en Affectation   Immédiat       
                          validation du   mode espèces   de paiement                  
                          chef du SFB,                   en espèces                   
                          crée une                                                    
                          affectation de                                              
                          paiement en                                                 
                          espèces pour                                                
                          l\'agent payeur                                             
                          avec le BRBC et                                             
                          le montant                                                  

  2        Agent payeur   Vérifie la      Affectation de Paiement      À la           Sécurité des
                          pièce           paiement en    enregistré    présentation   fonds
                          d\'identité,    espèces                      du producteur  
                          remet le                                                    
                          montant exact,                                              
                          photographie le                                             
                          BRBC acquitté                                               

  3        Agent payeur,  Reddition de    Paiements en   Caisse soldée Hebdomadaire   
           SFB,           caisse et       espèces                                     
           comptabilité   justification   synchronisés                                
                          de l\'avance                                                
  -----------------------------------------------------------------------------------------------------

### 

### WF4 - Transfert des stocks (SONAGESS_2026_003)

Direction porteuse : DGSA ; service porteur : Service de la gestion des
stocks (SGS). Durée actuelle : 15 jours.

  ----------------------------------------------------------------------------------------------------
  **N°**   **Acteur /   **Tâches           **Pièces        **Réponses      **Durée   **Contraintes**
           service**    effectuées**       attendues**     possibles /     cible**   
                                                           suite**                   
  -------- ------------ ------------------ --------------- --------------- --------- -----------------
  1        DCT          Fait le point des  BRBC validés    Situation des   1 j       Non-respect des
                        quantités                          stocks                    délais de
                        collectées par                     collectés                 transmission
                        comptoir                                                     

  2        DCT          Transmet           Situation       Accusé de       1 j       
                        électroniquement                   réception                 
                        la situation à la                                            
                        DGSA et à la DPL                                             

  3        Directeur    Réceptionne la     Situation des   Fiche           1 j       
           DGSA         situation et       stocks          d\'imputation             
                        l\'impute au SGS   collectés       au SGS                    

  4        Chef du SGS  Identifie les      Fiche           Demande de      1 j       Indisponibilité
                        magasins de        d\'imputation   transfert                 de magasins
                        réception, les                                               
                        fait préparer ;                                              
                        rédige la demande                                            
                        de transfert à la                                            
                        DPL, signée par le                                           
                        DGSA                                                         

  5        DPL          Programme les      Demande de      Programme et    1 j       Lenteur du
                        camions SONAGESS ; transfert       états de                  traitement par la
                        confie le reste à                  transport                 DMP
                        la DMP pour                                                  
                        recruter des                                                 
                        transporteurs                                                
                        privés ; établit                                             
                        le programme et                                              
                        les états de                                                 
                        transport ; les                                              
                        transmet à la DCT                                            
                        et à la DGSA                                                 

  6        DGSA, chef   Réceptionnent les  Bordereau de    États transmis  1 j       
           du SGS       états de           transmission    au magasinier             
                        transport, les     DPL                                       
                        imputent et les                                              
                        transmettent au                                              
                        magasinier                                                   

  7        DCT, agent   La DCT informe les État de         Bordereau       1 j       Arrivée tardive
           collecteur   agents (état de    transport       d\'enlèvement             des camions
                        transport transmis                                           
                        sur la tablette) ;                                           
                        l\'agent charge le                                           
                        camion en présence                                           
                        du chauffeur,                                                
                        établit le                                                   
                        bordereau                                                    
                        d\'enlèvement et                                             
                        le co-signe avec                                             
                        le transporteur                                              

  8        Magasinier   Reçoit le camion,  Bordereau       Bordereau de    1 j       
                        vérifie les        d\'enlèvement   réception                 
                        documents,                                                   
                        décharge, établit                                            
                        le bordereau de                                              
                        réception co-signé                                           
                        avec le                                                      
                        transporteur,                                                
                        renseigne les                                                
                        fiches de stocks,                                            
                        transmet                                                     
                        électroniquement                                             
                        la situation au                                              
                        SGS                                                          

  9        Chef du SGS  Vérifie les        Bordereaux de   Situation des   1 j       Disponibilité de
                        quantités reçues   réception       stocks reçus de           l\'information à
                        par rapport aux                    la collecte               temps
                        quantités                                                    
                        annoncées par la                                             
                        DCT et met à jour                                            
                        la situation des                                             
                        stocks                                                       
  ----------------------------------------------------------------------------------------------------

### WF5 - Rapprochement et clôture

  ------------------------------------------------------------------------------------------------
  **N°**   **Acteur /     **Tâches        **Pièces      **Réponses   **Durée     **Contraintes**
           service**      effectuées**    attendues**   possibles /  cible**     
                                                        suite**                  
  -------- -------------- --------------- ------------- ------------ ----------- -----------------
  1        Plateforme     Rapproche les   Relevés       Liste des    Quotidien   
                          paiements avec                écarts                   
                          les relevés des                                        
                          opérateurs et                                          
                          les caisses des                                        
                          agents payeurs                                         

  2        Chef du        Traite les      Écarts,       Période      Mensuel     
           service de la  écarts, exporte écritures     clôturée                 
           comptabilité   les écritures,                                         
                          clôture la                                             
                          période                                                
  ------------------------------------------------------------------------------------------------

## 

## III.2 - Fonctionnalités spécifiques

### III.2.1 - Diagramme de cas d\'utilisation

![usecase usecase](media/image2.png){width="6.25in"
height="4.774354768153981in"}

*Figure 2 -- Diagramme de cas d\'utilisation*

### III.2.2 - Description des cas d\'utilisation

**UC01 - Gérer campagnes et objectifs**

  -----------------------------------------------------
  **Rubrique**    **Description**
  --------------- -------------------------------------
  Acteurs         DCT ; approbation Direction

  Préconditions   Utilisateur habilité

  Scénario        Créer la campagne (dates) ; saisir
  nominal         les objectifs par spéculation,
                  région, province, comptoir ;
                  soumettre ; approuver.

  Exceptions      Objectifs supérieurs à la capacité
                  des comptoirs : alerte.

  Résultat        Campagne active ; objectifs visibles
                  dans la Statistique.
  -----------------------------------------------------

**UC02 - Gérer prix, seuils, plafonds**

  -----------------------------------------------------
  **Rubrique**    **Description**
  --------------- -------------------------------------
  Acteurs         DCT, DFC ; approbation Direction

  Préconditions   Campagne créée

  Scénario        Saisir le prix par spéculation et
  nominal         région (ou coût de production +
                  marge), la date d\'effet, les seuils
                  qualité, les plafonds ; soumettre ;
                  approuver.

  Exceptions      Prix sans approbation : inactif. Date
                  d\'effet passée : refusée.

  Résultat        Paramètres envoyés automatiquement
                  aux tablettes.
  -----------------------------------------------------

**UC03 - Gérer paramétrage et comptes**

  -----------------------------------------------------
  **Rubrique**    **Description**
  --------------- -------------------------------------
  Acteurs         Administrateur, DCT

  Préconditions   ---

  Scénario        Créer ou importer comptoirs,
  nominal         producteurs, collecteurs agréés,
                  magasins, opérateurs et zones ; créer
                  les comptes, rôles et périmètres ;
                  affecter les tablettes.

  Exceptions      Doublon : refusé.

  Résultat        Paramétrage historisé.
  -----------------------------------------------------

**UC04 - Gérer comptoir et personnel**

  -----------------------------------------------------
  **Rubrique**    **Description**
  --------------- -------------------------------------
  Acteurs         DCT, chef de comptoir

  Préconditions   Comptoir référencé

  Scénario        Renseigner type, capacité, GPS,
  nominal         opérateur exclusif ; affecter le
                  personnel ; enregistrer la formation
                  ; ouvrir le comptoir.

  Exceptions      Personnel non formé : ouverture
                  bloquée.

  Résultat        Comptoir ouvert, visible sur la
                  carte.
  -----------------------------------------------------

**UC05 - Suivre stock, kit, emballages**

  -----------------------------------------------------
  **Rubrique**    **Description**
  --------------- -------------------------------------
  Acteurs         Chef de comptoir ; DPL

  Préconditions   Comptoir ouvert

  Scénario        Remplir chaque soir la fiche
  nominal         journalière des stocks ; la
                  plateforme calcule l\'écart de stock,
                  le stock d\'emballages, l\'état du
                  kit ; les besoins sont transmis.

  Exceptions      Écart \> 2 % ou bascule non étalonnée
                  : alerte.

  Résultat        Situation du comptoir à jour.
  -----------------------------------------------------

**UC06 - Enregistrer une collecte**

  -----------------------------------------------------
  **Rubrique**    **Description**
  --------------- -------------------------------------
  Acteurs         Agent collecteur

  Préconditions   Tablette affectée au comptoir

  Scénario        Contrôler la qualité ; peser ;
  nominal         établir le BRBC ; établir l'état de
                  frais de manutention ; choisir le
                  mode de paiement ; faire accepter ;
                  photographier les fiches ;
                  synchroniser.

  Exceptions      Lot non conforme : refus. Hors réseau
                  : envoi différé.

  Résultat        Collecte reçue par la plateforme.
  -----------------------------------------------------

**UC07 - Examiner et valider une collecte**

  -----------------------------------------------------
  **Rubrique**    **Description**
  --------------- -------------------------------------
  Acteurs         Chef du SCP (examen), DCT
                  (validation)

  Préconditions   Collecte synchronisée

  Scénario        Consulter le dossier (fiche de
  nominal         qualité, pesées, BRBC, photos, carte,
                  alertes) ; le chef du SCP transmet au
                  DCT ou rejette avec motif ; le DCT
                  valide ou rejette ; tout rejet
                  revient sur la tablette de l\'agent.

  Exceptions      Rejet : retour sur la tablette.

  Résultat        BRBC validé et numéroté.
  -----------------------------------------------------

**UC08 - Établir les états de paiement**

  -----------------------------------------------------
  **Rubrique**    **Description**
  --------------- -------------------------------------
  Acteurs         DCT

  Préconditions   BRBC validés, états validés

  Scénario        Regrouper par comptoir les montants
  nominal         des spéculations, de la manutention
                  et du gardiennage ; produire l\'état
                  récapitulatif et la demande de
                  déblocage ; générer le bordereau de
                  transmission à la DFC.

  Exceptions      Pièce manquante : état bloqué.

  Résultat        Dossier transmis à la DFC.
  -----------------------------------------------------

**UC09 - Trier et transmettre (DFC)**

  -----------------------------------------------------
  **Rubrique**    **Description**
  --------------- -------------------------------------
  Acteurs         Directeur DFC, chef du SFB

  Préconditions   Bordereau de transmission reçu

  Scénario        Contrôler les pièces ; générer le
  nominal         bordereau des pièces acceptées vers
                  l\'étape suivante et le bordereau de
                  rejet vers l\'étape précédente.

  Exceptions      Toutes les pièces rejetées : retour
                  complet à la DCT.

  Résultat        Pièces acceptées transmises.
  -----------------------------------------------------

**UC10 - Initier et valider le paiement**

  -----------------------------------------------------
  **Rubrique**    **Description**
  --------------- -------------------------------------
  Acteurs         Agent initiateur (SFB), chef du SFB

  Préconditions   Pièces acceptées

  Scénario        L\'agent initie ; le chef du SFB
  nominal         valide avec second facteur ; la
                  plateforme envoie les ordres à
                  l\'opérateur avec une référence
                  unique et produit le rapport de
                  paiement.

  Exceptions      Initiateur = validateur : interdit.
                  Échec opérateur : relance après
                  correction tracée.

  Résultat        Producteurs payés ; rapport transmis
                  à la comptabilité.
  -----------------------------------------------------

**UC11 - Payer en espèces**

  -----------------------------------------------------
  **Rubrique**    **Description**
  --------------- -------------------------------------
  Acteurs         Agent payeur

  Préconditions   Paiement espèces validé par le chef
                  du SFB

  Scénario        Ouvrir l\'affectation ; vérifier la
  nominal         pièce ; remettre le montant ;
                  photographier le BRBC acquitté.

  Exceptions      Pièce non conforme : paiement refusé.

  Résultat        Paiement enregistré ; caisse
                  décrémentée.
  -----------------------------------------------------

**UC12 - Rapprocher**

  -----------------------------------------------------
  **Rubrique**    **Description**
  --------------- -------------------------------------
  Acteurs         Plateforme, service de la
                  comptabilité

  Préconditions   Paiements émis

  Scénario        Importer les relevés ; rapprocher ;
  nominal         lister et traiter les écarts.

  Exceptions      Écart non expliqué : escalade.

  Résultat        Paiements rapprochés.
  -----------------------------------------------------

**UC13 - Prendre en charge et lettrer**

  -----------------------------------------------------
  **Rubrique**    **Description**
  --------------- -------------------------------------
  Acteurs         Chef du service de la comptabilité

  Préconditions   Rapport de paiement

  Scénario        Vérifier ; prendre en charge
  nominal         (bordereau de prise en charge) ;
                  réceptionner les pièces ; lettrer.

  Exceptions      Pièce non conforme : bordereau de
                  rejet au SFB.

  Résultat        Opérations lettrées.
  -----------------------------------------------------

**UC14 - Suivre avances et caisses**

  -----------------------------------------------------
  **Rubrique**    **Description**
  --------------- -------------------------------------
  Acteurs         Service de la comptabilité

  Préconditions   Avances aux agents payeurs

  Scénario        Suivre avances, justifications,
  nominal         soldes ; régulariser.

  Exceptions      Avance non justifiée à échéance :
                  alerte.

  Résultat        Avances soldées.
  -----------------------------------------------------

**UC15 - Comptabiliser et exporter**

  -----------------------------------------------------
  **Rubrique**    **Description**
  --------------- -------------------------------------
  Acteurs         Chef du service de la comptabilité

  Préconditions   Opérations lettrées

  Scénario        Générer les écritures SYSCOHADA ;
  nominal         exporter vers le logiciel comptable ;
                  clôturer la période.

  Exceptions      Écarts non traités : clôture bloquée.

  Résultat        Écritures comptabilisées.
  -----------------------------------------------------

**UC16 - Programmer le transfert**

  -----------------------------------------------------
  **Rubrique**    **Description**
  --------------- -------------------------------------
  Acteurs         DCT, DGSA, SGS, DPL, DMP

  Préconditions   Situation des stocks collectés

  Scénario        La DCT transmet la situation ; la
  nominal         DGSA impute au SGS ; le SGS choisit
                  et prépare les magasins, rédige la
                  demande ; la DPL programme les
                  camions SONAGESS et confie le reste à
                  la DMP ; les états de transport sont
                  transmis.

  Exceptions      Magasins indisponibles ou camions
                  insuffisants : alerte.

  Résultat        États de transport transmis aux
                  comptoirs.
  -----------------------------------------------------

**UC17 - Enlever**

  -----------------------------------------------------
  **Rubrique**    **Description**
  --------------- -------------------------------------
  Acteurs         Agent collecteur

  Préconditions   État de transport

  Scénario        Charger ; saisir sacs et tonnage ;
  nominal         faire signer le transporteur ;
                  photographier ; synchroniser.

  Exceptions      Tonnage \> stock : bloqué.

  Résultat        Bordereau d\'enlèvement.
  -----------------------------------------------------

**UC18 - Réceptionner en magasin**

  -----------------------------------------------------
  **Rubrique**    **Description**
  --------------- -------------------------------------
  Acteurs         Magasinier, chef du SGS

  Préconditions   Bordereau d\'enlèvement

  Scénario        Contrôler ; saisir le bordereau de
  nominal         réception co-signé ; la plateforme
                  calcule l\'écart, met à jour les
                  fiches de stock et transmet la
                  situation au SGS, qui la vérifie.

  Exceptions      Écart \> tolérance : alerte DGSA.

  Résultat        Stock en magasin.
  -----------------------------------------------------

**UC19 - Vanner, décortiquer**

  -----------------------------------------------------
  **Rubrique**    **Description**
  --------------- -------------------------------------
  Acteurs         Responsable transformation

  Préconditions   Stock reçu

  Scénario        Saisir les entrées, sorties, produits
  nominal         finis ; calculer les rendements ;
                  saisir la maintenance.

  Exceptions      Rendement anormal : alerte.

  Résultat        Fiches usine.
  -----------------------------------------------------

**UC20 - Consulter le tableau de bord**

  -----------------------------------------------------
  **Rubrique**    **Description**
  --------------- -------------------------------------
  Acteurs         Tous selon périmètre

  Préconditions   Compte actif

  Scénario        Consulter indicateurs, carte, alertes
  nominal         ; descendre jusqu\'aux collectes.

  Exceptions      ---

  Résultat        ---
  -----------------------------------------------------

**UC21 - Éditer états et exports**

  -----------------------------------------------------
  **Rubrique**    **Description**
  --------------- -------------------------------------
  Acteurs         Selon matrice des droits

  Préconditions   Données validées

  Scénario        Choisir l\'état et les filtres ;
  nominal         exporter en Excel ou PDF.

  Exceptions      ---

  Résultat        État produit.
  -----------------------------------------------------

### 

### III.2.3 - Diagramme d\'activités

Le diagramme ci-dessous détaille le cas le plus fréquent : de la
collecte au paiement (UC06 à UC10).

![activite activite](media/image3.png){width="5.416666666666667in"
height="6.427083333333333in"}

*Figure 3 -- Diagramme d\'activités de la collecte au paiement*

### 

### III.2.4 - Exigences détaillées par module

Priorité : 1 = indispensable au lancement de la campagne 2026 ; 2 =
attendu en version 1 ; 3 = évolution.

### Application mobile (transversale aux modules)

  ----------------------------------------------------------------
  **Réf.**   **Exigence**                           **Priorité**
  ---------- -------------------------------------- --------------
  MO-01      Application Android développée sur     1
             mesure, installée et mise à jour à     
             distance sur les tablettes de la       
             SONAGESS.                              

  MO-02      Fonctionnement complet sans réseau :   1
             saisie, calculs, contrôles et stockage 
             local chiffré ; synchronisation        
             automatique et reprise après coupure.  

  MO-03      Formulaires : listes, filtres,         1
             contrôles bloquants et alertes,        
             calculs (montants, décision qualité).  

  MO-04      Capture GPS, photos compressées,       1
             signature du producteur et de l\'agent 
             sur l\'écran.                          

  MO-05      Copie du BRBC au producteur :          1
             impression sur imprimante portable.    

  MO-06      Tâches reçues depuis l\'application    1
             web : états de transport, paiements en 
             espèces, retours de rejet à corriger.  

  MO-07      Connexion par identifiant et code PIN  1
             ; verrouillage automatique.            

  MO-08      Interface en français, gros boutons,   1
             utilisable en plein soleil ; aide      
             intégrée à chaque écran.               

  MO-09      Lecture de codes-barres ou QR          2
             (bascule, sacs, bordereaux).           
  ----------------------------------------------------------------

### Module 1 -- Paramétrage

  ----------------------------------------------------------------
  **Réf.**   **Exigence**                           **Priorité**
  ---------- -------------------------------------- --------------
  PA-01      Gérer les campagnes et les objectifs   1
             de collecte par spéculation, région,   
             province et comptoir.                  

  PA-02      Gérer les prix de collecte avec date   1
             d\'effet et approbation ; calcul       
             possible à partir du coût de           
             production et de la marge incitative.  

  PA-03      Gérer le paramétrage : découpage     1
             administratif, comptoirs, producteurs, 
             collecteurs agréés, magasins, unités,  
             spéculations, variétés.                

  PA-04      Gérer les opérateurs de paiement et    1
             leur zone exclusive par comptoir.      

  PA-05      Paramétrer seuils qualité, plafonds,   1
             tolérances, barèmes (manutention,      
             transport, commissions).               

  PA-06      Gérer utilisateurs, rôles et           1
             périmètres ; un seul compte par        
             personne pour l\'application mobile et 
             l\'application web.                    

  PA-07      Envoyer automatiquement les            1
             référentiels et paramètres (comptoirs, 
             producteurs, prix, seuils) vers les    
             tablettes ; historiser les             
             modifications.                         

  PA-08      Gerer les comptoirs                    

  PA-09      Modifier les formulaires mobiles       2
             (questions, listes, contrôles) par     
             paramétrage, sans redévelopper         
             l\'application, avec gestion des       
             versions.                              
  ----------------------------------------------------------------

### 

### Module 2 -- Collecte

  ----------------------------------------------------------------
  **Réf.**   **Exigence**                           **Priorité**
  ---------- -------------------------------------- --------------
  CL-01      Recevoir les formulaires dès que la    1
             tablette capte le réseau ; aucune      
             donnée perdue en cas de coupure.       

  CL-02      Afficher chaque collecte comme une     1
             fiche lisible (producteur, qualité,    
             pesées, montants, photos, carte).      

  CL-03      Contrôles automatiques : doublons,     1
             GPS, poids atypique, bascule non       
             étalonnée, plafonds.                   

  CL-04      Circuit officiel SONAGESS_2026_001 :   1
             examen par le chef du Service collecte 
             et production, puis validation par le  
             DCT ; les DR et les points focaux      
             consultent leur zone.                  

  CL-05      La DCT établit l\'état récapitulatif   1
             des états de paiement et la demande de 
             déblocage, transmis électroniquement à 
             la DFC.                                

  CL-06      Rejet avec motif obligatoire, renvoyé  1
             automatiquement sur la tablette de     
             l\'agent pour correction.              

  CL-07      Génération automatique de la fiche de  1
             pesée, de la fiche journalière et de   
             la fiche mensuelle des stocks à partir 
             des collectes.                         

  CL-08      Numérotation officielle et édition PDF 1
             du BRBC ; rapprochement avec le carnet 
             papier pendant la transition.          

  CL-09      Validation en masse ; notification des 2
             collectes en attente au-delà de 48     
             heures.                                
  ----------------------------------------------------------------

### 

### Module 3 -- Paiement et comptabilité

  ----------------------------------------------------------------
  **Réf.**   **Exigence**                           **Priorité**
  ---------- -------------------------------------- --------------
  PY-01      Réception électronique des dossiers de 1
             paiement de la DCT (spéculations,      
             manutention, gardiennage) avec demande 
             de déblocage et pièces justificatives. 

  PY-02      À chaque étape (Directeur DFC, chef du 1
             SFB, agent initiateur), génération     
             automatique du bordereau électronique  
             de transmission des pièces acceptées   
             et du bordereau de rejet des pièces    
             rejetées, avec retour à l\'étape       
             précédente.                            

  PY-03      Initiation du paiement par l\'agent    1
             initiateur et validation par le chef   
             du SFB, deux personnes distinctes,     
             avec second facteur.                   

  PY-04      Intégration multi-opérateurs           1
             (opérateur de la zone) ; référence     
             unique par bénéficiaire contre tout    
             double paiement.                       

  PY-05      Statuts de paiement, confirmation de   1
             l\'opérateur, relance des échecs après 
             correction tracée ; rapport de         
             paiement transmis au service de la     
             comptabilité.                          

  PY-06      Paiement en espèces par affectation à  1
             l\'agent payeur et reddition de        
             caisse.                                

  PY-07      Suivi du délai de chaque étape par     1
             rapport à la cible (1 jour) et alerte  
             en cas de dépassement.                 

  PY-08      Rapprochement quotidien avec relevés   1
             opérateurs et caisses.                 

  PY-09      Quittance électronique rattachée au    2
             BRBC.                                  
  ----------------------------------------------------------------

### 

  ----------------------------------------------------------------
  **Réf.**   **Exigence**                           **Priorité**
  ---------- -------------------------------------- --------------
  CP-01      Prise en charge du paiement par le     1
             service de la comptabilité :           
             vérification, validation, bordereau de 
             prise en charge ou de rejet au SFB.    

  CP-02      Réception des pièces justificatives et 1
             lettrage des opérations ; état         
             récapitulatif des opérations lettrées. 

  CP-03      Génération des écritures SYSCOHADA     1
             (achats, règlements, frais de          
             manutention, de gardiennage, de        
             transport, frais des opérateurs) et    
             export vers le logiciel comptable.     

  CP-04      Suivi des avances de caisse aux agents 1
             payeurs et de leur justification.      

  CP-05      Comptabilité matières valorisée par    2
             comptoir et magasin.                   

  CP-06      Rapprochements et clôture mensuelle    2
             avec verrouillage.                     

  CP-07      Suivi budgétaire : budget approuvé,    2
             engagements, paiements.                
  ----------------------------------------------------------------

### 

### Module 4 -- Transfert des stocks

  ----------------------------------------------------------------
  **Réf.**   **Exigence**                           **Priorité**
  ---------- -------------------------------------- --------------
  TR-01      Transmission électronique par la DCT   1
             de la situation des stocks collectés   
             par comptoir à la DGSA et à la DPL,    
             avec accusé de réception.              

  TR-02      Imputation par le DGSA au SGS ; choix  1
             et préparation des magasins de         
             réception ; demande de transfert à la  
             DPL signée électroniquement par le     
             DGSA.                                  

  TR-03      Programmation par la DPL des camions   1
             SONAGESS ; transmission du reste à la  
             DMP pour le recrutement de             
             transporteurs privés ; états de        
             transport transmis à la DCT, à la DGSA 
             et aux tablettes.                      

  TR-04      Enlèvement avec bordereau              1
             d\'enlèvement co-signé par le          
             transporteur et l'agent de collecte.   

  TR-05      Réception en magasin : bordereau de    1
             réception co-signé par le transporteur 
             et le magasinier, fiches de stock,     
             transmission électronique au SGS ;     
             calcul et signalement des écarts       
             enlevé / reçu.                         

  TR-06      Vérification par le SGS des quantités  1
             reçues par rapport aux quantités       
             annoncées par la DCT ; situation des   
             stocks reçus de la collecte.           

  TR-07      Disponibilité des magasins et de la    2
             flotte ; alertes sur magasins          
             indisponibles et camions en retard.    

  TR-08      Vannage et décorticage : entrées,      2
             sorties, produits finis, rendements,   
             maintenance.                           
  ----------------------------------------------------------------

### 

### Module 5 -- Statistique

  ----------------------------------------------------
  **Indicateur**   **Calcul**        **Ventilation**
  ---------------- ----------------- -----------------
  Quantité         Somme des         Jour, cumul,
  collectée (t)    quantités         région, comptoir,
                   validées          spéculation

  Taux de          Collecté /        Région, province,
  réalisation (%)  objectif          comptoir,
                                     spéculation

  Montant dû et    BRBC validés,     Opérateur, mode
  payé (FCFA)      payés, en         de paiement
                   attente, échoués  

  Délai total de   Date de paiement  Comptoir, région
  paiement (jours) − date de pesée   

  Taux de rejet    Lots refusés /    Motif,
  qualité (%)      lots contrôlés    spéculation,
                                     comptoir

  Stock non enlevé Collecté − enlevé Comptoir
  (t) et           ; stock /         
  remplissage (%)  capacité          

  Écart enlèvement Enlevé − reçu     Magasin,
  / réception (t)                    transporteur

  Dossiers de      Nombre, montant   Étape, service
  paiement en      et ancienneté par 
  attente          étape du WF3      

  Délai de         Durée réelle par  Procédure, étape,
  paiement et de   étape comparée à  service
  transfert        la cible de 1     
  (jours)          jour              

  Coût complet de  (Achats +         Région, comptoir
  collecte         manutention +     
  (FCFA/t)         frais) / tonnage  

  Rendement de     Produits finis /  Unité, produit
  transformation   paddy             
  (%)                                
  ----------------------------------------------------

  ----------------------------------------------------------------
  **Réf.**   **État ou export**                     **Priorité**
  ---------- -------------------------------------- --------------
  ST-01      BRBC, fiche de pesée, fiche de qualité 1
             (annexes 1 à 3 du manuel)              

  ST-02      Situation journalière au DG et au      1
             comité de suivi                        

  ST-03      Situation des comptoirs et quantités à 1
             enlever                                

  ST-04      États de paiement ; liasse pour la DFC 1

  ST-05      Bordereaux d\'enlèvement et de         1
             réception, fiches de stock (annexes 4  
             à 7)                                   

  ST-06      Fiches usine (annexes 8 à 10) ;        2
             rapport de campagne                    

  ST-07      Export des données brutes (CSV, Stata, 2
             SPSS)                                  
  ----------------------------------------------------------------

# 

# IV - Transfert de compétences

- Remise du code source, des scripts de déploiement et des accès au
  dépôt Git à la SONAGESS, à chaque livraison.

- Formation des administrateurs système et base de données à
  l\'exploitation (sauvegardes, restauration, supervision, mise à jour).

- Rapport de transfert de compétences, avec évaluation des acquis, à la
  fin de chaque lot.

# V - Conduite du changement

Le passage du papier au numérique concerne plusieurs centaines d\'agents
de terrain, souvent peu familiers des outils numériques, et des
producteurs habitués au BRBC papier. Le plan suivant accompagne ce
changement.

  -------------------------------------------------------------------------------------
  **Phase**         **Actions**        **Cibles**     **Responsable**   **Période**
  ----------------- ------------------ -------------- ----------------- ---------------
  Sensibilisation   Présentation au    Direction, DR, DG, SCRP, DCT     Cadrage
                    CODIR, au comité   producteurs                      
                    de suivi et aux                                     
                    directions                                          
                    régionales ;                                        
                    messages radio et                                   
                    réunions                                            
                    villageoises via                                    
                    le DAC                                              

  Implication       Ateliers de        Utilisateurs   Prestataire, DCT  Développement
                    conception avec    clés                             
                    agents, chefs de                                    
                    comptoir et DFC ;                                   
                    démonstrations de                                   
                    fin de sprint                                       

  Formation         Formation de       Tous les       Prestataire, DCT  Avant ouverture
                    formateurs relais  profils                          des comptoirs
                    régionaux, puis                                     
                    formation                                           
                    obligatoire des                                     
                    agents (outils,                                     
                    qualité,                                            
                    entreposage) ;                                      
                    guides d\'une page                                  
                    par profil                                          

  Pilote            Un hub principal   Agents pilotes Équipe projet     Pilote
                    et une micro-zone                                   
                    ; collecte de                                       
                    retours et                                          
                    corrections                                         

  Double circuit    Maintien du carnet Comptoirs      DCT, DFC          Première
                    BRBC papier en                                      campagne
                    secours pendant la                                  
                    première campagne                                   
                    ; rapprochement                                     
                    numérique / papier                                  

  Accompagnement    Assistance         Terrain        SID, formateurs   Campagne
                    téléphonique,                                       
                    visites des                                         
                    formateurs relais,                                  
                    foire aux                                           
                    questions                                           

  Mesure de         Suivi du taux de   Direction      DCT               Mensuel
  l\'adoption       collectes saisies                                   
                    sur tablette, des                                   
                    délais de                                           
                    paiement, des                                       
                    rejets                                              
  -------------------------------------------------------------------------------------

# VI - Gestion des risques

  --------------------------------------------------------------------------
  **Risque**         **Probabilité**   **Impact**   **Mitigation**
  ------------------ ----------------- ------------ ------------------------
  Instabilité        Moyenne           Élevé        Portage par le DG et le
  institutionnelle                                  comité de suivi ;
  et politique                                      documentation complète ;
                                                    décisions consignées

  Non-prise en       Faible            Moyen        Technologies libres et
  compte de                                         largement utilisées ;
  l\'évolution des                                  versions LTS ;
  technologies                                      architecture modulaire

  Demandes de        Élevée            Moyen        Backlog priorisé ;
  changement en                                     comité technique pour
  cours de projet                                   arbitrer ; changements
                                                    majeurs reportés au lot
                                                    2

  Manque             Moyenne           Élevé        Critères d\'expertise
  d\'expertise de la                                exigés du prestataire ;
  maîtrise d\'œuvre                                 références similaires
                                                    (applications mobiles
                                                    hors ligne, intégration
                                                    mobile money)

  Spécifications     Moyenne           Élevé        Description détaillée
  techniques                                        des workflows et des cas
  ambiguës                                          d\'utilisation ;
                                                    validation des maquettes
                                                    ; points à valider
                                                    listés (XI.1)

  Faible             Moyenne           Élevé        Propriétaire du produit
  collaboration                                     désigné à la DCT ;
  développeurs /                                    démonstrations toutes
  métiers                                           les deux semaines

  Dépassement des    Élevée            Élevé        Découpage en deux lots ;
  délais                                            lot 1 limité aux
                                                    priorités 1 ; suivi
                                                    hebdomadaire

  Manque ou          Moyenne           Élevé        Budget validé avant
  insuffisance de                                   lancement ; acquisitions
  ressources                                        de tablettes séquencées
  financières                                       

  Faible adhésion    Moyenne           Élevé        Plan de conduite du
  des utilisateurs,                                 changement (VII) ;
  résistance au                                     interface simple ;
  changement                                        double circuit papier la
                                                    première campagne

  Indisponibilité ou Moyenne           Élevé        Contrats signés au
  retard des API des                                cadrage ; agrégateur en
  opérateurs                                        secours ; paiement en
                                                    espèces en repli

  Couverture réseau  Élevée            Moyen        Collecte hors ligne ;
  insuffisante aux                                  synchronisation différée
  comptoirs                                         

  Fraude             Moyenne           Élevé        Contrôles automatiques,
  (producteurs                                      GPS, photos, séparation
  fictifs, doublons,                                des tâches, contrôle a
  collusion)                                        posteriori, audit

  Rupture de         Moyenne           Élevé        Demandes de déblocage
  liquidités                                        anticipées ; suivi des
                                                    montants en attente par
                                                    étape

  Indisponibilité    Moyenne           Élevé        Situation des capacités
  des magasins de                                   en temps réel ; alerte
  réception                                         au SGS dès la
                                                    transmission de la DCT

  Arrivée tardive    Élevée            Élevé        Conventions-cadres
  des camions,                                      conclues avant la
  lenteur du                                        campagne ; suivi du
  recrutement des                                   délai de chaque état de
  transporteurs                                     transport
  privés (DMP)                                      

  Instabilité de la  Moyenne           Élevé        Haute disponibilité ;
  plateforme ou                                     intérim paramétrable par
  indisponibilité                                   étape ; alertes de
  d\'un acteur                                      dépassement de délai
  d\'une étape                                      

  Complexité d\'une  Moyenne           Élevé        Développeurs mobiles
  application mobile                                expérimentés ; tests
  hors ligne                                        terrain dès le sprint 3
  développée de zéro                                ; pilote avant
                                                    généralisation

  Perte ou vol de    Élevée            Moyen        Chiffrement, effacement
  tablettes                                         à distance, données
                                                    synchronisées, stock de
                                                    tablettes de rechange
  --------------------------------------------------------------------------

# VII - Modalités d\'intervention

## VII.1 - Durée de la mission et calendrier de mise en œuvre

Les travaux portent sur **12 semaines**.

  --------------------------------------------------------------
  **Phase**     **Contenu**                       **Semaines**
  ------------- --------------------------------- --------------
  1\. Cadrage   Validation des workflows, des     S1 à S3
  et socle      écrans, des rôles et des états;   
                liste des comptoirs ; maquettes ; 
                plan assurance qualité; analyse;  
                conception; architecture; comptes 
                référentiels; API;                
                synchronisation mobile.           

  2\. Lot 1     Application, collecte et          S3 à S5
                validation (SONAGESS_2026_001),   
                Transfert des stocks              
                (,SONAGESS_2026_003).             

  3\. Pilote    Un hub principal et une           S5 à S6
                micro-zone, circuit complet de la 
                pesée au paiement                 

  4\.           Formation des agents, mise en     S6 à S8
  Déploiement   production, accompagnement        
  lot 1                                           

  5\. Lot 2     comptabilité (prise en charge,    S8 à S12
                lettrage), transformation,        
                statistique avancée, paiement     
                (SONAGESS_2026_002), espèces      

  6\. Recette   Recette du lot 2, rapport de      S12
  et clôture    clôture ; recette définitive      
                après une campagne complète       
  --------------------------------------------------------------

## VII.2 - Gouvernance

Les organes de gestion du projet s\'inscrivent dans ceux prévus par le
guide de dématérialisation des procédures administratives.

  --------------------------------------------------------------------------
  **Organe**            **Composition**    **Rôle**          **Fréquence**
  --------------------- ------------------ ----------------- ---------------
  Comité de pilotage    DG (président),    Orientations,     Mensuelle
                        DCT, DFC, DGSA,    validation des    
                        DPL, DAI, chef du  livrables         
                        Service de         majeurs,          
                        l\'informatique et arbitrages        
                        de la              budgétaires       
                        digitalisation,                      
                        représentant du                      
                        ministère de                         
                        tutelle                              

  Comité technique      Chef du Service de Suivi             Hebdomadaire
                        l\'informatique et opérationnel,     
                        de la              validation des    
                        digitalisation,    spécifications,   
                        DSI du ministère,  arbitrage des     
                        DCT, DFC (SFB,     changements       
                        comptabilité),                       
                        DGSA (SGS),                          
                        developpeurs,                        
                        experts métiers                      

  Équipe projet         Propriétaire du    Réalisation,      Quotidienne
                        produit (DCT),     tests,            
                        ingénieurs         déploiement       
                        SONAGESS, équipe                     
                        de developpement                     

  Comité de suivi       Structure          Information sur   Selon son
  ministère--SONAGESS   existante de       l\'avancement et  calendrier
                        pilotage de la     l\'impact         
                        collecte                             
  --------------------------------------------------------------------------

## 

# VIII - Les livrables

  -----------------------------------------------------------
  **Livrable**                 **Phase**     **Validation**
  ---------------------------- ------------- ----------------
  Plan assurance qualité       Cadrage       Comité technique

  Rapport de cadrage et        Cadrage       Comité de
  spécifications                             pilotage
  fonctionnelles détaillées                  
  validées (dont maquettes)                  

  Application mobile et        Lot 1         DCT
  procédure de déploiement sur               
  les tablettes                              

  Dossier de conception        Lot 1         Comité technique
  technique (architecture,                   
  modèle de données, API)                    

  Application lot 1 en test et Lot 1         PV de recette
  en production ; code source                provisoire

  Rapport du pilote            Pilote        Comité de
                                             pilotage

  Guides utilisateur par       Déploiement   DCT, SID
  profil ; guides de l\'agent                
  collecteur et de l\'agent                  
  payeur ; manuel                            
  d\'exploitation                            

  Application lot 1 ; code     Lot 1         PV de recette
  source                                     

  Rapport de test d\'intrusion Avant         SID
  et plan de correction        production    

  Rapports d\'avancement       Toute la      Comité de
  mensuels                     mission       pilotage

  Rapports de transfert de     Fin de chaque SID
  compétences                  lot           

  Rapport de clôture           Fin de        Comité de
                               mission       pilotage
  -----------------------------------------------------------

# 

# IX - Autres

## IX.1 - Points à valider avant le lancement

  ----------------------------------------------------------
  **N°**   **Point**                          **Décideur**
  -------- ---------------------------------- --------------
  1        Sens du sigle « NBC » dans la      DCT
           liste des spéculations             

  2        Seuils de qualité du sésame et des SL/QS
           produits non couverts par          
           l\'annexe 1                        

  3        Opérateur de paiement exclusif de  DFC
           chaque zone                        

  4        Plafonds : paiement en espèces,    DFC, DCT
           quantité par producteur ; barème   
           de gardiennage                     

  5        Rôle du chef de comptoir dans le   DCT
           circuit de validation (absent de   
           la fiche SONAGESS_2026_001)        

  6        Objectif de la campagne 2026 à     DG
           paramétrer (300 000 t ou 530 000   
           t)                                 

  7        Liste définitive des 275 comptoirs DCT
           (fichier Excel annoncé)            

  8        Paiement autorisé sur le compte    DFC
           d\'un tiers (conjoint, trésorier)  

  9        Format d\'import du logiciel       DFC
           comptable de la DFC                

  10       Hébergement et nom de domaine      SID
  ----------------------------------------------------------

## 

## IX.2 - Documents de référence

- Manuel de procédures spécifique de la collecte bord champ et de la
  transformation des produits ASP (octobre 2025).

- Présentation « Initiative collecte bord champ : défis et perspectives
  » (Direction générale).

- Présentation « Enjeux de la collecte bord champ ».

- Liste des modules à développer.

- Fiche de description du workflow SONAGESS_2026_001 : gestion de la
  collecte (DCT).

- Fiche de description du workflow SONAGESS_2026_002 : paiement des
  spéculations, de la manutention et du gardiennage (DFC).

- Fiche de description du workflow SONAGESS_2026_003 : transfert de
  stocks (DGSA), et fiche de la DCT sur le transfert des stocks.

## 

## IX.3 - Sigles

  -------------------------------------------------
  **Sigle**   **Signification**
  ----------- -------------------------------------
  BRBC        Bordereau de réception bord champ

  CNIB        Carte nationale d\'identité burkinabè

  DAC         Dispositif d\'appui conseil

  DCT         Direction de la collecte et de la
              transformation

  DFC         Direction des finances et de la
              comptabilité

  DGSA        Direction de la gestion des stocks
              alimentaires

  DMP         Direction des marchés publics

  DSI         Direction des systèmes d\'information

  DPL         Direction du patrimoine et de la
              logistique

  DR          Direction régionale

  OAPH        Offensive agropastorale et
              halieutique

  SCP         Service collecte et production

  SFB         Service des finances et du budget

  SGS         Service de la gestion des stocks

  SID         Service de l\'informatique et de la
              digitalisation

  SIM         Système d\'information sur les
              marchés

  SL/QS       Service du laboratoire et de la
              qualité des stocks

  ZATA / UATA Zone / unité d\'appui technique en
              agriculture
  -------------------------------------------------

# Annexe : matrice des droits

O = oui ; P = dans son périmètre ; --- = non. Les points focaux et
directeurs régionaux consultent les données de leur zone. Les
transporteurs et les gardiens n\'ont pas d\'accès à la plateforme.

  ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **Action**         **Chef       **Agent    **Chef   **DCT**    **Dir.     **Agent   **Chef   **Compta-bilité**   **Magasi-nier**   **DGSA,   **DPL,   **Direction**   **Audit**   **Admin**
                     comptoir**   payeur**   SCP**               DFC**      init.     SFB**                                          SGS**     DMP**                                
                                                                            SFB**                                                                                                   
  ------------------ ------------ ---------- -------- ---------- ---------- --------- -------- ------------------- ----------------- --------- -------- --------------- ----------- -----------
  Consulter les      P            P          O        O          O          O         O        O                   ---               O         O        O               O           O
  collectes                                                                                                                                                                         

  Examiner les       ---          ---        O        ---        ---        ---       ---      ---                 ---               ---       ---      ---             ---         ---
  documents de                                                                                                                                                                      
  collecte                                                                                                                                                                          
  (transmettre ou                                                                                                                                                                   
  rejeter)                                                                                                                                                                          

  Valider ; état de  ---          ---        ---      O          ---        ---       ---      ---                 ---               ---       ---      ---             ---         ---
  paiement et                                                                                                                                                                       
  demande de                                                                                                                                                                        
  déblocage                                                                                                                                                                         

  Trier les pièces   ---          ---        ---      ---        O          O         O        O                   ---               ---       ---      ---             ---         ---
  (bordereaux de                                                                                                                                                                    
  transmission et de                                                                                                                                                                
  rejet)                                                                                                                                                                            

  Initier un         ---          ---        ---      ---        ---        O         ---      ---                 ---               ---       ---      ---             ---         ---
  paiement                                                                                                                                                                          

  Valider un         ---          ---        ---      ---        ---        ---       O        ---                 ---               ---       ---      ---             ---         ---
  paiement                                                                                                                                                                          

  Payer en espèces   ---          P          ---      ---        ---        ---       ---      ---                 ---               ---       ---      ---             ---         ---

  Prise en charge,   ---          ---        ---      ---        ---        ---       ---      O                   ---               ---       ---      ---             ---         ---
  lettrage,                                                                                                                                                                         
  comptabilisation                                                                                                                                                                  

  Situation des      ---          ---        ---      O          ---        ---       ---      ---                 ---               O         O        O               O           ---
  stocks collectés                                                                                                                                                                  
  (transmission)                                                                                                                                                                    

  Demande de         ---          ---        ---      ---        ---        ---       ---      ---                 ---               O         ---      ---             ---         ---
  transfert,                                                                                                                                                                        
  magasins                                                                                                                                                                          

  Programme et états ---          ---        ---      ---        ---        ---       ---      ---                 ---               ---       O        ---             ---         ---
  de transport                                                                                                                                                                      

  Réception en       ---          ---        ---      ---        ---        ---       ---      ---                 P                 O         ---      ---             ---         ---
  magasin, fiches de                                                                                                                                                                
  stock                                                                                                                                                                             

  Gérer son comptoir P            ---        ---      O          ---        ---       ---      ---                 ---               ---       O        ---             ---         ---
  (stock, kit,                                                                                                                                                                      
  emballages)                                                                                                                                                                       

  Exporter les états P            P          O        O          O          O         O        O                   P                 O         O        O               O           ---

  Voir CNIB et       P            P          O        O          O          O         O        ---                 ---               ---       ---      ---             O           ---
  numéros en clair                                                                                                                                                                  

  Modifier prix,     ---          ---        ---      Proposer   Proposer   ---       ---      ---                 ---               ---       ---      Approuver       ---         ---
  objectifs, seuils,                                                                                                                                                                
  barèmes                                                                                                                                                                           

  Gérer les comptes  ---          ---        ---      ---        ---        ---       ---      ---                 ---               ---       ---      ---             ---         O

  Consulter le       ---          ---        ---      ---        ---        ---       ---      ---                 ---               ---       ---      O               O           O
  journal d\'audit                                                                                                                                                                  
  ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
