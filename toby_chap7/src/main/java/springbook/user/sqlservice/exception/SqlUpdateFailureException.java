package springbook.user.sqlservice.exception;

public class SqlUpdateFailureException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public SqlUpdateFailureException(String msg) {
		super(msg);
	}
}
