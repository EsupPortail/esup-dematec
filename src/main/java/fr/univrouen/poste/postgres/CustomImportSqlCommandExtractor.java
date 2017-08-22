package fr.univrouen.poste.postgres;

import java.io.Reader;

import org.hibernate.tool.hbm2ddl.ImportSqlCommandExtractor;
import org.hibernate.tool.hbm2ddl.MultipleLinesSqlCommandExtractor;
import org.hibernate.tool.hbm2ddl.SingleLineSqlCommandExtractor;

/**
 * Simple Hack as workaround to mix MultipleLinesSqlCommandExtractor and SingleLineSqlCommandExtractor ...
 * Initial problem is described here : https://hibernate.atlassian.net/browse/HHH-2403
 *
 */
public class CustomImportSqlCommandExtractor implements ImportSqlCommandExtractor {

	private static final long serialVersionUID = 1L;
	
	private int fileNum = 0;
	
	private MultipleLinesSqlCommandExtractor multipleLinesSqlCommandExtractor = new MultipleLinesSqlCommandExtractor();
	
	private SingleLineSqlCommandExtractor singleLinesSqlCommandExtractor = new SingleLineSqlCommandExtractor();
	
	@Override
	public String[] extractCommands(Reader reader) {
		ImportSqlCommandExtractor sqlCommandExtractor = singleLinesSqlCommandExtractor;
		if(fileNum == 0) {
			sqlCommandExtractor =  multipleLinesSqlCommandExtractor;
		} 
		fileNum++;
		return sqlCommandExtractor.extractCommands(reader);
	}

}
