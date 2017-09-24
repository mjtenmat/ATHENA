package delphos.iu;

import java.awt.Frame;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;

import delphos.DocumentoAcademico;
import delphos.DocumentoWeb;
import delphos.Licitacion;
import delphos.Patente;
import delphos.Searcher;

public class PanelVigilanciaResultados extends JPanel implements ConDefaultButton{

	private static final long serialVersionUID = 1L;
	protected DelphosFrame framePrincipal;
	protected PanelVigilanciaResultadosController controller;
	protected PanelVigilanciaController controllerVigilancia;
	protected JButton btnVolver;
	protected JButton btnRR;
	protected JToggleButton btnVerRelevantes;
	private JTabbedPane panelTabs;
	protected JPanel panelLicitaciones;
	protected JPanel panelPatentes;
	protected JPanel panelDocumentosWeb;
	protected JPanel panelDocsAcademicos;

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
		
		panelDocsAcademicos = new JPanel();
		panelDocsAcademicos.setLayout(new BoxLayout(panelDocsAcademicos, BoxLayout.Y_AXIS));
		panelDocsAcademicos.setAlignmentX(LEFT_ALIGNMENT);
		panelDocsAcademicos.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		JScrollPane panelScrollDocs = new JScrollPane(panelDocsAcademicos);
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
		JPanel panelHorizontal = new JPanel();
		panelHorizontal.setLayout(new BoxLayout(panelHorizontal, BoxLayout.X_AXIS));
		panelHorizontal.setAlignmentX(LEFT_ALIGNMENT);
		this.btnVerRelevantes = new JToggleButton("Relevantes");
		this.btnVerRelevantes.addActionListener(this.controller);
		panelHorizontal.add(this.btnVerRelevantes);
		this.btnRR = new JButton("RR");
		this.btnRR.addActionListener(this.controller);
		panelHorizontal.add(this.btnRR);
		panelHorizontal.add(new JLabel(" Se muestran " + numDocsWeb + " documentos web:"));
		this.panelDocumentosWeb.add(panelHorizontal);
	}
	public void addLicitacion(Licitacion licitacion){
		this.panelLicitaciones.add(new PanelLicitacion(licitacion));
	}
	public void addPatente(Patente patente){
		this.panelPatentes.add(new PanelPatente(patente));
	}
	public void addDocumentoWeb(DocumentoWeb docWeb, PanelVigilanciaController controllerVigilancia){
		this.controllerVigilancia = controllerVigilancia;
		this.panelDocumentosWeb.add(new PanelDocumentoWeb(docWeb,controller, false));
	}

	@Override
	public JButton getDefaultButton() {
		return btnVolver;
	}

	public void borrarDocumentosAcademicos() {
		this.panelDocsAcademicos.removeAll();
	}

	public void setResultadoDocumentosAcademicos(int numDocsAcademicos) {
		this.panelDocumentosWeb.add(new JLabel("Se muestran " + numDocsAcademicos + " documentos académicos:"));
	}

	public void addDocumentoAcademico(DocumentoAcademico docAcademico, PanelVigilanciaController panelVigilanciaController, JPanel panel) {
		this.controllerVigilancia = controllerVigilancia;
		if (panel != null)
			panel.add(new PanelDocumentoAcademico(docAcademico,controllerVigilancia));
		else
			this.panelDocsAcademicos.add(new PanelDocumentoAcademico(docAcademico,controllerVigilancia));
	}
	
}
