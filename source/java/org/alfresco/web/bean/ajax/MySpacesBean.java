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

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.servlet.ajax.InvokeCommand;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.spaces.CreateSpaceWizard;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean backing the ajax requests from the MySpaces portlet webscript.
 * 
 * @author Kevin Roast
 */
public class MySpacesBean
{
   private static Log logger = LogFactory.getLog(MySpacesBean.class);
   
   @InvokeCommand.ResponseMimetype(value=MimetypeMap.MIMETYPE_HTML)
   public void createSpace() throws Exception
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      ResponseWriter out = fc.getResponseWriter();
      
      Map<String, String> requestMap = fc.getExternalContext().getRequestParameterMap();
      String path = (String)requestMap.get("path");
      String name = (String)requestMap.get("name");
      String title = (String)requestMap.get("title");
      String description = (String)requestMap.get("description");
      
      if (logger.isDebugEnabled())
         logger.debug("MySpacesBean.createSpace() path=" + path + " name=" + name +
               " title=" + title + " description=" + description);
      
      try
      {
         if (path != null && name != null)
         {
            NodeRef containerRef = FileUploadBean.pathToNodeRef(fc, path);
            if (containerRef != null)
            {
               NodeService nodeService = Repository.getServiceRegistry(fc).getNodeService();
               FileFolderService ffService = Repository.getServiceRegistry(fc).getFileFolderService();
               FileInfo folderInfo = ffService.create(containerRef, name, ContentModel.TYPE_FOLDER);
               if (logger.isDebugEnabled())
                  logger.debug("Created new folder: " + folderInfo.getNodeRef().toString());
               
               // apply the uifacets aspect - icon, title and description properties
               Map<QName, Serializable> uiFacetsProps = new HashMap<QName, Serializable>(4, 1.0f);
               uiFacetsProps.put(ApplicationModel.PROP_ICON, CreateSpaceWizard.DEFAULT_SPACE_ICON_NAME);
               uiFacetsProps.put(ContentModel.PROP_TITLE, title);
               uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, description);
               nodeService.addAspect(folderInfo.getNodeRef(), ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);
               
               out.write("OK: " + folderInfo.getNodeRef().toString());
            }
         }
      }
      catch (FileExistsException ferr)
      {
         out.write("ERROR: A file with that name already exists.");
      }
      catch (Throwable err)
      {
         out.write("ERROR: " + err.getMessage());
      }
   }
}
