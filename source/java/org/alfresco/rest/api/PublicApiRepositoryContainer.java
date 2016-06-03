/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.web.scripts.TenantRepositoryContainer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Authenticator;
import org.springframework.extensions.webscripts.WebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRuntime;

/**
 * Repository (server-tier) container for public api
 * 
 * @author steveglover
 * @author davidc
 */
public class PublicApiRepositoryContainer extends TenantRepositoryContainer
{
    protected static final Log logger = LogFactory.getLog(PublicApiRepositoryContainer.class);

    /**
     * Execute script within required level of transaction
     */
    @Override
    protected void transactionedExecute(final WebScript script, final WebScriptRequest scriptReq, final WebScriptResponse scriptRes)
        throws IOException
    {
        final HttpServletRequest httpServletRequest = WebScriptServletRuntime.getHttpServletRequest(scriptReq);
        if(httpServletRequest instanceof PublicApiHttpServletRequest)
        {
            // reset the request input stream if it has been read e.g. by getParameter
            PublicApiHttpServletRequest publicApiRequest = (PublicApiHttpServletRequest)httpServletRequest;
            publicApiRequest.resetInputStream();
        }

        super.transactionedExecute(script, scriptReq, scriptRes);
    }

    @Override
    public void executeScript(final WebScriptRequest scriptReq, final WebScriptResponse scriptRes, final Authenticator auth)
        throws IOException
    {
        String tenant = ((PublicApiTenantWebScriptServletRequest)scriptReq).getTenant();
        if (tenant != null)
        {
            // handle special tenant keys
            // -super-    => run as system tenant
            // -default-  => run as user's default tenant
            String user = null;
            if (tenant.equalsIgnoreCase(TenantUtil.DEFAULT_TENANT))
            {
                // switch from default to super tenant, if not authenticated
                user = AuthenticationUtil.getFullyAuthenticatedUser();
                if (user == null)
                {
                    tenant = TenantUtil.SYSTEM_TENANT;
                }
            }
            
            // run as super tenant
            if (tenant.equalsIgnoreCase(TenantUtil.SYSTEM_TENANT))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("executeScript (-system-): ["+user+","+tenant+"] "+scriptReq.getServicePath());
                }

                TenantUtil.runAsTenant(new TenantRunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        PublicApiRepositoryContainer.super.executeScript(scriptReq, scriptRes, auth);
                        return null;
                        
                    }
                }, TenantService.DEFAULT_DOMAIN);
            }
            else
            {
                if (tenant.equalsIgnoreCase(TenantUtil.DEFAULT_TENANT))
                {
                    tenant = tenantAdminService.getUserDomain(user);
                }

                // run as explicit tenant
                TenantUtil.runAsTenant(new TenantRunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        PublicApiRepositoryContainer.super.executeScript(scriptReq, scriptRes, auth);
                        return null;
                    }
                }, tenant);
            }
        }
    }
}
