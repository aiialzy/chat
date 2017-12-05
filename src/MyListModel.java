import javax.swing.*;
import java.util.ArrayList;

public class MyListModel extends AbstractListModel{
	private ArrayList<Person> people;

	public MyListModel(ArrayList<Person> linkMen){
		this.people = linkMen;
	}

	@Override
	public Object getElementAt(int index) {
		return people.get(index);
	}

	@Override
	public int getSize() {
		return people.size();
	}
}
