/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.extensions.config.ServerProperties;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Runtime;

import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.web.scripts.TenantWebScriptServletRequest;

public class PublicApiTenantWebScriptServletRequest extends TenantWebScriptServletRequest
{
    public static final String NETWORKS_PATH = "networks";
    public static final String NETWORK_PATH = "network";

    public PublicApiTenantWebScriptServletRequest(Runtime container, HttpServletRequest req, Match serviceMatch, ServerProperties serverProperties)
    {
        super(container, req, serviceMatch, serverProperties);
    }

    @Override
    protected void parse()
    {
        String realPathInfo = getRealPathInfo();

        if (realPathInfo.equals("") || realPathInfo.equals("/"))
        {
            // no tenant - "index" request
            tenant = TenantUtil.DEFAULT_TENANT;
            pathInfo = NETWORKS_PATH;
        }
        else if (realPathInfo.equals("/discovery"))
        {
            // The '/discovery' API is special and doesn't need network info, however,
            // we set the tenant to default, to satisfy PublicApiTenantAuthentication logic.
            tenant = TenantUtil.DEFAULT_TENANT;
            pathInfo = realPathInfo;
        }
        else
        {
            // optimisation - don't need to lowercase the whole path
            if (realPathInfo.substring(0, 5).toLowerCase().equals("/cmis"))
            {
                // cmis service document, pass through as is and set tenant to "-default-".
                tenant = TenantUtil.DEFAULT_TENANT;
                pathInfo = realPathInfo;
            }
            else
            {
                int idx = realPathInfo.indexOf('/', 1);

                // remove tenant
                tenant = realPathInfo.substring(1, idx == -1 ? realPathInfo.length() : idx);
                pathInfo = realPathInfo.substring(tenant.length() + 1);
            }
        }
    }
}
