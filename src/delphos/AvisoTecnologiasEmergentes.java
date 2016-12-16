package delphos;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class AvisoTecnologiasEmergentes {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	private String termino;
	private String tipo;
	private String titulo;
	private String extracto;
	private String url;
	private boolean revisado;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getTermino() {
		return termino;
	}
	public void setTermino(String termino) {
		this.termino = termino;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public String getTitulo() {
		return titulo;
	}
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	public String getExtracto() {
		return extracto;
	}
	public void setExtracto(String extracto) {
		this.extracto = extracto;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public boolean isRevisado() {
		return revisado;
	}
	public void setRevisado(boolean revisado) {
		this.revisado = revisado;
	}

}
