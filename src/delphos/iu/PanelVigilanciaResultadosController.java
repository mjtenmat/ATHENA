package delphos.iu;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import delphos.ContrastarCon;
import delphos.Cromosoma2;
import delphos.DocumentoWeb;
import delphos.Gen;
import delphos.Localizacion;
import delphos.Searcher;
import delphos.Sector;
import delphos.TipoOrganizacion;

public class PanelVigilanciaResultadosController implements ActionListener {
	protected PanelVigilanciaResultados panelVigilanciaResultados;
	protected PanelVigilanciaController panelVigilanciaController;
	protected DialogRelevantes dlgRelevantes;
	protected Set<DocumentoWeb> setResultadosRelevantesDocumentosWeb;

	public PanelVigilanciaResultadosController(PanelVigilanciaResultados panelVigilanciaResultados, PanelVigilanciaController controller) {

		this.panelVigilanciaResultados = panelVigilanciaResultados;
		this.panelVigilanciaController = controller;
		this.setResultadosRelevantesDocumentosWeb = new HashSet<>();

	}

	public void addRelevante(DocumentoWeb docWeb) {
		if (this.setResultadosRelevantesDocumentosWeb != null)
			this.setResultadosRelevantesDocumentosWeb.add(docWeb);
		actualizarDialogoRelevantes();
	}

	public void actualizarDialogoRelevantes() {
		if (this.dlgRelevantes == null)
			dlgRelevantes = new DialogRelevantes(this);
		dlgRelevantes.actualizar(this.setResultadosRelevantesDocumentosWeb);
	}

	public void removeRelevante(DocumentoWeb docWeb) {
		if (this.setResultadosRelevantesDocumentosWeb != null)
			this.setResultadosRelevantesDocumentosWeb.remove(docWeb);
		actualizarDialogoRelevantes();
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		panelVigilanciaResultados.framePrincipal.frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		if (ae.getSource() == panelVigilanciaResultados.btnVolver) {
			panelVigilanciaResultados.framePrincipal.controller
					.verPanel(panelVigilanciaResultados.framePrincipal.panelVigilancia);
		}

		if (ae.getSource() == panelVigilanciaResultados.btnVerRelevantes) {
			if (dlgRelevantes != null)
				dlgRelevantes.setVisible(panelVigilanciaResultados.btnVerRelevantes.isSelected());
		}
		
		if (ae.getSource() == panelVigilanciaResultados.btnRR) {
			System.out.println("Retroalimentación por Relevancia");
			//TODO: Pendiente de implementar
			this.algoritmoGenetico();
		}

		panelVigilanciaResultados.framePrincipal.frame.setCursor(Cursor.getDefaultCursor());
	}

