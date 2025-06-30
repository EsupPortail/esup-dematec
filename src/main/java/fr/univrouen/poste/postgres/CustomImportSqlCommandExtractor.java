package fr.univrouen.poste.postgres;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.dialect.Dialect;
import org.hibernate.tool.schema.spi.SqlScriptCommandExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple Hack as workaround to mix MultipleLinesSqlCommandExtractor and SingleLineSqlCommandExtractor ...
 * Initial problem is described here : <a href="https://hibernate.atlassian.net/browse/HHH-2403">HHH-2403</a>
 *
 * Migrated to Hibernate 7 using SqlScriptCommandExtractor
 */
public class CustomImportSqlCommandExtractor implements SqlScriptCommandExtractor {

	Logger log = LoggerFactory.getLogger(CustomImportSqlCommandExtractor.class);

	int fileNum = 0;
	
	@Override
	public List<String> extractCommands(Reader reader, Dialect dialect) {
		List<String> commands;

		try (BufferedReader bufferedReader = new BufferedReader(reader)) {
			if (fileNum == 0) {
				// First file: use multi-line SQL command extraction (like import.sql)
				commands = extractMultiLineCommands(bufferedReader);
			} else {
				// Other files: use single-line SQL command extraction (like import-one-line.sql)
				commands = extractSingleLineCommands(bufferedReader);
			}
			fileNum++;
		} catch (IOException e) {
			throw new RuntimeException("Error reading SQL import file", e);
		}

		log.warn("SQL commands extracted from file #" + fileNum + ": \n\n" + StringUtils.join(commands,"\n\n") + "\n\n");

		return commands;
	}

	/**
	 * Extract multi-line SQL commands separated by semicolons
	 */
	List<String> extractMultiLineCommands(BufferedReader reader) throws IOException {
		List<String> commands = new ArrayList<>();
		StringBuilder command = new StringBuilder();
		String line;

		while ((line = reader.readLine()) != null) {
			line = line.trim();

			// Skip empty lines and comments
			if (line.isEmpty() || line.startsWith("--") || line.startsWith("//") || line.startsWith("#")) {
				continue;
			}

			command.append(line).append(" ");

			// Check if the line ends with a semicolon (end of command)
			if (line.endsWith(";")) {
				String sql = command.toString().trim();
				// Remove the trailing semicolon
				if (sql.endsWith(";")) {
					sql = sql.substring(0, sql.length() - 1).trim();
				}
				if (!sql.isEmpty()) {
					commands.add(sql);
				}
				command = new StringBuilder();
			}
		}

		// Add any remaining command
		String remainingCommand = command.toString().trim();
		if (!remainingCommand.isEmpty()) {
			if (remainingCommand.endsWith(";")) {
				remainingCommand = remainingCommand.substring(0, remainingCommand.length() - 1).trim();
			}
			if (!remainingCommand.isEmpty()) {
				commands.add(remainingCommand);
			}
		}

		return commands;
	}

	/**
	 * Extract single-line SQL commands (one command per line)
	 */
	List<String> extractSingleLineCommands(BufferedReader reader) throws IOException {
		List<String> commands = new ArrayList<>();
		String line;

		while ((line = reader.readLine()) != null) {
			line = line.trim();

			// Skip empty lines and comments
			if (line.isEmpty() || line.startsWith("--") || line.startsWith("//") || line.startsWith("#")) {
				continue;
			}

			// Remove trailing semicolon if present
			if (line.endsWith(";")) {
				line = line.substring(0, line.length() - 1).trim();
			}

			if (!line.isEmpty()) {
				commands.add(line);
			}
		}

		return commands;
	}

}
