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

import java.io.InputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.servlet.http.HttpServletRequest;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.Pair;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.ajax.InvokeCommand;
import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.forms.XMLUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.springframework.util.FileCopyUtils;

/**
 * Bean for interacting with the file picker widget using ajax requests.
 */
public class FilePickerBean implements Serializable
{
   private static final Log LOGGER = LogFactory.getLog(FilePickerBean.class);

   private static final String CONFIGURED_SEARCH_QUERY_XPATH = "/search/query";
   private static final String CDATA_START_DELIM = "![CDATA[";
   private static final String CDATA_END_DELIM = "]]";

   // parameter names
   private static final String PARAM_FOLDER_RESTRICTION = "folderRestriction";
   private static final String PARAM_CONFIGURED_SEARCH_NAME = "configSearchName";
   private static final String PARAM_SELECTABLE_TYPES = "selectableTypes";
   private static final String PARAM_FILTER_MIME_TYPES = "filterMimetypes";
   private static final String PARAM_CURRENT_PATH = "currentPath";
   private static final String EXTERNAL_PROTOCOL_REGEXP = "^.*:.*";

   private final Set<NodeRef> uploads = new HashSet<NodeRef>();

   // property instance variables
   private AVMBrowseBean avmBrowseBean;
   transient private AVMService avmService;
   transient private NamespaceService namespaceService;
   transient private SearchService searchService;
   transient private NodeService nodeService;
   transient private DictionaryService dictionaryService;
   transient private ContentService contentService;

   // cached reference to the public saved searches folder
   private NodeRef publicSearchesRef = null;

   public FilePickerBean()
   {
   }

   public void clearUploadedFiles()
   {
      this.uploads.clear();
   }

   public NodeRef[] getUploadedFiles()
   {
      return (NodeRef[]) this.uploads.toArray(new NodeRef[this.uploads.size()]);
   }

   /**
    * Set avmBrowseBean property for this bean
    * 
    * @param avmBrowseBean
    *           the AVMBrowseBean object to pass into this property
    */
   public void setAvmBrowseBean(final AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }

   /**
    * Get avmBrowseBean property for this bean
    * 
    * @return avmBrowseBean property value for this bean
    */
   public AVMBrowseBean getAvmBrowseBean()
   {
      return this.avmBrowseBean;
   }

   /**
    * Set avmService property for this bean
    * 
    * @param avmService
    *           the avmService object to pass into this property
    */
   public void setAvmService(final AVMService avmService)
   {
      this.avmService = avmService;
   }

   /**
    * Get avmService property for this bean
    * 
    * @return avmService property value for this bean
    */
   public AVMService getAvmService()
   {
      if (this.avmService == null)
      {
         this.avmService = Repository.getServiceRegistry(
               FacesContext.getCurrentInstance()).getAVMService();
      }
      return this.avmService;
   }

   /**
    * Set nodeService property value for this bean
    * 
    * @param nodeService
    *           the NodeService object to pass into this property
    */
   public void setNodeService(final NodeService nodeService)
   {
      this.nodeService = nodeService;
   }

   /**
    * Get nodeService property for this bean
    * 
    * @return nodeService property value for this bean
    */
   public NodeService getNodeService()
   {
      if (this.nodeService == null)
      {
         this.nodeService = Repository.getServiceRegistry(
               FacesContext.getCurrentInstance()).getNodeService();
      }
      return this.nodeService;
   }

   /**
    * Set searchService property for this bean
    * 
    * @param searchService
    *           the SearchService object to pass into this property
    */
   public void setSearchService(final SearchService searchService)
   {
      this.searchService = searchService;
   }

   /**
    * Get SearchService property for this bean
    * 
    * @return searchService property value for this bean
    */
   public SearchService getSearchService()
   {
      if (this.searchService == null)
      {
         this.searchService = Repository.getServiceRegistry(
               FacesContext.getCurrentInstance()).getSearchService();
      }
      return this.searchService;
   }

   /**
    * Set dictionaryService property value for this bean
    * 
    * @param dictionaryService
    *           the DictionaryService object to pass into this property
    */
   public void setDictionaryService(final DictionaryService dictionaryService)
   {
      this.dictionaryService = dictionaryService;
   }

