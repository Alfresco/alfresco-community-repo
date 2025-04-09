/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.encryption;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * 
 * @since 4.0
 *
 */
public class BootstrapReEncryptor extends AbstractLifecycleBean
{
    private static Log logger = LogFactory.getLog(BootstrapReEncryptor.class);

    private boolean enabled;
    private ReEncryptor reEncryptor;

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void setReEncryptor(ReEncryptor reEncryptor)
    {
        this.reEncryptor = reEncryptor;
    }

    public int reEncrypt()
    {
        try
        {
            return reEncryptor.bootstrapReEncrypt();
        }
        catch (MissingKeyException e)
        {
            throw new AlfrescoRuntimeException("Bootstrap re-encryption failed", e);
        }
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        if (enabled)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Re-encrypting encryptable properties...");
            }
            int propertiesReEncrypted = reEncrypt();
            if (logger.isDebugEnabled())
            {
                logger.debug("...done, re-encrypted " + propertiesReEncrypted + " properties.");
            }
        }
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {}
}
