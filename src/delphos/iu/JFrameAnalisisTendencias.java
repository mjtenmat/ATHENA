package delphos.iu;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import delphos.AnalisisTendencia;
import delphos.Searcher;
import delphos.Tendencia;

public class JFrameAnalisisTendencias extends JFrame {

	protected JPanel panelPatentePeriodoAnterior;
	protected JPanel panelPatentePeriodoSeleccionado;
	protected JPanel panelPatentePeriodoPosterior;
	protected JPanel panelLicitacionPeriodoAnterior;
	protected JPanel panelLicitacionPeriodoSeleccionado;
	protected JPanel panelLicitacionPeriodoPosterior;
	private Tendencia tendencia;
	private Calendar fechaDesde;
	private Calendar fechaHasta;

	public JFrameAnalisisTendencias(Tendencia tendencia, Calendar fechaDesde, Calendar fechaHasta) {
		this.tendencia = tendencia;
		this.fechaDesde = fechaDesde;
		this.fechaHasta = fechaHasta;

		// Cálculo de Periodos
		//TODO: Refactorizar con Searcher.analizarTendencia en Java 8
		long diferencia = fechaHasta.getTimeInMillis() - fechaDesde.getTimeInMillis();
		TimeUnit tu=TimeUnit.DAYS;
		diferencia = tu.convert(diferencia, TimeUnit.MILLISECONDS);
		System.out.println("Diferencia (días): " + diferencia);
		Calendar fechaFinPeriodoAnterior = (Calendar)fechaDesde.clone();
		fechaFinPeriodoAnterior.add(Calendar.DATE, -1);
		Calendar fechaInicioPeriodoAnterior = (Calendar)fechaFinPeriodoAnterior.clone();
		fechaInicioPeriodoAnterior.add(Calendar.DATE, -((int)diferencia));
		Calendar fechaInicioPeriodoPosterior = (Calendar)fechaHasta.clone();
		fechaInicioPeriodoPosterior.add(Calendar.DATE, +1);
		Calendar fechaFinPeriodoPosterior = (Calendar)fechaInicioPeriodoPosterior.clone();
		fechaFinPeriodoPosterior.add(Calendar.DATE, ((int)diferencia));
		
		// Realizamos el análisis
		ArrayList<AnalisisTendencia> listaAnalisisTendencia = Searcher.analizarTendencia(tendencia, fechaDesde, fechaHasta);

		setTitle("Análisis de Tendencia");

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);

		JPanel panelPatente = new JPanel();
		tabbedPane.addTab("Patentes", null, panelPatente, null);
		panelPatente.setLayout(new BoxLayout(panelPatente, BoxLayout.Y_AXIS));
		// panelPatente.setLayout(new BoxLayout(panelPatente,
		// BoxLayout.Y_AXIS));

		JPanel panelPatentePeriodoAnterior = new PanelATPeriodo(PanelATPeriodo.Tipo.PATENTES, fechaInicioPeriodoAnterior, fechaFinPeriodoAnterior, listaAnalisisTendencia.get(0));
		// panelPatente.add(panelPatentePeriodoAnterior);

		JPanel panelPatentePeriodoSeleccionado = new PanelATPeriodo(PanelATPeriodo.Tipo.PATENTES, fechaDesde, fechaHasta, listaAnalisisTendencia.get(1));
		// panelPatente.add(panelPatentePeriodoSeleccionado);

		JPanel panelPatentePeriodoPosterior = new PanelATPeriodo(PanelATPeriodo.Tipo.PATENTES, fechaInicioPeriodoPosterior, fechaFinPeriodoPosterior, listaAnalisisTendencia.get(2));
		// panelPatente.add(panelPatentePeriodoPosterior);

		Dimension minimumSize = new Dimension(100, 50);
		JSplitPane sppPatente1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelPatentePeriodoSeleccionado, panelPatentePeriodoPosterior);
		sppPatente1.setOneTouchExpandable(true);
		sppPatente1.setDividerLocation(150);
		panelPatentePeriodoSeleccionado.setMinimumSize(minimumSize);
		panelPatentePeriodoPosterior.setMinimumSize(minimumSize);

		JSplitPane sppPatente2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelPatentePeriodoAnterior, sppPatente1);
		sppPatente2.setOneTouchExpandable(true);
		sppPatente2.setDividerLocation(150);
		panelPatentePeriodoAnterior.setMinimumSize(minimumSize);
		sppPatente2.setMinimumSize(minimumSize);

		panelPatente.add(sppPatente2);

		JPanel panelLicitacion = new JPanel();
		tabbedPane.addTab("Licitaciones", null, panelLicitacion, null);
		panelLicitacion.setLayout(new BoxLayout(panelLicitacion, BoxLayout.Y_AXIS));

		JPanel panelLicitacionPeriodoAnterior = new PanelATPeriodo(PanelATPeriodo.Tipo.LICITACIONES, fechaInicioPeriodoAnterior, fechaFinPeriodoAnterior, listaAnalisisTendencia.get(0));
		// panelLicitacion.add(panelLicitacionPeriodoAnterior);

		JPanel panelLicitacionPeriodoSeleccionado = new PanelATPeriodo(PanelATPeriodo.Tipo.LICITACIONES, fechaDesde, fechaHasta, listaAnalisisTendencia.get(1));
		// panelLicitacion.add(panelLicitacionPeriodoSeleccionado);

		JPanel panelLicitacionPeriodoPosterior = new PanelATPeriodo(PanelATPeriodo.Tipo.LICITACIONES, fechaInicioPeriodoPosterior, fechaFinPeriodoPosterior, listaAnalisisTendencia.get(2));
		// panelLicitacion.add(panelLicitacionPeriodoPosterior);

		JSplitPane sppLicitacion1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelLicitacionPeriodoSeleccionado, panelLicitacionPeriodoPosterior);
		sppLicitacion1.setOneTouchExpandable(true);
		sppLicitacion1.setDividerLocation(150);
		panelLicitacionPeriodoSeleccionado.setMinimumSize(minimumSize);
		panelLicitacionPeriodoPosterior.setMinimumSize(minimumSize);

		JSplitPane sppLicitacion2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelLicitacionPeriodoAnterior, sppLicitacion1);
		sppLicitacion2.setOneTouchExpandable(true);
		sppLicitacion2.setDividerLocation(150);
		panelLicitacionPeriodoAnterior.setMinimumSize(minimumSize);
		sppLicitacion2.setMinimumSize(minimumSize);

		panelLicitacion.add(sppLicitacion2);
	}
}
