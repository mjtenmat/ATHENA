package delphos.iu;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.hibernate.Query;

import delphos.AvisoTecnologiasEmergentes;
import java.awt.BorderLayout;

public class PanelAvisoTecnologia  extends JPanel implements ActionListener{
	protected JCheckBox chckbxRevisado;
	protected JCheckBox chckbxEliminar;
	private AvisoTecnologiasEmergentes aviso;
	private PanelAvisosTecnologia panelAvisosTecnologia;
	
	public PanelAvisoTecnologia(AvisoTecnologiasEmergentes aviso, PanelAvisosTecnologia panelAvisosTecnologia) {
		this.aviso = aviso;
		this.panelAvisosTecnologia = panelAvisosTecnologia;
		
		this.setPreferredSize(new Dimension(500, 112));
		this.setMinimumSize(new Dimension(500, 120));
		//this.setMaximumSize(new Dimension(2000, 100));
		
		JLabel lblTipo = new JLabel(aviso.getTipo()+": ");
		
		JLabel lblTtulo = new JLabel(aviso.getTitulo());
		
		JLabel lblUrl = new JLabel(aviso.getUrl());
		lblUrl.setForeground(Color.GREEN);
		lblUrl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lblUrl.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblUrl.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				try {
					// open the default web browser for the HTML page
					Desktop desktop = Desktop.getDesktop();
					if ((desktop.isSupported(Desktop.Action.BROWSE))) {
						Desktop.getDesktop().browse(
								java.net.URI.create(((JLabel) e.getSource())
										.getText()));
					}
					else{
						ProcessBuilder pb = new ProcessBuilder("/usr/bin/firefox", ((JLabel) e.getSource()).getText());
						Process p = pb.start();
					}
				}
				catch (Exception exc) {
					System.out.println("Error abriendo página web. "
							+ exc.getMessage());
				}
			}
		});
		
		chckbxRevisado = new JCheckBox("Revisado");
		chckbxRevisado.setSelected(aviso.isRevisado());
		chckbxRevisado.addActionListener(this);
		
		chckbxEliminar = new JCheckBox("Eliminar");
		chckbxEliminar.addActionListener(this);
		
		JTextPane taExtracto = new JTextPane();
		taExtracto.setText(aviso.getExtracto());
		taExtracto.setMaximumSize(new Dimension(1000, 30));
		taExtracto.setBackground(new Color(238, 238, 238));
		taExtracto.setEditable(false);
		taExtracto.setFont(new Font("Dialog", Font.PLAIN, 12));
		
		JLabel lblFecha = new JLabel(aviso.getFecha().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(24)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblTipo)
								.addComponent(lblFecha))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(lblUrl, GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
									.addGap(199))
								.addComponent(lblTtulo, GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(101)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(taExtracto, GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(chckbxRevisado)
									.addGap(18)
									.addComponent(chckbxEliminar)))))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblTipo)
						.addComponent(lblTtulo))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblUrl, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblFecha))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(chckbxRevisado)
						.addComponent(chckbxEliminar))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(taExtracto, GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
					.addContainerGap())
		);
		setLayout(groupLayout);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.chckbxRevisado){
			String sql = "UPDATE AvisoTecnologiasEmergentes SET revisado = ";
			if (this.chckbxRevisado.isSelected())
				sql += "1 ";
			else 
				sql += "0 ";
			sql += "WHERE id = " + this.aviso.getId();
			Query query = Delphos.getSession().createSQLQuery(sql);
			Delphos.getSession().beginTransaction();
			query.executeUpdate();
			Delphos.getSession().getTransaction().commit();
		}
		if (e.getSource() == this.chckbxEliminar){
			if (JOptionPane.showConfirmDialog(this, "¿Realmente quiere ELIMINAR este aviso?", "Confirmación", JOptionPane.OK_CANCEL_OPTION ) == JOptionPane.OK_OPTION){
				String sql = "DELETE FROM AvisoTecnologiasEmergentes WHERE id = " + this.aviso.getId();
				Query query = Delphos.getSession().createSQLQuery(sql);
				Delphos.getSession().beginTransaction();
				query.executeUpdate();
				Delphos.getSession().getTransaction().commit();
				//this.setVisible(false);
				this.panelAvisosTecnologia.cargarAvisos();
			}
			else{
				this.chckbxEliminar.setSelected(false);
			}
		}
		
	}
}
