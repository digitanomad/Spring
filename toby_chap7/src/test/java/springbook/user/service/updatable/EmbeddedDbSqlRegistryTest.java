package springbook.user.service.updatable;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import springbook.user.sqlservice.exception.SqlNotFoundException;
import springbook.user.sqlservice.exception.SqlUpdateFailureException;
import springbook.user.sqlservice.updatable.EmbeddedDbSqlRegistry;
import springbook.user.sqlservice.updatable.UpdatableSqlRegistry;

public class EmbeddedDbSqlRegistryTest extends AbstractUpdatableSqlRegistryTest {
	EmbeddedDatabase db;
	
	@Override
	protected UpdatableSqlRegistry createUpdatableSqlRegistry() {
		db = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL)
				.addScript("classpath:springbook/user/sqlservice/updatable/sqlRegistrySchema.sql")
				.build();
		
		EmbeddedDbSqlRegistry embeddedDbSqlRegistry = new EmbeddedDbSqlRegistry();
		embeddedDbSqlRegistry.setDataSource(db);
		
		return embeddedDbSqlRegistry;
	}
	
	@After
	public void tearDown() {
		db.shutdown();
	}
	
	@Test
	public void transactionUpdate() throws SqlNotFoundException {
		/*
		 * 초기 상태를 확인한다. 이미 슈퍼클래스의 다른 테스트 메소드에서 확인하긴 했지만
		 * 트랜잭션 콜백 후의 결과와 비교돼서 이 테스트의 목적인 롤백 후의 상태는
		 * 처음과 동일하다는 것을 비교해서 보여주려고 넣었다.
		 */
		checkFindResult("SQL1", "SQL2", "SQL3");
		
		Map<String, String> sqlmap = new HashMap<String, String>();
		sqlmap.put("KEY1", "Modified1");
		/*
		 * 두 번째 SQL의 키를 존재하지 않는 것으로 지정한다.
		 * 이 때문에 테스트는 실패할 것이고, 그 때 과연 롤백이 일어나는지 확인한다.
		 */
		sqlmap.put("KEY9999!@#$", "Modified9999");
		
		try {
			sqlRegistry.updateSql(sqlmap);
			/*
			 * 예외가 발생해서 catch 블록으로 넘어가지 않으면 뭔가 잘못된 것이다.
			 * 그 때는 테스트를 강제로 실패하게 만들고 기대와 다르게 동작한 원인을 찾도록 해야한다.
			 */
			fail();
		} catch (SqlUpdateFailureException e) {}
		
		/*
		 * 첫 번째 SQL은 정상적으로 수정했지만 트랜잭션이 롤백되기 때문에 다시 변경 이전 상태로 돌아와야 한다.
		 * 트랜잭션이 적용되지 않는다면 변경된 채로 남아서 테스트는 실패할 것이다.
		 */
		checkFindResult("SQL1", "SQL2", "SQL3");
	}

}
