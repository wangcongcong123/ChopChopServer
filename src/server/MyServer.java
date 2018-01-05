package server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * the class plays a role in listening the socket of the clients every time one
 * client asks a connection to the server, the class will assign a thread to
 * manage the client this is the starting program of the server you have to run
 * this class before you start your programs of the client
 * 
 * @author group 12
 * 
 */
public class MyServer extends JFrame implements Runnable {
	private Socket s = null;
	private ServerSocket ss = null;
	private ArrayList clients = new ArrayList();
	private JTextArea jta = new JTextArea();
	private JScrollPane jspJScrollPane = new JScrollPane();
	private boolean canRun = true;

	public MyServer() {
		jspJScrollPane.getViewport().add(jta, BorderLayout.CENTER);
		this.add(jspJScrollPane);
		this.setTitle("server");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBackground(Color.yellow);
		this.setSize(new Dimension(500, 500));
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		try {
			// give a port to ServerSocket and wait the corresponding connection
			// from the client
			ss = new ServerSocket(9999);
			new Thread(this).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		jta.append("Server opened successfully!\n");
	}

	public void run() {

		try {
			while (canRun) {
				s = ss.accept();

				// dispense a thread to manage a user
				// jta.append("one client
				// online......\n-----------------------------\n");
				ServerThread st = new ServerThread(s, this);
				st.start();
			}
		} catch (Exception e) {
			canRun = false;
			try {
				ss.close();
			} catch (Exception e2) {

			}
		}
	}

	public ArrayList getClients() {
		return clients;
	}

	public JTextArea getJTA() {
		return jta;
	}

	public static void main(String[] args) throws Exception {
		MyServer server = new MyServer();
	}

}
