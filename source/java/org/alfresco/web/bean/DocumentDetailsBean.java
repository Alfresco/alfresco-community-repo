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
package org.alfresco.web.bean;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.Utils.URLMode;
import org.alfresco.web.ui.common.component.UIActionLink;

/**
 * Backing bean providing access to the details of a document
 * 
 * @author gavinc
 */
public class DocumentDetailsBean extends BaseDetailsBean
{
   private static final String OUTCOME_RETURN = "showDocDetails";
   
   private static final String MSG_HAS_FOLLOWING_CATEGORIES = "has_following_categories";
   private static final String MSG_NO_CATEGORIES_APPLIED = "no_categories_applied";
   private static final String MSG_SUCCESS_UNLOCK = "success_unlock";
   private static final String MSG_ERROR_ASPECT_INLINEEDITABLE = "error_aspect_inlineeditable";
   private static final String MSG_ERROR_ASPECT_VERSIONING = "error_aspect_versioning";
   private static final String MSG_ERROR_ASPECT_CLASSIFY = "error_aspect_classify";
   private static final String MSG_ERROR_UPDATE_CATEGORY = "error_update_category";
   
   protected LockService lockService;
   protected VersionService versionService;
   protected CheckOutCheckInService cociService;
   protected MultilingualContentService multilingualContentService;
   protected ContentFilterLanguagesService contentFilterLanguagesService;
   
   private NodeRef addedCategory;
   private List categories;


   // ------------------------------------------------------------------------------
   // Construction 
   
   /**
    * Default constructor
    */
   public DocumentDetailsBean()
   {
      super();
      
      // initial state of some panels that don't use the default
      panels.put("version-history-panel", false);
      panels.put("ml-info-panel", false);
      panels.put("related-translation-panel", false);
   }
   
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters 
   
   /**
    * Resets any state that may be held by this bean
    */
   public void reset()
   {
      // reset the workflow cache
      this.workflowProperties = null;
      
      // reset the category caches
      this.categories = null;
      this.addedCategory = null;
   }
   
   /**
    * Returns the URL to download content for the current document
    * 
    * @return Content url to download the current document
    */
   public String getUrl()
   {
      return (String)getDocument().getProperties().get("url");
   }
   
   /**
    * Returns the URL to the content for the current document
    *  
    * @return Content url to the current document
    */
   public String getBrowserUrl()
   {
      Node doc = getLinkResolvedNode();
      return Utils.generateURL(FacesContext.getCurrentInstance(), doc, URLMode.HTTP_INLINE);
   }

   /**
    * Returns the download URL to the content for the current document
    *  
    * @return Download url to the current document
    */
   public String getDownloadUrl()
   {
      Node doc = getLinkResolvedNode();
      return Utils.generateURL(FacesContext.getCurrentInstance(), doc, URLMode.HTTP_DOWNLOAD);
   }
   
   /**
    * Resolve the actual document Node from any Link object that may be proxying it
    * 
    * @return current document Node or document Node resolved from any Link object
    */
   protected Node getLinkResolvedNode()
   {
      Node document = getDocument();
      if (ApplicationModel.TYPE_FILELINK.equals(document.getType()))
      {
         NodeRef destRef = (NodeRef)document.getProperties().get(ContentModel.PROP_LINK_DESTINATION);
         if (nodeService.exists(destRef))
         {
            document = new Node(destRef);
         }
      }
      return document;
   }
   
   /**
    * Determines whether the current document is versionable
    * 
    * @return true if the document has the versionable aspect
    */
   public boolean isVersionable()
   {
      return getDocument().hasAspect(ContentModel.ASPECT_VERSIONABLE);
   }
   
   /**
    * @return true if the current document has the 'inlineeditable' aspect applied
    */
   public boolean isInlineEditable()
   {
      return getDocument().hasAspect(ApplicationModel.ASPECT_INLINEEDITABLE);
   }
   
