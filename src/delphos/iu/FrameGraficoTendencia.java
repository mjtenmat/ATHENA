package delphos.iu;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jdatepicker.JDateComponentFactory;
import org.jdatepicker.JDatePicker;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.data.category.DefaultCategoryDataset;

import delphos.Tendencia;

public class FrameGraficoTendencia {
	private JFrame frame; 
	private ChartPanel panelDatos;
	private JButton btnAnalizar;
	private JDatePicker dpFechaDesde;
	private JDatePicker dpFechaHasta;
	private Tendencia tendencia;

	public FrameGraficoTendencia(ChartPanel panelDatos, Tendencia tendencia) {
		this.panelDatos = panelDatos;
		this.tendencia = tendencia;

		panelDatos.addChartMouseListener(new ChartMouseListener() {
			JDatePicker dpActual;

			@Override
			public void chartMouseClicked(ChartMouseEvent event) {
				CategoryItemEntity item = (CategoryItemEntity) event.getEntity(); 
				DefaultCategoryDataset dataset = (DefaultCategoryDataset) item.getDataset(); 
				String fecha = item.getColumnKey().toString();
				String categoria = item.getRowKey().toString(); // Patentes o
																// Licitaciones
				System.out.println("Columna: " + item.getColumnKey() + " Fila:  " + item.getRowKey());
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				if (dpActual == null)
					dpActual = dpFechaDesde;

				Calendar fechaDesde = Calendar.getInstance();
				try {
					fechaDesde.setTime(sdf.parse(fecha));
				} catch (Exception e) {
					// Puede que no tenga día
					sdf = new SimpleDateFormat("MM/yyyy");
					try {
						fechaDesde.setTime(sdf.parse(fecha));
					} catch (Exception e1) {
						// Puede que no tenga mes
						sdf = new SimpleDateFormat("yyyy");
						try {
							fechaDesde.setTime(sdf.parse(fecha));
						} catch (Exception e2) {
							// Pues ya ni idea
							e1.printStackTrace();
							return;
						}
					}
				}
				dpActual.getModel().setYear(fechaDesde.get(Calendar.YEAR));
				dpActual.getModel().setMonth(fechaDesde.get(Calendar.MONTH));
				dpActual.getModel().setDay(fechaDesde.get(Calendar.DAY_OF_MONTH));
				dpActual.getModel().setSelected(true);
				if (dpActual == dpFechaDesde)
					dpActual = dpFechaHasta;
				else {
					dpActual = dpFechaDesde;
				}
			}


			@Override
			public void chartMouseMoved(ChartMouseEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		frame = new JFrame();
		frame.setTitle("Tendencia");
		// f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout(0, 5));
		frame.add(this.panelDatos, BorderLayout.CENTER);
		this.panelDatos.setMouseWheelEnabled(true);
		this.panelDatos.setHorizontalAxisTrace(true);
		this.panelDatos.setVerticalAxisTrace(true);

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		// panel.add(createTrace());
		panel.add(

		crearBoton());
		panel.add((JComponent) crearFechaDesde());
		panel.add((JComponent) crearFechaHasta());
		// panel.add(createZoom());
		frame.add(panel, BorderLayout.SOUTH);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private JButton crearBoton() {
		btnAnalizar = new JButton("Analizar");
		btnAnalizar.setEnabled(true);

		btnAnalizar.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				Calendar fechaDesde = (Calendar) dpFechaDesde.getModel().getValue();
				Calendar fechaHasta = (Calendar) dpFechaHasta.getModel().getValue();
				JFrameAnalisisTendencias jfat = new JFrameAnalisisTendencias(tendencia, fechaDesde, fechaHasta);
				jfat.pack();
				jfat.setVisible(true);
				jfat.toFront();
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		});
		return btnAnalizar;
	}

	private JDatePicker crearFechaDesde() {
		dpFechaDesde = new JDateComponentFactory().createJDatePicker();
		dpFechaDesde.setTextEditable(true);
		dpFechaDesde.setShowYearButtons(true);
		dpFechaDesde.getModel().setValue(null);

		dpFechaDesde.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

			}
		});
		return dpFechaDesde;
	}

	private JDatePicker crearFechaHasta() {
		dpFechaHasta = new JDateComponentFactory().createJDatePicker();
		dpFechaHasta.setTextEditable(true);
		dpFechaHasta.setShowYearButtons(true);
		dpFechaHasta.getModel().setValue(null);

		dpFechaHasta.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

			}
		});
		return dpFechaHasta;
	}

}
