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

public class RegisterController implements Controllable {

    private static final Logger log = LoggerFactory.getLogger(RegisterController.class);
	private ResponseHandler responseHandler;
	private HttpRequest httpRequest;	
	private String FILE_NAME;
	
	public RegisterController(ResponseHandler responseHandler, HttpRequest httpRequest) {
		this.responseHandler = responseHandler;
		this.httpRequest = httpRequest;
		this.FILE_NAME = "./webapp/user/form.html";
	}

	@Override
	public void getPage() throws IOException {
		byte[] body = Files.readAllBytes(Paths.get(new File(FILE_NAME).toURI()));
    	responseHandler.response200Header(body.length);
    	responseHandler.responseBody(body);
	}

	public void handleRegister() throws IOException {
    	register();
    	responseHandler.response302Header("/index.html");
	}
    
	private void register() throws IOException {
    	User user = new User(httpRequest.getParam("userId"), httpRequest.getParam("password"), 
    			httpRequest.getParam("name"), httpRequest.getParam("email"));
    	DataBase.addUser(user);
    	log.debug("Added user: {}", user.getUserId());
    }

}
