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
import java.util.regex.Pattern;
import java.text.MessageFormat;
import java.util.*;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
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
import org.apache.commons.lang.StringUtils;
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
   private NamespaceService namespaceService;

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
    * @param namespaceService the namespaceService to set.
    */
   public void setNamespaceService(final NamespaceService namespaceService)
   {
      this.namespaceService = namespaceService;
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

      String currentPath = (String)externalContext.getRequestParameterMap().get("currentPath");
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
 
      final QName[] selectableTypes = 
         this.getSelectableTypes((String[])externalContext.getRequestParameterValuesMap().get("selectableTypes"));
      final Pattern[] filterMimetypes = 
         this.getFilterMimetypes((String[])externalContext.getRequestParameterValuesMap().get("filterMimetypes"));
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug(this + ".getFilePickerData(path = " + currentPath + 
                      ", selectableTypes = [" + StringUtils.join(selectableTypes, ",") +
                      "], filterMimetypes = [" + StringUtils.join(filterMimetypes, ",") +
                      "])");
      }

      final Document result = XMLUtil.newDocument();
      final Element filePickerDataElement = result.createElement("file-picker-data");
      result.appendChild(filePickerDataElement);


      final AVMNodeDescriptor currentNode = this.avmService.lookup(-1, currentPath);
      if (currentNode == null)
      {
         currentPath = AVMUtil.getWebappRelativePath(currentPath);
         
         filePickerDataElement.setAttribute("error", 
                                            MessageFormat.format(Application.getMessage(facesContext, "error_not_found"),
                                                                 currentPath.substring(currentPath.lastIndexOf("/") + 1, 
                                                                                       currentPath.length()),
                                                                 (currentPath.lastIndexOf("/") == 0 
                                                                  ? "/" 
                                                                  : currentPath.substring(0, currentPath.lastIndexOf("/")))));
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

      for (final Map.Entry<String, AVMNodeDescriptor> entry : 
              this.avmService.getDirectoryListing(-1, currentPath).entrySet())
      {
         if (!entry.getValue().isDirectory() && filterMimetypes.length != 0)
         {
            final String contentMimetype = this.avmService.getContentDataForRead(entry.getValue()).getMimetype();
            boolean matched = false;
            for (final Pattern p : filterMimetypes)
            {
               matched = p.matcher(contentMimetype).matches();
               if (LOGGER.isDebugEnabled())
               {
                  LOGGER.debug(p + ".matches(" + contentMimetype + ") = " + matched);
               }
               if (matched)
               {
                  break;
               }
            }
            if (!matched)
            {
               continue;
            }
         }
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

         boolean selectable = false;
         // faking this for now since i can't figure out how to efficiently get the type
         // qname from the avmservice
         for (final QName typeQName : selectableTypes)
         {
            selectable = selectable || (WCMModel.TYPE_AVM_FOLDER.equals(typeQName) && 
                                        entry.getValue().isDirectory());
            selectable = selectable || (WCMModel.TYPE_AVM_CONTENT.equals(typeQName) && 
                                        !entry.getValue().isDirectory());
         }
         e.setAttribute("selectable", Boolean.toString(selectable));
         filePickerDataElement.appendChild(e);
      }

      final ResponseWriter out = facesContext.getResponseWriter();
      XMLUtil.print(result, out);
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
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("item = " + item);
         }
         if (item.isFormField() && item.getFieldName().equals("upload-id"))
         {
            uploadId = item.getString();
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("uploadId is " + uploadId);
            }
         }
         if (item.isFormField() && item.getFieldName().equals("return-page"))
         {
            returnPage = item.getString();
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("returnPage is " + returnPage);
            }
         }
         else if (item.isFormField() && item.getFieldName().equals("currentPath"))
         {
            final String previewStorePath = 
               AVMUtil.getCorrespondingPathInPreviewStore(this.getCurrentAVMPath());
            currentPath = AVMUtil.buildPath(previewStorePath,
                                                 item.getString(),
                                                 AVMUtil.PathRelation.WEBAPP_RELATIVE);
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("currentPath is " + currentPath);
            }
         }
         else
         {
            filename = FilenameUtils.getName(item.getName());
            fileInputStream = item.getInputStream();

            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("uploading file " + filename);
            }
         }
      }

      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("saving file " + filename + " to " + currentPath);
      }
      
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
   }

   private String getCurrentAVMPath()
   {
      final AVMNode node = this.avmBrowseBean.getAvmActionNode();
      if (node == null)
      {
         return this.avmBrowseBean.getCurrentPath();
      }

      final String result = node.getPath();
      return node.isDirectory() ? result : AVMNodeConverter.SplitBase(result)[0];
   }

   private QName[] getSelectableTypes(final String[] selectableTypes)
   {
      final QName[] result = (selectableTypes == null 
                              ? new QName[] { WCMModel.TYPE_AVM_CONTENT, WCMModel.TYPE_AVM_FOLDER } 
                              : new QName[selectableTypes.length]);

      if (selectableTypes != null)
      {
         for (int i = 0; i < selectableTypes.length; i++)
         {
            result[i] = QName.resolveToQName(this.namespaceService, selectableTypes[i]);
         }
      }
      return result;
   }

   private Pattern[] getFilterMimetypes(final String[] filterMimetypes)
   {
      final Pattern[] result = filterMimetypes == null ? new Pattern[0] : new Pattern[filterMimetypes.length];
      if (filterMimetypes != null)
      {
         for (int i = 0; i < filterMimetypes.length; i++)
         {
            result[i] = Pattern.compile(filterMimetypes[i].replaceAll("\\*", "\\.*").replaceAll("\\/", "\\\\/"));
         }
      }
      return result;
   }
}
