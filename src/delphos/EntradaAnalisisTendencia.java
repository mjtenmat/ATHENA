package delphos;

public class EntradaAnalisisTendencia implements Comparable{
	private String termino;
	private int frecuenciaAbsoluta;

	public EntradaAnalisisTendencia(String termino, int frecuenciaAbsoluta) {
		this.termino = termino;
		this.frecuenciaAbsoluta = frecuenciaAbsoluta;
	}

	public void incrementar() {
		frecuenciaAbsoluta++;
	}

	public String getTermino() {
		return termino;
	}

	public void setTermino(String termino) {
		this.termino = termino;
	}

	public int getFrecuenciaAbsoluta() {
		return frecuenciaAbsoluta;
	}

	public void setFrecuenciaAbsoluta(int frecuenciaAbsoluta) {
		this.frecuenciaAbsoluta = frecuenciaAbsoluta;
	}

	@Override
	public String toString() {
		return "("+ frecuenciaAbsoluta +")" + termino;
	}

	@Override
	public int compareTo(Object o) {
		return ((EntradaAnalisisTendencia)o).frecuenciaAbsoluta - this.frecuenciaAbsoluta;
		//return 0;
	}

}
