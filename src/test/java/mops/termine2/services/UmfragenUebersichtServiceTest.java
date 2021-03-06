package mops.termine2.services;

import mops.termine2.authentication.Account;
import mops.termine2.database.BenutzerGruppeRepository;
import mops.termine2.database.UmfrageAntwortRepository;
import mops.termine2.database.UmfrageRepository;
import mops.termine2.database.entities.BenutzerGruppeDB;
import mops.termine2.database.entities.UmfrageDB;
import mops.termine2.models.Gruppe;
import mops.termine2.models.Umfrage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UmfragenUebersichtServiceTest {
	
	private transient UmfragenUebersichtService umfrageuebersichtService;
	
	private transient UmfrageService umfrageService;
	
	private transient GruppeService gruppeService;
	
	private transient UmfrageRepository umfrageRepository;
	
	private transient BenutzerGruppeRepository benutzerGruppeRepository;
	
	private transient UmfrageAntwortRepository antwortRepo;
	
	private transient UmfrageAntwortService umfrageAntwortService;
	
	@BeforeEach
	public void setUp() {
		umfrageRepository = mock(UmfrageRepository.class);
		antwortRepo = mock(UmfrageAntwortRepository.class);
		benutzerGruppeRepository = mock(BenutzerGruppeRepository.class);
		
		umfrageService = new UmfrageService(umfrageRepository, antwortRepo);
		umfrageAntwortService = new UmfrageAntwortService(antwortRepo, umfrageRepository);
		gruppeService = new GruppeService(benutzerGruppeRepository);
		umfrageuebersichtService = new UmfragenUebersichtService(
			umfrageService, gruppeService, umfrageAntwortService);
	}
	
	@Test
	public void testLoadOffeneUmfragenFuerBenutzer() {
		List<Integer> days = new ArrayList<>(Arrays.asList(5, -5, 1, -2));
		Account account = new Account("studentin", null, null, null);
		LocalDateTime ldt = LocalDateTime.now();
		
		BenutzerGruppeDB gruppe = new BenutzerGruppeDB();
		gruppe.setGruppe("Gruppe");
		gruppe.setGruppeId("1");
		
		List<Umfrage> umfragen = new ArrayList<>();
		List<UmfrageDB> umfragenDB = new ArrayList<>();
		
		for (Integer day : days) {
			Umfrage umfrage = new Umfrage();
			umfrage.setLink(day.toString());
			umfrage.setFrist(ldt.plusDays(day));
			umfrage.setVorschlaege(new ArrayList<>());
			umfragen.add(umfrage);
			
			UmfrageDB umfrageDB = new UmfrageDB();
			umfrageDB.setLink(day.toString());
			umfrageDB.setFrist(ldt.plusDays(day));
			umfragenDB.add(umfrageDB);
		}
		
		when(antwortRepo.findByBenutzerAndUmfrageLink(account.getName(), "5")).thenReturn(
			new ArrayList<>());
		when(antwortRepo.findByBenutzerAndUmfrageLink(account.getName(), "1")).thenReturn(
			new ArrayList<>());
		when(benutzerGruppeRepository.findByBenutzer(account.getName())).thenReturn(
			new ArrayList<>(Arrays.asList(gruppe)));
		when(umfrageRepository.findByGruppeIdOrderByFristAsc(gruppe.getGruppeId()))
			.thenReturn(umfragenDB);
		
		List<Umfrage> ergebnis =
			umfrageuebersichtService.loadOffeneUmfragen(account, null);
		List<Umfrage> erwartet =
			new ArrayList<>(Arrays.asList(umfragen.get(2), umfragen.get(0)));
		
		assertThat(ergebnis).isEqualTo(erwartet);
	}
	
	@Test
	public void testLoadAbgeschlosseneUmfragenFuerBenutzer() {
		List<Integer> fristTage = new ArrayList<>(Arrays.asList(5, -5, 1, -2));
		List<String> ergebnisse = new ArrayList<String>(Arrays.asList("1", "-2", "-1", "2"));
		Account account = new Account("studentin", null, null, null);
		LocalDateTime ldt = LocalDateTime.now();
		
		BenutzerGruppeDB gruppe = new BenutzerGruppeDB();
		gruppe.setGruppe("Gruppe");
		gruppe.setGruppeId("1");
		
		List<Umfrage> umfragen = new ArrayList<>();
		List<UmfrageDB> umfragenDB = new ArrayList<>();
		
		for (int i = 0; i < fristTage.size(); i++) {
			Umfrage umfrage = new Umfrage();
			umfrage.setLink(fristTage.get(i).toString());
			umfrage.setFrist(ldt.plusDays(fristTage.get(i)));
			umfrage.setErgebnis(ergebnisse.get(i));
			umfrage.setVorschlaege(new ArrayList<>());
			umfragen.add(umfrage);
			
			UmfrageDB umfrageDB = new UmfrageDB();
			umfrageDB.setLink(fristTage.get(i).toString());
			umfrageDB.setFrist(ldt.plusDays(fristTage.get(i)));
			umfrageDB.setErgebnis(ergebnisse.get(i));
			umfragenDB.add(umfrageDB);
		}
		
		when(benutzerGruppeRepository.findByBenutzer(account.getName())).thenReturn(
			new ArrayList<>(Arrays.asList(gruppe)));
		when(umfrageRepository.findByGruppeIdOrderByFristAsc(gruppe.getGruppeId()))
			.thenReturn(umfragenDB);
		
		List<Umfrage> ergebnis =
			umfrageuebersichtService.loadAbgeschlosseneUmfragen(account, null);
		List<Umfrage> erwartet =
			new ArrayList<>(Arrays.asList(umfragen.get(1), umfragen.get(3)));
		
		assertThat(ergebnis).isEqualTo(erwartet);
	}
	
	@Test
	public void testLoadOffeneUmfragenFuerGruppe() {
		List<Integer> days = new ArrayList<>(Arrays.asList(5, -5, 1, -2, 3));
		List<String> gruppenIds = new ArrayList<>(Arrays.asList("1", "5", "5", "7", "5"));
		Account account = new Account("studentin", null, null, null);
		LocalDateTime ldt = LocalDateTime.now();
		
		Gruppe gruppe = new Gruppe();
		gruppe.setName("Gruppe");
		gruppe.setId("5");
		
		List<Umfrage> umfragen = new ArrayList<>();
		List<UmfrageDB> unfragenDB = new ArrayList<>();
		
		for (int i = 0; i < days.size(); i++) {
			Umfrage umfrage = new Umfrage();
			umfrage.setLink(days.get(i).toString());
			umfrage.setFrist(ldt.plusDays(days.get(i)));
			umfrage.setGruppeId(gruppenIds.get(i));
			umfrage.setVorschlaege(new ArrayList<>());
			umfragen.add(umfrage);
			
			if (gruppenIds.get(i).equals(gruppe.getId())) {
				UmfrageDB umfrageDB = new UmfrageDB();
				umfrageDB.setLink(days.get(i).toString());
				umfrageDB.setFrist(ldt.plusDays(days.get(i)));
				umfrageDB.setGruppeId(gruppenIds.get(i));
				unfragenDB.add(umfrageDB);
			}
		}
		when(umfrageRepository.findByGruppeIdOrderByFristAsc(gruppe.getId()))
			.thenReturn(unfragenDB);
		
		List<Umfrage> ergebnis =
			umfrageuebersichtService.loadOffeneUmfragen(account, gruppe);
		List<Umfrage> erwartet =
			new ArrayList<>(Arrays.asList(umfragen.get(2), umfragen.get(4)));
		
		assertThat(ergebnis).isEqualTo(erwartet);
	}
	
	@Test
	public void testLoadAbgeschlosseneUmfragenFuerGruppe() {
		List<Integer> days = new ArrayList<>(Arrays.asList(5, -5, 1, -2, 3, -7));
		List<String> ergebnisse = new ArrayList<String>(Arrays.asList("1", "-2", "-1", "2", "3", "6"));
		List<String> gruppenIds = new ArrayList<>(Arrays.asList("1", "5", "5", "7", "5", "5"));
		Account account = new Account("studentin", null, null, null);
		LocalDateTime ldt = LocalDateTime.now();
		
		Gruppe gruppe = new Gruppe();
		gruppe.setName("Gruppe");
		gruppe.setId("5");
		
		List<Umfrage> umfragen = new ArrayList<>();
		List<UmfrageDB> umfragenDB = new ArrayList<>();
		
		for (int i = 0; i < days.size(); i++) {
			Umfrage umfrage = new Umfrage();
			umfrage.setLink(days.get(i).toString());
			umfrage.setFrist(ldt.plusDays(days.get(i)));
			umfrage.setGruppeId(gruppenIds.get(i));
			umfrage.setErgebnis(ergebnisse.get(i));
			umfrage.setVorschlaege(new ArrayList<>());
			umfragen.add(umfrage);
			
			if (gruppenIds.get(i).equals(gruppe.getId())) {
				UmfrageDB umfrageDB = new UmfrageDB();
				umfrageDB.setLink(days.get(i).toString());
				umfrageDB.setFrist(ldt.plusDays(days.get(i)));
				umfrageDB.setGruppeId(gruppenIds.get(i));
				umfrageDB.setErgebnis(ergebnisse.get(i));
				umfragenDB.add(umfrageDB);
			}
		}
		
		when(umfrageRepository.findByGruppeIdOrderByFristAsc(gruppe.getId()))
			.thenReturn(umfragenDB);
		
		List<Umfrage> result =
			umfrageuebersichtService.loadAbgeschlosseneUmfragen(account, gruppe);
		List<Umfrage> erwartet =
			new ArrayList<>(Arrays.asList(umfragen.get(5), umfragen.get(1)));
		
		assertThat(result).isEqualTo(erwartet);
	}
	
}
