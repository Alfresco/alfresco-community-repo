/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.cmis.ws;

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSUsernameTokenPrincipal;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;

public class AuthenticationInterceptor extends AbstractSoapInterceptor
{

    public AuthenticationInterceptor()
    {
        super(Phase.PRE_INVOKE);
    }

    public void handleMessage(SoapMessage message) throws Fault
    {
        @SuppressWarnings("unchecked")
        WSHandlerResult handlerResult = ((List<WSHandlerResult>) message.getContextualProperty(WSHandlerConstants.RECV_RESULTS)).get(0);
        WSSecurityEngineResult secRes = (WSSecurityEngineResult) handlerResult.getResults().get(0);
        WSUsernameTokenPrincipal principal = (WSUsernameTokenPrincipal) secRes.get(WSSecurityEngineResult.TAG_PRINCIPAL);

        // Authenticate
        AuthenticationUtil.setCurrentUser(principal.getName());
    }

}
