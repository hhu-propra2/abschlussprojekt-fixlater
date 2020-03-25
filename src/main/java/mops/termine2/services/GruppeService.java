package mops.termine2.services;

import mops.termine2.authentication.Account;
import mops.termine2.database.BenutzerGruppeRepository;
import mops.termine2.database.entities.BenutzerGruppeDB;
import mops.termine2.models.Gruppe;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GruppeService {
	
	private transient BenutzerGruppeRepository benutzerGruppeRepository;
	
	public GruppeService(BenutzerGruppeRepository benutzerGruppeRepository) {
		this.benutzerGruppeRepository = benutzerGruppeRepository;
	}
	
	/**
	 * Bekommt den Account übergeben und lädt die zugehörigen Gruppen
	 *
	 * @param account
	 * @return Liste von Gruppen
	 */
	public List<Gruppe> loadByBenutzer(Account account) {
		List<BenutzerGruppeDB> gruppenDB = benutzerGruppeRepository.findByBenutzer(account.getName());
		List<Gruppe> gruppen = new ArrayList<>();
		
		for (BenutzerGruppeDB gruppeDB : gruppenDB) {
			Gruppe g = new Gruppe();
			g.setId(gruppeDB.getGruppeId());
			g.setName(gruppeDB.getGruppe());
			gruppen.add(g);
		}
		
		return gruppen;
	}
	
	/**
	 * Lädt Gruppe nach Id
	 *
	 * @param id
	 * @return
	 */
	public Gruppe loadByGruppeId(Long id) {
		List<BenutzerGruppeDB> gruppeDB = benutzerGruppeRepository.findByGruppeId(id);
		
		if (gruppeDB.size() > 0) {
			Gruppe gruppe = new Gruppe();
			gruppe.setName(gruppeDB.get(0).getGruppe());
			gruppe.setId(gruppeDB.get(0).getGruppeId());
			return gruppe;
		}
		
		return null;
	}
	
	public boolean accountInGruppe(Account account, Long gruppeId) {
		String benutzer = account.getName();
		return benutzerGruppeRepository.findByBenutzerAndGruppeId(benutzer, gruppeId) != null;
	}
	
	public List<Gruppe> sortGroupsByName(List<Gruppe> gruppen) {
		return gruppen.stream()
			.sorted(Comparator.comparing(Gruppe::getName))
			.collect(Collectors.toList());
	}
}
