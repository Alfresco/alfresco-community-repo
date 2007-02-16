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
import org.alfresco.repo.webservice.types.QueryConfiguration;
import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Axis handler to extract the fetchSize parameter from the QueryConfiguration SOAP header.
 * The value of fetchSize is then placed in the MessageContext with a property name of
 * ALF_FETCH_SIZE 
 * 
 * @author gavinc
 */
public class QueryConfigHandler extends BasicHandler
{
   public static final String ALF_FETCH_SIZE = "ALF_FETCH_SIZE";

   private static final long serialVersionUID = 6467938074555362971L;
   private static Log logger = LogFactory.getLog(QueryConfigHandler.class);
   
   /**
    * @see org.apache.axis.Handler#invoke(org.apache.axis.MessageContext)
    */
   public void invoke(MessageContext msgContext) throws AxisFault
   {
      try
      {
         // determine the method we are calling
         String opName = "Unknown method";
         OperationDesc op = msgContext.getOperation();
         if (op != null)
         {
            opName = op.getName();
         }

         // try and find the appropriate header and extract info from it
         SOAPEnvelope env = msgContext.getRequestMessage().getSOAPEnvelope();
         SOAPHeaderElement header = env.getHeaderByName(Utils.REPOSITORY_SERVICE_NAMESPACE, "QueryHeader");
         if (header != null)
         {
            if (logger.isDebugEnabled())
               logger.debug("Found QueryHeader for call to " + opName);
            
            QueryConfiguration queryCfg = (QueryConfiguration)header.getObjectValue(QueryConfiguration.class);
            if (queryCfg != null)
            {
               int fetchSize = queryCfg.getFetchSize();
               
               if (logger.isDebugEnabled())
                  logger.debug("Fetch size for query = " + fetchSize);
               
               msgContext.setProperty(ALF_FETCH_SIZE, new Integer(fetchSize));
            }
            else
            {
               if (logger.isDebugEnabled())
                  logger.debug("Failed to find QueryConfiguration within QueryHeader");
            }
         }
         else
         {
            if (logger.isDebugEnabled())
            {
               logger.debug("QueryHeader was not found for call to " + opName);
            }
         }
      }
      catch (Throwable e)
      {
         if (logger.isDebugEnabled())
            logger.debug("Failed to determine fetch size", e);
      }
   }
}
