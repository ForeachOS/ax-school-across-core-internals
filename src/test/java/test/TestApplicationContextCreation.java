package test;

import com.foreach.across.school.internals.additionals.OtherComponentConfiguration;
import com.foreach.across.school.internals.application.MoreComponentsConfiguration;
import com.foreach.across.school.internals.application.MyComponent;
import com.foreach.across.school.internals.application.MyComponentConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 2018-10-03
 */
@Slf4j
public class TestApplicationContextCreation
{
	private AnnotationConfigApplicationContext applicationContext;
	private ConfigurableListableBeanFactory beanFactory;

	@Before
	public void recreateApplicationContext() {
		applicationContext = new AnnotationConfigApplicationContext();
		beanFactory = applicationContext.getBeanFactory();
	}

	/**
	 * Manually create an application context and add a plain component to it (note that the class does not need
	 * to be annotated with {@link org.springframework.stereotype.Component}. Only when the application context
	 * is refreshed will the instance of the component get created.
	 * <p>
	 * We also illustrate the behaviour of {@link ConfigurableApplicationContext#start()}, which appears to be
	 * deprecated (no longer used) in the Spring Boot era.
	 */
	@Test
	public void simpleApplicationContext() {
		// register will create the bean definition in the bean factory
		// the default bean name is derived from the simple class name: MyComponent -> myComponent
		applicationContext.register( MyComponent.class );
		assertTrue( beanFactory.containsBeanDefinition( "myComponent" ) );

		BeanDefinition beanDefinition = beanFactory.getBeanDefinition( "myComponent" );
		LOG.info( "myComponent: {}", beanDefinition );

		// this would not work as refresh() has not yet been called
		//assertNotNull( applicationContext.getBean( "myComponent" ));

		// calling refresh will initialize the beans - this starts up the bulk of the ApplicationContext
		applicationContext.refresh();

		// now my component exists, and the value returned from the ApplicationContext is the same as from the BeanFactory used
		MyComponent myComponent = applicationContext.getBean( MyComponent.class );
		assertNotNull( myComponent );
		assertSame( myComponent, beanFactory.getBean( MyComponent.class ) );

		// start() is only relevant for special Lifecycle events, but beans usually are created on refresh
		LOG.info( "Refresh happened, start not yet!" );

		assertFalse( myComponent.isRunning() );
		applicationContext.start();
		assertTrue( myComponent.isRunning() );

		// likewise stop() only sends the LifeCycle events but does not destroy the application context or bean factory
		applicationContext.stop();
		assertFalse( myComponent.isRunning() );
		assertSame( myComponent, applicationContext.getBean( MyComponent.class ) );

		applicationContext.close();

		// this would no longer work as the application context has been closed
		//assertNotNull( applicationContext.getBean( "myComponent" ));
	}

	/**
	 * Unlike manually registering the annotated class, when we register the component through a configuration class,
	 * its bean definition is only available after the call to refresh().
	 * <p>
	 * This illustrates there can be subtle differences in when bean definitions are registered.
	 *
	 * @see #beanRegistrationConditionals()
	 */
	@Test
	public void configurationClassForBeansRegistration() {
		applicationContext.register( MyComponentConfiguration.class );
		assertFalse( beanFactory.containsBeanDefinition( "myComponent" ) );

		applicationContext.refresh();
		assertTrue( beanFactory.containsBeanDefinition( "myComponent" ) );

		applicationContext.stop();
	}

	/**
	 * Class {@link MoreComponentsConfiguration} imports the {@link OtherComponentConfiguration}, which in turn
	 * registers a bean called "otherComponent".
	 *
	 * @see #manualBeanDefinition()
	 */
	@Test
	public void beanRegistrationConditionals() {
		{
			// add a configuration class which imports the bean definitions
			applicationContext.register( MoreComponentsConfiguration.class );

			// because MoreComponentsConfiguration is a @Configuration class, it is not scanned for bean definitions until refresh() is called
			assertFalse( beanFactory.containsBeanDefinition( "otherComponent" ) );

			applicationContext.refresh();
			assertTrue( beanFactory.containsBeanDefinition( "otherComponent" ) );
			assertNotNull( beanFactory.getBean( "otherComponent" ) );

			applicationContext.close();
		}

		recreateApplicationContext();

		{
			// MoreComponentsConfiguration has a condition that no bean "myComponent" should exist,
			// if we add MyComponentConfiguration then "otherComponent" will no longer get registered
			applicationContext.register( MyComponentConfiguration.class );
			applicationContext.register( MoreComponentsConfiguration.class );
			applicationContext.refresh();

			assertTrue( beanFactory.containsBeanDefinition( "myComponent" ) );
			assertFalse( beanFactory.containsBeanDefinition( "otherComponent" ) );

			applicationContext.close();
		}

		recreateApplicationContext();

		{
			// We can see the effect of registration order on conditionals, simply swap the configuration registrations around
			// Then both beans are available, this illustrates that conditionals are checked at registration time, not at the bean creation time.
			applicationContext.register( MoreComponentsConfiguration.class );
			applicationContext.register( MyComponentConfiguration.class );
			applicationContext.refresh();

			assertTrue( beanFactory.containsBeanDefinition( "myComponent" ) );
			assertTrue( beanFactory.containsBeanDefinition( "otherComponent" ) );

			applicationContext.close();
		}
	}

	/**
	 * There are several ways to add bean definitions, though most common are using component scans and {@link Configuration} classes.
	 * This sample illustrates manually adding a bean definition (something that has been made simpler in Spring FW 5), but there
	 * is also a {@link org.springframework.context.annotation.ImportBeanDefinitionRegistrar} for example.
	 *
	 * @see #beanDefinitionOverriding()
	 */
	@Test
	public void manualBeanDefinition() {
		BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) beanFactory;
		beanDefinitionRegistry.registerBeanDefinition( "manualBean", new RootBeanDefinition( MyComponent.class ) );

		applicationContext.refresh();

		assertTrue( applicationContext.getBean( "manualBean" ) instanceof MyComponent );

		applicationContext.stop();
	}

	/**
	 * Another situation where the registration order is important, has to do with bean definition overriding.
	 * A bean definition is identified by its name, and a same name will simply replace the previously registered bean definition.
	 * <p>
	 * As with conditionals, the order is important, last one wins.
	 */
	@Test
	public void beanDefinitionOverriding() {
		{
			// replace the original myComponent by a string
			applicationContext.register( MyComponentConfiguration.class );
			applicationContext.register( ReplaceMyComponent.class );

			applicationContext.refresh();

			assertTrue( applicationContext.getBean( "myComponent" ) instanceof String );

			applicationContext.stop();
		}

		recreateApplicationContext();

		{
			// first register the string, but afterwards the original - which will win
			applicationContext.register( ReplaceMyComponent.class );
			applicationContext.register( MyComponentConfiguration.class );

			applicationContext.refresh();

			assertTrue( applicationContext.getBean( "myComponent" ) instanceof MyComponent );

			applicationContext.stop();
		}
	}

	@Configuration
	static class ReplaceMyComponent
	{
		@Bean
		public String myComponent() {
			return "just-a-string";
		}
	}
}
