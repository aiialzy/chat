import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.io.*;
import java.net.Socket;
import java.util.Date;

public class ChangePhoto extends JFrame{

	private File file;
	private JLabel label;
	private Image img;
	private PrintWriter writer;
	private Socket socket;

	public ChangePhoto(Socket socket, PrintWriter writer, byte[] photo){
		this.writer = writer;
		this.socket = socket;
		this.img = Toolkit.getDefaultToolkit().createImage(photo);
		init();
		setVisible(true);
	}

	private void choosePhoto(){
		JFileChooser jfc = new JFileChooser();
		jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		jfc.showDialog(new JLabel(), "选择");
		file = jfc.getSelectedFile();
		this.img = Toolkit.getDefaultToolkit().getImage(file.getPath()).getScaledInstance(50, 50, Image.SCALE_SMOOTH);
		this.label.setIcon(new ImageIcon(img));
	}

	private void changePhoto(){
		try{
			this.img = ImageIO.read(file);
			long size = file.length();
			JSONObject json = new JSONObject();
			json.put(Constant.LABEL_CODE, Constant.CHANGE_PHOTO);
			json.put(Constant.PHOTO_SIZE, size);
			Constant.flushWrite(writer, json);
			new Thread(){
				public void run(){
					try{
						DataInputStream r = new DataInputStream(new FileInputStream(file));
						byte[] photo = new byte[(int)size];
						r.read(photo);
						r.close();
						DataOutputStream writePhoto = new DataOutputStream(socket.getOutputStream());
						writePhoto.write(photo);
						writePhoto.flush();
					}catch (IOException error){
						error.printStackTrace();
					}
				}
			}.start();
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	public void init(){
		setLayout(null);
		setTitle("更换头像");
		setBounds(Constant.SCREEN_WIDTH/2-115, Constant.SCREEN_HEIGHT /2-125, 220, 180);
		setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		label = new JLabel();
		img = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
		label.setIcon(new ImageIcon(img));
		label.setBounds(80, 25, 50, 50);
		add(label);

		JButton choose = new JButton("选择");
		choose.setBounds(30, 90, 60, 30);
		choose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				choosePhoto();
			}
		});
		add(choose);

		JButton change = new JButton("更换");
		change.setBounds(120, 90, 60, 30);
		change.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				changePhoto();
			}
		});
		add(change);
	}
}
