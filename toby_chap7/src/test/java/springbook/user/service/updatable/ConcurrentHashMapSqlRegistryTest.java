package springbook.user.service.updatable;

import springbook.user.sqlservice.updatable.ConcurrentHashMapSqlRegistry;
import springbook.user.sqlservice.updatable.UpdatableSqlRegistry;

public class ConcurrentHashMapSqlRegistryTest extends
		AbstractUpdatableSqlRegistryTest {

	@Override
	protected UpdatableSqlRegistry createUpdatableSqlRegistry() {
		return new ConcurrentHashMapSqlRegistry();
	}

}
