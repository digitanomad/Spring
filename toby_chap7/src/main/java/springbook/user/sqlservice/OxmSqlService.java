package springbook.user.sqlservice;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Unmarshaller;

import springbook.user.dao.UserDao;
import springbook.user.sqlservice.exception.SqlRetrievalFailureException;
import springbook.user.sqlservice.jaxb.SqlType;
import springbook.user.sqlservice.jaxb.Sqlmap;

public class OxmSqlService implements SqlService {
	// final이므로 변경 불가능하다. OxmSqlService와 OxmSqlReader는 강하게 결합되서 
	// 하나의 빈으로 등록되고 한 번에 설정할 수 있다.
	private final OxmSqlReader oxmSqlReader = new OxmSqlReader();
	private SqlRegistry sqlRegistry = new HashMapSqlRegistry();
	
	// SqlService의 실제 구현 부분을 위임할 대상인 BaseSqlService를 인스턴스 변수로 정의해둔다.
	private final BaseSqlService baseSqlService = new BaseSqlService();
	
	public void setUnmarshaller(Unmarshaller unmarshaller) {
		this.oxmSqlReader.setUnmarshaller(unmarshaller);
	}
	
	public void setSqlRegistry(SqlRegistry sqlRegistry) {
		this.sqlRegistry = sqlRegistry;
	}
	
	// OxmSqlService의 공개된 프로퍼티를 통해 DI 받은 것을 그대로 멤버 클래스의 오브젝트에 전달한다.
	// 이 Setter들은 단일 빈 설정구조를 위한 창구 역할을 할 뿐이다.
	public void setSqlmap(Resource sqlmap) {
		this.oxmSqlReader.setSqlmap(sqlmap);
	}

	@PostConstruct
	public void loadSql() {
		this.baseSqlService.setSqlReader(this.oxmSqlReader);
		this.baseSqlService.setSqlRegistry(this.sqlRegistry);

		this.baseSqlService.loadSql();
	}

	@Override
	public String getSql(String key) throws SqlRetrievalFailureException {
		return this.baseSqlService.getSql(key);
	}
	
	private class OxmSqlReader implements SqlReader {
		private Unmarshaller unmarshaller;
		private Resource sqlmap = new ClassPathResource("sqlmap.xml", UserDao.class);
		
		public void setUnmarshaller(Unmarshaller unmarshaller) {
			this.unmarshaller = unmarshaller;
		}

		public void setSqlmap(Resource sqlmap) {
			this.sqlmap = sqlmap;
		}
		
		@Override
		public void read(SqlRegistry sqlRegistry) {
			try {
				Source source = new StreamSource(this.sqlmap.getInputStream());
				// OxmSqlService를 통해 전달받은 OXM 인터페이스 구현 오브젝트를 가지고 언마셜링 작업 수행
				Sqlmap sqlmap = (Sqlmap) this.unmarshaller.unmarshal(source);
				for (SqlType sql : sqlmap.getSql()) {
					sqlRegistry.registerSql(sql.getKey(), sql.getValue());
				}
			} catch (IOException e) {
				throw new IllegalArgumentException(this.sqlmap.getFilename() + "을 가져올 수 없습니다.", e);
			}
			
		}
	}

}
