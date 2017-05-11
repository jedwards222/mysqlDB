/*
  Lab 2e
  James Edwards and Shashwat Chaturvedi


 */
import java.sql.*;

public class dbexampleccp {
    public static final String SERVER   = "jdbc:mysql://sunapee.cs.dartmouth.edu/";
    public static final String USERNAME = "ccpalmer";
    public static final String PASSWORD = "bug7!Ear23m";
    public static final String DATABASE = "cs61";
    public static final String QUERY    = "SELECT * FROM instructor;";

    public static void main(String[] args) {
		Connection con = null;
		Statement stmt = null;
		ResultSet res  = null;
		int numColumns = 0;

		// attempt to connect to db
		try {
		    // load mysql driver
		    Class.forName("com.mysql.jdbc.Driver").newInstance();
		    
		    // initialize connection
		    con = DriverManager.getConnection(SERVER+DATABASE, USERNAME, PASSWORD);
		    
		    System.out.println("Connection established.");

		    // initialize a query statement
		    stmt = con.createStatement();

		    // query db and save results
		    res = stmt.executeQuery(QUERY);

		    System.out.format("Query executed: '%s'\n\nResults:\n", QUERY);
		    
		    // the result set contains metadata
		    numColumns = res.getMetaData().getColumnCount();

		    // print table header
		    for(int i = 1; i <= numColumns; i++) {
				System.out.format("%-12s", res.getMetaData().getColumnName(i));
		    }
		    System.out.println("\n--------------------------------------------");

		    // iterate through results
		    while(res.next()) {
				for(int i = 1; i <= numColumns; i++) {
				    System.out.format("%-12s", res.getObject(i));
				}
				System.out.println("");
		    }
		} catch (SQLException e ) {          // catch SQL errors
		    System.err.format("SQL Error: %s", e.getMessage());
		} catch (Exception e) {              // anything else
		    e.printStackTrace();
		} finally {
		    // cleanup
		    try {
				con.close();
				stmt.close();
				res.close();
				System.out.print("\nConnection terminated.\n");
		    } catch (Exception e) { /* ignore cleanup errors */ }
		}
    }
}