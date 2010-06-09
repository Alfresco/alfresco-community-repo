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
package org.alfresco.web.bean.ml;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.ml.EditionService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIPanel.ExpandedEvent;

public class MultilingualManageDialog extends BaseDialogBean
{
   private static final long serialVersionUID = 4168804472130450812L;

   private final String MSG_MANAGE_DETAILS_FOR="manage_multilingual_details_for";
   private static final String MSG_CURRENT = "current";
   private static final String ML_VERSION_PANEL_ID = "ml-versions-panel";
   private static final String MSG_CLOSE = "close";
   private final static String MSG_LEFT_QUOTE = "left_qoute";
   private final static String MSG_RIGHT_QUOTE = "right_quote";

   protected Map<String, Boolean> panels = new HashMap<String, Boolean>(4, 1.0f);

   transient private MultilingualContentService multilingualContentService;
   transient private ContentFilterLanguagesService contentFilterLanguagesService;
   transient private EditionService editionService;
   transient private VersionService versionService;

   private Node translationDocument;

   /** For the client side iteration on the edition hitories list, it represents the index of the list */
   private int currentEditionCursorPosition;

   /** List of client light weight edition histories */
   private List<SingleEditionBean> editionHistory = null;

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
    * Returns the Node this bean is currently representing
    *
    * @return The Node
    */
   public Node getNode()
   {
      return this.browseBean.getDocument();
   }

   /**
    * @param multilingualContentService the multilingual ContentService to set
    */
   public void setMultilingualContentService(MultilingualContentService multilingualContentService)
   {
      this.multilingualContentService = multilingualContentService;
   }

