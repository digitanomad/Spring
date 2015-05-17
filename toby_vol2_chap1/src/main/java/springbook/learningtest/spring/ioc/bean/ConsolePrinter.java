package springbook.learningtest.spring.ioc.bean;

import springbook.learningtest.spring.ioc.Printer;

public class ConsolePrinter implements Printer {

	@Override
	public void print(String message) {
		System.out.println(message);
	}

}
