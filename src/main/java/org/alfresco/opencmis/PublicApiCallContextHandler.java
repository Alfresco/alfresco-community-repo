/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.opencmis;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.server.shared.BasicAuthCallContextHandler;

public class PublicApiCallContextHandler extends BasicAuthCallContextHandler
{
    private static final long serialVersionUID = 8877878113507734452L;

    @Override
	public Map<String, String> getCallContextMap(HttpServletRequest request)
	{
        Map<String, String> map = new HashMap<String, String>();
        
        Map<String, String> basicAuthMap = super.getCallContextMap(request);
        if (basicAuthMap != null && !basicAuthMap.isEmpty()) 
        {
            map.putAll(basicAuthMap);
        }

        // Adding the username in the context is needed because of the following reasons:
        // - CMISServletDispatcher is configured to ALWAYS use this class (PublicApiCallContextHandler)
        // - this class extends the BasicAuthCallContextHandler class which only puts the username in the context ONLY IF the request is having Basic auth
        // - therefor in the case of a Bearer auth, the username is never in the context, fact that ultimately leads to bugs when the response should be provided
        if (map.get(CallContext.USERNAME) == null && AuthenticationUtil.getFullyAuthenticatedUser() != null)
        {
            map.put(CallContext.USERNAME, AuthenticationUtil.getFullyAuthenticatedUser());
        }

        map.put("isPublicApi", "true");
        return map;
	}
}
