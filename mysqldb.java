/*
  Lab 2e
  James Edwards and Shashwat Chaturvedi
 */

import java.sql.*;
import java.util.Scanner;

public class mysqldb {
  public static final String SERVER   = "jdbc:mysql://sunapee.cs.dartmouth.edu/";
  public static final String USERNAME = "cshashwat";    // Fill in with credentials
  public static final String PASSWORD = "riderevent78"; // Fill in with credentials
  public static final String DATABASE = "cshashwat_db";
  public static final String QUERY    = "SELECT * FROM Author;";

  /*
  main() - connects to DB
  */
  public static void main(String[] args) {
		Connection con = null;
		Statement stmt = null;
		ResultSet res  = null;
		int numColumns = 0;

		try {
		    // load mysql driver
		    Class.forName("com.mysql.jdbc.Driver").newInstance();

		    // initialize connection
		    con = DriverManager.getConnection(SERVER+DATABASE, USERNAME, PASSWORD);

        Scanner s = new Scanner(System.in);
        boolean completed = false;

        // Come back to main once a user signs out and let main close the connection
        while (!completed) {
          // Prompt user to indicate whether they are author/editor/reviewer
          initialHelp();
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
            case 'd':
              System.out.println("See you next time!");
              completed = true;
              break;
            default:
              System.out.println("That is an invalid entry, try 'a', 'e', 'r', or 'd'");
              break;
            }
          }
          s.close();
		}
    catch (SQLException e ) {          // catch SQL errors
		    System.err.format("SQL Error: %s", e.getMessage());
		}
    catch (Exception e) {              // anything else
		    System.err.format("Error: %s", e.getMessage());
		}
    finally {
		  // cleanup
		  try {
  			con.close();
  			stmt.close();
  			res.close();
	    }
      catch (Exception e) { /* ignore cleanup errors */ }
		}
  }

  /*
  handleAuthor() - handle registering or logging in when in Author mode
  */
  public static void handleAuthor(Connection con) {
    System.out.println("You are in AUTHOR Mode.\nPlease register or login.");
    System.out.println("Type 'help' for list of commands.");
    Scanner s = new Scanner(System.in);
    boolean completed = false;

    // loop until author quits
    while (!completed) {
      try {
        System.out.print("Command: ");
        String action = s.next();

        if (action.equals("register")) {
          // Register author in database
          PreparedStatement registerQuery = con.prepareStatement(
            "INSERT INTO Author (author_lname, " +
            "author_fname, author_address, author_affiliation, author_email) " +
            "VALUES (?, ?, ?, ?, ?)");
          String fName = s.next();
          String lName = s.next();
          System.out.println("Welcome, " + fName + " " + lName + "!");
          registerQuery.setString(1, lName);
          registerQuery.setString(2, fName);
          s.nextLine();
          System.out.print("Please enter a mailing address: ");
          registerQuery.setString(3, s.nextLine());
          System.out.print("Please enter an email address: ");
          registerQuery.setString(4, s.nextLine());
          System.out.print("Please enter an affiliation: ");
          registerQuery.setString(5, s.nextLine());
          registerQuery.executeUpdate();

          Statement getAuthorID = con.createStatement();
          // Notify user of his ID
          ResultSet authorID =
            getAuthorID.executeQuery("SELECT LAST_INSERT_ID()");
          if (authorID.next()) {
            System.out.println("Your author ID is " + authorID.getObject(1));
            handleAuthorLoggedIn(con, authorID.getInt(1));
            completed = true;
          }
          registerQuery.close();
          getAuthorID.close();
          authorID.close();
        }
        else if (action.equals("login")) {
          // allow author to log in based on id
          int authorID = Integer.parseInt(s.next());
          ResultSet result = validId(con, 'a', authorID);

          // notify author if id doesn't exist in database
          if (!result.next()) {
            System.out.println("That ID is invalid. Please try again.");
          } else {
            System.out.println("Welcome " +
              result.getString("author_fname") + " " +
              result.getString("author_lname") + "!");
            System.out.println("Your manuscripts: ");
            authorStatus(con, authorID);
            handleAuthorLoggedIn(con, authorID);
            completed = true;
          }

          result.close();
        }
        else if (action.equals("help")) {
          authorHelp();
        }
        else if (action.equals("quit")) {
          completed = true;
        }
        else {
          System.out.println("ERROR: That command is invalid. Try again.");
          authorHelp();
        }
      }
      catch (SQLException e) {
        System.out.println("SQL ERROR: Input is invalid. Try again.");
      }
      catch (Exception e) {
        System.out.println("ERROR: Input is invalid. Try again.");
      }
    }
  }

  /*
  handleAuthorLoggedIn() - handle author interactions with db when logged in
  */
  public static void handleAuthorLoggedIn(Connection con, int authorID) {
    System.out.println("You are now logged in as an Author.");
    System.out.println("Type 'help' for list of commands.\n");
    Scanner s = new Scanner(System.in);
    boolean completed = false;
    while (!completed) {
      try {
        System.out.print("Command: ");
        String action = s.next();

        if (action.equals("status")) {
          // print out author manuscripts
          authorStatus(con, authorID);
        }
        else if (action.equals("retract")) {
          // delete author's chosen manuscripts
          int manuscriptID = s.nextInt();
          System.out.print("Are you sure? (y/n): ");
          String answer = s.next();
          if (answer.equals("y") || answer.equals("Y")) {
            authorManuscriptRetract(con, authorID, manuscriptID);
          }
          else {
            System.out.println("Will not retract manuscript.");
          }
        }
        else if (action.equals("submit")) {
          // submit new manuscript for authors
          PreparedStatement manuscriptQuery = con.prepareStatement(
            "INSERT INTO Manuscript (manuscript_title, manuscript_blob, " +
            "manuscript_update_date, manuscript_status, aoi_ri_code, " +
            "author_id, editor_id) VALUES (?, ?, NOW(), ?, ?, ?, ?)");
          manuscriptQuery.setString(1, s.next());
          authorUpdateAffiliation(con, authorID, s.next());
          int riCode = s.nextInt();

          if (!validRICode(con, riCode)) {
            System.out.println("Invalid RI code");
            Statement possibleAois = con.createStatement();
            printQuery(possibleAois.executeQuery("SELECT * from Aoi"));
            System.out.println("\nHere are the possible RI codes. Try again");
            possibleAois.close();
            continue;
          }

          // check if there are enough reviewers for manuscript
          String query = "SELECT COUNT(*) as num_reviewers FROM Reviewer_aoi " +
            "WHERE aoi_ri_code = " + riCode;
          Statement checkReviewers = con.createStatement();
          ResultSet numReviewers = checkReviewers.executeQuery(query);
          numReviewers.next();
          if (numReviewers.getInt(1) < 3) {
            System.out.println("There aren't enough reviewers for your " +
              "manuscript's area of interest. Please try again later.");
            continue;
          }
          numReviewers.close();
          checkReviewers.close();

          // retrieve an editor to assign to manuscript and notify author
          int editorID = authorEditor(con);
          if (editorID != 0) {
            System.out.print("Please enter the contents of your manuscript: ");
            s.nextLine();
            manuscriptQuery.setString(2, s.nextLine());
            manuscriptQuery.setString(3, "Submitted");
            manuscriptQuery.setInt(4, riCode);
            manuscriptQuery.setInt(5, authorID);
            manuscriptQuery.setInt(6, editorID);
            manuscriptQuery.executeUpdate();
            System.out.println("Manuscript created and assigned to editor " +
            editorID);
            boolean secondaryAuthorsDone = false;
            Statement stmt = con.createStatement();
            ResultSet manID =
              stmt.executeQuery("SELECT LAST_INSERT_ID()");
            manID.next();

            // get all secondary authors for manuscript
            int sAuthorOrderNum = 1;
            while (!secondaryAuthorsDone) {
              PreparedStatement secondaryAuthor = con.prepareStatement(
                "INSERT INTO Secondary_author (sauthor_order_num, " +
                "manuscript_id, sauthor_lname, sauthor_fname, " +
                "sauthor_address, sauthor_email) VALUES (?, ?, ?, ?, ?, ?)");
              System.out.print(
                "Do you have secondary authors to enter? Enter 'y' if so " +
                "(anything else is considerd no): ");
              String answer = s.next();
              if (answer.equals("y") || answer.equals("Y")) {
                System.out.print("Enter last name followed by first name " +
                  "(no commas): ");
                String lName = s.next();
                String fName = s.next();
                s.nextLine();
                System.out.print("Enter address: ");
                String address = s.nextLine();
                System.out.print("Enter email address: ");
                String email = s.next();
                secondaryAuthor.setInt(1, sAuthorOrderNum);
                secondaryAuthor.setInt(2, manID.getInt(1));
                secondaryAuthor.setString(3, lName);
                secondaryAuthor.setString(4, fName);
                secondaryAuthor.setString(5, address);
                secondaryAuthor.setString(6, email);
                secondaryAuthor.executeUpdate();
                sAuthorOrderNum++;
              }
              else {
                System.out.println("Thanks for entering all of your authors.");
                secondaryAuthorsDone = true;
              }
            }
          }
          else {
            // Possible reasons for failure
            System.out.println("Either your RI code is invalid or there are " +
            "currently no editors in the system. Please try again later.");
          }
          manuscriptQuery.close();
        }
        else if (action.equals("help")) {
          authorLoggedInHelp();
        }
        else if (action.equals("logout")) {
          completed = true;
        }
        else {
          System.out.println("That command is invalid.");
          authorLoggedInHelp();
        }
      }
      catch (Exception e) {
        e.printStackTrace();
        System.out.println("ERROR: Invalid input. Try again.");
      }
    }
  }

  /*
  authorStatus() - SQL query to retrieve all manuscripts for author
  */
  public static void authorStatus(Connection con, int authorID) {
    try {
      // presents manuscripts ordered by status, then man id
      String query =
        "SELECT manuscript_id, manuscript_title, manuscript_update_date, " +
        "manuscript_status, aoi_ri_code, editor_id FROM Manuscript " +
        "WHERE author_id = " + authorID + " " +
        "ORDER BY CASE manuscript_status " +
          "WHEN 'Submitted' THEN 1 WHEN 'UnderReview' THEN 2 " +
          "WHEN 'Rejected' THEN 3 WHEN 'Accepted' THEN 4 " +
          "WHEN 'Typeset' THEN 5 WHEN 'Scheduled' THEN 6 " +
          "WHEN 'Published' THEN 7 END, manuscript_id";
      Statement stmt = con.createStatement();
      ResultSet res = stmt.executeQuery(query);
      printQuery(res);
      stmt.close();
      res.close();
    }
    catch (SQLException e) {
      System.out.println("ERROR: Error in database. Try again.");
    }
  }

  /*
  authorManuscriptRetract() - SQL query to delete a manuscript for author
  */
  public static void authorManuscriptRetract(
    Connection con, int authorID, int manuscriptID) {
    try {
      // make sure manuscript is for the author
      // should not delete manuscripts that don't belong to him
      String checkQuery =
        "SELECT * FROM Manuscript WHERE manuscript_id = " + manuscriptID +
        " AND author_id = " + authorID;
      Statement stmt = con.createStatement();
      if (!stmt.executeQuery(checkQuery).next()) {
        System.out.println("Make sure your manuscript id is valid. " +
          "You cannot delete others' manuscripts");
      }
      else {
        String deleteQuery =
          "DELETE FROM Manuscript WHERE manuscript_id = " + manuscriptID;
        stmt = con.createStatement();
        stmt.executeUpdate(deleteQuery);
        System.out.println("Manuscript deleted from system");
      }
      stmt.close();
    }
    catch (SQLException e) {
      System.out.println("ERROR: Invalid input. Try again.");
    }
  }

  /*
  authorUpdateAffiliation() - SQL query to update author's affiliation
  */
  public static void authorUpdateAffiliation(
    Connection con, int authorID, String affiliation) {
    try {
      PreparedStatement stmt = con.prepareStatement(
        "UPDATE Author SET author_affiliation = ? WHERE author_id = ?");
      stmt.setString(1, affiliation);
      stmt.setInt(2, authorID);
      stmt.executeUpdate();
      stmt.close();
    }
    catch (SQLException e) {
      System.out.println("ERROR: Invalid input. Try again.");
    }
  }

  /*
  authorEditor() - SQL query to retrieve editor for new manuscript
  */
  public static int authorEditor(Connection con) {
    try {
      // retrieves editor who currently has least manuscripts assigned
      String query = "SELECT editor_id, MIN(manuscripts_per_editor) FROM " +
      "(SELECT editor_id, COUNT(*) as manuscripts_per_editor FROM " +
      "(SELECT * FROM Manuscript) as editors GROUP BY editor_id) as min_editor";
      Statement stmt = con.createStatement();
      ResultSet result = stmt.executeQuery(query);
      if (!result.next()) {
        return 0;
      }
      else {
        return result.getInt("editor_id");
      }
    }
    catch (SQLException e) {
      System.out.println("SQL ERROR: Database error. Try again.");
      return 0;
    }
  }

  /*
  handleEditor() - handle registering or logging in when in Editor mode
  */
  public static void handleEditor(Connection con) {
    System.out.println("You are in EDITOR Mode.\nPlease register or login.");
    System.out.println("Type 'help' for list of commands.");
    Scanner s = new Scanner(System.in);
    boolean finished = false;
    try {
      while (!finished) {
        System.out.print("Command: ");
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
    catch (SQLException e) {
      System.out.println("SQL ERROR: Input invalid. Try again.");
    }
    catch (Exception e) {
      System.out.println("ERROR: Input invalid. Try again.");
    }
  }

  /*
  editorStatus() - SQL query to retrieve manuscripts for editor
  */
  public static void editorStatus(Connection con) {
    try {
      // order by status and then man id
      String query =
        "SELECT manuscript_id, manuscript_title, manuscript_update_date, " +
        "manuscript_status, author_id, editor_id FROM Manuscript " +
        "ORDER BY CASE manuscript_status " +
          "WHEN 'Submitted' THEN 1 WHEN 'UnderReview' THEN 2 " +
          "WHEN 'Rejected' THEN 3 WHEN 'Accepted' THEN 4 " +
          "WHEN 'Typeset' THEN 5 WHEN 'Scheduled' THEN 6 " +
          "WHEN 'Published' THEN 7 END, manuscript_id";
      Statement stmt = con.createStatement();
      ResultSet res = stmt.executeQuery(query);
      printQuery(res);
      stmt.close();
      res.close();
    }
    catch (SQLException e) {
      System.out.println("SQL ERROR: Database error. Try again.");
    }
  }

  /*
  handleEditorLoggedIn() - handles all commands for editor when logged in
  */
  public static void handleEditorLoggedIn(Connection con, int id) {
    System.out.println("\nType 'help' for possible commands");
    Scanner s = new Scanner(System.in);
    boolean finished = false;
    int manID, issueID;
    Statement stmt = null;
    while (!finished) {
      try {
        stmt = con.createStatement();
        System.out.print("Command: ");
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
                    "UPDATE Manuscript SET manuscript_status = 'UnderReview', "
                    + "manuscript_update_date = NOW() WHERE manuscript_id = ?");
                  statusUpdate.setInt(1, manID);
                  statusUpdate.executeUpdate();
                  System.out.println("Review created and assigned.");
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
            manID = s.nextInt();
            // Check the manuscript is in the underReview status and has 3 completed reviews
            PreparedStatement rstateQuery = con.prepareStatement(
              "SELECT * FROM Manuscript WHERE manuscript_id = ? AND manuscript_status = 'UnderReview'");
            rstateQuery.setInt(1, manID);
            ResultSet rstateRes = rstateQuery.executeQuery();
            if (!rstateRes.next()) {
              System.out.println("Manuscript in invalid state");
              break;
            }


            PreparedStatement rstatusQuery = con.prepareStatement(
              "SELECT * FROM Manuscript NATURAL JOIN Review "
              + "WHERE manuscript_id = ? AND review_recommendation IS NULL");
            rstatusQuery.setInt(1, manID);
            ResultSet rstatusRes = rstatusQuery.executeQuery();
            if (rstatusRes.next()) {
              System.out.println("Error finding valid manuscript in database. This manuscript may not be in a valid state, or may not have enough completed reviews.");
              break;
            }

            PreparedStatement reviewCheck = con.prepareStatement(
              "SELECT COUNT(*) as reviews FROM Review WHERE manuscript_id = ?");
            reviewCheck.setInt(1, manID);
            ResultSet reviewRes = reviewCheck.executeQuery();
            if (!reviewRes.next()) {
              System.out.println("Error finding manuscript reviews");
              break;
            }
            else if (reviewRes.getInt(1) < 3) {
              System.out.println("Error: this manuscript does not have enough assigned reviews.");
              break;
            }

            PreparedStatement rejectQuery = con.prepareStatement(
              "UPDATE Manuscript SET manuscript_status = \"Rejected\", manuscript_update_date = NOW() WHERE manuscript_id = ?");
            rejectQuery.setInt(1, manID);
            rejectQuery.executeUpdate();
            break;

          case "accept":
            manID = s.nextInt();
            // Check the manuscript is in the underReview status and has 3 completed reviews
            PreparedStatement astateQuery = con.prepareStatement(
              "SELECT * FROM Manuscript WHERE manuscript_id = ? AND manuscript_status = 'UnderReview'");
            astateQuery.setInt(1, manID);
            ResultSet astateRes = astateQuery.executeQuery();
            if (!astateRes.next()) {
              System.out.println("Manuscript in invalid state");
              break;
            }


            PreparedStatement astatusQuery = con.prepareStatement(
              "SELECT * FROM Manuscript NATURAL JOIN Review "
              + "WHERE manuscript_id = ? AND review_recommendation IS NULL");
            astatusQuery.setInt(1, manID);
            ResultSet astatusRes = astatusQuery.executeQuery();
            if (astatusRes.next()) {
              System.out.println("Error finding valid manuscript in database. This manuscript may not be in a valid state, or may not have enough completed reviews.");
              break;
            }

            PreparedStatement areviewCheck = con.prepareStatement(
              "SELECT COUNT(*) as reviews FROM Review WHERE manuscript_id = ?");
            areviewCheck.setInt(1, manID);
            ResultSet areviewRes = areviewCheck.executeQuery();
            if (!areviewRes.next()) {
              System.out.println("Error finding manuscript reviews");
              break;
            }
            else if (areviewRes.getInt(1) < 3) {
              System.out.println("Error: this manuscript does not have enough assigned reviews.");
              break;
            }

            PreparedStatement acceptQuery = con.prepareStatement(
              "UPDATE Manuscript SET manuscript_status = \"Accepted\", manuscript_update_date = NOW() WHERE manuscript_id = ?");
            acceptQuery.setInt(1, manID);
            acceptQuery.executeUpdate();
            break;

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

                // Update article to Typescript state
                PreparedStatement updateMan = con.prepareStatement(
                  "UPDATE Manuscript SET manuscript_status = \"Typeset\", "
                  + "manuscript_update_date = NOW() WHERE manuscript_id = ?");
                updateMan.setInt(1, manID);
                updateMan.executeUpdate();
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
                if (artIssRes.getObject(1) != null) {
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
                    + ", issue_id = ? WHERE manuscript_id = ? ");

                  scheduleArticle.setInt(1, order);
                  scheduleArticle.setInt(2, start);
                  scheduleArticle.setInt(3, issueID);
                  scheduleArticle.setInt(4, manID);
                  scheduleArticle.executeUpdate();

                  // Change manuscript status to "Scheduled"
                  PreparedStatement scheduleManuscript = con.prepareStatement(
                    "UPDATE Manuscript set manuscript_status = 'Scheduled' " +
                    "WHERE manuscript_id = ?");
                  scheduleManuscript.setInt(1, manID);
                  scheduleManuscript.executeUpdate();
                  System.out.println("Article added and " +
                    "manuscript status set to scheduled");
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
            if (issuePeriod > 4 || issuePeriod < 1) {
              System.out.println("Please input a valid period: 1, 2, 3, or 4.");
              break;
            }

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
                System.out.println("Issue published");

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
        System.out.println("SQL ERROR: Input invalid. Try again.");
      }
      catch (Exception e) {
        System.out.println("ERROR: Input invalid. Try again.");
      }
      finally {
        try {
          stmt.close();
        }
        catch (Exception e) { /* do nothing */ }
      }
    }
  }

  /*
  handleReviewer() - register, login or resign reviewer
  */
  public static void handleReviewer(Connection con) {
    // Check whether they would like to login, register, or resign
    System.out.println("You are in REVIEWER Mode.\nPlease register, login, or resign");
    System.out.println("Type 'help' for list of commands.");
    Scanner s = new Scanner(System.in);
    boolean finished = false;
    while (!finished) {
      try{
        System.out.print("Command: ");
        String action = s.next();
        int id;
        switch (action) {
          case "help":
            reviewerHelp();
            break;

          case "login":
            id = s.nextInt();
            ResultSet res = validId(con, 'r', id);
            if (!res.next()) {
              System.out.println("ERROR: Invalid ID");
            } else {
              // Send welcome message
              String lname = res.getObject(2).toString();
              String fname = res.getObject(3).toString();
              System.out.println("Welcome " + fname + " " + lname + "! ");

              // Print out status of manuscripts
              System.out.println("Here are your current reviews: ");
              reviewerStatus(con, id);

              handleReviewerLoggedIn(con, id);
              finished = true;
            }
            break;

          case "resign":
            id = s.nextInt();
            PreparedStatement deleteUser = con.prepareStatement(
              "DELETE FROM Reviewer WHERE reviewer_id = ?");
            deleteUser.setInt(1, id);
            deleteUser.executeUpdate();
            System.out.println("User successfully deleted. Have a nice day not reviewing papers!");
            break;

          case "register":
            PreparedStatement registerQuery = con.prepareStatement(
              "INSERT INTO Reviewer (reviewer_lname, " +
              "reviewer_fname, reviewer_affiliation, reviewer_email) " +
              "VALUES (?, ?, ?, ?)");
            String fName = s.next();
            String lName = s.next();
            System.out.println("Welcome, " + fName + " " + lName + "!");
            registerQuery.setString(1, lName);
            registerQuery.setString(2, fName);
            s.nextLine();
            System.out.print("Please enter an affiliation: ");
            registerQuery.setString(3, s.nextLine());
            System.out.print("Please enter an email address: ");
            registerQuery.setString(4, s.nextLine());

            registerQuery.executeUpdate();

            Statement getReviewerID = con.createStatement();
            ResultSet reviewerID =
              getReviewerID.executeQuery("SELECT LAST_INSERT_ID()");
            if (reviewerID.next()) {
              int actualID = reviewerID.getInt(1);
              System.out.println("Your reviewer ID is " + actualID);
              System.out.println("Here are the possible areas of interest: ");
              Statement possibleAois = con.createStatement();
              printQuery(possibleAois.executeQuery("SELECT * from Aoi"));
              System.out.println(
                "You must have at least 1 AOI but no more than 3");

              int reviewerAois = 0;
              // handles inputting AOIs for reviewer
              while (reviewerAois < 3) {
                System.out.print("Enter the RICode of an AOI (or 'f' to finish): ");
                String str = s.next();
                if (str.equals("f")) {
                  if (reviewerAois == 0) {
                    System.out.println("Must have at least one AOI.");
                    continue;
                  }
                  break;
                }
                int aoi = 0;
                try {
                  aoi = Integer.parseInt(str);
                }
                catch (NumberFormatException num) {
                  System.out.println("Invalid option. Try again.");
                  continue;
                }
                if (!validRICode(con, aoi)) {
                  System.out.println("Must have a valid RI code. Try again.");
                  continue;
                }
                String query =
                  "INSERT INTO Reviewer_aoi (aoi_ri_code, reviewer_id) " +
                  "VALUES (?, ?)";
                PreparedStatement aoiStmt = con.prepareStatement(query);
                aoiStmt.setInt(1, aoi);
                aoiStmt.setInt(2, actualID);
                aoiStmt.executeUpdate();
                reviewerAois++;
              }
              System.out.println("You're all set!");

              handleReviewerLoggedIn(con, reviewerID.getInt(1));
              finished = true;
            }
            else {
              System.out.println("Error. Please try again.");
            }
            getReviewerID.close();
            registerQuery.close();
            reviewerID.close();

            break;

          case "quit":
            finished = true;
            System.out.println("Quitting...");
            break;

          default:
            System.out.println("Invalid command - try again.");
            reviewerHelp();
            break;
        }
      }
      catch (SQLException e) {
        System.out.println("SQL ERROR: Input invalid. Try again.");
      }
      catch (Exception e) {
        e.printStackTrace();
        System.out.println("ERROR: Input invalid. Try again.");
      }
    }
  }

  /*
  reviewerStatus() - SQL query that outputs status of all manuscripts
  being reviewed by this user
  */
  public static void reviewerStatus(Connection con, int id) {
    try {
      // order by status, then man id
      Statement stmt = con.createStatement();
      String query =
        "SELECT Manuscript.manuscript_id, manuscript_title, manuscript_update_date, " +
        "manuscript_status, author_id FROM Manuscript NATURAL JOIN Review " +
        "WHERE reviewer_id = " + id + " " +
        "ORDER BY CASE manuscript_status " +
          "WHEN 'Submitted' THEN 1 WHEN 'UnderReview' THEN 2 " +
          "WHEN 'Rejected' THEN 3 WHEN 'Accepted' THEN 4 " +
          "WHEN 'Typeset' THEN 5 WHEN 'Scheduled' THEN 6 " +
          "WHEN 'Published' THEN 7 END, Manuscript.manuscript_id";
      ResultSet res = stmt.executeQuery(query);
      printQuery(res);
      stmt.close();
      res.close();
    }
    catch (SQLException e) {
      System.out.println("SQL ERROR: Database error. Try again.");
    }
  }

  /*
  handleReviewerLoggedIn() - handles all commands for logged in reviewer
  */
  public static void handleReviewerLoggedIn(Connection con, int id) {
    System.out.println("Type 'help' for list of commands.");
    Scanner s = new Scanner(System.in);
    boolean finished = false;
    while(!finished) {
      try {
        System.out.print("Command: ");
        String action = s.next();
        switch (action) {
          case "help":
            reviewerLoggedInHelp();
            break;

          case "status":
            reviewerStatus(con, id);
            break;

          case "review":
            int manID = s.nextInt();
            // Check that this reviewer is reviewing this manuscript
            PreparedStatement checkMan = con.prepareStatement(
              "SELECT * FROM Review WHERE manuscript_id = ? AND reviewer_id = ?");
            checkMan.setInt(1, manID);
            checkMan.setInt(2, id);
            ResultSet res = checkMan.executeQuery();
            if (!res.next()) { // reviewer not assigned to this review
              System.out.println("You don't have access to this manuscript!");
              continue;
            }

            // Check that manuscript has not already been reviewed
            PreparedStatement checkStatus = con.prepareStatement(
              "SELECT * FROM Manuscript NATURAL JOIN Review " +
              "WHERE manuscript_id = ? AND manuscript_status = 'UnderReview'");
            checkStatus.setInt(1, manID);
            res = checkStatus.executeQuery();
            if (!res.next()) {
              System.out.println("This manuscript is not under review!");
              continue;
            }

            // ask if they want to reject or accept
            System.out.print("Please type 'a' if you would like to accept the manuscript (any other input taken as reject): ");
            String response = s.next().equals("a") ? "Accept" : "Reject";

            // ask for ratings 1-10 for four categories
            System.out.print("Please input a score (1-10) for appropriateness: ");
            int approp = s.nextInt();
            if (approp > 10 || approp < 1) {
              System.out.println("Invalid input!");
              continue;
            }
            s.nextLine();
            System.out.print("Please input a score (1-10) for clarity: ");
            int clarity = s.nextInt();
            if (clarity > 10 || clarity < 1) {
              System.out.println("Invalid input!");
              continue;
            }
            s.nextLine();
            System.out.print("Please input a score (1-10) for methodology: ");
            int method = s.nextInt();
            if (method > 10 || method < 1) {
              System.out.println("Invalid input!");
              continue;
            }
            s.nextLine();
            System.out.print("Please input a score (1-10) for contribution: ");
            int contrib = s.nextInt();
            if (contrib > 10 || contrib < 1) {
              System.out.println("Invalid input!");
              continue;
            }
            s.nextLine();

            // Update Review
            PreparedStatement updateReview = con.prepareStatement(
              "UPDATE Review SET review_date_returned = NOW(), "
              + "review_recommendation = ?, review_appropriateness = ?, "
              + "review_clarity = ?, review_methodology = ?, review_contribution"
              + " = ? WHERE reviewer_id = ? AND manuscript_id = ?");
              updateReview.setString(1, response);
              updateReview.setInt(2, approp);
              updateReview.setInt(3, clarity);
              updateReview.setInt(4, method);
              updateReview.setInt(5, contrib);
              updateReview.setInt(6, id);
              updateReview.setInt(7, manID);
              updateReview.executeUpdate();
              System.out.println("Review accepted! Thank you for your hard work");
            break;

          case "logout":
            finished = true;
            System.out.println("Quitting...");
            break;

          default:
            System.out.println("Invalid command - try again.");
            reviewerLoggedInHelp();
            break;
        }
      }
      catch (SQLException e) {
        System.out.println("SQL ERROR: Input invalid. Try again.");
      }
      catch (Exception e) {
        System.out.println("ERROR: Input invalid. Try again.");
      }
    }
  }

  /*
    validId() - SQL query to check if the given user exists in the db
    Returns true if user exists, otherwise returns false
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
      System.out.println("ERROR: Database error. Try again.");
      return null;
    }
  }

  public static void initialHelp() {
    System.out.println("Type 'a', 'e', or 'r' to indicate your job (author, editor, reviewer)");
    System.out.println("Type 'd' to exit the application");
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
    System.out.format("%-40s","See commands:");
    System.out.println("help");
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
    System.out.format("%-40s","See commands:");
    System.out.println("help");
  }

  /* Help output for user listing author commands */
  public static void authorHelp() {
    System.out.println("-----------------------Author Commands-----------------------");
    System.out.println("Note: some commands will prompt user for further input after the initial command");
    System.out.format("%-40s", "Register as a new user: ");
    System.out.println("register <fname> <lname>");
    System.out.format("%-40s","Login as a returning user: ");
    System.out.println("login <id>");
    System.out.format("%-40s","See commands:");
    System.out.println("help");
    System.out.format("%-40s","Return to main menu: ");
    System.out.println("quit");
  }
  public static void authorLoggedInHelp() {
    System.out.println("---------------Authorized Author Commands---------------");
    System.out.println("Note: some commands will prompt user for further input after the initial command");
    System.out.format("%-40s","Submit a manuscript: ");
    System.out.println("submit <title> <author_affiliation> <ri_code>");
    System.out.format("%-40s","See status of submitted manuscripts: ");
    System.out.println("status");
    System.out.format("%-40s","Retract a manuscript: ");
    System.out.println("retract <manuscript_id>");
    System.out.format("%-40s","See commands:");
    System.out.println("help");
    System.out.format("%-40s","Return to main menu: ");
    System.out.println("logout");
  }

  /* Help output for user listing reviewer commands */
  public static void reviewerHelp() {
    System.out.println("-----------------------Reviewer Commands-----------------------");
    System.out.println("Note: some commands will prompt user for further input after the initial command");
    System.out.format("%-40s", "Register as a new user: ");
    System.out.println("register <fname> <lname>");
    System.out.format("%-40s","Login as a returning user:");
    System.out.println("login <id>");
    System.out.format("%-40s","Resign as user: ");
    System.out.println("resign <id>");
    System.out.format("%-40s","Return to main menu:");
    System.out.println("quit");
    System.out.format("%-40s","See commands:");
    System.out.println("help");
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
    System.out.format("%-40s","See commands:");
    System.out.println("help");
  }

  /*
  validRICode() - SQL query to check if entered RI Code is valid
  */
  public static boolean validRICode(Connection con, int riCode) {
    try {
      String query = "SELECT * FROM Aoi WHERE aoi_ri_code = " + riCode;
      Statement stmt = con.createStatement();
      if (!stmt.executeQuery(query).next()) {
        stmt.close();
        return false;
      }
      stmt.close();
      return true;
    }
    catch (SQLException e) {
      System.out.println("ERROR: Invalid input. Try again.");
      return false;
    }
  }

  /*
    printQuery() - prints the result set of a query in a table format
  */
  public static void printQuery(ResultSet res) {
    try {
      int numColumns = res.getMetaData().getColumnCount();
      for(int i = 1; i <= numColumns; i++) {
        System.out.format("%-25s", res.getMetaData().getColumnName(i));
      }
      int numTicks = 25 * numColumns;
      System.out.println("");
      for (int i = 0; i < numTicks; i++) {
        System.out.print("-");
      }
      System.out.println("");
      res.beforeFirst();
      while (res.next()) {
        for (int i = 1; i <= numColumns; i++) {
          System.out.format("%-25s", res.getObject(i));
        }
        System.out.println("");
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
