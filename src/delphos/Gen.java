package delphos;

import java.util.HashMap;
import java.util.Map;

import com.mchange.v2.collection.MapEntry;

public class Gen implements Comparable<Gen>{
	private String raiz;
	private Map<String, Integer> terminos = new HashMap<>();
	private Boolean activo = true;

	public Gen(String raiz) {
		this.raiz = raiz;// TODO Auto-generated method stub
	}

	public String getRaiz() {
		return raiz;
	}

	public void setRaiz(String raiz) {
		this.raiz = raiz;
	}

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}

	public Map<String, Integer> getTerminos() {
		return terminos;
	}

	public void setTerminos(Map<String, Integer> terminos) {
		this.terminos = terminos;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((raiz == null) ? 0 : raiz.hashCode());
		return result;
	}

	/**
	 * Dos genes son iguales si tienen la misma raíz
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Gen other = (Gen) obj;
		if (raiz == null) {
			if (other.raiz != null)
				return false;
		} else if (!raiz.equals(other.raiz))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Gen: " + raiz + " " + activo + " -> " + terminos; 
	}

	public Gen clonar() {
		Gen clon = new Gen(this.raiz);
		clon.terminos = new HashMap<String, Integer>(this.terminos);
		clon.activo = this.activo;
		return clon;
	}

	@Override
	public int compareTo(Gen o) {
		return this.raiz.compareTo(o.raiz);
	}

	public void mutar() {
		//Cambia su valor
		this.activo = !this.activo;
		
	}

	public void sumar(Gen gen) {
		for(String termino : gen.getTerminos().keySet()) {
			if (this.terminos.containsKey(termino)) {
				//Si ya tiene el término, sumamos las frecuencias
				this.terminos.put(termino, this.terminos.get(termino)+ gen.getTerminos().get(termino));
			}
			else //Si no lo tiene, lo añadimos
				this.terminos.put(termino, gen.getTerminos().get(termino));
		}
	}

	public String getTerminoPrincipal() {
		Integer maximo = null;
		String resultado = "";
		for(String termino : this.terminos.keySet()) {
			if (maximo == null) {
				maximo = this.terminos.get(termino);
				resultado = termino;
			}
			else {
				if (maximo < this.terminos.get(termino)){
					maximo = this.terminos.get(termino);
					resultado = termino;
				}
			}
		}
		return resultado;
	}
}