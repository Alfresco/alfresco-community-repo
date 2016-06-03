package org.alfresco.encryption;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

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
        catch(MissingKeyException e)
        {
            throw new AlfrescoRuntimeException("Bootstrap re-encryption failed", e);
        }
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        if(enabled)
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("Re-encrypting encryptable properties...");
            }
            int propertiesReEncrypted = reEncrypt();
            if(logger.isDebugEnabled())
            {
                logger.debug("...done, re-encrypted " + propertiesReEncrypted + " properties.");
            }
        }
    }
    
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
    }
}