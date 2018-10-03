package com.foreach.across.school.internals.additionals;

import com.foreach.across.school.internals.application.MyComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Arne Vandamme
 * @since 2018-10-03
 */
@Configuration
public class OtherComponentConfiguration
{
	@Bean
	public MyComponent otherComponent() {
		return new MyComponent( "otherComponent" );
	}
}
