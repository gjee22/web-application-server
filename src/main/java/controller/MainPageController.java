package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webserver.HttpRequest;
import webserver.ResponseHandler;

public class MainPageController implements Controllable {
	
    private static final Logger log = LoggerFactory.getLogger(MainPageController.class);
	private ResponseHandler responseHandler;
	private HttpRequest httpRequest;
	private String FILE_NAME;
	
	public MainPageController(ResponseHandler responseHandler, HttpRequest httpRequest) {
		this.responseHandler = responseHandler;
		this.httpRequest = httpRequest;
		this.FILE_NAME = "./webapp/index.html";
	}

	@Override
	public void getPage() throws IOException {
		byte[] body = Files.readAllBytes(Paths.get(new File(FILE_NAME).toURI()));
    	responseHandler.response200Header(body.length);
    	responseHandler.responseBody(body);
	}

}
