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
import org.alfresco.util.ISO9075;
import org.alfresco.util.Pair;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.ajax.InvokeCommand;
import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.forms.XMLUtil;
import org.alfresco.web.ui.common.Utils;
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
public class FilePickerBean implements Serializable {
   private static final Log LOGGER = LogFactory.getLog(FilePickerBean.class);

   private static final String SAVED_SEARCH_QUERY_XPATH = "/search/query";
   private static final String CDATA_START_DELIM = "![CDATA[";
   private static final String CDATA_END_DELIM = "]]";

   // possible values for the saved_search_context parameter
   private static final String SAVED_SEARCHES_CONTEXT_USER = "user";
   private static final String SAVED_SEARCHES_CONTEXT_GLOBAL = "global";

   // parameter names
   private static final String PARAM_FOLDER_RESTRICTION = "folderRestriction";
   private static final String PARAM_SAVED_SEARCH_NAME = "savedSearchName";
   private static final String PARAM_SAVED_SEARCH_CONTEXT = "savedSearchContext";
   private static final String PARAM_SELECTABLE_TYPES = "selectableTypes";
   private static final String PARAM_FILTER_MIME_TYPES = "filterMimetypes";
   private static final String PARAM_CURRENT_PATH = "currentPath";

   private final Set<NodeRef> uploads = new HashSet<NodeRef>();

   // property instance variables
   private AVMBrowseBean avmBrowseBean;
   transient private AVMService avmService;
   transient private NamespaceService namespaceService;
   transient private SearchService searchService;
   transient private NodeService nodeService;
   transient private DictionaryService dictionaryService;
   transient private ContentService contentService;

   /** cached reference to the global saved searches folder */
   private NodeRef globalSearchesRef = null;

   /** cached reference to the current users saved searches folder */
   private NodeRef userSearchesRef = null;

   public FilePickerBean() {
   }

   public void clearUploadedFiles() {
      this.uploads.clear();
   }

   public NodeRef[] getUploadedFiles() {
      return (NodeRef[]) this.uploads.toArray(new NodeRef[this.uploads.size()]);
   }

   /**
    * Set avmBrowseBean property for this bean
    * 
    * @param avmBrowseBean
    *           the AVMBrowseBean object to pass into this property
    */
   public void setAvmBrowseBean(final AVMBrowseBean avmBrowseBean) {
      this.avmBrowseBean = avmBrowseBean;
   }

   /**
    * Get avmBrowseBean property for this bean
    * 
    * @return avmBrowseBean property value for this bean
    */
   public AVMBrowseBean getAvmBrowseBean() {
      return this.avmBrowseBean;
   }

   /**
    * Set avmService property for this bean
    * 
    * @param avmService
    *           the avmService object to pass into this property
    */
   public void setAvmService(final AVMService avmService) {
      this.avmService = avmService;
   }

