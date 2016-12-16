package delphos.iu;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import delphos.Jerarquia;
import delphos.Sector;

public class DelphosSelectionTreeDialog<T> extends DelphosSelectionDialog<T>{
	static final long serialVersionUID = 1L;

	private JTree tree;

	public DelphosSelectionTreeDialog(Class<T> clase, DelphosSelectionListener controller) {
		super(clase, controller, new JTree());
		this.clase = clase;
		this.tree = (JTree) this.selector;
		this.tree.setModel(new DefaultTreeModel(this.crearArbol()));
		
		this.tree = (JTree)this.selector;
		this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		tree.setExpandsSelectedPaths(true);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		this.seleccion = new HashSet<T>();

	}

	private DefaultMutableTreeNode crearArbol() {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode();
		this.cargarNodo(top);
		return top;
	}
	
	private void cargarNodo(DefaultMutableTreeNode nodo) {
		Criteria crit = Delphos.getSession().createCriteria(clase);
		if (nodo.isRoot())
			crit.add(Restrictions.isNull("padre"));
		else
			crit.add(Restrictions.eq("padre", nodo.getUserObject()));
		Iterator<T> iterador = crit.list().listIterator();
		while (iterador.hasNext()) {
			DefaultMutableTreeNode nodoHijo = new DefaultMutableTreeNode(
					iterador.next());
			nodo.add(nodoHijo);
			cargarNodo(nodoHijo);
		}
	}

	public <T> T getSeleccionUnico(Class<T> clazz) throws Exception {
		T elemento = clazz.newInstance();
		TreePath caminos[] = tree.getSelectionPaths();
		if (caminos.length > 1)
			throw new Exception("Selección Múltiple");

		if (caminos != null)
			return (T) ((DefaultMutableTreeNode) caminos[0]
					.getLastPathComponent()).getUserObject();

		return elemento;
	}

	public void seleccionarUnico(T elemento) {
		this.seleccion = new HashSet<T>();
		this.seleccion.add(elemento);
		tree.clearSelection();
		DefaultTreeModel modelo = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode raiz = (DefaultMutableTreeNode) modelo.getRoot();
		Enumeration e = raiz.depthFirstEnumeration();
		boolean encontrado = false;
		while (e.hasMoreElements() && !encontrado) {
			DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) e
					.nextElement();
			if (elemento.equals((T) nodo.getUserObject())) {
				tree.setSelectionPath(new TreePath(modelo.getPathToRoot(nodo)));
				encontrado = true;
			}
		}
	}

	@Override
	public void seleccionar() {
		this.tree.clearSelection();
		TreePath paths[] = new TreePath[this.seleccion.size()];
		int i = 0;
		DefaultTreeModel modelo = (DefaultTreeModel) this.tree.getModel();
		DefaultMutableTreeNode raiz = (DefaultMutableTreeNode) modelo.getRoot();
		Enumeration e = raiz.depthFirstEnumeration();
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) e
					.nextElement();
			if (this.seleccion.contains((T) nodo.getUserObject()))
				paths[i++] = new TreePath(modelo.getPathToRoot(nodo));
		}
		this.tree.setSelectionPaths(paths);
	}
	
	@Override
	protected void borrarSeleccion() {
		tree.clearSelection();
		this.seleccion = new HashSet<T>();
	}

	@Override
	protected void calcularSeleccion() {
		this.seleccion = new HashSet<T>();
		
		TreePath caminos[] = tree.getSelectionPaths();
		if (caminos != null)
			for (TreePath tp : caminos)
				this.seleccion.add((T) ((DefaultMutableTreeNode) tp
						.getLastPathComponent()).getUserObject());
	}
	
}
