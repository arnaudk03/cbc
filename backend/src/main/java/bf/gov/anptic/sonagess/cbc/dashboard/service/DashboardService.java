package bf.gov.anptic.sonagess.cbc.dashboard.service;

import bf.gov.anptic.sonagess.cbc.common.ResourceNotFoundException;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Campagne;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.CampagneStatut;
import bf.gov.anptic.sonagess.cbc.collectes.domain.CollecteStatut;
import bf.gov.anptic.sonagess.cbc.paiements.domain.OrdrePaiementStatut;
import bf.gov.anptic.sonagess.cbc.collectes.domain.StatutValidationBrbc;
import bf.gov.anptic.sonagess.cbc.collectes.repository.BordereauReceptionBrbcRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.CampagneRepository;
import bf.gov.anptic.sonagess.cbc.collectes.repository.CollecteBordChampRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.ComptoirRepository;
import bf.gov.anptic.sonagess.cbc.paiements.repository.OrdrePaiementRepository;
import bf.gov.anptic.sonagess.cbc.dashboard.dto.DashboardDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CampagneRepository campagneRepository;
    private final CollecteBordChampRepository collecteRepository;
    private final ComptoirRepository comptoirRepository;
    private final BordereauReceptionBrbcRepository bordereauRepository;
    private final OrdrePaiementRepository paiementRepository;

    @Transactional(readOnly = true)
    public DashboardDtos.DashboardResponse tableauDeBord(Long campagneId) {
        Campagne campagne = campagneId != null
                ? campagneRepository.findById(campagneId)
                    .orElseThrow(() -> new ResourceNotFoundException("Campagne introuvable."))
                : campagneRepository.findFirstByStatutOrderByDateDebutDesc(CampagneStatut.OUVERTE)
                    .orElse(null);

        if (campagne == null) {
            return new DashboardDtos.DashboardResponse(null,
                    new DashboardDtos.Kpis(0.0, 0.0, 0.0, 0, 0.0,
                            comptoirRepository.countByActifTrue(), 0, 0),
                    List.of(), List.of(), List.of());
        }

        double objectif = valeurOuZero(campagne.getObjectifTotalTonnes());
        double collecte = arrondi(valeurOuZero(collecteRepository.sumQuantiteCollectee(
                campagne.getId(), CollecteStatut.REJETEE)) / 1000.0);
        double taux = objectif > 0 ? arrondi(collecte / objectif * 100.0) : 0.0;

        long enAttente = collecteRepository.countByCampagneIdAndStatut(
                campagne.getId(), CollecteStatut.SYNCHRONISEE)
                + collecteRepository.countByCampagneIdAndStatut(
                campagne.getId(), CollecteStatut.EXAMINEE_SCP);

        DashboardDtos.Kpis kpis = new DashboardDtos.Kpis(
                arrondi(objectif),
                arrondi(collecte),
                taux,
                bordereauRepository.countByStatutValidation(StatutValidationBrbc.VALIDE_DCT),
                arrondi(valeurOuZero(paiementRepository.sumMontantByStatut(OrdrePaiementStatut.CONFIRME))),
                comptoirRepository.countByActifTrue(),
                enAttente,
                paiementRepository.countByStatut(OrdrePaiementStatut.INITIE)
        );

        List<DashboardDtos.SpeculationStat> speculations = collecteRepository
                .sumQuantiteParSpeculation(campagne.getId(), CollecteStatut.REJETEE).stream()
                .map(ligne -> new DashboardDtos.SpeculationStat(
                        (String) ligne[0], (String) ligne[1],
                        arrondi(((Number) ligne[2]).doubleValue() / 1000.0)))
                .toList();

        List<DashboardDtos.RegionStat> regions = collecteRepository
                .sumQuantiteParRegion(campagne.getId(), CollecteStatut.REJETEE.name()).stream()
                .map(ligne -> new DashboardDtos.RegionStat(
                        (String) ligne[0], arrondi(((Number) ligne[1]).doubleValue() / 1000.0)))
                .toList();

        List<DashboardDtos.EvolutionPoint> evolution = collecteRepository
                .evolutionMensuelle(campagne.getId(), CollecteStatut.REJETEE).stream()
                .map(ligne -> new DashboardDtos.EvolutionPoint(
                        (String) ligne[0], arrondi(((Number) ligne[1]).doubleValue() / 1000.0)))
                .toList();

        DashboardDtos.CampagneInfo info = new DashboardDtos.CampagneInfo(
                campagne.getId(), campagne.getCode(), campagne.getLibelle(), campagne.getStatut().name(),
                campagne.getDateDebut(), campagne.getDateFin());

        return new DashboardDtos.DashboardResponse(info, kpis, speculations, regions, evolution);
    }

    private double valeurOuZero(Double valeur) {
        return valeur != null ? valeur : 0.0;
    }

    private double arrondi(double valeur) {
        return Math.round(valeur * 100.0) / 100.0;
    }
}