   /**
    * Get avmService property for this bean
    * 
    * @return avmService property value for this bean
    */
   public AVMService getAvmService() {
      if (this.avmService == null) {
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
   public void setNodeService(final NodeService nodeService) {
      this.nodeService = nodeService;
   }

   /**
    * Get nodeService property for this bean
    * 
    * @return nodeService property value for this bean
    */
   public NodeService getNodeService() {
      if (this.nodeService == null) {
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
   public void setSearchService(final SearchService searchService) {
      this.searchService = searchService;
   }

   /**
    * Get SearchService property for this bean
    * 
    * @return searchService property value for this bean
    */
   public SearchService getSearchService() {
      if (this.searchService == null) {
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
   public void setDictionaryService(final DictionaryService dictionaryService) {
      this.dictionaryService = dictionaryService;
   }

   /**
    * Get dictionaryService property for this bean
    * 
    * @return dictionaryService property value for this bean
    */
   public DictionaryService getDictionaryService() {
      if (this.dictionaryService == null) {
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
   public void setNamespaceService(final NamespaceService namespaceService) {
      this.namespaceService = namespaceService;
   }

   /**
    * Get the namespaceService property value for this bean
    * 
    * @return namespaceService property value for this bean
    */
   public NamespaceService getNamespaceService() {
      if (this.namespaceService == null) {
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
   public void setContentService(final ContentService contentService) {
      this.contentService = contentService;
   }

   /**
    * Get contentService property value for this bean
    * 
    * @return contentService property value for this bean
    */
   public ContentService getContentService() {
      if (this.contentService == null) {
         this.contentService = Repository.getServiceRegistry(
               FacesContext.getCurrentInstance()).getContentService();
      }
      return this.contentService;
   }

   /**
    * Provides data for a file picker widget.
    */
   @InvokeCommand.ResponseMimetype(value = MimetypeMap.MIMETYPE_XML)
   public void getFilePickerData() throws Exception {
      final FacesContext facesContext = FacesContext.getCurrentInstance();
      final ExternalContext externalContext = facesContext.getExternalContext();

      String currentPath = null;

      // get 'folderRestriction' parameter value
      // - expecting an absolute AVM folder path to be held in this parameter
      String folderPathRestriction = null;
      String[] folderPathRestrictionParam = (String[]) externalContext
            .getRequestParameterValuesMap().get(PARAM_FOLDER_RESTRICTION);
      if ((folderPathRestrictionParam != null)
            && (folderPathRestrictionParam.length != 0)) {
         folderPathRestriction = folderPathRestrictionParam[0];
      }

      // if folder restriction path is neither null nor an empty string, then
      // assign it to current path
      if ((folderPathRestriction != null)
            && (folderPathRestriction.length() > 0)) {
         currentPath = folderPathRestriction;
      }
      // else set current path to current path parameter
      else {
         String currentPathParam = (String) externalContext
               .getRequestParameterMap().get(PARAM_CURRENT_PATH);

         // if current path parameter null then set current path to the current
         // AVM path
         if ((currentPathParam == null)) {
            currentPath = this.getCurrentAVMPath();
         }
         // else set current path to value help in current path
         // parameter (converted to absolute AVM path)
         else {
            final String previewStorePath = AVMUtil
                  .getCorrespondingPathInPreviewStore(this.getCurrentAVMPath());
            currentPath = AVMUtil.buildPath(previewStorePath, currentPathParam,
                  AVMUtil.PathRelation.WEBAPP_RELATIVE);
         }
      }

      // get savedSearchName parameter value
      String savedSearchName = null;
      String[] savedSearchNameParam = (String[]) externalContext
            .getRequestParameterValuesMap().get(PARAM_SAVED_SEARCH_NAME);
      if ((savedSearchNameParam != null) && (savedSearchNameParam.length != 0)) {
         savedSearchName = savedSearchNameParam[0];
      }

      // get savedSearchContext parameter value
      String savedSearchContext = null;
      String[] savedSearchContextParam = (String[]) externalContext
            .getRequestParameterValuesMap().get(PARAM_SAVED_SEARCH_CONTEXT);
      if ((savedSearchContextParam != null)
            && (savedSearchContextParam.length != 0)) {
         savedSearchContext = savedSearchContextParam[0];
      }

      // get selectableTypes parameter value
      final QName[] selectableTypes = this
            .getSelectableTypes((String[]) externalContext
                  .getRequestParameterValuesMap().get(PARAM_SELECTABLE_TYPES));

      // / get filterMimetypes parameter value
      final Pattern[] filterMimetypes = this
            .getFilterMimetypes((String[]) externalContext
                  .getRequestParameterValuesMap().get(PARAM_FILTER_MIME_TYPES));

      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug(this + ".getFilePickerData(path = " + currentPath
               + ", selectableTypes = ["
               + StringUtils.join(selectableTypes, ",")
               + "], filterMimetypes = ["
               + StringUtils.join(filterMimetypes, ",") + "])");
      }

      // create file picker data XML document to return
      // and append file picker data element to it
      final org.w3c.dom.Document filePickerDataDoc = XMLUtil.newDocument();
      final org.w3c.dom.Element filePickerDataElement = filePickerDataDoc
            .createElement("file-picker-data");
      filePickerDataDoc.appendChild(filePickerDataElement);

      // make sure that there is a node associated with current path
      // if not, set an applicable error message as an attribute on
      // the file picker data element
      final AVMNodeDescriptor currentNode = this.getAvmService().lookup(-1,
            currentPath);
      if (currentNode == null) {
         currentPath = AVMUtil.getWebappRelativePath(currentPath);

         filePickerDataElement.setAttribute("error", MessageFormat.format(
               Application.getMessage(facesContext, "error_not_found"),
               currentPath.substring(currentPath.lastIndexOf("/") + 1,
                     currentPath.length()),
               (currentPath.lastIndexOf("/") == 0 ? "/" : currentPath
                     .substring(0, currentPath.lastIndexOf("/")))));

         // TODO (Glen): Ariel's code - What on earth is this doing here?
         // It is overriding the value assigned two statements above!!!
         currentPath = this.getCurrentAVMPath();
      }
      // if the path points to a file instead of a directory,
      // then make sure that the current path points to
      // just the directory part of the path
      else if (!currentNode.isDirectory()) {
         currentPath = AVMNodeConverter.SplitBase(currentPath)[0];
      }

      // create current-node element representing node for current path
      // and append it to the file picker data element
      org.w3c.dom.Element currentNodeElement = filePickerDataDoc
            .createElement("current-node");
      currentNodeElement.setAttribute("avmPath", currentPath);
      currentNodeElement.setAttribute("webappRelativePath", AVMUtil
            .getWebappRelativePath(currentPath));
      currentNodeElement.setAttribute("type", "directory");

      // TODO (Glen): This was in Ariel's code. Is this the correct
      // image to set?
      currentNodeElement.setAttribute("image", "/images/icons/space_small.gif");

      filePickerDataElement.appendChild(currentNodeElement);

      // if saved search name and saved search context parameters supplied (i.e.
      // not null),
      // then get saved saved search node matching given name and context
      if ((savedSearchName != null) && (savedSearchName.length() != 0)
            && (savedSearchContext != null)
            && (savedSearchContext.length() != 0)) {
         // get node reference for named saved search in the given saved search
         // context
         NodeRef savedSearchNodeRef = getSavedSearches(savedSearchName,
               savedSearchContext);

         // run search to get content nodes returned in search result
         List<AVMNodeDescriptor> searchResultNodes = runSavedSearch(savedSearchNodeRef);

         // add elements for content nodes from search results as child nodes
         // of the file picker data element.
         addAVMNodesToElement(filePickerDataDoc, filePickerDataElement,
               searchResultNodes, selectableTypes, facesContext);
      } else {
         // add elements for child nodes of current path to file picker
         // data element
         addPathChildNodesToElement(filePickerDataDoc, filePickerDataElement,
               currentPath, selectableTypes, filterMimetypes, facesContext);
      }

      final ResponseWriter out = facesContext.getResponseWriter();
      XMLUtil.print(filePickerDataDoc, out);
   }

   @InvokeCommand.ResponseMimetype(value = MimetypeMap.MIMETYPE_HTML)
   public void uploadFile() throws Exception {
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
      for (FileItem item : fileItems) {
         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("item = " + item);
         }
         if (item.isFormField() && item.getFieldName().equals("upload-id")) {
            uploadId = item.getString();
            if (LOGGER.isDebugEnabled()) {
               LOGGER.debug("uploadId is " + uploadId);
            }
         }
         if (item.isFormField() && item.getFieldName().equals("return-page")) {
            returnPage = item.getString();
            if (LOGGER.isDebugEnabled()) {
               LOGGER.debug("returnPage is " + returnPage);
            }
         } else if (item.isFormField()
               && item.getFieldName().equals("currentPath")) {
            final String previewStorePath = AVMUtil
                  .getCorrespondingPathInPreviewStore(this.getCurrentAVMPath());
            currentPath = AVMUtil.buildPath(previewStorePath, item.getString(),
                  AVMUtil.PathRelation.WEBAPP_RELATIVE);
            if (LOGGER.isDebugEnabled()) {
               LOGGER.debug("currentPath is " + currentPath);
            }
         } else {
            filename = FilenameUtils.getName(item.getName());
            fileInputStream = item.getInputStream();

            if (LOGGER.isDebugEnabled()) {
               LOGGER.debug("uploading file " + filename);
            }
         }
      }

      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("saving file " + filename + " to " + currentPath);
      }

      try {
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
         returnPage = returnPage.replace("${_FILE_TYPE_IMAGE}", Utils
               .getFileTypeImage(facesContext, filename, true));
      } catch (Exception e) {
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

   private String getCurrentAVMPath() {
      final AVMNode node = this.getAvmBrowseBean().getAvmActionNode();
      if (node == null) {
         return this.getAvmBrowseBean().getCurrentPath();
      }

      final String result = node.getPath();
      return node.isDirectory() ? result
            : AVMNodeConverter.SplitBase(result)[0];
   }

   private QName[] getSelectableTypes(final String[] selectableTypes) {
      final QName[] result = (selectableTypes == null ? new QName[] {
            WCMModel.TYPE_AVM_CONTENT, WCMModel.TYPE_AVM_FOLDER }
            : new QName[selectableTypes.length]);

      if (selectableTypes != null) {
         for (int i = 0; i < selectableTypes.length; i++) {
            result[i] = QName.resolveToQName(this.getNamespaceService(),
                  selectableTypes[i]);
         }
      }
      return result;
   }

   private Pattern[] getFilterMimetypes(final String[] filterMimetypes) {
      final Pattern[] result = filterMimetypes == null ? new Pattern[0]
            : new Pattern[filterMimetypes.length];
      if (filterMimetypes != null) {
         for (int i = 0; i < filterMimetypes.length; i++) {
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
         Pattern[] filterMimetypes, FacesContext facesContext) {
      // append elements for the child AVM nodes of the current path
      // to parent element
      for (final Map.Entry<String, AVMNodeDescriptor> entry : this
            .getAvmService().getDirectoryListing(-1, path).entrySet()) {
         // if AVM node is a content node and the filter MIME types parameter
         // has been set, then only add child element for AVM node if it matches
         // one of the specified MIME types in the parameter
         if (!entry.getValue().isDirectory() && filterMimetypes.length != 0) {
            final String contentMimetype = this.getAvmService()
                  .getContentDataForRead(entry.getValue()).getMimetype();

            boolean matched = false;
            for (final Pattern p : filterMimetypes) {
               matched = p.matcher(contentMimetype).matches();
               if (LOGGER.isDebugEnabled()) {
                  LOGGER.debug(p + ".matches(" + contentMimetype + ") = "
                        + matched);
               }
               if (matched) {
                  break;
               }
            }

            // if AVM node MIME type doesn't match any of the types in the
            // filter MIME types parameter, then don't do any further processing
            // on it and jump back to the start of the AVM node traversal loop
            if (!matched) {
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
    * Add provided AVM node descriptors as child node elements to the provided
    * parent element
    * 
    * @param doc
    *           XML document to which the parent node belongs
    * @param parent
    *           parent element to add given nodes as child elements
    * @param nodes
    *           nodes which to add as child elements to parent elements
    * @param selectableTypes
    *           AVM node types which must be marked as selectable
    * @param facesContext
    *           faces context used to set image attribute on each child element
    */
   private void addAVMNodesToElement(org.w3c.dom.Document doc,
         org.w3c.dom.Element parent, List<AVMNodeDescriptor> nodes,
         QName[] selectableTypes, FacesContext facesContext) {
      for (AVMNodeDescriptor node : nodes) {
         // create child element representing AVM node and add to file picker
         // data element
         addAVMChildNodeToParentElement(doc, parent, node, selectableTypes,
               facesContext);
      }
   }

   /**
    * Run a saved search represented by the given node reference, against the
    * web project the XForm is currently within
    * 
    * @param savedQueryNodRef
    *           NodeRef of the saved query with which to run the search
    * 
    * @return content nodes returned by the search
    */
   private List<AVMNodeDescriptor> runSavedSearch(NodeRef savedSearchNodeRef) {
      // get the store id used to run the saved search query
      WebProject webProject = this.getAvmBrowseBean().getWebProject();
      String storeID = webProject.getStoreId();

      // extract the content of saved search node to XML document
      ContentReader contentReader = getContentService().getReader(
            savedSearchNodeRef, ContentModel.PROP_CONTENT);
      InputStream queryInpStream = contentReader.getContentInputStream();
      SAXReader reader = new SAXReader();
      Document savedQueryDoc = null;
      try {
         savedQueryDoc = reader.read(queryInpStream);
      } catch (DocumentException de) {
         // ignore exception and return null
         return null;
      }

      // extract search query from saved search XML document
      String query = null;
      XPath queryXPath = DocumentHelper.createXPath(SAVED_SEARCH_QUERY_XPATH);
      List xpathResult = queryXPath.selectNodes(savedQueryDoc);
      if ((xpathResult != null) && (xpathResult.size() != 0)) {
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
               && ((cdataStartDelimIndex + CDATA_START_DELIM.length()) < cdataEndDelimIndex)) {
            query = queryElemText.substring(cdataStartDelimIndex
                  + CDATA_START_DELIM.length(), cdataEndDelimIndex);
         }
      }

      // perform the search against the repository
      // if query was extracted from the saved search successfully
      // (extracted query non-null)
      List<AVMNodeDescriptor> resultNodeDescriptors = null;
      if ((query != null) && (query.length() != 0)) {
         ResultSet results = null;
         try {
            results = this.getSearchService().query(
                  new StoreRef(StoreRef.PROTOCOL_AVM, storeID),
                  SearchService.LANGUAGE_LUCENE, query);

            if (results.length() != 0) {
               resultNodeDescriptors = new ArrayList<AVMNodeDescriptor>();
               for (int i = 0; i < results.length(); i++) {
                  ResultSetRow row = results.getRow(i);
                  NodeRef resultNodeRef = row.getNodeRef();
                  Node resultNode = new Node(resultNodeRef);

                  // only add content type nodes to the search result
                  // as we don't want the user to navigate down into folders
                  // in the search results
                  if (getDictionaryService().isSubClass(resultNode.getType(),
                        ContentModel.TYPE_CONTENT)) {
                     Pair<Integer, String> pair = AVMNodeConverter
                           .ToAVMVersionPath(resultNodeRef);
                     Integer version = pair.getFirst();
                     String path = pair.getSecond();
                     resultNodeDescriptors.add(getAvmService().lookup(version,
                           path));
                  }
               }
            }
         } catch (Throwable err) {
            throw new AlfrescoRuntimeException("Failed to execute search: "
                  + query, err);
         } finally {
            if (results != null) {
               results.close();
            }
         }
      }

      return resultNodeDescriptors;
   }

   /**
    * Get the cached reference to the current user's saved searches folder. This
    * method will first get a reference to the current user's saved searches
    * folder and assign it to the cached reference if is it is null.
    * 
    * @return the cached reference to the current user's Saved Searches folder
    */
   private NodeRef getUserSearchesRef() {
      // if the cached reference is null, then get a reference to the
      // user's saved searches folder to assign to it
      if (userSearchesRef == null) {
         NodeRef globalRef = getGlobalSearchesRef();
         if (globalRef != null) {
            // Use the search service get a reference to the
            // current user's saved searches folder.
            // Search within the context of the global saved searches
            // folder
            FacesContext fc = FacesContext.getCurrentInstance();
            User user = Application.getCurrentUser(fc);
            String userName = ISO9075.encode(user.getUserName());
            String xpath = NamespaceService.APP_MODEL_PREFIX + ":"
                  + QName.createValidLocalName(userName);

            List<NodeRef> results = null;
            try {
               results = getSearchService().selectNodes(globalRef, xpath, null,
                     getNamespaceService(), false);
            } catch (AccessDeniedException err) {
               // ignore and return null
            }

            if ((results != null) && (results.size() != 0)) {
               userSearchesRef = results.get(0);
            }
         }
      }

      return userSearchesRef;
   }

   /**
    * Get the cached reference to the global saved searches folder. This method
    * will first get a reference to the global saved searches folder and assign
    * it to the cached reference if is it is null.
    * 
    * @return the cached reference to the global Saved Searches folder
    */
   private NodeRef getGlobalSearchesRef() {
      // if the cached reference is null, then get a reference to the
      // global saved searches folder to assign to it
      if (globalSearchesRef == null) {
         // Use the search service get a reference to the
         // global saved searches folder.
         FacesContext fc = FacesContext.getCurrentInstance();
         String xpath = Application.getRootPath(fc) + "/"
               + Application.getGlossaryFolderName(fc) + "/"
               + Application.getSavedSearchesFolderName(fc);

         List<NodeRef> results = null;
         try {
            results = getSearchService().selectNodes(
                  getNodeService().getRootNode(Repository.getStoreRef()),
                  xpath, null, getNamespaceService(), false);
         } catch (AccessDeniedException err) {
            // ignore and return null
         }

         if (results != null && results.size() != 0) {
            globalSearchesRef = results.get(0);
         }
      }

      return globalSearchesRef;
   }

   /**
    * Get node for saved search by name.
    * 
    * @param savedSearchName
    *           name of saved search for which to get node
    * @param savedSearchContext
    *           either "user" or "global", which says whether to get saved
    *           search out of current user's saved searches folder or global
    *           saved searches folder respectively
    * @return node reference for saved search
    */
   public NodeRef getSavedSearches(String savedSearchName,
         String savedSearchContext) {
      NodeRef savedSearchNodeRef = null;

      // get the saved searches folder reference from the
      // current user or global searches location
      NodeRef savedSearchesFolderRef = null;
      if (SAVED_SEARCHES_CONTEXT_USER.equals(savedSearchContext)) {
         savedSearchesFolderRef = getUserSearchesRef();
      } else if (SAVED_SEARCHES_CONTEXT_GLOBAL.equals(savedSearchContext)) {
         savedSearchesFolderRef = getGlobalSearchesRef();
      }

      // read the content nodes under the folder
      List<ChildAssociationRef> childRefs = getNodeService().getChildAssocs(
            savedSearchesFolderRef, ContentModel.ASSOC_CONTAINS,
            RegexQNamePattern.MATCH_ALL);

      // return content node with name matching given saved search name
      if (childRefs.size() != 0) {
         for (ChildAssociationRef ref : childRefs) {
            NodeRef childNodeRef = ref.getChildRef();
            Node childNode = new Node(childNodeRef);
            if (getDictionaryService().isSubClass(childNode.getType(),
                  ContentModel.TYPE_CONTENT)) {
               String childNodeName = childNode.getName();
               if (childNodeName.equals(savedSearchName)) {
                  savedSearchNodeRef = childNodeRef;
                  break;
               }
            }
         }
      }

      return savedSearchNodeRef;
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
         QName[] selectableTypes, FacesContext facesContext) {
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
            (node.isDirectory() ? "/images/icons/space_small.gif" : Utils
                  .getFileTypeImage(facesContext, node.getName(), true)));

      boolean selectable = false;

      // set 'selectable' attribute on child node to mark whether node should
      // be selectable in file picker or not
      //
      // TODO Ariel: faking this for now since i can't figure out how to
      // efficiently get the type
      // qname from the avmservice
      for (final QName typeQName : selectableTypes) {
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
