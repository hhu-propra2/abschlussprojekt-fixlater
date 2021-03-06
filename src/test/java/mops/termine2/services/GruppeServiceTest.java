package mops.termine2.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mops.termine2.authentication.Account;
import mops.termine2.database.BenutzerGruppeRepository;
import mops.termine2.database.entities.BenutzerGruppeDB;
import mops.termine2.models.Gruppe;
import mops.termine2.models.Terminfindung;
import mops.termine2.models.Umfrage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GruppeServiceTest {
	
	private transient GruppeService gruppeService;
	
	private transient BenutzerGruppeRepository benutzerGruppeRepository;
	
	@BeforeEach
	public void setUp() {
		benutzerGruppeRepository = mock(BenutzerGruppeRepository.class);
		gruppeService = new GruppeService(benutzerGruppeRepository);
	}
	
	@Test
	public void testLoadByBenutzer() {
		Account account = new Account("studentin", "abc@def.de", null, null);
		List<String> groupNames = new ArrayList<>(Arrays.asList("Best group eva", "FIXME", "Last One :("));
		
		List<BenutzerGruppeDB> gruppenDB = new ArrayList<>();
		List<Gruppe> gruppen = new ArrayList<>();
		for (String name : groupNames) {
			BenutzerGruppeDB gruppeDB = new BenutzerGruppeDB();
			gruppeDB.setGruppe(name);
			gruppeDB.setGruppeId("1");
			gruppenDB.add(gruppeDB);
			
			Gruppe gruppe = new Gruppe();
			gruppe.setName(name);
			gruppe.setId("1");
			gruppen.add(gruppe);
		}
		
		when(benutzerGruppeRepository.findByBenutzer(account.getName())).thenReturn(gruppenDB);
		
		List<Gruppe> ergebnis = gruppeService.loadByBenutzer(account);
		
		assertThat(ergebnis).isEqualTo(gruppen);
	}
	
	@Test
	public void testLoadById() {
		Gruppe expected = new Gruppe();
		expected.setId("1");
		expected.setName("Test");
		
		BenutzerGruppeDB user = new BenutzerGruppeDB();
		user.setBenutzer("Hallo");
		user.setGruppe("Test");
		user.setGruppeId("1");
		user.setId(1L);
		
		when(benutzerGruppeRepository.findByGruppeId("1"))
			.thenReturn(new ArrayList<BenutzerGruppeDB>(Arrays.asList(user)));
		
		Gruppe result = gruppeService.loadByGruppeId("1");
		
		assertThat(result).isEqualTo(expected);
	}
	
	@Test
	public void testLoadByIdNoGroup() {				
		when(benutzerGruppeRepository.findByGruppeId("1"))
			.thenReturn(new ArrayList<BenutzerGruppeDB>());
		
		Gruppe result = gruppeService.loadByGruppeId("1");
		
		assertThat(result).isNull();
	}
	
	@Test
	public void testAccountInGruppe() {
		Account account = new Account("studentin", "abc@def.de", null, null);
		BenutzerGruppeDB user = new BenutzerGruppeDB();
		user.setBenutzer("studentin");
		user.setGruppe("Test");
		user.setGruppeId("1");
		user.setId(1L);
		
		when(benutzerGruppeRepository.findByBenutzerAndGruppeId("studentin", "1"))
			.thenReturn(user);
		
		boolean result = gruppeService.pruefeAccountInGruppe(account, "1");
		
		assertThat(result).isEqualTo(true);
	}
	
	@Test
	public void testAccountNotInGruppe() {
		Account account = new Account("studentin", "abc@def.de", null, null);		
		when(benutzerGruppeRepository.findByBenutzerAndGruppeId("studentin", "1"))
			.thenReturn(null);
		
		boolean result = gruppeService.pruefeAccountInGruppe(account, "1");
		
		assertThat(result).isEqualTo(false);
	}
	
	@Test
	public void testSortedGroupnames() {
		Gruppe g1 = new Gruppe();
		g1.setId("1");
		g1.setName("a");
		Gruppe g2 = new Gruppe();
		g2.setId("2");
		g2.setName("b");
		List<Gruppe> input = new ArrayList<Gruppe>(
			Arrays.asList(
				g2, g1
			));
		
		List<Gruppe> expected = new ArrayList<Gruppe>(
			Arrays.asList(
				g1, g2
			));
		
		List<Gruppe> result = gruppeService.sortiereGruppenNachNamen(input);
		
		assertThat(result).isEqualTo(expected);		
	}
	
	@Test
	public void testAccessDeniedNull() {
		Account account = new Account("studentin", "abc@def.de", null, null);
		boolean result = gruppeService.pruefeGruppenzugriffVerweigert(account, null);
		assertThat(result).isEqualTo(false);
	}
	
	@Test
	public void testAccessDeniedMinus1() {
		Account account = new Account("studentin", "abc@def.de", null, null);
		boolean result = gruppeService.pruefeGruppenzugriffVerweigert(account, "-1");
		assertThat(result).isEqualTo(false);
	}
	
	@Test
	public void testAccessDenied() {
		Account account = new Account("studentin", "abc@def.de", null, null);		
		when(benutzerGruppeRepository.findByBenutzerAndGruppeId("studentin", "1"))
			.thenReturn(null);
		
		boolean result = gruppeService.pruefeGruppenzugriffVerweigert(account, "1");
		
		assertThat(result).isEqualTo(true);
	}
	
	@Test
	public void testAccessNotDenied() {
		Account account = new Account("studentin", "abc@def.de", null, null);
		BenutzerGruppeDB user = new BenutzerGruppeDB();
		user.setBenutzer("studentin");
		user.setGruppe("Test");
		user.setGruppeId("1");
		user.setId(1L);
		when(benutzerGruppeRepository.findByBenutzerAndGruppeId("studentin", "1"))
			.thenReturn(user);
		
		boolean result = gruppeService.pruefeGruppenzugriffVerweigert(account, "1");
		
		assertThat(result).isEqualTo(false);
	}
	
	@Test
	public void testExtrahiereNameUndId() {
		List<Gruppe> gruppen = new ArrayList<Gruppe>();
		for (int i = 1; i < 5; i++) {
			Gruppe gruppe = new Gruppe();
			gruppe.setId(String.valueOf(i));
			gruppe.setName("Gruppe" + i);
			gruppen.add(gruppe);
		}
		HashMap<String, String> expected = new HashMap<>();
		expected.put("1", "Gruppe1");
		expected.put("2", "Gruppe2");
		expected.put("3", "Gruppe3");
		expected.put("4", "Gruppe4");
		
		HashMap<String, String> result = gruppeService.extrahiereIdUndNameAusGruppen(gruppen);
		
		assertThat(result).isEqualTo(expected);
	}
	
	@Test
	public void setzeGruppenIDTerminfindung() {
		Terminfindung terminfindung = new Terminfindung();
		Gruppe gruppe = new Gruppe();
		gruppe.setId("1");
		gruppe.setName("Test");
		
		BenutzerGruppeDB user = new BenutzerGruppeDB();
		user.setBenutzer("studentin");
		user.setGruppe("Test");
		user.setGruppeId("1");
		user.setId(1L);
		
		when(benutzerGruppeRepository.findByGruppeId(gruppe.getId()))
			.thenReturn(Arrays.asList(user));
		
		gruppeService.setzeGruppeId(terminfindung, gruppe);
		
		assertThat(terminfindung.getGruppeId()).isEqualTo(gruppe.getId());
	}
	
	@Test
	public void setzeGruppenIDTerminfindungNichtInDB() {
		Terminfindung terminfindung = new Terminfindung();
		terminfindung.setGruppeId("0");
		Gruppe gruppe = new Gruppe();
		gruppe.setId("1");
		gruppe.setName("Test");
		
		when(benutzerGruppeRepository.findByGruppeId(gruppe.getId()))
			.thenReturn(new ArrayList<BenutzerGruppeDB>());
		
		gruppeService.setzeGruppeId(terminfindung, gruppe);
		
		assertThat(terminfindung.getGruppeId()).isEqualTo("0");
	}
	
	@Test
	public void setzeGruppenIDTerminfindungIDNull() {
		Terminfindung terminfindung = new Terminfindung();
		terminfindung.setGruppeId("0");
		Gruppe gruppe = new Gruppe();		
		
		gruppeService.setzeGruppeId(terminfindung, gruppe);
		
		assertThat(terminfindung.getGruppeId()).isEqualTo("0");
	}
	
	@Test
	public void setzeGruppenIDTerminfindungGruppeNull() {
		Terminfindung terminfindung = new Terminfindung();
		terminfindung.setGruppeId("0");
		
		gruppeService.setzeGruppeId(terminfindung, null);
		
		assertThat(terminfindung.getGruppeId()).isEqualTo("0");
	}
	
	@Test
	public void setzeGruppenIDUmfrage() {
		Umfrage umfrage = new Umfrage();
		Gruppe gruppe = new Gruppe();
		gruppe.setId("1");
		gruppe.setName("Test");
		
		BenutzerGruppeDB user = new BenutzerGruppeDB();
		user.setBenutzer("studentin");
		user.setGruppe("Test");
		user.setGruppeId("1");
		user.setId(1L);
		
		when(benutzerGruppeRepository.findByGruppeId(gruppe.getId()))
			.thenReturn(Arrays.asList(user));
		
		gruppeService.setzeGruppeId(umfrage, gruppe);
		
		assertThat(umfrage.getGruppeId()).isEqualTo(gruppe.getId());
	}
	
	@Test
	public void setzeGruppenIDUmfrageNichtInDB() {
		Umfrage umfrage = new Umfrage();
		umfrage.setGruppeId("0");
		Gruppe gruppe = new Gruppe();
		gruppe.setId("1");
		gruppe.setName("Test");
		
		when(benutzerGruppeRepository.findByGruppeId(gruppe.getId()))
			.thenReturn(new ArrayList<BenutzerGruppeDB>());
		
		gruppeService.setzeGruppeId(umfrage, gruppe);
		
		assertThat(umfrage.getGruppeId()).isEqualTo("0");
	}
	
}
