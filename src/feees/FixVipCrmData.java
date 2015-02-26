package feees;

import java.io.File;
import java.util.Scanner;

/**
 * TODO: document this type.
 * 
 * I created this script to clean up a CSV file in order to upload it to VIP CRM, a CRM tool used by Feees.
 *
 * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class FixVipCrmData {
	private static final String CSV_PATH = "vipcrm.csv";

	public static void main(String[] args) throws Exception {
		File csvFile = new File(CSV_PATH);
		System.out.println("csvFile.exists() = " + csvFile.exists() + "\n");

		try (Scanner scanner = new Scanner(csvFile)) {
			while (scanner.hasNextLine())
				try (Scanner lineScanner = new Scanner(scanner.nextLine())) {
					lineScanner.useDelimiter(";");

					Contato c = new Contato();
					c.funcao = lineScanner.next().trim();
					c.nome = lineScanner.next().trim();
					c.telefone = lineScanner.next().trim();
					if (lineScanner.hasNext()) c.email = lineScanner.next().trim();

					// Função
					c.funcao = "Coordenador do " + c.funcao;

					// Telefone
					if (c.telefone == null) c.telefone = "";
					else if (c.telefone.startsWith("9") || c.telefone.startsWith("8")) c.telefone = "027" + c.telefone;
					else if (c.telefone.startsWith("(")) c.telefone = "0" + c.telefone.substring(1, 3) + c.telefone.substring(4).trim();
					c.telefone = c.telefone.replace(" ", "").replace("-", "");
					if (c.telefone.startsWith("2")) c.telefone = "0" + c.telefone;
					if (c.telefone.length() == 11) c.telefone = c.telefone.substring(0, 3) + "9" + c.telefone.substring(3);

					// Email
					if (c.email == null) c.email = "";

					System.out.println(c);
				}
		}

		System.out.println("\n\nPronto!");
	}
}

class Contato {
	String nome;
	String funcao;
	String telefone;
	String email;

	/** @see java.lang.Object#toString() */
	@Override
	public String toString() {
		return nome + ";" + telefone + ";" + email + ";" + funcao;
	}
}
