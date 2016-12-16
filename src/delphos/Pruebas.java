package delphos;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class Pruebas {

	public static void main(String[] args) {
		Calendar fechaDesde = Calendar.getInstance();
		fechaDesde.set(2015, 10, 3);
		Calendar fechaHasta = Calendar.getInstance();
		fechaHasta.set(2015, 10, 7);
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
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		System.out.println("Periodo Anterior: " + sdf.format(fechaInicioPeriodoAnterior.getTime()) + " - " + sdf.format(fechaFinPeriodoAnterior.getTime()));
		System.out.println("Periodo Actual: " + sdf.format(fechaDesde.getTime()) + " - " + sdf.format(fechaHasta.getTime()));
		System.out.println("Periodo Posterior: " + sdf.format(fechaInicioPeriodoPosterior.getTime()) + " - " + sdf.format(fechaFinPeriodoPosterior.getTime()));
	}

}
