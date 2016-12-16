package delphos.iu;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.hibernate.Query;

import delphos.AvisoTendencia;
import delphos.Tendencia;

public class PanelAvisoTendencia  extends JPanel implements ActionListener{
	protected JCheckBox chckbxRevisado;
	protected JCheckBox chckbxEliminar;
	protected JButton btnVer;
	private AvisoTendencia aviso;
	private PanelAvisosTendencia panelAvisosTendencia;
	
	public PanelAvisoTendencia(AvisoTendencia aviso, PanelAvisosTendencia panelAvisosTendencia) {
		this.aviso = aviso;
		this.panelAvisosTendencia = panelAvisosTendencia;
		
		this.setPreferredSize(new Dimension(500, 74));
		this.setMinimumSize(new Dimension(500, 120));
		
		JLabel lblAviso = new JLabel(aviso.toString());
		chckbxRevisado = new JCheckBox("Revisado");
		chckbxRevisado.setSelected(aviso.isRevisado());
		chckbxRevisado.addActionListener(this);
		
		chckbxEliminar = new JCheckBox("Eliminar");
		chckbxEliminar.addActionListener(this);
		
		btnVer = new JButton("Ver");
		btnVer.addActionListener(this);
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(btnVer)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(chckbxRevisado)
							.addGap(18)
							.addComponent(chckbxEliminar))
						.addComponent(lblAviso, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblAviso)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(chckbxRevisado)
						.addComponent(btnVer)
						.addComponent(chckbxEliminar))
					.addContainerGap(16, Short.MAX_VALUE))
		);
		setLayout(groupLayout);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		this.panelAvisosTendencia.framePrincipal.frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		if (e.getSource() == this.chckbxRevisado){
			String sql = "UPDATE AvisoTendencia SET revisado = ";
			if (this.chckbxRevisado.isSelected())
				sql += "1 ";
			else 
				sql += "0 ";
			sql += "WHERE id = " + this.aviso.getId();
			Query query = Delphos.getSession().createSQLQuery(sql);
			Delphos.getSession().beginTransaction();
			query.executeUpdate();
			Delphos.getSession().getTransaction().commit();
		}
		if (e.getSource() == this.chckbxEliminar){
			if (JOptionPane.showConfirmDialog(this, "¿Realmente quiere ELIMINAR este aviso?", "Confirmación", JOptionPane.OK_CANCEL_OPTION ) == JOptionPane.OK_OPTION){
				String sql = "DELETE FROM AvisoTendencia WHERE id = " + this.aviso.getId();
				Query query = Delphos.getSession().createSQLQuery(sql);
				Delphos.getSession().beginTransaction();
				query.executeUpdate();
				Delphos.getSession().getTransaction().commit();
				this.setVisible(false);
				this.panelAvisosTendencia.cargarAvisos();
			}
			else{
				this.chckbxEliminar.setSelected(false);
			}
		}
		if (e.getSource() == this.btnVer){
			System.out.println("Mostrando Tendencia");
			//Hay que hacer dos gráficas, una para cada periodo
			//Tendencia tendencia = (Tendencia)this.aviso.getTendencia().clone();
			Tendencia tendencia = this.aviso.getTendencia();
			
			if (this.aviso.getTipo().equals(AvisoTendencia.LICITACIONES))
				tendencia.setIndicadorPatentes(false);
			else
				tendencia.setIndicadorLicitaciones(false);
			
			int tipoPeriodo = 0, periodo = 0, numPeriodo = 0;
			switch (this.aviso.getPeriodo()){
			case AvisoTendencia.ULTIMO_ANIO:
				tipoPeriodo = Calendar.YEAR;
				periodo = PanelTendenciasBuscador.PERIODO_ULTIMO_ANIO;
				numPeriodo = 1;
				break;
			case AvisoTendencia.ULTIMO_MES:
				tipoPeriodo = Calendar.MONTH;
				periodo = PanelTendenciasBuscador.PERIODO_ULTIMO_MES;
				numPeriodo = 1;
				break;
			case AvisoTendencia.ULTIMOS_3MESES:
				tipoPeriodo = Calendar.MONTH;
				periodo = PanelTendenciasBuscador.PERIODO_ULTIMO_TRIMESTRE;
				numPeriodo = 3;
				break;
			case AvisoTendencia.ULTIMOS_6MESES:
				tipoPeriodo = Calendar.MONTH;
				periodo = PanelTendenciasBuscador.PERIODO_ULTIMO_SEMESTRE;
				numPeriodo = 6;
				break;
			}
			
			Calendar fechaFinPeriodoActual = Calendar.getInstance(); 
			fechaFinPeriodoActual.setTime(this.aviso.getFechaRegistro());
			Calendar fechaInicioPeriodoActual = Calendar.getInstance();
			fechaInicioPeriodoActual.setTime(this.aviso.getFechaRegistro());
			fechaInicioPeriodoActual.add(tipoPeriodo, -numPeriodo);
			Calendar fechaInicioPeriodoAnterior = (Calendar)fechaInicioPeriodoActual.clone();
			fechaInicioPeriodoAnterior.add(tipoPeriodo, -numPeriodo);
			
			//Y llamar a:
			this.panelAvisosTendencia.framePrincipal.panelTendencias.compararGraficasTendencia(
					tendencia, PanelTendenciasBuscador.PERIODO_USUARIO, 
					(GregorianCalendar)fechaInicioPeriodoAnterior, 
					(GregorianCalendar)fechaInicioPeriodoActual,
					(GregorianCalendar)fechaFinPeriodoActual);
			
			this.panelAvisosTendencia.framePrincipal.frame.setCursor(Cursor.getDefaultCursor());
		}
	}
}
