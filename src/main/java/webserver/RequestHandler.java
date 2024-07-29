package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CaseFormat;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.HttpRequestUtils.Pair;
import util.IOUtils;



public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private boolean login = false;
    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다        	
        	BufferedReader br = new BufferedReader(new InputStreamReader(in));
        	String[] req = br.readLine().split(" ");
        	String reqType = req[0];
        	String reqPath = req[1];
        	System.out.println(login);
        	
        	switch (reqPath) {
        		case "/index.html" -> getIndex(new DataOutputStream(out));
        		case "/user/form.html" -> getRegister(new DataOutputStream(out));
        		case "/login_failed.html" -> getLoginFailurePage(new DataOutputStream(out));
        		case "/user/login.html" -> getLoginPage(new DataOutputStream(out));
        		case "/user/list" -> handleUserListRequest(new DataOutputStream(out), br);
        		default -> {
        			if (reqPath.startsWith("/user/create")) handleRegister(new DataOutputStream(out), br);
        			if (reqPath.startsWith("/user/login")) handleLogin(new DataOutputStream(out), br, reqType);
        		}
        	}
            log.info("Request type: {}, Request path: {}", req[0], req[1]);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    
    private void getIndex(DataOutputStream dos) throws IOException {
    	byte[] body = Files.readAllBytes(Paths.get(new File("./webapp/index.html").toURI()));
    	response200Header(dos, body.length);
    	responseBody(dos, body);
    }
    
    private void getRegister(DataOutputStream dos) throws IOException {
    	handleGetRequest(dos, "./webapp/user/form.html");
    }
    
    private void getLoginPage(DataOutputStream dos) throws IOException {
    	handleGetRequest(dos, "./webapp/user/login.html");
    }
    
    private void handleUserListRequest(DataOutputStream dos, BufferedReader br) throws IOException {
    	// 쿠키가 false를 리턴해야 할 때 안됨
    	if (checkLoginCookie(br)) {
        	byte[] body = Files.readAllBytes(Paths.get(new File("./webapp/user/list.html").toURI()));
    		response200Header(dos, body.length);
    		responseBody(dos, body);
    	} else {
    		response302Header(dos, 0, "http://localhost:8080/user/login.html");
    		responseBody(dos, new byte[0]);
    	}
    }
    
    private boolean checkLoginCookie(BufferedReader br) throws IOException {
    	String cur = "";
    	while (!(cur = br.readLine()).startsWith("Cookie"));
    	Pair headerPair = HttpRequestUtils.parseHeader(cur);
		String StringBool = headerPair.getValue().split("[=; ]")[1];
		boolean logined = Boolean.parseBoolean(StringBool);
    	return logined;
    }
    
    private void handleRegister(DataOutputStream dos, BufferedReader br) throws IOException {
    	String dataString = getUserInfo(br);
    	register(dataString);
    	response302Header(dos, 0, "http://localhost:8080/index.html");
    }
    
    private void handleLogin(DataOutputStream dos, BufferedReader br, String reqType) throws IOException {
    	if (reqType.equals("GET")) {
    		handleGetRequest(dos, "./webapp/user/login.html");
    	} 
    	else if (reqType.equals("POST")) {
    		String dataString = getUserInfo(br);
    		if (checkLogin(dataString)) {
    			handleLoginSuccess(dos);
    		}
    		else {
    			handleLoginFailure(dos);
    		}
    	}
    }
    
    private void getLoginFailurePage(DataOutputStream dos) throws IOException {
    	byte[] body = Files.readAllBytes(Paths.get(new File("./webapp/user/login_failed.html").toURI()));
    	response200Header(dos, body.length);
    	responseBody(dos, body);
    }
    
    private String getUserInfo(BufferedReader br) throws IOException {
		String cur = "";
		int contentLen = 0;
		while (!(cur = br.readLine()).isEmpty()) {
		    if (cur.startsWith("Content-Length:")) {
		        contentLen = Integer.parseInt(HttpRequestUtils.parseHeader(cur).getValue());
		    }
		}
		String dataString = IOUtils.readData(br, contentLen);
		return dataString;
	}
    
    private void handleLoginSuccess(DataOutputStream dos) throws IOException {
    	login = true;
    	response302Header(dos, 0, "http://localhost:8080/index.html");
    	responseBody(dos, new byte[0]);
    }
    
    private void handleLoginFailure(DataOutputStream dos) throws IOException {
    	response302Header(dos, 0, "http://localhost:8080/login_failed.html");
    	responseBody(dos, new byte[0]);
    }
    
    private void handleGetRequest(DataOutputStream dos, String file) throws IOException {
    	byte[] body = Files.readAllBytes(Paths.get(new File(file).toURI()));
    	response200Header(dos, body.length);
    	responseBody(dos, body);
    }
    
    private boolean checkLogin(String dataString) {
    	Map<String, String> map = HttpRequestUtils.parseQueryString(dataString);
    	User user = DataBase.findUserById(map.get("userId"));
    	return (user == null || !user.getPassword().equals(map.get("password"))) ? false : true;
    }
    
    private void register(String dataString) throws IOException {
    	Map<String, String> userInfo = HttpRequestUtils.parseQueryString(dataString);
    	User user = new User(userInfo.get("userId"), userInfo.get("password"), userInfo.get("name"), userInfo.get("email"));
    	DataBase.addUser(user);
    	for (User u : DataBase.findAll()) {
    		System.out.println(u);
    	}
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("Set-Cookie: logined=" + login + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response302Header(DataOutputStream dos, int lengthOfBodyContent, String loc) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Location: " + loc + "\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("Set-Cookie: logined=" + login + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
