package delphos.iu;

import javax.swing.JCheckBox;

import delphos.Resultado;

public class JCheckBoxResultadoRelevante extends JCheckBox{
	static final long serialVersionUID = 1L;
	private Resultado resultado;

	public JCheckBoxResultadoRelevante(Resultado resultado){
		super();
		this.resultado = resultado;
	}
	
	public void marcarComoRelevante(){
		this.resultado.setRelevante(true);
	}
	public void marcarComoNoRelevante(){
		this.resultado.setRelevante(false);
	}
	
	public Resultado getResultado(){
		return this.resultado;
	}
}