   /**
    * Get dictionaryService property for this bean
    * 
    * @return dictionaryService property value for this bean
    */
   public DictionaryService getDictionaryService()
   {
      if (this.dictionaryService == null)
      {
         this.dictionaryService = Repository.getServiceRegistry(
               FacesContext.getCurrentInstance()).getDictionaryService();
      }
      return this.dictionaryService;
   }

   /**
    * Set namespaceService property for this bean
    * 
    * @param namespaceService
    *           the NamespaceService object to pass into this property
    */
   public void setNamespaceService(final NamespaceService namespaceService)
   {
      this.namespaceService = namespaceService;
   }

   /**
    * Get the namespaceService property value for this bean
    * 
    * @return namespaceService property value for this bean
    */
   public NamespaceService getNamespaceService()
   {
      if (this.namespaceService == null)
      {
         this.namespaceService = Repository.getServiceRegistry(
               FacesContext.getCurrentInstance()).getNamespaceService();
      }
      return this.namespaceService;
   }

   /**
    * Set contentService property
    * 
    * @param contentService
    *           the ContentService object to pass into this property
    */
   public void setContentService(final ContentService contentService)
   {
      this.contentService = contentService;
   }

   /**
    * Get contentService property value for this bean
    * 
    * @return contentService property value for this bean
    */
   public ContentService getContentService()
   {
      if (this.contentService == null)
      {
         this.contentService = Repository.getServiceRegistry(
               FacesContext.getCurrentInstance()).getContentService();
      }
      return this.contentService;
   }

   /**
    * Provides data for a file picker widget.
    */
   @InvokeCommand.ResponseMimetype(value = MimetypeMap.MIMETYPE_XML)
   public void getFilePickerData() throws Exception
   {
      final FacesContext facesContext = FacesContext.getCurrentInstance();
      final ExternalContext externalContext = facesContext.getExternalContext();

      // get configured search name parameter value
      String configSearchName = null;
      String[] configSearchNameParam = (String[]) externalContext
            .getRequestParameterValuesMap().get(PARAM_CONFIGURED_SEARCH_NAME);
      if ((configSearchNameParam != null)
            && (configSearchNameParam.length != 0))
      {
         configSearchName = configSearchNameParam[0];
      }

      // get selectableTypes parameter value
      final QName[] selectableTypes = this
            .getSelectableTypes((String[]) externalContext
                  .getRequestParameterValuesMap().get(PARAM_SELECTABLE_TYPES));

      // / get filterMimetypes parameter value
      final Pattern[] filterMimetypes = this
            .getFilterMimetypes((String[]) externalContext
                  .getRequestParameterValuesMap().get(PARAM_FILTER_MIME_TYPES));

      // get 'folderRestriction' parameter value
      // expecting a relative AVM folder path to be held in this parameter
      // (relative to web project webapp root)
      String folderPathRestriction = null;
      String[] folderPathRestrictionParam = (String[]) externalContext
            .getRequestParameterValuesMap().get(PARAM_FOLDER_RESTRICTION);
      if ((folderPathRestrictionParam != null)
            && (folderPathRestrictionParam.length != 0))
      {
         folderPathRestriction = folderPathRestrictionParam[0];

         // remove leading '/' or '\' (if present) from path restriction
         if ((folderPathRestriction.charAt(0) == '/')
               || (folderPathRestriction.charAt(0) == '\\'))
         {
            folderPathRestriction = folderPathRestriction.substring(1);
         }
      }

      // ###
      // the following section sets file picker current path (the folder that
      // file picker
      // is opened at when selected/changed in the form)

      String currentPath = null;

      // get current path request parameter
      String currentPathReqParam = (String) externalContext
            .getRequestParameterMap().get(PARAM_CURRENT_PATH);

      // create file picker data XML document to return
      // and append file picker data element to it
      final org.w3c.dom.Document filePickerDataDoc = XMLUtil.newDocument();
      final org.w3c.dom.Element filePickerDataElement = filePickerDataDoc
            .createElement("file-picker-data");
      filePickerDataDoc.appendChild(filePickerDataElement);

      // if current path request parameter null then set current path to the
      // current AVM path
      if (currentPathReqParam == null)
      {
         currentPath = this.getCurrentAVMPath();
      }
      
      // Fix for ALF-3764. We cannot select an external protocol link (i.e. http://example.net by filepicker.
      // Such links should be typed manually in the text box.
      if (currentPathReqParam.matches(EXTERNAL_PROTOCOL_REGEXP))
      {
          currentPath = this.getCurrentAVMPath();
          filePickerDataElement.setAttribute("error", Application.getMessage(facesContext, "error_external_protocol_support"));
      }
      // else set current path to current path request parameter converted to
      // AVM preview
      // store path
      else
      {
         final String previewStorePath = AVMUtil
               .getCorrespondingPathInPreviewStore(this.getCurrentAVMPath());
         currentPath = AVMUtil.buildPath(previewStorePath, currentPathReqParam,
               AVMUtil.PathRelation.WEBAPP_RELATIVE);
      }

      // if folder path restriction (relative path) is set,
      // then calculate the absolute restriction path from the root
      // of the webapp and set the current path to that
      if ((folderPathRestriction != null)
            && (folderPathRestriction.length() != 0))
      {
         currentPath = AVMUtil.getWebappPath(currentPath) + "/" + folderPathRestriction;
      }

      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug(this + ".getFilePickerData(path = " + currentPath
               + ", folderRestriction = "
               + folderPathRestriction 
               + ", selectableTypes = ["
               + StringUtils.join(selectableTypes, ",")
               + "], filterMimetypes = ["
               + StringUtils.join(filterMimetypes, ",") + "])");
      }

