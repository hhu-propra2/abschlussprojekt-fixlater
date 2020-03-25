package mops.termine2.controller;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import mops.termine2.Konstanten;
import mops.termine2.authentication.Account;
import mops.termine2.controller.formular.AntwortFormUmfragen;
import mops.termine2.controller.formular.ErgebnisFormUmfragen;
import mops.termine2.models.Kommentar;
import mops.termine2.models.LinkWrapper;
import mops.termine2.models.Umfrage;
import mops.termine2.models.UmfrageAntwort;
import mops.termine2.services.AuthenticationService;
import mops.termine2.services.GruppeService;
import mops.termine2.services.KommentarService;
import mops.termine2.services.UmfrageAntwortService;
import mops.termine2.services.UmfrageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.annotation.SessionScope;

import javax.annotation.security.RolesAllowed;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Controller
@SessionScope
@RequestMapping("/termine2")
public class UmfragenAbstimmungController {
	
	private final transient Counter authenticatedAccess;
	
	@Autowired
	private AuthenticationService authenticationService;
	
	@Autowired
	private UmfrageService umfrageService;
	
	@Autowired
	private UmfrageAntwortService umfrageAntwortService;
	
	@Autowired
	private GruppeService gruppeService;
	
	@Autowired
	private KommentarService kommentarService;
	
	private HashMap<LinkWrapper, Umfrage> letzteUmfrage = new HashMap<>();
	
	public UmfragenAbstimmungController(MeterRegistry registry) {
		authenticatedAccess = registry.counter("access.authenticated");
	}
	
	@GetMapping("/umfragen/{link}")
	@RolesAllowed({Konstanten.ROLE_ORGA, Konstanten.ROLE_STUDENTIN})
	public String umfrageDetails(Principal p, Model m, @PathVariable("link") String link) {
		Account account;
		if (p != null) {
			m.addAttribute(Konstanten.ACCOUNT, authenticationService.createAccountFromPrincipal(p));
			account = authenticationService.createAccountFromPrincipal(p);
		} else {
			return "error/403";
		}
		
		Umfrage umfrage = umfrageService.loadByLinkMitVorschlaegen(link);
		if (umfrage == null) {
			return "error/404";
		}
		
		if (umfrage.getGruppeId() != null
			&& !gruppeService.accountInGruppe(account, umfrage.getGruppeId())) {
			return "error/403";
		}
		
		LocalDateTime now = LocalDateTime.now();
		if (umfrage.getFrist().isBefore(now)) {
			return "redirect:/termine2/umfragen/" + link + "/ergebnis";
		}
		
		Boolean bereitsTeilgenommen = umfrageAntwortService.hatNutzerAbgestimmt(account.getName(), link);
		if (bereitsTeilgenommen) {
			return "redirect:/termine2/umfragen/" + link + "/ergebnis";
		} else {
			return "redirect:/termine2/umfragen/" + link + "/abstimmung";
		}
	}
	
	@GetMapping("/umfragen/{link}/abstimmung")
	@RolesAllowed({Konstanten.ROLE_ORGA, Konstanten.ROLE_STUDENTIN})
	public String umfrageAbstimmung(Principal p, Model m, @PathVariable("link") String link) {
		
		Account account;
		Umfrage umfrage = umfrageService.loadByLinkMitVorschlaegen(link);
		
		if (p != null) {
			m.addAttribute(Konstanten.ACCOUNT, authenticationService.createAccountFromPrincipal(p));
			account = authenticationService.createAccountFromPrincipal(p);
		} else {
			return "error/403";
		}
		
		if (umfrage == null) {
			return "error/404";
		}
		
		if (umfrage.getGruppeId() != null
			&& !gruppeService.accountInGruppe(account, umfrage.getGruppeId())) {
			return "error/403";
		}
		
		LocalDateTime now = LocalDateTime.now();
		if (umfrage.getFrist().isBefore(now)) {
			return "redirect:/termine2/umfragen/" + link + "/ergebnis";
		}
		
		UmfrageAntwort antwort = umfrageAntwortService.loadByBenutzerAndLink(account.getName(), link);
		AntwortFormUmfragen antwortForm = new AntwortFormUmfragen();
		antwortForm.init(antwort);
		List<Kommentar> kommentare = kommentarService.loadByLink(link);
		
		LinkWrapper setLink = new LinkWrapper(link);
		letzteUmfrage.put(setLink, umfrage);
		m.addAttribute("umfrage", umfrage);
		m.addAttribute("antwort", antwortForm);
		m.addAttribute("kommentare", kommentare);
		m.addAttribute("neuerKommentar", new Kommentar());
		
		authenticatedAccess.increment();
		
		return "umfragen-abstimmung";
	}
	
