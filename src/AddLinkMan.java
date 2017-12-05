import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class AddLinkMan extends JFrame{

	private BufferedReader reader;
	private PrintWriter writer;
	private ArrayList<Person> person;
	private JList<Person> list;
	private String id;
	private JTextField textId;

	public AddLinkMan(BufferedReader reader, PrintWriter writer, ArrayList<Person> person, JList<Person> list, String id){
		this.reader = reader;
		this.writer = writer;
		this.person = person;
		this.list = list;
		this.id = id;

		init();
		setTitle("添加联系人");
		setBounds(Constant.SCREEN_WIDTH/2-150, Constant.SCREEN_HEIGHT/2-40, 300, 80);
		setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	private void add(){
		if(textId.getText().compareTo(id) == 0){
			JOptionPane.showMessageDialog(AddLinkMan.this, "不能添加自己", "提示", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JSONObject json = new JSONObject();
		json.put(Constant.LABEL_CODE, Constant.ADD_LINKMAN);
		json.put(Constant.LABEL_ID, textId.getText());
		Constant.flushWrite(writer, json);
		dispose();
	}

	private void init(){
		setLayout(new FlowLayout());

		JLabel label = new JLabel("ID");

		textId = new JTextField(15);
		textId.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ENTER){
					add();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {

			}
		});

		JButton button = new JButton("添加");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				add();
			}
		});
		button.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ENTER){
					add();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {

			}
		});

		add(label);
		add(textId);
		add(button);
	}
}
