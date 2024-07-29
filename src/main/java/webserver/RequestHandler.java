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
import java.nio.file.attribute.DosFileAttributes;
import java.text.FieldPosition;
import java.util.Map;

import org.hamcrest.core.AllOf;
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
        	DataOutputStream dos = new DataOutputStream(out);
        	switch (reqPath) {
        		case "/index.html", "/user/form.html", "/user/login_failed.html", "/user/login.html" 
        				-> handleGetRequest(dos, reqPath);
        		case "/user/list" -> handleUserListRequest(new DataOutputStream(out), br);
        		default -> {
        			if (reqPath.startsWith("/user/create")) handleRegister(dos, br);
        			if (reqPath.startsWith("/user/login")) handleLogin(dos, br, reqType);
        			if (reqPath.endsWith(".css")) applyCSS(dos, reqPath);
        		}
        	}
            log.info("Request type: {}, Request path: {}", req[0], req[1]);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void handleGetRequest(DataOutputStream dos, String file) throws IOException {
    	byte[] body = Files.readAllBytes(Paths.get(new File("./webapp" + file).toURI()));
    	responseHttpHeader(dos, "200", body.length, null);
    	responseBody(dos, body);
    }
    
    private void applyCSS(DataOutputStream dos, String reqPath) throws IOException {
    	byte[] body = Files.readAllBytes(Paths.get(new File("./webapp/" + reqPath).toURI()));
    	responseCSSHeader(dos, body.length);
    	responseBody(dos, body);
    }
    
    private void handleUserListRequest(DataOutputStream dos, BufferedReader br) throws IOException {
    	if (checkLoginCookie(br)) {
    		// Get all users and append it with html
        	handleGetRequest(dos, "/user/list.html");
    	} else {
    		responseHttpHeader(dos, "302", 0, "/user/login.html");
    		responseBody(dos, new byte[0]);
    	}
    }
    
    private boolean checkLoginCookie(BufferedReader br) throws IOException {
    	String cur = "";
    	while (!(cur = br.readLine()).isEmpty() && !cur.startsWith("Cookie")); 
    	if (cur.isEmpty() || cur == null) return false;
    	Map<String, String> cookies = HttpRequestUtils.parseCookies(cur.split(":")[1].trim());
    	boolean loggedIn = Boolean.parseBoolean(cookies.get("logined"));
    	return loggedIn;
    }
    
    private void handleRegister(DataOutputStream dos, BufferedReader br) throws IOException {
    	String dataString = getUserInfo(br);
    	register(dataString);
    	responseHttpHeader(dos, "302", 0, "/index.html");
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
    	responseLoginSuccessHeader(dos, 0);
    	responseBody(dos, new byte[0]);
    }
    
    private void handleLoginFailure(DataOutputStream dos) throws IOException {
    	responseHttpHeader(dos, "302", 0, "/user/login_failed.html");
    	responseBody(dos, new byte[0]);
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

    private void responseHttpHeader(DataOutputStream dos, String resType, int lengthOfBodyContent, String loc) {
        try {
        	if (resType.equals("302")) {
                dos.writeBytes("HTTP/1.1 302 OK \r\n");
                dos.writeBytes("Location: " + loc + "\r\n");
                
        	} else {
                dos.writeBytes("HTTP/1.1 200 OK \r\n");
        	}
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void responseLoginSuccessHeader(DataOutputStream dos, int lengthOfBodyContent) {
    	try {
    		dos.writeBytes("HTTP/1.1 302 OK \r\n");
    	    dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
    	    dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
    	    dos.writeBytes("Set-Cookie: logined=true\r\n");
    	    dos.writeBytes("Location: /index.html\r\n");
    	    dos.writeBytes("\r\n");
    	    log.debug("Entered login success header");
		} catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void responseCSSHeader(DataOutputStream dos, int lengthOfBodyContent) {
    	try {
    		log.debug("CSS is applied");
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
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
