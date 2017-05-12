/*
  Lab 2e
  James Edwards and Shashwat Chaturvedi


 */
import java.sql.*;
import java.util.Scanner;
import java.util.regex.*;

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
		    // Class.forName("com.mysql.jdbc.Driver").newInstance();

		    // initialize connection
		    // con = DriverManager.getConnection(SERVER+DATABASE, USERNAME, PASSWORD);

        Scanner s = new Scanner(System.in);
        // Prompt user to indicate whether they are author/editor/reviewer
        System.out.println("Type 'a', 'e', or 'r' to indicate your job (author, editor, reviewer)");
        boolean success = false;
        while (!success) {
          success = true;
          char resp = s.next().charAt(0);
          switch (resp) {
            case 'a':
              handleAuthor(con);
              break;
            case 'e':
              handleEditor(con);
              break;
            case 'r':
              handleReviewer(con);
              break;
            default:
              System.out.println("That is an invalid entry, try 'a', 'e', or 'r'");
              success = false;
              break;
            }
          }

        // Come back to main once a user signs out and let main close the connection


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
    //
		} catch (SQLException e ) {          // catch SQL errors
		    System.err.format("SQL Error: %s", e.getMessage());
		} catch (Exception e) {              // anything else
		    e.printStackTrace();
		} finally {
		  // cleanup
		  try {
  			// con.close();
  			// stmt.close();
  			// res.close();
  			System.out.print("\nConnection terminated.\n");
	    } catch (Exception e) { /* ignore cleanup errors */ }
		}
  }

  public static void handleAuthor(Connection con) {
    System.out.println("You are in AUTHOR Mode.\nPlease register or login");
    Scanner a = new Scanner(System.in);
    String action = a.next();
    // Pattern p = Pattern.compile("");
    // Matcher m = p.matcher("as");
    if (action == "register") {

    } else if (action == "login") {

    } else {
      System.out.println("Invalid command");
      return; // probably want better error handling...
    }
  }

  /*
  handleEditor() - handle registering or logging in when in Editor mode
  Return when error detected in user input
  */
  public static void handleEditor(Connection con) {
    System.out.println("You are in EDITOR Mode.\nPlease register or login");
    Scanner e = new Scanner(System.in);
    boolean finished = false;
    while (!finished) {
      String action = e.next();
      int id;
      // Pattern p = Pattern.compile("");
      // Matcher m = p.matcher("as");
      if (action == "register") {
        // read fname and lname, ensure no errors, create user in DB
        String fname = e.next();
        String lname = e.next();
        // Error detection?

        String query = "INSERT INTO Editor (editor_lname, editor_fname) VALUES";
        query += ("(" + lname + "," + fname + ");");
        // initialize a query statement
        Statement stmt = con.createStatement();
        // query db and save results
        ResultSet res = stmt.executeQuery(query);

        // check result? user is now logged in?

      } else if (action == "login") {
        id = e.nextInt();
        if (!validId(con, 'e', id)) {
          System.out.println("Invalid ID");
          return;
        } else {
          // USER IS LOGGED IN - show them their stuff
        }
      } else {
        System.out.println("Invalid command");
        return; // probably want better error handling...
      }
    }
  }


  public static void handleReviewer(Connection con) {
    // Check whether they would like to login, register, or resign
    System.out.println("You are in REVIEWER Mode.\nPlease register, login, or resign");
    Scanner r = new Scanner(System.in);
    String action = r.next();
    // Pattern p = Pattern.compile("");
    // Matcher m = p.matcher("as");
    if (action == "register") {

    } else if (action == "login") {

    } else if (action == "resign") {

    } else {
      System.out.println("Invalid command");
      return; // probably want better error handling...
    }
  }

  /*
    validId constructs a query to check if the given user exists in the db
    Returns true if user exists, otherwise returns false
  */
  public static boolean validId(Connection con, char mode, int id) {
    if (id < 0) {
      return false;
    }
    // Do SQL query
    String query = "SELECT * FROM ";
    switch (mode) { // will not reach here with invalid mode
      case 'a':
        query += "Author WHERE author_id = ";
        break;
      case 'e':
        query += "Editor WHERE editor_id = ";
        break;
      case 'r':
        query += "Reviewer WHERE reviewer_id = ";
        break;
    }
    query += id;
    // initialize a query statement
    Statement stmt = con.createStatement();
    // query db and save results
    ResultSet res = stmt.executeQuery(query);
    return (res.next()); // true if valid ID
  }
}
