import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/11/18 0018.
 */
public class Server {

	public static ArrayList<LinkMan> linkMen = new ArrayList<>();

	public final static int PERSON_NOT_FOUND = 404;
	public final static int PASSWORD_WRONG = 403;
	public final static int ALREADY_EXIST = 405;
	public final static int CHECK_OK = 200;

	private Connection connectSql = null;
	public static Statement stmt = null;

	private Connection connectAllLinkMan = null;
	public static Statement stmtAllLinkMan = null;

	public static void showLinkList(){
		System.out.println("连接列表");
		System.out.println("-------------------------------");
		for(int o=0; o<Server.linkMen.size(); ++o){
			System.out.println(Server.linkMen.get(o).getId());
		}
		System.out.println("-------------------------------");
	}

	public static int isOnline(String id){
		for(int i=0; i<linkMen.size(); ++i){
			LinkMan lm = linkMen.get(i);
			if(id.compareTo(lm.getId()) == 0){
				return i;
			}
		}
		return -1;
	}

	private int login(JSONObject info){
		String id = info.getString(Constant.LABEL_ID);
		String passwd = info.getString(Constant.LABEL_PASSWD);

		try{
			ResultSet person = Server.stmt.executeQuery("SELECT * FROM LINKMAN WHERE ID LIKE \"" + id + "\";");
			boolean check = person.next();
			if(!check){
				return PERSON_NOT_FOUND;
			}
			String gottenPasswd = person.getString("PASSWORD");
			if(gottenPasswd.compareTo(passwd) != 0){
				return PASSWORD_WRONG;
			}
			for(int i=0; i<Server.linkMen.size(); ++i){
				String cid = Server.linkMen.get(i).getId();
				if(cid.compareTo(info.getString(Constant.LABEL_ID)) == 0){
					JSONObject tem = new JSONObject();
					tem.put(Constant.LABEL_CODE, Constant.LOGIN_ALREADY_EXIST);
					Constant.flushWrite(Server.linkMen.get(i).getWriter(), tem);
					System.out.println("挤下去了");
				}
			}
		}catch (Exception error){
			error.printStackTrace();
		}
		return CHECK_OK;
	}

	private JSONObject getLinkList(JSONObject info){
		JSONObject json = new JSONObject();
		String id = info.getString(Constant.LABEL_ID);
		String str = "";
		try{
			json.put(Constant.LABEL_CODE, Constant.LOGIN_SUCCESS);
			str = "SELECT * FROM LINKMAN WHERE ID LIKE " + id + ";";
			ResultSet rs = Server.stmt.executeQuery(str);
			json.put(Constant.LABEL_NAME, rs.getString("NAME"));
			str = "SELECT * FROM L" + id + ";";
			rs = stmtAllLinkMan.executeQuery(str);
			int count = 0;
			while(rs.next()){
				count += 1;
			}
			json.put(Constant.LABEL_LINKMAN_COUNT, count);

			str = "SELECT * FROM L" + id + ";";
			rs = Server.stmtAllLinkMan.executeQuery(str);
			JSONObject tem = null;
			while(rs.next()){
				tem = new JSONObject();
				tem.put(Constant.LABEL_ID, rs.getString("ID"));
				tem.put(Constant.LABEL_NAME, rs.getString("NAME"));
				tem.put(Constant.LABEL_TYPE, rs.getString("TYPE"));
				json.append(Constant.LABEL_LINKLIST, tem);
			}
		}catch (SQLException e){
			e.printStackTrace();
		}
		return json;
	}

	private int register(JSONObject info){
		int count = 0;
		String name = info.getString(Constant.LABEL_NAME);
		String passwd = info.getString(Constant.LABEL_PASSWD);
		try{
			String str = "SELECT * FROM CONSTANT WHERE NAME LIKE \"PERSON_COUNT\";";
			ResultSet gCount = Server.stmt.executeQuery(str);
			count = gCount.getInt("COUNT");
			str = "UPDATE CONSTANT SET COUNT = " + (count+1) + " WHERE NAME LIKE \"PERSON_COUNT\";";
			Server.stmt.executeUpdate(str);

			String id = new Integer(count).toString();
			String path = "";
			str = "INSERT INTO LINKMAN VALUES(\"" + id + "\", \"" + name + "\", \"" + passwd + "\");";
			Server.stmt.executeUpdate(str);

			str = "CREATE TABLE L" + id + "(ID TEXT PRIMARY KEY NOT NULL, NAME TEXT NOT NULL, TYPE TEXT NOT NULL);";
			Server.stmtAllLinkMan.executeUpdate(str);
		}catch (Exception error){
			error.printStackTrace();
			return 0;
		}
		return count;
	}

	public static void main(String[] args){

		Server check = new Server();

		try{
			Class.forName("org.sqlite.JDBC");
			check.connectSql = DriverManager.getConnection("jdbc:sqlite:record.db");
			Server.stmt = check.connectSql.createStatement();

			check.connectAllLinkMan = DriverManager.getConnection("jdbc:sqlite:allLinkMan.db");
			Server.stmtAllLinkMan = check.connectAllLinkMan.createStatement();

			ServerSocket server = new ServerSocket(Constant.port);
			while(true){
				System.out.println("开始监听");
				Socket socket = server.accept();
				System.out.println(socket.getRemoteSocketAddress());

				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter writer = new PrintWriter(socket.getOutputStream());

				JSONObject json = new JSONObject(reader.readLine());


				switch (json.getString(Constant.LABEL_CODE)){
					case Constant.LOGIN:
						int result = check.login(json);
						JSONObject obj2;
						if(result== Server.CHECK_OK) {
							obj2 = check.getLinkList(json);
							String kid = json.getString(Constant.LABEL_ID);
							String name = obj2.getString(Constant.LABEL_NAME);
							LinkMan linkMan = new LinkMan(socket, reader, writer, kid, name);
							Server.linkMen.add(linkMan);
							new Thread(linkMan).start();
							System.out.println(kid + " :成功登录");
							showLinkList();
						}else if(result == Server.ALREADY_EXIST){
							obj2 = new JSONObject();
							obj2.put(Constant.LABEL_CODE, Constant.LOGIN_ALREADY_EXIST);
						}else {
							obj2 = new JSONObject();
							obj2.put(Constant.LABEL_CODE, Constant.LOGIN_FAIL);
						}
						Constant.flushWrite(writer, obj2);
						break;

					case Constant.REGISTER:
						int id = check.register(json);
						JSONObject obj = new JSONObject();
						if(id == 0){
							obj.put(Constant.LABEL_CODE, Constant.REGISTER_FAIL);
							Constant.flushWrite(writer, obj);
						}else {
							obj.put(Constant.LABEL_CODE, Constant.REGISTER_SUCCESS);
							obj.put(Constant.LABEL_ID, new Integer(id).toString());
							Constant.flushWrite(writer, obj);
						}
						break;
				}
			}
		}catch (Exception error){
			error.printStackTrace();
		}
	}
}
