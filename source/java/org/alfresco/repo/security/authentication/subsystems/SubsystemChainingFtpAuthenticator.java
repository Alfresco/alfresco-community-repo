
package org.alfresco.repo.security.authentication.subsystems;

import org.alfresco.filesys.auth.ftp.FTPAuthenticatorBase;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.management.subsystems.ChildApplicationContextManager;
import org.alfresco.repo.security.authentication.AbstractChainingFtpAuthenticator;
import org.springframework.context.ApplicationContext;

import java.util.LinkedList;
import java.util.List;

/**
 * This class wires up all the active {@link org.alfresco.filesys.auth.ftp.FTPAuthenticatorBase} beans in a chain.
 *
 * @author alex.mukha
 * @since 4.2.1
 */
public class SubsystemChainingFtpAuthenticator extends AbstractChainingFtpAuthenticator
{
    private ChildApplicationContextManager applicationContextManager;
    private String sourceBeanName;

    /**
     * IOC
     * @param applicationContextManager the applicationContextManager to set
     */
    public void setApplicationContextManager(ChildApplicationContextManager applicationContextManager)
    {
        this.applicationContextManager = applicationContextManager;
    }

    /**
     * Sets the name of the bean to look up in the child application contexts.
     *
     * @param sourceBeanName the bean name
     */
    public void setSourceBeanName(String sourceBeanName)
    {
        this.sourceBeanName = sourceBeanName;
    }

    @Override
    protected List<FTPAuthenticatorBase> getUsableFtpAuthenticators()
    {
        List<FTPAuthenticatorBase> result = new LinkedList<FTPAuthenticatorBase>();
        for (String instance : this.applicationContextManager.getInstanceIds())
        {
            try
            {
                ApplicationContext context = this.applicationContextManager.getApplicationContext(instance);
                FTPAuthenticatorBase authenticator = (FTPAuthenticatorBase) context.getBean(sourceBeanName);
                
                if(this.getClass().isInstance(authenticator))
                {
                    continue;
                }
                // Only add active authenticators. E.g. we might have an passthru FTP authenticator that is disabled.
                if (!(authenticator instanceof ActivateableBean)
                        || ((ActivateableBean) authenticator).isActive())
                {
                    result.add(authenticator);
                }
            }
            catch (RuntimeException e)
            {
                // The bean doesn't exist or this subsystem won't start. The reason would have been logged. Ignore and continue.
            }
        }
        return result;
    }
}
