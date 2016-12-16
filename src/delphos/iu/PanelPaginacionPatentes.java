package delphos.iu;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionListener;
import javax.swing.JButton;


//TODO: Unificar esta clase con PanelPaginacion
public class PanelPaginacionPatentes extends JPanel{
	protected BasicArrowButton btnAnterior;
	protected JLabel jlbPagina;
	protected BasicArrowButton btnSiguiente;
	private boolean ultimaPagina = false;
	
	public PanelPaginacionPatentes(int pagina, int total, ActionListener controller){
		this.setBackground(Color.WHITE);

		btnAnterior = new BasicArrowButton(SwingConstants.WEST);
		btnAnterior.setToolTipText("Página Anterior");
		btnAnterior.setName("btnAnterior");
		if (pagina == 0)
			btnAnterior.setEnabled(false);
		btnAnterior.addActionListener(controller);
		this.add(btnAnterior);

		jlbPagina = new JLabel();
		jlbPagina.setFont(new Font("Dialog", Font.PLAIN, 10));
		if(total > 0)
			jlbPagina.setText(" " + (pagina + 1) + " de " + total + " ");
		else
			jlbPagina.setText(" " + (pagina + 1) + " ");
		this.add(jlbPagina);
		
		btnSiguiente = new BasicArrowButton(SwingConstants.EAST);
		btnSiguiente.setToolTipText("Página Siguiente");
		btnSiguiente.setName("btnSiguiente");
		btnSiguiente.addActionListener(controller);
		this.add(btnSiguiente);
	}
		
}
