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
package org.alfresco.repo.security.authentication.identityservice;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Properties;

import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import org.alfresco.repo.cache.MemoryCache;
import org.alfresco.repo.cache.SimpleCache;

/**
 * Wiring test for the identity-service authentication subsystem context. Loads only the subsystem XML in isolation, stubs the external dependencies the user-token provider chain transitively needs, and verifies that {@link UserTokenProviders#chooseProvider} resolves to the expected implementation based on the {@code identity-service.authentication.userTokenCache.enabled} flag.
 *
 * <p>
 * This complements the unit tests for {@link CachingUserTokenProvider} and {@link DirectUserTokenProvider} (which test behaviour with hand-built objects) by exercising the actual production XML wiring: bean ids, factory-method binding, constructor-arg ordering, and the property key. It runs as a pure unit test - no database, no Identity Provider, no full ACS context.
 * </p>
 */
public class IdentityServiceUserTokenWiringTest
{
    private static final String CONTEXT_XML = "alfresco/subsystems/Authentication/identity-service/identity-service-authentication-context.xml";
    private static final String CACHE_FLAG = "identity-service.authentication.userTokenCache.enabled";

    @Test
    public void cacheEnabledFlagSelectsCachingProvider()
    {
        UserTokenProvider provider = loadUserTokenProvider("true");

        assertNotNull("userTokenProvider bean must be defined", provider);
        assertTrue("With userTokenCache.enabled=true, userTokenProvider must be a CachingUserTokenProvider but was: "
                + provider.getClass().getName(), provider instanceof CachingUserTokenProvider);
    }

    @Test
    public void cacheDisabledFlagSelectsDirectProvider()
    {
        UserTokenProvider provider = loadUserTokenProvider("false");

        assertTrue("With userTokenCache.enabled=false, userTokenProvider must be a DirectUserTokenProvider but was: "
                + provider.getClass().getName(), provider instanceof DirectUserTokenProvider);
    }

    @Test
    public void cacheDefaultsToDisabledWhenPropertyAbsent()
    {
        // No property override - the XML default is ":false"
        UserTokenProvider provider = loadUserTokenProvider(null);

        assertTrue("Without an explicit override, userTokenProvider must default to DirectUserTokenProvider but was: "
                + provider.getClass().getName(), provider instanceof DirectUserTokenProvider);
    }

    @Test
    public void allThreeProviderBeansAreDefined()
    {
        DefaultListableBeanFactory factory = loadFactory();

        assertTrue("directUserTokenProvider bean definition is missing",
                factory.containsBeanDefinition("directUserTokenProvider"));
        assertTrue("cachingUserTokenProvider bean definition is missing",
                factory.containsBeanDefinition("cachingUserTokenProvider"));
        assertTrue("userTokenProvider bean definition is missing",
                factory.containsBeanDefinition("userTokenProvider"));
    }

    @Test
    public void cachingProviderIsWiredWithDirectProviderAsDelegate()
    {
        DefaultListableBeanFactory factory = loadFactory();
        resolvePlaceholders(factory, "true");

        UserTokenProvider userTokenProvider = factory.getBean("userTokenProvider", UserTokenProvider.class);
        UserTokenProvider directBean = factory.getBean("directUserTokenProvider", UserTokenProvider.class);
        UserTokenProvider cachingBean = factory.getBean("cachingUserTokenProvider", UserTokenProvider.class);

        assertSame("userTokenProvider must be the same instance as cachingUserTokenProvider when the flag is on",
                cachingBean, userTokenProvider);
        assertNotNull("directUserTokenProvider bean must still be constructed (decorator wraps it)", directBean);
        assertTrue("cachingUserTokenProvider must be a CachingUserTokenProvider",
                cachingBean instanceof CachingUserTokenProvider);
        assertTrue("directUserTokenProvider must be a DirectUserTokenProvider",
                directBean instanceof DirectUserTokenProvider);
    }

    // ------- helpers -------

    private UserTokenProvider loadUserTokenProvider(String cacheFlagValue)
    {
        Properties props = new Properties();
        if (cacheFlagValue != null)
        {
            props.setProperty(CACHE_FLAG, cacheFlagValue);
        }
        return loadUserTokenProviderWith(props);
    }

    private UserTokenProvider loadUserTokenProviderWith(Properties props)
    {
        DefaultListableBeanFactory factory = loadFactory();
        resolvePlaceholders(factory, props);
        return factory.getBean("userTokenProvider", UserTokenProvider.class);
    }

    /**
     * Build a bean factory pre-loaded with the auth subsystem XML and with the external dependencies (which would otherwise drag in the entire ACS context) replaced by stubs. The auth subsystem XML defines {@code identityServiceFacade} and {@code jitProvisioningHandler} with their full production dependency graphs; those definitions are removed and overridden with manually-registered singletons here. {@code identityServiceUserTokenBackingCache} is defined in {@code cache-context.xml} (not this XML) so we just register a stub singleton.
     */
    private static DefaultListableBeanFactory loadFactory()
    {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        reader.loadBeanDefinitions(new ClassPathResource(CONTEXT_XML));

        overrideWithSingleton(factory, "identityServiceFacade", mock(IdentityServiceFacade.class));
        overrideWithSingleton(factory, "jitProvisioningHandler", mock(IdentityServiceJITProvisioningHandler.class));

        SimpleCache<String, UserToken> backingCache = new MemoryCache<>();
        factory.registerSingleton("identityServiceUserTokenBackingCache", backingCache);

        return factory;
    }

    private static void overrideWithSingleton(DefaultListableBeanFactory factory, String name, Object instance)
    {
        if (factory.containsBeanDefinition(name))
        {
            factory.removeBeanDefinition(name);
        }
        factory.registerSingleton(name, instance);
    }

    private static void resolvePlaceholders(DefaultListableBeanFactory factory, String cacheFlagValue)
    {
        Properties props = new Properties();
        if (cacheFlagValue != null)
        {
            props.setProperty(CACHE_FLAG, cacheFlagValue);
        }
        resolvePlaceholders(factory, props);
    }

    private static void resolvePlaceholders(DefaultListableBeanFactory factory, Properties props)
    {
        PropertySourcesPlaceholderConfigurer ppc = new PropertySourcesPlaceholderConfigurer();
        ppc.setIgnoreUnresolvablePlaceholders(true);
        ppc.setProperties(props);
        ppc.postProcessBeanFactory(factory);
    }
}
