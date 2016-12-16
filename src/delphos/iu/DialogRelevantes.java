package delphos.iu;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class DialogRelevantes extends JDialog{
	static final long serialVersionUID = 1L;
	protected PanelBusquedaController controller;
	private JScrollPane panelScroll;
	private JButton btnCancelar;
	private JButton btnBorrar;
	private JButton btnAceptar;
	private JPanel panelRelevantes;

	
	public DialogRelevantes(){
		this.setTitle("Fuentes Relevantes");
		this.setBounds(50, 500, 800, 700);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		panelRelevantes = new JPanel();
		panelRelevantes.setLayout(new BoxLayout(panelRelevantes, BoxLayout.Y_AXIS));
		getContentPane().add(panelRelevantes, BorderLayout.NORTH);
		panelScroll = new JScrollPane(panelRelevantes);
		panelScroll.setBorder(null);
		this.getContentPane().add(panelScroll);
				
		this.pack();   
        this.setLocationByPlatform(true);
	}

	public JPanel getPanel(){
		return panelRelevantes;
	}
	
	public void setController(PanelBusquedaController controller) {
		this.controller = controller;
	}
	
}
