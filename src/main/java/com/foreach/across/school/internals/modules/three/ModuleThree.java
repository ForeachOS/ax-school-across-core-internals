package com.foreach.across.school.internals.modules.three;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossRole;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.configurer.ComponentScanConfigurer;
import org.springframework.core.Ordered;

import java.util.Set;

public class ModuleThree extends AcrossModule
{
	@Override
	public String getName() {
		return "ModuleThree";
	}

	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		contextConfigurers.add( ComponentScanConfigurer.forAcrossModule( ModuleThree.class ) );
	}
}
