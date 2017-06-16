package delphos.iu;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import delphos.Jerarquia;
import delphos.Resultado;
import delphos.Searcher;

public class PanelBusquedaController implements ActionListener, MouseListener {
	public final int NUM_RESULTADOS_POR_PAGINA = 10;
	private PanelBusqueda panelBusqueda;
	//private PanelPaginacion panelPaginacion;
	private ArrayList<Resultado> listaResultados = new ArrayList<>();
	protected Set<Jerarquia> sectores = new HashSet<>();
	protected Set<Jerarquia> tiposOrganizacion = new HashSet<>();
	protected Set<Jerarquia> localizaciones = new HashSet<>();
	private int paginaActual;
	private ArrayList<Resultado> listaResultadosRelevantes = new ArrayList<>();
	private int numResultadosRelevantes = 0;
	private DelphosTreeDialog treeDialog;
	private Resultado resultadoNoRelevante = null; 
	private Resultado resultadoPulsado = null;
	private Integer numResultadosRelevantesUltimaRR = null;	//Para calcular precisión
	//private boolean mostrarBotonAG = false;
	private boolean relevantesAbiertos = false;

	public PanelBusquedaController(PanelBusqueda panel){
		this.panelBusqueda = panel;
	}
	
	//Método de ActionListener
	@Override
	public void actionPerformed(ActionEvent e) {
		this.panelBusqueda.framePrincipal.frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		switch (e.getSource().getClass().toString()) {
		case "class javax.swing.JButton":
			switch (((JButton) e.getSource()).getName()) {
			
			case "btnBuscar":
				this.listaResultados = Searcher.buscar(panelBusqueda.getConsulta(), sectores, tiposOrganizacion, localizaciones);
				this.paginaActual = 0;
				this.resultadoNoRelevante = null;
				this.panelBusqueda.getBtnRRmin().setEnabled(false);
				this.panelBusqueda.getBtnRRmax().setEnabled(false);
				this.panelBusqueda.getBtnAG().setEnabled(false);
				this.listaResultadosRelevantes.clear();
				numResultadosRelevantesUltimaRR = null;
				//this.mostrarBotonAG = false;
				this.mostrarResultados();
				break;
			
			case "btnRRmin":
				this.numResultadosRelevantesUltimaRR = this.listaResultadosRelevantes.size();
				this.listaResultados = Searcher.mejorarRRmin(this.listaResultadosRelevantes, this.resultadoNoRelevante, sectores, tiposOrganizacion, localizaciones);
				this.paginaActual = 0;
				//this.mostrarBotonAG = true;
				this.panelBusqueda.getBtnAG().setEnabled(true);
				this.mostrarResultados();
				break;

			case "btnRRmax":
				this.numResultadosRelevantesUltimaRR = this.listaResultadosRelevantes.size();
				this.listaResultados = Searcher.mejorarRRmax(this.listaResultadosRelevantes, this.resultadoNoRelevante, sectores, tiposOrganizacion, localizaciones);
				this.paginaActual = 0;
				//this.mostrarBotonAG = true;
				this.panelBusqueda.getBtnAG().setEnabled(true);
				this.mostrarResultados();
				break;
				
			case "btnAG":
				this.listaResultados = Searcher.mejorarAG(sectores, tiposOrganizacion, localizaciones);
				this.paginaActual = 0;
				//this.mostrarBotonAG = true;
				this.panelBusqueda.getBtnAG().setEnabled(true);
				this.mostrarResultados();
				break;
				
			case "btnRelevantes":
				this.relevantesAbiertos = !this.relevantesAbiertos;
				this.mostrarRelevantes();
				break;
				
			case "btnSectores":
				this.panelBusqueda.getTreeFrameSector().seleccionar(sectores);
				this.panelBusqueda.getTreeFrameSector().setVisible(true);
				break;
				
			case "btnTipoOrganizacion":
				this.panelBusqueda.getTreeFrameTipoOrganizacion().seleccionar(tiposOrganizacion);
				this.panelBusqueda.getTreeFrameTipoOrganizacion().setVisible(true);
				break;
			case "btnLocalizacion":
				this.panelBusqueda.getTreeFrameLocalizacion().seleccionar(localizaciones);
				this.panelBusqueda.getTreeFrameLocalizacion().setVisible(true);
				break;
				
			//Botones de DelphosTreeFrame
			case "btnCancelarDelphosTree":
				treeDialog = (DelphosTreeDialog) SwingUtilities.getRoot((Component) e.getSource());
				treeDialog.setVisible(false);
				break;
			case "btnBorrarDelphosTree":
				treeDialog = (DelphosTreeDialog) SwingUtilities.getRoot((Component) e.getSource());
				treeDialog.borrarSeleccion();
				break;
			case "btnAceptarDelphosTree":
				treeDialog = (DelphosTreeDialog) SwingUtilities.getRoot((Component) e.getSource());
				Set<Jerarquia> setRestriccion = null;
				String restriccion = treeDialog.getClase().getSimpleName();
				String texto = "";
				if (restriccion.equals("Sector"))
					setRestriccion = sectores;
				if (restriccion.equals("TipoOrganizacion"))
					setRestriccion = tiposOrganizacion;
				if (restriccion.equals("Localizacion"))
					setRestriccion = localizaciones;
				
				if (setRestriccion != null){
					setRestriccion = treeDialog.getSeleccion();
					Iterator it = setRestriccion.iterator();
					boolean primero = true;
					while (it.hasNext()){
						if (primero)
							texto = ((Jerarquia)it.next()).getNombre();
						else
							texto += ", " + ((Jerarquia)it.next()).getNombre();
						panelBusqueda.setTextoRestriccion(restriccion, texto);
					}
				}
				if (texto.length() == 0)
					panelBusqueda.setTextoRestriccion(restriccion, "Todos.");

				treeDialog.setVisible(false);
				
				//Guardamos la lista modificada
				if (restriccion.equals("Sector"))
					sectores = setRestriccion;
				if (restriccion.equals("TipoOrganizacion"))
					tiposOrganizacion = setRestriccion;
				if (restriccion.equals("Localizacion"))
					localizaciones = setRestriccion;
			}
			
		case "class javax.swing.plaf.basic.BasicArrowButton":
			switch (((JButton) e.getSource()).getName()) {
			case "btnAnterior":
				this.paginaActual--;
				if (this.paginaActual < 0)
					this.paginaActual = 0;
				this.mostrarResultados();
				break;
			case "btnSiguiente":
				this.paginaActual++;
				this.mostrarResultados();
				break;			
			}
			break;
			
		case "class delphos.iu.JCheckBoxResultadoRelevante":
			JCheckBoxResultadoRelevante jcheck = (JCheckBoxResultadoRelevante)e.getSource(); 
			jcheck.getResultado().setRelevante(jcheck.isSelected());//Marcamos o desmarcamos el resultado como relevante
			if(jcheck.isSelected()){
				listaResultadosRelevantes.add(jcheck.getResultado());
				//Comprobamos si hemos mejorado la precisión
				if (numResultadosRelevantesUltimaRR != null) //Es null si no ha habido RR
					if (listaResultadosRelevantes.size() > this.numResultadosRelevantesUltimaRR){
						//this.panelPaginacion.activarAG(false);
						//this.mostrarBotonAG = false;
						this.panelBusqueda.getBtnAG().setEnabled(false);
					}
			}
			else{
				listaResultadosRelevantes.remove(jcheck.getResultado());
				if (numResultadosRelevantesUltimaRR != null) //Es null si no ha habido RR
					if (listaResultadosRelevantes.size() <= this.numResultadosRelevantesUltimaRR){
						//this.panelPaginacion.activarAG(true);
						//this.mostrarBotonAG = true;
						this.panelBusqueda.getBtnAG().setEnabled(true);
					}
			}
			
			if (listaResultadosRelevantes.size() > 0){
				panelBusqueda.getBtnRRmin().setEnabled(true);
				panelBusqueda.getBtnRRmax().setEnabled(true);
				panelBusqueda.getBtnRelevantes().setEnabled(true);
			}
			else{
				panelBusqueda.getBtnRRmin().setEnabled(false);
				panelBusqueda.getBtnRRmax().setEnabled(false);
				panelBusqueda.getBtnRelevantes().setEnabled(false);
			}
			break;
		case "class javax.swing.JMenuItem":
			if (((JMenuItem)e.getSource()).getName().equals("ElegirNoRelevante")){
				this.resultadoNoRelevante = this.resultadoPulsado;
			}
			if (((JMenuItem)e.getSource()).getName().equals("DesmarcarNoRelevante")){
				this.resultadoNoRelevante = null;
			}
			this.mostrarResultados();
			this.mostrarRelevantes();
			break;
		}	
		this.panelBusqueda.framePrincipal.frame.setCursor(Cursor.getDefaultCursor());
	}
	
