package delphos.iu;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import delphos.AnalisisTendencia;
import delphos.EntradaAnalisisTendencia;

public class PanelATPeriodo extends JPanel{
	private final int ANCHO = 150;
	private final int ALTO = 125;
	public static enum Tipo {PATENTES, LICITACIONES};
	private Tipo tipo;
	private Calendar fechaDesde;
	private Calendar fechaHasta;
	
	protected JTextArea taSector;
	protected JTextArea taPais;
	private JTextArea taInventorTipo;
	protected JTextArea taInventor;
	protected JTextArea taTipo;
	protected JTextArea taSolicitante;
	protected JTextArea taContenido;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	
	public PanelATPeriodo(){
		super();
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JLabel lbl = new JLabel("De tal a tal");
		this.add(lbl);
		
		JPanel panelListas = new JPanel();
		this.add(panelListas);
		panelListas.setLayout(new BoxLayout(panelListas, BoxLayout.X_AXIS));
		
		JPanel panelSector = new JPanel();
		panelListas.add(panelSector);
		panelSector.setLayout(new BoxLayout(panelSector, BoxLayout.Y_AXIS));
		
		JLabel lblNewLabel = new JLabel("Sector");
		panelSector.add(lblNewLabel);
		
		taSector = new JTextArea();
		taSector.setMinimumSize(new Dimension(ANCHO, ALTO));
		JScrollPane jspSector = new JScrollPane (taSector, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panelSector.add(jspSector);
		taSector.setText("entrada 1\nentrada 2 \n");
		
		JPanel panelPais = new JPanel();
		panelListas.add(panelPais);
		panelPais.setLayout(new BoxLayout(panelPais, BoxLayout.Y_AXIS));
		
		JLabel lblNewLabel_1 = new JLabel("País");
		panelPais.add(lblNewLabel_1);
		
		taPais = new JTextArea();
		JScrollPane jspPais = new JScrollPane (taPais, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jspPais.setMinimumSize(new Dimension(ANCHO, ALTO));
		jspPais.setPreferredSize(new Dimension(ANCHO, ALTO));
		panelPais.add(jspPais);
		taPais.setText("entrada 1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111\nentrada 2 \n");
		
		JPanel panelInventor = new JPanel();
		panelListas.add(panelInventor);
		panelInventor.setLayout(new BoxLayout(panelInventor, BoxLayout.Y_AXIS));
		
		JLabel lblNewLabel_2 = new JLabel();
		if (this.tipo == Tipo.PATENTES)
			lblNewLabel_2.setText("Inventor");
		else
			lblNewLabel_2.setText("Tipo");
		panelInventor.add(lblNewLabel_2);
		
		taInventorTipo = new JTextArea();
		taInventorTipo.setMinimumSize(new Dimension(ANCHO, ALTO));
		JScrollPane jspInventorTipo = new JScrollPane (taInventorTipo, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panelInventor.add(jspInventorTipo);
		taInventor = taInventorTipo;
		taTipo = taInventorTipo;
		taInventorTipo.setText("entrada 1\nentrada 2 \n");

		
		JPanel panelSolicitante = new JPanel();
		panelListas.add(panelSolicitante);
		panelSolicitante.setLayout(new BoxLayout(panelSolicitante, BoxLayout.Y_AXIS));
		
		JLabel lblNewLabel_3 = new JLabel("Solicitante");
		panelSolicitante.add(lblNewLabel_3);
		
		taSolicitante = new JTextArea();
		taSolicitante.setMinimumSize(new Dimension(ANCHO, ALTO));
		JScrollPane jspSolicitante = new JScrollPane (taSolicitante, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panelSolicitante.add(jspSolicitante);
		taSolicitante.setText("entrada 1\nentrada 2 \n");

		JPanel panelContenido = new JPanel();
		panelListas.add(panelContenido);
		panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
		
		JLabel lblNewLabel_4 = new JLabel("Contenido");
		panelContenido.add(lblNewLabel_4);
		
		taContenido = new JTextArea();
		taContenido.setMinimumSize(new Dimension(ANCHO, ALTO));
		JScrollPane jspContenido = new JScrollPane (taContenido, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panelContenido.add(jspContenido);
		taContenido.setText("entrada 1\nentrada 2 \n");

	}


	public PanelATPeriodo(Tipo tipo, Calendar fechaDesde, Calendar fechaHasta, AnalisisTendencia analisisTendencia){
		super();
		
		this.tipo = tipo;
		this.fechaDesde = fechaDesde;
		this.fechaHasta = fechaHasta;
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JLabel lbl = new JLabel(sdf.format(fechaDesde.getTime()) + " - " + sdf.format(fechaHasta.getTime()));
		this.add(lbl);
		
		JPanel panelListas = new JPanel();
		this.add(panelListas);
		panelListas.setLayout(new BoxLayout(panelListas, BoxLayout.X_AXIS));
		
		JPanel panelSector = new JPanel();
		panelListas.add(panelSector);
		panelSector.setLayout(new BoxLayout(panelSector, BoxLayout.Y_AXIS));
		
		JLabel lblNewLabel = new JLabel("Sector");
		panelSector.add(lblNewLabel);
		
		taSector = new JTextArea();
		taSector.setMinimumSize(new Dimension(ANCHO, ALTO));
		JScrollPane jspSector = new JScrollPane (taSector, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jspSector.setMinimumSize(new Dimension(ANCHO, ALTO));
		jspSector.setPreferredSize(new Dimension(ANCHO, ALTO));
		panelSector.add(jspSector);
		ArrayList<EntradaAnalisisTendencia> listaEntradas = new ArrayList<EntradaAnalisisTendencia>();
		if (tipo == Tipo.PATENTES)
			listaEntradas = analisisTendencia.getListaPatenteSector();
		if (tipo == Tipo.LICITACIONES)
			listaEntradas = analisisTendencia.getListaLicitacionSector();
		Collections.sort(listaEntradas);
		StringBuilder textoPanel = new StringBuilder("");
		for(EntradaAnalisisTendencia entrada : listaEntradas)
			textoPanel.append(entrada.toString() + "\n");
		taSector.setText(textoPanel.toString());
		colocarScroll(jspSector);
		
		JPanel panelPais = new JPanel();
		panelListas.add(panelPais);
		panelPais.setLayout(new BoxLayout(panelPais, BoxLayout.Y_AXIS));
		
		JLabel lblNewLabel_1 = new JLabel("País");
		panelPais.add(lblNewLabel_1);
		
		taPais = new JTextArea();
		taPais.setMinimumSize(new Dimension(ANCHO, ALTO));
		JScrollPane jspPais = new JScrollPane (taPais, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jspPais.setMinimumSize(new Dimension(ANCHO, ALTO));
		jspPais.setPreferredSize(new Dimension(ANCHO, ALTO));
		panelPais.add(jspPais);
		//TODO: Esto necesita una refactorización para DRY
		listaEntradas = new ArrayList<EntradaAnalisisTendencia>();
		if (tipo == Tipo.PATENTES)
			listaEntradas = analisisTendencia.getListaPatentePais();
		if (tipo == Tipo.LICITACIONES)
			listaEntradas = analisisTendencia.getListaLicitacionPais();
		Collections.sort(listaEntradas);
		textoPanel = new StringBuilder("");
		for(EntradaAnalisisTendencia entrada : listaEntradas)
			textoPanel.append(entrada.toString() + "\n");
		taPais.setText(textoPanel.toString());
		colocarScroll(jspPais);
		
		JPanel panelInventor = new JPanel();
		panelListas.add(panelInventor);
		panelInventor.setLayout(new BoxLayout(panelInventor, BoxLayout.Y_AXIS));
		
		JLabel lblNewLabel_2 = new JLabel();
		if (this.tipo == Tipo.PATENTES)
			lblNewLabel_2.setText("Inventor");
		else
			lblNewLabel_2.setText("Tipo");
		panelInventor.add(lblNewLabel_2);
		
		taInventorTipo = new JTextArea();
		taInventorTipo.setMinimumSize(new Dimension(ANCHO, ALTO));
		JScrollPane jspInventorTipo = new JScrollPane (taInventorTipo, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jspInventorTipo.setMinimumSize(new Dimension(ANCHO, ALTO));
		jspInventorTipo.setPreferredSize(new Dimension(ANCHO, ALTO));
		panelInventor.add(jspInventorTipo);
		taInventor = taInventorTipo;
		taTipo = taInventorTipo;
		//TODO: Esto necesita una refactorización para DRY
		listaEntradas = new ArrayList<EntradaAnalisisTendencia>();
		if (tipo == Tipo.PATENTES)
			listaEntradas = analisisTendencia.getListaPatenteInventor();
		if (tipo == Tipo.LICITACIONES)
			listaEntradas = analisisTendencia.getListaLicitacionTipo();
		Collections.sort(listaEntradas);
		textoPanel = new StringBuilder("");
		for(EntradaAnalisisTendencia entrada : listaEntradas)
			textoPanel.append(entrada.toString() + "\n");
		taInventorTipo.setText(textoPanel.toString());
		colocarScroll(jspInventorTipo);
		
		JPanel panelSolicitante = new JPanel();
		panelListas.add(panelSolicitante);
		panelSolicitante.setLayout(new BoxLayout(panelSolicitante, BoxLayout.Y_AXIS));
		
		JLabel lblNewLabel_3 = new JLabel("Solicitante");
		panelSolicitante.add(lblNewLabel_3);
		
		taSolicitante = new JTextArea();
		taSolicitante.setMinimumSize(new Dimension(ANCHO, ALTO));
		JScrollPane jspSolicitante = new JScrollPane (taSolicitante, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jspSolicitante.setMinimumSize(new Dimension(ANCHO, ALTO));
		jspSolicitante.setPreferredSize(new Dimension(ANCHO, ALTO));
		panelSolicitante.add(jspSolicitante);
		//TODO: Esto necesita una refactorización para DRY
		listaEntradas = new ArrayList<EntradaAnalisisTendencia>();
		if (tipo == Tipo.PATENTES)
			listaEntradas = analisisTendencia.getListaPatenteSolicitante();
		if (tipo == Tipo.LICITACIONES)
			listaEntradas = analisisTendencia.getListaLicitacionSolicitante();
		Collections.sort(listaEntradas);
		textoPanel = new StringBuilder("");
		for(EntradaAnalisisTendencia entrada : listaEntradas)
			textoPanel.append(entrada.toString() + "\n");
		taSolicitante.setText(textoPanel.toString());
		colocarScroll(jspSolicitante);
		
		JPanel panelContenido = new JPanel();
		panelListas.add(panelContenido);
		panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
		
		JLabel lblNewLabel_4 = new JLabel("Contenido");
		panelContenido.add(lblNewLabel_4);
		
		taContenido = new JTextArea();
		taContenido.setMinimumSize(new Dimension(ANCHO, ALTO));
		JScrollPane jspContenido = new JScrollPane (taContenido, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jspContenido.setMinimumSize(new Dimension(ANCHO, ALTO));
		jspContenido.setPreferredSize(new Dimension(ANCHO, ALTO));
		panelContenido.add(jspContenido);
		//TODO: Esto necesita una refactorización para DRY
		listaEntradas = new ArrayList<EntradaAnalisisTendencia>();
		if (tipo == Tipo.PATENTES)
			listaEntradas = analisisTendencia.getListaPatenteContenido();
		if (tipo == Tipo.LICITACIONES)
			listaEntradas = analisisTendencia.getListaLicitacionContenido();
		Collections.sort(listaEntradas);
		textoPanel = new StringBuilder("");
		for(EntradaAnalisisTendencia entrada : listaEntradas)
			textoPanel.append(entrada.toString() + "\n");
		taContenido.setText(textoPanel.toString());
		taContenido.scrollRectToVisible(new Rectangle(0, 0, 20, 20));
		//colocarScroll(jspContenido);
	}

	private void colocarScroll(JScrollPane scrollPane){
		JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
	    JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();
	    verticalScrollBar.setValue(0);
	    horizontalScrollBar.setValue(0);
		
//		JViewport jv = scrollPane.getViewport();
//		jv.setViewPosition(new Point(0,0));
		
		
	}
}
