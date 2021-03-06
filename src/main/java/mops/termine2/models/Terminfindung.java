package mops.termine2.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(exclude = "teilgenommen")
public class Terminfindung {
	
	private String link;
	
	private String titel;
	
	private String ort;
	
	private String beschreibung;
	
	@DateTimeFormat(pattern = "dd.MM.yyyy, HH:mm")
	private List<LocalDateTime> vorschlaege;
	
	private String ersteller;
	
	@DateTimeFormat(pattern = "dd.MM.yyyy, HH:mm")
	private LocalDateTime frist;
	
	@DateTimeFormat(pattern = "dd.MM.yyyy, HH:mm")
	private LocalDateTime loeschdatum;
	
	private String gruppeId;
	
	private String gruppeName;
	
	@DateTimeFormat(pattern = "dd.MM.yyyy, HH:mm")
	private LocalDateTime ergebnis;
	
	private Boolean teilgenommen = false;
	
	private Boolean ergebnisVorFrist = false;
	
	private Boolean einmaligeAbstimmung = true;
}
	


