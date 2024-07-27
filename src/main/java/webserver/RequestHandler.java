package webserver;

import java.awt.desktop.UserSessionEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.Map;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;



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
        	String dataString = br.readLine().split(" ")[1];
            log.info("Received data: {}", dataString);
            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = getContent(dataString);
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private byte[] getContent(String dataString) throws IOException {
    	return switch (dataString) {		
	    	case "/index.html" -> { yield Files.readAllBytes(Paths.get(new File("./webapp/index.html").toURI())); }
	    	case "/user/form.html" -> { yield Files.readAllBytes(Paths.get(new File("./webapp/user/form.html").toURI())); }
			default -> { 
				if (dataString.startsWith("/user/create?")) yield register(dataString.split("\\?")[1]); 
				yield "Hello World".getBytes(); 
			}
		};
    }
    
    private byte[] register(String dataString) throws IOException {
    	Map<String, String> userInfo = HttpRequestUtils.parseQueryString(dataString);
    	User user = new User(userInfo.get("userId"), userInfo.get("password"), userInfo.get("name"), userInfo.get("email"));
    	DataBase.addUser(user);
    	for (User u : DataBase.findAll()) {
    		System.out.println(u);
    	}
    	return Files.readAllBytes(Paths.get(new File("./webapp/user/form.html").toURI()));
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
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
