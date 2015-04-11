package springbook.learningtest.junit;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.CoreMatchers.*;



@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(locations="/junit.xml")
public class JUnitSpingTest {
	@Autowired
	ApplicationContext context;
	
	static Set<JUnitSpingTest> testObjects = new HashSet<JUnitSpingTest>();
	static ApplicationContext contextObject = null;
	
	@Test
	public void test1() {
		Assert.assertThat(testObjects, not(hasItem(this)));
		testObjects.add(this);
		Assert.assertThat(contextObject == null || contextObject == this.context, is(true));
		contextObject = this.context;
	}
	
	@Test
	public void test2() {
		Assert.assertThat(testObjects, not(hasItem(this)));
		testObjects.add(this);
		
		Assert.assertTrue(contextObject == null || contextObject == this.context);
		contextObject = this.context;
	}
	
	@Test
	public void test3() {
		Assert.assertEquals(this.context, contextObject);
	}
	
}