      // make sure that there is a node associated with current path
      // if not, set an applicable error message as an attribute on
      // the file picker data element
      final AVMNodeDescriptor currentNode = this.getAvmService().lookup(-1,
            currentPath);

      // if the current node is null (path held in current path variable is
      // invalid), then add an error attribute to the file picker data
      if (currentNode == null)
      {
         currentPath = AVMUtil.getWebappRelativePath(currentPath);

         filePickerDataElement.setAttribute("error", MessageFormat.format(
               Application.getMessage(facesContext, "error_not_found"),
               "'" + currentPath.substring(currentPath.lastIndexOf("/") + 1, currentPath.length()) + "'",
               (currentPath.lastIndexOf("/") == 0 ? "/" : currentPath.substring(
                     0, currentPath.lastIndexOf("/")))));

         // If folder restriction has been set, since the derived
         // current path is invalid just set it to null
         if ((folderPathRestriction != null)
               && (folderPathRestriction.length() != 0))
         {
            currentPath = null;
         } else
         // otherwise folder restriction has not been set, so it should be safe
         // to fall back to setting the current path to the current AVM path
         // (as was the behaviour before the folder restriction feature
         // was added)
         {
            currentPath = this.getCurrentAVMPath();
         }
      }
      // else current node is not null (path held in current path variable is
      // valid)
      else
      {
         // if node for current path points to a file instead of a directory,
         // then make sure that the current path points to
         // just the directory part of the path
         if (!currentNode.isDirectory())
         {
            currentPath = AVMNodeConverter.SplitBase(currentPath)[0];
         }
      }

      // create current-node element representing node for current path
      // and append it to the file picker data element
      org.w3c.dom.Element currentNodeElement = filePickerDataDoc
            .createElement("current-node");
      if (currentPath == null)
      {
         currentNodeElement.setAttribute("avmPath", "");
         currentNodeElement.setAttribute("webappRelativePath", "");
      } else
      {
         currentNodeElement.setAttribute("avmPath", currentPath);
         currentNodeElement.setAttribute("webappRelativePath", AVMUtil
               .getWebappRelativePath(currentPath));
      }
      currentNodeElement.setAttribute("type", "directory");
      currentNodeElement.setAttribute("image", "/images/icons/space_small.gif");

      filePickerDataElement.appendChild(currentNodeElement);

