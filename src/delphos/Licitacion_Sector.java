package delphos;

import java.util.ArrayList;
import java.util.Iterator;

import javax.persistence.Entity;

import org.hibernate.Query;

import delphos.iu.Delphos;

@Entity
public class Licitacion_Sector extends Jerarquia<Licitacion_Sector> {

	public Licitacion_Sector() {
		super(Licitacion_Sector.class);
	}

	public String getListaCPV() {
		//Devuelve una lista de códigos CPV separados por comas, incluyendo subsectores.
		//El código CPV está en el nombre, siempre que sea numérico
		
		StringBuilder resultado = new StringBuilder();
		if (this.getNombre().matches("\\d*"))
			//resultado.append(this.getNombre() + ",");
			return this.getNombre();
		
		ArrayList<Licitacion_Sector> listaSubSectores = this.getSubSectores();
		for (Licitacion_Sector subsector : listaSubSectores)
			if (subsector.getNombre().matches("\\d*"))
				resultado.append(subsector.getNombre() + ",");
				
		String sResultado = resultado.toString();
		if (sResultado.length() > 0)
			sResultado = sResultado.substring(0, sResultado.length()-1);	//Quitamos la última coma
		
		return sResultado;
	}

	private ArrayList<Licitacion_Sector> getSubSectores() {
		//Devolvemos solo lista de CPVs numéricos
		ArrayList<Licitacion_Sector> listaSubsectores = new ArrayList<Licitacion_Sector>();
		
		Query query = Delphos.getSession().createQuery("FROM Licitacion_Sector WHERE idPadre = " + this.getId());
		ArrayList<Licitacion_Sector> listaAuxiliar1 = (ArrayList<Licitacion_Sector>) query.list();
		
		//Añadimos subsectores
		ArrayList<Licitacion_Sector> listaAuxiliar2 = new ArrayList<Licitacion_Sector>();
		Iterator it = listaAuxiliar1.iterator();
		while(it.hasNext()){
			Licitacion_Sector next = (Licitacion_Sector) it.next();
			if (next.getNombre().matches("\\d*"))
				listaSubsectores.add(next);
			else
				listaAuxiliar2.addAll(((Licitacion_Sector)next).getSubSectores());
		}
		
		listaSubsectores.addAll(listaAuxiliar2);
		
		return listaSubsectores;
	}

}
