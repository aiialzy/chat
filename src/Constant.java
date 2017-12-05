import org.json.JSONObject;

import java.awt.*;
import java.io.PrintWriter;

/**
* Created by Administrator on 2017/11/18 0018.
*/
public class Constant {
	public final static String ip = "127.0.0.1";
	public final static int port = 2017;

	public final static String LOGIN = "1000";
	public final static String LOGIN_SUCCESS = "3003";
	public final static String LOGIN_FAIL = "3002";
	public final static String LOGIN_ALREADY_EXIST = "3004";

	public final static String REGISTER = "1001";
	public final static String REGISTER_SUCCESS = "3001";
	public final static String REGISTER_FAIL = "3000";

	public final static String PERSON = "person";
	public final static String PERSON_MESSAGE = "2000";
	public final static String GROUP = "group";
	public final static String GROUP_MESSAGE = "2001";
	public final static String OFFLINE_MESSAGE = "2002";

	public final static String ADD_LINKMAN = "4000";
	public final static String ADD_LINKMAN_SUCCESS = "4001";
	public final static String ADD_LINKMAN_FAIL = "4002";
	public final static String ADD_LINKMAN_ED = "4003";
	public final static String ADD_LINKMAN_REJECT = "4004";
	public final static String ADD_LINKMAN_ACCEPT = "4005";
	public final static String ADD_LINKMAN_ALREADY_EXIST = "4006";
	public final static String ADD_OFFLINE_LINKMAN = "4007";

	public final static String LABEL_CODE = "code";
	public final static String LABEL_ID = "id";
	public final static String LABEL_NAME = "name";
	public final static String LABEL_PASSWD = "passwd";
	public final static String LABEL_LINKLIST = "linkList";
	public final static String LABEL_CONTENT = "content";
	public final static String LABEL_SENDER = "sender";
	public final static String LABEL_GETTER = "getter";
	public final static String LABEL_TYPE = "type";
	public final static String LABEL_LINKMAN_COUNT = "linkmanCount";
	public final static String LABEL_PHOTO_EXIST = "photoExist";

	public final static String CREATE_GROUP = "5000";
	public final static String CREATE_GROUP_SUCCESS = "5001";
	public final static String CREATE_GROUP_FAIL = "5002";
	public final static String ADD_GROUP = "5003";
	public final static String ADD_GROUP_SUCCESS = "5004";
	public final static String ADD_GROUP_FAIL = "5005";

	public final static String CHANGE_PHOTO = "6000";
	public final static String PHOTO_SIZE = "6001";
	public final static String PHOTO = "6002";
	public final static String END = "6003";
	public final static String READY = "6004";
	public final static String MYPHOTO = "6005";
	public final static String UPDATE_PHOTO = "6006";

	public final static int SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
	public final static int SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;


	public static void flushWrite(PrintWriter writer, JSONObject json){
		writer.println(json);
		writer.flush();
	}
}