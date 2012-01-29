package phyutility.drb;
/*
     Derby - WwdUtils.java - utilitity methods used by WwdEmbedded.java

        Licensed to the Apache Software Foundation (ASF) under one
           or more contributor license agreements.  See the NOTICE file
           distributed with this work for additional information
           regarding copyright ownership.  The ASF licenses this file
           to you under the Apache License, Version 2.0 (the
           "License"); you may not use this file except in compliance
           with the License.  You may obtain a copy of the License at

             http://www.apache.org/licenses/LICENSE-2.0

           Unless required by applicable law or agreed to in writing,
           software distributed under the License is distributed on an
           "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
           KIND, either express or implied.  See the License for the
           specific language governing permissions and limitations
           under the License.    

 */

import java.io.*;
import java.sql.*;
public class WwdUtils {


	/***      Check for  WISH_LIST table    ****/
	public static boolean wwdChk4Table (Connection conTst ) throws SQLException {
		boolean chk = true;
		boolean doCreate = false;
		try {
			Statement s = conTst.createStatement();
			s.execute("SELECT * FROM TREES");
		}  catch (SQLException sqle) {
			String theError = (sqle).getSQLState();
			//   System.out.println("  Utils GOT:  " + theError);
			/** If table exists will get -  WARNING 02000: No row was found **/
			if (theError.equals("42X05"))   // Table does not exist
			{  return false;
			}  else if (theError.equals("42X14") || theError.equals("42821"))  {
				System.out.println("WwdChk4Table: Incorrect table definition. Drop table WISH_LIST and rerun this program");
				throw sqle;   
			} else { 
				System.out.println("WwdChk4Table: Unhandled SQLException" );
				throw sqle; 
			}
		}
		//  System.out.println("Just got the warning - table exists OK ");
		return true;
	}  /*** END wwdInitTable  **/

}