package delphos.iu;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionListener;
import javax.swing.JButton;

public class PanelPaginacion {
	protected JPanel panel;
	protected BasicArrowButton btnAnterior;
	protected JLabel jlbPagina;
	protected BasicArrowButton btnSiguiente;
	private JButton btnAG;
	private boolean ultimaPagina = false;
	
	public PanelPaginacion(int pagina, int total, ActionListener controller){
		panel = new JPanel();
		panel.setBackground(Color.WHITE);

		btnAnterior = new BasicArrowButton(SwingConstants.WEST);
		btnAnterior.setToolTipText("Página Anterior");
		btnAnterior.setName("btnAnterior");
		if (pagina == 0)
			btnAnterior.setEnabled(false);
		btnAnterior.addActionListener(controller);
		panel.add(btnAnterior);

		jlbPagina = new JLabel();
		jlbPagina.setFont(new Font("Dialog", Font.PLAIN, 10));
		jlbPagina.setText(" " + (pagina + 1) + " de " + total + " ");
		panel.add(jlbPagina);
		
		btnSiguiente = new BasicArrowButton(SwingConstants.EAST);
		btnSiguiente.setToolTipText("Página Siguiente");
		btnSiguiente.setName("btnSiguiente");
		btnSiguiente.addActionListener(controller);
		panel.add(btnSiguiente);
		if (pagina == total -1){
			btnSiguiente.setEnabled(false);
			btnAG = new JButton("AG");
			btnAG.setVisible(false);
			btnAG.setName("btnAG");
			btnAG.addActionListener(controller);
			panel.add(btnAG);
			ultimaPagina = true;
		}
	}
	
	public void activarAG(boolean val){
		if (ultimaPagina)
			btnAG.setVisible(val);
	}
	
	public JPanel getPanel() {
		return panel;
	}
}
