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

import org.alfresco.repo.webservice.Utils;
import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.providers.java.RPCProvider;
import org.springframework.web.context.WebApplicationContext;

/**
 * A custom Axis RPC Provider that retrieves services via Spring
 * 
 * @author gavinc
 */
public class SpringBeanRPCProvider extends RPCProvider
{
   private static final long serialVersionUID = 2173234269124176995L;
   private static final String OPTION_NAME = "springBean";
   private static WebApplicationContext webAppCtx;

   /**
    * Retrieves the class of the bean represented by the given name
    * 
    * @see org.apache.axis.providers.java.JavaProvider#getServiceClass(java.lang.String, org.apache.axis.handlers.soap.SOAPService, org.apache.axis.MessageContext)
    */
   @Override
   protected Class getServiceClass(String beanName, SOAPService service, MessageContext msgCtx) throws AxisFault
   {
      Class clazz = null;
      
      Object bean = getBean(msgCtx, beanName);
      if (bean != null)
      {
         clazz = bean.getClass();
      }
      
      return clazz;
   }

   /**
    * @see org.apache.axis.providers.java.JavaProvider#getServiceClassNameOptionName()
    */
   @Override
   protected String getServiceClassNameOptionName()
   {
      return OPTION_NAME;
   }

   /**
    * Retrieves the bean with the given name from the current spring context
    * 
    * @see org.apache.axis.providers.java.JavaProvider#makeNewServiceObject(org.apache.axis.MessageContext, java.lang.String)
    */
   @Override
   protected Object makeNewServiceObject(MessageContext msgCtx, String beanName) throws Exception
   {
      return getBean(msgCtx, beanName);
   }
   
   /**
    * Retrieves the bean with the given name from the current spring context
    * 
    * @param msgCtx Axis MessageContext
    * @param beanName Name of the bean to lookup
    * @return The instance of the bean
    */
   private Object getBean(MessageContext msgCtx, String beanName) throws AxisFault
   {
      return getWebAppContext(msgCtx).getBean(beanName);
   }
   
   /**
    * Retrieves the Spring context from the web application
    * 
    * @param msgCtx Axis MessageContext
    * @return The Spring web app context
    */
   private WebApplicationContext getWebAppContext(MessageContext msgCtx) throws AxisFault
   {
      if (webAppCtx == null && msgCtx != null)
      {
         webAppCtx = Utils.getSpringContext(msgCtx);
      }
      
      if (webAppCtx == null)
      {
         throw new AxisFault("Failed to retrieve the Spring web application context");
      }
      
      return webAppCtx;
   }
   
   @Override
   public void initServiceDesc(SOAPService service, MessageContext msgContext) throws AxisFault 
   {
       if( msgContext != null )
       {
           getWebAppContext(msgContext);
       }
       super.initServiceDesc(service, msgContext);
   }
}
