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

import java.io.File;
import java.io.Serializable;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.servlet.http.HttpServletRequest;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.alfresco.web.app.servlet.BaseServlet;
import org.alfresco.web.app.servlet.ajax.InvokeCommand;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.forms.XMLUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Kevin Roast
 */
public class FileUploadBean
{
   private static Log logger = LogFactory.getLog(FileUploadBean.class);
   
   /**
    * Ajax method to upload a file. A multi-part form is required as the input.
    * 
    * "return-page" = 
    * "currentPath" = 
    * and the file item itself
    * 
    * @throws Exception
    */
   @InvokeCommand.ResponseMimetype(value=MimetypeMap.MIMETYPE_HTML)
   public void uploadFile() throws Exception
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      ExternalContext externalContext = fc.getExternalContext();
      HttpServletRequest request = (HttpServletRequest)externalContext.getRequest();
      
      ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
      upload.setHeaderEncoding("UTF-8");
      
      List<FileItem> fileItems = upload.parseRequest(request);
      FileUploadBean bean = new FileUploadBean();
      String currentPath = null;
      String filename = null;
      String returnPage = null;
      File file = null;
      
      for (FileItem item : fileItems)
      {
         if (item.isFormField() && item.getFieldName().equals("return-page"))
         {
            returnPage = item.getString();
         }
         else if (item.isFormField() && item.getFieldName().equals("currentPath"))
         {
            currentPath = URLDecoder.decode(item.getString(), "UTF-8");
         }
         else
         {
            filename = FilenameUtils.getName(item.getName());
            file = TempFileProvider.createTempFile("alfresco", ".upload");
            item.write(file);
         }
      }
      
      if (logger.isDebugEnabled())
         logger.debug("Ajax file upload request: " + filename + " to path: " + currentPath + " return page: " + returnPage);
      
      try
      {
         if (file != null && currentPath != null && currentPath.length() != 0)
         {
            // convert cm:name based path to a NodeRef
            StringTokenizer t = new StringTokenizer(currentPath, "/");
            int tokenCount = t.countTokens();
            String[] elements = new String[tokenCount];
            for (int i=0; i<tokenCount; i++)
            {
               elements[i] = t.nextToken();
            }
            
            NodeRef containerRef = BaseServlet.resolveWebDAVPath(fc, elements, false);
            
            if (containerRef != null)
            {
               // Try and extract metadata from the file
               String mimetype = Repository.getMimeTypeForFileName(fc, filename);
               ContentReader cr = new FileContentReader(file);
               cr.setMimetype(mimetype);
               
               // create properties for content type
               String author = null;
               String title = null;
               String description = null;
               Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>(5, 1.0f);
               if (Repository.extractMetadata(fc, cr, contentProps))
               {
                  author = (String)(contentProps.get(ContentModel.PROP_AUTHOR));
                  title = (String)(contentProps.get(ContentModel.PROP_TITLE));
                  description = (String)(contentProps.get(ContentModel.PROP_DESCRIPTION));
               }
               
               // default the title to the file name if not set
               if (title == null)
               {
                  title = filename;
               }
               
               ServiceRegistry services = Repository.getServiceRegistry(fc);
               FileInfo fileInfo = services.getFileFolderService().create(
                     containerRef, filename, ContentModel.TYPE_CONTENT);
               NodeRef fileNodeRef = fileInfo.getNodeRef();
               
               // set the author aspect
               if (author != null)
               {
                  Map<QName, Serializable> authorProps = new HashMap<QName, Serializable>(1, 1.0f);
                  authorProps.put(ContentModel.PROP_AUTHOR, author);
                  services.getNodeService().addAspect(fileNodeRef, ContentModel.ASPECT_AUTHOR, authorProps);
               }
               
               // apply the titled aspect - title and description
               Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(2, 1.0f);
               titledProps.put(ContentModel.PROP_TITLE, title);
               titledProps.put(ContentModel.PROP_DESCRIPTION, description);
               services.getNodeService().addAspect(fileNodeRef, ContentModel.ASPECT_TITLED, titledProps);
               
               // get a writer for the content and put the file
               ContentWriter writer = services.getContentService().getWriter(fileNodeRef, ContentModel.PROP_CONTENT, true);
               writer.setMimetype(mimetype);
               writer.setEncoding("UTF-8");
               writer.putContent(file);
            }
         }
      }
      catch (Exception e)
      {
         returnPage = returnPage.replace("${UPLOAD_ERROR}", e.getMessage());
      }

      Document result = XMLUtil.newDocument();
      Element htmlEl = result.createElement("html");
      result.appendChild(htmlEl);
      Element bodyEl = result.createElement("body");
      htmlEl.appendChild(bodyEl);

      Element scriptEl = result.createElement("script");
      bodyEl.appendChild(scriptEl);
      scriptEl.setAttribute("type", "text/javascript");
      Node scriptText = result.createTextNode(returnPage);
      scriptEl.appendChild(scriptText);

      if (logger.isDebugEnabled())
         logger.debug("File upload request complete.");
      
      ResponseWriter out = fc.getResponseWriter();
      XMLUtil.print(result, out);
      out.close();
   }
}
