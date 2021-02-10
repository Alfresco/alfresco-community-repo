/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.opencmis;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionInterceptor;
import org.alfresco.service.cmr.security.AuthorityService;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import lib3party.org.apache.chemistry.opencmis.server.support.wrapper.ConformanceCmisServiceWrapper;
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

    private int memoryThreshold = super.getMemoryThreshold();
    private long maxContentSize = super.getMaxContentSize();
    private CMISConnector connector;
    private RetryingTransactionInterceptor cmisTransactions;
    private AlfrescoCmisExceptionInterceptor cmisExceptions;
    private AlfrescoCmisServiceInterceptor cmisControl;
    private AlfrescoCmisStreamInterceptor cmisStreams;
    private CMISTransactionAwareHolderInterceptor cmisHolder;
    private AuthorityService authorityService;

    private String cmisCreateDocRequestRenditionsSet = null;

    /**
     *
     * @param memoryThreshold in KB
     */
    public void setMemoryThreshold(double memoryThreshold)
    {
        this.memoryThreshold = ((int) memoryThreshold) * 1024;
    }

    /**
     *
     * @param maxContentSize in MB
     */
    public void setMaxContentSize(double maxContentSize)
    {
        this.maxContentSize = ((long) maxContentSize) * 1024 * 1024;
    }

    @Override
    public int getMemoryThreshold() {
        return memoryThreshold;
    }

    @Override
    public long getMaxContentSize() {
        return maxContentSize;
    }

    /**
     * Sets the Authority Service.
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

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

    /**
     * @param cmisStreams                   interceptor to create reusable ContentStreams
     */
    public void setCmisStreams(AlfrescoCmisStreamInterceptor cmisStreams)
    {
        this.cmisStreams = cmisStreams;
    }

    public void setCmisHolder(CMISTransactionAwareHolderInterceptor cmisHolder)
    {
        this.cmisHolder = cmisHolder;
    }

    public String getCmisCreateDocRequestRenditionsSet() {
        return cmisCreateDocRequestRenditionsSet;
    }

    public void setCmisCreateDocRequestRenditionsSet(String cmisCreateDocRequestRenditionsSet) {
        this.cmisCreateDocRequestRenditionsSet = cmisCreateDocRequestRenditionsSet;
    }

    @Override
    public void init(Map<String, String> parameters)
    {
    }
    
    public void init()
    {
//        this.service = getCmisServiceTarget(connector);
//        
//        // Wrap it
//        ProxyFactory proxyFactory = new ProxyFactory(service);
//        proxyFactory.addInterface(AlfrescoCmisService.class);
//        proxyFactory.addAdvice(cmisExceptions);
//        proxyFactory.addAdvice(cmisControl);
//        proxyFactory.addAdvice(cmisStreams);
//        proxyFactory.addAdvice(cmisTransactions);
//        AlfrescoCmisService cmisService = (AlfrescoCmisService) proxyFactory.getProxy();
//
//        this.serviceWrapper = new CmisServiceWrapper<CmisService>(
//                cmisService,
//                connector.getTypesDefaultMaxItems(), connector.getTypesDefaultDepth(),
//                connector.getObjectsDefaultMaxItems(), connector.getObjectsDefaultDepth());

        if (logger.isInfoEnabled())
        {
            logger.info("init: cmis.create.doc.request.renditions.set=" + cmisCreateDocRequestRenditionsSet);
        }
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
    public CmisService getService(final CallContext context)
    {
        if (logger.isDebugEnabled())
        {
            StringBuilder sb = new StringBuilder();
            sb.append("getService: ").append(AuthenticationUtil.getFullyAuthenticatedUser())
                    .append(" [runAsUser=").append(AuthenticationUtil.getRunAsUser())
                    .append(",ctxUserName=").append(context.getUsername())
                    .append(",ctxRepoId=").append(context.getRepositoryId()).append("]");

            logger.debug(sb.toString());
        }

        // Avoid using guest user if the user is provided in the context
        if(AuthenticationUtil.getFullyAuthenticatedUser() != null && authorityService.isGuestAuthority(AuthenticationUtil.getFullyAuthenticatedUser()))
        {
            AuthenticationUtil.clearCurrentSecurityContext();
        }

        AlfrescoCmisService service = getCmisServiceTarget(connector);
        if (service instanceof AlfrescoCmisServiceImpl)
        {
            Set<String> stringSet = parseCommaSeparatedSet(getCmisCreateDocRequestRenditionsSet());
            ((AlfrescoCmisServiceImpl)service).setCmisRequestRenditionsOnCreateDoc(stringSet);

            if (logger.isTraceEnabled())
            {
                logger.trace("getService: cmis.create.doc.request.renditions.set=" + stringSet);
            }
        }

        // Wrap it
        ProxyFactory proxyFactory = new ProxyFactory(service);
        proxyFactory.addInterface(AlfrescoCmisService.class);
        proxyFactory.addAdvice(cmisExceptions);
        proxyFactory.addAdvice(cmisControl);
        proxyFactory.addAdvice(cmisStreams);
        proxyFactory.addAdvice(cmisTransactions);
        proxyFactory.addAdvice(cmisHolder);
        AlfrescoCmisService cmisService = (AlfrescoCmisService) proxyFactory.getProxy();

        ConformanceCmisServiceWrapper wrapperService = new ConformanceCmisServiceWrapper(
                cmisService,
                connector.getTypesDefaultMaxItems(), connector.getTypesDefaultDepth(),
                connector.getObjectsDefaultMaxItems(), connector.getObjectsDefaultDepth());

        // We use our specific open method here because only we know about it
        cmisService.open(context);

        return wrapperService;
    }

    protected AlfrescoCmisService getCmisServiceTarget(CMISConnector connector)
    {
        return new AlfrescoCmisServiceImpl(connector);
    }

    private Set<String> parseCommaSeparatedSet(String str)
    {
        Set<String> stringSet = new HashSet<>();
        if (str != null)
        {
            StringTokenizer st = new StringTokenizer(str, ",");
            while (st.hasMoreTokens())
            {
                String entry = st.nextToken().trim();
                if (!entry.isEmpty())
                {
                    stringSet.add(entry);
                }
            }
        }
        return stringSet;
    }
}
