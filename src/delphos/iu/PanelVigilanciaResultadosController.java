package delphos.iu;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import delphos.DocumentoWeb;

public class PanelVigilanciaResultadosController implements ActionListener {
	protected PanelVigilanciaResultados panelVigilanciaResultados;
	protected DialogRelevantes dlgRelevantes;
	protected Set<DocumentoWeb> setResultadosRelevantesDocumentosWeb;

	public PanelVigilanciaResultadosController(PanelVigilanciaResultados panelVigilanciaResultados) {

		this.panelVigilanciaResultados = panelVigilanciaResultados;
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

		panelVigilanciaResultados.framePrincipal.frame.setCursor(Cursor.getDefaultCursor());
	}

}
