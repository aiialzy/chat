/**
 * Created by Administrator on 2017/11/19 0019.
 */
public class Person {
	private String name;
	private String id;
	private StringBuilder chatRecord;
	private int spoi = 0;
	private String type = "";
	public byte[] photo = null;

	public int getSpoi() {
		return spoi;
	}

	public String getType() {
		return type;
	}

	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}

	public void setSpoi(int spoi) {
		this.spoi = spoi;
	}

	public Person(String name, String id, String type){
		this.name = name;
		this.id = id;
		this.type = type;
		chatRecord = new StringBuilder();
	}

	public Person(String name, String id, String type, byte[] photo){
		this(name, id, type);
		this.photo = photo;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public String getChatRecord(){
		return chatRecord.toString();
	}

	public void appendRecord(String str){
		chatRecord.append(str);
	}

}
