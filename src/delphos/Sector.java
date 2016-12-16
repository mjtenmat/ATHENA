package delphos;

import javax.persistence.Entity;

@Entity
public class Sector extends Jerarquia<Sector> {
	
	public Sector(){
		super(Sector.class);
	}
}
