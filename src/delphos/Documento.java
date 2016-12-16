package delphos;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Documento {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private String texto;
	
	public String getTexto() {
		return texto;
	}
	public void setTexto(String texto) {
		this.texto = texto;
	}
}