	//Métodos de MouseListener
	@Override
	public void mouseClicked(MouseEvent e) {
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		if(e.isPopupTrigger()){
			this.resultadoPulsado = ((PanelResultado)e.getSource()).getResultado();
			JPopupMenu mnPopup = new JPopupMenu();
			JMenuItem miNoRelevante = new JMenuItem("Elegir como No Relevante");
			miNoRelevante.setName("ElegirNoRelevante");
			if (this.resultadoPulsado.equals(this.resultadoNoRelevante)){
				miNoRelevante.setText("Desmarcar No Relevante");	
				miNoRelevante.setName("DesmarcarNoRelevante");
			}
			miNoRelevante.addActionListener(this);
			mnPopup.add(miNoRelevante);
			mnPopup.show(e.getComponent(), e.getX(), e.getY());
		}
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	private void mostrarResultados(){
		panelBusqueda.getPanelResultados().removeAll();
//		int totalPaginas = (Integer)(listaResultados.size() / NUM_RESULTADOS_POR_PAGINA)+1;
//		if (listaResultados.size() % NUM_RESULTADOS_POR_PAGINA == 0)
//			totalPaginas--;
	    
		//Presentamos los resultados
		JLabel jlbResumenResultados = new JLabel();
		if (listaResultados.size() == 0)
			jlbResumenResultados.setText("No se han encontrado resultados.");
		else{
			String texto = "Encontrados " + Searcher.getNumResultadosUltimaBusqueda() + " resultados.";
			if (Searcher.getNumResultadosUltimaBusqueda() > Searcher.RESULTADOS_UMBRAL)
				texto +=  " Mostrando " + Searcher.RESULTADOS_UMBRAL; 
			jlbResumenResultados.setText(texto);
		}

		this.panelBusqueda.getPanelResultados().add(jlbResumenResultados);
		
		int primerResultado = paginaActual*NUM_RESULTADOS_POR_PAGINA;	//primer resultado a mostrar
		Iterator<Resultado> iterador = listaResultados.listIterator(primerResultado);
		int i = 0;
		while ( ( i < NUM_RESULTADOS_POR_PAGINA) && iterador.hasNext()){
			Resultado resultado = iterador.next();
			PanelResultado panelResultado = new PanelResultado(resultado,this.panelBusqueda);
			if (resultado.equals(this.resultadoNoRelevante))
				panelResultado.marcarNoRelevante();
			this.panelBusqueda.getPanelResultados().add(panelResultado);
			i++;
		}
//		this.panelPaginacion = new PanelPaginacion(paginaActual, totalPaginas, this);
//		this.panelPaginacion.activarAG(this.mostrarBotonAG);
//		this.panelBusqueda.getPanelResultados().add(this.panelPaginacion.getPanel())
		
		this.panelBusqueda.framePrincipal.getFrame().pack();
		
		//Colocamos la barra de scroll al inicio
		JViewport jv = this.panelBusqueda.panelScroll.getViewport();
		jv.setViewPosition(new Point(0,0));
	}

	private void mostrarRelevantes(){
		JPanel panelRelevantes = panelBusqueda.dlgRelevantes.getPanel();
		panelRelevantes.removeAll();
		for(Resultado resultadoRelevante : listaResultadosRelevantes){
			PanelResultado panelResultado = new PanelResultado(resultadoRelevante, panelBusqueda);
			if (resultadoRelevante.equals(this.resultadoNoRelevante))
				panelResultado.marcarNoRelevante();
			panelRelevantes.add(panelResultado);
		}

		panelBusqueda.dlgRelevantes.pack();   
		panelBusqueda.dlgRelevantes.setVisible(this.relevantesAbiertos);
	}
	
}
