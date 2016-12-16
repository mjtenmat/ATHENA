package delphos.iu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

public class DelphosFrameController implements ActionListener{
	protected DelphosFrame framePrincipal;
	
	public DelphosFrameController(DelphosFrame framePrincipal){
		this.framePrincipal = framePrincipal;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println("Pulsado menú");
		if (e.getSource() == framePrincipal.miBuscador){
			this.verPanel(framePrincipal.panelBusqueda);
			framePrincipal.panelBusqueda.jtfConsulta.requestFocus();
		}
		if (e.getSource() == framePrincipal.miVigilancia){
			framePrincipal.panelVigilancia.actualizarNumAvisosPendientes();
			this.verPanel(framePrincipal.panelVigilancia);
			framePrincipal.panelVigilancia.tfTextoLibre.requestFocus();
		}
		if (e.getSource() == framePrincipal.miTendencias){
			framePrincipal.panelTendencias.actualizarNumAvisosPendientes();
			this.verPanel(framePrincipal.panelTendencias);
			//framePrincipal.panelTendencias.tfTextoLibre.requestFocus();
		}
		if (e.getSource() == framePrincipal.miFuentesAlta){
			this.verPanel(framePrincipal.panelAltaFuente);
			framePrincipal.panelAltaFuente.tfUrl.requestFocus();
		}
		if (e.getSource() == framePrincipal.miHosts){
			this.verPanel(framePrincipal.panelHosts);
			framePrincipal.panelHosts.tfHost.requestFocus();
		}
		if (e.getSource() == framePrincipal.panelAvisosTecnologia.btnVolver){
			framePrincipal.panelVigilancia.actualizarNumAvisosPendientes();
			this.verPanel(framePrincipal.panelVigilancia);
		}
		if (e.getSource() == framePrincipal.panelAvisosTendencia.btnVolver){
			framePrincipal.panelTendencias.actualizarNumAvisosPendientes();
			this.verPanel(framePrincipal.panelTendencias);
		}
	}
	
	public void verPanel(JPanel panel){
		
		if (panel == framePrincipal.panelVigilancia)
			framePrincipal.panelVigilancia.actualizarNumAvisosPendientes();
		
		framePrincipal.panelBusqueda.setVisible(false);
		framePrincipal.panelVigilancia.setVisible(false);
		framePrincipal.panelAltaFuente.setVisible(false);
		framePrincipal.panelHosts.setVisible(false);
		framePrincipal.panelVigilanciaResultados.setVisible(false);
		framePrincipal.panelAvisosTecnologia.setVisible(false);
		framePrincipal.panelAvisosTendencia.setVisible(false);
		framePrincipal.panelTendencias.setVisible(false);
		framePrincipal.panelEdicionTecnologiasEmergentes.setVisible(false);
		
		framePrincipal.frame.getRootPane().setDefaultButton(((ConDefaultButton)panel).getDefaultButton());
		panel.setVisible(true);
	}

}
