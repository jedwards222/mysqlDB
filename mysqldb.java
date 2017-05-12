/*
  Lab 2e
  James Edwards and Shashwat Chaturvedi
 */

import java.sql.*;
import java.util.Scanner;

public class mysqldb {
  public static final String SERVER   = "jdbc:mysql://sunapee.cs.dartmouth.edu/";
  public static final String USERNAME = "cshashwat"; // Fill in with credentials
  public static final String PASSWORD = "riderevent78"; // Fill in with credentials
  public static final String DATABASE = "cshashwat_db";
  public static final String QUERY    = "SELECT * FROM Author;";

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
    //
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

  public static void handleAuthor(Connection con) {
    System.out.println("You are in AUTHOR Mode.\nPlease register or login.");
    authorHelp();
    Scanner a = new Scanner(System.in);
    boolean completed = false;
    try {
      while (!completed) {
        System.out.print("Command: ");
        String action = a.next();
        if (action.equals("register")) {
          String insertQuery = "INSERT INTO Author (author_lname, " +
            "author_fname, author_address, author_affiliation, author_email) " +
            "VALUES (?, ?, ?, ?, ?)";
          PreparedStatement registerQuery = con.prepareStatement(insertQuery);
          String fName = a.next();
          String lName = a.next();
          System.out.println("Welcome, " + fName + " " + lName + "!");
          registerQuery.setString(1, lName);
          registerQuery.setString(2, fName);
          System.out.print("Please enter a mailing address: ");
          registerQuery.setString(3, a.next());
          System.out.print("Please enter an email address: ");
          registerQuery.setString(4, a.next());
          System.out.print("Please enter an affiliation: ");
          registerQuery.setString(5, a.next());
          registerQuery.executeUpdate();

          Statement getAuthorID = con.createStatement();
          ResultSet authorID =
            getAuthorID.executeQuery("SELECT LAST_INSERT_ID()");
          if (authorID.next()) {
            System.out.println("Your author ID is " + authorID.getObject(1));
            completed = true;
          } else {
            System.out.println("Error. Please try again.");
          }
        } else if (action.equals("login")) {
          PreparedStatement loginQuery = con.prepareStatement(
            "SELECT * FROM Author WHERE author_id = ?");
          loginQuery.setInt(1, Integer.parseInt(a.next()));
          ResultSet result = loginQuery.executeQuery();
          if (!result.next()) {
            System.out.println("That ID is invalid. Please try again.");
          } else {
            System.out.println("Welcome " +
              result.getString("author_fname") + " " +
              result.getString("author_lname") + "!");
            completed = true;
          }
        } else {
          System.out.println("That command is invalid.");
          System.out.println("\n\n");
          authorHelp();
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      System.out.println("Seems like there were errors with your syntax. " +
        "Please try again. Remember: fill out every field!");
      System.out.println("\n\n");
      handleAuthor(con);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Seems like there was a system error. " +
        "Please try again.");
      System.out.println("\n\n");
      handleAuthor(con);
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
    try {
      while (!finished) {
        String action = e.next();
        int id;
        if (action.equals("register")) {
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

          // TODO: check result? user is now logged in?
          // how do we know which ID this editor now has?

        } else if (action.equals("login")) {
          id = e.nextInt();
          ResultSet res = validId(con, 'e', id);
          if (!res.next()) {
            System.out.println("Invalid ID");
            return;
          } else {
            printQuery(null, res);
            // USER IS LOGGED IN - show them their stuff
            // Query for editor's manuscripts
            System.out.println();
          }
        } else {
          System.out.println("Invalid command");
          return; // probably want better error handling...
        }
      }
    }
    catch (SQLException exception) {
      exception.printStackTrace();
    }
    finally {
      e.close();
    }
  }


  public static void handleReviewer(Connection con) {
    // Check whether they would like to login, register, or resign
    System.out.println("You are in REVIEWER Mode.\nPlease register, login, or resign");
    Scanner r = new Scanner(System.in);
    String action = r.next();
    // Pattern p = Pattern.compile("");
    // Matcher m = p.matcher("as");
    if (action.equals("register")) {

    } else if (action.equals("login")) {

    } else if (action.equals("resign")) {

    } else {
      System.out.println("Invalid command");
      return; // probably want better error handling...
    }
  }

  /*
    validId constructs a query to check if the given user exists in the db
    Returns true if user exists, otherwise returns false

    Maybe should return
  */
  public static ResultSet validId(Connection con, char mode, int id) {
    if (id < 0) {
      return null;
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
    Statement stmt = null;
    ResultSet res = null;
    try {
      // initialize a query statement
      stmt = con.createStatement();
      // query db and save results
      res = stmt.executeQuery(query);
      return res;
    }
    catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }


  /*
    Returns the status of manuscripts associated with the given user
    Prints to stdout and returns nothing
  */
  public static void status(Connection con, char mode, int id) {

  }

  /* Help output for user listing editor commands */
  public static void editorHelp() {
    System.out.println("-----------------------Editor Commands-----------------------");
    System.out.format("%-40s", "Register as a new user: ");
    System.out.println("register <fname> <lname>");
    System.out.format("%-40s","Login as a returning user:");
    System.out.println("login <id>");
    System.out.format("%-40s","See status of your manuscripts: ");
    System.out.println("status");
    System.out.format("%-40s","Assign a manuscript to a reviewer: ");
    System.out.println("assign <manuscript_id> <reviewer_id>");
    System.out.format("%-40s","Reject a manuscript: ");
    System.out.println("reject <manuscript_id>");
    System.out.format("%-40s","Accept a manuscript: ");
    System.out.println("accept <manuscript_id>");
    System.out.format("%-40s","Typeset a manuscript: ");
    System.out.println("typeset <manuscript_id> <num_pages>");
    System.out.format("%-40s","Schedule a manuscript: ");
    System.out.println("schedule <manuscript_id> <issue_id>");
    System.out.format("%-40s","Publish an issue: ");
    System.out.println("publish <issue_id>");
  }

  /* Help output for user listing author commands */
  public static void authorHelp() {
    System.out.println("-----------------------Author Commands-----------------------");
    System.out.println("Note: some commands will prompt user for further input after the initial command");
    System.out.format("%-40s", "Register as a new user: ");
    System.out.println("register <fname> <lname>");
    System.out.format("%-40s","Login as a returning user:");
    System.out.println("login <id>");
    System.out.format("%-40s","Submit a manuscript: ");
    System.out.println("submit <title> <author_affiliation> <ri_code>");
    System.out.format("%-40s","See status of submitted manuscripts:");
    System.out.println("status");
    System.out.format("%-40s","Retract a manuscript: ");
    System.out.println("retract <manuscript_id>");
  }

  /* Help output for user listing reviewer commands */
  public static void reviewerHelp() {
    System.out.println("-----------------------Reviewer Commands-----------------------");
    System.out.println("Note: some commands will prompt user for further input after the initial command");
    System.out.format("%-40s", "Register as a new user: ");
    System.out.println("register <fname> <lname> <email>");
    System.out.format("%-40s","Login as a returning user:");
    System.out.println("login <id>");
    System.out.format("%-40s","Resign as user: ");
    System.out.println("resign <id>");
    System.out.format("%-40s","See status of current reviewed manuscripts:");
    System.out.println("status");
    System.out.format("%-40s","Conduct a review: ");
    System.out.println("review <manuscript_id>");
  }

  /*
    Prints the query in a table format
  */
  public static void printQuery(String query, ResultSet res) {
    // don't need to print query
    if (query != null) {
      System.out.format("Query executed: '%s'\n\nResults:\n", query);
    }
    try {
      int numColumns = res.getMetaData().getColumnCount();
      for(int i = 1; i <= numColumns; i++) {
        System.out.format("%-20s", res.getMetaData().getColumnName(i));
      }
      System.out.println("\n-------------------------------------------------");
      res.beforeFirst();
      while (res.next()) {
        for (int i = 1; i <= numColumns; i++) {
          System.out.format("%-20s", res.getObject(i));
        }
        System.out.println("");
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
  }

}
