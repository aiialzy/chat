import org.json.JSONObject;
import sun.misc.BASE64Encoder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;

/**
 * Created by Administrator on 2017/11/18 0018.
 */
public class ChatRegister extends JFrame{

	private PrintWriter writer;
	private Socket socket;
	private BufferedReader reader;


	private void register(JTextField name, JPasswordField passwd){
		if(name.getText().isEmpty()){
			JOptionPane.showMessageDialog(this, "昵称不能为空", "错误", JOptionPane.ERROR_MESSAGE);
			return;
		}else if(new String(passwd.getPassword()).isEmpty()){
			JOptionPane.showMessageDialog(this, "密码不能为空", "错误", JOptionPane.ERROR_MESSAGE);
			return;
		}else if(!name.getText().matches(".*\\w*.*")){
			JOptionPane.showMessageDialog(this, "昵称有空白符", "错误", JOptionPane.ERROR_MESSAGE);
			return;
		}else if(!new String(passwd.getPassword()).matches("[0-9a-zA-Z_+\\-*/.]{6,15}")){
			JOptionPane.showMessageDialog(this, "密码仅能用数字、大小字母和 _ - * / + .", "错误", JOptionPane.ERROR_MESSAGE);
			return;
		}
		String id = "";
		try{
			try{
				socket = new Socket(Constant.ip, Constant.port);
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				writer = new PrintWriter(socket.getOutputStream());
			}catch (IOException e){
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "无法正常连接服务器", "提示", JOptionPane.ERROR_MESSAGE);
			}
			String password = new String(passwd.getPassword());
			try{
				MessageDigest md5 = MessageDigest.getInstance("MD5");
				password = new BASE64Encoder().encode(md5.digest(password.getBytes("utf-8")));
			}catch (Exception e){
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "未知错误", "注册失败", JOptionPane.ERROR_MESSAGE);
			}
			JSONObject json = new JSONObject();
			json.put(Constant.LABEL_CODE, Constant.REGISTER);
			json.put(Constant.LABEL_NAME, name.getText());
			json.put(Constant.LABEL_PASSWD, password);
			Constant.flushWrite(writer, json);
			json = new JSONObject(reader.readLine());
			String code = json.getString(Constant.LABEL_CODE);
			if(code.compareTo(Constant.REGISTER_FAIL) == 0){
				JOptionPane.showMessageDialog(ChatRegister.this, "注册失败", "提示", JOptionPane.ERROR_MESSAGE);
				return;
			}
			id = json.getString("id");
			writer.close();
			reader.close();
			socket.close();

			if(code.compareTo(Constant.REGISTER_SUCCESS) == 0){
				JOptionPane.showMessageDialog(ChatRegister.this, "获得账号ID:"+id, "注册成功", JOptionPane.PLAIN_MESSAGE);
				dispose();
				new ChatLogin(id);
				File file = new File("L" + id);
				if(!file.exists()){
					file.mkdir();
				}
			}
		}catch (IOException error){
			error.printStackTrace();
		}
	}

	public ChatRegister(){
		setBounds(Constant.SCREEN_WIDTH/2-100, Constant.SCREEN_HEIGHT /2-100, 230, 140);
		init();
		setTitle("注册");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setVisible(true);
	}

	private void init(){
		setLayout(new FlowLayout());
		JLabel labelName = new JLabel("昵称");

		JTextField textName = new JTextField(15);

		JLabel labelPasswd = new JLabel("密码");

		JPasswordField textPasswd = new JPasswordField(15);

		textName.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ENTER){
					register(textName, textPasswd);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {

			}
		});

		textPasswd.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ENTER){
					register(textName, textPasswd);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {

			}
		});

		JButton buttonRegister = new JButton("注册");
		buttonRegister.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				register(textName, textPasswd);
			}
		});

		buttonRegister.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ENTER){
					register(textName, textPasswd);
				}
			}
			@Override
			public void keyReleased(KeyEvent e) {
			}
		});

		JButton buttonBack = new JButton("返回");
		buttonBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
				new ChatLogin();
			}
		});

		add(labelName);
		add(textName);
		add(labelPasswd);
		add(textPasswd);
		add(buttonRegister);
		add(buttonBack);
	}
}
