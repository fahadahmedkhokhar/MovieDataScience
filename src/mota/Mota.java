/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mota;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 *
 * @author Fahad Ahmed Khokhar
 */
public class Mota {
private final static String jdbcDatabase = "nomi";
	private final static String jdbcDriver = "com.mysql.jdbc.Driver";
	private final static String jdbcUrl = "jdbc:mysql://localhost:3306/mysql?zeroDateTimeBehavior=convertToNull";
	private final static String jdbcUser = "root";
	private final static String jdbcPassword = "";
    /**
     * @param args the command line arguments
     */
        private static Connection makeConnection() throws ClassNotFoundException, SQLException {
		Connection con;
		Class.forName(jdbcDriver);
		System.setProperty("jdbc.drivers", jdbcDriver);

		con = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
		con.setAutoCommit(false);

		Statement s1 = con.createStatement();
		//s1.executeUpdate("DROP DATABASE IF EXISTS " + jdbcDatabase + ";");
		s1.close();

		Statement s2 = con.createStatement();
		//s2.executeUpdate("CREATE DATABASE  " + jdbcDatabase + ";");
		s2.close();

		con.setCatalog(jdbcDatabase);
		return con;
	}
        
        private static void insertFromCsv(Connection con, String csvFname, boolean ignoreFirstLine, String tableName) throws SQLException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(csvFname));

		StringBuilder sb = new StringBuilder();

		String curLine;
		while ((curLine = br.readLine()) != null) {
			if (ignoreFirstLine) {
				ignoreFirstLine = false;
				continue;
			}

			String[] arr = (curLine + " ").split(",");
			for (int i = 0; i < arr.length; ++i) {
				arr[i] = arr[i].trim();
				if (arr[i].equals("")) {
					arr[i] = "NULL";
				}
			}
			sb.append("(");
			sb.append(String.join(",", arr));
			sb.append("),");
		}
                System.out.println(sb);
		sb.deleteCharAt(sb.length() - 1);
		br.close();

		update(con, "INSERT INTO " + tableName + " VALUES " + sb.toString() + ";");
	}
        
        private static int update(Connection con, String query) throws SQLException {
		try {
			Statement s = con.createStatement();
			s.executeUpdate(query);
			return s.getUpdateCount();
		}
		catch (SQLException e) {
			throw new SQLException("Bad query \"" + query + "\"\n" + e.getMessage());
		}
	}
        
        private static void createTables(Connection con) throws SQLException, IOException {
		update(con,
				"CREATE TABLE IF NOT EXISTS genres (" +
						"id varchar(20) NOT NULL, " +
						"movie_id varchar(20) NOT NULL, " +
						"genre_id varchar(20) NOT NULL, " +
						"genre_name VARCHAR(250) NOT NULL, " +
						"PRIMARY KEY (id));");
		//insertFromCsv(con, "./data/genres.csv", false, "genres");
                

		update(con,
				"CREATE TABLE IF NOT EXISTS keywords (" +
						"id varchar(20) NOT NULL, " +
						"movie_id varchar(20) NOT NULL, " +
						"keywords varchar(20) NOT NULL, " +
						"keywords_name varchar(250), " +
						"PRIMARY KEY (id));");
		//insertFromCsv(con, "teams.csv", false, "teams");

		update(con,
				"CREATE TABLE IF NOT EXISTS parsed (" +
						"id_parsed varchar(20) NOT NULL, " +
						"id varchar(20) NOT NULL, " +
						"original_title varchar(250) NOT NULL, " +
						"popularity varchar(250), " +
                                                "vote_average varchar(250)," + 
                                                "vote_count varchar(20), "+
						"PRIMARY KEY (id_parsed));");
		//insertFromCsv(con, "members.csv", false, "members");
	}
private static ResultSet query(Connection con, String query) throws SQLException {
		try {
			Statement s = con.createStatement();
			return s.executeQuery(query);
		}
		catch (SQLException e) {
			throw new SQLException("Bad query \"" + query + "\"\n" + e.getMessage());
		}
	}
        private static void query3(Connection con, String movie) throws SQLException {
		ResultSet rs = query(con,
				"select * from parsed where original_title like '"+movie+"' "
                                        + "OR vote_average = (select vote_average from parsed where original_title like '"+movie+"')"
		);
		System.out.println(String.format( "Movie Name"));
		System.out.println(String.format( "---------"));
		while (rs.next()) {
			System.out.println(String.format( rs.getString("original_title")));
		}
		rs.close();
	}

    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        // TODO code application logic here
        Scanner sc = new Scanner(System.in);
        Connection con = makeConnection();
        createTables(con);

        System.out.println("Enter the movie name :");
        String movie = sc.nextLine();
     	query3(con, movie);
        
        System.out.println("Enter the movie you most liked :");
        String movielike = sc.nextLine();
     	query3(con, movielike);

        
    }
    
}
