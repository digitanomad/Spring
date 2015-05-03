package springbook.user.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.mail.MailSender;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import springbook.user.AppContext;
import springbook.user.dao.UserDao;
import springbook.user.domain.Level;
import springbook.user.domain.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes={AppContext.class})
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
	
	/*
	 * 등록된 빈 내역을 조회하는 테스트 메소드
	 */
	
	@Autowired
	DefaultListableBeanFactory bf;
	
	@Test
	public void beans() {
		for (String n : bf.getBeanDefinitionNames()) {
			System.out.println(n + "\t" + bf.getBean(n).getClass().getName());
		}
	}
	
	
	/*
	 * 테스트 클래스
	 */
	
	public static class TestUserServiceImpl extends UserServiceImpl {
		private String id = "madnite1";
		
		public void setUserDao(UserDao userDao) {
			this.userDao = userDao;
		}

		public void setMailSender(MailSender mailSender) {
			this.mailSender = mailSender;
		}


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
		private static final long serialVersionUID = 1L;

	}
}
