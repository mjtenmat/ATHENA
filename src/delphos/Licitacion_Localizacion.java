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
public class Licitacion_Localizacion extends Jerarquia<Licitacion_Localizacion> {

	public Licitacion_Localizacion() {
		super(Licitacion_Localizacion.class);
	}

	public String getListaCodigos() {
		//Devuelve una lista de códigos separados por comas, incluyendo sublocalizaciones.
		
		StringBuilder resultado = new StringBuilder();
		resultado.append(this.getNombre() + ",");
		
		ArrayList<Licitacion_Localizacion> listaSubLoc = this.getSubLoc();
		for (Licitacion_Localizacion subLoc : listaSubLoc)
			resultado.append(subLoc.getNombre() + ",");
				
		String sResultado = resultado.toString();
		if (sResultado.length() > 0)
			sResultado = sResultado.substring(0, sResultado.length()-1);	//Quitamos la última coma
		
		return sResultado;
	}

	private ArrayList<Licitacion_Localizacion> getSubLoc() {
		ArrayList<Licitacion_Localizacion> listaSubLoc = new ArrayList<Licitacion_Localizacion>();
		
		Query query = Delphos.getSession().createQuery("FROM Licitacion_Localizacion WHERE idPadre = " + this.getId());
		listaSubLoc = (ArrayList<Licitacion_Localizacion>) query.list();
		
		//Añadimos subLocalizaciones
		ArrayList<Licitacion_Localizacion> listaAuxiliar = new ArrayList<Licitacion_Localizacion>();
		Iterator it = listaSubLoc.iterator();
		while(it.hasNext())
			listaAuxiliar.addAll(((Licitacion_Localizacion)it.next()).getSubLoc());
		
		listaSubLoc.addAll(listaAuxiliar);
		
		return listaSubLoc;
	}

}
