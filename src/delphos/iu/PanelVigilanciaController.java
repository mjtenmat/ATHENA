package delphos.iu;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import org.hibernate.Query;
import org.hibernate.Session;

import delphos.CPI;
import delphos.CPV;
import delphos.ContrastarCon;
import delphos.DocumentoWeb;
import delphos.Licitacion;
import delphos.Licitacion_Localizacion;
import delphos.Localizacion;
import delphos.Patente;
import delphos.Patente_Localizacion;
import delphos.Patente_Sector;
import delphos.Searcher;
import delphos.Sector;
import delphos.TipoOrganizacion;

public class PanelVigilanciaController implements ActionListener, DelphosSelectionListener, MouseListener {
	private PanelVigilancia panelVigilancia;
	private PanelPaginacionPatentes pnPagTED;
	private PanelPaginacionPatentes pnPagEPO;
	private PanelPaginacionPatentes pnPagDocsWeb;
	protected Set<Sector> sectores = new HashSet<>();
	protected Set<CPV> codCPV = new HashSet<>();
	protected Set<CPI> codCPI = new HashSet<>();
	protected Set<Patente_Sector> licitacionSectores = new HashSet<>();
	protected Set<Patente_Localizacion> patenteLocalizacion = new HashSet<>();
	protected Set<Licitacion_Localizacion> licitacionLocalizacion = new HashSet<>();
	protected Set<Patente_Sector> patenteSectores = new HashSet<>();
	protected Set<Localizacion> docsWebLocalizacion = new HashSet<>();
	protected Set<Sector> docsWebSector = new HashSet<>();
	protected Set<TipoOrganizacion> docsWebTipoOrganizacion = new HashSet<>();
	protected Set<TipoOrganizacion> setTipoOrganizacion = new HashSet<>();
	protected ArrayList<Licitacion> listaResultadosLicitaciones;
	protected ArrayList<Patente> listaResultadosPatentes;
	protected ArrayList<DocumentoWeb> listaResultadosDocumentosWeb;
	private int indiceBusquedaTED = 0;
	private int indiceBusquedaEPO = 0;
	private int indiceBusquedaDocumentosWeb = 0;
	private String textoLibreCompleto;

	public PanelVigilanciaController(PanelVigilancia panelVigilancia) {
		this.panelVigilancia = panelVigilancia;
		//Creados para que el ActionListener no de error
		this.pnPagTED = new PanelPaginacionPatentes(0, 0, this);
		this.pnPagEPO = new PanelPaginacionPatentes(0, 0, this); 
		this.pnPagDocsWeb = new PanelPaginacionPatentes(0, 0, this);
	}

	public DefaultComboBoxModel<String> getListaPaisesLicitacion() {
		return getLista("SELECT DISTINCT(pais) FROM Licitacion_Localizacion ORDER BY pais ASC");
	}

	public DefaultComboBoxModel<String> getListaCiudadesLicitacion() {
		return getLista("SELECT DISTINCT(ciudad) FROM Licitacion_Localizacion ORDER BY ciudad ASC");
	}

	public DefaultComboBoxModel<String> getListaEntidadesEmisorasLicitacion() {
		return getLista("SELECT DISTINCT(entidadEmisora) FROM Licitacion ORDER BY entidadEmisora ASC");
	}

	public DefaultComboBoxModel<String> getListaTipoLicitacion() {
		return getLista("SELECT DISTINCT(nombre) FROM TipoLicitacion ORDER BY nombre ASC");
	}

