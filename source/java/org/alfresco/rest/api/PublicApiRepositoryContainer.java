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
