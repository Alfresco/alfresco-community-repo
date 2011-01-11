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
package org.alfresco.web.forms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.WebProject;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides management of forms.
 *
 * @author Ariel Backenroth
 */
public final class FormsService 
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
   private NodeRef webContentFormsNodeRef;
   
   /** instantiated using spring */
   public FormsService(final ContentService contentService,
                       final NodeService nodeService,
                       final NamespaceService namespaceService,
                       final SearchService searchService,
                       final PolicyComponent policyComponent)
   {
      this.contentService = contentService;
      this.nodeService = nodeService;
      this.namespaceService = namespaceService;
      this.searchService = searchService;
      policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onMoveNode"), 
                                         WCMAppModel.TYPE_FORMFOLDER, 
                                         new JavaBehaviour(this, 
                                                           "handleMoveFormFolder", 
                                                           Behaviour.NotificationFrequency.FIRST_EVENT));
      policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteNode"), 
                                         WCMAppModel.TYPE_FORMFOLDER, 
                                         new JavaBehaviour(this, 
                                                           "handleDeleteFormFolder", 
                                                           Behaviour.NotificationFrequency.FIRST_EVENT));
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

         this.contentFormsNodeRef = getNodeRefFromXPath(xpath);
      }
      return this.contentFormsNodeRef;
   }
   
   /**
    * @return the cached reference to the WCM Content Forms folder
    */
   public NodeRef getWebContentFormsNodeRef()
   {
      if (this.webContentFormsNodeRef == null)
      {
         final FacesContext fc = FacesContext.getCurrentInstance();
         final String xpath = (Application.getRootPath(fc) + "/" +
                               Application.getGlossaryFolderName(fc) + "/" +
                               Application.getWebContentFormsFolderName(fc));
         
         this.webContentFormsNodeRef = getNodeRefFromXPath(xpath);
      }
      return this.webContentFormsNodeRef;
   }
   
   private NodeRef getNodeRefFromXPath(String xpath)
   {
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("locating noderef at " + xpath);
      final List<NodeRef> results = 
         searchService.selectNodes(this.nodeService.getRootNode(Repository.getStoreRef()),
                                   xpath,
                                   null,
                                   namespaceService,
                                   false);
      return (results != null && results.size() == 1 ? results.get(0) : null);
   }
   
   /** 
    * returns registered forms 
    *
    * @return registered (ECM) forms
    */
   public Collection<Form> getForms()
   {
      final String query =
         "+ASPECT:\"" + WCMAppModel.ASPECT_FORM + 
         "\" +PARENT:\"" + this.getContentFormsNodeRef() + "\"";

      return getForms(query);
   }
   
   /** 
    * returns registered web forms 
    *
    * @return registered (WCM) forms
    */
   public Collection<Form> getWebForms()
   {
      final String query =
         "+ASPECT:\"" + WCMAppModel.ASPECT_FORM + 
         "\" +PARENT:\"" + this.getWebContentFormsNodeRef() + "\"";
      
      return getForms(query);
   }
      
   private Collection<Form> getForms(String query)
   {
      final ResultSet rs = this.searchService.query(Repository.getStoreRef(),
                                                    SearchService.LANGUAGE_LUCENE,
                                                    query);
      try
      {
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("found " + rs.length() + " form definitions");
         final Collection<Form> result = new ArrayList<Form>(rs.length());
         for (final ResultSetRow row : rs)
         {
            result.add(this.getForm(row.getNodeRef()));
         }
         QuickSort sorter = new QuickSort((List)result, "name", true, IDataContainer.SORT_CASEINSENSITIVE);
         sorter.sort();
         
         return result;
      }
      finally
      {
         rs.close();
      }
   }
   
   /** 
    * return the form by name or <tt>null</tt> if not found 
    *
    * @return the form by name or <tt>null</tt> if not found 
    * @deprecated
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
    * return the web form by name or <tt>null</tt> if not found 
    *
    * @return the (WCM) form by name or <tt>null</tt> if not found 
    * @deprecated
    */
   public Form getWebForm(final String name)
     throws FormNotFoundException
   {
     final NodeRef result = this.nodeService.getChildByName(this.getWebContentFormsNodeRef(),
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

   public FormInstanceData getFormInstanceData(final int version, final String avmPath) throws FormNotFoundException
   {
      return this.getFormInstanceData(AVMNodeConverter.ToNodeRef(version, avmPath));
   }

   public FormInstanceData getFormInstanceData(final NodeRef nodeRef) throws FormNotFoundException
   {
      final String avmPath = AVMNodeConverter.ToAVMVersionPath(nodeRef).getSecond();
      final WebProject webProject = new WebProject(avmPath);
       
      FormInstanceData fid = null;
      try
      {
          fid = new FormInstanceDataImpl(nodeRef, this, webProject);
          return fid;
      }
      catch (IllegalArgumentException iae)
      {
          // note: FormNotFoundException extends FileNotFoundException
          throw new FormNotFoundException(iae.getMessage());
      }
   }

   public Rendition getRendition(final int version, final String avmPath)
   {
      return this.getRendition(AVMNodeConverter.ToNodeRef(version, avmPath));
   }

   public Rendition getRendition(final NodeRef nodeRef)
   {
      return new RenditionImpl(nodeRef, this);
   }

   public List<WebProject> getAssociatedWebProjects(final Form form)
   {
      final List<NodeRef> formConfigurations = this.getFormConfigurations(form.getName());
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("found " + formConfigurations.size() + 
                      " web projects configured with " + form.getName());
      }
      final List<WebProject> result = new ArrayList<WebProject>(formConfigurations.size());
      for (final NodeRef ref : formConfigurations)
      {
         final List<ChildAssociationRef> parents = this.nodeService.getParentAssocs(ref);
         assert parents.size() != 1 : ("expected only one parent for " + ref +
                                       " got " + parents.size());
         result.add(new WebProject(parents.get(0).getParentRef()));
      }
      return result;
   }

   // event handlers

   public void handleMoveFormFolder(final ChildAssociationRef oldChild, final ChildAssociationRef newChild)
   {
      final String oldName = oldChild.getQName().getLocalName();
      final String newName = newChild.getQName().getLocalName();
      final List<NodeRef> formConfigurations = this.getFormConfigurations(oldName);
      // find all webprojects that used the old name
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("handling rename (" + oldName +
                      " => " + newName + 
                      ") for " + formConfigurations.size());
      }
      for (final NodeRef ref : formConfigurations)
      {
         this.nodeService.setProperty(ref, 
                                      WCMAppModel.PROP_FORMNAME, 
                                      newName);
      }
   }

   public void handleDeleteFormFolder(final ChildAssociationRef childRef,
                                      final boolean isArchivedNode)
   {
      final String formName = childRef.getQName().getLocalName();
      final List<NodeRef> formConfigurations = this.getFormConfigurations(formName);
      for (final NodeRef ref : formConfigurations)
      {
         final List<ChildAssociationRef> parents = this.nodeService.getParentAssocs(ref);
         assert parents.size() != 1 : ("expected only one parent for " + ref +
                                       " got " + parents.size());
         final NodeRef parentRef = parents.get(0).getParentRef();
         if (LOGGER.isDebugEnabled())
         {

            LOGGER.debug("removing configuration for " + formName +
                         " from web project " + this.nodeService.getProperty(parentRef, ContentModel.PROP_NAME));
         }
		 // ALF-3751: Validate this is the real form folder rather than a copy
         if (childRef.getParentRef().equals(parentRef))
         {
            this.nodeService.removeChild(parentRef, ref);
         }
      }
   }

   /**
    * Return the list of web project nodes that reference a form name in their model  
    */
   private List<NodeRef> getFormConfigurations(final String formName)
   {
      final String query = 
         "+TYPE:\"" + WCMAppModel.TYPE_WEBFORM + "\"" +
         " +@" + Repository.escapeQName(WCMAppModel.PROP_FORMNAME) + 
         ":\"" + formName + "\"";
      final ResultSet rs = this.searchService.query(Repository.getStoreRef(),
                                                    SearchService.LANGUAGE_LUCENE,
                                                    query);
      try
      {
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("query " + query + " returned " + rs.length() + " results");
         }
         final List<NodeRef> result = new ArrayList<NodeRef>(rs.length());
         for (final ResultSetRow row : rs)
         {
            result.add(row.getNodeRef());
         }
         return result;
      }
      finally
      {
         rs.close();
      }
   }
}
