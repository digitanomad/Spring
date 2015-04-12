package springbook.user.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import springbook.user.dao.UserDao;
import springbook.user.domain.Level;
import springbook.user.domain.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/applicationContext.xml")
// 롤백 여부에 대한 기본 설정과 트랜잭션 매니저 빈을 지정하는 데 사용할 수 있다.
// 디폴트 트랜잭션 매니저 아이디는 관례를 따라서 transactionManager로 되어있다.
//@TransactionConfiguration(defaultRollback=false)

public class UserServiceTest {

	@Autowired
	UserService userService;
	
	// 같은 타입의 빈이 두 개 존재하기 때문에 필드 이름을 기준으로 주입될 빈이 결정된다.
	// 자동 프록시 생성기에 의해 트랜잭션 부가기능이 testUserService 빈에 적용됐는지를
	// 확인하는 것이 목적이다.
	@Autowired
	UserService testUserService;

	@Autowired
	UserDao userDao;

	@Autowired
	PlatformTransactionManager transactionManager;

	@Autowired
	MailSender mailSender;

/*	@Autowired
	UserServiceImpl userServiceImpl;*/
	@Autowired
	ApplicationContext context;

	// 테스트 픽스
	List<User> users;

	@Before
	public void setUp() {
		users = Arrays.asList(new User("bumjin", "박범진", "p1", Level.BASIC, UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER - 1, 0, "bumjin@gmail.com"), 
				new User("joytouch", "강명성", "p2", Level.BASIC, UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER, 0, "joytouch@gmail.com"),
				new User("erwins", "신승한", "p3", Level.SILVER, 60, UserServiceImpl.MIN_RECOMMEND_FOR_GOLD - 1, "erwins@gmail.com"),
				new User("madnite1", "이상호", "p4", Level.SILVER, 60, UserServiceImpl.MIN_RECOMMEND_FOR_GOLD, "madnite1@gmail.com"),
				new User("green", "오민규", "p5", Level.GOLD, 100, Integer.MAX_VALUE, "green@gmail.com"));
	}

/*	@Test
	@DirtiesContext
	public void upgradeLevels() {
		userDao.deleteAll();
		
		for (User user : users) {
			userDao.add(user);
		}
		
		MockMailSender mockMailSender = new MockMailSender();
		userServiceImpl.setMailSender(mockMailSender);
		
		userService.upgradeLevels();
		
		checkLevelUpgraded(users.get(0), false);
		checkLevelUpgraded(users.get(1), true);
		checkLevelUpgraded(users.get(2), false);
		checkLevelUpgraded(users.get(3), true);
		checkLevelUpgraded(users.get(4), false);
		
		List<String> request = mockMailSender.getRequests();
		assertThat(request.size(), is(2));
		assertThat(request.get(0), is(users.get(1).getEmail()));
		assertThat(request.get(1), is(users.get(3).getEmail()));
	}*/
	
	@Test
	public void isolateUpgradeLevels() {
		UserServiceImpl userServiceImpl = new UserServiceImpl();
		
		MockUserDao mockUserDao = new MockUserDao(this.users);			
		userServiceImpl.setUserDao(mockUserDao);
			
		MockMailSender mockMailSender = new MockMailSender();
		userServiceImpl.setMailSender(mockMailSender);

		userServiceImpl.upgradeLevels();
			
		List<User> updated = mockUserDao.getUpdated();
		assertThat(updated.size(), is(2));
		checkUserAndLevel(updated.get(0), "joytouch", Level.SILVER);
		checkUserAndLevel(updated.get(1), "madnite1", Level.GOLD);

		List<String> request = mockMailSender.getRequests();
		assertThat(request.size(), is(2));
		assertThat(request.get(0), is(users.get(1).getEmail()));
		assertThat(request.get(1), is(users.get(3).getEmail()));
	}
	
	@Test
	public void add() {
		userDao.deleteAll();

		User userWithLevel = users.get(4);
		User userWithoutLevel = users.get(0);
		userWithoutLevel.setLevel(null);

		userService.add(userWithLevel);
		userService.add(userWithoutLevel);

		User userWithLevelRead = userDao.get(userWithLevel.getId());
		User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());