	@GetMapping("/umfragen/{link}/ergebnis")
	@RolesAllowed({Konstanten.ROLE_ORGA, Konstanten.ROLE_STUDENTIN})
	public String umfrageErgebnis(Principal p, Model m, @PathVariable("link") String link) {
		
		Umfrage umfrage = umfrageService.loadByLinkMitVorschlaegen(link);
		Account account;
		List<UmfrageAntwort> antworten;
		
		if (p != null) {
			m.addAttribute(Konstanten.ACCOUNT, authenticationService.createAccountFromPrincipal(p));
			account = authenticationService.createAccountFromPrincipal(p);
		} else {
			return "error/403";
		}
		
		if (umfrage == null) {
			return "error/404";
		}
		
		if (umfrage.getGruppeId() != null
			&& !gruppeService.accountInGruppe(account, umfrage.getGruppeId())) {
			return "error/403";
		}
		
		//Wenn Ergebnis erst nach Frist angezeigt werden soll,
		//muss dies hier noch abgefragt werden und evtl auf die
		//Abstimmungsseite umgeleitet werden;
		
		LocalDateTime now = LocalDateTime.now();
		Boolean bereitsTeilgenommen = umfrageAntwortService.hatNutzerAbgestimmt(account.getName(), link);
		if (!bereitsTeilgenommen && umfrage.getFrist().isAfter(now)) {
			System.out.println("abstimmung");
			return "redirect:/termine2/umfragen/" + link + "/abstimmung";
		}
		
		antworten = umfrageAntwortService.loadAllByLink(link);
		UmfrageAntwort nutzerAntwort = umfrageAntwortService.loadByBenutzerAndLink(
			account.getName(), link);
		ErgebnisFormUmfragen ergebnis = new ErgebnisFormUmfragen(antworten, umfrage, nutzerAntwort);
		List<Kommentar> kommentare = kommentarService.loadByLink(link);
		m.addAttribute("umfrage", umfrage);
		m.addAttribute("ergebnis", ergebnis);
		m.addAttribute("kommentare", kommentare);
		m.addAttribute("neuerKommentar", new Kommentar());
		
		authenticatedAccess.increment();
		
		return "umfragen-ergebnis";
	}
	
	
	@PostMapping(path = "/umfragen/{link}", params = "sichern")
	@RolesAllowed({Konstanten.ROLE_ORGA, Konstanten.ROLE_STUDENTIN})
	public String saveAbstimmung(Principal p,
								 Model m,
								 @PathVariable("link") String link,
								 @ModelAttribute AntwortFormUmfragen antwortForm) {
		Account account;
		if (p != null) {
			m.addAttribute(Konstanten.ACCOUNT, authenticationService.createAccountFromPrincipal(p));
			account = authenticationService.createAccountFromPrincipal(p);
		} else {
			System.out.println("nicht autorisiert");
			return null;
		}
		
		Umfrage umfrage =
			umfrageService.loadByLinkMitVorschlaegen(link);
		if (umfrage == null) {
			return "error/404";
		}
		
		if (umfrage.getGruppeId() != null
			&& !gruppeService.accountInGruppe(account, umfrage.getGruppeId())) {
			return "error/403";
		}
		
		LocalDateTime now = LocalDateTime.now();
		if (umfrage.getFrist().isBefore(now)) {
			return "redirect:/termine2/umfragen/" + link + "/abstimmung";
		}
		
		LinkWrapper linkWrapper = new LinkWrapper(link);
		if (!umfrage.equals(letzteUmfrage.get(linkWrapper))) {
			return "redirect:/termine2/umfragen/" + link;
		}
		
		UmfrageAntwort umfrageAntwort = AntwortFormUmfragen.mergeToAnswer(umfrage, account.getName(),
			antwortForm);
		
		umfrageAntwortService.abstimmen(umfrageAntwort, umfrage);
		authenticatedAccess.increment();
		
		return "redirect:/termine2/umfragen/" + link;
	}
	
	@PostMapping(path = "/umfragen/{link}", params = "kommentarSichern")
	@RolesAllowed({Konstanten.ROLE_ORGA, Konstanten.ROLE_STUDENTIN})
	public String saveKommentar(Principal p, Model m, @PathVariable("link") String link, Kommentar neuerKommentar) {
		Account account;
		if (p != null) {
			m.addAttribute(Konstanten.ACCOUNT, authenticationService.createAccountFromPrincipal(p));
			account = authenticationService.createAccountFromPrincipal(p);
		} else {
			return null;
		}
		
		Umfrage umfrage = umfrageService.loadByLinkMitVorschlaegen(link);
		if (umfrage == null) {
			return "error/404";
		}
		
		if (umfrage.getGruppeId() != null
			&& !gruppeService.accountInGruppe(account, umfrage.getGruppeId())) {
			return "error/403";
		}
		
		LocalDateTime now = LocalDateTime.now();
		neuerKommentar.setLink(link);
		neuerKommentar.setErstellungsdatum(now);
		m.addAttribute("neuerKommentar", neuerKommentar);
		kommentarService.save(neuerKommentar);
		authenticatedAccess.increment();
		
		return "redirect:/termine2/umfragen/" + link;
	}
}
