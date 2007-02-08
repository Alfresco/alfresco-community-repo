/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.webservice.axis;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.webservice.authentication.AuthenticationFault;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSPasswordCallback;

/**
 * CallbackHandler that verifies the given ticket in the password element of the UsernameToken
 * header is still a valid ticket
 * 
 * @author gavinc
 */
public class TicketCallbackHandler implements CallbackHandler
{
   private static final Log logger = LogFactory.getLog(TicketCallbackHandler.class);
      
   private AuthenticationService authenticationService;
   
   /**
    * Sets the AuthenticationService instance to use
    * 
    * @param authenticationService The AuthenticationService
    */
   public void setAuthenticationService(AuthenticationService authenticationService)
   {
      this.authenticationService = authenticationService;
   }

   /**
    * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
    */
   public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
   {
      for (int i = 0; i < callbacks.length; i++) 
      {
         if (callbacks[i] instanceof WSPasswordCallback) 
         {
            WSPasswordCallback pc = (WSPasswordCallback)callbacks[i];
            String ticket = pc.getPassword();
            
            if (logger.isDebugEnabled())
            {
               logger.debug("Verifying ticket for: " + pc.getIdentifer());
               logger.debug("Ticket: " + ticket);
            }

            // ensure the ticket is valid
            try
            {
               this.authenticationService.validate(ticket);
            }
            catch (AuthenticationException ae)
            {
               if (logger.isDebugEnabled())
                  logger.debug("Ticket validation failed: " + ae.getMessage());
               
               // NOTE: Throwing AuthenticationFault just gets consumed and the ws-security handler 
               //       reports a missing password; we would need to modify the WSS4J code to let
               //       the exception bubble up so for now just let the default message get thrown
               throw new AuthenticationFault(701, "Authentication failed due to an invalid ticket");
            }
            
            if (logger.isDebugEnabled())
               logger.debug("Ticket validated successfully");
            
            // if all is well set the password to return as the given ticket
            pc.setPassword(pc.getPassword());
         }
         else 
         {
            throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
         }
      }
   }
}
