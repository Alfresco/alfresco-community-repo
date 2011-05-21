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
package org.alfresco.web.bean.content;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.ml.EditionService;
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
import org.alfresco.web.bean.BaseDetailsBean;
import org.alfresco.web.bean.dialog.NavigationSupport;
import org.alfresco.web.bean.ml.SingleEditionBean;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.Utils.URLMode;
import org.alfresco.web.ui.common.component.UIActionLink;

/**
 * Backing bean providing access to the details of a document
 *
 * @author gavinc
 */
public class DocumentDetailsDialog extends BaseDetailsBean implements  NavigationSupport
{
   private static final long serialVersionUID = -8579599071702546214L;

   private static final String OUTCOME_RETURN = null;

   private static final String MSG_HAS_FOLLOWING_CATEGORIES = "has_following_categories";
   private static final String MSG_NO_CATEGORIES_APPLIED = "no_categories_applied";
   private static final String MSG_SUCCESS_UNLOCK = "success_unlock";
   private static final String MSG_CURRENT = "current";
   private static final String MSG_ERROR_ASPECT_INLINEEDITABLE = "error_aspect_inlineeditable";
   private static final String MSG_ERROR_ASPECT_VERSIONING = "error_aspect_versioning";
   private static final String MSG_ERROR_ASPECT_CLASSIFY = "error_aspect_classify";
   private static final String MSG_DETAILS_OF = "details_of";
   private static final String MSG_LOCATION = "location";
   private final static String MSG_LEFT_QUOTE = "left_qoute";
   private final static String MSG_RIGHT_QUOTE = "right_quote";
   private final static String MSG_CLOSE = "close";

   private static final String ML_VERSION_PANEL_ID = "ml-versions-panel";

   private final static String DOC_DETAILS_STACK = "_alfDocDetailsStack";

   transient protected LockService lockService;
   transient protected VersionService versionService;
   transient protected CheckOutCheckInService cociService;
   transient protected MultilingualContentService multilingualContentService;
   transient protected ContentFilterLanguagesService contentFilterLanguagesService;
   transient protected EditionService editionService;

   private Node translationDocument;

   /** List of client light weight edition histories */
   private List<SingleEditionBean> editionHistory = null;

   /** For the client side iteration on the edition hitories list, it represents the index of the list */
   private int currentEditionCursorPosition;


   // ------------------------------------------------------------------------------
   // Construction

   /**
    * Default constructor
    */
   public DocumentDetailsDialog()
   {
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
   }

   @Override
   @SuppressWarnings("unchecked")
   public void init(Map<String, String> parameters)
   {   
       super.init(parameters);
       //Remember active node.
       Stack stack = getRecentNodeRefsStack();
       stack.push(getNode().getNodeRef().getId());
   }
   
   @Override
   @SuppressWarnings("unchecked")
   public void restored()
   {
       super.restored();
       Stack stack = getRecentNodeRefsStack();
       if (stack.isEmpty() == false)
       {
           browseBean.setupContentAction((String) stack.peek(), true);
       }
   }
   
   @Override
   @SuppressWarnings("unchecked")
   public String cancel()
   {
       Stack stack = getRecentNodeRefsStack();
       if (stack.isEmpty() == false)
       {
           stack.pop();
       }
       return super.cancel();
   }
   
