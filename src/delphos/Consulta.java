package delphos;

import java.util.Set;

public class Consulta {
	
	private Set<Peso> pesos;
	private double similitud;

	public void setPesos(Set<Peso> pesos) {
		this.pesos = pesos;
	}

	public double getSimilitud() {
		return similitud;
	}

	public void setSimilitud(double similitud) {
		this.similitud = similitud;
	}

	public Set<Peso> getPesos() {
		return pesos;
	}

}
