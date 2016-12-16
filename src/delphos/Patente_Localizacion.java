package delphos;

import java.util.ArrayList;
import java.util.Iterator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.Query;

import delphos.iu.Delphos;

@Entity
public class Patente_Localizacion{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private String nombre;
	private String descripcion;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	
	public String toString(){
		return this.getNombre() + " - " + this.getDescripcion();
	}

	public String getListaCodigos() {
		//Devuelve una lista de códigos separados por comas, incluyendo sublocalizaciones.
		
		StringBuilder resultado = new StringBuilder();
		resultado.append(this.getNombre() + ",");
		
		ArrayList<Patente_Localizacion> listaSubLoc = this.getSubLoc();
		for (Patente_Localizacion subLoc : listaSubLoc)
			resultado.append(subLoc.getNombre() + ",");

		String sResultado = resultado.toString();
		if (sResultado.length() > 0)
			sResultado = sResultado.substring(0, sResultado.length()-1);	//Quitamos la última coma
		
		return sResultado;
	}

	private ArrayList<Patente_Localizacion> getSubLoc() {
		ArrayList<Patente_Localizacion> listaSubLoc = new ArrayList<Patente_Localizacion>();
		
		Query query = Delphos.getSession().createQuery("FROM Patente_Localizacion WHERE idPadre = " + this.getId());
		listaSubLoc = (ArrayList<Patente_Localizacion>) query.list();
		
		//Añadimos subLocalizaciones
		ArrayList<Patente_Localizacion> listaAuxiliar = new ArrayList<Patente_Localizacion>();
		Iterator it = listaSubLoc.iterator();
		while(it.hasNext())
			listaAuxiliar.addAll(((Patente_Localizacion)it.next()).getSubLoc());
		
		listaSubLoc.addAll(listaAuxiliar);
		
		return listaSubLoc;
	}

}