   /**
    * @return true if the current document has the 'inlineeditable' aspect applied
    */
   public boolean isMultilingual()
   {
      return getDocument().hasAspect(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT);
   }
   
   /**
    * Returns a list of objects representing the versions of the 
    * current document 
    * 
    * @return List of previous versions
    */
   public List getVersionHistory()
   {
      List<MapNode> versions = new ArrayList<MapNode>();
      
      if (getDocument().hasAspect(ContentModel.ASPECT_VERSIONABLE))
      {
         VersionHistory history = this.versionService.getVersionHistory(getDocument().getNodeRef());
   
         if (history != null)
         {
            for (Version version : history.getAllVersions())
            {
               // create a map node representation of the version
               MapNode clientVersion = new MapNode(version.getFrozenStateNodeRef());
               clientVersion.put("versionLabel", version.getVersionLabel());
               clientVersion.put("notes", version.getDescription());
               clientVersion.put("author", version.getCreator());
               clientVersion.put("versionDate", version.getCreatedDate());
               clientVersion.put("url", DownloadContentServlet.generateBrowserURL(version.getFrozenStateNodeRef(), 
                     clientVersion.getName()));
               
               // add the client side version to the list
               versions.add(clientVersion);
            }
         }
      }
      
      return versions;
   }
   
   /**
    * Returns a list of objects representing the editions of the 
    * logical document  
    * 
    * @return List of editions
    */
   public List getEditionHistory()
   {
      List<MapNode> editions = new ArrayList<MapNode>();
      
      if (getDocument().getType().equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER))
      {
         
      }
      
