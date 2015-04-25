package springbook.user.sqlservice.updatable;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import springbook.user.sqlservice.exception.SqlNotFoundException;
import springbook.user.sqlservice.exception.SqlUpdateFailureException;

public class EmbeddedDbSqlRegistry implements UpdatableSqlRegistry {
	JdbcTemplate jdbc;
	/*
	 * JdbcTemplate과 트랜잭션을 동기화해주는 트랜잭션 템플릿이다. 멀티스레드 환경에서 공유 가능하다.
	 */
	TransactionTemplate transactionTemplate;

	public void setDataSource(DataSource dataSource) {
		jdbc = new JdbcTemplate(dataSource);
		transactionTemplate = new TransactionTemplate(
				new DataSourceTransactionManager(dataSource));
	}

	@Override
	public void registerSql(String key, String sql) {
		jdbc.update("insert into sqlmap(key_, sql_) values (?,?)", key, sql);
	}

	@Override
	public String findSql(String key) throws SqlNotFoundException {
		try {
			return jdbc.queryForObject("select sql_ from sqlmap where key_=?",
					String.class, key);
		} catch (EmptyResultDataAccessException e) {
			throw new SqlNotFoundException(key + "에 해당하는 SQL을 찾을 수 없습니다.");
		}
	}

	/*
	 * update()는 SQL 실행 결과로 영향을 받은 레코드의 개수를 리턴한다. 이를 이용하면 주어진 키(key)를 가진 SQL이
	 * 존재했는지를 간단히 확인할 수 있다.
	 */
	@Override
	public void updateSql(String key, String sql)
			throws SqlUpdateFailureException {
		int affected = jdbc.update("update sqlmap set sql_=? where key_=?",
				sql, key);
		if (affected == 0) {
			throw new SqlUpdateFailureException(key + "에 해당하는 SQL을 찾을 수 없습니다.");
		}
	}

	@Override
	public void updateSql(final Map<String, String> sqlmap)
			throws SqlUpdateFailureException {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			/*
			 * 트랜잭션 템플릿이 만드는 트랜잭션 경계 안에서 동작할 코드를 콜백 형태로 만들고 TransactionTemplate의
			 * execute() 메소드에 전달한다.
			 */
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				for (Map.Entry<String, String> entry : sqlmap.entrySet()) {
					try {
						updateSql(entry.getKey(), entry.getValue());
					} catch (SqlUpdateFailureException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

}