   @SuppressWarnings("unchecked")
   private Stack getRecentNodeRefsStack()
   {
       FacesContext fc = FacesContext.getCurrentInstance();
       Stack stack = (Stack) fc.getExternalContext().getSessionMap().get(DOC_DETAILS_STACK);
       if (stack == null)
       {
           stack = new Stack();
           fc.getExternalContext().getSessionMap().put(DOC_DETAILS_STACK, stack);
       }
       return stack;
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
    * @return the translation document for this node
    */   
   public Node getTranslationDocument() 
   {
      return translationDocument;
   }

   /**
    * Before opening the ml container details, remeber the translation
    * from which the action comes.
    *
    * @param node
    */
   public void setTranslationDocument(Node node)
   {
      this.translationDocument = node;
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
         if (getNodeService().exists(destRef))
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
    * Fixes an issue reported in https://issues.alfresco.com/jira/browse/ETWOONE-92
    * 
    * @return Returns action 
    */
   public String editContentProperties()
   {
       NodeRef nodeRef = getDocument().getNodeRef();
       if (this.getNodeService().exists(nodeRef))
       {
           navigator.setupDispatchContext(getDocument());
           return "dialog:editContentProperties";
       }
       else
       {
           Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] {nodeRef}) );
           return "browse";
       }
       
   }

   /**
    * Save the state of the panel that was expanded/collapsed
    */
   public void expandPanel(ActionEvent event)
   {
      super.expandPanel(event);
      String id = event.getComponent().getId();

      if(id.startsWith(ML_VERSION_PANEL_ID))
      {
         this.currentEditionCursorPosition = Integer.parseInt(id.substring("ml-versions-panel".length())) - 1;
      }
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
         VersionHistory history = this.getVersionService().getVersionHistory(getDocument().getNodeRef());

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

               if(getDocument().hasAspect(ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION))
               {
                  clientVersion.put("url", null);
               }
               else
               {
                  clientVersion.put("url", DownloadContentServlet.generateBrowserURL(version.getFrozenStateNodeRef(),
                        clientVersion.getName()));
               }


               // add the client side version to the list
               versions.add(clientVersion);
            }
         }
      }

      return versions;
   }

   /**
    * For the client side iteration on the edition hitories list,
    * return the next edition history.
    *
    * @return a light weight representation of an edition history
    */
   public SingleEditionBean getNextSingleEditionBean()
   {
      currentEditionCursorPosition++;

      return getCurrentSingleEditionBean();
   }

   /**
    * For the client side iteration on the edition hitories list,
    * return the current edition history.
    *
    * @return a light weight representation of an edition history
    */
   public SingleEditionBean getCurrentSingleEditionBean()
   {
      return editionHistory.get(currentEditionCursorPosition);
   }

   /**
    * Constructs a list of objects representing the editions of the
    * logical document
    *
    * @return List of editions
    */
   @SuppressWarnings("unused")
   private List<SingleEditionBean> initEditionHistory()
   {
      // get the mlContainer
      NodeRef mlContainer = getDocumentMlContainer().getNodeRef();

      // get all editions (in descending order - ie. most recent first)
      List<Version> orderedEditionList = new ArrayList<Version>(getEditionService().getEditions(mlContainer).getAllVersions());

      // the list of Single Edition Bean to return
      editionHistory = new ArrayList<SingleEditionBean>(orderedEditionList.size());

      boolean firstEdition = true;

      // for each edition, init a SingleEditionBean
      for (Version edition : orderedEditionList)
      {
         SingleEditionBean editionBean = new SingleEditionBean();

         MapNode clientEdition = new MapNode(edition.getFrozenStateNodeRef());

         String editionLabel = edition.getVersionLabel();
         if (firstEdition)
         {
            editionLabel += " (" + Application.getMessage(FacesContext.getCurrentInstance(), MSG_CURRENT) + ")";
         }

         clientEdition.put("editionLabel", editionLabel);
         clientEdition.put("editionNotes", edition.getDescription());
         clientEdition.put("editionAuthor", edition.getCreator());
         clientEdition.put("editionDate", edition.getCreatedDate());

         // Set the edition of the edition bean
         editionBean.setEdition(clientEdition);

         // get translations
         List<VersionHistory> translationHistories = null;

         if (firstEdition)
         {
            // Get the translations because the current edition doesn't content link with its
            // translation in the version store.
            Map<Locale, NodeRef> translations = getMultilingualContentService().getTranslations(mlContainer);
            translationHistories = new ArrayList<VersionHistory>(translations.size());
            for (NodeRef translation : translations.values())
            {
               translationHistories.add(getVersionService().getVersionHistory(translation));
            }
         }
         else
         {
            translationHistories = getEditionService().getVersionedTranslations(edition);
         }

         // add each translation in the SingleEditionBean
         for (VersionHistory versionHistory : translationHistories)
         {
            // get the list of versions (in descending order - ie. most recent first)
            List<Version> orderedVersions = new ArrayList<Version>(versionHistory.getAllVersions());

            // the last version (ie. most recent) is the first version of the list
            Version lastVersion = orderedVersions.get(0);

            // get the properties of the lastVersion
            Map<QName, Serializable> lastVersionProperties = getEditionService().getVersionedMetadatas(lastVersion);
            Locale language  = (Locale) lastVersionProperties.get(ContentModel.PROP_LOCALE);

            // create a map node representation of the last version
            MapNode clientLastVersion = new MapNode(lastVersion.getFrozenStateNodeRef());

            clientLastVersion.put("versionName", lastVersionProperties.get(ContentModel.PROP_NAME));
            // use the node service for the description to ensure that the returned value is a text and not a MLText
            clientLastVersion.put("versionDescription", getNodeService().getProperty(lastVersion.getFrozenStateNodeRef(), ContentModel.PROP_DESCRIPTION));
            clientLastVersion.put("versionAuthor", lastVersionProperties.get(ContentModel.PROP_AUTHOR));
            clientLastVersion.put("versionCreatedDate",  lastVersionProperties.get(ContentModel.PROP_CREATED));
            clientLastVersion.put("versionModifiedDate", lastVersionProperties.get(ContentModel.PROP_MODIFIED));
            clientLastVersion.put("versionLanguage", this.getContentFilterLanguagesService().convertToNewISOCode(language.getLanguage()).toUpperCase());

            if(getNodeService().hasAspect(lastVersion.getFrozenStateNodeRef(), ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION))
            {
               clientLastVersion.put("versionUrl", null);
            }
            else
            {
               clientLastVersion.put("versionUrl", DownloadContentServlet.generateBrowserURL(lastVersion.getFrozenStateNodeRef(), clientLastVersion.getName()));
            }

            // add a translation of the editionBean
            editionBean.addTranslations(clientLastVersion);
         }
         editionHistory.add(editionBean);
         firstEdition = false;
      }

      return editionHistory;
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
         Collection<NodeRef> categories = (Collection<NodeRef>)this.getNodeService().getProperty(
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
               if (this.getNodeService().exists(ref))
               {
                  builder.append("<li>");
                  builder.append(Utils.encode(Repository.getNameForCategoryNode(this.getNodeService(), ref)));
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
    * Applies the classifiable aspect to the current document
    */
   public void applyClassifiable()
   {
      try
      {
         RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(FacesContext.getCurrentInstance());
         RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
         {
            public Object execute() throws Throwable
            {
               // add the general classifiable aspect to the node
               getNodeService().addAspect(getDocument().getNodeRef(), ContentModel.ASPECT_GEN_CLASSIFIABLE, null);
               return null;
            }
         };
         txnHelper.doInTransaction(callback);

         // reset the state of the current document
         getDocument().reset();
      }
      catch (Throwable e)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), MSG_ERROR_ASPECT_CLASSIFY), e.getMessage()), e);
         ReportedException.throwIfNecessary(e);
      }
   }

   /**
    * Applies the versionable aspect to the current document
    */
   public void applyVersionable()
   {
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
         RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
         {
            public Object execute() throws Throwable
            {
               // add the versionable aspect to the node
               getNodeService().addAspect(getDocument().getNodeRef(), ContentModel.ASPECT_VERSIONABLE, null);
               return null;
            }
         };
         txnHelper.doInTransaction(callback);

         // reset the state of the current document
         getDocument().reset();

         // get hold of the main property sheet on the page and remove the children to force a refresh
         UIComponent comp = context.getViewRoot().findComponent("dialog:dialog-body:document-props");
         if (comp != null)
         {
            comp.getChildren().clear();
         }
      }
      catch (Throwable e)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), MSG_ERROR_ASPECT_VERSIONING), e.getMessage()), e);
         ReportedException.throwIfNecessary(e);
      }
   }

   /**
    * Action Handler to unlock a locked document
    */
   public void unlock(final ActionEvent event)
   {
      final FacesContext fc = FacesContext.getCurrentInstance();

      try
      {
         RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(FacesContext.getCurrentInstance());
         RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
         {
            public Object execute() throws Throwable
            {
               getLockService().unlock(getNode().getNodeRef());

               String msg = Application.getMessage(fc, MSG_SUCCESS_UNLOCK);
               FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
               String formId = Utils.getParentForm(fc, event.getComponent()).getClientId(fc);
               fc.addMessage(formId + ':' + getPropertiesPanelId(), facesMsg);

               getNode().reset();
               return null;
            }
         };
         txnHelper.doInTransaction(callback);
      }
      catch (Throwable e)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               fc, Repository.ERROR_GENERIC), e.getMessage()), e);
         ReportedException.throwIfNecessary(e);
      }
   }

   /**
    * Applies the inlineeditable aspect to the current document
    */
   public String applyInlineEditable()
   {
      try
      {
         RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(FacesContext.getCurrentInstance());
         RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
         {
            public Object execute() throws Throwable
            {
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
               getNodeService().addAspect(getDocument().getNodeRef(), ApplicationModel.ASPECT_INLINEEDITABLE, props);

               return null;
            }
         };
         txnHelper.doInTransaction(callback);

         // reset the state of the current document
         getDocument().reset();
      }
      catch (Throwable e)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), MSG_ERROR_ASPECT_INLINEEDITABLE), e.getMessage()), e);
         ReportedException.throwIfNecessary(e);
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
                  getRecentNodeRefsStack().clear();
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
                  getRecentNodeRefsStack().clear();
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
         NodeRef workingCopyRef = this.getCheckOutCheckInService().getWorkingCopy(getDocument().getNodeRef());
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
      Node currentNode = getNode();

      if(ContentModel.TYPE_MULTILINGUAL_CONTAINER.equals(currentNode.getType()))
      {
         return currentNode;
      }
      else
      {
         NodeRef nodeRef = getNode().getNodeRef();

         return new Node(getMultilingualContentService().getTranslationContainer(nodeRef));
      }
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

   protected LockService getLockService()
   {
      if (lockService == null)
      {
         lockService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getLockService();
      }
      return lockService;
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

   protected VersionService getVersionService()
   {
      if (versionService == null)
      {
         versionService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getVersionService();
      }
      return versionService;
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

   protected CheckOutCheckInService getCheckOutCheckInService()
   {
      if (cociService == null)
      {
         cociService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getCheckOutCheckInService();
      }
      return cociService;
   }

   /**
    * @param multilingualContentService the multilingual ContentService to set
    */
   public void setMultilingualContentService(MultilingualContentService multilingualContentService)
   {
      this.multilingualContentService = multilingualContentService;
   }

   protected MultilingualContentService getMultilingualContentService()
   {
      if (multilingualContentService == null)
      {
         multilingualContentService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getMultilingualContentService();
      }
      return multilingualContentService;
   }

   /**
    * @param contentFilterLanguagesService The Content Filter Languages Service to set.
    */
   public void setContentFilterLanguagesService(ContentFilterLanguagesService contentFilterLanguagesService)
   {
      this.contentFilterLanguagesService = contentFilterLanguagesService;
   }

   protected ContentFilterLanguagesService getContentFilterLanguagesService()
   {
      if (contentFilterLanguagesService == null)
      {
         contentFilterLanguagesService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getContentFilterLanguagesService();
      }
      return contentFilterLanguagesService;
   }

   /**
    * @param EditionService The Edition Service to set.
    */
   public void setEditionService(EditionService editionService)
   {
      this.editionService = editionService;
   }

   protected EditionService getEditionService()
   {
      if (editionService == null)
      {
         editionService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getEditionService();
      }
      return editionService;
   }

   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CLOSE);
   }

   public String getContainerSubTitle()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_LOCATION) + ": " + 
      getDocument().getNodePath().toDisplayPath(getNodeService(), getPermissionService());
   }

   public String getContainerTitle()
   {
       FacesContext fc = FacesContext.getCurrentInstance();
       return Application.getMessage(fc, MSG_DETAILS_OF) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE) + getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
   }

   public String getCurrentItemId()
   {
      return getId();
   }

   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return null;
   }

   public String getOutcome()
   {
      return "dialog:close:dialog:showDocDetails";
   }
}
