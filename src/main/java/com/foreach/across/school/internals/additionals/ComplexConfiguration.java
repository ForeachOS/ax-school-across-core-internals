package com.foreach.across.school.internals.additionals;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author Arne Vandamme
 * @since 2018-10-03
 */
@Configuration
@ComponentScan("some.package")
@Import({ ComplexConfiguration.NestedConfiguration.class, ComplexConfiguration.SomeBeanDefinitionImporter.class })
public class ComplexConfiguration
{
	@Bean
	public String someBean() {
		return "";
	}

	@Bean
	public String someOtherBean() {
		return "hello";
	}

	@Configuration
	public static class NestedConfiguration
	{
		@Bean
		public String someOtherBean() {
			return "goodbye";
		}
	}

	public static class SomeBeanDefinitionImporter implements ImportBeanDefinitionRegistrar
	{
		@Override
		public void registerBeanDefinitions( AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry ) {

		}
	}
}
