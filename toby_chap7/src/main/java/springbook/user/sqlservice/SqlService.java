package springbook.user.sqlservice;

import springbook.user.sqlservice.exception.SqlRetrievalFailureException;

public interface SqlService {

	String getSql(String key) throws SqlRetrievalFailureException;
}
