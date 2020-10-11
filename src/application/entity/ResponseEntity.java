package application.entity;

import org.apache.http.entity.ContentType;

public class ResponseEntity<T> {

	private T responsePayload;
	private int statusCode;
	private ContentType contentType;
	private String charset;
	
	public ResponseEntity(T payload, int statusCode, ContentType contentType, String charset) {
		this.responsePayload = payload;
		this.statusCode = statusCode;
		this.contentType = contentType;
		this.charset = charset;
	}
	
	
	public ResponseEntity(T payload, int statusCode, ContentType contentType) {
		this(payload, statusCode, contentType, "UTF-8");
	}


	public T getResponsePayload() {
		return responsePayload;
	}


	public int getStatusCode() {
		return statusCode;
	}


	public ContentType getContentType() {
		return contentType;
	}


	public String getCharset() {
		return charset;
	}


	@Override
	public String toString() {
		return "ResponseEntity [responsePayload=" + responsePayload + ", statusCode=" + statusCode + ", contentType="
				+ contentType + ", charset=" + charset + "]";
	}
	
	
	
	
	
}
