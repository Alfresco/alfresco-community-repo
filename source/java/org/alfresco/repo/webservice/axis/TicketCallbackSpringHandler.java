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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.webservice.axis;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Axis handler that retrieves the TicketCallbackHandler instance from 
 * a Spring context. The authentication service is injected by Spring 
 * so that when it gets called by the WSS4J handler it can verify the 
 * ticket passed to the service.
 * The callback handler is then added to the MessageContext under the standard 
 * WsHandlerConstants.PW_CALLBACK_REF property.
 * 
 * @author gavinc
 */
public class TicketCallbackSpringHandler extends BasicHandler
{
   @SuppressWarnings("unused")
private static final Log logger = LogFactory.getLog(TicketCallbackSpringHandler.class);
   private static final String BEAN_NAME = "ticketCallbackHandler";
   private static final long serialVersionUID = -135125831180499667L;

   /**
    * @see org.apache.axis.Handler#invoke(org.apache.axis.MessageContext)
    */
   public void invoke(MessageContext msgContext) throws AxisFault
   {
      // get hold of the Spring context and retrieve the AuthenticationService
      HttpServletRequest req = (HttpServletRequest)msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
      ServletContext servletCtx = req.getSession().getServletContext();
      WebApplicationContext webAppCtx = WebApplicationContextUtils.getRequiredWebApplicationContext(servletCtx);
      TicketCallbackHandler callback = (TicketCallbackHandler)webAppCtx.getBean(BEAN_NAME);
      
      // store the callback in the context where the WS-Security handler can pick it up from
      msgContext.setProperty(WSHandlerConstants.PW_CALLBACK_REF, callback);
   }
}
