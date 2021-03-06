package mops.termine2.services;

import mops.termine2.database.TerminfindungAntwortRepository;
import mops.termine2.database.TerminfindungRepository;
import mops.termine2.database.entities.TerminfindungAntwortDB;
import mops.termine2.database.entities.TerminfindungDB;
import mops.termine2.enums.Antwort;
import mops.termine2.models.Terminfindung;
import mops.termine2.models.TerminfindungAntwort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * Der TerminfindungAntwortService bietet Methoden zur Abstimmung in einer Terminfindung
 * und bildet die dementsprechende Schnittstelle zwischen Datenbank und Controller
 */
@Service
public class TerminfindungAntwortService {
	
	private TerminfindungAntwortRepository antwortRepo;
	
	private TerminfindungRepository terminRepo;
	
	public TerminfindungAntwortService(TerminfindungAntwortRepository terminfindungAntwortRepository,
								TerminfindungRepository terminfindungRepository) {
		antwortRepo = terminfindungAntwortRepository;
		terminRepo = terminfindungRepository;
	}
	
	/**
	 * Speichert Antworten zu einer Terminabstimmung in der Datenbank
	 *
	 * @param antwort Die Antwort des Benutzers für die Abstimmung 
	 * @param terminfindung Die Terminfindung, bei der abgestimmt wurde
	 */
	public void abstimmen(TerminfindungAntwort antwort, Terminfindung terminfindung) {
		
		List<TerminfindungAntwortDB> antwortenToDelete =
			antwortRepo.findByBenutzerAndTerminfindungLink(antwort.getKuerzel(),
				terminfindung.getLink());
		
		antwortRepo.deleteAll(antwortenToDelete);
		for (LocalDateTime termin : terminfindung.getVorschlaege()) {
			TerminfindungAntwortDB db = new TerminfindungAntwortDB();
			TerminfindungDB terminfindungDB = terminRepo.findByLinkAndTermin(terminfindung.getLink(),
				termin);
			
			db.setAntwort(antwort.getAntworten().get(termin));
			db.setBenutzer(antwort.getKuerzel());
			db.setPseudonym(antwort.getPseudonym());
			db.setTerminfindung(terminfindungDB);
			if (terminfindungDB != null) {
				antwortRepo.save(db);
			}
		}
	}
	
	/**
	 * Prüft, ob der Benutzer bei der entsprechenden Terminfindung abgestimmt hat
	 * 
	 * @param benutzer Der Benutzer, dessen Abstimmungsstatus abgefragt werden soll
	 * @param link Der Link zu der Terminfindung
	 * 
	 * @return {@code true}, falls der Benutzer bereits bei der Terminfindung abgestimmt hat, 
	 * ansonsten {@code false}
	 */
	public boolean hatNutzerAbgestimmt(String benutzer, String link) {
		List<TerminfindungAntwortDB> antworten =
			antwortRepo.findByBenutzerAndTerminfindungLink(benutzer, link);
		return !antworten.isEmpty();
	}
	
	/**
	 * Löscht alle Antworten der Terminfindung mit Link {@code link}
	 *
	 * @param link Der Link zu der Terminfindung
	 */
	public void deleteAllByLink(String link) {
		antwortRepo.deleteByTerminfindungLink(link);
	}
	
	/**
	 * Lädt die Antwort eines Benutzers {@code benutzer} zu einer Terminfindung
	 * mit Link {@code link}
	 *
	 * @param benutzer der Benutzer, dessen Antwort gesucht wird
	 * @param link der Link der Terminfindung
	 * 
	 * @return die Antwort des Benutzers für die Terminfindung
	 */	
	public TerminfindungAntwort loadByBenutzerUndLink(String benutzer, String link) {		
		List<TerminfindungAntwortDB> alteAntwort =
			antwortRepo.findByBenutzerAndTerminfindungLink(benutzer, link);
		List<TerminfindungDB> antwortMoeglichkeiten = terminRepo.findByLink(link);
		
		return baueAntwortFuerBenutzer(benutzer, alteAntwort,
			antwortMoeglichkeiten);
	}
	
	/**
	 * Lädt alle Antworten, die zu der Terminfindung mit Link {@code link} gehören
	 *
	 * @param link der Link der Terminfindung
	 * 
	 * @return Liste von Antworten zu der Terminfindung
	 */
	public List<TerminfindungAntwort> loadAllByLink(String link) {
		List<TerminfindungAntwortDB> terminfindungAntwortDBList =
			antwortRepo.findAllByTerminfindungLink(link);
		List<TerminfindungDB> antwortMoeglichkeiten = terminRepo.findByLink(link);
		
		return baueAntworten(terminfindungAntwortDBList,
			antwortMoeglichkeiten);
	}
	
	private List<TerminfindungAntwort> baueAntworten(
		List<TerminfindungAntwortDB> antwortDBS, List<TerminfindungDB> antwortMoeglichkeiten) {
		
		List<TerminfindungAntwort> terminAntworten = new ArrayList<>();
		if (!antwortDBS.isEmpty()) {
			List<String> benuternamen = new ArrayList<>();
			
			for (TerminfindungAntwortDB antwortDB : antwortDBS) {
				String benutzer = antwortDB.getBenutzer();
				if (!benuternamen.contains(antwortDB.getBenutzer())) {
					List<TerminfindungAntwortDB> nutzerAntworten = filtereAntwortenNachBenutzer(
						antwortDBS, benutzer);
					terminAntworten.add(baueAntwortFuerBenutzer(
						benutzer, nutzerAntworten, antwortMoeglichkeiten));
					benuternamen.add(benutzer);
				}				
			}
		}
		return terminAntworten;		
	}
	
	private TerminfindungAntwort baueAntwortFuerBenutzer(
		String benutzer, List<TerminfindungAntwortDB> alteAntworten,
		List<TerminfindungDB> antwortMoglichkeiten) {
		
		TerminfindungAntwort antwort = new TerminfindungAntwort();
		antwort.setKuerzel(benutzer);
		antwort.setLink(antwortMoglichkeiten.get(0).getLink());
		if (!alteAntworten.isEmpty()) {
			antwort.setPseudonym(alteAntworten.get(0).getPseudonym());
		} else {
			antwort.setPseudonym(benutzer);
		}
		
		LinkedHashMap<LocalDateTime, Antwort> alteAntwortenMap = new LinkedHashMap<>();
		for (TerminfindungAntwortDB alteAntwort : alteAntworten) {
			alteAntwortenMap.put(alteAntwort.getTerminfindung().getTermin(), alteAntwort.getAntwort());
		}
		LinkedHashMap<LocalDateTime, Antwort> antwortenMap = new LinkedHashMap<>();
		
		for (TerminfindungDB antwortMoglichkeit : antwortMoglichkeiten) {
			LocalDateTime termin = antwortMoglichkeit.getTermin();
			Antwort alteAntwort = alteAntwortenMap.get(termin);
			antwortenMap.put(termin, Objects.requireNonNullElse(alteAntwort, Antwort.VIELLEICHT));
		}
		
		antwort.setAntworten(antwortenMap);
		return antwort;
	}	
	
	private List<TerminfindungAntwortDB> filtereAntwortenNachBenutzer(
		List<TerminfindungAntwortDB> antwortDBS, String benutzer) {
		List<TerminfindungAntwortDB> nutzerAntwortenDB = new ArrayList<>();
		for (TerminfindungAntwortDB terminAntwortDB : antwortDBS) {
			if (benutzer.equals(terminAntwortDB.getBenutzer())) {
				nutzerAntwortenDB.add(terminAntwortDB);
			}
		}
		return nutzerAntwortenDB;
	}
}
