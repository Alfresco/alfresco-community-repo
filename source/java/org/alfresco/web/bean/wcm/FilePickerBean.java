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
 * http://www.alfresco.com/legal/licensing" 
 */
package org.alfresco.web.bean.wcm;

import java.io.*;
import java.util.*;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.app.servlet.ajax.InvokeCommand;
import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.forms.*;
import org.alfresco.web.ui.common.Utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.FileCopyUtils;

import org.w3c.dom.*;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.ls.*;
import org.xml.sax.SAXException;

/**
 * Bean for interacting with the file picker widget using ajax requests.
 */
public class FilePickerBean
{
   private static final Log LOGGER = LogFactory.getLog(FilePickerBean.class);
   private final Set<NodeRef> uploads = new HashSet<NodeRef>();

   private AVMBrowseBean avmBrowseBean;
   private AVMService avmService;

   public FilePickerBean()
   {
   }

   public void clearUploadedFiles()
   {
      this.uploads.clear();
   }

   public NodeRef[] getUploadedFiles()
   {
      return (NodeRef[])this.uploads.toArray(new NodeRef[this.uploads.size()]);
   }
   
   /**
    * @param avmBrowseBean the avmBrowseBean to set.
    */
   public void setAvmBrowseBean(final AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   /**
    * @param avmService the avmService to set.
    */
   public void setAvmService(final AVMService avmService)
   {
      this.avmService = avmService;
   }

   /**
    * Provides data for a file picker widget.
    */
   @InvokeCommand.ResponseMimetype(value=MimetypeMap.MIMETYPE_XML)
   public void getFilePickerData()
      throws Exception
   {
      final FacesContext facesContext = FacesContext.getCurrentInstance();
      final ExternalContext externalContext = facesContext.getExternalContext();

      final Map requestParameters = externalContext.getRequestParameterMap();
      String currentPath = (String)requestParameters.get("currentPath");
      if (currentPath == null)
      {
         currentPath = this.getCurrentAVMPath();
      }
      else
      {
         final String previewStorePath = 
            AVMUtil.getCorrespondingPathInPreviewStore(this.getCurrentAVMPath());
         currentPath = AVMUtil.buildPath(previewStorePath,
                                              currentPath,
                                              AVMUtil.PathRelation.WEBAPP_RELATIVE);
      }
      LOGGER.debug(this + ".getFilePickerData(" + currentPath + ")");

      final Document result = XMLUtil.newDocument();
      final Element filePickerDataElement = result.createElement("file-picker-data");
      result.appendChild(filePickerDataElement);


      final AVMNodeDescriptor currentNode = this.avmService.lookup(-1, currentPath);
      if (currentNode == null)
      {
         final Element errorElement = result.createElement("error");
         errorElement.appendChild(result.createTextNode("Path " + currentPath + " not found"));
         filePickerDataElement.appendChild(errorElement);
         currentPath = this.getCurrentAVMPath();
      }
      else if (! currentNode.isDirectory())
      {
         currentPath = AVMNodeConverter.SplitBase(currentPath)[0];
      }
      
      Element e = result.createElement("current-node");
      e.setAttribute("avmPath", currentPath);
      e.setAttribute("webappRelativePath", 
                     AVMUtil.getWebappRelativePath(currentPath));
      e.setAttribute("type", "directory");
      e.setAttribute("image", "/images/icons/space_small.gif");
      filePickerDataElement.appendChild(e);

      for (Map.Entry<String, AVMNodeDescriptor> entry : 
              this.avmService.getDirectoryListing(-1, currentPath).entrySet())
      {
         e = result.createElement("child-node");
         e.setAttribute("avmPath", entry.getValue().getPath());
         e.setAttribute("webappRelativePath", 
                        AVMUtil.getWebappRelativePath(entry.getValue().getPath()));
         e.setAttribute("type", entry.getValue().isDirectory() ? "directory" : "file");
         e.setAttribute("image", (entry.getValue().isDirectory()
                                  ? "/images/icons/space_small.gif"
                                  : Utils.getFileTypeImage(facesContext, 
                                                           entry.getValue().getName(),
                                                           true)));
         filePickerDataElement.appendChild(e);
      }

      final ResponseWriter out = facesContext.getResponseWriter();
      XMLUtil.print(result, out);
      out.close();
   }
   
   @InvokeCommand.ResponseMimetype(value=MimetypeMap.MIMETYPE_HTML)
   public void uploadFile()
      throws Exception
   {
      LOGGER.debug(this + ".uploadFile()");
      final FacesContext facesContext = FacesContext.getCurrentInstance();
      final ExternalContext externalContext = facesContext.getExternalContext();
      final HttpServletRequest request = (HttpServletRequest)
         externalContext.getRequest();

      final ServletFileUpload upload = 
         new ServletFileUpload(new DiskFileItemFactory());
      upload.setHeaderEncoding("UTF-8");
      final List<FileItem> fileItems = upload.parseRequest(request);
      final FileUploadBean bean = new FileUploadBean();
      String uploadId = null;
      String currentPath = null;
      String filename = null;
      String returnPage = null;
      InputStream fileInputStream = null;
      for (FileItem item : fileItems)
      {
         LOGGER.debug("item = " + item);
         if (item.isFormField() && item.getFieldName().equals("upload-id"))
         {
            uploadId = item.getString();
            LOGGER.debug("uploadId is " + uploadId);
         }
         if (item.isFormField() && item.getFieldName().equals("return-page"))
         {
            returnPage = item.getString();
            LOGGER.debug("returnPage is " + returnPage);
         }
         else if (item.isFormField() && item.getFieldName().equals("currentPath"))
         {
            final String previewStorePath = 
               AVMUtil.getCorrespondingPathInPreviewStore(this.getCurrentAVMPath());
            currentPath = AVMUtil.buildPath(previewStorePath,
                                                 item.getString(),
                                                 AVMUtil.PathRelation.WEBAPP_RELATIVE);
            LOGGER.debug("currentPath is " + currentPath);
         }
         else
         {
            filename = FilenameUtils.getName(item.getName());
            fileInputStream = item.getInputStream();
            LOGGER.debug("uploading file " + filename);
         }
      }

      LOGGER.debug("saving file " + filename + " to " + currentPath);
      
      try
      {
         FileCopyUtils.copy(fileInputStream, 
                            this.avmService.createFile(currentPath, filename));
         final Map<QName, PropertyValue> props = new HashMap<QName, PropertyValue>(1, 1.0f);
         props.put(ContentModel.PROP_TITLE, new PropertyValue(DataTypeDefinition.TEXT, filename));
//         props.put(ContentModel.PROP_DESCRIPTION, 
//                   new PropertyValue(DataTypeDefinition.TEXT,
//                                     "Uploaded for form " + this.xformsSession.getForm().getName()));
         this.avmService.setNodeProperties(currentPath + "/" + filename, props);
         this.avmService.addAspect(currentPath + "/" + filename, ContentModel.ASPECT_TITLED); 
         
         this.uploads.add(AVMNodeConverter.ToNodeRef(-1, currentPath + "/" + filename));
         returnPage = returnPage.replace("${_FILE_TYPE_IMAGE}",
                                         Utils.getFileTypeImage(facesContext, filename, true));
      }
      catch (Exception e)
      {
         LOGGER.debug(e.getMessage(), e);
         returnPage = returnPage.replace("${_UPLOAD_ERROR}", e.getMessage());
      }

      LOGGER.debug("upload complete.  sending response: " + returnPage);
      final Document result = XMLUtil.newDocument();
      final Element htmlEl = result.createElement("html");
      result.appendChild(htmlEl);
      final Element bodyEl = result.createElement("body");
      htmlEl.appendChild(bodyEl);

      final Element scriptEl = result.createElement("script");
      bodyEl.appendChild(scriptEl);
      scriptEl.setAttribute("type", "text/javascript");
      final Node scriptText = result.createTextNode(returnPage);
      scriptEl.appendChild(scriptText);

      final ResponseWriter out = facesContext.getResponseWriter();
      XMLUtil.print(result, out);
      out.close();
   }

   private String getCurrentAVMPath()
   {
      AVMNode node = this.avmBrowseBean.getAvmActionNode();
      if (node == null)
      {
         return this.avmBrowseBean.getCurrentPath();
      }

      final String result = node.getPath();
      return node.isDirectory() ? result : AVMNodeConverter.SplitBase(result)[0];
   }
}