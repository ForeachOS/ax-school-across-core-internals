package com.foreach.across.school.internals.modules.two;

import com.foreach.across.school.internals.modules.SomeEvent;
import com.foreach.across.school.internals.modules.one.ExposedComponentOne;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InternalComponentTwo {
    private final ExposedComponentOne exposedComponentOne;

    public InternalComponentTwo(ExposedComponentOne exposedComponentOne) {
        LOG.info("Component created: {} (using {})", getClass(), exposedComponentOne);

        this.exposedComponentOne = exposedComponentOne;
    }

    @EventListener
    public void receive(SomeEvent event) {
        event.add(getClass().getSimpleName());
    }
}
