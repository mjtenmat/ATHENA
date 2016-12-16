package delphos.iu;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

import org.hibernate.Criteria;
import org.hibernate.cfg.NotYetImplementedException;

import delphos.Jerarquia;

public abstract class DelphosSelectionDialog<T> extends JDialog implements ActionListener{
	static final long serialVersionUID = 1L;

	Class<T> clase;
	protected Set<T> seleccion;
	private DelphosSelectionListener controller;
	protected JComponent selector;	//Será JTree<T> o JList<T>
	private JScrollPane panelScroll;
	private JButton btnCancelar;
	private JButton btnBorrar;
	private JButton btnAceptar;

	public DelphosSelectionDialog(Class<T> clase, DelphosSelectionListener controller, JComponent selector) {
		this.clase = clase;
		this.controller = controller;
		this.selector = selector;
		this.seleccion = new HashSet<T>();

		this.setTitle(clase.getSimpleName());
		this.setBounds(100, 100, 450, 300);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		panelScroll = new JScrollPane(this.selector);
		panelScroll.setBorder(null);
		this.getContentPane().add(panelScroll);

		JPanel panel = new JPanel();
		this.getContentPane().add(panel, BorderLayout.SOUTH);

		btnCancelar = new JButton("Cancelar");
		btnCancelar.addActionListener(this);
		panel.add(btnCancelar);

		btnBorrar = new JButton("Borrar Selección");
		btnBorrar.addActionListener(this);
		panel.add(btnBorrar);

		btnAceptar = new JButton("Aceptar");
		btnAceptar.addActionListener(this);
		panel.add(btnAceptar);

		this.pack();
		this.setLocationByPlatform(true);

		this.controller.onCrear(this);
	}

	public Class<T> getClase() {
		return clase;
	}

	public Set<T> getSeleccion() {
		return this.seleccion;
	}
	
	public void setSeleccion(Set<T> seleccion){
		this.seleccion = seleccion;
		seleccionar();
	}

	protected abstract void calcularSeleccion();
	protected abstract void seleccionar();
	protected abstract void borrarSeleccion();
	
	public void mostrar(){
		seleccionar();
		this.setVisible(true);
	}
	
	public String getTextoSeleccion() {
		String resultado = "Todos";
		Iterator it = getSeleccion().iterator();
		if (it.hasNext())
			resultado = it.next().toString();
		while (it.hasNext()) {
			resultado += ", " + it.next().toString();
		}

		return resultado;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == this.btnCancelar) {
			this.controller.onCancelar(this);
			this.setVisible(false);
		}

		if (e.getSource() == this.btnBorrar){
			this.controller.onBorrar(this);
			this.borrarSeleccion();
		}

		if (e.getSource() == this.btnAceptar) {
			calcularSeleccion();
			this.controller.onAceptar(this);
			this.setVisible(false);
		}

	}

	
}
