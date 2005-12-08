/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
