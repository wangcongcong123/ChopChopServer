package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * the class are mainly some JDBCUtils
 * 
 * @author group 12
 * 
 */
public class JDBCUtils {
	// some main parameters related to mysql database
	private static final String DRIVERCLASS = "com.mysql.jdbc.Driver";
	private static final String URL = "jdbc:mysql://localhost:3306/chopchop?useUnicode=true&characterEncoding=utf8";
	private static final String USER = "root";
	private static final String PWD = "123";

	// to get the connection between mysql database and server by using JDBC
	// APIs
	public static Connection getConnection() throws Exception {
		loadDriver();
		Connection connection = DriverManager.getConnection(URL, USER, PWD);
		return connection;

	}

	// to load the connection driver before getting connection to mysql
	public static void loadDriver() throws ClassNotFoundException {
		Class.forName(DRIVERCLASS);
	}

	// a method to release three objects created when using JDBC APIs
	public static void release(ResultSet rs, Statement stat, Connection cnn) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			rs = null;
		}
		release(stat, cnn);
	}

	// a method to release two objects created when using JDBC APIs
	public static void release(Statement stat, Connection cnn) {
		if (stat != null) {
			try {
				stat.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			stat = null;
		}
		if (cnn != null) {
			try {
				cnn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			cnn = null;
		}
	}

}
