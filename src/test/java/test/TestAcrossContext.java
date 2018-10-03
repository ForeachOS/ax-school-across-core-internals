package test;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.school.internals.additionals.OtherComponentConfiguration;
import com.foreach.across.school.internals.application.MyComponentConfiguration;
import org.junit.Test;
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

	@Test
	public void acrossContextWithoutParentAndNonExposedBeans() {
		AcrossContext acrossContext = new AcrossContext();
		acrossContext.addModule( new EmptyAcrossModule( "MyModule", OtherComponentConfiguration.class ) );

		acrossContext.bootstrap();

		ApplicationContext parent = AcrossContextUtils.getApplicationContext( acrossContext );
		ApplicationContext module = AcrossContextUtils.getApplicationContext( acrossContext.getModule( "MyModule" ) );

		// 2 application contexts have been created, o
		assertNotSame( parent, module );

		assertFalse( parent.containsBean( "otherComponent" ) );
		assertTrue( module.containsLocalBean( "otherComponent" ) );

		acrossContext.shutdown();
	}
}
