package test;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.context.AcrossConfigurableApplicationContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.AcrossListableBeanFactory;
import com.foreach.across.core.context.ExposedBeanDefinition;
import com.foreach.across.school.internals.additionals.OtherComponentConfiguration;
import com.foreach.across.school.internals.application.MyComponentConfiguration;
import com.foreach.across.school.internals.modules.one.ModuleOne;
import com.foreach.across.school.internals.modules.three.ModuleThree;
import com.foreach.across.school.internals.modules.two.ModuleTwo;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 2018-10-03
 */
public class TestAcrossContext
{
	/**
	 * Simple example that illustrates that a child can see beans from the parent context,
	 * but not the other way around.
	 */
	@Test
	public void applicationContextHierarchy() {
		AnnotationConfigApplicationContext root = new AnnotationConfigApplicationContext();
		root.register( MyComponentConfiguration.class );
		root.refresh();

		AnnotationConfigApplicationContext child = new AnnotationConfigApplicationContext();
		child.register( OtherComponentConfiguration.class );
		child.setParent( root );
		child.refresh();

		// myComponent is registered in root, and is visible in the child context
		assertTrue( root.containsLocalBean( "myComponent" ) );
		assertFalse( child.containsLocalBean( "myComponent" ) );
		assertTrue( child.containsBean( "myComponent" ) );

		// otherComponent is registered in the child context and root has no way of knowing it exists
		assertTrue( child.containsBean( "otherComponent" ) );
		assertFalse( root.containsBean( "otherComponent" ) );

		child.stop();
		root.stop();
	}

	/**
	 * Illustrates how modules are bootstrapped in order, based on their dependencies.
	 */
	@Test
	public void simpleAcrossContextStarting() {
		AcrossContext acrossContext = new AcrossContext();
		acrossContext.addModule( new ModuleTwo() );
		acrossContext.addModule( new ModuleOne() );
		acrossContext.addModule( new ModuleThree() );
		acrossContext.bootstrap();
		acrossContext.shutdown();
	}

	/**
	 * Illustrates how regular beans only exist inside the actual module.
	 */
	@Test
	public void acrossContextWithoutParentAndNonExposedBeans() {
		AcrossContext acrossContext = new AcrossContext();
		acrossContext.addModule( new EmptyAcrossModule( "MyModule", OtherComponentConfiguration.class ) );

		acrossContext.bootstrap();

		// once a context has bootstrapped, ApplicationContext instances have been created
		ApplicationContext across = AcrossContextUtils.getApplicationContext( acrossContext );
		ApplicationContext module = AcrossContextUtils.getApplicationContext( acrossContext.getModule( "MyModule" ) );

		// 2 different application contexts have been created, context + module
		assertNotSame( across, module );

		// otherComponent was created in MyModule and can only be seen there
		assertFalse( across.containsBean( "otherComponent" ) );
		assertTrue( module.containsLocalBean( "otherComponent" ) );

		acrossContext.shutdown();
	}

	/**
	 * Illustrates how a bean gets exposed: a special bean definition is created in all other
	 * Across-aware contexts: the direct parent and the other modules.
	 * It does not matter in which order the modules were bootstrapped, the exposed beans will
	 * eventually be registered.
	 */
	@Test
	public void acrossContextWithExposedBean() {
		AcrossContext acrossContext = new AcrossContext();
		acrossContext.addModule( new EmptyAcrossModule( "MyModule", MyComponentConfiguration.class ) );
		acrossContext.addModule( new EmptyAcrossModule( "OtherModule" ) );

		acrossContext.bootstrap();

		AcrossConfigurableApplicationContext across = AcrossContextUtils.getApplicationContext( acrossContext );
		AcrossConfigurableApplicationContext myModule = AcrossContextUtils.getApplicationContext( acrossContext.getModule( "MyModule" ) );
		AcrossConfigurableApplicationContext otherModule = AcrossContextUtils.getApplicationContext( acrossContext.getModule( "OtherModule" ) );

		// myComponent is exposed from the module to the parent and other module
		assertTrue( across.containsBean( "myComponent" ) );
		assertTrue( myModule.containsLocalBean( "myComponent" ) );

		// a special 'ExposedBeanDefinition' is registered in the bean factories of other module and the parent
		BeanDefinition definitionInMyModule = myModule.getBeanFactory().getBeanDefinition( "myComponent" );
		BeanDefinition definitionInOtherModule = otherModule.getBeanFactory().getBeanDefinition( "myComponent" );
		BeanDefinition definitionInParent = across.getBeanFactory().getBeanDefinition( "myComponent" );

		assertNotSame( definitionInMyModule, definitionInParent );
		assertTrue( definitionInParent instanceof ExposedBeanDefinition );
		assertSame( definitionInOtherModule, definitionInParent );

		acrossContext.shutdown();
	}

	/**
	 * Illustrates that an additional parent bean factory is introduced in the hierarchy
	 * for supporting exposed beans.
	 */
	@Test
	public void acrossContextWithAParentApplicationContext() {
		AnnotationConfigApplicationContext parentApplicationContext = new AnnotationConfigApplicationContext();
		ConfigurableListableBeanFactory parentBeanFactory = parentApplicationContext.getBeanFactory();
		parentApplicationContext.refresh();

		// before the Across context starts the introduced parent is the root of the ApplicationContext hierarchy
		assertNull( parentBeanFactory.getParentBeanFactory() );
		assertNull( parentApplicationContext.getParent() );

		// we startup the Across context with the parent context attached
		AcrossContext acrossContext = new AcrossContext();
		acrossContext.setParentApplicationContext( parentApplicationContext );
		acrossContext.addModule( new EmptyAcrossModule( "MyModule", MyComponentConfiguration.class ) );
		acrossContext.bootstrap();

		// myComponent is now also exposed and visible in the parent, however it is not registered directly in the parent
		assertTrue( parentBeanFactory.containsBean( "myComponent" ) );
		assertFalse( parentBeanFactory.containsLocalBean( "myComponent" ) );

		// the exposed bean definition is added to an additional Across-aware context instead,
		// that has been introduced as the new root of the hierarchy
		assertNotNull( parentApplicationContext.getParent() );
		AcrossListableBeanFactory alb = (AcrossListableBeanFactory) parentBeanFactory.getParentBeanFactory();
		assertNotNull( alb );
		assertTrue( alb.containsLocalBean( "myComponent" ) );
		assertTrue( alb.getBeanDefinition( "myComponent" ) instanceof ExposedBeanDefinition );

		acrossContext.shutdown();

		// Across attempts to remove the artificially introduced context when shutting down
		assertNull( parentBeanFactory.getParentBeanFactory() );
		assertNull( parentApplicationContext.getParent() );

		parentApplicationContext.stop();
	}
}
