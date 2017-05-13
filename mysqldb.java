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
    System.out.println("You are in EDITOR Mode.\nPlease register or login.");
    System.out.println("Type 'help' for list of commands.");
    Scanner s = new Scanner(System.in);
    boolean finished = false;
    try {
      while (!finished) {
        String action = s.next();
        int id;
        if (action.equals("register")) {
          // read fname and lname, ensure no errors, create user in DB
          String fname = s.next();
          String lname = s.next();
          // Error detection?

          // Insert new editor
          String query = "INSERT INTO Editor (editor_lname, editor_fname) VALUES";
          query += (" (\"" + lname + "\", \"" + fname + "\");");
          Statement stmt = con.createStatement();
          stmt.executeUpdate(query);

          // Get new editor's ID
          ResultSet res = stmt.executeQuery("SELECT LAST_INSERT_ID()");
          int lastId = -1;
          if (res.next()) {
            lastId = res.getInt(1);
            System.out.print("Welcome " + fname + " " + lname + "! ");
            System.out.println("Your userID is: " + lastId);
            handleEditorLoggedIn(con, lastId); // separate function for logged in use
            finished = true;
          }
          else {
            System.out.println("ERROR: SQL problem getting ID");
          }
        }
        else if (action.equals("login")) {
          id = s.nextInt();
          ResultSet res = validId(con, 'e', id);
          if (!res.next()) {
            System.out.println("ERROR: Invalid ID");
          } else {
            // Send welcome message
            String lname = res.getObject(2).toString();
            String fname = res.getObject(3).toString();
            System.out.println("Welcome " + fname + " " + lname + "! ");

            // Print out status of manuscripts
            editorStatus(con);

            handleEditorLoggedIn(con, id);
            finished = true;
          }
        }
        else if (action.equals("help")) {
          editorHelp();
        }
        else if (action.equals("quit")) {
          finished = true;
        }
        else {
          System.out.println("ERROR: Invalid command");
          editorHelp();
        }
      }
    }
    catch (SQLException exception) {
      exception.printStackTrace();
    }
    finally {
      s.close();
    }
  }

  public static void editorStatus(Connection con) {
    try {
      String query = "SELECT manuscript_id, manuscript_title, "
              + "manuscript_update_date, manuscript_status, author_id, editor_id "
              + "FROM Manuscript ORDER BY manuscript_status, manuscript_id";
      Statement stmt = con.createStatement();
      ResultSet res = stmt.executeQuery(query);
      printQuery(query, res);
      res.close();
      stmt.close();
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static void handleEditorLoggedIn(Connection con, int id) {
    System.out.println("\nType 'help' for possible commands");
    Scanner s = new Scanner(System.in);
    boolean finished = false;
    int manID, issueID;
    Statement stmt = null;
    while (!finished) {
      try {
        stmt = con.createStatement();
        String action = s.next();
        switch (action) {
          case "status":
            editorStatus(con);
            break;
          case "assign":
            manID = s.nextInt();
            int revID = s.nextInt();

            // Check that the manuscript has a valid status
            PreparedStatement statusQuery = con.prepareStatement(
              "SELECT manuscript_status FROM Manuscript WHERE manuscript_id = ?");
            statusQuery.setInt(1, manID);
            ResultSet statusRes = statusQuery.executeQuery();
            if (!statusRes.next()) {
              System.out.println("ERROR: invalid manID");
            }
            else {
              String status = statusRes.getObject(1).toString();
              if (status.equals("Submitted") || status.equals("UnderReview")) {
                // Check that the reviewer's aoi code matches that of the manuscript
                PreparedStatement assignQuery = con.prepareStatement(
                  "SELECT aoi_ri_code FROM Manuscript WHERE manuscript_id = ? AND aoi_ri_code IN "
                  + "(SELECT aoi_ri_code FROM Reviewer_aoi WHERE reviewer_id = ?)");
                assignQuery.setInt(1, manID);
                assignQuery.setInt(2, revID);
                ResultSet res = assignQuery.executeQuery();

                if (res.next()) { // valid reviewer
                  // create the review
                  PreparedStatement assignUpdate = con.prepareStatement(
                    "INSERT INTO Review (manuscript_id, reviewer_id, review_date_sent) "
                    + "VALUES (?, ?, NOW())");
                  assignUpdate.setInt(1, manID);
                  assignUpdate.setInt(2, revID);
                  assignUpdate.executeUpdate();

                  // update the manuscript's status
                  PreparedStatement statusUpdate = con.prepareStatement(
                    "UPDATE Manuscript SET manuscript_status = \"UnderReview\" "
                    + "SET manuscript_update_date = NOW() WHERE manuscript_id = ?");
                  statusUpdate.setInt(1, manID);
                  statusUpdate.executeUpdate();
                }
                else {
                  System.out.println("That reviewer has no experience in the given "
                                + "manuscript's subject. Try a different reviewer");
                }
              }
              else {
                System.out.println("ERROR: Manuscript is not in valid state");
              }
            }
            break;
          case "reject":
            PreparedStatement rejectQuery = con.prepareStatement(
              "UPDATE Manuscript SET manuscript_status = \"Rejected\", manuscript_update_date = NOW() WHERE manuscript_id = ?");
            rejectQuery.setInt(1, s.nextInt());
            rejectQuery.executeUpdate();
            break;
          case "accept":
            PreparedStatement acceptQuery = con.prepareStatement(
              "UPDATE Manuscript SET manuscript_status = \"Accepted\", manuscript_update_date = NOW() WHERE manuscript_id = ?");
            acceptQuery.setInt(1, s.nextInt());
            acceptQuery.executeUpdate();
            break;
//TODO: add to typeset, query to update man status
          case "typeset":
            // Get args
            manID = s.nextInt();
            int numPages = s.nextInt();

            // Check that the manuscript is currently in the Accepted state
            PreparedStatement stateQuery = con.prepareStatement(
              "SELECT manuscript_status FROM Manuscript WHERE manuscript_id = ?");
            stateQuery.setInt(1, manID);
            ResultSet stateRes = stateQuery.executeQuery();
            if (stateRes.next()) {
              if (stateRes.getObject(1).toString().equals("Accepted")) {
                // Insert a new article
                PreparedStatement insertArticle = con.prepareStatement(
                  "INSERT INTO Article (manuscript_id, article_num_pages) VALUES "
                    + "(?, ?)");
                insertArticle.setInt(1, manID);
                insertArticle.setInt(2, numPages);
                insertArticle.executeUpdate();
              }
              else {
                System.out.println("ERROR: manuscript in invalid state");
              }
            }
            else {
              System.out.println("ERROR: manuscript not found");
            }
            break;

          case "schedule":
            // Get args
            manID = s.nextInt();
            issueID = s.nextInt();

            // Make sure manuscript is 'Typeset'
            PreparedStatement typeQuery = con.prepareStatement(
              "SELECT manuscript_status FROM Manuscript WHERE manuscript_id = ?");
            typeQuery.setInt(1, manID);
            ResultSet typeRes = typeQuery.executeQuery();
            if (typeRes.next()) {
              if (typeRes.getObject(1).toString().equals("Typeset")) {

                // Make sure article's issue is NULL
                PreparedStatement artIssQuery = con.prepareStatement(
                  "SELECT issue_id FROM Article WHERE manuscript_id = ?");
                artIssQuery.setInt(1, manID);
                ResultSet artIssRes = artIssQuery.executeQuery();
                if (!artIssRes.next()) {
                  System.out.println("ERROR: issue not found");
                  break;
                }
                if (!artIssRes.getObject(1).equals(null)) {
                  System.out.println("ERROR: article already scheduled");
                  break;
                }

                // Get the row for the article with highest starting page for current issue from article
                PreparedStatement orderQuery = con.prepareStatement(
                  "SELECT article_order_num, article_num_pages, article_start_page"
                  +" FROM Article WHERE issue_id = ? ORDER BY article_order_num;");
                orderQuery.setInt(1, issueID);
                ResultSet orderRes = orderQuery.executeQuery();
                int oldLength = 0;
                int oldStart = 1;
                int oldOrder = 0;
                while (orderRes.next()) {
                  oldOrder = orderRes.getInt(1);
                  oldLength = orderRes.getInt(2);
                  oldStart = orderRes.getInt(3);
                }
                int order = oldOrder + 1;

                // Get current manuscript's article page length
                PreparedStatement lengthQuery = con.prepareStatement(
                  "SELECT article_num_pages FROM Article WHERE manuscript_id = ?");
                lengthQuery.setInt(1, manID);
                ResultSet lengthResult = lengthQuery.executeQuery();
                if (!lengthResult.next()) {
                  System.out.println("ERROR: issue not found");
                  break;
                }
                int length = lengthResult.getInt(1);

                // Add starting page to that article length, add current article length
                int start = oldStart + oldLength;
                int sum = start + length;

                // Check if this new value is > 100
                if (sum > 100) {
                  System.out.println("ERROR: The given issue has no room for that article.");
                }
                else {
                  // Update Article for given man_id
                  PreparedStatement scheduleArticle = con.prepareStatement(
                    "UPDATE Article SET article_order_num = ?, article_start_page = ?"
                    + ", issue_id = ?) WHERE manuscript_id = ? ");
                  scheduleArticle.setInt(1, order);
                  scheduleArticle.setInt(2, start);
                  scheduleArticle.setInt(3, issueID);
                  scheduleArticle.setInt(4, manID);
                  scheduleArticle.executeUpdate();
                }

              }
              else {
                System.out.println("ERROR: manuscript in invalid state");
              }
            }
            else {
              System.out.println("ERROR: manuscript not found");
            }


            break;

          case "create":
            // Get args
            int issueYear = s.nextInt();
            int issuePeriod = s.nextInt();

            // Create issue
            PreparedStatement createIssue = con.prepareStatement(
              "INSERT INTO Issue (issue_year, issue_period) "
              + "VALUES (?, ?)");
            createIssue.setInt(1, issueYear);
            createIssue.setInt(2, issuePeriod);
            createIssue.executeUpdate();

            // Return Issue ID
            ResultSet res = stmt.executeQuery("SELECT LAST_INSERT_ID()");
            issueID = -1;
            if (res.next()) {
              issueID = res.getInt(1);
              System.out.println("Your issue has been created with ID = " + issueID);
            }
            else {
              System.out.println("ERROR: Issue not correctly created");
            }
            break;
          case "publish":
            // Get args
            issueID = s.nextInt();
            // Check the issue has at least one article
            PreparedStatement issueCheck = con.prepareStatement(
              "SELECT COUNT(*) FROM Article WHERE issue_id = ?");
            issueCheck.setInt(1, issueID);
            ResultSet articleCount = issueCheck.executeQuery();
            if (articleCount.next()) {
              if (articleCount.getInt(1) > 0) {

                // Update all manuscripts from this issue status to published
                PreparedStatement manuscriptsUpdate = con.prepareStatement(
                  "UPDATE Manuscript SET manuscript_status = \"Published\", "
                  + "manuscript_update_date = NOW() WHERE manuscript_id IN "
                  + "(SELECT Mans.manuscript_id FROM (SELECT * FROM "
                  + "Manuscript NATURAL JOIN Article WHERE Article.issue_id = ?)"
                  + " as Mans)");
                manuscriptsUpdate.setInt(1, issueID);
                manuscriptsUpdate.executeUpdate();

                // change issue publish date to current date
                PreparedStatement issueUpdate = con.prepareStatement(
                  "UPDATE Issue SET issue_print_date=NOW() WHERE issue_id = ?");
                issueUpdate.setInt(1, issueID);
                issueUpdate.executeUpdate();

              }
              else {
                System.out.println("ERROR: That issue has no articles");
              }
            }
            else {
              System.out.println("ERROR: Failed to check issue validity");
            }
            break;
          case "logout":
            finished = true;
            break;
          case "help":
            editorLoggedInHelp();
            break;
          default:
            System.out.println("ERROR: Invalid command");
            editorLoggedInHelp();
            break;
        }
      }
      catch (SQLException exception) {
        exception.printStackTrace();
      }
      finally {
        try {
          stmt.close();
        }
        catch (Exception e) { /* do nothing */ }
      }
    }
    s.close();
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
    System.out.format("%-40s","Return to main menu:");
    System.out.println("quit");
  }
  public static void editorLoggedInHelp() {
    System.out.println("---------------Authorized Editor Commands---------------");
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
    System.out.format("%-40s","Create an issue: ");
    System.out.println("create <issue_year> <issue_period>");
    System.out.format("%-40s","Publish an issue: ");
    System.out.println("publish <issue_id>");
    System.out.format("%-40s","Return to main menu:");
    System.out.println("logout");
  }

  /* Help output for user listing author commands */
  public static void authorHelp() {
    System.out.println("-----------------------Author Commands-----------------------");
    System.out.println("Note: some commands will prompt user for further input after the initial command");
    System.out.format("%-40s", "Register as a new user: ");
    System.out.println("register <fname> <lname>");
    System.out.format("%-40s","Login as a returning user:");
    System.out.println("login <id>");
    System.out.format("%-40s","Return to main menu:");
    System.out.println("quit");
  }
  public static void authorLoggedInHelp() {
    System.out.println("---------------Authorized Author Commands---------------");
    System.out.println("Note: some commands will prompt user for further input after the initial command");
    System.out.format("%-40s","Submit a manuscript: ");
    System.out.println("submit <title> <author_affiliation> <ri_code>");
    System.out.format("%-40s","See status of submitted manuscripts:");
    System.out.println("status");
    System.out.format("%-40s","Retract a manuscript: ");
    System.out.println("retract <manuscript_id>");
    System.out.format("%-40s","Return to main menu:");
    System.out.println("logout");
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
    System.out.format("%-40s","Return to main menu:");
    System.out.println("quit");
  }
  public static void reviewerLoggedInHelp() {
    System.out.println("---------------Authorized Reviewer Commands---------------");
    System.out.println("Note: some commands will prompt user for further input after the initial command");
    System.out.format("%-40s","See status of current reviewed manuscripts:");
    System.out.println("status");
    System.out.format("%-40s","Conduct a review: ");
    System.out.println("review <manuscript_id>");
    System.out.format("%-40s","Return to main menu:");
    System.out.println("logout");
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
