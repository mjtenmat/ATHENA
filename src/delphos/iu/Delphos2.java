package delphos.iu;

	

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
	 

public class Delphos2 extends JFrame {
	public Delphos2() {
	}
	    public static void main(String[] args) {
	        Delphos2 delphosFrame = new Delphos2();
	        JPanel panelBox = new JPanel(new BorderLayout());
	        JPanel panelFlow = new JPanel();
	        panelFlow.setLayout(new BoxLayout(panelFlow, BoxLayout.Y_AXIS));
	        panelBox.add(panelFlow);
	        JTextArea textArea = new JTextArea();
	        textArea.setLineWrap(true);
	        String texto = "ñsald kfjñalsdkj<b>fñadlskjf</b> ñlsakdj fñlsadkjf ñlsakdjf ñlsdkj fñlsakjdf ñlsakdjf ñlskdf ";
	        textArea.setText(texto+texto+texto+texto+texto+texto+texto);
	        textArea.setEditable(false);
	        panelFlow.add(textArea);
	        
	        JLabel label = new JLabel("Label");
	        panelFlow.add(label);
	        
	        delphosFrame.getContentPane().add(panelBox);
//	        noWrapPanel = new JPanel(new BorderLayout());
//	        //noWrapPanel.setPreferredSize(new Dimension(200,200));
//	        noWrapPanel.add(textPane);
//	        scrollPane = new JScrollPane(noWrapPanel);
//	        scrollPane.setPreferredSize(new Dimension(200,200));
//	        scrollPane.setViewportView(textPane); // creates a wrapped scroll pane using the text pane as a viewport.
//	         
//	        delphosFrame.add(scrollPane);
//	        delphosFrame.setPreferredSize(new Dimension(200,200));
	        delphosFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
	        delphosFrame.pack();
	        delphosFrame.setVisible(true);
	    }
	}