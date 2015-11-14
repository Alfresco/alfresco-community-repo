/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.traitextender;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

public class SpringExtensionBundle implements InitializingBean
{
    private static Log logger = LogFactory.getLog(SpringExtensionBundle.class);

    private List<SpringBeanExtension<?, ?>> extensions = Collections.emptyList();

    private String id;

    private boolean enabled = true;

    private RegistryExtensionBundle extensionBundle;

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void setExtensions(List<SpringBeanExtension<?, ?>> extensions)
    {
        this.extensions = extensions;
    }

    public void setId(String id)
    {
        this.id = id;
    }

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
            logger.info("The extension bundle " + id+" is spring-enabled. Starting ... ");
            start();
        }
        else
        {
            logger.info("Extension bundle " + id + " is spring-disabled.");
        }
    }

}
