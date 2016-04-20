package org.alfresco.repo.management.subsystems;

import java.io.IOException;

import org.alfresco.repo.descriptor.DescriptorServiceAvailableEvent;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.license.LicenseDescriptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;

/**
 * {@link SwitchableApplicationContextFactory} that only allows the subsystem to be switched
 * if the current subsystem is the unencrypted content store. When an attempt is made to switch away
 * from any other store (e.g. the encrypted store) then nothing happens. This is achieved by returning
 * <code>false</code> for {@link #isUpdateable(String)} calls when the current sourceBeanName is
 * that of the unencrypted content store's subsystem.
 *  
 * @author Matt Ward
 */
public class CryptodocSwitchableApplicationContextFactory extends SwitchableApplicationContextFactory
	
{
    private static final String SOURCE_BEAN_PROPERTY = "sourceBeanName";
    private String unencryptedContentStoreBeanName;
    private String encryptedContentStoreBeanName;
    private DescriptorService descriptorService;
    private static final Log logger = LogFactory.getLog(CryptodocSwitchableApplicationContextFactory.class);
    
    @Override
    public boolean isUpdateable(String name)
    {
        if (name == null)
        {
            throw new IllegalStateException("Property name cannot be null");
        }
        
        boolean updateable = true;
        if (name.equals(SOURCE_BEAN_PROPERTY))
        {
        	if(getCurrentSourceBeanName().equals(unencryptedContentStoreBeanName))
        	{
        		if(descriptorService != null)
        		{
        			LicenseDescriptor license = descriptorService.getLicenseDescriptor();
        			if(license != null && license.isCryptodocEnabled())
        			{
        				return true;
        			}
        			return false;
        		}
        	}

        	// can the source bean name be changed?
        	if(!getCurrentSourceBeanName().equals(unencryptedContentStoreBeanName))
        	{
        		// the subsystem has been switched once.
        		return false;
        	}
        }
        return updateable;
    }

    @Override
    protected PropertyBackedBeanState createInitialState() throws IOException
    {
        return new CryptoSwitchableState(sourceBeanName);
    }


    /**
     * The bean name of the unencrypted ContentStore subsystem.
     * 
     * @param unencryptedContentStoreBeanName String
     */
    public void setUnencryptedContentStoreBeanName(String unencryptedContentStoreBeanName)
    {
        this.unencryptedContentStoreBeanName = unencryptedContentStoreBeanName;
    }
    
	public String getEncryptedContentStoreBeanName() 
	{
		return encryptedContentStoreBeanName;
	}

	public void setEncryptedContentStoreBeanName(
			String encryptedContentStoreBeanName) 
	{
	    this.encryptedContentStoreBeanName = encryptedContentStoreBeanName;
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
            if (!isUpdateable(name))
            {
            	if(value.equalsIgnoreCase(unencryptedContentStoreBeanName))
            	{
            		throw new IllegalStateException("Switching to an unencrypted content store is not possible.");
            	}
            	if(value.equalsIgnoreCase(encryptedContentStoreBeanName))
            	{
            		throw new IllegalStateException("Switching to an encrypted content store is not licensed.");
            	}
                throw new IllegalStateException("Switching to an unknown content store is not possible." + value);
            }
            super.setProperty(name, value);
        }
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
