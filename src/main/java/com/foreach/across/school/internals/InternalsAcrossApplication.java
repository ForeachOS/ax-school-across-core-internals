package com.foreach.across.school.internals;

import com.foreach.across.config.AcrossApplication;
import org.springframework.boot.SpringApplication;

import java.util.Collections;

@AcrossApplication(
		modules = {
				
		}
)
public class InternalsAcrossApplication
{
	public static void main( String[] args ) {
		SpringApplication springApplication = new SpringApplication( InternalsAcrossApplication.class );
		springApplication.setDefaultProperties( Collections.singletonMap( "spring.config.location", "${user.home}/dev-configs/internals-application.yml" ) );
		springApplication.run( args );
	}
}