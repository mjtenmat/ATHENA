package delphos.iu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.SwingConstants;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.AbstractListModel;

public class PanelEdicionTecnologiasEmergentes extends JPanel implements ActionListener, ConDefaultButton{ 
	protected JTextField tfAnadir;
	protected JTextField tfRelacionados;
	protected JButton btnAnadir;
	protected JButton btnEliminar;
	protected DefaultListModel listModel;
	protected JList listaTerminos;

	public PanelEdicionTecnologiasEmergentes(DelphosFrame framePrincipal) {
		
		JLabel lblNewLabel = new JLabel("Editar Listado Tecnologías Emergentes");
		lblNewLabel.setBackground(Color.ORANGE);
		lblNewLabel.setOpaque(true);
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
		JLabel lblAadir = new JLabel("Añadir:");
		
		tfAnadir = new JTextField();
		tfAnadir.setColumns(10);
		
		JLabel lblRelacionados = new JLabel("Relacionados:");
		
		tfRelacionados = new JTextField();
		tfRelacionados.setColumns(10);
		
		btnAnadir = new JButton("Añadir");
		this.btnAnadir.addActionListener(this);
		
		btnEliminar = new JButton("Eliminar");
		this.btnEliminar.addActionListener(this);
		
		listModel = new DefaultListModel();
		try (BufferedReader br = new BufferedReader(new FileReader(Delphos.FICHERO_TECNOLOGIAS_EMERGENTES)))
		{
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				listModel.addElement(sCurrentLine);
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		JScrollPane scrollPane = new JScrollPane();
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(btnEliminar))
						.addComponent(lblNewLabel, GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblAadir)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(tfAnadir, 148, 148, 148)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(lblRelacionados)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(tfRelacionados, GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(btnAnadir)
							.addGap(13))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 544, Short.MAX_VALUE)))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(lblNewLabel, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblAadir)
						.addComponent(tfAnadir, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblRelacionados)
						.addComponent(tfRelacionados, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnAnadir))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnEliminar)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
					.addContainerGap())
		);
		
		listaTerminos = new JList(listModel);
		scrollPane.setViewportView(listaTerminos);
		setLayout(groupLayout);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.btnAnadir){
			try{
				FileWriter fw = new FileWriter(new File(Delphos.FICHERO_TECNOLOGIAS_EMERGENTES).getAbsoluteFile(), true);
				BufferedWriter bw = new BufferedWriter(fw);
				String linea = this.tfAnadir.getText();
				if(!this.tfRelacionados.getText().isEmpty())
					linea += ", " + this.tfRelacionados.getText();
				bw.append(linea+"\n");
				this.listModel.addElement(linea);
				bw.close();
				this.tfAnadir.setText("");
				this.tfRelacionados.setText("");
			}
			catch(IOException ex){
				ex.printStackTrace();
			}
		}
		if (e.getSource() == this.btnEliminar){
			try{
				int index = this.listaTerminos.getSelectedIndices().length - 1;

		        while (this.listaTerminos.getSelectedIndices().length != 0) {
		            this.listModel.removeElementAt(this.listaTerminos.getSelectedIndices()[index--]);
		        }
		        
				FileWriter fw = new FileWriter(new File(Delphos.FICHERO_TECNOLOGIAS_EMERGENTES).getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
			    for (int i = 0; i < this.listModel.size(); i++) {
			      String sel = (String)this.listModel.getElementAt(i);
			      bw.write(sel + "\n");
			    }
				bw.close();
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
		
	}

	@Override
	public JButton getDefaultButton() {
		return btnAnadir;
	}

}
