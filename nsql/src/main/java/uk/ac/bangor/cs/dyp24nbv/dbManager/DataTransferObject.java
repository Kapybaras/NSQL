package uk.ac.bangor.cs.dyp24nbv.dbManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

@org.springframework.stereotype.Component
public class DataTransferObject implements Transferable<String> {
	Connection conn;
	Statement st;

	public DataTransferObject() throws Exception {
		SimpleDataSource.init("database.properties");
		conn = SimpleDataSource.getConnection();
		st = conn.createStatement();
	}

	@Override
	public ArrayList<ArrayList<String>> getAllparametersFromStatement(String SqlStatement) {
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		try {
			ArrayList<String> temp = new ArrayList<String>();
			ResultSet rs = st.executeQuery(SqlStatement);
			while (rs.next()) {
				temp = new ArrayList<String>();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					temp.add(rs.getString(rs.getMetaData().getColumnName(i)));
				}
				result.add(temp);
			}
			rs.close();
			return result;
		} catch (Exception e) {
			return result;
		}
	}
}