package controller;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import webserver.HttpRequest;
import webserver.RequestHandler;
import webserver.ResponseHandler;

public class UserListController implements Controllable {
	
    private static final Logger log = LoggerFactory.getLogger(UserListController.class);
	private ResponseHandler responseHandler;
	private HttpRequest httpRequest;
	private String FILE_NAME;
	
	public UserListController(ResponseHandler responseHandler, HttpRequest httpRequest) {
		this.responseHandler = responseHandler;
		this.httpRequest = httpRequest;
		this.FILE_NAME = "./webapp/user/list.html";
	}

	@Override
	public void getPage() throws IOException {
		if (checkLoginCookie()) {
			byte[] body = getAllUsers();
			responseHandler.response200Header(body.length);
	    	responseHandler.responseBody(body);
		} else {
			responseHandler.response302Header("/user/login.html");
	    	responseHandler.responseBody(new byte[0]);
		}
	}
	
	private boolean checkLoginCookie() throws IOException {
    	Map<String, String> cookies = null;
    	if (httpRequest.getHeaders().containsKey("Cookie")) {
        	cookies = HttpRequestUtils.parseCookies(httpRequest.getHeader("Cookie"));
    	}
    	boolean loggedIn = Boolean.parseBoolean(cookies.get("logined"));
    	return loggedIn;
    }
	
	private byte[] getAllUsers() throws IOException {
		BufferedReader fileReader = new BufferedReader(new FileReader("./webapp/user/list.html")); 
		StringBuilder fileContent = new StringBuilder();
		String cur = null;
		String checkLocString = "<th scope=\"row\">2</th> <td>slipp</td> <td>슬립</td> "
				+ "<td>slipp@sample.net</td><td><a href=\"#\" class=\"btn btn-success\" "
				+ "role=\"button\">수정</a></td>";
		do { fileContent.append(cur = fileReader.readLine()); }
		while ((!cur.contains(checkLocString))); 
		
		addAllUsersToList(fileReader, fileContent);
		
		while ((cur = fileReader.readLine()) != null) fileContent.append(cur);
		
		return fileContent.toString().getBytes(Charset.forName("UTF-8"));
	}

	private void addAllUsersToList(BufferedReader fileReader, StringBuilder fileContent)
			throws IOException, UnsupportedEncodingException {
		Object[] users = DataBase.findAll().toArray();
		for (int i = 0; i < users.length; i++) {
			User user = (User) users[i];
			log.debug("User {} in the list", user.getUserId());
			fileContent.append("<tr>\r\n");
			fileContent.append("                    <th scope=\"row\">" + (i + 3) + "</th> ");
			fileContent.append("<td>" + URLDecoder.decode(user.getUserId(), "UTF-8") + "</td> ");
			fileContent.append("<td>" + URLDecoder.decode(user.getName(), "UTF-8") + "</td> ");
			fileContent.append("<td>" + URLDecoder.decode(user.getEmail(), "UTF-8") + "</td> ");
			fileContent.append("<td><a href=\"#\" class=\"btn btn-success\" role=\"button\">수정</a></td>");
		}
	}
}
