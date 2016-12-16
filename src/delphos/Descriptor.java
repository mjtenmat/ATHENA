package delphos;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Descriptor implements Cloneable{
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	private String texto;
	
	public Object clone() {
		Object obj = null;
		try {
			obj = super.clone();
		} catch (CloneNotSupportedException e) {
			System.out.println("Descriptor: CloneNotSupportedException.");
			System.exit(-1);
		}
		return obj;
	}
	public int getId() {
		return id;
	}
	public String getTexto() {
		return texto;
	}
	public void setTexto(String texto) {
		this.texto = texto;
	}
	
}