   /**
    * @return multilingualContentService
    */
   protected MultilingualContentService getMultilingualContentService()
   {
      //check for null for cluster environment
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

   /**
    * @return contentFilterLanguagesService
    */
   protected ContentFilterLanguagesService getContentFilterLanguagesService()
   {
      //check for null for cluster environment
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

   /**
    * @return editionService
    */
   protected EditionService getEditionService()
   {
      //check for null for cluster environment
      if (editionService == null)
      {
         editionService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getEditionService();
      }
      return editionService;
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
    * @return versionService
    */
   protected VersionService getVersionService()
   {
      //check for null for cluster environment
      if (versionService == null)
      {
         versionService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getVersionService();
      }
      return versionService;
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
    * @return Returns the panels expanded state map.
    */
   public Map<String, Boolean> getPanels()
   {
      return this.panels;
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getContainerTitle()
   {
       FacesContext fc = FacesContext.getCurrentInstance();
       return Application.getMessage(fc, MSG_MANAGE_DETAILS_FOR) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE)
               + getDocument().getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
   }

   @Override
   public String getCancelButtonLabel() 
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CLOSE);
   }

   /**
    * Returns a list of objects representing the translations of the current document
    *
    * @return List of translations
    */
   public List getTranslations()
   {
      List<MapNode> translations = new ArrayList<MapNode>();

      Node document = getDocument();

      boolean canNewEdtion = MultilingualUtils.canStartNewEditon(document, FacesContext.getCurrentInstance());

      if (document.hasAspect(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT) || ContentModel.TYPE_MULTILINGUAL_CONTAINER.equals(document.getType()))
      {
         Map<Locale, NodeRef> translationsMap = getMultilingualContentService().getTranslations(getDocument().getNodeRef());

         if (translationsMap != null && translationsMap.size() > 0)
         {
            for (Map.Entry entry : translationsMap.entrySet())
            {
               NodeRef nodeRef = (NodeRef) entry.getValue();

               // create a map node representation of the translation
               MapNode mapNode = new MapNode(nodeRef);

               Locale locale = (Locale) getNodeService().getProperty(nodeRef, ContentModel.PROP_LOCALE);

               String lgge = (locale != null) ?
                     // convert the locale into new ISO codes
                     getContentFilterLanguagesService().convertToNewISOCode(locale.getLanguage()).toUpperCase()
                     : null;

               mapNode.put("name", getNodeService().getProperty(nodeRef, ContentModel.PROP_NAME));
               mapNode.put("language", lgge);
               mapNode.put("url", DownloadContentServlet.generateBrowserURL(nodeRef, mapNode.getName()));

               boolean isEmpty = new Boolean(getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION));

               mapNode.put("notEmpty", !isEmpty);
               mapNode.put("userHasRight", new Boolean(canNewEdtion && !isEmpty));
               // add the client side version to the list
               translations.add(mapNode);
            }
         }
      }

      return translations;
   }

   /**
    * Init the mlContainer histories and returns an empty list to fill a rich list value without content.
    *
    * @return an empty list
    */
   public List getEmptyListAndInitEditions()
   {
      // this call ensures that the edition of the mlContainer must be
      // re-initialized.

      // remove each old mlContainer's translations of the panel list
      List<String> panelsToRemove = new ArrayList<String>();
      for(Map.Entry<String, Boolean> panel : panels.entrySet())
      {
         if(panel.getKey().startsWith(ML_VERSION_PANEL_ID))
         {
            //panels.remove(panel.getKey());
            panelsToRemove.add(panel.getKey());
         }
      }

      for(String panelId : panelsToRemove)
      {
         panels.remove(panelId);
      }

      // init the Edition histories
      initEditionHistory();
      currentEditionCursorPosition = -1;

      return new ArrayList(0);
   }

   /**
    * For the client side iteration on the edition hitories list, returns the number of editions.
    *
    * @return the number of edition of the current mlContainer
    */
   public int getEditionSize()
   {
      // return the size of the list
      return editionHistory.size();
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
      if (currentEditionCursorPosition < getEditionSize())
      {
         return editionHistory.get(currentEditionCursorPosition);
      }

      return null;
   }

   /**
    * Restore the translationf from which the ml container
    * details dialog comes.
    */
   public void resetMLDocument(ActionEvent event)
   {
      this.browseBean.setupCommonBindingProperties(this.translationDocument);
      this.browseBean.setDocument(this.translationDocument);
   }

   /**
    * Action handler to remove a custom view template from the current node
    */
   public void removeTemplate(ActionEvent event)
   {
      try
      {
         // clear template property
         this.getNodeService().setProperty(getNode().getNodeRef(), ContentModel.PROP_TEMPLATE, null);
         this.getNodeService().removeAspect(getNode().getNodeRef(), ContentModel.ASPECT_TEMPLATABLE);

         // reset node details for next refresh of details page
         getNode().reset();
      }
      catch (Exception e)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), e.getMessage()), e);
      }
   }

   /**
    * Save the state of the panel that was expanded/collapsed
    */
   public void expandPanel(ActionEvent event)
   {
      if (event instanceof ExpandedEvent)
      {
         String id = event.getComponent().getId();
         // we prefix some panels with "no-" which we remove to give consistent behaviour in the UI
         if (id.startsWith("no-") == true)
         {
            id = id.substring(3);
         }
         this.panels.put(id, ((ExpandedEvent)event).State);
      }

      String id = event.getComponent().getId();

      if(id.startsWith(ML_VERSION_PANEL_ID))
      {
         this.currentEditionCursorPosition = Integer.parseInt(id.substring("ml-versions-panel".length())) - 1;
      }
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
    * Constructs a list of objects representing the editions of the
    * logical document
    *
    * @return List of editions
    */
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
            for (Version checkVersion : versionHistory.getAllVersions())
            {
                NodeRef frozenStateNodeRef = checkVersion.getFrozenStateNodeRef();
                if (frozenStateNodeRef.getStoreRef().getIdentifier().equals("lightWeightVersionStore"))
                {
                    // It's the old one pulled back by serialization
                    // Repopulate the version history
                    NodeRef versionedNodeRef = checkVersion.getVersionedNodeRef();
                    versionHistory = versionService.getVersionHistory(versionedNodeRef);
                    break;
                }
            }

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
            clientLastVersion.put("versionLanguage", getContentFilterLanguagesService().convertToNewISOCode(language.getLanguage()).toUpperCase());

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
   @Override
   protected String getDefaultCancelOutcome()
   {
      resetMLDocument(null);
      return "dialog:close";
   }
}
