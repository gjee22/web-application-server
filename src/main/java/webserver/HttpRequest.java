package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;
import util.HttpRequestUtils.Pair;
import util.IOUtils;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);
	private BufferedReader br;
	private HttpType reqType;
	private String reqPath;
	private Map<String, String> headers;
	private Map<String, String> params;
	
	public HttpRequest(InputStream in) {
		this.br = new BufferedReader(new InputStreamReader(in));
		this.headers = new HashMap<String, String>();
		this.params = new HashMap<String, String>();
		String cur = null;
		try {
			cur = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (cur != null) {
			String[] splitCur = cur.split(" ");
			if (splitCur[0].equals("GET")) this.reqType = HttpType.GET;
			if (splitCur[0].equals("POST")) this.reqType = HttpType.POST;
			this.reqPath = splitCur[1];
			log.debug("Request type: {}, Request Path: {}", this.reqType, this.reqPath);
		}
	}
	
	public HttpType getReqType() {
		return this.reqType;
	}
	
	public String getReqPath() {
		return this.reqPath;
	}
	
	public Map<String, String> getHeaders() {
		return headers;
	}
	
	public String getHeader(String key) {
		return headers.get(key);
	}
	
	public String getParam(String key) {
		return params.get(key);
	}
	
	public void parseRequest() throws IOException {
		String cur;
		while (!(cur = br.readLine()).isEmpty()) {
			log.debug("Header: {}", cur);
			Pair curHeader = HttpRequestUtils.parseHeader(cur);
			headers.put(curHeader.getKey(), curHeader.getValue());
		}
		
		if (reqType == HttpType.POST) {
			String reqContent = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length").trim()));
	    	Map<String, String> userInfo = HttpRequestUtils.parseQueryString(reqContent);
	    	for (String keyString : userInfo.keySet()) {
	    		params.put(keyString, userInfo.get(keyString));
				log.debug("Param: {} {}", keyString, userInfo.get(keyString));
	    	}
		}
	}
}
