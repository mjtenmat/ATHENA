package delphos.iu;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PanelVigilanciaResultadosController implements ActionListener{
	private PanelVigilanciaResultados panelVigilanciaResultados;
	
	public PanelVigilanciaResultadosController(
			PanelVigilanciaResultados panelVigilanciaResultados) {
		
		this.panelVigilanciaResultados = panelVigilanciaResultados;
		
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		panelVigilanciaResultados.framePrincipal.frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		if (ae.getSource() == panelVigilanciaResultados.btnVolver){
			panelVigilanciaResultados.framePrincipal.controller.verPanel(panelVigilanciaResultados.framePrincipal.panelVigilancia);
		}
		panelVigilanciaResultados.framePrincipal.frame.setCursor(Cursor.getDefaultCursor());
	}

}
