/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.repo.management.subsystems;

import org.alfresco.repo.descriptor.DescriptorServiceAvailableEvent;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.license.LicenseDescriptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationEvent;

import java.io.IOException;

/**
 * {@link SwitchableApplicationContextFactory} that only allows the subsystem to be switched from unencrypted to encrypted, 
 * or if the two subsystems have the same ecrypted state.
 * Switching back to unencrypted from encrypted content store is not allowed. 
 * 
 * @author Matt Ward
 */
public class CryptodocSwitchableApplicationContextFactory extends SwitchableApplicationContextFactory
{
    private DescriptorService descriptorService;
    private static final Log logger = LogFactory.getLog(CryptodocSwitchableApplicationContextFactory.class);
    
    @Override
    protected PropertyBackedBeanState createInitialState() throws IOException
    {
        return new CryptoSwitchableState(sourceBeanName);
    }
    
	protected class CryptoSwitchableState extends SwitchableState
    {
        protected CryptoSwitchableState(String sourceBeanName)
        {
            super(sourceBeanName);
        }

        @Override
        public void setProperty(String name, String value)
        {
            if (name.equals(SOURCE_BEAN_PROPERTY))
            {
                ChildApplicationContextFactory newSourceBean;
                try
                {
                    newSourceBean = getParent().getBean(value, ChildApplicationContextFactory.class);
                }
                catch (BeansException e)
                {
                    throw new IllegalStateException("Switching to the unknown content store \"" + value + "\" is not possible.");
                }

                if (canSwitchSubsystemTo(newSourceBean, value))
                {
                    boolean isNewEncrypted = isEncryptedContentStoreSubsystem(newSourceBean, value);
                    if (isNewEncrypted && !isEncryptionSupported())
                    {
                        throw new IllegalStateException("Switching to the encrypted content store \"" + value + "\" is not licensed.");
                    }
                } 
                else
                {
                    throw new IllegalStateException("Switching to the unencrypted content store \"" + value + "\" is not possible.");
                }
            }
            super.setProperty(name, value);
        }
    }

    private boolean canSwitchSubsystemTo(Object newSourceBean, String beanName)
    {
        Object currentSourceBean = getParent().getBean(getCurrentSourceBeanName());
        boolean isCurrentEncrypted = isEncryptedContentStoreSubsystem(currentSourceBean, getCurrentSourceBeanName());
        // Can switch from an unencrypted content store to any kind of content store
        if (!isCurrentEncrypted)
        {
            return true;
        }
        boolean isNewEncrypted = isEncryptedContentStoreSubsystem(newSourceBean, beanName);
        // Can switch from an encrypted content store only to another encrypted one
        return isCurrentEncrypted && isNewEncrypted;
    }

    private boolean isEncryptedContentStoreSubsystem(Object sourceBean, String beanName)
    {
        boolean isEncrypted = false;
        if (sourceBean instanceof EncryptedContentStoreChildApplicationContextFactory)
        {
            isEncrypted = ((EncryptedContentStoreChildApplicationContextFactory) sourceBean).isEncryptedContent();
        }
        //If not explicitly set as encrypted, check if the source bean is the cryptodoc subsystem bean
        if (!isEncrypted)
        {
            isEncrypted = beanName.equals("encryptedContentStore");
        }
        return isEncrypted;
    }

    private boolean isEncryptionSupported()
    {
        boolean isSupported = true;
        if (descriptorService != null)
        {
            LicenseDescriptor license = descriptorService.getLicenseDescriptor();
            isSupported = license != null && license.isCryptodocEnabled();
        }
        return isSupported;
    }
    
	public void onApplicationEvent(ApplicationEvent event)
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("event : " + event);
		}
		
		if (event instanceof DescriptorServiceAvailableEvent)
		{
			descriptorService = ((DescriptorServiceAvailableEvent)event).getDescriptorService();
		}
	    super.onApplicationEvent(event);
	}

}
