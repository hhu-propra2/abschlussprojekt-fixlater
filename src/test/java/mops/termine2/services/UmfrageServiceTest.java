package mops.termine2.services;

import mops.termine2.Konstanten;
import mops.termine2.authentication.Account;
import mops.termine2.database.UmfrageAntwortRepository;
import mops.termine2.database.UmfrageRepository;
import mops.termine2.database.entities.UmfrageDB;
import mops.termine2.enums.Modus;
import mops.termine2.models.Umfrage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UmfrageServiceTest {
	
	private static final String[] TITEL = {"Toller Titel", "Besserer Titel", "Super Titel"};
	
	private static final long MAXANTWORT = 13L;
	
	private static final LocalDateTime LOESCHDATUM = LocalDateTime.of(1, 3, 1, 1, 1, 1, 1);
	
	private static final String[] LINK = {"BruderJakob", "AlleMeineEntchen", "BieneMaya"};
	
	private static final String[] GRUPPE = {"1", "2", "3"};
	
	private static final LocalDateTime FRIST = LocalDateTime.of(1, 1, 1, 1, 1, 1, 1);
	
	private static final String[] ERSTELLER = {"Me", "You", "He"};
	
	private static final String[] BESCHREIBUNG = {"Tolle Beschreibung",
		"Bessere Beschreibung",
		"Super Beschreibung"};
	
	private transient UmfrageService service;
	
	private transient UmfrageRepository umfrageRepository;
	
	private transient UmfrageAntwortRepository umfrageAntwortRepository;
	
	@BeforeEach
	public void setUp() {
		umfrageRepository = mock(UmfrageRepository.class);
		umfrageAntwortRepository = mock(UmfrageAntwortRepository.class);
		service = new UmfrageService(umfrageRepository, umfrageAntwortRepository);
	}
	
	@Test
	public void saveEineUmfrageMitDreiVorschlaegen() {
		int anzahl = 3;
		Umfrage umfrage = erstelleBeispielUmfrage(anzahl, 0, 0, 0, 0, 0);
		service.save(umfrage);
		Mockito.verify(umfrageRepository, times(1)).saveAll(any());
	}
	
	@Test
	public void updateUmfrage() {
		Umfrage umfrage = erstelleBeispielUmfrage(4, 0, 0, 0, 0, 0);
		List<UmfrageDB> db = erstelleUmfrageDBListeGruppe(5, 0, 0, 0, 0, 0);
		List<UmfrageDB> expectedToSave = db.subList(0, 4);
		List<UmfrageDB> expectedToDelete = Arrays.asList(db.get(4));
		
		when(umfrageRepository.findByLink(umfrage.getLink())).thenReturn(db);
		
		service.save(umfrage);
		
		verify(umfrageRepository, times(1)).saveAll(expectedToSave);
		verify(umfrageRepository, times(1)).deleteAll(expectedToDelete);
	}
	
	@Test
	public void loadUmfrageByLinkMitDreiVorschlaegen() {
		int anzahl = 3;
		List<UmfrageDB> umfrageDBs = erstelleUmfrageDBListeGruppe(anzahl, 0, 0, 0, 0, 0);
		when(umfrageRepository.findByLink(LINK[0])).thenReturn(umfrageDBs);
		Umfrage erwartet = erstelleBeispielUmfrage(anzahl, 0, 0, 0, 0, 0);
		
		Umfrage ergebnis = service.loadByLink(LINK[0]);
		
		assertThat(ergebnis).isEqualTo(erwartet);
	}
	
	@Test
	public void loadUmfrageByLinkNichtExistent() {
		List<UmfrageDB> umfrageDBs = new ArrayList<UmfrageDB>();
		when(umfrageRepository.findByLink(LINK[0])).thenReturn(umfrageDBs);
		
		Umfrage ergebnis = service.loadByLink(LINK[0]);
		
		assertThat(ergebnis).isEqualTo(null);
	}
	
	@Test
	public void loadUmfrageByLinkNull() {
		when(umfrageRepository.findByLink(LINK[0])).thenReturn(null);
		
		Umfrage ergebnis = service.loadByLink(LINK[0]);
		
		assertThat(ergebnis).isEqualTo(null);
	}
	
	@Test
	public void loadUmfragenByErstellerEineUmfrageKeineVorschlaege() {
		int anzahl = 3;
		List<UmfrageDB> umfrageDBs = erstelleUmfrageDBListeGruppe(anzahl, 0, 0, 0, 0, 0);
		when(umfrageRepository.findByErstellerOrderByFristAsc(ERSTELLER[0])).thenReturn(umfrageDBs);
		Umfrage erwartet = erstelleBeispielUmfrage(anzahl, 0, 0, 0, 0, 0);
		erwartet.setVorschlaege(new ArrayList<String>());
		
		Umfrage ergebnis = service.loadByErstellerOhneVorschlaege(ERSTELLER[0]).get(0);
		
		assertThat(ergebnis).isEqualTo(erwartet);
	}
	
	@Test
	public void loadUmfragenByErstellerZweiUmfragenEinErsteller() {
		List<UmfrageDB> umfrageDBs1 = erstelleUmfrageDBListeGruppe(3, 0, 0, 0, 0, 0);
		List<UmfrageDB> umfrageDBs2 = erstelleUmfrageDBListeGruppe(3, 1, 0, 1, 1, 1);
		List<UmfrageDB> umfrageDBs = new ArrayList<UmfrageDB>(umfrageDBs1);
		for (UmfrageDB db : umfrageDBs2) {
			umfrageDBs.add(db);
		}
		when(umfrageRepository.findByErstellerOrderByFristAsc(ERSTELLER[0])).thenReturn(umfrageDBs);
		Umfrage erwartet1 = erstelleBeispielUmfrage(3, 0, 0, 0, 0, 0);
		erwartet1.setVorschlaege(new ArrayList<String>());
		Umfrage erwartet2 = erstelleBeispielUmfrage(3, 1, 0, 1, 1, 1);
		erwartet2.setVorschlaege(new ArrayList<String>());
		
		Umfrage ergebnis1 = service.loadByErstellerOhneVorschlaege(ERSTELLER[0]).get(0);
		Umfrage ergebnis2 = service.loadByErstellerOhneVorschlaege(ERSTELLER[0]).get(1);
		
		assertThat(erwartet1).isEqualTo(ergebnis1);
		assertThat(erwartet2).isEqualTo(ergebnis2);
	}
	
	@Test
	public void loadUmfragenByGruppeEineUmfrageKeineVorschlaege() {
		int anzahl = 3;
		List<UmfrageDB> umfrageDBs = erstelleUmfrageDBListeGruppe(anzahl, 0, 0, 0, 0, 0);
		when(umfrageRepository.findByGruppeIdOrderByFristAsc(GRUPPE[0])).thenReturn(umfrageDBs);
		Umfrage erwartet = erstelleBeispielUmfrage(anzahl, 0, 0, 0, 0, 0);
		erwartet.setVorschlaege(new ArrayList<String>());
		
		Umfrage ergebnis = service.loadByGruppeOhneVorschlaege(GRUPPE[0]).get(0);
		
		assertThat(ergebnis).isEqualTo(erwartet);
	}
	
	@Test
	public void loadUmfragenByGruppeZweiUmfragenEineGruppe() {
		List<UmfrageDB> umfrageDBs1 = erstelleUmfrageDBListeGruppe(3, 0, 0, 0, 0, 0);
		List<UmfrageDB> umfrageDBs2 = erstelleUmfrageDBListeGruppe(3, 1, 1, 0, 1, 1);
		List<UmfrageDB> umfrageDBs = new ArrayList<UmfrageDB>(umfrageDBs1);
		for (UmfrageDB db : umfrageDBs2) {
			umfrageDBs.add(db);
		}
		when(umfrageRepository.findByGruppeIdOrderByFristAsc(GRUPPE[0])).thenReturn(umfrageDBs);
		Umfrage erwartet1 = erstelleBeispielUmfrage(3, 0, 0, 0, 0, 0);
		erwartet1.setVorschlaege(new ArrayList<String>());
		Umfrage erwartet2 = erstelleBeispielUmfrage(3, 1, 1, 0, 1, 1);
		erwartet2.setVorschlaege(new ArrayList<String>());
		
		Umfrage ergebnis1 = service.loadByGruppeOhneVorschlaege(GRUPPE[0]).get(0);
		Umfrage ergebnis2 = service.loadByGruppeOhneVorschlaege(GRUPPE[0]).get(1);
		
		assertThat(erwartet1).isEqualTo(ergebnis1);
		assertThat(erwartet2).isEqualTo(ergebnis2);
	}
	
	@Test
	public void loadByBenutzer() {
		String benutzer = "benutzer";
		List<UmfrageDB> umfrageDBs = new ArrayList<>();
		List<UmfrageDB> umfrageDBs1;
		List<UmfrageDB> umfrageDBs2;
		umfrageDBs1 = erstelleUmfrageDBListeGruppe(0, 0, 0, 0, 0, 0);
		umfrageDBs2 = erstelleUmfrageDBListeGruppe(0, 1, 1, 1, 1, 1);
		umfrageDBs.addAll(umfrageDBs1);
		umfrageDBs.addAll(umfrageDBs2);
		
		when(umfrageAntwortRepository.findUmfrageDbByBenutzer(benutzer)).thenReturn(umfrageDBs);
		
		List<Umfrage> erwartet = new ArrayList<>(
			Arrays.asList(erstelleBeispielUmfrage(0, 0, 0, 0, 0, 0),
				erstelleBeispielUmfrage(0, 1, 1, 1, 1, 1)));
		
		List<Umfrage> ergebnis = service.loadAllBenutzerHatAbgestimmtOhneVorschlaege(benutzer);
		
		assertThat(ergebnis).isEqualTo(erwartet);
	}
	
	@Test
	public void loescheAbgelaufene() {
		service.loescheAbgelaufeneUmfragen();
		
		Mockito.verify(umfrageAntwortRepository, times(1)).deleteByUmfrageLoeschdatumBefore(any());
		Mockito.verify(umfrageRepository, times(1)).deleteByLoeschdatumBefore(any());
	}
	
	@Test
	public void loescheVorschlag() {
		Umfrage umfrage = erstelleBeispielUmfrage(5, 0, 0, 0, 0, 0);
		List<String> expected = Arrays.asList(
				umfrage.getVorschlaege().get(1),
				umfrage.getVorschlaege().get(2),
				umfrage.getVorschlaege().get(3),
				umfrage.getVorschlaege().get(4)
			);
		
		service.loescheVorschlag(umfrage, 0);
		
		assertThat(umfrage.getVorschlaege()).isEqualTo(expected);
	}
	
	@Test
	public void loescheVorschlagNull() {
		Umfrage umfrage = erstelleBeispielUmfrage(5, 0, 0, 0, 0, 0);
		umfrage.setVorschlaege(null);
		
		service.loescheVorschlag(umfrage, 0);
		
		assertThat(umfrage.getVorschlaege()).isNull();
	}
	
	@Test
	public void loescheVorschlagOutOfBounds() {
		Umfrage umfrage = erstelleBeispielUmfrage(5, 0, 0, 0, 0, 0);
		List<String> expected = Arrays.asList(
				umfrage.getVorschlaege().get(0),
				umfrage.getVorschlaege().get(1),
				umfrage.getVorschlaege().get(2),
				umfrage.getVorschlaege().get(3),
				umfrage.getVorschlaege().get(4)
			);
		
		service.loescheVorschlag(umfrage, 5);
		
		assertThat(umfrage.getVorschlaege()).isEqualTo(expected);
	}
	
	@Test
	public void setzeGruppenname() {
		Umfrage u1 = new Umfrage();
		u1.setGruppeId("1");
		Umfrage u2 = new Umfrage();
		u2.setGruppeId("3");
		List<Umfrage> umfragen = Arrays.asList(u1, u2);
		
		HashMap<String, String> gruppen = new HashMap<>();
		gruppen.put("1", "Gruppe1");
		gruppen.put("2", "Gruppe2");
		gruppen.put("3", "Gruppe3");
		gruppen.put("4", "Gruppe4");
		
		service.setzeGruppenName(umfragen, gruppen);

		assertThat(u1.getGruppeName()).isEqualTo("Gruppe1");
		assertThat(u2.getGruppeName()).isEqualTo("Gruppe3");
	}
	
	@Test
	public void erstelleUmfrageOhneFehler() {
		Account user = new Account("user", null, null, null);
		Umfrage umfrage = erstelleBeispielUmfrage(3, 0, 0, 0, 0, 0);
		umfrage.setVorschlaege(Arrays.asList(
				"1", "2", "1", null, ""
			));	
		List<String> expectedVorschlaege = Arrays.asList(
			 	"1", "2"
			);
		
		List<String> result = service.erstelleUmfrage(user, umfrage);
		
		assertThat(result).isEqualTo(new ArrayList<String>());
		assertThat(umfrage.getErsteller()).isEqualTo("user");
		assertThat(umfrage.getVorschlaege()).isEqualTo(expectedVorschlaege);
	}
	
	@Test
	public void erstelleUmfrageKeineVorschlaege() {
		Account user = new Account("user", null, null, null);
		Umfrage umfrage = erstelleBeispielUmfrage(3, 0, 0, 0, 0, 0);
		umfrage.setVorschlaege(null);
		
		List<String> expectedVorschlaege = new ArrayList<String>();
		expectedVorschlaege.add("");
		
		List<String> result = service.erstelleUmfrage(user, umfrage);
		
		assertThat(result).isEqualTo(Arrays.asList(Konstanten.MESSAGE_KEIN_VORSCHLAG));
		assertThat(umfrage.getErsteller()).isEqualTo("user");
		assertThat(umfrage.getVorschlaege()).isEqualTo(expectedVorschlaege);
	}
	
	private List<UmfrageDB> erstelleUmfrageDBListeGruppe(
		int anzahl, int bIndex, int eIndex, int gIndex, int lIndex, int tIndex) {
		List<UmfrageDB> umfrageDBs = new ArrayList<UmfrageDB>();
		if (anzahl != 0) {
			List<String> vorschlaege = erstelleVorschlaege(anzahl);
			for (String s : vorschlaege) {
				UmfrageDB umfrageDB = new UmfrageDB();
				umfrageDB.setAuswahlmoeglichkeit(s);
				umfrageDB.setBeschreibung(BESCHREIBUNG[bIndex]);
				umfrageDB.setErsteller(ERSTELLER[eIndex]);
				umfrageDB.setFrist(FRIST);
				umfrageDB.setGruppeId(GRUPPE[gIndex]);
				umfrageDB.setLink(LINK[lIndex]);
				umfrageDB.setLoeschdatum(LOESCHDATUM);
				umfrageDB.setMaxAntwortAnzahl(13L);
				umfrageDB.setModus(Modus.GRUPPE);
				umfrageDB.setTitel(TITEL[tIndex]);
				umfrageDBs.add(umfrageDB);
			}
		} else {
			UmfrageDB umfrageDB = new UmfrageDB();
			umfrageDB.setBeschreibung(BESCHREIBUNG[bIndex]);
			umfrageDB.setErsteller(ERSTELLER[eIndex]);
			umfrageDB.setFrist(FRIST);
			umfrageDB.setGruppeId(GRUPPE[gIndex]);
			umfrageDB.setLink(LINK[lIndex]);
			umfrageDB.setLoeschdatum(LOESCHDATUM);
			umfrageDB.setMaxAntwortAnzahl(13L);
			umfrageDB.setModus(Modus.GRUPPE);
			umfrageDB.setTitel(TITEL[tIndex]);
			umfrageDBs.add(umfrageDB);
		}
		return umfrageDBs;
		
	}
	
	private Umfrage erstelleBeispielUmfrage(
		int anzahl, int bIndex, int eIndex, int gIndex, int lIndex, int tIndex) {
		Umfrage umfrage = new Umfrage();
		umfrage.setBeschreibung(BESCHREIBUNG[bIndex]);
		umfrage.setErsteller(ERSTELLER[eIndex]);
		umfrage.setFrist(FRIST);
		umfrage.setGruppeId(GRUPPE[gIndex]);
		umfrage.setLink(LINK[lIndex]);
		umfrage.setLoeschdatum(LOESCHDATUM);
		umfrage.setMaxAntwortAnzahl(MAXANTWORT);
		umfrage.setTitel(TITEL[tIndex]);
		List<String> vorschlaege = erstelleVorschlaege(anzahl);
		umfrage.setVorschlaege(vorschlaege);
		return umfrage;
		
	}
	
	private List<String> erstelleVorschlaege(int anzahl) {
		List<String> vorschlaege = new ArrayList<>();
		for (int i = 0; i < anzahl; i++) {
			vorschlaege.add("Vorschlag " + i);
		}
		return vorschlaege;
		
	}
	
}
