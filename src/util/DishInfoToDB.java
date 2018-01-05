package util;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Properties;

import org.junit.Test;

/**
 * this class is used to insert 40 dishes into mysql database(chopchop) by
 * running the update test unity method
 * 
 * @author Group12
 * 
 */

public class DishInfoToDB {
	@Test
	public void update() {
		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			conn = JDBCUtils.getConnection();
			String sql = "update dishlist set dish_face=? where dish_code=?";
			stmt = conn.prepareStatement(sql);
			for (int j = 1; j < 5; j++) {
				for (int i = 1 + 100 * j; i < 11 + 100 * j; i++) {
					if (isWinOS()) {
						File file = new File(".\\image\\dishPictures\\" + i + ".jpg");
						stmt.setBinaryStream(8, new FileInputStream(".\\image\\dishPictures\\" + i + ".jpg"), (int) file.length());
					} else {
						File file = new File("./image/dishPictures/" + i + ".jpg");
						stmt.setBinaryStream(1, new FileInputStream("./image/dishPictures/" + i + ".jpg"), (int) file.length());
					}
					stmt.setInt(2, i);
					stmt.executeUpdate();
				}
			}
			System.out.println("done");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.release(stmt, conn);
		}
	}

	// true for win, false for mac,
	private boolean isWinOS() {
		Properties prop = System.getProperties();
		String os = prop.getProperty("os.name");
		System.out.println(os);
		if (os.startsWith("Mac") || os.startsWith("mac")) {
			// for mac os
			return false;
		} else if (os.startsWith("Win") || os.startsWith("win")) {
			// for windows os
			return true;
		}
		return true;
	}

}
