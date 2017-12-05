import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.PrintWriter;

public class CreateGroup extends JFrame{

	private PrintWriter writer;
	private JTextField textName;

	public PrintWriter getWriter() {
		return writer;
	}

	private void create(){
		JSONObject json = new JSONObject();
		json.put(Constant.LABEL_CODE, Constant.CREATE_GROUP);
		json.put(Constant.LABEL_NAME, textName.getText());
		Constant.flushWrite(this.getWriter(), json);
		dispose();
	}

	public CreateGroup(PrintWriter writer){
		this.writer = writer;
		init();
	}

	private void init(){

		setTitle("创建群组");
		setBounds(Constant.SCREEN_WIDTH/2-150, Constant.SCREEN_HEIGHT /2-45, 300, 90);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(false);

		KeyListener listener = new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					create();
				}
			}
			@Override
			public void keyReleased(KeyEvent e) {
			}
		};

		setLayout(new FlowLayout());
		JLabel labelName = new JLabel("群名称");
		textName = new JTextField();
		textName = new JTextField(15);
		textName.addKeyListener(listener);

		JButton buttonCreate = new JButton("创建");
		buttonCreate.addKeyListener(listener);
		buttonCreate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				create();
			}
		});

		add(labelName);
		add(textName);
		add(buttonCreate);

		setVisible(true);
	}
}
