package springbook.user.sqlservice.exception;

public class SqlNotFoundException extends Exception {

	public SqlNotFoundException(String msg) {
		 super(msg);
	}

	public SqlNotFoundException() {
	}

	private static final long serialVersionUID = 1L;
}
