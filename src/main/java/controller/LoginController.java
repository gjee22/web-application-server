package controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import webserver.HttpRequest;
import webserver.ResponseHandler;

public class LoginController implements Controllable {
	
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
	private ResponseHandler responseHandler;	
	private HttpRequest httpRequest;
	private String FILE_NAME;
	
	public LoginController(ResponseHandler responseHandler, HttpRequest httpRequest) {
		this.responseHandler = responseHandler;
		this.httpRequest = httpRequest;
		this.FILE_NAME = "./webapp/user/login.html";
	}

	@Override
	public void getPage() throws IOException {
		byte[] body = Files.readAllBytes(Paths.get(new File(FILE_NAME).toURI()));
    	responseHandler.response200Header(body.length);
    	responseHandler.responseBody(body);
	}

	public void handleLogin() throws IOException {
    	if (checkLogin()) {
			handleLoginSuccess();
		}
		else {
			handleLoginFailure();
		}
	}		
	
	public void getLoginFailure() throws IOException {
		String path = "./webapp/user/login_failed.html";
		byte[] body = Files.readAllBytes(Paths.get(new File(path).toURI()));
    	responseHandler.response200Header(body.length);
    	responseHandler.responseBody(body);
	}
	
    private void handleLoginSuccess() throws IOException {
    	responseHandler.responseLoginSuccessHeader(0);
    	responseHandler.responseBody(new byte[0]);
    }
    
    private void handleLoginFailure() throws IOException {
    	responseHandler.response302Header("/user/login_failed.html");
    	responseHandler.responseBody(new byte[0]);
    }
    
    private boolean checkLogin() {
    	User user = DataBase.findUserById(httpRequest.getParam("userId"));
    	return (user == null || !user.getPassword().equals(httpRequest.getParam("password"))) ? false : true;
    }

}
