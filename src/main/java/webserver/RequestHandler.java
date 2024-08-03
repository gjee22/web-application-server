package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import controller.Controllable;
import controller.LoginController;
import controller.MainPageController;
import controller.RegisterController;
import controller.UserListController;


public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private ResponseHandler responseHandler;
    private HttpRequest httpRequest;
    Map<String, Controllable> controllers = new HashMap<String, Controllable>();
    private Socket connection;
    
    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
        	httpRequest = new HttpRequest(in);
        	httpRequest.parseRequest();
        	
        	setup(out, httpRequest);
        	getPage(httpRequest.getReqPath());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

	private void setup(OutputStream out, HttpRequest httpRequest) {
		this.responseHandler = new ResponseHandler(new DataOutputStream(out), log);
		controllers.put("mainPage", new MainPageController(this.responseHandler, httpRequest));
		controllers.put("login", new LoginController(this.responseHandler, httpRequest));
		controllers.put("register", new RegisterController(this.responseHandler, httpRequest));
		controllers.put("userList", new UserListController(this.responseHandler, httpRequest));
	}

	private void getPage(String reqPath) throws IOException {
		switch (reqPath) {
			case "/index.html" -> controllers.get("mainPage").getPage();
			case "/user/form.html" -> controllers.get("login").getPage();
			case "/user/login.html" -> controllers.get("register").getPage();
			case "/user/login_failed.html" -> ((LoginController) controllers.get("login")).getLoginFailure();
			case "/user/list" -> controllers.get("userList").getPage();
			default -> {
				if (reqPath.startsWith("/user/create")) ((RegisterController) controllers.get("register")).handleRegister();
				if (reqPath.startsWith("/user/login")) ((LoginController) controllers.get("login")).handleLogin();
				if (reqPath.endsWith(".css")) applyCSS(reqPath);
			}
		}
	}
    
    private void applyCSS(String reqPath) throws IOException {
    	byte[] body = Files.readAllBytes(Paths.get(new File("./webapp/" + reqPath).toURI()));
    	responseHandler.responseCSSHeader(body.length);
    	responseHandler.responseBody(body);
    }

}
