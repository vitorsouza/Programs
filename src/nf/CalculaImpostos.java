package nf;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Program that calculates the taxes that should be paid by the taker of an invoice.
 * 
 * I wrote this program in order to help me produce invoices for my company. A note is added to each invoice informing
 * the client about the taxes he should deduct from the total amount due and pay themselves.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class CalculaImpostos {
	public static BigDecimal valorNF = new BigDecimal("19600");

	public static final String[] nomeImpostos = new String[] { "PIS", "COFINS", "CSLL", "IR" };

	public static final BigDecimal[] percentualImpostos = new BigDecimal[] { new BigDecimal("0.65"), new BigDecimal("3"), new BigDecimal("1"), new BigDecimal("1.5") };

	public static final BigDecimal cem = new BigDecimal("100");

	public static void main(String[] args) {
		Locale.setDefault(new Locale("pt", "BR"));
		NumberFormat nf = NumberFormat.getCurrencyInstance();

		if (args.length > 0) try {
			valorNF = new BigDecimal(args[0]);
		}
		catch (Exception e) {
			System.out.println("Invalid value: " + args[0]);
			System.exit(1);
		}

		BigDecimal somaPercentual = new BigDecimal("0");
		BigDecimal somaValor = new BigDecimal("0");

		String[] valores = new String[nomeImpostos.length];
		for (int i = 0; i < nomeImpostos.length; i++) {
			BigDecimal valor = valorNF.multiply(percentualImpostos[i]).divide(cem);
			somaPercentual = somaPercentual.add(percentualImpostos[i]);
			somaValor = somaValor.add(valor);
			valores[i] = nf.format(valor);
			System.out.println(nomeImpostos[i] + " (" + percentualImpostos[i] + "%) = " + valores[i]);
		}

		System.out.println("\nTotal (" + somaPercentual + "%) = " + nf.format(somaValor));

		System.out.println("\n-----------------\n");
		System.out.printf("6,15%% de impostos (%s) deverao ser retidos pelo tomador referentes a: 0,65%% de PIS (%s); 3%% de COFINS (%s); 1%% de CSLL (%s); e 1,5%% de IR (%s). Dados bancarios para o deposito: Banco do Brasil, agencia 4292-7, conta corrente 100.098-5.%n%n", nf.format(somaValor), valores[0], valores[1], valores[2], valores[3]);
	}
}
