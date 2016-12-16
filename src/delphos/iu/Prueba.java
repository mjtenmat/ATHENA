package delphos.iu;

import java.awt.BorderLayout;

import javax.swing.JFrame;

public class Prueba extends JFrame{
	
	public static void main(String[] args){
		new Prueba();
	}
	
	public Prueba(){
		this.setTitle("Pruebas");
		getContentPane().add(new PanelATPeriodo());
		this.pack();
		this.setVisible(true);
	}

}
