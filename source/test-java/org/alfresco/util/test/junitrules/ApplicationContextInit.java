/*
 * Copyright (C) 2005-2012
 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.util.test.junitrules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.rules.ExternalResource;
import org.springframework.context.ApplicationContext;

/**
 * This JUnit rule can be used to bring up a {@link ApplicationContext spring application context}.
 * <p/>
 * Example usage:
 * <pre>
 * public class YourTestClass
 * {
 *     // Parameterless construction brings up the default Alfresco spring configuration.
 *     &#64;ClassRule public static final ApplicationContextInit APP_CONTEXT_RULE = new ApplicationContextInit();
 *     private static NodeService NODE_SERVICE;
 *     
 *     &#64;BeforeClass public static void initSpringServices() throws Exception
 *     {
 *         NODE_SERVICE = (NodeService) APP_CONTEXT_RULE.getApplicationContext().getBean("nodeService");
 *     }
 * }
 * </pre>
 * 
 * @author Neil Mc Erlean
 * @since Odin
 */
public class ApplicationContextInit extends ExternalResource
{
    public static final String GLOBAL_INTEGRATION_TEST_CONFIG = "classpath:alfresco/test/global-integration-test-context.xml";
    
    /**
     * The locations for the application context configurations.
     */
    private final String[] configLocations;
    
    /**
     * The initialised {@link ApplicationContext spring context}.
     */
    private ApplicationContext appContext;
    
    /**
     * Construct a JUnit rule which will bring up a spring ApplicationContext based on the default Alfresco spring context.
     */
    public ApplicationContextInit()
    {
        this(ApplicationContextHelper.CONFIG_LOCATIONS);
    }
    
    /**
     * Construct a JUnit rule which will bring up an ApplicationContext based on the specified spring contexts.
     * 
     * @param configLocations locations of spring contexts
     */
    public ApplicationContextInit(String... configLocations)
    {
        List<String> requestedConfigs = new ArrayList<String>();
        requestedConfigs.addAll(Arrays.asList(configLocations));
        
        // No matter what spring contexts are provided in construction of this object, we always
        // add the global test integration config to the end.
        // Yes, this will mean that devs cannot override that context file, but it's almost empty anyway.
        // We may have to change how this class handles this, but for now: keep it simple, s.
        requestedConfigs.add(GLOBAL_INTEGRATION_TEST_CONFIG);
        
        this.configLocations = requestedConfigs.toArray(new String[0]);
    }
    
    /**
     * This factory method constructs a JUnit rule which will bring up an ApplicationContext consisting
     * of the default Alfresco context with any additionConfigLocations appended. It is a convenient way to specify
     * override contexts in test code.
     * 
     * @param additionalConfigLocations addition config locations containing additional or overriding beans.
     */
    public static ApplicationContextInit createStandardContextWithOverrides(String... additionalConfigLocations)
    {
        List<String> contexts = new ArrayList<String>();
        
        // The defaults (currently only one)
        for (String defaultConfigLocation: ApplicationContextHelper.CONFIG_LOCATIONS)
        {
            contexts.add(defaultConfigLocation);
        }
        
        // any user supplied extras
        for (String additionalContext : additionalConfigLocations)
        {
            contexts.add(additionalContext);
        }
        
        String[] contextsAsArray = contexts.toArray(new String[0]);
        
        return new ApplicationContextInit(contextsAsArray);
    }
    
    @Override protected void before()
    {
        log.debug("Initialising custom Spring Configuration: " + Arrays.asList(configLocations).toString());
        
        appContext = ApplicationContextHelper.getApplicationContext(configLocations);
    }
    
    @Override protected void after()
    {
        // Intentionally empty. Do nothing.
    }
    
    /**
     * Gets the configLocations as supplied to the code on construction.
     */
    public List<String> getConfigLocations()
    {
        return Arrays.asList(configLocations);
    }
    
    /**
     * Gets the ApplicationContext as initialised by the rule.
     */
    public ApplicationContext getApplicationContext()
    {
        if (this.appContext == null)
        {
            // Chain order is wrong, try to help out by doing the @Before now
            before();
        }
        return this.appContext;
    }
    
    private static final Log log = LogFactory.getLog(ApplicationContextInit.class);

}
