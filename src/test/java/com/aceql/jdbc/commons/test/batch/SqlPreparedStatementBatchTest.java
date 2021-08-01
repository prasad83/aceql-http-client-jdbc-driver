package com.aceql.jdbc.commons.test.batch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import com.aceql.jdbc.commons.AceQLConnection;
import com.aceql.jdbc.commons.test.SqlDeleteTest;
import com.aceql.jdbc.commons.test.SqlSelectTest;
import com.aceql.jdbc.commons.test.connection.DirectConnectionBuilder;

public class SqlPreparedStatementBatchTest {
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
	Connection connection = new DirectConnectionBuilder().createPostgreSql();
	callInsertFlow(connection);
    }

    /**
     * @param connection
     * @throws SQLException
     */
    public static void callInsertFlow(Connection connection) throws SQLException {
	if (connection instanceof AceQLConnection) {
	    System.out.println("Connection is an AceQLConnection!");
	}

	SqlDeleteTest sqlDeleteTest = new SqlDeleteTest(connection, System.out);
	sqlDeleteTest.deleteCustomerAll();
	insertWithBatch(connection);
	SqlSelectTest sqlSelectTest = new SqlSelectTest(connection, System.out);
	sqlSelectTest.selectCustomerPreparedStatement();
    }

    private static void insertWithBatch(Connection connection) throws SQLException {
	//final String sql = "insert into customer values ({0}, 'Sir', 'Doe', 'Andr�', '1600 Pennsylvania Ave NW', 'Washington', 'DC 20500', NULL)";
	final String sql = "insert into customer values (?, ?, ?, ?, ?, ?, ?, ?)";
	 
	PreparedStatement statement = connection.prepareStatement(sql);
	for (int i = 1; i < 3; i++) {
	    int j = 1;
	    statement.setInt(j++, i);
	    statement.setString(j++, "Sir");
	    statement.setString(j++, "Doe" + i);
	    statement.setString(j++, "Andr�" + i);
	    statement.setString(j++, "1600 Pennsylvania Ave NW" + i);
	    statement.setString(j++, "Washington" + i);
	    statement.setString(j++, "DC 20500" + i);
	    statement.setString(j++,  null);
	    statement.addBatch();
	}
	int[] rc = statement.executeBatch();

	for (int i : rc) {
	    System.out.print(i + " ");
	}
	System.out.println();
    }

    /**
     * @param connection
     * @throws SQLException
     */
    public static void updateWithBatch(Connection connection) throws SQLException {
	connection.setAutoCommit(false);

	String sql = "update customer set customer_title = 'Sir'";
	Statement statement = connection.createStatement();

	StringBuilder sb = new StringBuilder();
	int cpt = 100000;
	System.out.println(new Date() + " Batch Occurences: " + cpt);
	for (int i = 0; i < cpt; i++) {

	    if (i > 0 && cpt % i == 1000) {
		System.out.println(new Date() + " Batch Added: " + i);
		connection.commit();
	    }
	    statement.addBatch(sql);
	    sb.append(sql);
	}

	connection.commit();
	statement.executeBatch();
	statement.close();

	System.out.println(new Date() + " cpt : " + cpt);
	System.out.println(new Date() + " size: " + sb.toString().length() / 1024 + " Kb");

	System.out.println(new Date() + " Done!");
    }

}