	private DefaultComboBoxModel<String> getLista(String consulta) {
		Session session = Delphos.getSession();
		Query query = session.createSQLQuery(consulta);
		List<String> lista = query.list();
		lista.add(0, null);
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel(lista.toArray(new String[lista.size()]));
		return model;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		this.panelVigilancia.framePrincipal.frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		if (e.getSource() == this.panelVigilancia.btnLicitacionSectores)
			this.panelVigilancia.dstdLicitacionSector.mostrar();

		if (e.getSource() == this.panelVigilancia.btnLicitacionLocalizacion)
			this.panelVigilancia.dstdLicitacionLocalizacion.mostrar();
		
		if (e.getSource() == this.panelVigilancia.btnPatenteLocalizacion)
			this.panelVigilancia.dsldPatenteLocalizacion.mostrar();
		
		if (e.getSource() == this.panelVigilancia.btnPatenteSectores)
			this.panelVigilancia.dstdPatenteSector.mostrar();
		
		if (e.getSource() == this.panelVigilancia.btnDocsWebLocalizacion)
			this.panelVigilancia.dstdDocsWebLocalizacion.mostrar();
		
		if (e.getSource() == this.panelVigilancia.btnDocsWebSector)
			this.panelVigilancia.dstdDocsWebSector.mostrar();
		
		if (e.getSource() == this.panelVigilancia.btnDocsWebTipoOrganizacion)
			this.panelVigilancia.dstdDocsWebTipoOrganizacion.mostrar();
		
		if (e.getSource() == this.panelVigilancia.btnContrastarCon)
			this.panelVigilancia.dstdContrastarCon.mostrar();
		
		if (e.getSource() == this.panelVigilancia.btnBuscar) {
			this.indiceBusquedaTED = 1;
			this.indiceBusquedaEPO = 1;
			this.indiceBusquedaDocumentosWeb = 0;
			
			textoLibreCompleto = crearTextoLibreCompleto();

			if (this.panelVigilancia.chckbxLicitaciones.isSelected()){
				System.out.println("buscarYMostrarLicitaciones");
				this.buscarYMostrarLicitaciones();
			}
			if (this.panelVigilancia.chckbxPatentes.isSelected()){
				System.out.println("buscarYMostrarPatentes");
				this.buscarYMostrarPatentes();
			}
			if (this.panelVigilancia.chckbxDocsWeb.isSelected()){
				System.out.println("buscarYMostrarDocsWeb");
				this.buscarYMostrarDocumentosWeb();
			}
			
			if (!(this.panelVigilancia.chckbxLicitaciones.isSelected() ||
				  this.panelVigilancia.chckbxPatentes.isSelected() ||
				  this.panelVigilancia.chckbxDocsWeb.isSelected())){
				JOptionPane.showMessageDialog(this.panelVigilancia, "Debe seleccionar al menos uno de los resultados", "No hay selección", JOptionPane.WARNING_MESSAGE);
			}
			else{
				this.panelVigilancia.framePrincipal.controller.verPanel(panelVigilancia.framePrincipal.panelVigilanciaResultados);
				this.panelVigilancia.revalidate();
				this.panelVigilancia.framePrincipal.getFrame().revalidate();
			}

		}
		
		if (e.getSource() == this.panelVigilancia.btnVerResultadosAnteriores) {
			this.panelVigilancia.framePrincipal.controller.verPanel(this.panelVigilancia.framePrincipal.panelVigilanciaResultados);
		}

		if (e.getSource() == this.pnPagTED.btnAnterior) {
			System.out.println("Botón Página Anterior en Licitaciones");
			this.indiceBusquedaTED -= 1;
			buscarYMostrarLicitacionesPorPagina();
			this.panelVigilancia.revalidate();
			this.panelVigilancia.framePrincipal.getFrame().revalidate();
		}

		if (e.getSource() == this.pnPagTED.btnSiguiente) {
			System.out.println("Botón Página Siguiente en Licitaciones");
			this.indiceBusquedaTED += 1;
			buscarYMostrarLicitacionesPorPagina();
			this.panelVigilancia.revalidate();
			this.panelVigilancia.framePrincipal.getFrame().revalidate();
		}

		if (e.getSource() == this.pnPagEPO.btnAnterior) {
			System.out.println("Botón Página Anterior en Patentes");
			this.indiceBusquedaEPO -= 100;
			buscarYMostrarPatentes();
			this.panelVigilancia.revalidate();
			this.panelVigilancia.framePrincipal.getFrame().revalidate();
		}

		if (e.getSource() == this.pnPagEPO.btnSiguiente) {
			System.out.println("Botón Página Siguiente en Patentes");
			this.indiceBusquedaEPO += 100;
			buscarYMostrarPatentes();
			this.panelVigilancia.revalidate();
			this.panelVigilancia.framePrincipal.getFrame().revalidate();
		}
		
		if (e.getSource() == this.pnPagDocsWeb.btnAnterior) {
			System.out.println("Botón Página Anterior en Documentos Web");
			this.indiceBusquedaDocumentosWeb -= 50;
			buscarYMostrarDocumentosWeb();
			this.panelVigilancia.revalidate();
			this.panelVigilancia.framePrincipal.getFrame().revalidate();
		}

		if (e.getSource() == this.pnPagDocsWeb.btnSiguiente) {
			System.out.println("Botón Página Siguiente en Documentos Web");
			this.indiceBusquedaDocumentosWeb += 50;
			buscarYMostrarDocumentosWeb();
			this.panelVigilancia.revalidate();
			this.panelVigilancia.framePrincipal.getFrame().revalidate();
		}
		
		if (e.getSource() == this.panelVigilancia.btnRevisarAvisos) {
			panelVigilancia.framePrincipal.controller.verPanel(panelVigilancia.framePrincipal.panelAvisosTecnologia);
		}

		if (e.getSource() == this.panelVigilancia.btnEditarListadoTecnologiasEmergentes) {
			panelVigilancia.framePrincipal.controller.verPanel(panelVigilancia.framePrincipal.panelEdicionTecnologiasEmergentes);
		}
		
		if (e.getSource().getClass().equals(javax.swing.JMenuItem.class))  {
			System.out.println("Buscar más como " + PopUpDocsWeb.textoSeleccionado);
			this.textoLibreCompleto += " " + PopUpDocsWeb.textoSeleccionado;
			this.buscarYMostrarDocumentosWeb();
		}
		
		this.panelVigilancia.framePrincipal.frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
		
	private String crearTextoLibreCompleto() {
		//Elaboración del textoLibre con ContrastarCon
		String textoLibreCompleto = this.panelVigilancia.tfTextoLibre.getText();
		
		Set<ContrastarCon> txt = this.panelVigilancia.dstdContrastarCon.getSeleccion();

//		if (this.panelVigilancia.dstdContrastarCon.getSeleccion() != null){
//			for(ContrastarCon entidad : this.panelVigilancia.dstdContrastarCon.getSeleccion())
//				textoLibreCompleto += " \"" + entidad.getNombre() +"\"";
//		}	

		System.out.println(textoLibreCompleto);
		
		return textoLibreCompleto;
	}

	private void buscarYMostrarLicitacionesPorPagina(){
		try {
			this.listaResultadosLicitaciones = Searcher.buscarLicitacionesEnLineaPorPagina(this.indiceBusquedaTED);
			mostrarLicitaciones();
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(panelVigilancia, "Error en la consulta online a TED", "Error", JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
			return;
		}
	}
	
	private void mostrarLicitaciones(){
		// Cambiamos el panel por el de resultados
		this.panelVigilancia.framePrincipal.panelVigilanciaResultados.borrarLicitaciones();
		this.panelVigilancia.framePrincipal.panelVigilanciaResultados.setResultadoLicitaciones(listaResultadosLicitaciones.size());
		Iterator<Licitacion> it = this.listaResultadosLicitaciones.iterator();
		while (it.hasNext()) {
			this.panelVigilancia.framePrincipal.panelVigilanciaResultados.addLicitacion(it.next());
		}
		int pagActual = this.indiceBusquedaTED;
		int pagTotal = Math.round(Searcher.totalResultadosTED/25);
		pnPagTED = new PanelPaginacionPatentes(pagActual, pagTotal, this);
		this.panelVigilancia.framePrincipal.panelVigilanciaResultados.panelLicitaciones.add(pnPagTED);
	}
	
	private void buscarYMostrarLicitaciones(){
		try {
//			this.listaResultadosLicitaciones = Searcher.buscarLicitacionesEnLinea(this.textoLibreCompleto, (GregorianCalendar) this.panelVigilancia.dpFechaDesde.getModel().getValue(), (GregorianCalendar) this.panelVigilancia.dpFechaHasta.getModel().getValue(), this.panelVigilancia.dstdLicitacionLocalizacion.getSeleccion(), (String) this.panelVigilancia.cbTipoLicitacion.getSelectedItem(),
//					this.panelVigilancia.jtfEntidadEmisora.getText(), this.panelVigilancia.dstdLicitacionSector.getSeleccion());
			this.listaResultadosLicitaciones = Searcher.buscarLicitacionesEnLineaModoExperto(this.textoLibreCompleto, (GregorianCalendar) this.panelVigilancia.dpFechaDesde.getModel().getValue(), (GregorianCalendar) this.panelVigilancia.dpFechaHasta.getModel().getValue(), this.panelVigilancia.dstdLicitacionLocalizacion.getSeleccion(), (String) this.panelVigilancia.cbTipoLicitacion.getSelectedItem(),
					this.panelVigilancia.jtfEntidadEmisora.getText(), this.panelVigilancia.dstdLicitacionSector.getSeleccion(),
					this.panelVigilancia.dstdContrastarCon.getSeleccion());
			mostrarLicitaciones();
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(panelVigilancia, "Error en la consulta online a TED", "Error", JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		}	
	}

	private void buscarYMostrarPatentes() {
		try {
			this.listaResultadosPatentes = Searcher.buscarPatentesEnLinea(this.textoLibreCompleto, (GregorianCalendar) this.panelVigilancia.dpPatentesFechaDesde.getModel().getValue(), (GregorianCalendar) this.panelVigilancia.dpPatentesFechaHasta.getModel().getValue(), this.panelVigilancia.tfInventor.getText(), this.panelVigilancia.tfSolicitante.getText(), this.panelVigilancia.dstdPatenteSector.getSeleccion(), this.panelVigilancia.dsldPatenteLocalizacion.getSeleccion(), this.indiceBusquedaEPO, this.panelVigilancia.dstdContrastarCon.getSeleccion());
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this.panelVigilancia, "Se ha producido un error en la consulta.\nPor favor, replantéela.", "ERROR EN LA CONSULTA", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
			this.panelVigilancia.framePrincipal.frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			return;
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(this.panelVigilancia, "El número de códigos de CPI es excesivo (máximo 10).\nPor favor, redúzcalos y replantée la consulta.", "ERROR EN LA CONSULTA", JOptionPane.ERROR_MESSAGE);
			this.panelVigilancia.framePrincipal.frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			return;
		}
		this.panelVigilancia.framePrincipal.panelVigilanciaResultados.borrarPatentes();
		this.panelVigilancia.framePrincipal.panelVigilanciaResultados.setResultadoPatentes(listaResultadosPatentes.size());
		Iterator<Patente> itPat = this.listaResultadosPatentes.iterator();
		while (itPat.hasNext()) {
			this.panelVigilancia.framePrincipal.panelVigilanciaResultados.addPatente(itPat.next());
		}
		int pagActual = Math.round(this.indiceBusquedaEPO/99);
		int pagTotal = Math.round(Searcher.totalResultadosEPO/99);
		pnPagEPO = new PanelPaginacionPatentes(pagActual, pagTotal, this);
		this.panelVigilancia.framePrincipal.panelVigilanciaResultados.panelPatentes.add(pnPagEPO);

	}

	private void buscarYMostrarDocumentosWeb(){
		try {
			this.listaResultadosDocumentosWeb= Searcher.buscarDocumentosWeb(this.textoLibreCompleto, this.panelVigilancia.dstdDocsWebLocalizacion.getSeleccion(), this.panelVigilancia.dstdDocsWebSector.getSeleccion(), this.panelVigilancia.dstdDocsWebTipoOrganizacion.getSeleccion(), this.indiceBusquedaDocumentosWeb, this.panelVigilancia.dstdContrastarCon.getSeleccion());
			mostrarDocumentosWeb();
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(panelVigilancia, "Error en la consulta de Documentos Web", "Error", JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		}	
	}
	
	private void mostrarDocumentosWeb(){
		// Cambiamos el panel por el de resultados
		this.panelVigilancia.framePrincipal.controller.verPanel(panelVigilancia.framePrincipal.panelVigilanciaResultados);
		this.panelVigilancia.framePrincipal.panelVigilanciaResultados.borrarDocumentosWeb();
		this.panelVigilancia.framePrincipal.panelVigilanciaResultados.setResultadoDocumentosWeb(listaResultadosDocumentosWeb.size());
		Iterator<DocumentoWeb> it = this.listaResultadosDocumentosWeb.iterator();
		while (it.hasNext()) {
			this.panelVigilancia.framePrincipal.panelVigilanciaResultados.addDocumentoWeb(it.next(),this);
		}
		int pagActual = this.indiceBusquedaDocumentosWeb;
		int pagTotal = -1;
		pnPagDocsWeb = new PanelPaginacionPatentes(pagActual, pagTotal, this);
		this.panelVigilancia.framePrincipal.panelVigilanciaResultados.panelDocumentosWeb.add(pnPagDocsWeb);
	}
	
	@Override
	public void onAceptar(DelphosSelectionDialog dtd) {
		System.out.println("Aceptar");

		if (dtd == this.panelVigilancia.dstdLicitacionSector) {
			licitacionSectores = dtd.getSeleccion();
			this.panelVigilancia.lblLicitacionSectores.setText(dtd.getTextoSeleccion());
		}
		
		if (dtd == this.panelVigilancia.dstdLicitacionLocalizacion) {
			licitacionLocalizacion = dtd.getSeleccion();
			this.panelVigilancia.lblLicitacionLocalizacion.setText(dtd.getTextoSeleccion());
		}
		
		if (dtd == this.panelVigilancia.dsldPatenteLocalizacion) {
			patenteLocalizacion = dtd.getSeleccion();
			this.panelVigilancia.lblPatenteLocalizacion.setText(dtd.getTextoSeleccion());
		}

		if (dtd == this.panelVigilancia.dstdPatenteSector) {
			patenteSectores = dtd.getSeleccion();
			this.panelVigilancia.lblPatenteSectores.setText(dtd.getTextoSeleccion());
		}
		
		if (dtd == this.panelVigilancia.dstdDocsWebLocalizacion) {
			docsWebLocalizacion = dtd.getSeleccion();
			this.panelVigilancia.lblDocsWebLocalizacion.setText(dtd.getTextoSeleccion());
		}
		
		if (dtd == this.panelVigilancia.dstdDocsWebSector) {
			docsWebSector = dtd.getSeleccion();
			this.panelVigilancia.lblDocsWebSector.setText(dtd.getTextoSeleccion());
		}
		
		if (dtd == this.panelVigilancia.dstdDocsWebTipoOrganizacion) {
			docsWebTipoOrganizacion = dtd.getSeleccion();
			this.panelVigilancia.lblDocsWebTipoOrganizacion.setText(dtd.getTextoSeleccion());
		}
		
		if (dtd == this.panelVigilancia.dstdContrastarCon) {
			setTipoOrganizacion = dtd.getSeleccion();
			//this.panelVigilancia.lblDocsWebTipoOrganizacion.setText(dtd.getTextoSeleccion());
		}
	}

	@Override
	public void onCancelar(DelphosSelectionDialog dtd) {

	}

	@Override
	public void onBorrar(DelphosSelectionDialog dtd) {

	}

	@Override
	public void onCrear(DelphosSelectionDialog dtd) {

	}

	@Override
	 public void mousePressed(MouseEvent e){
        if (e.isPopupTrigger())
            doPop(e);
    }

    public void mouseReleased(MouseEvent e){
        if (e.isPopupTrigger())
            doPop(e);
    }

    private void doPop(MouseEvent e){
    	//Obtenemos el texto seleccionado
    	String textoSeleccionado = ((JTextArea)e.getSource()).getSelectedText();
    	System.out.println(textoSeleccionado);
        PopUpDocsWeb jPopupMenu = new PopUpDocsWeb(this, textoSeleccionado);
        jPopupMenu.show(e.getComponent(), e.getX(), e.getY());
    }

	@Override
	public void mouseClicked(MouseEvent e) {
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
}

