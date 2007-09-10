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
import org.alfresco.util.ISO9075;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMUtil;
import org.alfresco.web.bean.wcm.WebProject;
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
      sp.setQuery("+ASPECT:\"" + WCMAppModel.ASPECT_FORM + 
                  "\" +PARENT:\"" + this.getContentFormsNodeRef() + "\"");
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
      throws FormNotFoundException
   {
      final NodeRef result = this.nodeService.getChildByName(this.getContentFormsNodeRef(),
                                                             ContentModel.ASSOC_CONTAINS,
                                                             name);
      if (result == null)
      {
         throw new FormNotFoundException(name);
      }
      return this.getForm(result);
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
      if (!this.nodeService.hasAspect(nodeRef, WCMAppModel.ASPECT_FORM))
      {
         throw new IllegalArgumentException("node " + nodeRef + " is not a form");
      }
      final Form result = new FormImpl(nodeRef, this);
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("loaded form " + result + " for noderef " + nodeRef);
      return result;
   }

   public FormInstanceData getFormInstanceData(final int version, final String avmPath)
   {
      return this.getFormInstanceData(AVMNodeConverter.ToNodeRef(version, avmPath));
   }

   public FormInstanceData getFormInstanceData(final NodeRef nodeRef)
   {
      final String avmPath = AVMNodeConverter.ToAVMVersionPath(nodeRef).getSecond();
      final WebProject webProject = new WebProject(avmPath);
      return new FormInstanceDataImpl(nodeRef, this)
      {
         @Override
         public Form getForm()
            throws FormNotFoundException
         {
            final Form f = super.getForm();
            try
            {
               return webProject.getForm(f.getName());
            }
            catch (FormNotFoundException fnfne)
            {
               throw new FormNotFoundException(f, webProject, this);
            }
         }
      };
   }

   public Rendition getRendition(final int version, final String avmPath)
   {
      return this.getRendition(AVMNodeConverter.ToNodeRef(version, avmPath));
   }

   public Rendition getRendition(final NodeRef nodeRef)
   {
      return new RenditionImpl(nodeRef, this);
   }
}
