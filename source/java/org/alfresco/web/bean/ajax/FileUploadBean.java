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
package org.alfresco.web.bean.ajax;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
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
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.springframework.extensions.surf.util.URLDecoder;
import org.alfresco.web.app.servlet.BaseServlet;
import org.alfresco.web.app.servlet.ajax.InvokeCommand;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.util.XMLUtil;
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
 * Bean managing the ajax servlet upload of a multi-part form containing file content
 * to be added at a specific path in the server. 
 * 
 * @author Kevin Roast
 */
public class FileUploadBean implements Serializable
{
   private static final long serialVersionUID = 4555828718375916674L;
   
   private static Log logger = LogFactory.getLog(FileUploadBean.class);
   
   /**
    * Ajax method to upload file content. A multi-part form is required as the input.
    * 
    * "return-page" = javascript to execute on return from the upload request
    * "currentPath" = the cm:name based server path to upload the content into
    * and the file item content.
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
            currentPath = URLDecoder.decode(item.getString());
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
            NodeRef containerRef = pathToNodeRef(fc, currentPath);
            if (containerRef != null)
            {
               // Guess the mimetype
               String mimetype = Repository.getMimeTypeForFileName(fc, filename);
               
               // Now guess the encoding
               String encoding = "UTF-8";
               InputStream is = null;
               try
               {
                  is = new BufferedInputStream(new FileInputStream(file));
                  encoding = Repository.guessEncoding(fc, is, mimetype);
               }
               catch (Throwable e)
               {
                  // Bad as it is, it's not terminal
                  logger.error("Failed to guess character encoding of file: " + file, e);
               }
               finally
               {
                  if (is != null)
                  {
                     try { is.close(); } catch (Throwable e) {}
                  }
               }
               
               // Try and extract metadata from the file
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
                  title = DefaultTypeConverter.INSTANCE.convert(String.class, contentProps.get(ContentModel.PROP_TITLE));
                  description = DefaultTypeConverter.INSTANCE.convert(String.class, contentProps.get(ContentModel.PROP_DESCRIPTION));
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
               writer.setEncoding(encoding);
               writer.putContent(file);          
            }
         }
      }
      catch (Exception e)
      {
         returnPage = returnPage.replace("${UPLOAD_ERROR}", e.getMessage());
      }
      finally
      {
          if(file != null)
          {
              logger.debug("delete temporary file:" + file.getPath());
              // Delete the temporary file
              file.delete();
          }
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
      {
         logger.debug("File upload request complete.");
      }
      ResponseWriter out = fc.getResponseWriter();
      XMLUtil.print(result, out);
   }
   
   static NodeRef pathToNodeRef(FacesContext fc, String path)
   {
      // convert cm:name based path to a NodeRef
      StringTokenizer t = new StringTokenizer(path, "/");
      int tokenCount = t.countTokens();
      String[] elements = new String[tokenCount];
      for (int i=0; i<tokenCount; i++)
      {
         elements[i] = t.nextToken();
      }
      return BaseServlet.resolveWebDAVPath(fc, elements, false);
   }
}
