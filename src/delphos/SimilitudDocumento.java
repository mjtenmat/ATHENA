package delphos;

public class SimilitudDocumento implements Comparable<SimilitudDocumento> {
	double similitud;
	Integer idDocumento;
	double exhaustividad;
	double precision;

	public SimilitudDocumento(double similarity, Integer idDocument) {
		this.similitud = similarity;
		this.idDocumento = idDocument;
	}

	public double getSimilitud() {
		return similitud;
	}
	public void setSimilitud(double similitud) {
		this.similitud = similitud;
	}
	public Integer getIdDocumento() {
		return idDocumento;
	}
	public void setIdDocumento(Integer idDocumento) {
		this.idDocumento = idDocumento;
	}
	public double getExhaustividad() {
		return exhaustividad;
	}
	public void setExhaustividad(double exhaustividad) {
		this.exhaustividad = exhaustividad;
	}
	public double getPrecision() {
		return precision;
	}
	public void setPrecision(double precision) {
		this.precision = precision;
	}

	@Override
	public int compareTo(SimilitudDocumento o) {
		if (this.similitud == o.similitud)
			return 0;
		if (this.similitud > o.similitud)
			return -1;
		else
			return 1;
	}

	@Override
	public String toString() {
		return "SimilitudDocumento [similitud=" + similitud + ", idDocumento=" + idDocumento + ", exhaustividad="
				+ exhaustividad + ", precision=" + precision + "]";
	}
	
}