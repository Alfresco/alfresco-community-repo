/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.opencmis;

import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionInterceptor;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.server.support.CmisServiceWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactory;

/**
 * Factory for OpenCMIS service objects.
 * 
 * @author florian.mueller
 * @author Derek Hulley
 */
public class AlfrescoCmisServiceFactory extends AbstractServiceFactory
{
    private static final Log logger = LogFactory.getLog(AlfrescoCmisServiceFactory.class);
    
    private CMISConnector connector;
    private RetryingTransactionInterceptor cmisTransactions;
    private AlfrescoCmisExceptionInterceptor cmisExceptions;
    private AlfrescoCmisServiceInterceptor cmisControl;

    /**
     * Sets the CMIS connector.
     */
    public void setCmisConnector(CMISConnector connector)
    {
        this.connector = connector;
    }

    /**
     * @param cmisTransactions                      the interceptor that applies appropriate transactions
     */
    public void setCmisTransactions(RetryingTransactionInterceptor cmisTransactions)
    {
        this.cmisTransactions = cmisTransactions;
    }

    /**
     * @param cmisExceptions                        interceptor to translate exceptions
     */
    public void setCmisExceptions(AlfrescoCmisExceptionInterceptor cmisExceptions)
    {
        this.cmisExceptions = cmisExceptions;
    }

    /**
     * @param cmisControl                           interceptor that provides logging and authentication checks
     */
    public void setCmisControl(AlfrescoCmisServiceInterceptor cmisControl)
    {
        this.cmisControl = cmisControl;
    }

    @Override
    public void init(Map<String, String> parameters)
    {
    }

    @Override
    public void destroy()
    {
    }
    
    /**
     * TODO:
     *      We are producing new instances each time.   
     */
    @Override
    public CmisService getService(CallContext context)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("\n" +
                    "CMIS getService(): \n" +
                    "   Authenticated as: " + AuthenticationUtil.getFullyAuthenticatedUser() + "\n" +
                    "   Running as:       " + AuthenticationUtil.getRunAsUser() + "\n" +
                    "   User:             " + context.getUsername() + "\n" +
                    "   Repo:             " + context.getRepositoryId());
        }
        
        AlfrescoCmisService cmisServiceTarget = new AlfrescoCmisServiceImpl(connector);
        
        // Wrap it
        ProxyFactory proxyFactory = new ProxyFactory(cmisServiceTarget);
        proxyFactory.addInterface(AlfrescoCmisService.class);
        proxyFactory.addAdvice(cmisExceptions);
        proxyFactory.addAdvice(cmisControl);
        proxyFactory.addAdvice(cmisTransactions);
        AlfrescoCmisService cmisService = (AlfrescoCmisService) proxyFactory.getProxy();
            
        CmisServiceWrapper<CmisService> wrapperService = new CmisServiceWrapper<CmisService>(
                cmisService,
                connector.getTypesDefaultMaxItems(), connector.getTypesDefaultDepth(),
                connector.getObjectsDefaultMaxItems(), connector.getObjectsDefaultDepth());

        // We use our specific open method here because only we know about it
        cmisService.open(context);
        
        return wrapperService;
    }
}
