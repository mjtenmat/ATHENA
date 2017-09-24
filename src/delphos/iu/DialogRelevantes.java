package delphos.iu;

import java.awt.BorderLayout;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import delphos.DocumentoWeb;
import delphos.Resultado;

public class DialogRelevantes extends JDialog{
	static final long serialVersionUID = 1L;
	protected PanelVigilanciaResultadosController controller;
	private JScrollPane panelScroll;
	private JButton btnCancelar;
	private JButton btnBorrar;
	private JButton btnAceptar;
	private JPanel panelRelevantes;
	private PanelVigilanciaResultadosController controlador;
	
	private Set<DocumentoWeb> setResultadosRelevantesDocumentosWeb;
	
	public DialogRelevantes(PanelVigilanciaResultadosController controlador){
		this.controlador = controlador;
		this.setTitle("Documentos Relevantes");
		this.setBounds(50, 500, 800, 700);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		panelRelevantes = new JPanel();
		panelRelevantes.setLayout(new BoxLayout(panelRelevantes, BoxLayout.Y_AXIS));
		getContentPane().add(panelRelevantes, BorderLayout.NORTH);
		panelScroll = new JScrollPane(panelRelevantes);
		panelScroll.setBorder(null);
		this.getContentPane().add(panelScroll);
				
		this.pack();   
        //this.setLocationByPlatform(true);
	}

	public JPanel getPanel(){
		return panelRelevantes;
	}
	
	public void setController(PanelVigilanciaResultadosController panelVigilanciaResultadosController) {
		this.controller = panelVigilanciaResultadosController;
	}
	
	public void actualizar(Set<DocumentoWeb> setResultadosRelevantesDocumentosWeb) {
		this.setResultadosRelevantesDocumentosWeb = setResultadosRelevantesDocumentosWeb;
		this.panelRelevantes.removeAll();
		Iterator<DocumentoWeb> it = this.setResultadosRelevantesDocumentosWeb.iterator();
		while(it.hasNext()) {
			panelRelevantes.add(new PanelDocumentoWeb(it.next(), controlador, true));
		}

		this.pack();
		//this.setVisible(true);
	}
	
}
