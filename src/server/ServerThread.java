package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import util.JDBCUtils;

/**
 * a class to deal with the events required by users
 * 
 * @author group 12
 * 
 */
public class ServerThread extends Thread {
	private Socket s = null;
	private BufferedReader br = null;
	private PrintStream ps = null;
	private boolean check = true;
	private Properties prop = null;
	private String os = null;
	private MyServer myServer;
	private String account = null;

	public ServerThread(Socket s, MyServer myServer) throws Exception {
		prop = System.getProperties();
		os = prop.getProperty("os.name");
		this.s = s;
		this.myServer = myServer;
		// get the input and output of the client based on TCP protocol
		br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		ps = new PrintStream(s.getOutputStream());
	}

	public void run() {

		try {
			while (check) {
				// read in orders sent from the client
				String str = br.readLine();
				if (str != null) {
					// to take different actions by different orders
					if (str.equals("login")) {
						login();
					} else if (str.equals("register")) {
						register();
					} else if (str.equals("UploadFace")) {
						uploadFace();
					} else if (str.equals("UpdateFace")) {
						updateFace();
					} else if (str.equals("DisplayFaceAndName")) {
						displayFaceAndName();
					} else if (str.equals("GoMapFrame")) {
						ps.println("MapFrame");
					} else if (str.equals("GoCSFrame")) {
						ps.println("CurriculumScheduleFrame");
					} else if (str.equals("GoCanteenFrame")) {
						ps.println("Canteen_tiantianFrame");
					} else if (str.equals("GoRegisterFrame")) {
						ps.println("RegisterFrame");
					} else if (str.equals("CSGoMainFrame")) {
						ps.println("CurriculumScheduleBack");
					} else if (str.equals("CanteenGoMainFrame")) {
						ps.println("CanteenFrameBack");
					} else if (str.equals("SavaCurriculumSchedule")) {
						savaCurriculumSchedule();
					} else if (str.equals("UpdateCurriculumSchedule")) {
						updateCurriculumSchedule();
					} else if (str.equals("displayCurriculumSchedule")) {
						displayCS();
					} else if (str.equals("sendMessage")) {
						sendMessage();
					} else if (str.equals("displayChatRecording")) {
						displayCR();
					} else if (str.equals("sendComment")) {
						sendComment();
					} else if (str.equals("displayDishComments")) {
						displayDC();
					} else if (str.equals("ClickDishLikeButton")) {
						updateDishLikeNumber();
					} else if (str.equals("DishInfoGoCanteenFrame")) {
						ps.println("DishInfoFrameBack");
					} else if (str.equals("displayDishList")) {
						displayDL();
					} else if (str.equals("GoDishInfoFrame")) {
						ps.println("GoDishInfoFrameDone");
					} else if (str.equals("initializeDishInfo")) {
						initializeDishInfo();
					} else if (str.equals("searchDish")) {
						searchDish();
					} else if (str.equals("MapGoMainFrame")) {
						ps.println("MapGoMainFrameDone");
					} else if (str.equals("ModifyUserInfo")) {
						ModifyUserInfo();
					}
				} else {
					check = false;
					myServer.getClients().remove(this);
					myServer.getJTA().append(account + " logout........\n---------------------\n");
					myServer.setTitle("Current online clients:" + myServer.getClients().size());
					br.close();
					ps.close();
				}
			}
		} catch (IOException e) {
			check = false;
			myServer.getClients().remove(this);
			myServer.getJTA().append(account + " logout........\n---------------------\n");
			myServer.setTitle("Current online clients:" + myServer.getClients().size());
			try {
				br.close();
				ps.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void ModifyUserInfo() {
		String account = null;
		String password = null;
		String name = null;
		Connection cnnConnection = null;
		PreparedStatement statement = null;
		try {
			account = br.readLine();
			password = br.readLine();
			name = br.readLine();
			cnnConnection = JDBCUtils.getConnection();
			String sql = "update users set password=?, name=? where id=?";
			statement = cnnConnection.prepareStatement(sql);
			statement.setString(1, password);
			statement.setString(2, name);
			statement.setString(3, account);
			statement.executeUpdate();
			ps.println("ModifyUserInfoDone");
		} catch (IOException e) {
			ps.println("error");
			e.printStackTrace();
		} catch (Exception e) {
			ps.println("error");
			e.printStackTrace();
		} finally {
			JDBCUtils.release(statement, cnnConnection);
		}
	}

	private void searchDish() {
		String dishName = null;
		double price1;
		double price2;
		String canteenName = null;
		String sorting;
		Connection cnnConnection = null;
		PreparedStatement statement = null;
		ResultSet rsResultSet = null;
		InputStream in = null;
		String outfilename = null;
		OutputStream out = null;
		try {
			dishName = br.readLine();
			price1 = Double.parseDouble(br.readLine());
			price2 = Double.parseDouble(br.readLine());
			canteenName = br.readLine();
			sorting = br.readLine();
			cnnConnection = JDBCUtils.getConnection();
			String sql = null;
			if (sorting.equals("SortedByLike")) {
				sql = "SELECT * FROM dishList WHERE dish_name like ? AND price>=? AND price<=? AND canteen=? ORDER BY like_number DESC";
			} else {
				sql = "SELECT * FROM dishList WHERE dish_name like ? AND price>=? AND price<=? AND canteen=? ORDER BY price ASC";
			}
			statement = cnnConnection.prepareStatement(sql);
			statement.setString(1, "%" + dishName + "%");
			statement.setDouble(2, price1);
			statement.setDouble(3, price2);
			statement.setString(4, canteenName);
			rsResultSet = statement.executeQuery();
			ps.println("searchDLDone");
			ps.println("start");
			int flag = 1;
			while (rsResultSet.next()) {
				flag = 0;
				int dishcode = rsResultSet.getInt("dish_code");
				ps.println(dishcode);
				ps.println(rsResultSet.getString("dish_name"));
				ps.println(rsResultSet.getInt("like_Number"));
				ps.println(rsResultSet.getDouble("price"));
				in = new BufferedInputStream(rsResultSet.getBinaryStream("dish_face"));

				if (os.startsWith("Mac") || os.startsWith("mac")) {
					// for mac os
					outfilename = "/Users/wangcongcong/Desktop/ChopChopClient/image/dishPictures/" + dishcode + ".jpg";
				} else if (os.startsWith("Win") || os.startsWith("win")) {
					// for windows os
					outfilename = ".\\image\\dishPictures\\" + dishcode + ".jpg";
				}
				out = new BufferedOutputStream(new FileOutputStream(outfilename));
				int b;
				while ((b = in.read()) != -1) {
					out.write(b);
				}
				in.close();
				out.close();
				ps.println(outfilename);
			}
			ps.println(flag);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.release(rsResultSet, statement, cnnConnection);
		}

	}

	// to get dish Information by dish code
	private void initializeDishInfo() {
		Connection cnnConnection = null;
		PreparedStatement statement = null;
		ResultSet rsResultSet = null;
		int dishcode = 0;
		InputStream in = null;
		String outfilename = null;
		OutputStream out = null;
		try {
			dishcode = Integer.parseInt(br.readLine());
			cnnConnection = JDBCUtils.getConnection();
			String sql = "SELECT * FROM dishList WHERE dish_code=?";
			statement = cnnConnection.prepareStatement(sql);
			statement.setInt(1, dishcode);
			rsResultSet = statement.executeQuery();
			ps.println("displayDishInfoDone");
			if (rsResultSet.next()) {
				ps.println(rsResultSet.getString("canteen"));
				ps.println(rsResultSet.getString("dish_name"));
				ps.println(rsResultSet.getInt("like_Number"));
				ps.println(rsResultSet.getDouble("price"));
				ps.println(rsResultSet.getString("introduction"));
				in = new BufferedInputStream(rsResultSet.getBinaryStream("dish_face"));

				if (os.startsWith("Mac") || os.startsWith("mac")) {
					// for mac os
					outfilename = "/Users/wangcongcong/Desktop/ChopChopClient/image/dishPictures/" + dishcode + ".jpg";
				} else if (os.startsWith("Win") || os.startsWith("win")) {
					// for windows os
					outfilename = ".\\image\\dishPictures\\" + dishcode + ".jpg";
				}
				out = new BufferedOutputStream(new FileOutputStream(outfilename));
				int b;
				while ((b = in.read()) != -1) {
					out.write(b);
				}
				in.close();
				out.close();
			}
			ps.println(outfilename);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.release(rsResultSet, statement, cnnConnection);
		}

	}

	// TO SHOE THE DISHLIST OF CANTEEN
	private void displayDL() {
		Connection cnnConnection = null;
		PreparedStatement statement = null;
		ResultSet rsResultSet = null;
		String canteenNameString = null;
		InputStream in = null;
		String outfilename = null;
		OutputStream out = null;
		try {
			canteenNameString = br.readLine();
			if (canteenNameString.equals("tiantian")) {
				canteenNameString = "天天餐厅";
			} else if (canteenNameString.equals("meishiyuan")) {
				canteenNameString = "美食园";
			} else if (canteenNameString.equals("aoyun")) {
				canteenNameString = "奥运餐厅";
			} else if (canteenNameString.equals("sansi")) {
				canteenNameString = "三四餐厅";
			}
			cnnConnection = JDBCUtils.getConnection();
			String sql = "SELECT * FROM dishList WHERE canteen = ?";
			statement = cnnConnection.prepareStatement(sql);
			statement.setString(1, canteenNameString);
			rsResultSet = statement.executeQuery();
			ps.println("displayDLDone");
			ps.println("start");
			while (rsResultSet.next()) {
				int dishcode = rsResultSet.getInt("dish_code");
				ps.println(dishcode);
				ps.println(rsResultSet.getString("dish_name"));
				ps.println(rsResultSet.getInt("like_Number"));
				ps.println(rsResultSet.getDouble("price"));
				in = new BufferedInputStream(rsResultSet.getBinaryStream("dish_face"));
				if (os.startsWith("Mac") || os.startsWith("mac")) {
					// for mac os
					outfilename = "/Users/wangcongcong/Desktop/ChopChopClient/image/dishPictures/" + dishcode + ".jpg";
				} else if (os.startsWith("Win") || os.startsWith("win")) {
					// for windows os
					outfilename = ".\\image\\dishPictures\\" + dishcode + ".jpg";
				}
				out = new BufferedOutputStream(new FileOutputStream(outfilename));
				int b;
				while ((b = in.read()) != -1) {
					out.write(b);
				}
				in.close();
				out.close();
				ps.println(outfilename);
			}
			ps.println(0);
			// ps.println(canteenNameString);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.release(rsResultSet, statement, cnnConnection);
		}
	}

	private void updateDishLikeNumber() {
		int dishcode = 0;
		String likeNumber = null;
		Connection cnnConnection = null;
		PreparedStatement statement = null;
		try {
			dishcode = Integer.parseInt(br.readLine());
			likeNumber = br.readLine();
			cnnConnection = JDBCUtils.getConnection();
			String sql = "update dishlist set like_Number=? where dish_code=?";
			statement = cnnConnection.prepareStatement(sql);
			statement.setInt(1, Integer.parseInt(likeNumber) + 1);
			statement.setInt(2, dishcode);
			statement.executeUpdate();
			ps.println("ClickDishLikeButtonDone");
			ps.println(Integer.parseInt(likeNumber) + 1);
		} catch (IOException e) {
			ps.println("error");
			e.printStackTrace();
		} catch (Exception e) {
			ps.println("error");
			e.printStackTrace();
		} finally {
			JDBCUtils.release(statement, cnnConnection);
		}

	}

	private void displayDC() {

		Connection cnnConnection = null;
		PreparedStatement statement = null;
		ResultSet rsResultSet = null;
		int dishcode = 0;
		try {
			dishcode = Integer.parseInt(br.readLine());
			cnnConnection = JDBCUtils.getConnection();
			String sql = "SELECT comment FROM comments WHERE dish_code=?";
			statement = cnnConnection.prepareStatement(sql);
			statement.setInt(1, dishcode);
			rsResultSet = statement.executeQuery();
			ps.println("displayDCDone");
			ps.println("start");
			while (rsResultSet.next()) {
				String commentString = rsResultSet.getString("comment");
				ps.println(commentString);
			}
			ps.println("over");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.release(rsResultSet, statement, cnnConnection);
		}

	}

	private void sendComment() {
		String account = null;
		String comment = null;
		int dishcode = 0;// waiting to modify
		Connection cnnConnection = null;
		PreparedStatement statement = null;
		try {
			dishcode = Integer.parseInt(br.readLine());
			account = br.readLine();
			comment = br.readLine();
			cnnConnection = JDBCUtils.getConnection();
			String sql = "INSERT into comments VALUES(null,?,?,?)";
			statement = cnnConnection.prepareStatement(sql);
			statement.setString(1, account);
			statement.setInt(2, dishcode);
			statement.setString(3, comment);
			statement.executeUpdate();
			ps.println("sendCommentDone");
			ps.println(comment);
		} catch (IOException e) {
			ps.println("error");
			e.printStackTrace();
		} catch (Exception e) {
			ps.println("error");
			e.printStackTrace();
		} finally {
			JDBCUtils.release(statement, cnnConnection);
		}

	}

	// a method to display the chat recording in canteen frame
	private void displayCR() {

		Connection cnnConnection = null;
		PreparedStatement statement = null;
		ResultSet rsResultSet = null;

		try {
			cnnConnection = JDBCUtils.getConnection();
			String sql = "SELECT message FROM chatRecording";
			statement = cnnConnection.prepareStatement(sql);
			rsResultSet = statement.executeQuery();
			ps.println("displayCRDone");
			ps.println("start");
			while (rsResultSet.next()) {
				String messageString = rsResultSet.getString("message");
				ps.println(messageString);
			}
			ps.println("over");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.release(rsResultSet, statement, cnnConnection);
		}

	}

	// a method to store the users' messages in database and send messages to
	// all clients
	// online
	private void sendMessage() {
		String account = null;
		String message = null;
		Connection cnnConnection = null;
		PreparedStatement statement = null;
		try {
			account = br.readLine();
			message = br.readLine();
			cnnConnection = JDBCUtils.getConnection();
			String sql = "INSERT into chatRecording VALUES(null,?,?)";
			statement = cnnConnection.prepareStatement(sql);
			statement.setString(1, account);
			statement.setString(2, message);
			statement.executeUpdate();
			// the following for loop is to send the message to all online users
			for (int i = 0; i < myServer.getClients().size(); i++) {
				ServerThread st = (ServerThread) myServer.getClients().get(i);
				st.ps.println("sendMessageDone");
				st.ps.println(message);
			}
		} catch (IOException e) {
			ps.println("error");
			e.printStackTrace();
		} catch (Exception e) {
			ps.println("error");
			e.printStackTrace();
		} finally {
			JDBCUtils.release(statement, cnnConnection);
		}

	}

	// a method to initialize the curriculum schedule and return the results
	// after handling
	private void displayCS() {

		String account = null;
		Connection cnnConnection = null;
		PreparedStatement statement = null;
		ResultSet rsResultSet = null;

		try {
			account = br.readLine();
			cnnConnection = JDBCUtils.getConnection();
			String sql = "SELECT courseInfo FROM CurriculumSchedule WHERE user_id = ?";
			statement = cnnConnection.prepareStatement(sql);
			statement.setString(1, account);
			rsResultSet = statement.executeQuery();
			String[] temp = new String[48];
			int i = 0;
			while (rsResultSet.next()) {
				temp[i] = rsResultSet.getString("courseInfo");
				i++;
			}
			ps.println("displayCSDone");
			for (int j = 0; j < temp.length; j++) {
				ps.println(temp[j]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.release(rsResultSet, statement, cnnConnection);
		}

	}

	// a method to update the curriculum schedule and return the results after
	// handling
	private void updateCurriculumSchedule() {

		Connection cnnConnection = null;
		PreparedStatement statement = null;
		String account = null;
		try {
			account = br.readLine();
			cnnConnection = JDBCUtils.getConnection();
			String sql = "UPDATE CurriculumSchedule SET courseInfo=? WHERE user_id=? AND course_id=?";
			statement = cnnConnection.prepareStatement(sql);// pre-compiling
			for (int i = 0; i < 6; i++) {
				for (int j = 0; j < 8; j++) {

					statement.setString(1, br.readLine());
					statement.setString(2, account);
					statement.setString(3, br.readLine());
					statement.addBatch();
				}
			}
			statement.executeBatch();
			ps.println("UpdateCurriculumScheduleDone");
		} catch (Exception e) {
			ps.println("error");
			e.printStackTrace();
		} finally {
			JDBCUtils.release(statement, cnnConnection);
		}
	}

	// a method to save the curriculum schedule and return the results after
	// handling
	private void savaCurriculumSchedule() {

		Connection cnnConnection = null;
		PreparedStatement statement = null;
		try {
			cnnConnection = JDBCUtils.getConnection();
			String sql = "INSERT INTO CurriculumSchedule values(null,?,?,?)";
			statement = cnnConnection.prepareStatement(sql);// pre-compiling
			// insert CurriculumSchedule information into database by batch
			// operations
			for (int i = 0; i < 6; i++) {
				for (int j = 0; j < 8; j++) {
					statement.setString(1, br.readLine());
					statement.setString(2, br.readLine());
					statement.setString(3, br.readLine());
					statement.addBatch();
				}
			}
			statement.executeBatch();
			ps.println("SavaCurriculumScheduleDone");
		} catch (Exception e) {
			ps.println("error");
			e.printStackTrace();
		} finally {
			JDBCUtils.release(statement, cnnConnection);
		}

	}

	// a method to update face and return the results after handling
	private void updateFace() {

		String account = null;
		String facefilename = null;
		Connection cnnConnection = null;
		PreparedStatement statement = null;
		try {
			account = br.readLine();
			facefilename = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			cnnConnection = JDBCUtils.getConnection();
			String sql = "update userLabel set user_face=? where user_id=?";
			statement = cnnConnection.prepareStatement(sql);// pre-compiling
			File file = new File(facefilename);
			// to store pictures' files in database by its filename
			statement.setBinaryStream(1, new FileInputStream(facefilename), (int) file.length());
			statement.setString(2, account);
			statement.executeUpdate();
			ps.println("UpdateFaceSucessfully");
		} catch (Exception e) {
			ps.println("error");
			e.printStackTrace();
		} finally {
			JDBCUtils.release(statement, cnnConnection);
		}

	}

	// a method to upload face and return the results after handling
	private void uploadFace() {

		String account = null;
		String facefilename = null;
		Connection cnnConnection = null;
		PreparedStatement statement = null;
		try {
			account = br.readLine();
			facefilename = br.readLine();

		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			cnnConnection = JDBCUtils.getConnection();
			String sql = "INSERT INTO userLabel values(?,?)";
			statement = cnnConnection.prepareStatement(sql);// use pre-compiling
															// to execute sql
															// statements

			File file = new File(facefilename);
			statement.setString(1, account);
			statement.setBinaryStream(2, new FileInputStream(facefilename), (int) file.length());
			statement.executeUpdate();
			ps.println("UploadFaceSucessfully");
		} catch (Exception e) {
			ps.println("error");
			e.printStackTrace();
		} finally {
			JDBCUtils.release(statement, cnnConnection);
		}
	}

	// a method to displayFaceAndName and return the results after handling
	private void displayFaceAndName() {

		String account = null;
		Connection cnnConnection = null;
		PreparedStatement statement = null;
		ResultSet rsResultSet = null;
		String name = null;
		String outfilename = null;
		String password = null;
		try {
			account = br.readLine();
			cnnConnection = JDBCUtils.getConnection();
			String sql = "SELECT users.name, users.password,userLabel.user_face FROM users,userLabel where users.id=userLabel.user_id AND userLabel.user_id = ?";
			statement = cnnConnection.prepareStatement(sql);
			statement.setString(1, account);
			rsResultSet = statement.executeQuery();
			if (rsResultSet.next()) {
				name = rsResultSet.getString("name");
				password = rsResultSet.getString("password");
				InputStream in = new BufferedInputStream(rsResultSet.getBinaryStream("user_face"));
				if (os.startsWith("Mac") || os.startsWith("mac")) {
					// for mac os
					outfilename = "/Users/wangcongcong/Desktop/ChopChopClient/image/face/" + account + "'sFace"
							+ ".gif";
				} else if (os.startsWith("Win") || os.startsWith("win")) {
					// for windows os
					outfilename = ".\\image\\face\\" + account + "'sFace" + ".gif";
				}
				OutputStream out = new BufferedOutputStream(new FileOutputStream(outfilename));
				int b;
				while ((b = in.read()) != -1) {
					out.write(b);
				}
				in.close();
				out.close();
				// // 获取输出流
				// outfilename = "not null";
				// OutputStream out = s.getOutputStream();
				// InputStream IS = rsResultSet.getBinaryStream("user_face");
				// byte[] buf = new byte[1024];
				// int len = 0;
				// while ((len = IS.read(buf)) != -1) {
				// out.write(buf, 0, len);
				// }
				// s.shutdownOutput();
				// IS.close();
				// out.close();
				// ps = new PrintStream(out);
			}
			ps.println("displayFaceAndNameDone");
			ps.println(name);
			ps.println(password);
			ps.println(outfilename);
		} catch (Exception e) {
			ps.println("error");
			e.printStackTrace();
		} finally {
			JDBCUtils.release(rsResultSet, statement, cnnConnection);
		}
	}

	// a method to login and return the results after handling
	private void login() {
		String password = null;

		Connection cnnConnection = null;
		PreparedStatement statement = null;
		ResultSet rsResultSet = null;
		try {
			account = br.readLine();// read account from the client
			password = br.readLine();// read password from the client
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			cnnConnection = JDBCUtils.getConnection();
			String sql = "SELECT * FROM users";
			statement = cnnConnection.prepareStatement(sql);
			rsResultSet = statement.executeQuery();
			boolean isCorrect = false;
			while (rsResultSet.next()) {
				if (account.equals(rsResultSet.getString("id")) && password.equals(rsResultSet.getString("password"))) {
					isCorrect = true;
				}

			}
			if (!isCorrect) {
				ps.println("PasswordWrong");
			} else {
				for (int i = 0; i < myServer.getClients().size(); i++) {
					ServerThread st = (ServerThread) myServer.getClients().get(i);
					if (st.account.equals(account)) {
						ps.println("AlreadyLogined");
						return;
					}
				}
				myServer.getClients().add(this);
				myServer.getJTA().append(account + " online........\n---------------------\n");
				myServer.setTitle("Current online clients:" + myServer.getClients().size());
				ps.println("successfully");
			}
		} catch (ClassNotFoundException e1) {
			ps.println("error");
		} catch (SQLException e1) {
			ps.println("error");
		} catch (Exception e1) {
			ps.println("error");
		} finally {
			JDBCUtils.release(statement, cnnConnection);
		}

	}

	// a method to register and return the results after handling
	private void register() {
		String account = null;
		String password1 = null;
		String password2 = null;
		String name = null;

		Connection cnnConnection = null;
		PreparedStatement statement = null;
		try {
			account = br.readLine();// read account from the client
			password1 = br.readLine();// read password from the client
			password2 = br.readLine();
			name = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if ("".equals(password1) || "".equals(password2) || "".equals(account) || "".equals(name)) {
			ps.println("NoCompletion");
			return;
		}
		if (!password1.equals(password2)) {
			ps.println("TwoPasswordsDifferent");
			return;
		}
		try {
			cnnConnection = JDBCUtils.getConnection();
			// wait to change it into pre-compiling situation, which will be
			// safer
			String sql = "insert into users values(?,?,?)";
			statement = cnnConnection.prepareStatement(sql);
			statement.setString(1, account);
			statement.setString(2, password1);
			statement.setString(3, name);
			statement.executeUpdate();
			ps.println("successfully!");
		} catch (ClassNotFoundException e1) {
			ps.println("error");
		} catch (SQLException e1) {
			ps.println("AccountExisted");
		} catch (Exception e1) {
			ps.println("error");
		} finally {
			JDBCUtils.release(statement, cnnConnection);
		}
	}

}
