package delphos;

import java.util.ArrayList;

public class Cromosoma{
	private ArrayList<Descriptor> descriptores = new ArrayList<Descriptor>();
	private ArrayList<Double> pesos = new ArrayList<Double>();
		
	public Cromosoma clone(){
		Cromosoma clon = new Cromosoma();
		for (Descriptor desc : descriptores)
			clon.getDescriptores().add((Descriptor)desc.clone());
		for (Double peso : pesos)
			clon.getPesos().add(peso);
			
		return clon;
	}
	
 	public ArrayList<Descriptor> getDescriptores() {
		return descriptores;
	}
	public void setDescriptores(ArrayList<Descriptor> descriptores) {
		this.descriptores = descriptores;
	}
	public ArrayList<Double> getPesos() {
		return pesos;
	}
	public void setPesos(ArrayList<Double> pesos) {
		this.pesos = pesos;
	}
	
}
