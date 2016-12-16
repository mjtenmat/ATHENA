package delphos.iu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.hibernate.Query;

import delphos.AvisoTecnologiasEmergentes;

public class PanelAvisosTecnologia extends JPanel implements ConDefaultButton{
	protected DelphosFrame framePrincipal;
	protected JButton btnVolver;
	JPanel panelContenido;
	
	public PanelAvisosTecnologia(DelphosFrame framePrincipal) {
		this.framePrincipal = framePrincipal;
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane);
		
		panelContenido = new JPanel();
		scrollPane.setViewportView(panelContenido);
		panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
		
		JLabel lblAvisos = new JLabel("Avisos");
		add(lblAvisos, BorderLayout.NORTH);
		lblAvisos.setBackground(Color.ORANGE);
		lblAvisos.setOpaque(true);
		lblAvisos.setHorizontalAlignment(SwingConstants.CENTER);
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		add(panel, BorderLayout.SOUTH);
		
		btnVolver = new JButton("Volver");
		btnVolver.addActionListener(framePrincipal.controller);
		panel.add(btnVolver);
		
		this.cargarAvisos();
	}

	protected void cargarAvisos(){
		String sql = "SELECT id, termino, tipo, titulo, extracto, url, revisado FROM AvisoTecnologiasEmergentes ORDER BY termino ASC";
		Query query = Delphos.getSession().createSQLQuery(sql);
		List list = query.list();
		Iterator it = list.iterator();
		String palabraActual = "";
		this.panelContenido.removeAll();
		while (it.hasNext()){
			Object[] tupla = (Object[])it.next();
			if (!palabraActual.equals(tupla[1].toString())){
				JLabel lblPalabra = new JLabel("<html><font color=\"blue\">" + tupla[1].toString()+": </font></html>");
				JPanel subPanel = new JPanel();
				subPanel.setPreferredSize(new Dimension(500, 25));
				subPanel.setMaximumSize(new Dimension(2000, 25));
				subPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
				subPanel.add(lblPalabra);
				if (palabraActual != ""){
					subPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
					panelContenido.add(Box.createRigidArea(new Dimension(500, 20)));
				}
				panelContenido.add(subPanel);
				
				palabraActual = tupla[1].toString();
			}
			AvisoTecnologiasEmergentes aviso = new AvisoTecnologiasEmergentes();
			aviso.setId((int)tupla[0]);
			aviso.setTermino(tupla[1].toString());
			aviso.setTipo(tupla[2].toString());
			if (tupla[3] != null)
				aviso.setTitulo(tupla[3].toString());
			if (tupla[4] != null)
				aviso.setExtracto(tupla[4].toString());
			aviso.setUrl(tupla[5].toString());
			aviso.setRevisado((Byte)tupla[6] != 0);
			panelContenido.add(new PanelAvisoTecnologia(aviso, this));
		}
		panelContenido.add(Box.createVerticalGlue());
		this.revalidate();
	}

	@Override
	public JButton getDefaultButton() {
		return btnVolver;
	}
	
}
