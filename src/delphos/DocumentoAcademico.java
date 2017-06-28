package delphos;

import java.util.Date;

public class DocumentoAcademico {
	private String titulo;
	private String Autor;
	private String Entidad;
	private String fechaDisponibilidad;
	private String resumen;
	private String href;
	
	public String getTitulo() {
		return titulo;
	}
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	public String getAutor() {
		return Autor;
	}
	public void setAutor(String autor) {
		Autor = autor;
	}
	public String getFechaDisponibilidad() {
		return fechaDisponibilidad;
	}
	public void setFechaPublicacion(String fechaDisponibilidad) {
		this.fechaDisponibilidad = fechaDisponibilidad;
	}
	public String getResumen() {
		return resumen;
	}
	public void setResumen(String resumen) {
		this.resumen = resumen;
	}
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public String getEntidad() {
		return Entidad;
	}
	public void setEntidad(String entidad) {
		Entidad = entidad;
	}
	
}
