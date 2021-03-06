package com.foreach.across.school.internals.modules.two;

import com.foreach.across.core.annotations.Exposed;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@Exposed
public class ExposedComponentTwo implements Supplier<String>
{
    @Override
    public String get() {
        return "hello from module two";
    }
}
