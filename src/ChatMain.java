import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Administrator on 2017/11/18 0018.
 */
public class ChatMain extends JFrame implements Runnable{
	public final static int LINKMAN_HEIGHT = 690;
	public final static int LINKMAN_WIDTH = 200;

	private JScrollPane jspl;
	private JScrollPane jsplSaid;
	private JTextArea area;
	private JList list;

	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;

	private ArrayList<Person> linkMen = new ArrayList<>();

	private String id = "";
	private String name = "";
	private byte[] photo = null;

	public Person currentLinkman;

	public String getId() {
		return id;
	}

	public void saveLog(File file, String str){
		try{
			File dic = new File(file.getParent());
			if(!dic.exists()){
				dic.mkdir();
			}
			FileWriter fileWriter = new FileWriter(file, true);
			if(file.exists()){
				fileWriter.append(str);
			}else {
				fileWriter.write(str);
			}
			fileWriter.close();
		}catch (IOException e){
			e.printStackTrace();
		}

	}

	public ChatMain(Socket socket, BufferedReader reader, PrintWriter writer, JSONObject json, String id){
		this.socket = socket;
		this.reader = reader;
		this.writer = writer;
		this.id = id;

		try{
			this.name = json.getString(Constant.LABEL_NAME);
			JSONObject tem;
			JSONArray jsonArray = json.getJSONArray(Constant.LABEL_LINKLIST);
			for(int i = 0; i<json.getInt(Constant.LABEL_LINKMAN_COUNT); ++i){
				tem = jsonArray.getJSONObject(i);
				String name = tem.getString(Constant.LABEL_NAME);
				String tid = tem.getString(Constant.LABEL_ID);
				String type = tem.getString(Constant.LABEL_TYPE);
				Person aPerson = new Person(name, tid, type);
				File record = new File("L" + this.id + "\\L" + tid);
				if(record.exists()){
					BufferedReader readRecord = new BufferedReader(new FileReader(record));
					String str = "";
					while((str=readRecord.readLine()) != null){
						aPerson.appendRecord(str + "\n");
					}
				}
				linkMen.add(aPerson);
			}
		}catch (Exception e){
		}
		setBounds(Constant.SCREEN_WIDTH/2-400, Constant.SCREEN_HEIGHT/2-400, 800, 730);
		init();
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setTitle(this.name + "的聊天室");

		new Thread(this).start();
	}

	public JTextArea getArea() {
		return area;
	}

	public void autoScroll(){
		if(area.getCaretPosition() == currentLinkman.getSpoi()){
			area.setCaretPosition(area.getDocument().getLength());
		}
	}

	private void updatePhoto(JSONObject json, Socket socket){
		for(int i=0; i<linkMen.size(); ++i){
			if(json.getString(Constant.LABEL_ID).compareTo(linkMen.get(i).getId()) == 0){
				Person theOne = linkMen.get(i);
				byte[] photo = new byte[json.getInt(Constant.PHOTO_SIZE)];
				try{
					DataInputStream readPhoto = new DataInputStream(socket.getInputStream());
					readPhoto.read(photo);
					readPhoto = null;
				}catch (IOException e){
					e.printStackTrace();
				}
				theOne.setPhoto(photo);
				break;
			}
		}
	}

	private void updateMyPhoto(JSONObject json, Socket socket){
		try{
			this.photo = new byte[json.getInt(Constant.PHOTO_SIZE)];
			DataInputStream readPhoto = new DataInputStream(socket.getInputStream());
			readPhoto.read(photo);
			readPhoto = null;
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	private void setCurrentLinkman(int index){
		currentLinkman = linkMen.get(index);
	}

	private void sendMessage(JTextArea textArea){
		String time = new Date().toString();
		String text = textArea.getText();
		String str = this.name + "\t" + time + "\n" + text + "\n\n";
		currentLinkman.appendRecord(str);
		File file = new File("L" + getId() + "\\L" + currentLinkman.getId());
		saveLog(file, str);
		area.append(str);

		JSONObject json = new JSONObject();
		if(currentLinkman.getType().compareTo(Constant.PERSON) == 0){
			json.put(Constant.LABEL_CODE, Constant.PERSON_MESSAGE);
		}else {
			json.put(Constant.LABEL_CODE, Constant.GROUP_MESSAGE);
		}
		json.put(Constant.LABEL_SENDER, this.id);
		json.put(Constant.LABEL_GETTER, currentLinkman.getId());
		json.put(Constant.LABEL_CONTENT, text);
		Constant.flushWrite(writer, json);

		textArea.setText("");
		autoScroll();
	}

	@Override
	public String getName() {
		return name;
	}

	private void init(){
		setLayout(null);

		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("账号");

		JMenuItem infomation = new JMenuItem("账号信息");
		infomation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(ChatMain.this, "ID:" + getId() + "\n昵称:" + getName(), "账号信息", JOptionPane.PLAIN_MESSAGE);
			}
		});
		menu.add(infomation);