      return editions;
   }
   
   /**
    * Returns a list of objects representing the translations of the 
    * current document 
    * 
    * @return List of translations
    */
   public List getTranslations()
   {
      List<MapNode> translations = new ArrayList<MapNode>();
      
      if (getDocument().hasAspect(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT))
      {
        
        Map<Locale, NodeRef> translationsMap = this.multilingualContentService.getTranslations(getDocument().getNodeRef());
   
         if (translationsMap != null && translationsMap.size() > 0)
         {
            for (Map.Entry entry : translationsMap.entrySet())
            {
               NodeRef nodeRef = (NodeRef) entry.getValue();              
               
               // create a map node representation of the translation
               MapNode mapNode = new MapNode(nodeRef);   
               
               Locale locale = (Locale) nodeService.getProperty(nodeRef, ContentModel.PROP_LOCALE); 
               
               String lgge = (locale != null) ?  
                     // convert the locale into new ISO codes
                     contentFilterLanguagesService.convertToNewISOCode(locale.getLanguage()).toUpperCase() 
                     : null ;
               
               mapNode.put("name", nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
               mapNode.put("language", lgge);
               mapNode.put("url", DownloadContentServlet.generateBrowserURL(nodeRef, mapNode.getName()));
               
               // add the client side version to the list
               translations.add(mapNode);
            }
         }
      }
      
      return translations;
   }
   
   
   /**
    * Determines whether the current document has any categories applied
    * 
    * @return true if the document has categories attached
    */
   public boolean isCategorised()
   {
      return getDocument().hasAspect(ContentModel.ASPECT_GEN_CLASSIFIABLE);
   }
   
   /**
    * Returns a list of objects representing the categories applied to the 
    * current document
    *  
    * @return List of categories
    */
   public String getCategoriesOverviewHTML()
   {
      String html = null;
      
      if (isCategorised())
      {
         // we know for now that the general classifiable aspect only will be
         // applied so we can retrive the categories property direclty
         Collection<NodeRef> categories = (Collection<NodeRef>)this.nodeService.getProperty(
                 getDocument().getNodeRef(), ContentModel.PROP_CATEGORIES);
         
         if (categories == null || categories.size() == 0)
         {
            html = Application.getMessage(FacesContext.getCurrentInstance(), MSG_NO_CATEGORIES_APPLIED);
         }
         else
         {
            StringBuilder builder = new StringBuilder(Application.getMessage(FacesContext.getCurrentInstance(), 
                  MSG_HAS_FOLLOWING_CATEGORIES));
            
            builder.append("<ul>");
            for (NodeRef ref : categories)
            {
               if (this.nodeService.exists(ref))
               {
                  builder.append("<li>");
                  builder.append(Repository.getNameForNode(this.nodeService, ref));
                  builder.append("</li>");
               }
            }
            builder.append("</ul>");
            
            html = builder.toString();
         }
      }
      
      return html;
   }

   /**
    * Event handler called to setup the categories for editing
    * 
    * @param event The event
    */
   public void setupCategoriesForEdit(ActionEvent event)
   {
      this.categories = (List)this.nodeService.getProperty(getDocument().getNodeRef(), 
               ContentModel.PROP_CATEGORIES);
   }
   
   /**
    * Returns a Map of the initial categories on the node keyed by the NodeRef
    * 
    * @return Map of initial categories
    */
   public List getCategories()
   {
      return this.categories;
   }
   
   /**
    * Sets the categories Map
    * 
    * @param categories
    */
   public void setCategories(List categories)
   {
      this.categories = categories;
   }
   
   /**
    * Returns the last category added from the multi value editor
    * 
    * @return The last category added
    */
   public NodeRef getAddedCategory()
   {
      return this.addedCategory;
   }

   /**
    * Sets the category added from the multi value editor
    * 
    * @param addedCategory The added category
    */
   public void setAddedCategory(NodeRef addedCategory)
   {
      this.addedCategory = addedCategory;
   }
   
   /**
    * Updates the categories for the current document
    *  
    * @return The outcome
    */
   public String saveCategories()
   {
      String outcome = "cancel";
      
      UserTransaction tx = null;
      
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance());
         tx.begin();
         
         // firstly retrieve all the properties for the current node
         Map<QName, Serializable> updateProps = this.nodeService.getProperties(
               getDocument().getNodeRef());
         
         // create a node ref representation of the selected id and set the new properties
         updateProps.put(ContentModel.PROP_CATEGORIES, (Serializable)this.categories);
         
         // set the properties on the node
         this.nodeService.setProperties(getDocument().getNodeRef(), updateProps);
         
         // commit the transaction
         tx.commit();
         
         // reset the state of the current document so it reflects the changes just made
         getDocument().reset();
         
         outcome = "finish";
      }
      catch (Throwable e)
      {
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), MSG_ERROR_UPDATE_CATEGORY), e.getMessage()), e);
      }
      
      return outcome;
   }
   
   /**
    * Applies the classifiable aspect to the current document
    */
   public void applyClassifiable()
   {
      UserTransaction tx = null;
      
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance());
         tx.begin();
         
         // add the general classifiable aspect to the node
         this.nodeService.addAspect(getDocument().getNodeRef(), ContentModel.ASPECT_GEN_CLASSIFIABLE, null);
         
         // commit the transaction
         tx.commit();
         
         // reset the state of the current document
         getDocument().reset();
      }
      catch (Throwable e)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), MSG_ERROR_ASPECT_CLASSIFY), e.getMessage()), e);
      }
   }
   
   /**
    * Applies the versionable aspect to the current document
    */
   public void applyVersionable()
   {
      UserTransaction tx = null;
      
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance());
         tx.begin();
         
         // add the versionable aspect to the node
         this.nodeService.addAspect(getDocument().getNodeRef(), ContentModel.ASPECT_VERSIONABLE, null);
         
         // commit the transaction
         tx.commit();
         
         // reset the state of the current document
         getDocument().reset();
      }
      catch (Throwable e)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), MSG_ERROR_ASPECT_VERSIONING), e.getMessage()), e);
      }
   }
   
   /**
    * Action Handler to unlock a locked document
    */
   public void unlock(ActionEvent event)
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      
      UserTransaction tx = null;
      
      try
      {
         tx = Repository.getUserTransaction(fc);
         tx.begin();
         
         this.lockService.unlock(getNode().getNodeRef());
         
         String msg = Application.getMessage(fc, MSG_SUCCESS_UNLOCK);
         FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
         String formId = Utils.getParentForm(fc, event.getComponent()).getClientId(fc);
         fc.addMessage(formId + ':' + getPropertiesPanelId(), facesMsg);
         
         getNode().reset();
         
         // commit the transaction
         tx.commit();
      }
      catch (Throwable e)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               fc, Repository.ERROR_GENERIC), e.getMessage()), e);
      }
   }
   
   /**
    * Applies the inlineeditable aspect to the current document
    */
   public String applyInlineEditable()
   {
      UserTransaction tx = null;
      
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance());
         tx.begin();
         
         // add the inlineeditable aspect to the node
         Map<QName, Serializable> props = new HashMap<QName, Serializable>(1, 1.0f);
         String contentType = null;
         ContentData contentData = (ContentData)getDocument().getProperties().get(ContentModel.PROP_CONTENT);
         if (contentData != null)
         {
            contentType = contentData.getMimetype();
         }
         if (contentType != null)
         {
            // set the property to true by default if the filetype is a known content type
            if (MimetypeMap.MIMETYPE_HTML.equals(contentType) ||
                MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(contentType) ||
                MimetypeMap.MIMETYPE_XML.equals(contentType) ||
                MimetypeMap.MIMETYPE_TEXT_CSS.equals(contentType) ||
                MimetypeMap.MIMETYPE_JAVASCRIPT.equals(contentType))
            {
               props.put(ApplicationModel.PROP_EDITINLINE, true);
            }
         }
         this.nodeService.addAspect(getDocument().getNodeRef(), ApplicationModel.ASPECT_INLINEEDITABLE, props);
         
         // commit the transaction
         tx.commit();
         
         // reset the state of the current document
         getDocument().reset();
      }
      catch (Throwable e)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), MSG_ERROR_ASPECT_INLINEEDITABLE), e.getMessage()), e);
      }
      
      // force recreation of the details view - this means the properties sheet component will reinit
      return OUTCOME_RETURN;
   }
   
   /**
    * Navigates to next item in the list of content for the current Space
    */
   public void nextItem(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id != null && id.length() != 0)
      {
         List<Node> nodes = this.browseBean.getContent();
         if (nodes.size() > 1)
         {
            // perform a linear search - this is slow but stateless
            // otherwise we would have to manage state of last selected node
            // this gets very tricky as this bean is instantiated once and never
            // reset - it does not know when the document has changed etc.
            for (int i=0; i<nodes.size(); i++)
            {
               if (id.equals(nodes.get(i).getId()) == true)
               {
                  Node next;
                  // found our item - navigate to next
                  if (i != nodes.size() - 1)
                  {
                     next = nodes.get(i + 1);
                  }
                  else
                  {
                     // handle wrapping case
                     next = nodes.get(0);
                  }
                  
                  // prepare for showing details for this node
                  this.browseBean.setupContentAction(next.getId(), false);
                  break;
               }
            }
         }
      }
   }
   
   /**
    * Navigates to the previous item in the list of content for the current Space
    */
   public void previousItem(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id != null && id.length() != 0)
      {
         List<Node> nodes = this.browseBean.getContent();
         if (nodes.size() > 1)
         {
            // see above
            for (int i=0; i<nodes.size(); i++)
            {
               if (id.equals(nodes.get(i).getId()) == true)
               {
                  Node previous;
                  // found our item - navigate to previous
                  if (i != 0)
                  {
                     previous = nodes.get(i - 1);
                  }
                  else
                  {
                     // handle wrapping case
                     previous = nodes.get(nodes.size() - 1);
                  }
                  
                  // prepare for showing details for this node
                  this.browseBean.setupContentAction(previous.getId(), false);
                  break;
               }
            }
         }
      }
   }
   
   /**
    * @see org.alfresco.web.bean.BaseDetailsBean#getPropertiesPanelId()
    */
   protected String getPropertiesPanelId()
   {
      return "document-props";
   }
   
   /**
    * Returns a model for use by a template on the Document Details page.
    * 
    * @return model containing current document and current space info.
    */
   public Map getTemplateModel()
   {
      Map<String, Object> model = new HashMap<String, Object>(2, 1.0f);
      
      model.put("document", getDocument().getNodeRef());
      model.put("space", this.navigator.getCurrentNode().getNodeRef());
      model.put(TemplateService.KEY_IMAGE_RESOLVER, imageResolver);
      
      return model;
   }
   
   /**
    * Returns whether the current document is locked
    * 
    * @return true if the document is checked out
    */
   public boolean isLocked()
   {
      return getDocument().isLocked();
   }
   
   /**
    * Returns whether the current document is a working copy
    * 
    * @return true if the document is a working copy
    */
   public boolean isWorkingCopy()
   {
      return getDocument().hasAspect(ContentModel.ASPECT_WORKING_COPY);
   }
   
   /**
    * @return the working copy document Node for this document if found and the 
    *         current has permission or null if not
    */
   public Node getWorkingCopyDocument()
   {
      Node workingCopyNode = null;
      
      if (isLocked())
      {
         NodeRef workingCopyRef = this.cociService.getWorkingCopy(getDocument().getNodeRef());
         if (workingCopyRef != null)
         {
            workingCopyNode = new Node(workingCopyRef);
            
            // if the current user does not have read permission on 
            // working copy return null
            if (workingCopyNode.hasPermission(PermissionService.READ) == false)
            {
               workingCopyNode = null;
            }
         }
      }
      
      return workingCopyNode;
   }
   
   /**
    * Returns whether the current document is a working copy owned by the current User
    * 
    * @return true if the document is a working copy owner by the current User
    */
   public boolean isOwner()
   {
      return getDocument().isWorkingCopyOwner();
   }
   
   /**
    * Returns the Node this bean is currently representing
    * 
    * @return The Node
    */
   public Node getNode()
   {
      return this.browseBean.getDocument();
   }
   
   /**
    * Returns the document this bean is currently representing
    * 
    * @return The document Node
    */
   public Node getDocument()
   {
      return this.getNode();
   }

   /**
    * Returns the ml container of the document this bean is currently representing
    * 
    * @return The document multilingual container NodeRef
    */
   public Node getDocumentMlContainer()
   {
      NodeRef nodeRef = getNode().getNodeRef();
      
      return new Node(multilingualContentService.getTranslationContainer(nodeRef));
   }
   

   /**
    * Sets the lock service instance the bean should use
    * 
    * @param lockService The LockService
    */
   public void setLockService(LockService lockService)
   {
      this.lockService = lockService;
   }

   /**
    * Sets the version service instance the bean should use
    * 
    * @param versionService The VersionService
    */
   public void setVersionService(VersionService versionService)
   {
      this.versionService = versionService;
   }
   
   /**
    * Sets the checkincheckout service instance the bean should use
    * 
    * @param cociService The CheckOutCheckInService
    */
   public void setCheckOutCheckInService(CheckOutCheckInService cociService)
   {
      this.cociService = cociService;
   }

   /**
    * @param multilingualContentService the multilingualContentService to set
    */
   public void setMultilingualContentService(
            MultilingualContentService multilingualContentService) 
   {
         this.multilingualContentService = multilingualContentService;
   }
   
   /**
    * @param contentFilterLanguagesService The ContentFilterLanguagesService to set. 
    */
   public void setContentFilterLanguagesService(ContentFilterLanguagesService contentFilterLanguagesService)
   {
         this.contentFilterLanguagesService = contentFilterLanguagesService;
   }
}
