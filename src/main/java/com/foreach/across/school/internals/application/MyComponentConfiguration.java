package com.foreach.across.school.internals.application;

import com.foreach.across.core.annotations.Exposed;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Arne Vandamme
 * @since 2018-10-03
 */
@Configuration
public class MyComponentConfiguration
{
	@Bean
	@Exposed
	public MyComponent myComponent() {
		return new MyComponent( "myComponent" );
	}
}
