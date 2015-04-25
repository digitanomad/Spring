package springbook.user.sqlservice.updatable;

import java.util.Map;

import springbook.user.sqlservice.SqlRegistry;
import springbook.user.sqlservice.exception.SqlUpdateFailureException;

public interface UpdatableSqlRegistry extends SqlRegistry {

	public void updateSql(String key, String sql) throws SqlUpdateFailureException;
	public void updateSql(Map<String, String> sqlmap) throws SqlUpdateFailureException;
}