		assertThat(userWithLevel.getLevel(), is(userWithLevelRead.getLevel()));
		assertThat(userWithoutLevelRead.getLevel(), is(Level.BASIC));
	}

	@Test
	public void upgradeAllOrNothing() throws Exception {
//		TestUserService testUserService = new TestUserService(users.get(3).getId());
//		testUserService.setUserDao(userDao);
//		testUserService.setMailSender(mailSender);
		
		// 팩토리 빈 자체를 가져와야 하므로 빈 이름이 &를 반드시 넣어야 한다.
//		ProxyFactoryBean txProxyFactoryBean = context.getBean("&userService", ProxyFactoryBean.class);
//		txProxyFactoryBean.setTarget(testUserService);
//		UserService txUserService = (UserService) txProxyFactoryBean.getObject();
		
//		TransactionHandler txHanlder = new TransactionHandler();
//		txHanlder.setTarget(testUserService);
//		txHanlder.setTransactionManager(transactionManager);
//		txHanlder.setPattern("upgradeLevels");

//		UserServiceTx txUserService = new UserServiceTx();
//		txUserService.setTransactionManager(transactionManager);
//		txUserService.setUserService(testUserService);
		
//		UserService txUserService = (UserService) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {UserService.class}, txHanlder);

		userDao.deleteAll();

		for (User user : users) {
			userDao.add(user);
		}

		try {
			this.testUserService.upgradeLevels();
			fail("TestUserServiceException expected");
		} catch (TestUserServiceException e) {
		}

		checkLevelUpgraded(users.get(1), false);
	}
	
	@Test(expected=TransientDataAccessResourceException.class)
	public void readOnlyTransactionAttribute() {
		testUserService.getAll();
	}
	
	@Test
	public void transactionSync() {
		DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
		// 트랜잭션 매니저에게 트랜잭션을 요청한다. 기존에 시작된 트랜잭션이 없으니 새로운 트랜잭션을 시작시키고
		// 트랜잭션 정보를 돌려준다. 동시에 만들어진 트랜잭션을 다른 곳에서도 사용할 수 있도록 동기화한다.
		TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);
		
//		userService.deleteAll();
//		
//		userService.add(users.get(0));
//		userService.add(users.get(1));
//		
//		
//		transactionManager.commit(txStatus);
//		assertThat(userDao.getCount(), is(2));
		
		// 롤백 테스트
		try {
			userService.deleteAll();
			userService.add(users.get(0));
			userService.add(users.get(1));
		} finally {
			transactionManager.rollback(txStatus);
		}
	}
	
	@Test
	@Transactional
	@Rollback(false)
	public void transactionSyncTest() {
		userService.deleteAll();
		userService.add(users.get(0));
		userService.add(users.get(1));
	}

	private void checkLevelUpgraded(User user, boolean upgraded) {
		User userUpdate = userDao.get(user.getId());

		if (upgraded) {
			assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
		} else {
			assertThat(userUpdate.getLevel(), is(user.getLevel()));
		}
	}
	
	static class TestUserServiceImpl extends UserServiceImpl {
		private String id = "madnite1";

		protected void upgradeLevel(User user) {
			if (user.getId().equals(this.id)) {
				throw new TestUserServiceException();
			}

			super.upgradeLevel(user);
		}
		
		public List<User> getAll() {
			for (User user : super.getAll()) {
				super.update(user);
			}
			
			return null;
		}

	}

	static class TestUserServiceException extends RuntimeException {

	}

	static class MockMailSender implements MailSender {
		private List<String> requests = new ArrayList<String>();

		public List<String> getRequests() {
			return requests;
		}

		public void send(SimpleMailMessage mailMessage) throws MailException {
			requests.add(mailMessage.getTo()[0]);
		}

		public void send(SimpleMailMessage... mailMessages)
				throws MailException {

		}
	}
	
	static class MockUserDao implements UserDao {
		private List<User> users;
		private List<User> updated = new ArrayList<User>();
		
		public MockUserDao(List<User> users) {
			this.users = users;
		}
		
		public List<User> getUpdated() {
			return this.updated;
		}
		
		public List<User> getAll() {
			return this.users;
		}
		
		public void update(User user) {
			updated.add(user);
		}
		
		public void add(User user) { throw new UnsupportedOperationException(); }
		public User get(String id) { throw new UnsupportedOperationException(); }
		public void deleteAll() { throw new UnsupportedOperationException(); }
		public int getCount() { throw new UnsupportedOperationException(); }
	}
	
	private void checkUserAndLevel(User updated, String expectedId, Level expectedLevel) {
		assertThat(updated.getId(), is(expectedId));
		assertThat(updated.getLevel(), is(expectedLevel));
	}
}
