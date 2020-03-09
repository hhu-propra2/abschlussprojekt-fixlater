package mops.termine2.database.entities;

import lombok.Data;
import mops.termine2.enums.Modus;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Data
public class Terminfindung {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long terminfindungId;
	
	private Date termin;
	
	private String ort;
	
	private String link;
	
	private Date frist;
	
	private Date loeschdatum;
	
	private String ersteller;
	
	private String titel;
	
	private Modus modus;
	
	private String beschreibung;
	
}
