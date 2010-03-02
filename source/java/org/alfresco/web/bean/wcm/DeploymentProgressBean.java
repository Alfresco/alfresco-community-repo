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
package org.alfresco.web.bean.wcm;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean used by the AJAX callback from the monitor deployment dialog
 * to track progress of deployments.
 * 
 * @author gavinc
 */
public class DeploymentProgressBean implements Serializable
{
   private static final long serialVersionUID = 3940559099944268131L;
   
   private static Log logger = LogFactory.getLog(DeploymentProgressBean.class);
   
   public void getStatus() throws IOException
   {
      FacesContext context = FacesContext.getCurrentInstance();
      ResponseWriter out = context.getResponseWriter();
      
      Map params = context.getExternalContext().getRequestParameterMap();
      String monitorIds = (String)params.get("ids");
      
      if (logger.isDebugEnabled())
         logger.debug("Retrieving progress status for ids: " + monitorIds);
      
      if (monitorIds != null && monitorIds.length() > 0)
      {
         StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>");
         xml.append("<deployment-progess>");
         
         StringTokenizer tokenizer = new StringTokenizer(monitorIds, ",");
         while (tokenizer.hasMoreTokens())
         {
            String id = tokenizer.nextToken().trim();
            
            // try and find the deployment monitor object in the session
            DeploymentMonitor monitor = (DeploymentMonitor)context.getExternalContext().
                  getSessionMap().get(id);
            
            if (monitor != null)
            {
               if (logger.isDebugEnabled())
                  logger.debug("Found monitor object for id '" + id + "': " + monitor);
               
               xml.append(monitor.toXML());
               
               // cleanup the monitor object from the session if it has completed
               if (monitor.isFinished())
               {
                  context.getExternalContext().getSessionMap().remove(id);
               }
            }
         }
         
         xml.append("</deployment-progess>");
         
         // send the generated XML back to the tree
         out.write(xml.toString());
         
         if (logger.isDebugEnabled())
            logger.debug("returning XML: " + xml.toString());
      }
   }
}
