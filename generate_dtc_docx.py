import os
import docx
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import nsdecls, qn

def set_cell_background(cell, fill_hex):
    shading_elm = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{fill_hex}"/>')
    cell._tc.get_or_add_tcPr().append(shading_elm)

def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = parse_xml(f'<w:tcMar {nsdecls("w")}><w:top w:w="{top}" w:type="dxa"/><w:bottom w:w="{bottom}" w:type="dxa"/><w:left w:w="{left}" w:type="dxa"/><w:right w:w="{right}" w:type="dxa"/></w:tcMar>')
    tcPr.append(tcMar)

def create_dtc_document(output_path):
    doc = Document()
    
    # Page setup (A4 margins 2cm)
    sections = doc.sections
    for s in sections:
        s.top_margin = Inches(0.8)
        s.bottom_margin = Inches(0.8)
        s.left_margin = Inches(0.8)
        s.right_margin = Inches(0.8)
        
    primary_color = RGBColor(2, 132, 199)    # Bleu ANPTIC/DGTD #0284c7
    dark_color = RGBColor(15, 23, 42)        # Gris foncé #0f172a
    gray_color = RGBColor(100, 116, 139)     # Gris moyen #64748b
    header_bg = "0284C7"
    alt_row_bg = "F1F5F9"
    border_color = "CBD5E1"

    # Style definitions
    normal_style = doc.styles['Normal']
    normal_style.font.name = 'Calibri'
    normal_style.font.size = Pt(11)
    normal_style.font.color.rgb = dark_color

    # --- 1. PAGE DE GARDE INSTITUTIONNELLE ---
    p_header = doc.add_paragraph()
    p_header.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p_header.add_run("MINISTÈRE DE L'AGRICULTURE, DES RESSOURCES ANIMALES ET HALIEUTIQUES (MARAH)\n")
    run.bold = True
    run.font.size = Pt(10)
    run.font.color.rgb = gray_color
    run_sg = p_header.add_run("SECRÉTARIAT GÉNÉRAL\n-------------------------\n")
    run_sg.font.size = Pt(9)
    run_sg.font.color.rgb = gray_color
    run_sonagess = p_header.add_run("SOCIÉTÉ NATIONALE DE GESTION DU STOCK DE SÉCURITÉ ALIMENTAIRE (SONAGESS)\n")
    run_sonagess.bold = True
    run_sonagess.font.size = Pt(11)
    run_sonagess.font.color.rgb = primary_color

    p_devise = doc.add_paragraph()
    p_devise.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    run_bf = p_devise.add_run("BURKINA FASO\nLa Patrie ou la Mort, nous Vaincrons\n")
    run_bf.bold = True
    run_bf.font.size = Pt(10)

    doc.add_paragraph("\n")

    p_title = doc.add_paragraph()
    p_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run_title = p_title.add_run("DOCUMENT DE CONCEPTION TECHNIQUE (DTC)\nET SPÉCIFICATIONS DÉTAILLÉES\n")
    run_title.bold = True
    run_title.font.size = Pt(22)
    run_title.font.color.rgb = primary_color

    p_sub = doc.add_paragraph()
    p_sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run_sub = p_sub.add_run("DÉMATÉRIALISATION DE LA COLLECTE BORD CHAMP (CBC)\nOffensive Agropastorale et Halieutique (OAPH)\n")
    run_sub.bold = True
    run_sub.font.size = Pt(14)
    run_sub.font.color.rgb = dark_color

    doc.add_paragraph("\n")

    # Table Métadonnées document
    table_meta = doc.add_table(rows=6, cols=2)
    table_meta.alignment = WD_TABLE_ALIGNMENT.CENTER
    meta_data = [
        ("Référence du document", "DTC_Collecte_Bord_Champ_SONAGESS_V1.0.docx"),
        ("Date d'élaboration", "Octobre 2026"),
        ("Indice de révision", "1.0 - Version de Référence"),
        ("État", "Document Validé"),
        ("Document source", "Cahier des charges (Analyse) CBC - SONAGESS"),
        ("Technologies cibles", "Spring Boot 3 (Java 21) | Angular 18+ | PostgreSQL 16 | Android Offline")
    ]
    for i, (k, v) in enumerate(meta_data):
        cell_k, cell_v = table_meta.rows[i].cells
        cell_k.text = k
        cell_k.paragraphs[0].runs[0].bold = True
        cell_k.paragraphs[0].runs[0].font.size = Pt(9.5)
        set_cell_background(cell_k, "E2E8F0")
        cell_v.text = v
        cell_v.paragraphs[0].runs[0].font.size = Pt(9.5)
        set_cell_background(cell_v, "F8FAFC")
        set_cell_margins(cell_k, 60, 60, 100, 100)
        set_cell_margins(cell_v, 60, 60, 100, 100)

    doc.add_page_break()

    # --- TABLE DES MISES À JOUR & DIFFUSION ---
    h1 = doc.add_heading("Table des mises à jour du document", level=2)
    h1.style.font.color.rgb = primary_color
    t_rev = doc.add_table(rows=3, cols=4)
    t_rev.alignment = WD_TABLE_ALIGNMENT.CENTER
    headers = ["Indice", "Date", "Objet de la mise à jour", "Auteur / Entité"]
    for j, h in enumerate(headers):
        c = t_rev.rows[0].cells[j]
        c.text = h
        c.paragraphs[0].runs[0].bold = True
        set_cell_background(c, header_bg)
        c.paragraphs[0].runs[0].font.color.rgb = RGBColor(255, 255, 255)
    
    rev_data = [
        ("0.1", "10/10/2026", "Création initiale à partir du cahier des charges", "Équipe Projet DGTD / SONAGESS"),
        ("1.0", "25/10/2026", "Consolidation UML, modèles de données, sécurité 2FA & architecture technique", "Direction des Systèmes d'Information")
    ]
    for i, row in enumerate(rev_data):
        for j, val in enumerate(row):
            c = t_rev.rows[i+1].cells[j]
            c.text = val
            c.paragraphs[0].runs[0].font.size = Pt(9)
            if i % 2 == 1:
                set_cell_background(c, alt_row_bg)

    doc.add_paragraph("\n")
    h2 = doc.add_heading("Diffusion", level=2)
    h2.style.font.color.rgb = primary_color
    t_diff = doc.add_table(rows=4, cols=3)
    t_diff.alignment = WD_TABLE_ALIGNMENT.CENTER
    headers_diff = ["Destinataires", "Rôle / Responsabilité", "Version"]
    for j, h in enumerate(headers_diff):
        c = t_diff.rows[0].cells[j]
        c.text = h
        c.paragraphs[0].runs[0].bold = True
        set_cell_background(c, header_bg)
        c.paragraphs[0].runs[0].font.color.rgb = RGBColor(255, 255, 255)
    
    diff_data = [
        ("Comité de Pilotage & Direction Générale SONAGESS", "Arbitrage stratégique et validation", "1.0"),
        ("Directions Métiers (DCT, DFC, DGSA, DPL, Comptabilité)", "Examen des fonctionnalités et processus métiers", "1.0"),
        ("Équipe d'ingénierie logicielle & Experts Sécurité (ANSSI)", "Implémentation technique et audits de conformité", "1.0")
    ]
    for i, row in enumerate(diff_data):
        for j, val in enumerate(row):
            c = t_diff.rows[i+1].cells[j]
            c.text = val
            c.paragraphs[0].runs[0].font.size = Pt(9)
            if i % 2 == 1:
                set_cell_background(c, alt_row_bg)

    doc.add_page_break()

    # --- SIGLES ET ABRÉVIATIONS ---
    h_sigles = doc.add_heading("Sigles et abréviations", level=1)
    h_sigles.style.font.color.rgb = primary_color
    sigles = [
        ("ANO", "Avis de Non Objection"),
        ("ANSSI", "Agence Nationale de Sécurité des Systèmes d'Information"),
        ("API", "Application Programming Interface (Interface de programmation d'application)"),
        ("BPMN", "Business Process Model and Notation"),
        ("BRBC", "Bordereau de Réception Bord Champ (document pivot à 5 volets dématérialisé)"),
        ("CBC", "Collecte Bord Champ"),
        ("CNIB", "Carte Nationale d'Identité Burkinabè"),
        ("DCT", "Direction Commerciale et Technique (SONAGESS)"),
        ("DFC", "Direction des Finances et de la Comptabilité (SONAGESS)"),
        ("DGSA", "Direction Générale de la Sécurité Alimentaire"),
        ("DPL", "Direction du Patrimoine et de la Logistique"),
        ("DMP", "Direction des Marchés Publics"),
        ("DTC", "Dossier de Conception Technique"),
        ("JWT", "JSON Web Token"),
        ("MCD / MLD", "Modèle Conceptuel de Données / Modèle Logique de Données"),
        ("OAPH", "Offensive Agropastorale et Halieutique"),
        ("OTP", "One-Time Password (Mot de passe à usage unique)"),
        ("RBAC", "Role-Based Access Control (Contrôle d'accès basé sur les rôles)"),
        ("SCP", "Service Collecte et Production"),
        ("SFB", "Service des Finances et du Budget"),
        ("SGS", "Service de Gestion des Stocks"),
        ("SoD", "Segregation of Duties (Séparation des tâches et des pouvoirs)"),
        ("SONAGESS", "Société Nationale de Gestion du Stock de Sécurité Alimentaire"),
        ("SYSCOHADA", "Système Comptable de l'Organisation pour l'Harmonisation en Afrique du Droit des Affaires")
    ]
    t_sig = doc.add_table(rows=len(sigles)+1, cols=2)
    t_sig.alignment = WD_TABLE_ALIGNMENT.CENTER
    t_sig.rows[0].cells[0].text = "Sigle"
    t_sig.rows[0].cells[1].text = "Signification complète"
    set_cell_background(t_sig.rows[0].cells[0], header_bg)
    set_cell_background(t_sig.rows[0].cells[1], header_bg)
    t_sig.rows[0].cells[0].paragraphs[0].runs[0].font.color.rgb = RGBColor(255, 255, 255)
    t_sig.rows[0].cells[1].paragraphs[0].runs[0].font.color.rgb = RGBColor(255, 255, 255)
    for i, (k, v) in enumerate(sigles):
        c0, c1 = t_sig.rows[i+1].cells
        c0.text = k
        c0.paragraphs[0].runs[0].bold = True
        c0.paragraphs[0].runs[0].font.size = Pt(9)
        c1.text = v
        c1.paragraphs[0].runs[0].font.size = Pt(9)
        if i % 2 == 1:
            set_cell_background(c0, alt_row_bg)
            set_cell_background(c1, alt_row_bg)

    doc.add_page_break()

    # --- SECTION I : CONTEXTE ET JUSTIFICATION ---
    h_i = doc.add_heading("I. Contexte et justification", level=1)
    h_i.style.font.color.rgb = primary_color
    doc.add_paragraph(
        "La Société Nationale de Gestion du Stock de Sécurité Alimentaire (SONAGESS), érigée en centrale d'achats dans le cadre de "
        "l'Offensive Agropastorale et Halieutique (OAPH), assure l'achat direct auprès des producteurs des céréales et légumineuses "
        "(riz paddy, maïs, niébé, sorgho, soja, mil, etc.) constituant les stocks de souveraineté nationale et de régulation des marchés.\n"
        "Lors de la campagne 2025-2026, 315 264 tonnes ont été collectées au 31 juillet 2026, mobilisant plus de 265 agents répartis dans 12 régions, "
        "avec des paiements effectués via les canaux de Mobile Money (Sank Money, Coris Money, Moov Money, LigdiCash)."
    )
    doc.add_paragraph(
        "Cependant, le bilan opérationnel a mis en évidence quatre ruptures majeures :\n"
        "• Une collecte dispersée en plein air générant d'importants surcoûts et risques de perte/vol ;\n"
        "• Des risques accrus de dégradation de la qualité des grains ;\n"
        "• Une saturation logistique et des retards dans l'enlèvement vers les magasins centraux (délai moyen de 15 jours) ;\n"
        "• Des ruptures de trésorerie et un délai de paiement manuel sur papier atteignant 21 jours ouvrables.\n"
        "Pour la campagne 2026, la SONAGESS restructure la collecte autour de comptoirs fixes (19 hubs principaux de plus de 5 000 t, "
        "78 comptoirs secondaires de 1 000 à 4 999 t et 178 comptoirs intermédiaires). La digitalisation complète de la chaîne par une plateforme "
        "sécurisée et interopérable constitue le prérequis absolu pour garantir la transparence, réduire le délai de paiement à 7 jours ouvrables "
        "et le délai de transfert physique des stocks à 9 jours."
    )

    # --- SECTION II : PÉRIMÈTRE DU PROJET ---
    h_ii = doc.add_heading("II. Périmètre du projet", level=1)
    h_ii.style.font.color.rgb = primary_color
    doc.add_heading("1. Portée fonctionnelle et géographique", level=2)
    doc.add_paragraph(
        "Le projet couvre l'ensemble des 13 régions du Burkina Faso, 275 comptoirs fixes et itinérants, ainsi que l'ensemble des magasins de stockage "
        "centraux et régionaux de la SONAGESS. Il englobe :\n"
        "• La gestion mobile sur le terrain en mode déconnecté (Offline-first) : identification du producteur, contrôle qualité automatisé, pesée par bascule, émission du BRBC dématérialisé et signature électronique tactile ;\n"
        "• La validation administrative et hiérarchique : examen par le Service Collecte et Production (SCP) et validation par la Direction Commerciale et Technique (DCT) ;\n"
        "• L'exécution financière sécurisée : contrôle par la Direction des Finances et de la Comptabilité (DFC), initiation par l'agent SFB, validation financière à double facteur (2FA/OTP) par le Chef SFB et décaissement automatique Mobile Money ;\n"
        "• La logistique et le suivi des stocks : programmation des enlèvements (DPL/DMP), suivi des camions, réception contradictoire en magasin central (SGS/DGSA) et détection des écarts de pesée ;\n"
        "• La comptabilité et le reporting : lettrage, réconciliation bancaire, génération des écritures SYSCOHADA et tableaux de bord temps réel."
    )
    doc.add_heading("2. Exclusions", level=2)
    doc.add_paragraph(
        "Sont expressément exclus du périmètre applicatif :\n"
        "• La gestion bancaire interne des opérateurs télécoms (les interfaces se limitent aux API de déboursement sécurisées) ;\n"
        "• La maintenance physique du matériel roulant (camions) et des bascules de pesée (gérée par des prestataires tiers agréés) ;\n"
        "• La commercialisation et la redistribution des stocks aux populations (objet d'une plateforme aval distincte)."
    )

    # --- SECTION III : OBJECTIFS ET LIVRABLES ---
    h_iii = doc.add_heading("III. Objectifs du projet et livrables", level=1)
    h_iii.style.font.color.rgb = primary_color
    doc.add_paragraph(
        "Objectif général : Concevoir, développer et déployer une plateforme logicielle intégrée, sécurisée et souveraine permettant de dématérialiser "
        "à 100 % la collecte bord champ, les flux financiers et la traçabilité des stocks de sécurité alimentaire.\n\n"
        "Objectifs spécifiques & KPIs cibles :\n"
        "• Réduction du délai moyen de paiement des producteurs de 21 jours à 7 jours ouvrables maximum ;\n"
        "• Réduction du délai d'acheminement des grains en magasins centraux de 15 jours à 9 jours maximum ;\n"
        "• Taux de rapprochement automatique des paiements mobiles supérieur à 98 % ;\n"
        "• Disponibilité journalière à 18h00 de la situation consolidée nationale des stocks et de la trésorerie ;\n"
        "• Zéro perte de données de collecte grâce au stockage local chiffré et à la synchronisation résiliente.\n\n"
        "Livrables officiels attendus :\n"
        "1. Cahier des charges et note de cadrage validés ;\n"
        "2. Dossier de Conception Technique (DTC) et spécifications détaillées (le présent document) ;\n"
        "3. Fiches de description formelle des workflows SONAGESS_2026_001, 002 et 003 ;\n"
        "4. Codes sources complets documentés (Backend Spring Boot, Frontend Angular, Application Android) ;\n"
        "5. Manuels d'utilisation (profils terrain, valideurs siège, comptables, administrateurs) ;\n"
        "6. Guides d'exploitation, de déploiement et d'administration système ;\n"
        "7. Rapports de tests fonctionnels, de tests de charge et rapport d'audit de sécurité ANSSI (Avis de Non Objection)."
    )

    # --- SECTION IV : EXPRESSION ET ANALYSE DES BESOINS ---
    h_iv = doc.add_heading("IV. Expression et analyse des besoins", level=1)
    h_iv.style.font.color.rgb = primary_color
    doc.add_paragraph(
        "Le système actuel repose sur des bordereaux papier physiques en 5 volets (BRBC), fiches de pesée manuelles et états de transmission physiques. "
        "Ce fonctionnement entraîne des risques majeurs d'erreurs de calcul, des falsifications de pesée, des pertes de pièces justificatives dans le transport "
        "et l'impossibilité pour le Directeur Général de disposer d'une vision consolidée en temps opportun.\n\n"
        "Besoins clés adressés par la solution :\n"
        "• Rigueur du contrôle qualité : saisie obligatoire des métriques (humidité <= 14%, impuretés <= 2%, avaries) avec validation bloquante immédiate sur la tablette ;\n"
        "• Fiabilité de la pesée : calcul automatique du poids net (poids brut - tare emballages) et du montant brut sans intervention manuelle de l'agent ;\n"
        "• Sécurité financière et séparation des tâches (SoD) : aucune transaction financière ne peut être validée par celui qui l'a initiée. Authentification 2FA requise ;\n"
        "• Résilience hors-réseau : capacité de collecter en brousse sans connectivité GSM/Internet pendant plusieurs jours avec chiffrement AES-256 local."
    )

    # --- SECTION V : SPÉCIFICATIONS FONCTIONNELLES ET ACTEURS ---
    h_v = doc.add_heading("V. Spécifications fonctionnelles et acteurs", level=1)
    h_v.style.font.color.rgb = primary_color
    doc.add_heading("1. Matrice des Rôles et Droits d'Accès (Conforme Annexe CDC)", level=2)
    doc.add_paragraph(
        "Le tableau ci-dessous formalise la matrice des droits d'accès basée sur les rôles (RBAC) et les périmètres d'intervention géographiques (O = Oui, P = Dans son périmètre, --- = Non) :"
    )
    
    t_roles = doc.add_table(rows=11, cols=6)
    t_roles.alignment = WD_TABLE_ALIGNMENT.CENTER
    h_r = ["Fonctionnalité / Action", "Agent Collecteur", "Chef Comptoir", "Chef SCP", "Directeur DCT", "Chef SFB / DFC"]
    for j, val in enumerate(h_r):
        c = t_roles.rows[0].cells[j]
        c.text = val
        c.paragraphs[0].runs[0].bold = True
        c.paragraphs[0].runs[0].font.size = Pt(8.5)
        set_cell_background(c, header_bg)
        c.paragraphs[0].runs[0].font.color.rgb = RGBColor(255, 255, 255)

    r_data = [
        ("Enregistrer collecte (Qualité, Pesée, BRBC)", "O", "P", "---", "---", "---"),
        ("Examiner les dossiers de collecte (SCP)", "---", "---", "O", "---", "---"),
        ("Valider collecte & générer bordereau transmission", "---", "---", "---", "O", "---"),
        ("Trier les pièces financières (DFC)", "---", "---", "---", "---", "O"),
        ("Initier un paiement mobile (SFB)", "---", "---", "---", "---", "O (Agent SFB)"),
        ("Valider un paiement mobile avec 2FA", "---", "---", "---", "---", "O (Chef SFB)"),
        ("Payer en espèces (résiduel)", "---", "---", "---", "---", "P (Agent payeur)"),
        ("Programme et transport d'enlèvement", "---", "---", "---", "O", "---"),
        ("Réception en magasin et constat écarts", "---", "---", "---", "---", "---"),
        ("Consulter les tableaux de bord temps réel", "---", "P", "O", "O", "O")
    ]
    for i, row in enumerate(r_data):
        for j, val in enumerate(row):
            c = t_roles.rows[i+1].cells[j]
            c.text = val
            c.paragraphs[0].runs[0].font.size = Pt(8)
            if i % 2 == 1:
                set_cell_background(c, alt_row_bg)

    doc.add_heading("2. Concordance avec les outils de gestion historiques de la SONAGESS (outils de gestion.xlsx)", level=2)
    doc.add_paragraph(
        "L'analyse approfondie du classeur officiel « outils de gestion.xlsx » utilisé sur le terrain lors des campagnes précédentes "
        "a permis d'établir une correspondance biunivoque stricte entre les formulaires papier historiques et les entités/modules dématérialisés "
        "de la future plateforme logicielle. Cette concordance garantit que les habitudes réglementaires sont respectées tout en éliminant les lenteurs et fraudes :"
    )

    t_tools = doc.add_table(rows=7, cols=4)
    t_tools.alignment = WD_TABLE_ALIGNMENT.CENTER
    h_tools = ["Outil Papier Historique", "Objet & Caractéristiques physiques", "Entité & Module Dématérialisé", "Gains et Automatismes"]
    for j, val in enumerate(h_tools):
        c = t_tools.rows[0].cells[j]
        c.text = val
        c.paragraphs[0].runs[0].bold = True
        c.paragraphs[0].runs[0].font.size = Pt(8.5)
        set_cell_background(c, header_bg)
        c.paragraphs[0].runs[0].font.color.rgb = RGBColor(255, 255, 255)

    tools_data = [
        (
            "BRBC\n(Bordereau de Réception Bord Champ)",
            "Formulaire pivot à 5 volets papier autocopiants (Blanc, Bleu, Vert, Rouge, Jaune). Saisie manuelle producteur, CNIB, PU, Quantité, Montant en lettres, signatures conjointes.",
            "Module Collecte / Entité BordereauReception_BRBC\n(Génération d'un BRBC dématérialisé unique horodaté avec QR Code de traçabilité).",
            "Calcul automatique du montant net (Kg * PU). Remplacement des 5 volets par des signatures tactiles certifiées et diffusion instantanée multi-services (SCP, DCT, DFC)."
        ),
        (
            "BEBC\n(Bordereau d'Enlèvement de Céréales)",
            "Liasse en 5 volets pour l'enlèvement physique des grains par les camions. Mentionne la localité, le magasin, le transporteur, le N° véhicule, le colisage, le tonnage.",
            "Module Transfert / Entité BordereauEnlevement_BEBC\n(Contrôle bloquant : Tonnage enlevé <= Stock théorique comptoir).",
            "Élimination des sur-enlèvements. Signature conjointe chauffeur / agent. Suivi en temps réel de la flotte de camions en transit vers les magasins centraux."
        ),
        (
            "FP\n(Fiche de Pesées)",
            "Fiche d'enregistrement individuel des pesées par bascule. Détail de chaque sacée, pesée brute, tare emballage et calcul du poids net.",
            "Module Mobile Offline / Entité FichePesee_FP\n(Interface de pesée connectable ou saisie guidée sur tablette avec tare déduite automatiquement).",
            "Zéro erreur de calcul arithmétique. Détection des bascules non étalonnées. Historisation horodatée de chaque pesée avec coordonnées GPS du comptoir."
        ),
        (
            "FSJ\n(Fiche de Stock Journalière)",
            "Cahier journalier tenu au comptoir enregistrant les Entrées, Sorties et Stocks en sacs, poids et valeur financière CFA.",
            "Module Stock Comptoir / Entité FicheStockJournaliere_FSJ\n(Mise à jour en temps réel à chaque émission de BRBC et de BEBC).",
            "Disponibilité à 18h00 de la situation consolidée sans calcul manuel. Alerte immédiate si le stock physique présente un écart > 2 % avec le stock théorique."
        ),
        (
            "FSM\n(Fiche de Stock Mensuelle)",
            "Rapprochement mensuel des magasins récepteurs : stock initial, entrées ventilées par N° BRBC, sorties ventilées par N° BEBC, stock final.",
            "Module Magasin Central / Entité FicheStockMensuelle_FSM\n(Agrégation automatique et lettrage avec les réceptions effectives).",
            "Rapprochement automatisé à 100 % entre les expéditions comptoirs (BEBC) et les réceptions physiques en magasins récepteurs (BRM)."
        ),
        (
            "Feuil2\n(Stock Matériel & Phytosanitaire)",
            "Registre de gestion des emballages (sacs neufs/usagés), des bâches, des kits de pesée et des produits de conservation.",
            "Module Logistique & Kits / Entité FicheStockMateriel_Emballages\n(Suivi des dotations aux agents et alertes de réapprovisionnement).",
            "Gestion rigoureuse des 7 millions d'emballages prévus en 2026. Prévention des ruptures de sacs sur les comptoirs isolés."
        )
    ]
    for i, row in enumerate(tools_data):
        for j, val in enumerate(row):
            c = t_tools.rows[i+1].cells[j]
            c.text = val
            c.paragraphs[0].runs[0].font.size = Pt(8)
            if i % 2 == 1:
                set_cell_background(c, alt_row_bg)

    doc.add_page_break()

    # --- SECTION VI : EXIGENCES DE SÉCURITÉ ANSSI ---
    h_vi = doc.add_heading("VI. Exigences de sécurité et conformité ANSSI", level=1)
    h_vi.style.font.color.rgb = primary_color
    doc.add_paragraph(
        "En application des référentiels nationaux de l'ANSSI et de la loi n°001-2021/AN relative à la protection des données personnelles, "
        "la plateforme intègre les exigences de sécurité suivantes dès sa conception (Security by Design) :\n"
        "• Authentification forte : Mots de passe hashés par algorithme BCrypt (cost factor 12) et obligation de second facteur (2FA TOTP/SMS) pour tout rôle financier ou d'administration ;\n"
        "• Séparation des pouvoirs (SoD) : Impossibilité cryptographique et applicative qu'un même utilisateur initie et valide un ordre de virement Mobile Money ;\n"
        "• Chiffrement de bout en bout : Flux réseaux sécurisés sous TLS 1.3 avec certificat HSTS valide. Base SQLite locale des tablettes Android chiffrée sous SQLCipher (clé AES-256) ;\n"
        "• Journal d'audit immuable : Enregistrement de chaque action (connexion, validation, échec, transfert financier) avec horodatage certifié, IP et identifiant opérateur. Les journaux sont non modifiables ;\n"
        "• Résilience & Continuité : Sauvegardes journalières externalisées et plan de reprise d'activité (RPO < 15 min, RTO < 2h)."
    )

    # --- SECTION VII : ARCHITECTURE LOGICIELLE DU SYSTÈME ---
    h_vii = doc.add_heading("VII. Architecture logicielle du système", level=1)
    h_vii.style.font.color.rgb = primary_color
    doc.add_paragraph(
        "La solution adopte une architecture 3-tiers modulaire articulée autour d'un socle d'API REST hautement disponible :\n\n"
        "1. Couche Présentation (Clients) :\n"
        "   • Portail Web Siège (Angular 18+) : Application monopage (SPA) responsive, modulaire, intégrant Tailwind/CSS moderne, Chart.js pour les dashboards et guards de sécurité RBAC.\n"
        "   • Application Mobile Terrain (Android / Kotlin) : Application native optimisée pour tablettes Android, embarquant une base locale SQLite chiffrée, un moteur de règles de calcul hors-ligne et un module de synchronisation résiliente.\n\n"
        "2. Couche Métier & Services (Spring Boot 3 / Java 21) :\n"
        "   • Modules applicatifs : Module Sécurité/IAM, Module Paramétrage & Campagne, Module Collecte & Qualité, Module Trésorerie & Mobile Money, Module Logistique & Magasins, Module Audit & Reporting.\n"
        "   • Gestion des transactions ACID, validation formelle des DTO (Bean Validation) et exposition d'API REST documentées sous OpenAPI 3.0 (Swagger).\n\n"
        "3. Couche Données & Caching :\n"
        "   • PostgreSQL 16 : Système de gestion de base de données relationnelle garantissant l'intégrité référentielle, le partitionnement par campagne et l'audit trail.\n"
        "   • Redis : Cache distribué en mémoire pour la mise en cache des prix garantis, des barèmes et la gestion des sessions d'authentification."
    )

    doc.add_page_break()

    # --- SECTION VIII : MODÉLISATION UML COMPLÈTE AVEC DIAGRAMMES ---
    h_viii = doc.add_heading("VIII. Modélisation UML complète", level=1)
    h_viii.style.font.color.rgb = primary_color
    
    doc.add_heading("1. Diagramme Global des Cas d'Utilisation (Acteurs UML Bonshommes)", level=2)
    doc.add_paragraph(
        "Ce diagramme illustre les interactions des différents acteurs avec les modules fonctionnels de la plateforme, "
        "selon la notation UML standard représentant chaque acteur par un bonhomme (stick figure) :"
    )
    img_usecase = "docs/conception/diagrams/1_usecase.png"
    if os.path.exists(img_usecase):
        doc.add_picture(img_usecase, width=Inches(6.5))
        p_cap = doc.add_paragraph("Figure 1 : Diagramme Global des Cas d'Utilisation — Plateforme CBC SONAGESS")
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_cap.runs[0].font.size = Pt(9)
        p_cap.runs[0].italic = True

    doc.add_paragraph("\n")
    doc.add_heading("2. Diagramme de Cas d'Utilisation : Authentification & Sécurité (IAM)", level=2)
    doc.add_paragraph(
        "Détail des cas d'utilisation liés à la sécurité des accès, incluant la double authentification (2FA), le verrouillage de compte et la conformité ANSSI :"
    )
    img_auth_uc = "docs/conception/diagrams/usecase_auth.png"
    if os.path.exists(img_auth_uc):
        doc.add_picture(img_auth_uc, width=Inches(6.2))
        p_cap = doc.add_paragraph("Figure 2 : Cas d'Utilisation Authentification, 2FA et Gestion des Sessions")
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_cap.runs[0].font.size = Pt(9)
        p_cap.runs[0].italic = True

    doc.add_page_break()

    doc.add_heading("3. Diagramme de Séquence : Authentification Sécurisée, JWT & 2FA", level=2)
    doc.add_paragraph(
        "Ce diagramme de séquence décrit le flux complet d'authentification avec Spring Security, l'évaluation du risque, l'émission du challenge OTP pour les rôles financiers et l'audit trail :"
    )
    img_seq_auth = "docs/conception/diagrams/seq_auth.png"
    if os.path.exists(img_seq_auth):
        doc.add_picture(img_seq_auth, width=Inches(6.2))
        p_cap = doc.add_paragraph("Figure 3 : Séquence d'Authentification Spring Security, JWT et Challenge 2FA/OTP")
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_cap.runs[0].font.size = Pt(9)
        p_cap.runs[0].italic = True

    doc.add_paragraph("\n")
    doc.add_heading("4. Diagramme de Séquence : Collecte Bord Champ & Contrôle Qualité", level=2)
    doc.add_paragraph(
        "Flux d'exécution sur le terrain en mode déconnecté (tablette Android), vérification immédiate des barèmes qualité, pesée, signature électronique et synchronisation Spring Boot :"
    )
    img_seq_col = "docs/conception/diagrams/3_seq_collecte.png"
    if os.path.exists(img_seq_col):
        doc.add_picture(img_seq_col, width=Inches(6.2))
        p_cap = doc.add_paragraph("Figure 4 : Séquence de Collecte Bord Champ en mode déconnecté et synchronisation")
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_cap.runs[0].font.size = Pt(9)
        p_cap.runs[0].italic = True

    doc.add_page_break()

    doc.add_heading("5. Diagramme de Séquence : Examen SCP & Validation DCT", level=2)
    doc.add_paragraph(
        "Parcours d'approbation administrative des collectes synchronisées, avec gestion des motifs de rejet et constitution de l'état officiel de transmission vers la DFC :"
    )
    img_seq_val = "docs/conception/diagrams/4_seq_validation.png"
    if os.path.exists(img_seq_val):
        doc.add_picture(img_seq_val, width=Inches(6.2))
        p_cap = doc.add_paragraph("Figure 5 : Séquence d'Examen technique (SCP) et Validation hiérarchique (DCT)")
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_cap.runs[0].font.size = Pt(9)
        p_cap.runs[0].italic = True

    doc.add_paragraph("\n")
    doc.add_heading("6. Diagramme de Séquence : Initiation & Validation 2FA du Paiement Mobile", level=2)
    doc.add_paragraph(
        "Exécution financière avec application stricte de la séparation des tâches (SoD), authentification 2FA du Chef SFB et déboursement via Mobile Money (Sank, Coris, Moov) :"
    )
    img_seq_pay = "docs/conception/diagrams/5_seq_paiement.png"
    if os.path.exists(img_seq_pay):
        doc.add_picture(img_seq_pay, width=Inches(6.2))
        p_cap = doc.add_paragraph("Figure 6 : Séquence de Paiement Mobile Money sécurisé par second facteur")
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_cap.runs[0].font.size = Pt(9)
        p_cap.runs[0].italic = True

    doc.add_page_break()

    doc.add_heading("7. Diagramme de Séquence : Enlèvement Comptoir & Réception Magasins", level=2)
    doc.add_paragraph(
        "Gestion logistique des transports, pesée contradictoire au départ et à l'arrivée et détection automatique des écarts de transit avec alerte DGSA :"
    )
    img_seq_trans = "docs/conception/diagrams/6_seq_transfert.png"
    if os.path.exists(img_seq_trans):
        doc.add_picture(img_seq_trans, width=Inches(6.2))
        p_cap = doc.add_paragraph("Figure 7 : Séquence d'Enlèvement, Réception en Magasin central et calcul d'écart")
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_cap.runs[0].font.size = Pt(9)
        p_cap.runs[0].italic = True

    doc.add_paragraph("\n")
    doc.add_heading("8. Diagramme de Classes & Modèle de Données JPA", level=2)
    doc.add_paragraph(
        "Structure relationnelle complète des entités Spring Data JPA organisée en 3 sous-diagrammes thématiques :\n"
        "  (A) Sécurité & Découpage Administratif BF — IAM, RBAC, 2FA, Audit, Locality/LocalityHierarchy ;\n"
        "  (B) Paramétrage, Collecte Bord Champ & Paiements — Campagne, BRBC, FP, Paiement Mobile Money ;\n"
        "  (C) Stocks & Logistique — FSJ, Emballages, BEBC, Magasins."
    )

    # --- Figure 8a : Sécurité & Découpage Administratif ---
    doc.add_heading("Figure 8a — Module Sécurité, IAM & Découpage Administratif Officiel BF", level=3)
    img_8a = "docs/conception/diagrams/2a_classes_securite_admin.png"
    if os.path.exists(img_8a):
        doc.add_picture(img_8a, width=Inches(6.5))
        p_cap = doc.add_paragraph(
            "Figure 8a : Sécurité & IAM (Utilisateur, Role, Permission, TwoFactorAuth, RefreshToken, TabletteDevice, JournalAudit) "
            "+ Découpage Administratif Officiel BF — Locality / LocalityHierarchy (Closure Table) — "
            "DGTD/DCSI, Décrets Juillet 2025 : 17 Régions, 47 Provinces, 351 Communes."
        )
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_cap.runs[0].font.size = Pt(9)
        p_cap.runs[0].italic = True

    doc.add_page_break()

    # --- Figure 8b : Collecte Bord Champ & Paiements ---
    doc.add_heading("Figure 8b — Module Paramétrage, Collecte Bord Champ & Paiements", level=3)
    img_8b = "docs/conception/diagrams/2b_classes_collecte_paiement.png"
    if os.path.exists(img_8b):
        doc.add_picture(img_8b, width=Inches(6.5))
        p_cap = doc.add_paragraph(
            "Figure 8b : Paramétrage (Campagne, Speculation, PrixGarantie, Comptoir) + Collecte Bord Champ "
            "(Producteur localisé via Locality, CollecteBordChamp, ControleQualite, FichePesee_FP, "
            "BordereauReception_BRBC) + Paiement Mobile Money (OrdrePaiement)."
        )
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_cap.runs[0].font.size = Pt(9)
        p_cap.runs[0].italic = True

    doc.add_page_break()

    # --- Figure 8c : Stocks & Logistique ---
    doc.add_heading("Figure 8c — Module Stocks & Logistique", level=3)
    img_8c = "docs/conception/diagrams/2c_classes_stocks_logistique.png"
    if os.path.exists(img_8c):
        doc.add_picture(img_8c, width=Inches(6.5))
        p_cap = doc.add_paragraph(
            "Figure 8c : Stocks & Logistique — FicheStockJournaliere_FSJ (Feuille FSJ), "
            "FicheStockMateriel_Emballages (Feuil2), BordereauEnlevement_BEBC (Feuille BEBC), "
            "MagasinCentral, BordereauReceptionMagasin."
        )
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_cap.runs[0].font.size = Pt(9)
        p_cap.runs[0].italic = True

    doc.add_page_break()


    # --- SECTION IX : CONCEPTION DE LA BASE DE DONNÉES ET DICTIONNAIRE ---
    h_ix = doc.add_heading("IX. Conception de la base de données et modèle relationnel", level=1)
    h_ix.style.font.color.rgb = primary_color
    doc.add_paragraph(
        "La base PostgreSQL 16 est organisée selon une architecture relationnelle en 3e forme normale (3NF) avec contraintes d'intégrité strictes.\n"
        "Extrait du dictionnaire des données des tables pivots :"
    )
    
    t_dict = doc.add_table(rows=19, cols=5)
    t_dict.alignment = WD_TABLE_ALIGNMENT.CENTER
    h_d = ["Table / Entité", "Attribut technique", "Type SQL", "Contrainte", "Correspondance Outil Excel"]
    for j, val in enumerate(h_d):
        c = t_dict.rows[0].cells[j]
        c.text = val
        c.paragraphs[0].runs[0].bold = True
        c.paragraphs[0].runs[0].font.size = Pt(8.5)
        set_cell_background(c, header_bg)
        c.paragraphs[0].runs[0].font.color.rgb = RGBColor(255, 255, 255)

    dict_data = [
        ("t_producteur", "cnib_numero / lieu / date", "VARCHAR(30), DATE", "NOT NULL", "Feuilles BRBC & FP : N° CNIB, du, Lieu"),
        ("t_producteur", "nom_prenom / telephone", "VARCHAR(150), VARCHAR(20)", "NOT NULL", "Feuilles BRBC & FP : Nom, Prénom, Tél"),
        ("t_fiche_pesee_fp", "numero_fiche / site_comptoir", "VARCHAR(30)", "UNIQUE NOT NULL", "Feuille FP : N° fiche, Site de collecte"),
        ("t_fiche_pesee_fp", "poids_brut / tare / poids_net", "NUMERIC(10,2)", "NOT NULL", "Feuille FP : Grille des pesées individuelles"),
        ("t_brbc", "numero_brbc", "VARCHAR(30)", "UNIQUE NOT NULL", "Feuille BRBC : N° Bordereau Réception"),
        ("t_brbc", "quantite_kg / pu_cfa", "NUMERIC(10,2)", "NOT NULL", "Feuille BRBC : Quantité (KG), PU"),
        ("t_brbc", "montant_net_cfa / en_lettres", "NUMERIC(12,2), TEXT", "NOT NULL", "Feuille BRBC : Montant chiffré & en lettres"),
        ("t_brbc", "observations_receptionnaire", "TEXT", "NULLABLE", "Feuille BRBC : Observations du réceptionnaire"),
        ("t_bebc", "numero_bebc", "VARCHAR(30)", "UNIQUE NOT NULL", "Feuille BEBC : N° Bordereau d'Enlèvement"),
        ("t_bebc", "objet_enlevement / beneficiaire", "VARCHAR(150)", "NOT NULL", "Feuille BEBC : Objet enlèvement, Bénéficiaires"),
        ("t_bebc", "colisage / nbre_sacs / tonnage", "VARCHAR(30), INT, NUMERIC", "NOT NULL", "Feuille BEBC : Colisage, Nbre sacs, Tonnage"),
        ("t_bebc", "transporteur_nom / n_vehicule", "VARCHAR(100), VARCHAR(25)", "NOT NULL", "Feuille BEBC : Identité complète, N° Véhicule"),
        ("t_fsj", "date / nature_operation", "DATE, VARCHAR(50)", "NOT NULL", "Feuille FSJ : Date, Nature opération"),
        ("t_fsj", "entree / sortie / stock (sacs/kg/valeur)", "INT, NUMERIC, NUMERIC", "NOT NULL", "Feuille FSJ : Entrée, Sortie, Stock"),
        ("t_stock_materiel", "designation / reference_doc", "VARCHAR(100), VARCHAR(50)", "NOT NULL", "Feuille Feuil2 : Désignation, Réfé Doc"),
        ("t_ordre_paiement", "ref_transaction / statut / operateur", "VARCHAR(50)", "NOT NULL", "Paiement Mobile Money (Sank, Coris, Moov)"),
        ("localities", "code / name / type (LocalityType)", "VARCHAR(255), ENUM", "UNIQUE NOT NULL", "Découpge Administratif BF : COUNTRY/REGION/PROVINCE/COMMUNE/SECTOR/VILLAGE"),
        ("locality_hierarchy", "ancestor_id / descendant_id / depth", "UUID, UUID, INT", "PK composite NOT NULL", "Closure Table : Navigation O(1) dans l'arbre administratif BF")
    ]
    for i, row in enumerate(dict_data):
        for j, val in enumerate(row):
            c = t_dict.rows[i+1].cells[j]
            c.text = val
            c.paragraphs[0].runs[0].font.size = Pt(8)
            if i % 2 == 1:
                set_cell_background(c, alt_row_bg)

    doc.add_page_break()

    # --- SECTION X : CONCEPTION DES TESTS ---
    h_x = doc.add_heading("X. Stratégie et conception des tests", level=1)
    h_x.style.font.color.rgb = primary_color
    doc.add_paragraph(
        "Afin de garantir une qualité irréprochable et l'absence de régression, la stratégie de test s'articule autour de la pyramide des tests :\n"
        "• Tests unitaires (JUnit 5, Mockito) : Couverture minimale de 80 % sur les services métiers (calculs de tare, barèmes qualité, calcul des montants) ;\n"
        "• Tests d'intégration (Spring Boot Test, Testcontainers PostgreSQL) : Validation des flux transactionnels, isolation ACID et contraintes de base de données ;\n"
        "• Tests d'API (MockMvc / REST Assured) : Validation des codes retours HTTP, pagination, sécurité des routes et validation des schémas JSON DTO ;\n"
        "• Tests End-to-End (Playwright) : Scénarios complets sur Angular (de la consultation des collectes à la validation financière 2FA) ;\n"
        "• Tests de résilience mobile : Déconnexion volontaire réseau, extinction de tablette en cours de pesée, reprise sur coupure et validation de l'intégrité SQLite locale."
    )

    # --- SECTION XI : INFRASTRUCTURE, DÉPLOIEMENT & DEVOPS ---
    h_xi = doc.add_heading("XI. Infrastructure, déploiement et DevSecOps", level=1)
    h_xi.style.font.color.rgb = primary_color
    doc.add_paragraph(
        "L'hébergement est prévu sur l'infrastructure souveraine de l'État (Data Center ANPTIC ou cloud gouvernemental) :\n"
        "• Environnements cloisonnés : DÉVELOPPEMENT, PRÉ-PRODUCTION / RECETTE, PRODUCTION ;\n"
        "• Conteneurisation Docker : Chaque composant (Spring Boot, Angular/Nginx, PostgreSQL, Redis) dispose d'une image Docker sécurisée et allégée ;\n"
        "• Pipeline CI/CD automatisé : Compilation Maven, tests automatisés, analyse de qualité SonarQube, audit de vulnérabilités des dépendances (Trivy/OWASP Dependency-Check) et déploiement sans interruption de service (Rolling Update) ;\n"
        "• Supervision et métriques : Intégration de Spring Boot Actuator, Prometheus et Grafana pour la surveillance continue du CPU, de la mémoire et des temps de réponse API."
    )

    # --- SECTION XII : MAINTENANCE, ÉVOLUTIVITÉ ET FEUILLE DE ROUTE ---
    h_xii = doc.add_heading("XII. Maintenance, conduite du changement et feuille de route", level=1)
    h_xii.style.font.color.rgb = primary_color
    doc.add_paragraph(
        "Feuille de route de mise en œuvre par tranches verticales (4 Sprints prioritaires) :\n"
        "• Tranche 1 (Sprint 1) : Socle Sécurité IAM, RBAC, paramétrage des comptoirs, spéculations et campagnes 2026 ;\n"
        "• Tranche 2 (Sprint 2) : Application Android de collecte terrain (offline-first), contrôles qualité, pesée et émission des BRBC dématérialisés ;\n"
        "• Tranche 3 (Sprint 3) : Portail Web Angular (SCP/DCT), chaîne d'approbation et module de paiement mobile sécurisé 2FA (DFC/SFB) ;\n"
        "• Tranche 4 (Sprint 4) : Module logistique d'enlèvement, réceptions magasins centraux (SGS/DGSA), lettrage SYSCOHADA et audit de sécurité ANSSI pour mise en production officielle."
    )

    # Save document
    doc.save(output_path)
    print(f"Document successfully created at: {output_path}")

if __name__ == "__main__":
    out_file = "/Users/anptic/Documents/DGTD/DCSI/ProjetInformatique/CBC/Document_Technique_de_Conception_CBC_SONAGESS_V1.docx"
    create_dtc_document(out_file)