      // if configured search name supplied (i.e. neither null nor empty),
      // then get configured search node matching given name
      // and add the nodes from the search result to the 
      // file-picker-data element
      if ((configSearchName != null) && (configSearchName.length() != 0))
      {
         // get node reference for named configured search
         NodeRef configuredSearchNodeRef = getConfiguredSearches(configSearchName);

         // if configured search node ref is null, then there is no
         // configured search matching the name in the 'config search name'
         // parameter, so add error message as attribute to file-picker-data
         // element
         if (configuredSearchNodeRef == null)
         {
            filePickerDataElement.setAttribute("error", MessageFormat.format(
                  Application.getMessage(facesContext, "error_search_not_exist"),
                  configSearchName));
         }
         else
         // else node ref for named configured search is not null, then
         // add content nodes from search results as child elements of 
         // the file picker data element.
         {
            try
            {
               addSearchResultNodes(filePickerDataDoc, filePickerDataElement,
                     configuredSearchNodeRef, selectableTypes, facesContext);
            }
            // if searcher exception thrown whilst getting search results,
            // then add error message as attribute to file-picker-data element
            catch (SearcherException e)
            {
               filePickerDataElement.setAttribute("error", MessageFormat.format(
                     Application.getMessage(facesContext, "error_retrieving_search_results"),
                     configSearchName, e.getMessage()));
            }
         }
      }
      else
      {
         // add elements for child nodes of current path to file picker
         // data element if current path is not null
         if (currentPath != null)
         {
            addPathChildNodesToElement(filePickerDataDoc,
                  filePickerDataElement, currentPath, selectableTypes,
                  filterMimetypes, facesContext);
         }
      }

