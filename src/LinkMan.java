import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.sql.ResultSet;

public class LinkMan implements Runnable{
	private String id;
	private String name;
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;


	public LinkMan(Socket socket, BufferedReader reader, PrintWriter writer, String id, String name){
		this.id = id;
		this.name = name;
		this.socket = socket;
		this.reader = reader;
		this.writer = writer;
	}

	public String getName() {
		return name;
	}

	public Socket getSocket() {
		return socket;
	}

	public BufferedReader getReader() {
		return reader;
	}

	public PrintWriter getWriter() {
		return writer;
	}

	public String getId() {
		return id;
	}

	private void sendGroupMessage(JSONObject json){
		try{
			String command = "SELECT * FROM G" + json.getString(Constant.LABEL_GETTER) + ";";
			ResultSet result = Server.stmtAllLinkMan.executeQuery(command);
			while(result.next()){
				String target = result.getString("ID");
				int num = Server.isOnline(target);
				if(target.compareTo(this.getId())!=0){
					JSONObject tem = new JSONObject();
					tem.put(Constant.LABEL_CODE, Constant.GROUP_MESSAGE);
					tem.put(Constant.LABEL_SENDER, json.getString(Constant.LABEL_GETTER));
					tem.put(Constant.LABEL_CONTENT, json.getString(Constant.LABEL_CONTENT));
					tem.put(Constant.LABEL_NAME, this.getName());
					if(num>=0){
						Constant.flushWrite(Server.linkMen.get(num).getWriter(), tem);
					}else {
						try{
							File file = new File("messages\\L" + target);
							if(file.exists()){
								BufferedReader input = new BufferedReader(new FileReader(file));
								StringBuilder builder = new StringBuilder();
								String str = "";
								while((str=input.readLine()) != null){
									builder.append(str);
								}
								input.close();
								JSONObject tem2 = new JSONObject(builder.toString());
								tem2.append(Constant.OFFLINE_MESSAGE, tem);
								FileWriter output = new FileWriter(file);
								output.write(tem2.toString());
								output.close();
							}else {
								JSONObject tem2 = new JSONObject();
								tem2.append(Constant.OFFLINE_MESSAGE, tem);
								FileWriter output = new FileWriter(file);
								output.write(tem2.toString());
								output.close();
							}
						}catch (IOException e){
							e.printStackTrace();
						}
					}
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private void acceptAddLinkMan(JSONObject json){
		try{
			String command = "SELECT * FROM LINKMAN;";
			String addId = json.getString(Constant.LABEL_ID);
			ResultSet result = Server.stmt.executeQuery(command);
			while(result.next()){
				if(addId.compareTo(result.getString("ID")) == 0) {
					command = "INSERT INTO L" + this.getId() + " VALUES(\"" + addId + "\", \"" + result.getString("NAME") + "\", \"" + Constant.PERSON + "\");";
					Server.stmtAllLinkMan.executeUpdate(command);
					command = "INSERT INTO L" + addId + " VALUES(\"" + this.getId() + "\", \"" + this.getName() + "\", \"" + Constant.PERSON + "\");";
					Server.stmtAllLinkMan.executeUpdate(command);
				}
			}
			int n = Server.isOnline(addId);
			if(n >= 0){
				JSONObject tem = new JSONObject();
				tem.put(Constant.LABEL_CODE, Constant.ADD_LINKMAN_SUCCESS);
				tem.put(Constant.LABEL_ID, this.getId());
				tem.put(Constant.LABEL_NAME, this.getName());
				tem.put(Constant.LABEL_TYPE, Constant.PERSON);
				Constant.flushWrite(Server.linkMen.get(n).getWriter(), tem);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private void addGroup(JSONObject json){
		try{
			String tid = json.getString(Constant.LABEL_ID);
			String command = "INSERT INTO G" + tid + " VALUES(\"" + this.getId() + "\", \"" + this.getName() + "\");";
			Server.stmtAllLinkMan.executeUpdate(command);

			command = "SELECT * FROM GROUPS WHERE ID LIKE \""+ tid +"\";";
			ResultSet result = Server.stmt.executeQuery(command);
			command = "INSERT INTO L" + this.getId() + " VALUES(\"" + tid + "\", \"" + result.getString("NAME") + "\", \"" + Constant.GROUP + "\");";
			Server.stmtAllLinkMan.executeUpdate(command);

			JSONObject tem = new JSONObject();
			tem.put(Constant.LABEL_CODE, Constant.ADD_GROUP_SUCCESS);
			tem.put(Constant.LABEL_ID, tid);
			tem.put(Constant.LABEL_NAME, result.getString("NAME"));
			Constant.flushWrite(writer, tem);
		}catch (Exception e){
			e.printStackTrace();
			JSONObject tem = new JSONObject();
			tem.put(Constant.LABEL_CODE, Constant.ADD_GROUP_FAIL);
			Constant.flushWrite(writer, tem);
		}
	}

	private void addLinkMan(JSONObject json){
		String addId = json.getString(Constant.LABEL_ID);
		JSONObject tem = new JSONObject();
		tem.put(Constant.LABEL_CODE, Constant.ADD_LINKMAN_ED);
		tem.put(Constant.LABEL_ID, this.getId());
		try{
			String command = "SELECT * FROM L" + getId() + ";";
			ResultSet result = Server.stmtAllLinkMan.executeQuery(command);
			while(result.next()){
				if(result.getString("ID").compareTo(addId) == 0){
					tem = new JSONObject();
					tem.put(Constant.LABEL_CODE, Constant.ADD_LINKMAN_ALREADY_EXIST);
					Constant.flushWrite(getWriter(), tem);
					return;
				}
			}

			command = "SELECT * FROM LINKMAN WHERE ID LIKE \"" + addId + "\";";
			result = Server.stmt.executeQuery(command);
			if(!result.next()){
				IOException te = new IOException();
				throw te;
			}

			command = "SELECT * FROM LINKMAN;";
			result = Server.stmt.executeQuery(command);
			while (result.next()) {
				String target = result.getString("ID");
				if (this.getId().compareTo(target) == 0) {
					tem.put(Constant.LABEL_NAME, result.getString("NAME"));
					int n = Server.isOnline(addId);
					if(n >= 0) {
						Constant.flushWrite(Server.linkMen.get(n).getWriter(), tem);
					}else {
						File file = new File("addLinkMan\\L" + addId);
						if(file.exists()){
							BufferedReader readAddLinkMan = new BufferedReader(new FileReader(file));
							StringBuilder builder = new StringBuilder();
							String str = "";
							while((str=readAddLinkMan.readLine()) != null){
								builder.append(str);
							}
							readAddLinkMan.close();
							JSONObject lms = new JSONObject(builder.toString());
							lms.append(Constant.ADD_OFFLINE_LINKMAN, tem);
							FileWriter wlms = new FileWriter(file);
							wlms.write(lms.toString());
							wlms.close();
						}else {
							JSONObject lms = new JSONObject();
							lms.append(Constant.ADD_OFFLINE_LINKMAN, tem);
							FileWriter wlms = new FileWriter(file);
							wlms.write(lms.toString());
							wlms.close();
						}
					}
					return;
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		tem.put(Constant.LABEL_CODE, Constant.ADD_LINKMAN_FAIL);
		Constant.flushWrite(writer, tem);
	}



	private void createGroup(JSONObject json){
		try{
			String str = "SELECT * FROM CONSTANT WHERE NAME LIKE \"PERSON_COUNT\";";
			ResultSet gCount = Server.stmt.executeQuery(str);
			int count = gCount.getInt("COUNT");
			str = "UPDATE CONSTANT SET COUNT = " + (count+1) + " WHERE NAME LIKE \"PERSON_COUNT\";";
			Server.stmt.executeUpdate(str);

			String tid = new Integer(count).toString();
			str = "INSERT INTO GROUPS VALUES(\"" + tid + "\",\"" + json.getString(Constant.LABEL_NAME) + "\")";
			Server.stmt.executeUpdate(str);

			str = "CREATE TABLE G" + tid + "(ID TEXT PRIMARY KEY NOT NULL, NAME TEXT NOT NULL);";
			Server.stmtAllLinkMan.executeUpdate(str);
			str = "INSERT INTO G" + tid + " VALUES(\"" + this.getId() + "\", \"" + this.getName() + "\");";
			Server.stmtAllLinkMan.executeUpdate(str);

			str = "INSERT INTO L" + this.getId() + " VALUES(\"" + tid  + "\", \"" + json.getString(Constant.LABEL_NAME) + "\", \"" + Constant.GROUP + "\");";
			Server.stmtAllLinkMan.executeUpdate(str);

			JSONObject tem = new JSONObject();
			tem.put(Constant.LABEL_CODE, Constant.CREATE_GROUP_SUCCESS);
			tem.put(Constant.LABEL_ID, tid);
			tem.put(Constant.LABEL_NAME, json.getString(Constant.LABEL_NAME));
			Constant.flushWrite(getWriter(), tem);
			return;
		}catch (Exception e){
			e.printStackTrace();
		}
		JSONObject tem = new JSONObject();
		tem.put(Constant.LABEL_CODE, Constant.CREATE_GROUP_FAIL);
		Constant.flushWrite(getWriter(), tem);
	}

	private void updatePhoto(){
		try{
			String command = "SELECT * FROM L" + getId() + ";";
			ResultSet rs = Server.stmtAllLinkMan.executeQuery(command);
			JSONObject sjson = new JSONObject();
			sjson.put(Constant.LABEL_CODE, Constant.LABEL_PHOTO_EXIST);
			while(rs.next()){
				String p = "photos\\L" + rs.getString("ID") + ".jpg";
				File photo = new File(p);
				if(photo.exists()){
					sjson.put(Constant.LABEL_ID, rs.getString("ID"));
					int size = (int)photo.length();
					sjson.put(Constant.PHOTO_SIZE, size);
					Constant.flushWrite(writer, sjson);
					DataInputStream readPhoto = new DataInputStream(new FileInputStream(photo));
					byte[] photoBuffer= new byte[size];
					readPhoto.read(photoBuffer);
					readPhoto.close();
					readPhoto = null;
					DataOutputStream writePhoto = new DataOutputStream(socket.getOutputStream());
					writePhoto.write(photoBuffer);
					writePhoto.flush();
					writePhoto = null;
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private void updateMyPhoto(){
		JSONObject sjson = new JSONObject();
		sjson.put(Constant.LABEL_CODE, Constant.MYPHOTO);
		String p = "photos\\L" + this.getId() + ".jpg";
		File photo = new File(p);
		if(photo.exists()){
			int size = (int)photo.length();
			sjson.put(Constant.PHOTO_SIZE, size);
			Constant.flushWrite(writer, sjson);
			try{
				DataInputStream readPhoto = new DataInputStream(new FileInputStream(photo));
				byte[] photoBuffer= new byte[size];
				readPhoto.read(photoBuffer);
				readPhoto.close();
				readPhoto = null;
				DataOutputStream writePhoto = new DataOutputStream(socket.getOutputStream());
				writePhoto.write(photoBuffer);
				writePhoto.flush();
				writePhoto = null;
			}catch (IOException e){
				e.printStackTrace();
			}
		}
	}

	private void sendPersonMessage(JSONObject json){
		String fid = json.getString(Constant.LABEL_GETTER);
		int num = Server.isOnline(fid);
		if (num >= 0) {
			Constant.flushWrite(Server.linkMen.get(num).getWriter(), json);
		}else {
			try{
				File file = new File("messages\\L" + fid);
				if(file.exists()){
					BufferedReader input = new BufferedReader(new FileReader(file));
					StringBuilder builder = new StringBuilder();
					String str = "";
					while((str=input.readLine()) != null){
						builder.append(str);
					}
					input.close();
					JSONObject tem = new JSONObject(builder.toString());
					tem.append(Constant.OFFLINE_MESSAGE, json);
					FileWriter output = new FileWriter(file);
					output.write(tem.toString());
					output.close();
				}else {
					JSONObject tem = new JSONObject();
					tem.append(Constant.OFFLINE_MESSAGE, json);
					FileWriter output = new FileWriter(file);
					output.write(tem.toString());
					output.close();
				}
			}catch (IOException e){
				e.printStackTrace();
			}
		}
	}

	private void sendOfflineMessages(){
		File file = new File("messages\\L" + getId());
		if(file.exists() && file.length()!=0){
			try{
				BufferedReader bReader = new BufferedReader(new FileReader(file));
				StringBuilder builder = new StringBuilder();
				String str = "";
				while((str=bReader.readLine()) != null){
					builder.append(str);
				}
				bReader.close();
				JSONObject json = new JSONObject(builder.toString());
				JSONArray array = json.getJSONArray(Constant.OFFLINE_MESSAGE);
				for(int i=0; i<array.length(); ++i){
					Constant.flushWrite(writer, array.getJSONObject(i));
				}
				file.delete();
			}catch (IOException e){
				e.printStackTrace();
			}
		}
	}

	private void sendPreAddLinkMan(){
		File file = new File("addLinkMan\\L" + getId());
		if(file.exists() && file.length()!=0){
			try{
				BufferedReader bReader = new BufferedReader(new FileReader(file));
				StringBuilder builder = new StringBuilder();
				String str = "";
				while((str=bReader.readLine()) != null){
					builder.append(str);
				}
				bReader.close();
				JSONObject json = new JSONObject(builder.toString());
				JSONArray array = json.getJSONArray(Constant.ADD_OFFLINE_LINKMAN);
				for(int i=0; i<array.length(); ++i){
					Constant.flushWrite(writer, array.getJSONObject(i));
				}
				file.delete();
			}catch (IOException e){
				e.printStackTrace();
			}

		}
	}

	private void changePhoto(JSONObject json){
		try{
			byte[] photo = new byte[(int)json.getLong(Constant.PHOTO_SIZE)];
			DataInputStream readPhoto = new DataInputStream(socket.getInputStream());
			readPhoto.read(photo);
			new Thread(){
				public void run(){
					try{
						FileOutputStream writePhoto = new FileOutputStream(new File("photos\\L" + LinkMan.this.getId() + ".jpg"));
						writePhoto.write(photo);
						writePhoto.close();
					}catch (IOException e){
						e.printStackTrace();
					}
				}
			}.start();
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			while (true) {
				JSONObject json = new JSONObject(reader.readLine());
				String code = json.getString(Constant.LABEL_CODE);
				switch (code) {
					case Constant.ADD_LINKMAN:
						addLinkMan(json);
						break;

					case Constant.PERSON_MESSAGE:
						sendPersonMessage(json);
						break;

					case Constant.GROUP_MESSAGE:
						sendGroupMessage(json);
						break;

					case Constant.ADD_LINKMAN_ACCEPT:
						acceptAddLinkMan(json);
						break;

					case Constant.ADD_LINKMAN_REJECT:
						int n = Server.isOnline(json.getString(Constant.LABEL_ID));
						if(n >= 0){
							JSONObject tem = new JSONObject();
							tem.put(Constant.LABEL_CODE, Constant.ADD_LINKMAN_REJECT);
							tem.put(Constant.LABEL_ID, this.getId());
							tem.put(Constant.LABEL_NAME, this.getName());
							Constant.flushWrite(Server.linkMen.get(n).getWriter(), tem);
						}
						break;

					case Constant.CREATE_GROUP:
						createGroup(json);
						break;

					case Constant.ADD_GROUP:
						addGroup(json);
						break;

					case Constant.CHANGE_PHOTO:
						changePhoto(json);
						break;

					case Constant.READY:
						updateMyPhoto();
						updatePhoto();
						sendOfflineMessages();
						sendPreAddLinkMan();
						break;

					case Constant.UPDATE_PHOTO:
						updatePhoto();
						break;
				}
			}
		} catch (Exception e) {
			System.out.println(this.id + " :断开连接");
			for (int i = 0; i < Server.linkMen.size(); ++i) {
				LinkMan lm = Server.linkMen.get(i);
				if (this.id.compareTo(lm.getId()) == 0) {
					Server.linkMen.remove(i);
				}
			}
			Server.showLinkList();
		}
	}
}
