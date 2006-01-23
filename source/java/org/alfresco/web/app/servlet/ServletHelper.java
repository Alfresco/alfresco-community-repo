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
package org.alfresco.web.app.servlet;

import javax.servlet.ServletContext;

import org.alfresco.service.ServiceRegistry;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Useful constant values and common methods for Alfresco servlets.
 * 
 * @author Kevin Roast
 */
public final class ServletHelper
{
   /** an existing Ticket can be passed to most servlet for non-session based authentication */
   public static final String ARG_TICKET   = "ticket";
   
   /** forcing guess access is available on most servlets */
   public static final String ARG_GUEST    = "guest";
   
   /** public service bean IDs **/
   public static final String AUTHENTICATION_SERVICE = "authenticationService";
   public static final String PERSON_SERVICE = "personService";
   
   /**
    * Return the ServiceRegistry helper instance
    * 
    * @param sc      ServletContext
    * 
    * @return ServiceRegistry
    */
   public static ServiceRegistry getServiceRegistry(ServletContext sc)
   {
      WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);
      return (ServiceRegistry)wc.getBean(ServiceRegistry.SERVICE_REGISTRY);
   }
   
   /**
    * Private constructor
    */
   private ServletHelper()
   {
   }
}