	private void algoritmoGenetico() {
		System.out.println("Algoritmo Genético.");
		//Creamos la lista de Cromosomas
		List<Cromosoma2> listaCromosomas = new ArrayList<>();
		for(DocumentoWeb documentoWeb : setResultadosRelevantesDocumentosWeb)
			listaCromosomas.add(new Cromosoma2(documentoWeb));
		
		//Expansión de los cromosomas, añadiendo los genes de cada uno a los demás, pero desactivados
		System.out.println("Expandiendo el primer cromosoma.");
		for(int i = 1; i < listaCromosomas.size(); i++) {
			for(Gen gen : listaCromosomas.get(i).getGenes()) {
				if(!listaCromosomas.get(0).getGenes().contains(gen)) {
					Gen nuevoGen = gen.clonar();
					nuevoGen.setActivo(false);
					listaCromosomas.get(0).getGenes().add(nuevoGen);
					System.out.println("Añadiendo gen " + nuevoGen.getRaiz());
				}
				else {
					System.out.println("Ya existe el gen " + gen.getRaiz());
				}
			}
		}
		System.out.println("Expandiendo el resto de cromosomas.");
		for(Gen gen : listaCromosomas.get(0).getGenes()) {
			for(int i = 1; i < listaCromosomas.size(); i++) {				
				if(!listaCromosomas.get(i).getGenes().contains(gen)) {
					Gen nuevoGen = gen.clonar();
					nuevoGen.setActivo(false);
					listaCromosomas.get(i).getGenes().add(nuevoGen);
					System.out.println("Añadiendo gen " + nuevoGen.getRaiz() + " al cromosoma " + i);
				}
				else {
					System.out.println("Ya existe el gen " + gen.getRaiz() + " en el cromosoma " + i);
				}
			}
		}
		
		/*
		Iterator<Cromosoma2> it = listaCromosomas.iterator();
		while (it.hasNext()) {
			Cromosoma2 crAExpandir = it.next();
			System.out.println("EXPANDIENDO: " + crAExpandir);
			
			for(Cromosoma2 cromosoma : listaCromosomas) {
				System.out.println("Analizando: " + cromosoma);
				if (cromosoma.equals(crAExpandir)) {
					System.out.println("evitado el propio cromosoma.");
					continue;
				}
				//Revisamos sus genes
				for(Gen gen : cromosoma.getGenes()) {
					if(!crAExpandir.getGenes().contains(gen)) {
						Gen nuevoGen = gen.clonar();
						nuevoGen.setActivo(false);
						crAExpandir.getGenes().add(nuevoGen);
						System.out.println("Añadiendo gen " + nuevoGen.getRaiz());
					}
					else {
						System.out.println("Ya existe el gen " + gen.getRaiz());
					}
				}
			}
			Collections.sort(crAExpandir.getGenes());
		}
		*/
		
		//Ordenamos los cromosomas
		for(Cromosoma2 cr : listaCromosomas)
			Collections.sort(cr.getGenes());
		
		for(int i = 0; i < listaCromosomas.size(); i++)
			System.out.println("Num genes: " + listaCromosomas.get(i).getGenes().size());
		verListaCromosomas(listaCromosomas);
		
		//Cruce de Cromosomas
		ArrayList<Cromosoma2> cromosomasCruzables = new ArrayList<>();
		for (Cromosoma2 cromosoma : listaCromosomas) {
			if (cromosoma.getCruzable())
				cromosomasCruzables.add(cromosoma);
		}
		Cromosoma2 primeroCruzable = cromosomasCruzables.get(0).clonar();
		
		System.out.println("Hay " + cromosomasCruzables.size() + " cromosomas cruzables.");
		
		for (int i = 0; i < cromosomasCruzables.size(); i = i+2) {
			if (i + 1 < cromosomasCruzables.size())
				Cromosoma2.cruzar(cromosomasCruzables.get(i), cromosomasCruzables.get(i+1));
			else
				Cromosoma2.cruzar(cromosomasCruzables.get(i), primeroCruzable);
		}
		
		System.out.println("Cromosomas Cruzados:");
		verListaCromosomas(listaCromosomas);
		
		//Mutación de todos los cromosomas
		for(Cromosoma2 cromosoma : listaCromosomas)
			cromosoma.mutar();
		
		System.out.println("Cromosomas Mutados:");
		verListaCromosomas(listaCromosomas);
		
		//Buscamos con todos los cromosomas
		ArrayList<DocumentoWeb> listaResultadosDocumentosWeb = buscarCombinacion(listaCromosomas);
		
		System.out.println("Obtenidos " + listaResultadosDocumentosWeb.size());
		System.out.println(listaResultadosDocumentosWeb);
		
		//Si hay resultados, se muestran y fin del algoritmo - PENDIENTE DE PRUEBA
		if (listaResultadosDocumentosWeb.size()>0) {
			System.out.println("Mostrando resultados de la combinación completa.");
			mostrarResultados(listaResultadosDocumentosWeb);
			return;
		}
		else {
			//Si no hay resultados. Probamos cada combinación
			System.out.println("No hay resultados con todos los cromosomas. Iniciando combinaciones:");
			//Mapa de combinaciones de cromosomas, indexado por su número de combinación
			Map<Integer, ArrayList<Cromosoma2>> combinaciones = new HashMap<>();
			//Mapa de resultados obtenidos por cada combinación
			Map<Integer, ArrayList<DocumentoWeb>> resultados = new HashMap<>();
			
			int numCombinaciones = (int) Math.pow(2, listaCromosomas.size()) - 1;
			System.out.println("NumCom" + numCombinaciones);
			for (int i = 1; i <= numCombinaciones; i++) {
				ArrayList<Cromosoma2> combinacion = new ArrayList<>();
				int tmp = i;
				for(int j = listaCromosomas.size() - 1; j >= 0 ; j--) {
					if (tmp >= Math.pow(2, j)) {
						combinacion.add(listaCromosomas.get(j));
						tmp -= Math.pow(2, j);
					}
				}
				combinaciones.put(i, new ArrayList<Cromosoma2>(combinacion));
				//Buscamos con la combinación
				resultados.put(i, buscarCombinacion(combinacion));
				
				System.out.println("Combinación " + i + ": Cromosomas: " + combinaciones.get(i).size() + " Resultados: " + resultados.get(i).size());
			}
			//Clasificamos el mapa de resultados por número de resultados obtenidos.
			List<Entry<Integer, ArrayList<DocumentoWeb>>> combinacionesPorNumResultados = new ArrayList<>(resultados.entrySet());
			combinacionesPorNumResultados.sort(new Comparator<Entry<Integer, ArrayList<DocumentoWeb>>>() {

				@Override
				public int compare(Entry<Integer, ArrayList<DocumentoWeb>> e1, Entry<Integer, ArrayList<DocumentoWeb>> e2) {
					return e2.getValue().size() - e1.getValue().size();
				}
	        	
			});
	        //Comprobación de la lista de combinacionesPorNumResultados
	        System.out.println("\nComprobación de resultados ordenados");
	        for(Entry<Integer, ArrayList<DocumentoWeb>> entry : combinacionesPorNumResultados) {
	        	System.out.println("Combinación: " + entry.getKey() + " NumResultados: " + entry.getValue().size());
	        }
	        
	        //Ordenamos los cromosomas por menor participación
	        Map<Cromosoma2, Integer> cromosomasParticipacion = new HashMap<>();
	        for(Entry<Integer, ArrayList<DocumentoWeb>> entry : combinacionesPorNumResultados) {
	        	if (entry.getValue().size() > 0) {
	        		//Incluimos sus cromosomas en el mapa
	        		int tmp = entry.getKey();	//Número de la combinación
	        		for(int j = listaCromosomas.size() - 1; j >= 0 ; j--) {
						if (tmp >= Math.pow(2, j)) {
							if (!cromosomasParticipacion.containsKey(listaCromosomas.get(j)))
								cromosomasParticipacion.put(listaCromosomas.get(j), 1);
							else
								cromosomasParticipacion.put(listaCromosomas.get(j), cromosomasParticipacion.get(listaCromosomas.get(j)) + 1);
							tmp -= Math.pow(2, j);
						}
	        		}
	        	}
	        }
	        List<Entry<Cromosoma2, Integer>> listaCromosomasParticipacion = new ArrayList<>(cromosomasParticipacion.entrySet());
	        listaCromosomasParticipacion.sort(new Comparator<Entry<Cromosoma2, Integer>>() {

				@Override
				public int compare(Entry<Cromosoma2, Integer> o1, Entry<Cromosoma2, Integer> o2) {
					return o1.getValue().compareTo(o2.getValue());
				}
	        	
			});
	        
	        //Comprobación de la participación de cromosomas
	        System.out.println("\nComprobación de Participación de cada cromosoma en resultados");
	        for(Entry<Cromosoma2, Integer> entry : listaCromosomasParticipacion) {
	        	System.out.println("CR" + listaCromosomas.indexOf(entry.getKey()) + ": " + entry.getValue() + " veces.");
	        }
	        
	        //Generamos la lista de resultados
	        listaResultadosDocumentosWeb = new ArrayList<>();
	        int combinacion = (int) (Math.pow(2, listaCromosomas.size()));
	        for(Entry<Cromosoma2, Integer> entry : listaCromosomasParticipacion) {
	        	Cromosoma2 cromosoma = entry.getKey();
	        	int indiceCromosoma = listaCromosomas.indexOf(cromosoma);
	        	//Calculamos la combinación en la que NO está el cromosoma
	        	combinacion -= Math.pow(2, indiceCromosoma);
	        	listaResultadosDocumentosWeb.addAll(resultados.get(combinacion));
	        }
	        mostrarResultados(listaResultadosDocumentosWeb);
		}
	}
	
