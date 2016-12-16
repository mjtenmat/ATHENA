package delphos.iu;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import delphos.DocumentoWeb;
import delphos.Licitacion;
import delphos.Patente;
import delphos.Searcher;

public class PanelVigilanciaResultados extends JPanel implements ConDefaultButton{

	protected DelphosFrame framePrincipal;
	protected PanelVigilanciaResultadosController controller;
	protected PanelVigilanciaController controllerVigilancia;
	protected JButton btnVolver;
	private JTabbedPane panelTabs;
	protected JPanel panelLicitaciones;
	protected JPanel panelPatentes;
	protected JPanel panelDocumentosWeb;
	private JPanel panelDocs;

	public PanelVigilanciaResultados(DelphosFrame framePrincipal) {
		this.framePrincipal = framePrincipal;
		this.controller = new PanelVigilanciaResultadosController(this);
		this.setAlignmentX(LEFT_ALIGNMENT);
		this.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.btnVolver = new JButton("Volver");
		this.btnVolver.addActionListener(this.controller);
		this.add(btnVolver);
		crearTabs();
	}
	
	private void crearTabs(){
		this.panelTabs = new JTabbedPane();
		
		panelLicitaciones = new JPanel();
		panelLicitaciones.setLayout(new BoxLayout(panelLicitaciones, BoxLayout.Y_AXIS));
		panelLicitaciones.setAlignmentX(LEFT_ALIGNMENT);
		panelLicitaciones.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		JScrollPane panelScrollLicitaciones = new JScrollPane(panelLicitaciones);
		this.panelTabs.addTab("Licitaciones", new ImageIcon(Delphos.class.getClassLoader().getResource("res/licitaciones.png")), panelScrollLicitaciones, "Concursos públicos");
		//this.panelTabs.addTab("Licitaciones", panelScrollLicitaciones);
		
		panelPatentes = new JPanel();
		panelPatentes.setLayout(new BoxLayout(panelPatentes, BoxLayout.Y_AXIS));
		panelPatentes.setAlignmentX(LEFT_ALIGNMENT);
		panelPatentes.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		JScrollPane panelScrollPatentes = new JScrollPane(panelPatentes);
		this.panelTabs.addTab("Patentes", new ImageIcon(Delphos.class.getClassLoader().getResource("res/patentes.png")), panelScrollPatentes, "Patentes");
		//this.panelTabs.addTab("Patentes", panelScrollPatentes);
		
		panelDocumentosWeb = new JPanel();
		panelDocumentosWeb.setLayout(new BoxLayout(panelDocumentosWeb, BoxLayout.Y_AXIS));
		panelDocumentosWeb.setAlignmentX(LEFT_ALIGNMENT);
		panelDocumentosWeb.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		JScrollPane panelScrollDocumentosWeb = new JScrollPane(panelDocumentosWeb);
		this.panelTabs.addTab("Documentos Web", new ImageIcon(Delphos.class.getClassLoader().getResource("res/docsweb.png")), panelScrollDocumentosWeb, "Documentos Web");
		//this.panelTabs.addTab("Patentes", panelScrollPatentes);
		
		panelDocs = new JPanel();
		JScrollPane panelScrollDocs = new JScrollPane(panelDocs);
		this.panelTabs.addTab("Docs.Académicos", new ImageIcon(Delphos.class.getClassLoader().getResource("res/docs.png")), panelScrollDocs, "Tesis, Artículos, Congresos...");
		//this.panelTabs.addTab("Docs. Académicos", panelScrollDocs);

		add(panelTabs);
	}
	public void borrarLicitaciones(){
		this.panelLicitaciones.removeAll();
	}
	public void borrarPatentes(){
		this.panelPatentes.removeAll();
	}
	public void borrarDocumentosWeb(){
		this.panelDocumentosWeb.removeAll();
	}
	public void setResultadoLicitaciones(int numLicitaciones){
		this.panelLicitaciones.add(new JLabel("Se muestran " + numLicitaciones + " licitaciones de un total de " + Searcher.totalResultadosTED));
	}
	public void setResultadoPatentes(int numPatentes){
		this.panelPatentes.add(new JLabel("Se muestran " + numPatentes + " patentes de un total de " + Searcher.totalResultadosEPO));
	}
	public void setResultadoDocumentosWeb(int numDocsWeb){
		this.panelDocumentosWeb.add(new JLabel("Se muestran " + numDocsWeb + " documentos web:"));
	}
	public void addLicitacion(Licitacion licitacion){
		this.panelLicitaciones.add(new PanelLicitacion(licitacion));
	}
	public void addPatente(Patente patente){
		this.panelPatentes.add(new PanelPatente(patente));
	}
	public void addDocumentoWeb(DocumentoWeb docWeb, PanelVigilanciaController controllerVigilancia){
		this.controllerVigilancia = controllerVigilancia;
		this.panelDocumentosWeb.add(new PanelDocumentoWeb(docWeb,controllerVigilancia));
	}

	@Override
	public JButton getDefaultButton() {
		return btnVolver;
	}

}
