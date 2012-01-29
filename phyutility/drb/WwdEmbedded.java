package phyutility.drb;

import java.sql.*;

public class WwdEmbedded {
	//   ## DEFINE VARIABLES SECTION ##
	// define the driver to use 
	String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	// the database name  
	String dbName = "derbydatabase";
	// define the Derby connection URL to use 
	String connectionURL = "jdbc:derby:" + dbName + ";create=true";

	Connection conn = null;
	Statement s;
	PreparedStatement psInsert;
	ResultSet myWishes;
	String printLine = "  __________________________________________________";
	String createString = "CREATE TABLE TREES (TREE_ID INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY, TREE_STRING VARCHAR(30000) NOT NULL) ";

	String dropString = "DROP TABLE TREES";

	public WwdEmbedded(String name) {
		this.dbName = name;
		connectionURL = "jdbc:derby:" + dbName + ";create=true";
		//   Beginning of JDBC code sections   
		//   ## LOAD DRIVER SECTION ##
		try {
			/*
			 **  Load the Derby driver. 
			 **     When the embedded Driver is used this action start the Derby engine.
			 **  Catch an error and suggest a CLASSPATH problem
			 */
			Class.forName(driver);
			System.out.println(driver + " loaded. ");
		} catch (java.lang.ClassNotFoundException e) {
			System.err.print("ClassNotFoundException: ");
			System.err.println(e.getMessage());
			System.out
			.println("\n    >>> Please check your CLASSPATH variable   <<<\n");
		}
		//connectToDB();
		//System.out.println("table made = "+makeTable(true));
		//System.out.println(this.getTableTreeSize());
	}

	public int getTableTreeSize() {
		int ret = 0;
		try {
			//			Select all records in the WISH_LIST table
			myWishes = s.executeQuery("select TREE_ID,TREE_STRING from TREES");
			while (myWishes.next()) {
				ret++;
			}
		} catch (Throwable e) {
			/*       Catch all exceptions and pass them to 
			 **       the exception reporting method             */
			System.out.println(" . . . exception thrown:");
			errorPrint(e);
		}
		return ret;
	}

	public void connectToDB() {
		//		Beginning of Primary DB access section
		//   ## BOOT DATABASE SECTION ##
		try {
			// Create (if needed) and connect to the database
			conn = DriverManager.getConnection(connectionURL);
			System.out.println("Connected to database " + dbName);
		} catch (Throwable e) {
			/*       Catch all exceptions and pass them to 
			 **       the exception reporting method             */
			System.out.println(" . . . exception thrown:");
			errorPrint(e);
		}
	}

	public boolean makeTable(boolean force) {
		boolean make = false;
		try {
			//   ## INITIAL SQL SECTION ## 
			//   Create a statement to issue simple commands.  
			s = conn.createStatement();
			// Call utility method to check if table exists.
			//      Create the table if needed
			if (!WwdUtils.wwdChk4Table(conn) || force == true) {
				System.out.println(" . . . . creating table TREES");
				if (!WwdUtils.wwdChk4Table(conn)) {
					s.execute(createString);
				} else {
					s.execute(dropString);
					s.execute(createString);
				}
				make = true;
			}
			//  Prepare the insert statement to use 
			psInsert = conn
			.prepareStatement("insert into TREES(TREE_STRING) values (?)");
		} catch (Throwable e) {
			/*       Catch all exceptions and pass them to 
			 **       the exception reporting method             */
			System.out.println(" . . . exception thrown:");
			errorPrint(e);
		}
		return make;
	}

	public void addTree(String tree) {
		try {
			//Insert the text entered into the WISH_ITEM table
			psInsert.setString(1, tree);
			psInsert.executeUpdate();
		} catch (Throwable e) {
			/*       Catch all exceptions and pass them to 
			 **       the exception reporting method             */
			System.out.println(" . . . exception thrown:");
			errorPrint(e);
		}
	}

	public String getTree(int num) {
		String ret = "";
		try {
			//			Select all records in the WISH_LIST table
			myWishes = s
			.executeQuery("select TREE_ID,TREE_STRING from TREES where TREE_ID = "
					+ num);
			//  Loop through the ResultSet and print the data 
			while (myWishes.next()) {
				//System.out.println("TREE " + myWishes.getInt(1) + " = " + myWishes.getString(2));
				ret = myWishes.getString(2);
			}
			//  Close the resultSet 
			myWishes.close();
		} catch (Throwable e) {
			/*       Catch all exceptions and pass them to 
			 **       the exception reporting method             */
			System.out.println(" . . . exception thrown:");
			errorPrint(e);
		}
		return ret;
	}

	public jade.tree.Tree getJadeTree(int num) {
		String ret = "";
		num = num+1;
		try {
			//			Select all records in the WISH_LIST table
			myWishes = s
			.executeQuery("select TREE_ID,TREE_STRING from TREES where TREE_ID = "
					+ num);
			//  Loop through the ResultSet and print the data 
			while (myWishes.next()) {
				//System.out.println("TREE " + myWishes.getInt(1) + " = " + myWishes.getString(2));
				ret = myWishes.getString(2);
			}
			//  Close the resultSet 
			myWishes.close();
		} catch (Throwable e) {
			/*       Catch all exceptions and pass them to 
			 **       the exception reporting method             */
			System.out.println(" . . . exception thrown:");
			errorPrint(e);
		}
		jade.tree.Tree rettree = null;
		//System.out.println(num+" "+ret);
		jade.tree.TreeReader tr = new jade.tree.TreeReader();
		tr.setTree(ret);
		rettree = tr.readTree();
		return rettree;
	}

	public void closeDB() {
		try {
			// Release the resources (clean up )
			psInsert.close();
			s.close();
			conn.close();
			System.out.println("Closed connection");

			//   ## DATABASE SHUTDOWN SECTION ## 
			/*** In embedded mode, an application should shut down Derby.
			       Shutdown throws the XJ015 exception to confirm success. ***/
			if (driver.equals("org.apache.derby.jdbc.EmbeddedDriver")) {
				boolean gotSQLExc = false;
				try {
					DriverManager.getConnection("jdbc:derby:;shutdown=true");
				} catch (SQLException se) {
					if (se.getSQLState().equals("XJ015")) {
						gotSQLExc = true;
					}
				}
				if (!gotSQLExc) {
					System.out.println("Database did not shut down normally");
				} else {
					System.out.println("Database shut down normally");
				}
			}

			//  Beginning of the primary catch block: uses errorPrint method
		} catch (Throwable e) {
			/*       Catch all exceptions and pass them to 
			 **       the exception reporting method             */
			System.out.println(" . . . exception thrown:");
			errorPrint(e);
		}
		System.out.println("Working With Derby JDBC program ending.");
	}

	//   ## DERBY EXCEPTION REPORTING CLASSES  ## 
	/***     Exception reporting methods
	 **      with special handling of SQLExceptions
	 ***/
	static void errorPrint(Throwable e) {
		if (e instanceof SQLException)
			SQLExceptionPrint((SQLException) e);
		else {
			System.out.println("A non SQL error occured.");
			e.printStackTrace();
		}
	} // END errorPrint 

	//  Iterates through a stack of SQLExceptions 
	static void SQLExceptionPrint(SQLException sqle) {
		while (sqle != null) {
			System.out.println("\n---SQLException Caught---\n");
			System.out.println("SQLState:   " + (sqle).getSQLState());
			System.out.println("Severity: " + (sqle).getErrorCode());
			System.out.println("Message:  " + (sqle).getMessage());
			sqle.printStackTrace();
			sqle = sqle.getNextException();
		}
	} //  END SQLExceptionPrint   	
}
