package delphos.iu;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class PopUpDocsWeb extends JPopupMenu{
	JMenuItem anItem;
	static String textoSeleccionado; //Qué chapuza!
    public PopUpDocsWeb(PanelVigilanciaController panelVigilanciaController, String textoSeleccionado){
    	PopUpDocsWeb.textoSeleccionado = textoSeleccionado;
        anItem = new JMenuItem("Más como este");
        anItem.addActionListener(panelVigilanciaController);
        add(anItem);
    }
}