      final ResponseWriter out = facesContext.getResponseWriter();
      XMLUtil.print(filePickerDataDoc, out);
   }

   @InvokeCommand.ResponseMimetype(value = MimetypeMap.MIMETYPE_HTML)
   public void uploadFile() throws Exception
   {
      LOGGER.debug(this + ".uploadFile()");
      final FacesContext facesContext = FacesContext.getCurrentInstance();
      final ExternalContext externalContext = facesContext.getExternalContext();
      final HttpServletRequest request = (HttpServletRequest) externalContext
            .getRequest();

      final ServletFileUpload upload = new ServletFileUpload(
            new DiskFileItemFactory());
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
         } else if (item.isFormField()
               && item.getFieldName().equals("currentPath"))
         {
            final String previewStorePath = AVMUtil
                  .getCorrespondingPathInPreviewStore(this.getCurrentAVMPath());
            currentPath = AVMUtil.buildPath(previewStorePath, item.getString(),
                  AVMUtil.PathRelation.WEBAPP_RELATIVE);
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("currentPath is " + currentPath);
            }
         } else
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
         FileCopyUtils.copy(fileInputStream, this.getAvmService().createFile(
               currentPath, filename));
         final Map<QName, PropertyValue> props = new HashMap<QName, PropertyValue>(
               1, 1.0f);
         props.put(ContentModel.PROP_TITLE, new PropertyValue(
               DataTypeDefinition.TEXT, filename));
         // props.put(ContentModel.PROP_DESCRIPTION,
         // new PropertyValue(DataTypeDefinition.TEXT,
         // "Uploaded for form " + this.xformsSession.getForm().getName()));
         this.getAvmService().setNodeProperties(currentPath + "/" + filename,
               props);
         this.getAvmService().addAspect(currentPath + "/" + filename,
               ContentModel.ASPECT_TITLED);

         this.uploads.add(AVMNodeConverter.ToNodeRef(-1, currentPath + "/"
               + filename));
         returnPage = returnPage.replace("${_FILE_TYPE_IMAGE}", org.alfresco.repo.web.scripts.FileTypeImageUtils
               .getFileTypeImage(facesContext, filename, true));
      } catch (Exception e)
      {
         LOGGER.debug(e.getMessage(), e);
         returnPage = returnPage.replace("${_UPLOAD_ERROR}", e.getMessage());
      }

      LOGGER.debug("upload complete.  sending response: " + returnPage);
      final org.w3c.dom.Document result = XMLUtil.newDocument();
      final org.w3c.dom.Element htmlEl = result.createElement("html");
      result.appendChild(htmlEl);
      final org.w3c.dom.Element bodyEl = result.createElement("body");
      htmlEl.appendChild(bodyEl);

      final org.w3c.dom.Element scriptEl = result.createElement("script");
      bodyEl.appendChild(scriptEl);
      scriptEl.setAttribute("type", "text/javascript");
      final org.w3c.dom.Node scriptText = result.createTextNode(returnPage);
      scriptEl.appendChild(scriptText);

      final ResponseWriter out = facesContext.getResponseWriter();
      XMLUtil.print(result, out);
   }

   private String getCurrentAVMPath()
   {
      final AVMNode node = this.getAvmBrowseBean().getAvmActionNode();
      if (node == null)
      {
         return this.getAvmBrowseBean().getCurrentPath();
      }

      final String result = node.getPath();
      return node.isDirectory() ? result
            : AVMNodeConverter.SplitBase(result)[0];
   }

   private QName[] getSelectableTypes(final String[] selectableTypes)
   {
      final QName[] result = (selectableTypes == null ? new QName[]
      { WCMModel.TYPE_AVM_CONTENT, WCMModel.TYPE_AVM_FOLDER }
            : new QName[selectableTypes.length]);

      if (selectableTypes != null)
      {
         for (int i = 0; i < selectableTypes.length; i++)
         {
            result[i] = QName.resolveToQName(this.getNamespaceService(),
                  selectableTypes[i]);
         }
      }
      return result;
   }

   private Pattern[] getFilterMimetypes(final String[] filterMimetypes)
   {
      final Pattern[] result = filterMimetypes == null ? new Pattern[0]
            : new Pattern[filterMimetypes.length];
      if (filterMimetypes != null)
      {
         for (int i = 0; i < filterMimetypes.length; i++)
         {
            result[i] = Pattern.compile(filterMimetypes[i].replaceAll("\\*",
                  "\\.*").replaceAll("\\/", "\\\\/"));
         }
      }
      return result;
   }

   /**
    * Add child nodes of supplied path to given parent element. Directory
    * listing is done on given path. Elements representing the child nodes
    * returned in the directory listing are added to the supplied parent
    * element.
    * 
    * @param doc
    *           XML document to which the parent node belongs
    * @param parent
    *           parent element to add given nodes to as child elements
    * @param path
    *           path from which to extract child nodes
    * @param selectableTypes
    *           array of types which are the only ones that should be selectable
    *           in the file picker
    * @param filterMimetypes
    *           array of MIME type patterns used to filter out child nodes
    *           extracted from the given path which don't match the given MIME
    *           type patterns
    * @param facesContext
    *           faces context used to set image attribute on each child element
    */
   private void addPathChildNodesToElement(org.w3c.dom.Document doc,
         org.w3c.dom.Element parent, String path, QName[] selectableTypes,
         Pattern[] filterMimetypes, FacesContext facesContext)
   {
      // append elements for the child AVM nodes of the current path
      // to parent element
      for (final Map.Entry<String, AVMNodeDescriptor> entry : this
            .getAvmService().getDirectoryListing(-1, path).entrySet())
      {
         // if AVM node is a content node and the filter MIME types parameter
         // has been set, then only add child element for AVM node if it matches
         // one of the specified MIME types in the parameter
         if (!entry.getValue().isDirectory() && filterMimetypes.length != 0)
         {
            final String contentMimetype = this.getAvmService()
                  .getContentDataForRead(entry.getValue()).getMimetype();

            boolean matched = false;
            for (final Pattern p : filterMimetypes)
            {
               matched = p.matcher(contentMimetype).matches();
               if (LOGGER.isDebugEnabled())
               {
                  LOGGER.debug(p + ".matches(" + contentMimetype + ") = "
                        + matched);
               }
               if (matched)
               {
                  break;
               }
            }

            // if AVM node MIME type doesn't match any of the types in the
            // filter MIME types parameter, then don't do any further processing
            // on it and jump back to the start of the AVM node traversal loop
            if (!matched)
            {
               continue;
            }
         }

         // create child element representing AVM node and add to file picker
         // data element
         addAVMChildNodeToParentElement(doc, parent, entry.getValue(),
               selectableTypes, facesContext);
      }
   }

   /**
    * Add result content nodes from configured search as 
    * child node elements to the provided parent element
    * 
    * @param doc
    *           XML document to which the supplied parent node belongs
    * @param parent
    *           parent element to which to add search result content nodes 
    *           as child elements
    * @param configuredSearchNodeRef
    *           configured search node reference for which result content nodes 
    *           are added as child elements to the provided parent element
    * @param selectableTypes
    *           node types which must be marked as selectable in the file
    *           picker
    * @param facesContext
    *           faces context used to set image attribute on each child element
    */
   private void addSearchResultNodes(org.w3c.dom.Document doc,
         org.w3c.dom.Element parent, NodeRef configuredSearchNodeRef,
         QName[] selectableTypes, FacesContext facesContext)
   {
      // run configured search to get content nodes returned in search result
      List<AVMNodeDescriptor> searchResultNodes = runConfiguredSearch(configuredSearchNodeRef);
      
      // if there are no search results (i.e. null) throw exception
      if (searchResultNodes == null)
      {
         throw new SearcherException("No results returned by search query.\n"
                     + "Search node reference: " + configuredSearchNodeRef);
      }

      for (AVMNodeDescriptor node : searchResultNodes)
      {
         // create child element representing AVM node and add to file picker
         // data element
         addAVMChildNodeToParentElement(doc, parent, node, selectableTypes,
               facesContext);
      }
   }

   /**
    * Run a configured search represented by the given node reference, against
    * the web project the XForm is currently within
    * 
    * @param configuredQueryNodRef
    *           NodeRef of the configured query with which to run the search
    * 
    * @return content nodes returned by the search
    */
   private List<AVMNodeDescriptor> runConfiguredSearch(
         NodeRef configSearchNodeRef)
   {
      // get the store id used to run the configured search query
      WebProject webProject = this.getAvmBrowseBean().getWebProject();
      String storeID = webProject.getStoreId();

      // extract the content of configured search node to XML document
      ContentReader contentReader = getContentService().getReader(
            configSearchNodeRef, ContentModel.PROP_CONTENT);
      InputStream queryInpStream = contentReader.getContentInputStream();
      SAXReader reader = new SAXReader();
      Document queryDoc = null;
      try
      {
         queryDoc = reader.read(queryInpStream);
      } catch (DocumentException de)
      {
         // ignore exception and return null
         return null;
      }

      // extract search query from configured search XML document
      String query = null;
      XPath queryXPath = DocumentHelper
            .createXPath(CONFIGURED_SEARCH_QUERY_XPATH);
      List xpathResult = queryXPath.selectNodes(queryDoc);
      if ((xpathResult != null) && (xpathResult.size() != 0))
      {
         // get the text from the query element
         Element queryElement = (Element) xpathResult.get(0);
         String queryElemText = queryElement.getText();

         // now extract the actual search query string from the CDATA section
         // within that text

         int cdataStartDelimIndex = queryElemText.indexOf(CDATA_START_DELIM);
         int cdataEndDelimIndex = queryElemText.indexOf(CDATA_END_DELIM);

         // if the CDATA start delimiter is found in the query element text
         // && there is text between the CDATA start and end delimiters then
         // extract
         // the query string from the CDATA section
         if ((cdataStartDelimIndex > -1)
               && ((cdataStartDelimIndex + CDATA_START_DELIM.length()) < cdataEndDelimIndex))
         {
            query = queryElemText.substring(cdataStartDelimIndex
                  + CDATA_START_DELIM.length(), cdataEndDelimIndex);
         }
         else
         {
            // otherwise just use the text as is
            query = queryElemText;
         }
      }

      // perform the search against the repository
      // if query was extracted from the configured search successfully
      // (extracted query non-null)
      List<AVMNodeDescriptor> resultNodeDescriptors = null;
      if ((query != null) && (query.length() != 0))
      {
         ResultSet results = null;
         try
         {
            results = this.getSearchService().query(
                  new StoreRef(StoreRef.PROTOCOL_AVM, storeID),
                  SearchService.LANGUAGE_LUCENE, query);

            if (results.length() != 0)
            {
               resultNodeDescriptors = new ArrayList<AVMNodeDescriptor>();
               for (int i = 0; i < results.length(); i++)
               {
                  ResultSetRow row = results.getRow(i);
                  NodeRef resultNodeRef = row.getNodeRef();
                  Node resultNode = new Node(resultNodeRef);

                  // only add content type nodes to the search result
                  // as we don't want the user to navigate down into folders
                  // in the search results
                  if (getDictionaryService().isSubClass(resultNode.getType(),
                        ContentModel.TYPE_CONTENT))
                  {
                     Pair<Integer, String> pair = AVMNodeConverter
                           .ToAVMVersionPath(resultNodeRef);
                     Integer version = pair.getFirst();
                     String path = pair.getSecond();
                     resultNodeDescriptors.add(getAvmService().lookup(version,
                           path));
                  }
               }
            }
         } catch (Throwable err)
         {
            throw new AlfrescoRuntimeException("Failed to execute search: "
                  + query, err);
         } finally
         {
            if (results != null)
            {
               results.close();
            }
         }
      }

      return resultNodeDescriptors;
   }

   /**
    * Get the cached reference to the public saved searches folder. This method
    * will first get a reference to the public saved searches folder and assign
    * it to the cached reference if is it is null.
    * 
    * @return the cached reference to the public Saved Searches folder
    */
   private NodeRef getPublicSearchesRef()
   {
      // if the cached reference is null, then get a reference to the
      // public saved searches folder to assign to it
      if (publicSearchesRef == null)
      {
         // Use the search service get a reference to the
         // public saved searches folder.
         FacesContext fc = FacesContext.getCurrentInstance();
         String xpath = Application.getRootPath(fc) + "/"
               + Application.getGlossaryFolderName(fc) + "/"
               + Application.getSavedSearchesFolderName(fc);

         List<NodeRef> results = null;
         try
         {
            results = getSearchService().selectNodes(
                  getNodeService().getRootNode(Repository.getStoreRef()),
                  xpath, null, getNamespaceService(), false);
         } catch (AccessDeniedException err)
         {
            // ignore and return null
         }

         if (results != null && results.size() != 0)
         {
            publicSearchesRef = results.get(0);
         }
      }

      return publicSearchesRef;
   }

   /**
    * Get node for configured search by name.
    * 
    * @param configSearchName
    *           name of configured search for which to get node
    * @return node reference for configured search
    */
   public NodeRef getConfiguredSearches(String configSearchName)
   {
      NodeRef configSearchNodeRef = null;

      // get the folder reference from the
      // public searches location
      NodeRef publicSearchesFolderRef = getPublicSearchesRef();

      // read the content nodes under the folder
      List<ChildAssociationRef> childRefs = getNodeService().getChildAssocs(
            publicSearchesFolderRef, ContentModel.ASSOC_CONTAINS,
            RegexQNamePattern.MATCH_ALL);

      // return content node with name matching given configured search name
      if (childRefs.size() != 0)
      {
         for (ChildAssociationRef ref : childRefs)
         {
            NodeRef childNodeRef = ref.getChildRef();
            Node childNode = new Node(childNodeRef);
            if (getDictionaryService().isSubClass(childNode.getType(),
                  ContentModel.TYPE_CONTENT))
            {
               String childNodeName = childNode.getName();
               if (childNodeName.equals(configSearchName))
               {
                  configSearchNodeRef = childNodeRef;
                  break;
               }
            }
         }
      }

      return configSearchNodeRef;
   }

   /**
    * Create child element representing given AVM node and add to given parent
    * element
    * 
    * @param doc
    *           Document to which given parent element belongs
    * @param parent
    *           parent element to add AVM node to as child element
    * @param node
    *           AVM node to add as child node to given parent
    * @param selectableTypes
    *           AVM node types which must be marked as selectable
    * @param facesContent
    *           Faces context used to get file-type icon for given AVM node
    */
   private void addAVMChildNodeToParentElement(org.w3c.dom.Document doc,
         org.w3c.dom.Element parent, AVMNodeDescriptor node,
         QName[] selectableTypes, FacesContext facesContext)
   {
      // create child node element to add to file picker data
      org.w3c.dom.Element childNodeElement = doc.createElement("child-node");
      childNodeElement.setAttribute("avmPath", node.getPath());
      childNodeElement.setAttribute("webappRelativePath", AVMUtil
            .getWebappRelativePath(node.getPath()));
      childNodeElement.setAttribute("type", node.isDirectory() ? "directory"
            : "file");

      // Set image attribute on each child
      // TODO (Glen): IS this the right image to set?
      // originally from Ariel's code
      childNodeElement.setAttribute("image",
            (node.isDirectory() ? "/images/icons/space_small.gif" : org.alfresco.repo.web.scripts.FileTypeImageUtils
                  .getFileTypeImage(facesContext, node.getName(), true)));

      boolean selectable = false;

      // set 'selectable' attribute on child node to mark whether node should
      // be selectable in file picker or not
      //
      // TODO Ariel: faking this for now since i can't figure out how to
      // efficiently get the type
      // qname from the avmservice
      for (final QName typeQName : selectableTypes)
      {
         selectable = selectable
               || (WCMModel.TYPE_AVM_FOLDER.equals(typeQName) && node
                     .isDirectory());
         selectable = selectable
               || (WCMModel.TYPE_AVM_CONTENT.equals(typeQName) && !node
                     .isDirectory());
      }
      childNodeElement.setAttribute("selectable", Boolean.toString(selectable));

      // append child node element to parent
      parent.appendChild(childNodeElement);
   }
}
