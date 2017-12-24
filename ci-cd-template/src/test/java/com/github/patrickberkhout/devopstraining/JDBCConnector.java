package com.github.patrickberkhout.devopstraining;

import java.nio.file.Path;
import java.util.Properties;

public abstract class JDBCConnector {

	private Properties properties = new Properties();

	protected JDBCConnector withProperties(Properties p) {
		this.properties = p;
		return this;
	}

	abstract int query(String sql, JDBCQueryResultFiles resultFiles);
	
	protected int query(String sql) {
		return query(sql,null);
	};

	protected String getProperty(String key) {
		if (!properties.containsKey(key)) {
			throw new IllegalArgumentException("Missing key " + key);
		}
		return properties.getProperty(key);

	}

	public static class JDBCQueryResultFiles {
		private Path resultCSVFile;

		public Path getResultCSVFile() {
			return resultCSVFile;
		}

		public void setResultCSVFile(Path resultCSVFile) {
			this.resultCSVFile = resultCSVFile;
		}



	}
}
