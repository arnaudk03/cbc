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
    ? `
      <div class="bloc">
        <h2>Réception en magasin</h2>
        <div class="grille">
          <div class="champ"><span>Numéro de réception</span><strong>${echapper(transfert.reception.numeroReception)}</strong></div>
          <div class="champ"><span>Magasin</span><strong>${echapper(transfert.reception.magasinNom)}</strong></div>
          <div class="champ"><span>Magasinier</span><strong>${echapper(transfert.reception.magasinier)}</strong></div>
          <div class="champ"><span>Date de déchargement</span><strong>${formaterDate(transfert.reception.dateDechargement)}</strong></div>
          <div class="champ"><span>Sacs reçus</span><strong>${echapper(transfert.reception.nombreSacsRecus)}</strong></div>
          <div class="champ"><span>Tonnage déchargé</span><strong>${formaterKg(transfert.reception.tonnageDechargeKg)}</strong></div>
          <div class="champ"><span>Écart</span><strong>${formaterKg(transfert.reception.ecartPoidsKg)}</strong></div>
          <div class="champ"><span>Conformité</span><strong>${echapper(transfert.reception.statutConformite)}</strong></div>
        </div>
      </div>`
    : '';

  return `<!doctype html>
<html lang="fr">
<head>
  <meta charset="utf-8" />
  <title>BEBC ${echapper(transfert.numeroBebc)}</title>
  <style>
    @page { size: A4 portrait; margin: 12mm; }
    * { box-sizing: border-box; }
    body {
      margin: 0;
      color: #0f172a;
      font-family: Arial, Helvetica, sans-serif;
      font-size: 11px;
      line-height: 1.4;
    }
    .entete {
      display: flex;
      justify-content: space-between;
      gap: 24px;
      padding-bottom: 12px;
      border-bottom: 3px solid #0b4d2c;
    }
    .pays {
      color: #64748b;
      font-size: 9px;
      font-weight: 700;
      letter-spacing: 0.14em;
      text-transform: uppercase;
    }
    .institution {
      margin-top: 4px;
      color: #0b4d2c;
      font-size: 17px;
      font-weight: 800;
      letter-spacing: 0.04em;
    }
    .devise {
      margin-top: 2px;
      color: #475569;
      font-size: 9px;
      font-style: italic;
    }
    .entete-droite { text-align: right; }
    .numero {
      color: #0b4d2c;
      font-size: 14px;
      font-weight: 800;
    }
    .statut {
      display: inline-block;
      margin-top: 6px;
      padding: 3px 10px;
      border-radius: 999px;
      background: #e9f9ef;
      color: #15803d;
      font-size: 9px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
    h1 {
      margin: 20px 0 2px;
      color: #0f172a;
      font-size: 19px;
      text-align: center;
      text-transform: uppercase;
      letter-spacing: 0.06em;
    }
    .sous-titre {
      margin-bottom: 18px;
      color: #64748b;
      font-size: 10px;
      text-align: center;
    }
    .grille {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 8px 28px;
    }
    .champ {
      padding: 6px 0;
      border-bottom: 1px solid #e2e8f0;
    }
    .champ span {
      display: block;
      color: #64748b;
      font-size: 8px;
      font-weight: 700;
      letter-spacing: 0.08em;
      text-transform: uppercase;
    }
    .champ strong {
      display: block;
      margin-top: 2px;
      color: #1e293b;
      font-size: 11px;
    }
    .bloc { margin-top: 18px; }
    .bloc h2 {
      margin: 0 0 8px;
      padding-bottom: 5px;
      border-bottom: 1px solid #cbd5e1;
      color: #0b4d2c;
      font-size: 12px;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
    table {
      width: 100%;
      margin-top: 8px;
      border-collapse: collapse;
    }
    th {
      padding: 7px 8px;
      background: #f1f5f9;
      border: 1px solid #cbd5e1;
      color: #475569;
      font-size: 8px;
      font-weight: 700;
      letter-spacing: 0.06em;
      text-align: left;
      text-transform: uppercase;
    }
    td {
      padding: 7px 8px;
      border: 1px solid #e2e8f0;
      color: #334155;
      vertical-align: top;
    }
    .centre { text-align: center; }
    .droite { text-align: right; font-variant-numeric: tabular-nums; }
    .sous-texte {
      display: block;
      margin-top: 1px;
      color: #94a3b8;
      font-size: 8px;
    }
    .totaux {
      display: flex;
      justify-content: flex-end;
      gap: 28px;
      margin-top: 10px;
      padding: 9px 12px;
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      font-weight: 700;
    }
    .totaux span { color: #475569; font-size: 9px; text-transform: uppercase; }
    .totaux strong { color: #0f172a; }
    .signatures {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 28px;
      margin-top: 42px;
    }
    .signature {
      padding-top: 8px;
      border-top: 1px solid #94a3b8;
      color: #475569;
      font-size: 9px;
      text-align: center;
    }
    .signature strong {
      display: block;
      margin-bottom: 32px;
      color: #1e293b;
      font-size: 10px;
    }
    .pied {
      margin-top: 28px;
      padding-top: 8px;
      border-top: 1px solid #e2e8f0;
      color: #94a3b8;
      font-size: 8px;
      text-align: center;
    }
  </style>
</head>
<body>
  <header class="entete">
    <div>
      <div class="pays">Burkina Faso</div>
      <div class="institution">SONAGESS</div>
      <div class="devise">Unité — Progrès — Justice</div>
    </div>
    <div class="entete-droite">
      <div class="pays">Bordereau d’enlèvement</div>
      <div class="numero">${echapper(transfert.numeroBebc)}</div>
      <div class="statut">${echapper(STATUT_BEBC_LABELS[transfert.statut] ?? transfert.statut)}</div>
    </div>
  </header>

  <h1>Bordereau d’enlèvement BEBC</h1>
  <div class="sous-titre">Transfert de stock du comptoir vers un magasin central</div>

  <div class="grille">
    <div class="champ"><span>Comptoir de départ</span><strong>${echapper(transfert.comptoirNom)}</strong></div>
    <div class="champ"><span>Magasin de destination</span><strong>${echapper(transfert.magasinDestinationNom)}</strong></div>
    <div class="champ"><span>Direction</span><strong>${echapper(transfert.direction)}</strong></div>
    <div class="champ"><span>Localité</span><strong>${echapper(transfert.localite)}</strong></div>
    <div class="champ"><span>Bénéficiaire</span><strong>${echapper(transfert.beneficiaire)}</strong></div>
    <div class="champ"><span>Objet de l’enlèvement</span><strong>${echapper(transfert.objetEnlevement)}</strong></div>
    <div class="champ"><span>Date d’enlèvement</span><strong>${formaterDate(transfert.dateEnlevement)}</strong></div>
    <div class="champ"><span>Date d’arrivée prévue</span><strong>${formaterDateCourte(transfert.dateArriveePrevue)}</strong></div>
    <div class="champ"><span>Transporteur</span><strong>${echapper(transfert.nomTransporteur)}</strong></div>
    <div class="champ"><span>Véhicule</span><strong>${echapper(transfert.numeroVehicule)}</strong></div>
  </div>

  <div class="bloc">
    <h2>Détail des spéculations enlevées</h2>
    <table>
      <thead>
        <tr>
          <th style="width: 36px">N°</th>
          <th>Spéculation</th>
          <th class="droite" style="width: 80px">Sacs</th>
          <th class="droite" style="width: 110px">Colisage</th>
          <th class="droite" style="width: 120px">Quantité</th>
        </tr>
      </thead>
      <tbody>${lignes || '<tr><td colspan="5" class="centre">Aucune ligne de spéculation</td></tr>'}</tbody>
    </table>
    <div class="totaux">
      <span>Total sacs <strong>${echapper(transfert.nombreSacs)}</strong></span>
      <span>Tonnage enlevé <strong>${formaterKg(transfert.tonnageEnleveKg)}</strong></span>
    </div>
  </div>

  ${reception}

  <div class="signatures">
    <div class="signature"><strong>L’agent de collecte</strong>Nom et signature</div>
    <div class="signature"><strong>Le transporteur</strong>Nom et signature</div>
    <div class="signature"><strong>Le magasinier</strong>Nom et signature</div>
  </div>

  <div class="pied">
    Document généré par la plateforme SONAGESS CBC — ${formaterDate(new Date().toISOString())}
  </div>
</body>
</html>`;
}
