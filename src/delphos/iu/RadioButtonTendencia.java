package delphos.iu;

import javax.swing.JRadioButton;

import delphos.Tendencia;

public class RadioButtonTendencia extends JRadioButton {
	private Tendencia tendencia;
	
	public RadioButtonTendencia(Tendencia tendencia){
		this.tendencia = tendencia;
		StringBuilder sbTexto = new StringBuilder(tendencia.getTerminoPrincipal());
		if (tendencia.getFiltroPrincipalLicitaciones() != null)
			sbTexto.append(" - FP Licitaciones: " + tendencia.getFiltroPrincipalLicitaciones());
		if (tendencia.getFiltroPrincipalPatentes() != null)
			sbTexto.append(" - FP Patentes: " + tendencia.getFiltroPrincipalPatentes());
		this.setText(sbTexto.toString());
	}

	public Tendencia getTendencia() {
		return tendencia;
	}

	public void setTendencia(Tendencia tendencia) {
		this.tendencia = tendencia;
	}

}
