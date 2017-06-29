package delphos;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.criterion.Example;

import delphos.iu.Delphos;


@Entity
public class UltimoAvisoDSpace{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private String termino;
	private String url;

	public UltimoAvisoDSpace() {
		termino = null;
		url = null;
	}
	
	public UltimoAvisoDSpace(String termino, String url) {
		this.termino = termino;
		this.url = url;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTermino() {
		return termino;
	}

	public void setTermino(String termino) {
		this.termino = termino;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public static UltimoAvisoDSpace buscar(String termino){
		UltimoAvisoDSpace ua = new UltimoAvisoDSpace(termino, null);
		return (UltimoAvisoDSpace) Delphos.getSession().createCriteria(UltimoAvisoDSpace.class).add(Example.create(ua)).uniqueResult();
	}
}
