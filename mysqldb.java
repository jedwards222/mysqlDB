/*
  Lab 2e
  James Edwards and Shashwat Chaturvedi


 */
import java.sql.*;

public class mysqldb {
  public static final String SERVER   = "jdbc:mysql://sunapee.cs.dartmouth.edu/";
  public static final String USERNAME = "user"; // Fill in with credentials
  public static final String PASSWORD = "pass"; // Fill in with credentials
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

        // 1. Prompt user to sign in/ sign up

        // 2. Handle user differently depending on if they are E/R/A
          // a different handler function for each of them?

        // 3. come back to main once a user signs out and let main close the connection


        /* This code is a good example, but not to be used by our program
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
        */

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
