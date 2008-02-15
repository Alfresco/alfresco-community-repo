/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.web.bean.ajax;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.web.app.servlet.ajax.InvokeCommand;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean backing the ajax requests from various Portlet webscripts.
 * 
 * @author Mike Hatfield
 */
public class PortletActionsBean implements Serializable
{
   private static final long serialVersionUID = -8230154592621310289L;
   
   private static Log logger = LogFactory.getLog(PortletActionsBean.class);
   
   @InvokeCommand.ResponseMimetype(value=MimetypeMap.MIMETYPE_HTML)
   public void deleteItem() throws Exception
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      ResponseWriter out = fc.getResponseWriter();
      
      Map<String, String> requestMap = fc.getExternalContext().getRequestParameterMap();
      String strNodeRef = (String)requestMap.get("noderef");
      if (strNodeRef != null && strNodeRef.length() != 0)
      {
         try
         {
            Repository.getServiceRegistry(fc).getFileFolderService().delete(new NodeRef(strNodeRef));
            out.write("OK: " + strNodeRef);
         }
         catch (Throwable err)
         {
            out.write("ERROR: " + err.getMessage());
         }
      }
   }

   @InvokeCommand.ResponseMimetype(value=MimetypeMap.MIMETYPE_HTML)
   public void checkoutItem() throws Exception
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      ResponseWriter out = fc.getResponseWriter();
      
      Map<String, String> requestMap = fc.getExternalContext().getRequestParameterMap();
      String strNodeRef = (String)requestMap.get("noderef");
      if (strNodeRef != null && strNodeRef.length() != 0)
      {
         try
         {
            Repository.getServiceRegistry(fc).getCheckOutCheckInService().checkout(new NodeRef(strNodeRef));
            out.write("OK: " + strNodeRef);
         }
         catch (Throwable err)
         {
            out.write("ERROR: " + err.getMessage());
         }
      }
   }

   @InvokeCommand.ResponseMimetype(value=MimetypeMap.MIMETYPE_HTML)
   public void checkinItem() throws Exception
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      ResponseWriter out = fc.getResponseWriter();
      
      Map<String, String> requestMap = fc.getExternalContext().getRequestParameterMap();
      String strNodeRef = (String)requestMap.get("noderef");
      if (strNodeRef != null && strNodeRef.length() != 0)
      {
         try
         {
            Map<String, Serializable> props = new HashMap<String, Serializable>(2, 1.0f);
            props.put(Version.PROP_DESCRIPTION, "");
            props.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
            Repository.getServiceRegistry(fc).getCheckOutCheckInService().checkin(new NodeRef(strNodeRef), props);
            out.write("OK: " + strNodeRef);
         }
         catch (Throwable err)
         {
            out.write("ERROR: " + err.getMessage());
         }
      }
   }
}