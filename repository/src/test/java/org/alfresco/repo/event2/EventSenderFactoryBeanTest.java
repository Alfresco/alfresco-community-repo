/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Executor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.PropertyResolver;

import org.alfresco.error.AlfrescoRuntimeException;

@RunWith(MockitoJUnitRunner.class)
public class EventSenderFactoryBeanTest
{
    @Mock
    private EventSender eventSender;
    @Mock
    private PropertyResolver propertyResolver;
    @Mock
    private Event2MessageProducer event2MessageProducer;
    @Mock
    private Executor executor;

    private EventSenderFactoryBean factory;

    @Before
    public void setUp() throws Exception
    {
        factory = new EventSenderFactoryBean(propertyResolver, event2MessageProducer, executor, executor) {
            @Override
            protected EventSender createInstance()
            {
                return eventSender;
            }
        };
        factory.afterPropertiesSet();
    }

    @Test
    public void shouldNotInitializeSenderOnBeanCreation()
    {
        verify(eventSender, never()).initialize();
    }

    @Test
    public void shouldInitializeSenderOnStart()
    {
        factory.start();
        verify(eventSender).initialize();
    }

    @Test
    public void shouldTrackRunningState()
    {
        assertThat(factory.isRunning()).isFalse();
        factory.start();
        assertThat(factory.isRunning()).isTrue();
        factory.stop();
        assertThat(factory.isRunning()).isFalse();
    }

    @Test
    public void shouldNotDestroySenderOnStop()
    {
        factory.start();
        factory.stop();
        verify(eventSender, never()).destroy();
    }

    @Test
    public void shouldDestroySenderOnBeanDestroy() throws Exception
    {
        factory.destroy();
        verify(eventSender).destroy();
    }

    @Test
    public void shouldAutoStart()
    {
        assertThat(factory.isAutoStartup()).isTrue();
    }

    @Test
    public void shouldNotMarkAsRunningWhenInitializeThrows()
    {
        doThrow(new RuntimeException("init failed")).when(eventSender).initialize();
        assertThatExceptionOfType(AlfrescoRuntimeException.class).isThrownBy(() -> factory.start());
        assertThat(factory.isRunning()).isFalse();
    }

    @Test
    public void shouldNotInitializeSenderTwiceWhenStartCalledTwice()
    {
        factory.start();
        factory.start();
        verify(eventSender).initialize();
    }

    @Test
    public void shouldReinitializeSenderOnRestartAfterStop()
    {
        factory.start();
        factory.stop();
        factory.start();

        verify(eventSender, times(2)).initialize();
    }
}
