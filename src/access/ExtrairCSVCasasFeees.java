package access;

import java.util.Collections;

import com.healthmarketscience.jackcess.CursorBuilder;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;

/**
 * TODO: document this type.
 * 
 * I created this script to extract some information (in Brazilian Portuguese) I needed from an access database.
 *
 * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class ExtrairCSVCasasFeees {
	/**
	 * TODO: document this method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Database db = DatabaseBuilder.open(Feees.FILE_FEEES_DB);
		Table institutionTable = db.getTable("InstituicaoEspirita");
		Table cityTable = db.getTable("Cidade");

		for (Row row : institutionTable) {
			boolean isCasa = Boolean.parseBoolean(row.get("casaEspirita").toString());
			boolean isAdesa = Boolean.parseBoolean(row.get("adesa").toString());
			boolean isDesativada = Boolean.parseBoolean(row.get("desativada").toString());

			if (isCasa && isAdesa && (!isDesativada)) {
				StringBuilder builder = new StringBuilder();
				builder.append('(');
				builder.append(row.get("sigla"));

				Object codCidade = row.get("codCidade");

				Row cityRow = CursorBuilder.findRow(cityTable, Collections.singletonMap("codCidade", codCidade));
				if (cityRow != null) {
					builder.append(", ");
					builder.append(cityRow.get("nome"));
				}

				builder.append(')');

				System.out.println(row.get("nome") + ";" + builder);
			}
		}
	}

}
