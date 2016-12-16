package delphos.iu;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JButton;

import java.awt.BorderLayout;

public class PanelAltaFuente extends JPanel implements ConDefaultButton{
	protected DelphosFrame framePrincipal;
	protected PanelAltaFuenteController controller;
	protected JTextField tfUrl;
	protected JButton btnAceptar;

	public PanelAltaFuente(DelphosFrame framePrincipal) {
		this.framePrincipal = framePrincipal;
		this.controller = new PanelAltaFuenteController(this);
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		add(panel_2, BorderLayout.NORTH);
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.Y_AXIS));
		
		JLabel lblNewLabel = new JLabel("Alta de Fuente");
		panel_2.add(lblNewLabel);
		
		JPanel panel = new JPanel();
		panel_2.add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel_1 = new JLabel("URL (completa): ");
		panel.add(lblNewLabel_1);
		
		tfUrl = new JTextField();
		tfUrl.setToolTipText("http://www.example.com/dir/pag.html");
		panel.add(tfUrl);
		tfUrl.setColumns(10);
		
		JPanel panel_1 = new JPanel();
		panel_2.add(panel_1);
		
		JButton btnCancelar = new JButton("Cancelar");
		btnCancelar.setName("btnCancelar");
		btnCancelar.addActionListener(controller);
		panel_1.add(btnCancelar);
		
		btnAceptar = new JButton("Aceptar");
		btnAceptar.setName("btnAceptar");
		btnAceptar.addActionListener(controller);
		panel_1.add(btnAceptar);
		
		this.setVisible(false);
	}

	public String getUrl() {
		return tfUrl.getText();
	}

	@Override
	public JButton getDefaultButton() {
		return btnAceptar;
	}

}
