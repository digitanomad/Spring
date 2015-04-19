package springbook.user.sqlservice;

import springbook.user.sqlservice.exception.SqlNotFoundException;

public interface SqlRegistry {
	void registerSql(String key, String sql);
	
	String findSql(String key) throws SqlNotFoundException;
}
