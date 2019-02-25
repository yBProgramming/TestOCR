package yocr.bcel.com.la.main;

public class ResponseClient {
	private boolean success;
	private String message;
	private Object data;
	public ResponseClient(boolean success, String message, Object data) {
		this.setSuccess(success);
		this.setMessage(message);
		this.setData(data);
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
}
