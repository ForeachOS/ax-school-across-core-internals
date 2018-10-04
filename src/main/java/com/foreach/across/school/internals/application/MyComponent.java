package com.foreach.across.school.internals.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.Lifecycle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Just a class, not annotated with {@link org.springframework.stereotype.Component}.
 * See {@link MyComponentConfiguration} and {@link com.foreach.across.school.internals.additionals.OtherComponentConfiguration}
 * for the creation of an instance.
 *
 * @author Arne Vandamme
 * @since 2018-10-03
 */
@Slf4j
public class MyComponent implements Lifecycle
{
	private String name = getClass().getName();

	public MyComponent() {
	}

	public MyComponent( String name ) {
		this.name = name;
	}

	private boolean running;

	@PostConstruct
	public void sayHello() {
		LOG.info( "Hello, i am {}", name );
	}

	@PreDestroy
	public void sayGoodbye() {
		LOG.info( "I am {} saying goodbye :(", name );
	}

	@Override
	public void start() {
		LOG.info( "I have started" );
		this.running = true;
	}

	@Override
	public void stop() {
		LOG.info( "I have stopped" );
		this.running = false;
	}

	@Override
	public boolean isRunning() {
		return running;
	}
}
