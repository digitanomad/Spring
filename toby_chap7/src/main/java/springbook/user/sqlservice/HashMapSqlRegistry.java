package springbook.user.sqlservice;

import java.util.HashMap;
import java.util.Map;

import springbook.user.sqlservice.exception.SqlNotFoundException;

public class HashMapSqlRegistry implements SqlRegistry {
	private Map<String, String> sqlMap = new HashMap<String, String>();
	
	@Override
	public void registerSql(String key, String sql) {
		sqlMap.put(key, sql);
	}

	@Override
	public String findSql(String key) throws SqlNotFoundException {
		String sql = sqlMap.get(key);
		if (sql == null) {
			throw new SqlNotFoundException();
		} else {
			return sql;
		}
	}

}