	private void mostrarResultados(ArrayList<DocumentoWeb> listaResultadosDocumentosWeb) {
		this.panelVigilanciaResultados.borrarDocumentosWeb();
		this.panelVigilanciaResultados
				.setResultadoDocumentosWeb(listaResultadosDocumentosWeb.size());
		Iterator<DocumentoWeb> it2 = listaResultadosDocumentosWeb.iterator();
		while (it2.hasNext()) {
			this.panelVigilanciaResultados.addDocumentoWeb(it2.next(), this.panelVigilanciaController);
		}
		int pagActual = 0;
		int pagTotal = -1;
		PanelPaginacionPatentes pnPagDocsWeb = new PanelPaginacionPatentes(pagActual, pagTotal, this);
		this.panelVigilanciaResultados.panelDocumentosWeb.add(pnPagDocsWeb);
		this.panelVigilanciaController.panelVigilancia.framePrincipal.controller
		.verPanel(panelVigilanciaController.panelVigilancia.framePrincipal.panelVigilanciaResultados);
		this.panelVigilanciaController.panelVigilancia.revalidate();
		this.panelVigilanciaController.panelVigilancia.framePrincipal.getFrame().revalidate();
	}

	private ArrayList<DocumentoWeb> buscarCombinacion(List<Cromosoma2> listaCromosomas) {
		//Buscamos con todos los cromosomas
		String textoLibre = Cromosoma2.verTextoLibre(listaCromosomas);		
		ArrayList<DocumentoWeb> listaResultadosDocumentosWeb = new ArrayList<>();
		try {
			listaResultadosDocumentosWeb = Searcher.buscarDocumentosWeb(textoLibre, new HashSet<Localizacion>(), new HashSet<Sector>(),
					new HashSet<TipoOrganizacion>(), 0, new HashSet<ContrastarCon>(), false, false, false, "");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return listaResultadosDocumentosWeb;
	}

	private void verListaCromosomas(List<Cromosoma2> lista) {
		System.out.println("LISTA DE CROMOSOMAS:");
		//Cabecera
		System.out.print("Término\t\t");
		for (int i = 0; i < lista.size(); i++) {
			System.out.print("CR-" + i + "\t");
		}
		System.out.println();
		for (int i = 0; i < lista.get(0).getGenes().size(); i++) {
			System.out.print(lista.get(0).getGenes().get(i).getRaiz()+ "\t\t");
			for(int j = 0; j < lista.size(); j++)
				System.out.print(lista.get(j).getGenes().get(i).getActivo()+"\t");
			System.out.println();
		}
	}
	
}
