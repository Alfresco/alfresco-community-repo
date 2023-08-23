/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.event2;

import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;

@ContextHierarchy({
    // Context hierarchy inherits context config from parent classes and extends it with TestConfig from this class
    @ContextConfiguration(classes = DirectEventGeneratorTest.TestConfig.class)
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class DirectEventGeneratorTest extends EventGeneratorTest
{
    @Autowired
    private InstantiatedBeansRegistry instantiatedBeansRegistry;

    @Autowired
    private EventSender directEventSender;

    @BeforeClass
    public static void beforeClass()
    {
        System.setProperty("repo.event2.queue.skip", "true");
    }

    //@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    public void testIfEnqueuingEventSenderIsNotInstantiated()
    {
        final Set<String> instantiatedBeans = this.instantiatedBeansRegistry.getBeans();

        assertTrue(skipEventQueue);
        assertFalse(instantiatedBeans.contains("enqueuingEventSender"));
    }

    //@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    public void testIfDirectSenderIsSetInEventGenerator()
    {
        assertTrue(skipEventQueue);
        assertEquals(directEventSender, eventGenerator.getEventSender());
    }

    @Configuration
    public static class TestConfig
    {
        @Bean
        public BeanPostProcessor instantiatedBeansRegistry()
        {
            return new InstantiatedBeansRegistry();
        }
    }

    protected static class InstantiatedBeansRegistry implements BeanPostProcessor
    {
        private final Set<String> registeredBeans = new HashSet<>();

        @Override
        public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException
        {
            registeredBeans.add(beanName);
            return bean;
        }

        public Set<String> getBeans() {
            return registeredBeans;
        }
    }
}
