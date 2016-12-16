package delphos;

import javax.persistence.Entity;

@Entity
public class Patente_Sector extends Jerarquia<Patente_Sector> {
	
	public Patente_Sector(){
		super(Patente_Sector.class);
	}

}
