package webserver;

import java.io.DataOutputStream;
import java.io.IOException;

import org.slf4j.Logger;


public class ResponseHandler {
	
	private DataOutputStream dos;
	private Logger log;
	
	public ResponseHandler(DataOutputStream dos, Logger log) {
		this.dos = dos;
		this.log = log;
	}
	
	public void response200Header(int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
	
	public void response302Header(String loc) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Location: " + loc + "\r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes(
            		"Content-Length: " + 0 + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
	public void responseLoginSuccessHeader(int lengthOfBodyContent) {
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
    
	public void responseCSSHeader(int lengthOfBodyContent) {
    	try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

	public void responseBody(byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
