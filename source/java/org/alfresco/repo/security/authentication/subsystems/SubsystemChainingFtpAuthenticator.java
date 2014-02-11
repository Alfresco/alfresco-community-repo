/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

package org.alfresco.repo.security.authentication.subsystems;

import org.alfresco.filesys.auth.ftp.FTPAuthenticatorBase;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.management.subsystems.ChildApplicationContextManager;
import org.alfresco.repo.security.authentication.AbstractChainingFtpAuthenticator;
import org.springframework.context.ApplicationContext;

import java.util.LinkedList;
import java.util.List;

/**
 * This class wires up all the active {@link #FTPAuthenticatorBase} beans in a chain.
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
        List<FTPAuthenticatorBase> result = new LinkedList<>();
        for (String instance : this.applicationContextManager.getInstanceIds())
        {
            try
            {
                ApplicationContext context = this.applicationContextManager.getApplicationContext(instance);
                FTPAuthenticatorBase authenticator = (FTPAuthenticatorBase) context.getBean(sourceBeanName);
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
