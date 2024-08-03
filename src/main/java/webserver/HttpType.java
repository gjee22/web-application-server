package webserver;

public enum HttpType {
	GET,
	POST;
	
	public boolean isPost(HttpType httpType) {
		return httpType == HttpType.POST;
	}
}
