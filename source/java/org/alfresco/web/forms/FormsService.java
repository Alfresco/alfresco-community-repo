/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.forms;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMConstants;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Provides management of forms.
 *
 * @author Ariel Backenroth
 */
public final class FormsService 
   implements Serializable
{
   private static final Log LOGGER = LogFactory.getLog(FormsService.class);
   
   /** the single instance initialized using spring */
   private static FormsService INSTANCE;

   
   private static final RenderingEngine[] RENDERING_ENGINES = new RenderingEngine[] 
   {
      new FreeMarkerRenderingEngine(),
      new XSLTRenderingEngine(),
      new XSLFORenderingEngine()
   };
   
   private final ContentService contentService;
   private final NodeService nodeService;
   private final NamespaceService namespaceService;
   private final SearchService searchService;

   private NodeRef contentFormsNodeRef;
   
   /** instantiated using spring */
   public FormsService(final ContentService contentService,
                       final NodeService nodeService,
                       final NamespaceService namespaceService,
                       final SearchService searchService)
   {
      this.contentService = contentService;
      this.nodeService = nodeService;
      this.namespaceService = namespaceService;
      this.searchService = searchService;
      if (INSTANCE == null)
         INSTANCE = this;
   }
   
   /** Provides the forms service instance, loads config if necessary */
   public static FormsService getInstance()
   {
      return FormsService.INSTANCE;
   }

   /**
    * Provides all registered rendering engines.
    */
   public RenderingEngine[] getRenderingEngines()
   {
      return FormsService.RENDERING_ENGINES;
   }

   /**
    * Returns the rendering engine with the given name.
    *
    * @param name the name of the rendering engine.
    *
    * @return the rendering engine or <tt>null</tt> if not found.
    */
   public RenderingEngine getRenderingEngine(final String name)
   {
      for (RenderingEngine re : this.getRenderingEngines())
      {
         if (re.getName().equals(name))
         {
            return re;
         }
      }
      return null;
   }

   public RenderingEngine guessRenderingEngine(final String fileName)
   {
      for (RenderingEngine re : this.getRenderingEngines())
      {
         if (fileName.endsWith(re.getDefaultTemplateFileExtension()))
         {
            return re;
         }
      }
      return null;
   }

   /**
    * @return the cached reference to the WCM Content Forms folder
    */
   public NodeRef getContentFormsNodeRef()
   {
      if (this.contentFormsNodeRef == null)
      {
         final FacesContext fc = FacesContext.getCurrentInstance();
         final String xpath = (Application.getRootPath(fc) + "/" +
                               Application.getGlossaryFolderName(fc) + "/" +
                               Application.getContentFormsFolderName(fc));
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("locating content forms at " + xpath);
         final List<NodeRef> results = 
            searchService.selectNodes(this.nodeService.getRootNode(Repository.getStoreRef()),
                                      xpath,
                                      null,
                                      namespaceService,
                                      false);
         this.contentFormsNodeRef = (results != null && results.size() == 1 ? results.get(0) : null);
      }
      return this.contentFormsNodeRef;
   }
   
   /** 
    * returns all registered forms 
    *
    * @return all registered forms.
    */
   public Collection<Form> getForms()
   {
      final SearchParameters sp = new SearchParameters();
      sp.addStore(Repository.getStoreRef());
      sp.setLanguage(SearchService.LANGUAGE_LUCENE);
      sp.setQuery("ASPECT:\"" + WCMAppModel.ASPECT_FORM + "\"");
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("running query [" + sp.getQuery() + "]");
      final ResultSet rs = this.searchService.query(sp);
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("received " + rs.length() + " results");
      final Collection<Form> result = new ArrayList<Form>(rs.length());
      for (ResultSetRow row : rs)
      {
         result.add(this.getForm(row.getNodeRef()));
      }
      QuickSort sorter = new QuickSort((List)result, "name", true, IDataContainer.SORT_CASEINSENSITIVE);
      sorter.sort();
      
      return result;
   }
   
   /** 
    * return the form by name or <tt>null</tt> if not found 
    *
    * @return the form by name or <tt>null</tt> if not found 
    */
   public Form getForm(final String name)
   {
      final SearchParameters sp = new SearchParameters();
      sp.addStore(Repository.getStoreRef());
      sp.setLanguage(SearchService.LANGUAGE_LUCENE);
      sp.setQuery("ASPECT:\"" + WCMAppModel.ASPECT_FORM + 
                  "\" AND @" + Repository.escapeQName(ContentModel.PROP_NAME) + 
                  ":\"" + name + "\"");
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("running query [" + sp.getQuery() + "]");
      final ResultSet rs = this.searchService.query(sp);
      NodeRef result = null;
      for (ResultSetRow row : rs)
      {
         final NodeRef nr = row.getNodeRef();
         if (this.nodeService.getProperty(nr, ContentModel.PROP_NAME).equals(name))
         {
            result = nr;
            break;
         }
      }
      if (result == null && LOGGER.isDebugEnabled())
         LOGGER.debug("unable to find template type " + name);
      return result != null ? this.getForm(result) : null;
   }

   /**
    * Returns the form backed by the given NodeRef.  The NodeRef should
    * point to the schema for this form.
    *
    * @param nodeRef the node ref for the schema for the form
    * @return the form for the given node ref.
    */
   public Form getForm(final NodeRef nodeRef)
   {
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("loading form for " + nodeRef);
      final Form result = new FormImpl(nodeRef);
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("loaded form " + result);
      return result;
   }
}
