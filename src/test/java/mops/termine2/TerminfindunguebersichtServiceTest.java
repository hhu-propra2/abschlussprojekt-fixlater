package mops.termine2;

import mops.termine2.authentication.Account;
import mops.termine2.database.BenutzerGruppeRepository;
import mops.termine2.database.TerminfindungRepository;
import mops.termine2.database.entities.BenutzerGruppeDB;
import mops.termine2.database.entities.TerminfindungDB;
import mops.termine2.models.Terminfindung;
import mops.termine2.services.GruppeService;
import mops.termine2.services.TerminfindungService;
import mops.termine2.services.TerminfindunguebersichtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TerminfindunguebersichtServiceTest {
	
	private transient TerminfindunguebersichtService terminfindunguebersichtService;
	
	private transient TerminfindungService terminfindungService;
	
	private transient GruppeService gruppeService;
	
	private transient TerminfindungRepository terminfindungRepository;
	
	private transient BenutzerGruppeRepository benutzerGruppeRepository;
	
	@BeforeEach
	public void setUp() {
		terminfindungRepository = mock(TerminfindungRepository.class);
		terminfindungService = new TerminfindungService(terminfindungRepository);
		
		benutzerGruppeRepository = mock(BenutzerGruppeRepository.class);
		gruppeService = new GruppeService(benutzerGruppeRepository);
		
		terminfindunguebersichtService = new TerminfindunguebersichtService(
			terminfindungService, gruppeService);
	}
	
	@Test
	public void testLoadOffeneTerminfindungenFuerBenutzer() {
		List<Integer> days = new ArrayList<>(Arrays.asList(5, -5, 1, -2));
		Account account = new Account("studentin", null, null, null);
		LocalDateTime ldt = LocalDateTime.now();
		
		BenutzerGruppeDB gruppe = new BenutzerGruppeDB();
		gruppe.setGruppe("Gruppe");
		
		List<Terminfindung> terminfindungen = new ArrayList<>();
		List<TerminfindungDB> terminfindungenDB = new ArrayList<>();
		
		for (Integer day : days) {
			Terminfindung termin = new Terminfindung();
			termin.setLink(day.toString());
			termin.setFrist(ldt.plusDays(day));
			terminfindungen.add(termin);
			
			TerminfindungDB terminDB = new TerminfindungDB();
			terminDB.setLink(day.toString());
			terminDB.setFrist(ldt.plusDays(day));
			terminfindungenDB.add(terminDB);
		}
		
		when(benutzerGruppeRepository.findByBenutzer(account.getName())).thenReturn(
			new ArrayList<>(Arrays.asList(gruppe)));
		when(terminfindungRepository.findByGruppe(gruppe.getGruppe())).thenReturn(terminfindungenDB);
		
		List<Terminfindung> ergebnis =
			terminfindunguebersichtService.loadOffeneTerminfindungenFuerBenutzer(account);
		List<Terminfindung> erwartet =
			new ArrayList<>(Arrays.asList(terminfindungen.get(0), terminfindungen.get(2)));
		
		assertThat(ergebnis).isEqualTo(erwartet);
	}
	
	@Test
	public void testLoadAbgeschlosseneTerminfindungenFuerBenutzer() {
		List<Integer> days = new ArrayList<>(Arrays.asList(5, -5, 1, -2));
		Account account = new Account("studentin", null, null, null);
		LocalDateTime ldt = LocalDateTime.now();
		
		BenutzerGruppeDB gruppe = new BenutzerGruppeDB();
		gruppe.setGruppe("Gruppe");
		
		List<Terminfindung> terminfindungen = new ArrayList<>();
		List<TerminfindungDB> terminfindungenDB = new ArrayList<>();
		
		for (Integer day : days) {
			Terminfindung termin = new Terminfindung();
			termin.setLink(day.toString());
			termin.setFrist(ldt.plusDays(day));
			terminfindungen.add(termin);
			
			TerminfindungDB terminDB = new TerminfindungDB();
			terminDB.setLink(day.toString());
			terminDB.setFrist(ldt.plusDays(day));
			terminfindungenDB.add(terminDB);
		}
		
		when(benutzerGruppeRepository.findByBenutzer(account.getName())).thenReturn(
			new ArrayList<>(Arrays.asList(gruppe)));
		when(terminfindungRepository.findByGruppe(gruppe.getGruppe())).thenReturn(terminfindungenDB);
		
		List<Terminfindung> ergebnis =
			terminfindunguebersichtService.loadAbgeschlosseneTerminfindungenFuerBenutzer(account);
		List<Terminfindung> erwartet =
			new ArrayList<>(Arrays.asList(terminfindungen.get(1), terminfindungen.get(3)));
		
		assertThat(ergebnis).isEqualTo(erwartet);
	}
	
}
