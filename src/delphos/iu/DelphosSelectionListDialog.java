package delphos.iu;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.hibernate.Criteria;
import org.hibernate.cfg.NotYetImplementedException;

import delphos.Jerarquia;

public class DelphosSelectionListDialog<T> extends DelphosSelectionDialog<T>{
	static final long serialVersionUID = 1L;

	private JList<T> lista;

	public DelphosSelectionListDialog(Class<T> clase, DelphosSelectionListener controller) {
		super(clase, controller, new JList());
		
		this.clase = clase;
		this.lista = (JList) this.selector;
		this.lista.setModel(this.crearLista());
		this.lista
				.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	private ListModel<T> crearLista() {
		DefaultListModel<T> resultado = new DefaultListModel<T>();
		Criteria crit = Delphos.getSession().createCriteria(clase);
		Iterator<T> it = crit.list().iterator();
		while (it.hasNext()) {
			resultado.addElement(it.next());
			;
		}
		return resultado;
	}
	
	@Override
	protected void calcularSeleccion() {
		this.seleccion = new HashSet<T>();

		int[] indices = this.lista.getSelectedIndices();
		for (int i = 0; i < indices.length; i++) {
			this.seleccion.add(this.lista.getModel().getElementAt(indices[i]));
		}
	}

	@Override
	protected void seleccionar() {
		this.lista.clearSelection();
		
		ListModel<T> modelo = this.lista.getModel();
		
		for(int i = 0; i < modelo.getSize(); i++)
			if (this.seleccion.contains(modelo.getElementAt(i)))
					lista.addSelectionInterval(i, i);
	}

	@Override
	protected void borrarSeleccion() {
		lista.clearSelection();
		this.seleccion = new HashSet<T>();
	}
}