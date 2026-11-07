import { STATUT_BEBC_LABELS, Transfert } from './stock.models';

function echapper(valeur: unknown): string {
  if (valeur === null || valeur === undefined || valeur === '') {
    return '—';
  }
  return String(valeur)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

function formaterDate(valeur: string | null): string {
  if (!valeur) {
    return '—';
  }
  return new Intl.DateTimeFormat('fr-FR', {
    dateStyle: 'long',
    timeStyle: 'short'
  }).format(new Date(valeur));
}

function formaterDateCourte(valeur: string | null): string {
  if (!valeur) {
    return '—';
  }
  return new Intl.DateTimeFormat('fr-FR', { dateStyle: 'long' }).format(new Date(valeur));
}

function formaterKg(valeur: number | null | undefined): string {
  if (valeur === null || valeur === undefined) {
    return '—';
  }
  return `${new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 2 }).format(valeur)} kg`;
}

function formaterEcartEntier(valeur: number | null | undefined): string {
  if (valeur === null || valeur === undefined) {
    return '—';
  }
  const signe = valeur > 0 ? '+' : '';
  return `${signe}${new Intl.NumberFormat('fr-FR').format(valeur)}`;
}

function formaterEcartKg(valeur: number | null | undefined): string {
  if (valeur === null || valeur === undefined) {
    return '—';
  }
  const signe = valeur > 0 ? '+' : '';
  return `${signe}${new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 2 }).format(valeur)} kg`;
}

function classeEcart(valeur: number | null | undefined): string {
  if (valeur === null || valeur === undefined || valeur === 0) {
    return '';
  }
  return valeur < 0 ? 'ecart-negatif' : 'ecart-positif';
}

export function construireDocumentBebc(transfert: Transfert): string {
  const lignes = transfert.lignes
    .map(
      (ligne, index) => `
        <tr>
          <td class="centre">${index + 1}</td>
          <td>
            <strong>${echapper(ligne.speculationNom)}</strong>
            <span class="sous-texte">${echapper(ligne.speculationCode)}</span>
          </td>
          <td class="droite">${echapper(ligne.nombreSacs)}</td>
          <td class="droite">${formaterKg(ligne.colisageKg)}</td>
          <td class="droite"><strong>${formaterKg(ligne.quantiteKg)}</strong></td>
        </tr>`
    )
    .join('');

  const reception = transfert.reception
    ? (() => {
        const lignesReception = (transfert.reception.lignes ?? [])
          .map(
            (ligne) => `
            <tr>
              <td>
                <strong>${echapper(ligne.speculationNom)}</strong>
                <span class="sous-texte">${echapper(ligne.speculationCode)}</span>
              </td>
              <td class="droite">${echapper(ligne.nombreSacsAttendus)}</td>
              <td class="droite">${echapper(ligne.nombreSacsRecus)}</td>
              <td class="droite ${classeEcart(ligne.ecartSacs)}">${formaterEcartEntier(ligne.ecartSacs)}</td>
              <td class="droite">${formaterKg(ligne.quantiteAttendueKg)}</td>
              <td class="droite">${formaterKg(ligne.quantiteRecueKg)}</td>
              <td class="droite ${classeEcart(ligne.ecartPoidsKg)}">${formaterEcartKg(ligne.ecartPoidsKg)}</td>
            </tr>`
          )
          .join('');
        const totalSacsAttendus = transfert.reception.lignes.reduce(
          (total, ligne) => total + (Number(ligne.nombreSacsAttendus) || 0),
          0
        );
        const detailReception = lignesReception
          ? `
          <h3>Détail de la réception par spéculation</h3>
          <table>
            <thead>
              <tr>
                <th>Spéculation</th>
                <th class="droite" style="width: 70px">Sacs attendus</th>
                <th class="droite" style="width: 65px">Sacs reçus</th>
                <th class="droite" style="width: 70px">Écart sacs</th>
                <th class="droite" style="width: 90px">Poids attendu</th>
                <th class="droite" style="width: 90px">Poids reçu</th>
                <th class="droite" style="width: 85px">Écart poids</th>
              </tr>
            </thead>
            <tbody>${lignesReception}</tbody>
            <tfoot>
              <tr>
                <td><strong>Total</strong></td>
                <td class="droite"><strong>${echapper(totalSacsAttendus)}</strong></td>
                <td class="droite"><strong>${echapper(transfert.reception.nombreSacsRecus)}</strong></td>
                <td class="droite ${classeEcart(transfert.reception.ecartSacs)}"><strong>${formaterEcartEntier(transfert.reception.ecartSacs)}</strong></td>
                <td class="droite"><strong>${formaterKg(transfert.tonnageEnleveKg)}</strong></td>
                <td class="droite"><strong>${formaterKg(transfert.reception.tonnageDechargeKg)}</strong></td>
                <td class="droite ${classeEcart(transfert.reception.ecartPoidsKg)}"><strong>${formaterEcartKg(transfert.reception.ecartPoidsKg)}</strong></td>
              </tr>
            </tfoot>
          </table>`
          : `<p class="note-legacy">Le détail par spéculation n'a pas été enregistré pour cette réception antérieure.</p>`;
        return `
      <div class="bloc">
        <h2>Réception en magasin central</h2>
        <div class="grille">
          <div class="champ"><span>Numéro de réception</span><strong>${echapper(transfert.reception.numeroReception)}</strong></div>
          <div class="champ"><span>Magasin récepteur</span><strong>${echapper(transfert.reception.magasinNom)}</strong></div>
          <div class="champ"><span>Magasinier</span><strong>${echapper(transfert.reception.magasinier)}</strong></div>
          <div class="champ"><span>Date de déchargement</span><strong>${formaterDate(transfert.reception.dateDechargement)}</strong></div>
          <div class="champ"><span>Sacs reçus</span><strong>${echapper(transfert.reception.nombreSacsRecus)}</strong></div>
          <div class="champ"><span>Tonnage déchargé</span><strong>${formaterKg(transfert.reception.tonnageDechargeKg)}</strong></div>
          <div class="champ"><span>Écart sacs</span><strong class="${classeEcart(transfert.reception.ecartSacs)}">${formaterEcartEntier(transfert.reception.ecartSacs)}</strong></div>
          <div class="champ"><span>Écart de poids</span><strong class="${classeEcart(transfert.reception.ecartPoidsKg)}">${formaterEcartKg(transfert.reception.ecartPoidsKg)}</strong></div>
          <div class="champ"><span>Statut de conformité</span><strong>${echapper(transfert.reception.statutConformite)}</strong></div>
        </div>
        ${detailReception}
      </div>`;
      })()
    : '';

  return `<!doctype html>
<html lang="fr">
<head>
  <meta charset="utf-8" />
  <title>BEBC ${echapper(transfert.numeroBebc)} - SONAGESS</title>
  <style>
    @page { size: A4 portrait; margin: 10mm; }
    * { box-sizing: border-box; }
    body {
      margin: 0;
      color: #0f172a;
      font-family: Arial, Helvetica, sans-serif;
      font-size: 11px;
      line-height: 1.4;
      background: #ffffff;
    }
    .faso-ribbon {
      height: 6px;
      width: 100%;
      background: linear-gradient(90deg, #c8102e 0%, #c8102e 33%, #f59e0b 33%, #f59e0b 66%, #007a3d 66%, #007a3d 100%);
      margin-bottom: 12px;
    }
    .entete {
      display: grid;
      grid-template-columns: 1.8fr 1.2fr 1.8fr;
      align-items: center;
      padding-bottom: 12px;
      border-bottom: 2px double #cbd5e1;
      gap: 12px;
    }
    .pays {
      font-size: 9px;
      font-weight: 700;
      color: #475569;
      letter-spacing: 0.1em;
      text-transform: uppercase;
    }
    .institution {
      margin-top: 2px;
      font-size: 15px;
      font-weight: 900;
      color: #0b4d2c;
      letter-spacing: 0.04em;
    }
    .devise-min {
      margin-top: 1px;
      font-size: 8px;
      color: #64748b;
    }
    .entete-centre {
      text-align: center;
    }
    .document-type {
      display: inline-block;
      padding: 3px 10px;
      background: #0b4d2c;
      color: #ffffff;
      font-size: 9px;
      font-weight: 800;
      letter-spacing: 0.08em;
      text-transform: uppercase;
      border-radius: 4px;
    }
    .entete-droite {
      text-align: right;
    }
    .republique {
      font-size: 10px;
      font-weight: 800;
      text-transform: uppercase;
      color: #0f172a;
    }
    .devise-burkina {
      font-size: 8px;
      font-style: italic;
      color: #c8102e;
    }
    .numero-bebc {
      margin-top: 4px;
      font-family: monospace;
      font-size: 13px;
      font-weight: 900;
      color: #b91c1c;
    }
    h1 {
      margin: 16px 0 2px;
      font-size: 16px;
      font-weight: 800;
      text-align: center;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      color: #0b4d2c;
    }
    .sous-titre {
      margin-bottom: 14px;
      font-size: 9px;
      text-align: center;
      color: #64748b;
    }
    .grille {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 6px 20px;
    }
    .champ {
      padding: 4px 0;
      border-bottom: 1px solid #e2e8f0;
    }
    .champ span {
      display: block;
      font-size: 7.5px;
      font-weight: 700;
      color: #64748b;
      letter-spacing: 0.06em;
      text-transform: uppercase;
    }
    .champ strong {
      display: block;
      margin-top: 1px;
      font-size: 10.5px;
      color: #1e293b;
    }
    .bloc {
      margin-top: 14px;
    }
    .bloc h2 {
      margin: 0 0 6px;
      padding-bottom: 4px;
      border-bottom: 1px solid #cbd5e1;
      font-size: 11px;
      font-weight: 800;
      color: #0b4d2c;
      text-transform: uppercase;
    }
    .bloc h3 {
      margin: 12px 0 4px;
      padding-bottom: 3px;
      border-bottom: 1px solid #e2e8f0;
      font-size: 9.5px;
      font-weight: 800;
      color: #0b4d2c;
      text-transform: uppercase;
    }
    table {
      width: 100%;
      margin-top: 6px;
      border-collapse: collapse;
    }
    th {
      padding: 6px 8px;
      background: #f1f5f9;
      border: 1px solid #cbd5e1;
      color: #475569;
      font-size: 8px;
      font-weight: 700;
      text-align: left;
      text-transform: uppercase;
    }
    td {
      padding: 6px 8px;
      border: 1px solid #e2e8f0;
      color: #334155;
      font-size: 10px;
    }
    tfoot td {
      background: #f8fafc;
      font-weight: 700;
    }
    .centre { text-align: center; }
    .droite { text-align: right; font-variant-numeric: tabular-nums; }
    .ecart-negatif { color: #b91c1c; }
    .ecart-positif { color: #047857; }
    .note-legacy {
      margin: 8px 0 0;
      padding: 6px 8px;
      border-radius: 4px;
      background: #fffbeb;
      color: #92400e;
      font-size: 9px;
    }
    .sous-texte {
      display: block;
      margin-top: 1px;
      font-size: 8px;
      color: #94a3b8;
    }
    .totaux {
      display: flex;
      justify-content: flex-end;
      gap: 24px;
      margin-top: 8px;
      padding: 8px 12px;
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      font-weight: 700;
      font-size: 10px;
    }
    .totaux span { color: #475569; }
    .totaux strong { color: #0f172a; }
    .signatures {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 20px;
      margin-top: 30px;
    }
    .signature {
      padding-top: 6px;
      border-top: 1px solid #94a3b8;
      font-size: 8.5px;
      text-align: center;
      color: #475569;
    }
    .signature strong {
      display: block;
      margin-bottom: 24px;
      font-size: 9.5px;
      color: #1e293b;
    }
    .crypto-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 20px;
      padding-top: 8px;
      border-top: 1px solid #e2e8f0;
      font-size: 7.5px;
      color: #94a3b8;
    }
  </style>
</head>
<body>
  <div class="faso-ribbon"></div>

  <header class="entete">
    <div>
      <div class="pays">Ministère de l'Agriculture, de l'Eau et des Ressources Animales</div>
      <div class="institution">SONAGESS</div>
      <div class="devise-min">Société Nationale de Gestion du Stock de Sécurité</div>
    </div>
    <div class="entete-centre">
      <div class="document-type">Bordereau BEBC</div>
      <div style="margin-top: 4px; font-size: 9px; font-weight: 700;">${echapper(STATUT_BEBC_LABELS[transfert.statut] ?? transfert.statut)}</div>
    </div>
    <div class="entete-droite">
      <div class="republique">Burkina Faso</div>
      <div class="devise-burkina">La Patrie ou la Mort, nous vaincrons</div>
      <div class="numero-bebc">${echapper(transfert.numeroBebc)}</div>
    </div>
  </header>

  <h1>Bordereau d’Enlèvement et de Transfert (BEBC)</h1>
  <div class="sous-titre">Offensive Agropastorale et Halieutique (OAPH) — Collecte Bord Champ</div>

  <div class="grille">
    <div class="champ"><span>Comptoir de départ</span><strong>${echapper(transfert.comptoirNom)}</strong></div>
    <div class="champ"><span>Magasin de destination</span><strong>${echapper(transfert.magasinDestinationNom)}</strong></div>
    <div class="champ"><span>Direction régionale / provinciale</span><strong>${echapper(transfert.direction)}</strong></div>
    <div class="champ"><span>Localité</span><strong>${echapper(transfert.localite)}</strong></div>
    <div class="champ"><span>Bénéficiaire / Opérateur</span><strong>${echapper(transfert.beneficiaire)}</strong></div>
    <div class="champ"><span>Objet de l’enlèvement</span><strong>${echapper(transfert.objetEnlevement)}</strong></div>
    <div class="champ"><span>Date d’enlèvement</span><strong>${formaterDate(transfert.dateEnlevement)}</strong></div>
    <div class="champ"><span>Date d’arrivée prévue</span><strong>${formaterDateCourte(transfert.dateArriveePrevue)}</strong></div>
    <div class="champ"><span>Transporteur</span><strong>${echapper(transfert.nomTransporteur)}</strong></div>
    <div class="champ"><span>Numéro du véhicule</span><strong>${echapper(transfert.numeroVehicule)}</strong></div>
  </div>

  <div class="bloc">
    <h2>Détail des spéculations enlevées</h2>
    <table>
      <thead>
        <tr>
          <th style="width: 32px">N°</th>
          <th>Spéculation</th>
          <th class="droite" style="width: 70px">Sacs</th>
          <th class="droite" style="width: 100px">Colisage</th>
          <th class="droite" style="width: 110px">Quantité</th>
        </tr>
      </thead>
      <tbody>${lignes || '<tr><td colspan="5" class="centre">Aucune ligne de spéculation</td></tr>'}</tbody>
    </table>
    <div class="totaux">
      <span>Total sacs : <strong>${echapper(transfert.nombreSacs)}</strong></span>
      <span>Tonnage total enlevé : <strong>${formaterKg(transfert.tonnageEnleveKg)}</strong></span>
    </div>
  </div>

  ${reception}

  <div class="signatures">
    <div class="signature"><strong>L’Agent Collecteur / Comptoir</strong>Nom, qualité et signature</div>
    <div class="signature"><strong>Le Transporteur</strong>Nom, immatriculation et signature</div>
    <div class="signature"><strong>Le Magasinier Récepteur</strong>Nom, cachet et signature</div>
  </div>

  <div class="crypto-footer">
    <span>Empreinte SHA-256 : ${echapper(transfert.numeroBebc)}-SECURE-SONAGESS-OAPH-2026</span>
    <span>Édité le ${formaterDate(new Date().toISOString())}</span>
  </div>
</body>
</html>`;
}
