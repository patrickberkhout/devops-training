package com.github.patrickberkhout.devopstraining;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.teiid.adminapi.Admin;
import org.teiid.adminapi.jboss.AdminFactory;
import org.teiid.client.plan.PlanNode;
import org.teiid.jdbc.TeiidStatement;

public class TeiidConnector extends JDBCConnector {

	public Admin getAdmin() throws Exception {
		String host = this.getProperty("admin.host");
		int port = Integer.parseInt(this.getProperty("admin.port"));
		String username = this.getProperty("admin.username");
		String password = this.getProperty("admin.passwors");
		return AdminFactory.getInstance().createAdmin(host, port, username, password.toCharArray());
	}

	@Override
	int query(String sql, JDBCQueryResultFiles resultFiles) {
		boolean debug = false;
		boolean planTxt = false;
		boolean planXml = false;
		boolean resultCSV = false;
		int count = 0;
		if (resultFiles != null && resultFiles instanceof TeiidQueryResultFiles) {
			TeiidQueryResultFiles teiidQueryResultFiles = (TeiidQueryResultFiles) resultFiles;
			debug = teiidQueryResultFiles.getDebugFile() != null;
			planTxt = teiidQueryResultFiles.getPlanTxtFile() != null;
			planXml = teiidQueryResultFiles.getPlanXmlFile() != null;
		}
		resultCSV = resultFiles != null && resultFiles.getResultCSVFile() != null;

		try {

			// create query with optional debug option
			Statement statement = getConnection(5).createStatement();
			if (debug) {
				statement.execute("set showplan debug");
			}
			ResultSet mainResultSet = statement.executeQuery(sql);

			// get the plan description that teiid created for this query
			if (planTxt || planXml) {
				TeiidStatement tstatement = statement.unwrap(TeiidStatement.class);
				statement.setFetchSize(1);
				PlanNode queryPlan = tstatement.getPlanDescription();
				if (planTxt) {
					FileUtils.writeStringToFile(((TeiidQueryResultFiles) resultFiles).getPlanTxtFile(),
							queryPlan.toString());
				}
				if (planXml) {
					FileUtils.writeStringToFile(((TeiidQueryResultFiles) resultFiles).getPlanXmlFile(),
							queryPlan.toXml());
				}
			}

			// write results to file
			if (resultCSV) {
				count = FileUtils.writeResultSetToCSVFile(resultFiles.getResultCSVFile(), mainResultSet);
			} else {
				while (mainResultSet.next()) {
					count++;
				}
			}

			// write the debug output to file
			if (debug) {
				ResultSet planQ = statement.executeQuery("show plan");
				planQ.next();
				FileUtils.writeStringToFile(((TeiidQueryResultFiles) resultFiles).getDebugFile(), planQ.getString(3));
			}
			return count;

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private Connection getConnection() throws SQLException {
		Connection conn;
		String username = this.getProperty("username");
		String password = this.getProperty("password");
		String url = this.getProperty("url");
		conn = DriverManager.getConnection(url, username, password);
		return conn;
	}

	private Connection getConnection(int seconds) throws SQLException {
		if (seconds <= 0) {
			return getConnection();
		}
		try {
			return getConnection();
		} catch (SQLException t) {
			System.out.println("Waiting for connection: " + seconds + " seconds");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			return getConnection(seconds - 1);
		}
	}

	public static class TeiidQueryResultFiles extends JDBCQueryResultFiles {
		private Path planTxtFile;
		private Path planXmlFile;
		private Path debugFile;

		public Path getPlanTxtFile() {
			return planTxtFile;
		}

		public void setPlanTxtFile(Path planTxtFile) {
			this.planTxtFile = planTxtFile;
		}

		public Path getPlanXmlFile() {
			return planXmlFile;
		}

		public void setPlanXmlFile(Path planXmlFile) {
			this.planXmlFile = planXmlFile;
		}

		public Path getDebugFile() {
			return debugFile;
		}

		public void setDebugFile(Path debugFile) {
			this.debugFile = debugFile;
		}

	}
}
