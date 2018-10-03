package com.foreach.across.school.internals.application;

import com.foreach.across.school.internals.additionals.OtherComponentConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Arne Vandamme
 * @since 2018-10-03
 */
@Configuration
@ConditionalOnMissingBean(name = "myComponent")
@Import(OtherComponentConfiguration.class)
public class MoreComponentsConfiguration
{
}
