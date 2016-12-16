package delphos.iu;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import delphos.Sector;

public class DelphosTreeDialog<T> extends JDialog {
	static final long serialVersionUID = 1L;
	//private JFrame frame;
	Class<T> clase;
	protected ActionListener controller;
	private JTree tree;
	private JScrollPane panelScroll;
	private JButton btnCancelar;
	private JButton btnBorrar;
	private JButton btnAceptar;

	public DelphosTreeDialog(Class<T> clase) {
		this.clase = clase;
		//frame = new JFrame();
		this.setTitle(clase.getSimpleName());
		this.setBounds(100, 100, 450, 300);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		tree = new JTree(this.crearArbol());
		this.setModoSeleccion(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION); //Por defecto selección múltiple
		//tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		tree.setExpandsSelectedPaths(true);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		//Para mostrar inicialmente el árbol desplegado
		//for( int i = 0; i < tree.getRowCount(); ++i )    
		//  tree.expandRow( i );  
		
		//this.getContentPane().add(tree, BorderLayout.CENTER);
		panelScroll = new JScrollPane(tree);
		panelScroll.setBorder(null);
		this.getContentPane().add(panelScroll);
		
		JPanel panel = new JPanel();
		this.getContentPane().add(panel, BorderLayout.SOUTH);
		
		btnCancelar = new JButton("Cancelar");
		btnCancelar.setName("btnCancelarDelphosTree");
		panel.add(btnCancelar);
		
		btnBorrar = new JButton("Borrar Selección");
		btnBorrar.setName("btnBorrarDelphosTree");
		btnBorrar.addActionListener(controller);
		panel.add(btnBorrar);
		
		btnAceptar = new JButton("Aceptar");
		btnAceptar.setName("btnAceptarDelphosTree");
		btnAceptar.addActionListener(controller);
		panel.add(btnAceptar);
		
		this.pack();   
        this.setLocationByPlatform(true);
	}
	
	public void setModoSeleccion(int modoSeleccion){
		tree.getSelectionModel().setSelectionMode(modoSeleccion);
	}
	
	public Class<T> getClase() {
		return clase;
	}

	public ActionListener getController() {
		return controller;
	}

	public void setController(ActionListener controller) {
		this.controller = controller;
		btnCancelar.addActionListener(controller);
		btnBorrar.addActionListener(controller);
		btnAceptar.addActionListener(controller);
	}
	
	private DefaultMutableTreeNode crearArbol(){
		DefaultMutableTreeNode top = new DefaultMutableTreeNode();
		this.cargarNodo(top);
		return top;
	}
	
	private void cargarNodo(DefaultMutableTreeNode nodo){
		Criteria crit = Delphos.getSession().createCriteria(clase);
		if (nodo.isRoot())
			crit.add(Restrictions.isNull("padre"));
		else
			crit.add(Restrictions.eq("padre",nodo.getUserObject()));
		Iterator<Sector> iterador = crit.list().listIterator();
		while(iterador.hasNext()){
			DefaultMutableTreeNode nodoHijo = new DefaultMutableTreeNode(iterador.next());
			nodo.add(nodoHijo);
			cargarNodo(nodoHijo);
		}
	}

	/**
	 * Devuelve los elementos seleccionados en el árbol
	 * 
	 * @return ArrayList
	 */
	public Set<T> getSeleccion(){
		Set<T> seleccion = new HashSet<T>();
		TreePath caminos[] = tree.getSelectionPaths();
		if (caminos != null)
			for (TreePath tp : caminos)
				seleccion.add((T)((DefaultMutableTreeNode)tp.getLastPathComponent()).getUserObject());
		
//		DefaultMutableTreeNode raiz = (DefaultMutableTreeNode)tree.getModel().getRoot();
//		Enumeration e = raiz.depthFirstEnumeration();
//		while (e.hasMoreElements()){
//			if (e)
//			seleccion.add((T)e.nextElement());
//		}
		return seleccion;
	}
	public <T> T getSeleccionUnico(Class<T> clazz) throws Exception{
		//Class<T> clazz;
		T elemento = clazz.newInstance();
		TreePath caminos[] = tree.getSelectionPaths();
		if (caminos.length > 1)
			throw new Exception("Selección Múltiple");
		
		if (caminos != null)
			return (T)((DefaultMutableTreeNode)caminos[0].getLastPathComponent()).getUserObject();
			
		return elemento;
	}

	public void seleccionar(Collection<T> lista){
		tree.clearSelection();
		TreePath paths[] = new TreePath[lista.size()];
		int i = 0;
		DefaultTreeModel modelo = (DefaultTreeModel)tree.getModel();
		DefaultMutableTreeNode raiz = (DefaultMutableTreeNode)modelo.getRoot();
		Enumeration e = raiz.depthFirstEnumeration();
		while (e.hasMoreElements()){
			DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) e.nextElement();
			if (lista.contains((T)nodo.getUserObject()))
				paths[i++] = new TreePath(modelo.getPathToRoot(nodo));
		}
		tree.setSelectionPaths(paths);
	}
	
	public <T> void seleccionarUnico(T elemento){
		tree.clearSelection();
		DefaultTreeModel modelo = (DefaultTreeModel)tree.getModel();
		DefaultMutableTreeNode raiz = (DefaultMutableTreeNode)modelo.getRoot();
		Enumeration e = raiz.depthFirstEnumeration();
		boolean encontrado = false;
		while (e.hasMoreElements() && !encontrado){
			DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) e.nextElement();
			if (elemento.equals((T)nodo.getUserObject())){
				tree.setSelectionPath(new TreePath(modelo.getPathToRoot(nodo)));
				encontrado = true;
			}
		}
	}

	public void borrarSeleccion(){
		tree.clearSelection();
	}

}
