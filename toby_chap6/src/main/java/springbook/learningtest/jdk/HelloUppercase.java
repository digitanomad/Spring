package springbook.learningtest.jdk;

public class HelloUppercase implements Hello {
	Hello hello;
	
	public HelloUppercase(Hello hello) {
		this.hello = hello;
	}
	
	public String sayHello(String name) {
		return this.hello.sayHello(name).toUpperCase();
	}

	public String sayHi(String name) {
		return this.hello.sayHi(name).toUpperCase();
	}

	public String sayThankYou(String name) {
		return this.hello.sayThankYou(name).toUpperCase();
	}

}
