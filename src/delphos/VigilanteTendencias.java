package delphos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;

import org.hibernate.Query;

import delphos.iu.Delphos;

public class VigilanteTendencias extends TimerTask {

	private ArrayList<Tendencia> listaTendencias;

	public static void main(String args[]) {
		new VigilanteTendencias().run();
	}

	@Override
	public void run() {
		System.out.println("Iniciando Vigilante de Tendencias.");

		// Seleccionamos todas las tendencias registradas.
		Query query = Delphos.getSession().createQuery("FROM Tendencia");
		listaTendencias = (ArrayList<Tendencia>) query.list();

		for (Tendencia tendencia : listaTendencias) {
			try {
				analizarTendencia(tendencia);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static void analizarTendencia(Tendencia tendencia) throws Exception {
		System.out.println(tendencia.getId() + " " + tendencia.getTerminoPrincipal());

		int tipoPeriodo = 0, periodo = 0;
		String nombrePeriodo = null;

		if (tendencia.getIncUltimoAnio() != null) {
			System.out.println("Calcular tendencia último año.");
			nombrePeriodo = AvisoTendencia.ULTIMO_ANIO;
			tipoPeriodo = Calendar.YEAR;
			periodo = 1;
			try{
				calcularVariacion(tendencia, tipoPeriodo, periodo, nombrePeriodo);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		if (tendencia.getIncUltimoMes() != null) {
			System.out.println("Calcular tendencia último mes.");
			nombrePeriodo = AvisoTendencia.ULTIMO_MES;
			tipoPeriodo = Calendar.MONTH;
			periodo = 1;
			try{
				calcularVariacion(tendencia, tipoPeriodo, periodo, nombrePeriodo);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		if (tendencia.getIncUltimos3Meses() != null) {
			System.out.println("Calcular tendencia últimos 3 meses.");
			nombrePeriodo = AvisoTendencia.ULTIMOS_3MESES;
			tipoPeriodo = Calendar.MONTH;
			periodo = 3;
			try{
				calcularVariacion(tendencia, tipoPeriodo, periodo, nombrePeriodo);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		if (tendencia.getIncUltimos6Meses() != null) {
			System.out.println("Calcular tendencia últimos 6 meses.");
			nombrePeriodo = AvisoTendencia.ULTIMOS_6MESES;
			tipoPeriodo = Calendar.MONTH;
			periodo = 6;
			try{
				calcularVariacion(tendencia, tipoPeriodo, periodo, nombrePeriodo);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	private static void calcularVariacion(Tendencia tendencia, int tipoPeriodo, int periodo, String nombrePeriodo) throws Exception{
		Calendar hoy = Calendar.getInstance(); // Es hoy
		hoy.setTime(new Date());
		Calendar fecha2 = (Calendar) hoy.clone();
		Calendar fecha3;

		fecha2.add(tipoPeriodo, -periodo);
		fecha3 = (Calendar) fecha2.clone();
		fecha3.add(tipoPeriodo, -periodo);
		System.out.println(hoy.getTime() + " - " + fecha2.getTime() + " - " + fecha3.getTime());
		Tendencia tendenciaClon = tendencia.crearClonParaTotales();
		if(tendencia.getIndicadorLicitaciones()){
			int numLicitacionesPeriodoActual = Searcher.verNumLicitacionesModoExperto(fecha2, hoy, tendencia);
			int numTotalLicitacionesPeriodoActual = Searcher.verNumLicitacionesModoExperto(fecha2, hoy, tendenciaClon);
			int numLicitacionesPeriodoAnterior = Searcher.verNumLicitacionesModoExperto(fecha3, fecha2, tendencia);
			int numTotalLicitacionesPeriodoAnterior = Searcher.verNumLicitacionesModoExperto(fecha3, fecha2, tendenciaClon);
			double porcentajeLicitacionesPeriodoActual = 100d * (double)numLicitacionesPeriodoActual / (double)numTotalLicitacionesPeriodoActual;
			double porcentajeLicitacionesPeriodoAnterior = 100d * (double)numLicitacionesPeriodoAnterior / (double)numTotalLicitacionesPeriodoAnterior;
			double incrementoLicitaciones = Math.abs(porcentajeLicitacionesPeriodoActual - porcentajeLicitacionesPeriodoAnterior);
			System.out.println("Licitaciones:");
			System.out.println("\tnumLicitacionesPeriodoActual = " + numLicitacionesPeriodoActual);
			System.out.println("\tnumTotalLicitacionesPeriodoActual = " + numTotalLicitacionesPeriodoActual);
			System.out.println("\tnumLicitacionesPeriodoAnterior = " + numLicitacionesPeriodoAnterior);
			System.out.println("\tnumTotalLicitacionesPeriodoAnterior = " + numTotalLicitacionesPeriodoAnterior);
			System.out.println("\tporcentajeLicitacionesPeriodoActual = " + porcentajeLicitacionesPeriodoActual);
			System.out.println("\tporcentajeLicitacionesPeriodoAnterior = " + porcentajeLicitacionesPeriodoAnterior);
			System.out.println("\tincrementoLicitaciones = " + incrementoLicitaciones);
			if (incrementoLicitaciones > tendencia.getIncremento(nombrePeriodo))
				registrarAvisoTendencia(tendencia, porcentajeLicitacionesPeriodoAnterior, porcentajeLicitacionesPeriodoActual, AvisoTendencia.LICITACIONES, nombrePeriodo);
			else
				System.out.println("Incremento: " + incrementoLicitaciones + " menor que (" + tendencia.getIncremento(nombrePeriodo) +") " + tendencia.getIncremento(nombrePeriodo));
		}
		if(tendencia.getIndicadorPatentes()){
			int num1 = Searcher.verNumPatentes(fecha3, fecha2, tendencia);
			int num2 = Searcher.verNumPatentes(fecha2, hoy, tendencia);
			int numPatentesPeriodoActual = Searcher.verNumPatentes(fecha2, hoy, tendencia);
			int numTotalPatentesPeriodoActual = Searcher.verNumPatentes(fecha2, hoy, tendenciaClon);
			int numPatentesPeriodoAnterior = Searcher.verNumPatentes(fecha3, fecha2, tendencia);
			int numTotalPatentesPeriodoAnterior = Searcher.verNumPatentes(fecha3, fecha2, tendenciaClon);
			double porcentajePatentesPeriodoActual = 100d * (double)numPatentesPeriodoActual / (double)numTotalPatentesPeriodoActual;
			double porcentajePatentesPeriodoAnterior = 100d * (double)numPatentesPeriodoAnterior / (double)numTotalPatentesPeriodoAnterior;
			double incrementoPatentes = Math.abs(porcentajePatentesPeriodoActual - porcentajePatentesPeriodoAnterior);
			System.out.println("Patentes:");
			System.out.println("\tnumPatentesPeriodoActual = " + numPatentesPeriodoActual);
			System.out.println("\tnumTotalPatentesPeriodoActual = " + numTotalPatentesPeriodoActual);
			System.out.println("\tnumPatentesPeriodoAnterior = " + numPatentesPeriodoAnterior);
			System.out.println("\tnumTotalPatentesPeriodoAnterior = " + numTotalPatentesPeriodoAnterior);
			System.out.println("\tporcentajePatentesPeriodoActual = " + porcentajePatentesPeriodoActual);
			System.out.println("\tporcentajePatentesPeriodoAnterior = " + porcentajePatentesPeriodoAnterior);
			System.out.println("\tincrementoPatentes = " + incrementoPatentes);
			if (incrementoPatentes > tendencia.getIncremento(nombrePeriodo))
				registrarAvisoTendencia(tendencia, porcentajePatentesPeriodoAnterior, porcentajePatentesPeriodoActual, AvisoTendencia.PATENTES, nombrePeriodo);
			else
				System.out.println("Incremento: " + incrementoPatentes + " menor que (" + tendencia.getIncremento(nombrePeriodo) +") " + tendencia.getIncremento(nombrePeriodo));

		}		
	}

	private static void registrarAvisoTendencia(Tendencia tendencia, Double pcPeriodoAnterior, Double pcPeriodoActual, String tipoTendencia, String nombrePeriodo) {
		System.out.println("Registrando aviso");
		AvisoTendencia avisoTendencia = new AvisoTendencia(tendencia, pcPeriodoAnterior, pcPeriodoActual, tipoTendencia, nombrePeriodo);
		System.out.println(avisoTendencia);
		
	}

}
