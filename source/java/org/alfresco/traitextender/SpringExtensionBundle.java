/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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

package org.alfresco.traitextender;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * A {@link SpringBeanExtension}s collection that get registered on the
 * {@link Extender}'s registry on {@link #afterPropertiesSet()}.<br>
 * Works in conjunction with {@link SpringBeanExtension}s and
 * {@link SpringExtensionPoint}s to define and start spring based
 * {@link ExtensionBundle}s of {@link SingletonExtension}s.<br>
 * The spring-context XML sample bellow shows the definition of spring-bundled
 * trait-extensions:
 * 
 * <pre>
 * {@code
 * 
 *  <bean id="ep1" class="org.alfresco.traitextender.SpringExtensionPoint">
 *     <property name="extension" value="org.alfresco.sample.Extension1" />
 *     <property name="trait" value="org.alfresco.sample.Trait1" />
 *  </bean>
 *  
 *  <bean id="ep2" class="org.alfresco.traitextender.SpringExtensionPoint">
 *     <property name="extension" value="org.alfresco.sample.Extension2" />
 *     <property name="trait" value="org.alfresco.sample.Trait2" />
 *  </bean>
 * 
 *   <bean id="extension1" class="org.alfresco.sample.Extension1">
 *    <property name="extensionPoint" ref="ep1" />
 *  </bean>
 * 
 *  <bean id="extension2" class="org.alfresco.sample.Extension2">
 *     <property name="extensionPoint" ref="ep2" />
 *  </bean>
 * 
 *  <bean id="aBundle" class="org.alfresco.traitextender.SpringExtensionBundle">
 *    <property name="id" value="org.alfresco.sample.aBundle" />
 *    <property name="enabled" value="true" />
 *    <property name="extensions">
 *       <list>
 *          <ref bean="extension1" />
 *          <ref bean="extension2" />
 *       </list >
 *    </property>
 *  </bean>
 * }
 * </pre>
 * 
 * @author Bogdan Horje
 */
public class SpringExtensionBundle implements InitializingBean
{
    private static Log logger = LogFactory.getLog(SpringExtensionBundle.class);

    private List<SpringBeanExtension<?, ?>> extensions = Collections.emptyList();

    private String id;

    private boolean enabled = true;

    private RegistryExtensionBundle extensionBundle;

    /**
     * @param enabled <code>true</code> if the current bundle should be
     *            registered.<br>
     *            <code>false</code> if the current bundle should skip extension
     *            registration
     */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /**
     * Returns whether the current bundle should be registered or not.
     *
     * @return {@code true} if the current bundle should be registered, otherwise {@code false}
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    public void setExtensions(List<SpringBeanExtension<?, ?>> extensions)
    {
        this.extensions = extensions;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * Creates a {@link RegistryExtensionBundle} and registers all contained
     * {@link SpringBeanExtension}s with it.<br>
     * When all extension have successfully registered it starts the
     * {@link RegistryExtensionBundle}.<br>
     * The previously created {@link RegistryExtensionBundle} is stored for
     * later start or {@link #stop()} operations.
     * 
     * @see Extender#start(ExtensionBundle)
     * @see Extender#stop(ExtensionBundle)
     */
    public synchronized void start()
    {

        if (extensionBundle == null)
        {
            logger.info("Registering extension bundle " + id);
            extensionBundle = new RegistryExtensionBundle(id);

            for (SpringBeanExtension<?, ?> springExtension : extensions)
            {
                try
                {
                    springExtension.register(extensionBundle);
                }
                catch (Exception error)
                {
                    throw new InvalidExtension("Could not register extension " + springExtension + " with "
                                                           + extensionBundle,
                                               error);
                }

            }
        }

        logger.info("Starting extension bundle " + id);
        Extender.getInstance().start(extensionBundle);
    }

    /**
     * Stops a previously {@link #start()} created
     * {@link RegistryExtensionBundle}.
     * 
     * @see Extender#start(ExtensionBundle)
     * @see Extender#stop(ExtensionBundle)
     */
    public synchronized void stop()
    {
        if (extensionBundle == null)
        {
            logger.info("Stop request for unregistered extension bundle " + id);
        }
        logger.info("Stopping extension bundle " + id);
        Extender.getInstance().stop(extensionBundle);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        if (this.enabled)
        {
            logger.info("The extension bundle " + id + " is spring-enabled. Starting ... ");
            start();
        }
        else
        {
            logger.info("Extension bundle " + id + " is spring-disabled.");
        }
    }

}
