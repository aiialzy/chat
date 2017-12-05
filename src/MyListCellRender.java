import javax.swing.*;
import java.awt.*;

public class MyListCellRender extends JLabel implements ListCellRenderer{

	private JLabel currentName;
	private JPanel panel;
	private ChatMain cmain;

	public MyListCellRender(JLabel currentName, JPanel panel, ChatMain cmain){
		this.currentName = currentName;
		this.panel = panel;
		this.cmain = cmain;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Person man = (Person) value;

		String name = man.getName();
		setText("<html>" + name + "<br>ID:" + man.getId() + "</html>");
		setFont(new Font("黑体", Font.PLAIN, 14));

		Image img = null;
		if(man.getType().compareTo(Constant.PERSON) == 0){
			if(man.photo != null){
				img = Toolkit.getDefaultToolkit().createImage(man.photo).getScaledInstance(50, 50, Image.SCALE_SMOOTH);
			}else {
				img = Toolkit.getDefaultToolkit().getImage("timg.jpg").getScaledInstance(50, 50, Image.SCALE_SMOOTH);
			}
		}else {
			img = Toolkit.getDefaultToolkit().getImage("timg2.jpg").getScaledInstance(50, 50, Image.SCALE_SMOOTH);
		}
		setIcon(new ImageIcon(img));
		setIconTextGap(30);

		if(isSelected){
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
			currentName.setText(name);
			cmain.currentLinkman = cmain.getLinkMen().get(index);
			cmain.getArea().setText(cmain.currentLinkman.getChatRecord());
			cmain.autoScroll();
			panel.setVisible(true);
		}else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		setEnabled(list.isEnabled());
		setOpaque(true);
		return this;
	}
}
