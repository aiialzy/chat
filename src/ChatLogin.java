import org.json.JSONObject;
import sun.misc.BASE64Encoder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;

/**
 * Created by Administrator on 2017/11/18 0018.
 */
public class ChatLogin {

	private Socket socketLinkServer;
	private PrintWriter writer;
	private BufferedReader reader;

	private JTextField textId;
	private JPasswordField textPasswd;

	JFrame frame = null;

	public ChatLogin(){
		init();
		//linkServer();
	}

	public ChatLogin(String id){
		init();
		textId.setText(id);
	}

	private void linkServer(){
		try{
			socketLinkServer = new Socket(Constant.ip, Constant.port);
			reader = new BufferedReader(new InputStreamReader(socketLinkServer.getInputStream()));
			writer = new PrintWriter(new PrintWriter(socketLinkServer.getOutputStream()));
			String account = textId.getText();
			String passwd = new String(textPasswd.getPassword());
			try{
				MessageDigest md5 = MessageDigest.getInstance("MD5");
				passwd = new BASE64Encoder().encode(md5.digest(passwd.getBytes("utf-8")));
			}catch (Exception e){
				e.printStackTrace();
				JOptionPane.showMessageDialog(frame, "未知错误", "登录失败", JOptionPane.ERROR_MESSAGE);
			}
			JSONObject json = new JSONObject();
			json.put(Constant.LABEL_CODE, Constant.LOGIN);
			json.put(Constant.LABEL_ID, account);
			json.put(Constant.LABEL_PASSWD, passwd);
			Constant.flushWrite(writer, json);
			json = new JSONObject(reader.readLine());
			String code = json.getString(Constant.LABEL_CODE);
			if(code.compareTo(Constant.LOGIN_SUCCESS) == 0){
				frame.dispose();
				new ChatMain(socketLinkServer, reader, writer, json, account);
				return;
			}else if(code.compareTo(Constant.LOGIN_ALREADY_EXIST) == 0){
				JOptionPane.showMessageDialog(frame, "账号已登录", "登录失败", JOptionPane.ERROR_MESSAGE);
				return;
			}
			JOptionPane.showMessageDialog(frame, "账号或者密码错误", "登录失败", JOptionPane.ERROR_MESSAGE);
			reader.close();
			writer.close();
			socketLinkServer.close();
		}catch (IOException error){
			JOptionPane.showMessageDialog(frame, "无法正常连接服务器", "登录失败", JOptionPane.ERROR_MESSAGE);
			error.printStackTrace();
		}
	}

	private void init(){

		frame = new JFrame("登录");
		frame.setBounds(Constant.SCREEN_WIDTH/2-100, Constant.SCREEN_HEIGHT /2-100, 230, 140);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);

		KeyListener listener = new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					linkServer();
				}
			}
			@Override
			public void keyReleased(KeyEvent e) {
			}
		};

		frame.setLayout(new FlowLayout());
		JLabel labelId = new JLabel("账号");
		textId = new JTextField(15);
		textId.setText("10001");
		textId.addKeyListener(listener);

		JLabel labelPasswd = new JLabel("密码");
		textPasswd = new JPasswordField(15);
		textPasswd.addKeyListener(listener);

		JButton buttonLogin = new JButton("登录");
		buttonLogin.addKeyListener(listener);
		buttonLogin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				linkServer();
			}
		});

		JButton buttonRegister = new JButton("注册");
		buttonRegister.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				new ChatRegister();
			}
		});
		textPasswd.setText("123456");


		frame.add(labelId);
		frame.add(textId);
		frame.add(labelPasswd);
		frame.add(textPasswd);
		frame.add(buttonLogin);
		frame.add(buttonRegister);

		frame.setVisible(true);
	}

	public static void main(String[] args){
		new ChatLogin();
	}
}
