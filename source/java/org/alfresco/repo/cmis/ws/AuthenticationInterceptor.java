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
package org.alfresco.repo.cmis.ws;

import java.util.List;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.util.auth.Authorization;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSUsernameTokenPrincipal;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;

/**
 * @author Dmitry Velichkevich
 */
public class AuthenticationInterceptor extends AbstractSoapInterceptor
{
    private AuthenticationService authenticationService;
    private TransactionService transactionService;

    public AuthenticationInterceptor()
    {
        super(Phase.PRE_INVOKE);
    }

    public void handleMessage(SoapMessage message) throws Fault
    {
        @SuppressWarnings("unchecked")
        WSHandlerResult handlerResult = ((List<WSHandlerResult>) message.getContextualProperty(WSHandlerConstants.RECV_RESULTS)).get(0);
        WSSecurityEngineResult secRes = (WSSecurityEngineResult) handlerResult.getResults().get(0);
        final WSUsernameTokenPrincipal principal = (WSUsernameTokenPrincipal) secRes.get(WSSecurityEngineResult.TAG_PRINCIPAL);

        // Authenticate
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                try
                {
                    Authorization auth = new Authorization(principal.getName(), principal.getPassword());
                    if (auth.isTicket())
                    {
                        authenticationService.validate(auth.getTicket());
                    }
                    else
                    {
                        authenticationService.authenticate(auth.getUserName(), auth.getPassword().toCharArray());
                    }
                }
                catch (Throwable e)
                {
                    throw new SecurityException("Invalid user name or password specified");
                }

                return null;
            }
        });
    }

    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
}
