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

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean used by the AJAX callback from the RunLinkValidationDialog.
 * 
 * @author gavinc
 */
public class LinkValidationProgressBean implements Serializable
{
   private static final long serialVersionUID = -6250162468103556028L;

   private AVMBrowseBean avmBrowseBean;
   
   private static Log logger = LogFactory.getLog(LinkValidationProgressBean.class);
   
   public void getStatus() throws IOException
   {
      FacesContext context = FacesContext.getCurrentInstance();
      ResponseWriter out = context.getResponseWriter();
      
      if (logger.isDebugEnabled())
         logger.debug("Retrieving progress status for link validation check...");
      
      StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>");
      xml.append("<link-validation-progress finished=\"");
      xml.append(this.avmBrowseBean.getLinkValidationMonitor().isDone());
      xml.append("\" file-count=\"");
      xml.append(this.avmBrowseBean.getLinkValidationMonitor().getFileUpdateCount());
      xml.append("\" link-count=\"");
      xml.append(this.avmBrowseBean.getLinkValidationMonitor().getUrlUpdateCount());
      xml.append("\"></link-validation-progress>");
         
      // send the generated XML back to the callee
      out.write(xml.toString());
         
      if (logger.isDebugEnabled())
         logger.debug("returning XML: " + xml.toString());
   }

   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
}
