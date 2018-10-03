package com.foreach.across.school.internals;

import com.foreach.across.config.AcrossWebApplicationAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;

/**
 * @author Arne Vandamme
 * @since 2018-10-03
 */
@SpringBootApplication(
		exclude = {
				AcrossWebApplicationAutoConfiguration.class,
				LiquibaseAutoConfiguration.class
		},
		scanBasePackages = "com.foreach.across.school.internals.application"
)
public class InternalsSpringBootApplication
{
	public static void main( String[] args ) {
		SpringApplication springApplication = new SpringApplication( InternalsSpringBootApplication.class );
		springApplication.run( args );
	}
}