		JMenuItem changePhoto = new JMenuItem("更换头像");
		changePhoto.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ChangePhoto(socket, writer, photo);
			}
		});
		menu.add(changePhoto);


		JMenuItem addLinkMan = new JMenuItem("添加联系人");
		addLinkMan.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new AddLinkMan(reader, writer, linkMen, list, id);
			}
		});
		menu.add(addLinkMan);


		JMenuItem exit = new JMenuItem("退出");
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		menu.add(exit);
		menuBar.add(menu);


		JMenu groupMenu = new JMenu("群组");
		JMenuItem createGroup = new JMenuItem("创建群组");
		createGroup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new CreateGroup(writer);
			}
		});
		groupMenu.add(createGroup);


		JMenuItem addGroup = new JMenuItem("添加群组");
		addGroup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new AddGroup(writer, linkMen, list);
			}
		});
		groupMenu.add(addGroup);
		menuBar.add(groupMenu);

		setJMenuBar(menuBar);

		JPanel content = new JPanel();
		content.setLayout(null);
		content.setBounds(LINKMAN_WIDTH+1, 0, 593, LINKMAN_HEIGHT-4);
		content.setBorder(BorderFactory.createEtchedBorder());
		content.setVisible(false);

		JLabel linkManName = new JLabel();
		linkManName.setFont(new Font("宋体", Font.BOLD, 25));
		linkManName.setBounds(23, 10, 593, 25);

		area = new JTextArea();
		area.setEditable(false);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		jsplSaid = new JScrollPane(area);
		jsplSaid.setBounds(23, 45, 555, 510);

		JTextArea contentSend = new JTextArea(3, 50);
		contentSend.setLineWrap(true);
		contentSend.setWrapStyleWord(true);
		jspl = new JScrollPane(contentSend);
		jspl.setBounds(23, 565, 555, 68);
		contentSend.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ENTER && e.isControlDown()){
					sendMessage(contentSend);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});

		JButton send = new JButton("发送");
		send.setBounds(498, 636, 80, 30);
		send.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ENTER){
					sendMessage(contentSend);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {

			}
		});
		send.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendMessage(contentSend);
			}
		});

		content.add(send);
		content.add(jsplSaid);
		content.add(jspl);
		content.add(linkManName);

		MyListModel men = new MyListModel(linkMen);
		list = new JList(men);
		list.setCellRenderer(new MyListCellRender(linkManName, content, this));
		JScrollPane jslp = new JScrollPane(list);
		jslp.setBounds(0, 0, LINKMAN_WIDTH, LINKMAN_HEIGHT);

		add(content);
		add(jslp);
	}

	private void onlineMessage(JSONObject json, String code){
		String senderId = json.getString(Constant.LABEL_SENDER);
		for(int i=0; i<linkMen.size(); ++i){
			Person who = linkMen.get(i);
			if(senderId.compareTo(who.getId()) == 0){
				String n = "";
				if(code.compareTo(Constant.PERSON_MESSAGE) == 0){
					n = linkMen.get(i).getName();
				}else {
					n = json.getString(Constant.LABEL_NAME);
				}
				String str = n
						+ "\t" + new Date().toString() + "\n"
						+ json.getString(Constant.LABEL_CONTENT)
						+ "\n\n";
				File file = new File("L" + getId() + "\\L" + senderId);
				saveLog(file, str);
				who.appendRecord(str);
				if(currentLinkman!=null && senderId.compareTo(currentLinkman.getId()) == 0){
					area.setText(currentLinkman.getChatRecord());
					autoScroll();
				}else {
				}
				break;
			}
		}

	}

	private void updatePhoto(){
		JSONObject json = new JSONObject();
		json.put(Constant.LABEL_CODE, Constant.UPDATE_PHOTO);
		Constant.flushWrite(writer, json);
	}

	private void lastWork(){
		JSONObject sjson = new JSONObject();
		sjson.put(Constant.LABEL_CODE, Constant.READY);
		Constant.flushWrite(writer, sjson);
	}

	public ArrayList<Person> getLinkMen() {
		return linkMen;
	}

	public JList getList() {
		return list;
	}

	@Override
	public void run() {
		JSONObject json;
		try{
			lastWork();
			while(true){
				json = new JSONObject(reader.readLine());
				String code = json.getString(Constant.LABEL_CODE);
				switch (code){
					case Constant.LOGIN_ALREADY_EXIST:
						JOptionPane.showMessageDialog(this, "账号已在别处登录", "提示", JOptionPane.WARNING_MESSAGE);
						System.exit(0);
						break;

					case Constant.GROUP_MESSAGE:
					case Constant.PERSON_MESSAGE:
						onlineMessage(json, code);
						break;

					case Constant.ADD_LINKMAN_SUCCESS:
						JOptionPane.showMessageDialog(this, "添加成功", "提示", JOptionPane.PLAIN_MESSAGE);
						linkMen.add(new Person(json.getString(Constant.LABEL_NAME), json.getString(Constant.LABEL_ID), Constant.PERSON));
						list.updateUI();
						break;

					case Constant.ADD_LINKMAN_REJECT:
						JOptionPane.showMessageDialog(this, "添加\nID:" + json.getString(Constant.LABEL_ID) + "\n昵称:" + json.getString(Constant.LABEL_NAME) + "\n为好友时被拒绝", "提示", JOptionPane.ERROR_MESSAGE);
						break;

					case Constant.ADD_LINKMAN_FAIL:
						JOptionPane.showMessageDialog(this, "添加失败", "提示", JOptionPane.ERROR_MESSAGE);
						break;

					case Constant.ADD_LINKMAN_ED:
						int n = JOptionPane.showConfirmDialog(this, "<html>昵称：" + json.getString(Constant.LABEL_NAME)
								+ "<br>ID:" + json.getString(Constant.LABEL_ID), "添加好友邀请", JOptionPane.YES_NO_OPTION);
						JSONObject tem = new JSONObject();
						if(n == JOptionPane.YES_OPTION){
							linkMen.add(new Person(json.getString(Constant.LABEL_NAME), json.getString(Constant.LABEL_ID), Constant.PERSON));
							list.updateUI();
							tem.put(Constant.LABEL_CODE, Constant.ADD_LINKMAN_ACCEPT);
						}else {
							tem.put(Constant.LABEL_CODE, Constant.ADD_LINKMAN_REJECT);
						}
						tem.put(Constant.LABEL_ID, json.getString(Constant.LABEL_ID));
						tem.put(Constant.LABEL_NAME, json.getString(Constant.LABEL_NAME));
						Constant.flushWrite(writer, tem);
						break;

					case Constant.ADD_LINKMAN_ALREADY_EXIST:
						JOptionPane.showMessageDialog(this, "不能添加已有联系人", "添加联系人失败", JOptionPane.ERROR_MESSAGE);
						break;

					case Constant.CREATE_GROUP_SUCCESS:
						JOptionPane.showMessageDialog(this, "创建群组成功\n群号:" + json.getString(Constant.LABEL_ID) + "\n群名:" + json.getString(Constant.LABEL_NAME), "提示", JOptionPane.PLAIN_MESSAGE);
						this.getLinkMen().add(new Person(json.getString(Constant.LABEL_NAME), json.getString(Constant.LABEL_ID), Constant.GROUP));
						this.getList().updateUI();
						break;

					case Constant.CREATE_GROUP_FAIL:
						JOptionPane.showMessageDialog(this, "创建群组失败", "提示", JOptionPane.ERROR_MESSAGE);
						break;

					case Constant.ADD_GROUP_SUCCESS:
						JOptionPane.showMessageDialog(this, "添加群组成功\n群号:" + json.getString(Constant.LABEL_ID) + "\n群名:" + json.getString(Constant.LABEL_NAME), "提示", JOptionPane.PLAIN_MESSAGE);
						this.getLinkMen().add(new Person(json.getString(Constant.LABEL_NAME), json.getString(Constant.LABEL_ID), Constant.GROUP));
						this.getList().updateUI();
						break;

					case Constant.ADD_GROUP_FAIL:
						JOptionPane.showMessageDialog(this, "添加群组失败", "提示", JOptionPane.ERROR_MESSAGE);
						break;

					case Constant.LABEL_PHOTO_EXIST:
						updatePhoto(json, socket);
						break;

					case Constant.MYPHOTO:
						updateMyPhoto(json, socket);
						break;
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}