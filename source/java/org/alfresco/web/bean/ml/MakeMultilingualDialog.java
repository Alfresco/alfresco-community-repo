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

import java.util.Locale;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.users.UserPreferencesBean;

/**
 * Dialog bean to make a node multilingual
 *
 * @author Yannick Pignot
 */
public class MakeMultilingualDialog extends BaseDialogBean
{
   private static final long serialVersionUID = -2862474360129631088L;

   transient private MultilingualContentService multilingualContentService;

   private UserPreferencesBean userPreferencesBean;

   /** if needed, add a new translation with content after this dialog closed */
   public static final String OPT_ADD_WITH_CONTENT = "ADD_WITH_CONTENT";
   /** if needed, add a new translation without content after this dialog closed */
   public static final String OPT_ADD_WITHOUT_CONTENT = "ADD_WITHOUT_CONTENT";

   // The author is a mandatory properties
   private String author;

   // The langage of the node to make multilingual
   private String language;

   // set to true, a new translation will be added at the end of this dialog
   private boolean addTranslationAfter = true;

   // set if the new translation to add will be an empty document or not
   private String addingMode = OPT_ADD_WITH_CONTENT;

   // the node to edit
   protected Node editableNode;


   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);

      // setup the editable node
      this.editableNode = initEditableNode();

      NodeRef nodeRef = this.editableNode.getNodeRef();

      // propose the author and the language of the content for the properties of the MLContainer
      if (this.getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_AUTHOR) == true
            && this.getNodeService().getProperty(nodeRef, ContentModel.PROP_AUTHOR) != null)
      {
         setAuthor((String) this.getNodeService().getProperty(nodeRef, ContentModel.PROP_AUTHOR));
      }
      else
      {
         setAuthor("");
      }

      if (this.getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_LOCALIZED) == true
            && this.getNodeService().getProperty(nodeRef, ContentModel.PROP_LOCALE) != null)
      {
         setLanguage(((Locale) this.getNodeService().getProperty(nodeRef, ContentModel.PROP_LOCALE)).toString());
      }
      else
      {
         setLanguage(null);
      }

      addTranslationAfter = false;
      addingMode = OPT_ADD_WITH_CONTENT;
   }

   /**
    * Init the editable Node
    */
   protected Node initEditableNode()
   {
      return new Node(this.browseBean.getDocument().getNodeRef());
   }

   /* (non-Javadoc)
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      Locale locale = I18NUtil.parseLocale(getLanguage());

      NodeRef nodeRef = this.editableNode.getNodeRef();

      // make this node multilingual
      getMultilingualContentService().makeTranslation(nodeRef, locale);
      NodeRef mlContainer = getMultilingualContentService().getTranslationContainer(nodeRef);

      // if the author of the node is not set, set it with the default author name of
      // the new ML Container
      String nodeAuthor = (String) getNodeService().getProperty(nodeRef, ContentModel.PROP_AUTHOR);
      if (nodeAuthor == null || nodeAuthor.length() < 1)
         getNodeService().setProperty(nodeRef, ContentModel.PROP_AUTHOR, getAuthor());

      // set properties of the ml container
      getNodeService().setProperty(mlContainer, ContentModel.PROP_AUTHOR, getAuthor());

      return outcome;
   }

   /* (non-Javadoc)
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#doPostCommitProcessing(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      //   reset the document held by the browse bean as it's just been updated
      this.browseBean.getDocument().reset();

      // go to the set add translation dialog if asked
      // to otherwise just return
      if (this.addTranslationAfter)
      {
         if(addingMode.equalsIgnoreCase(OPT_ADD_WITHOUT_CONTENT))
         {
            return "dialog:addTranslationWithoutContent";
         }
         else
         {
            AddTranslationDialog dialog = (AddTranslationDialog) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "AddTranslationDialog");
            dialog.start(null);
            return "dialog:addTranslation";
         }
      }

      // close the dialog
      return outcome;
   }

   @Override
   public boolean getFinishButtonDisabled()
   {
      return !(author != null && author.length() != 0 && language != null && language.length() != 0);
   }


   // ------------------------------------------------------------------------------
   // Bean getters and setters

   /**
    * Returns the node being edited
    *
    * @return The node being edited
    */
   public Node getEditableNode()
   {
      return this.editableNode;
   }

   /**
    * @return the author of the new created MLContainer
    */
   public String getAuthor()
   {
      return author;
   }

   /**
    * @param author the author of new created MLContainer
    */
   public void setAuthor(String author)
   {
      this.author = author;
   }

   /**
    * @return if a new translation must be added after this dialog closes
    */
   public boolean isAddTranslationAfter()
   {
      return addTranslationAfter;
   }

   /**
    * @param addTranslationAfter set to true, a new translation will be added after this dialog closes
    */
   public void setAddTranslationAfter(boolean addTranslationAfter)
   {
      this.addTranslationAfter = addTranslationAfter;
   }

   /**
    * @return the language of the translation
    */
   public String getLanguage()
   {
      return language;
   }

   /**
    * @param language the language of the translation
    */
   public void setLanguage(String language)
   {
      this.language = language;
   }

   /**
    * @param editableNode the node will become a multilingual
    */
   public void setEditableNode(Node editableNode)
   {
      this.editableNode = editableNode;
   }

   /**
    * @return if the new translation must be added at the end of this dialog,
    * this mode defines if the translation will be an empty document or not
    */
   public String getAddingMode()
   {
      return addingMode;
   }

   /**
    * @param addingMode if the new translation must be added at the end of this dialog,
    * this mode defines if the translation will be an empty document or not
    */
   public void setAddingMode(String addingMode)
   {
      this.addingMode = addingMode;
   }

   /**
    * @return the complete list of available languages for the multilinguism
    */
   public SelectItem[] getFilterLanguages()
   {
      return userPreferencesBean.getContentFilterLanguages(false);
   }

   public void setMultilingualContentService(
         MultilingualContentService multilingualContentService)
   {
      this.multilingualContentService = multilingualContentService;
   }
   
   private MultilingualContentService getMultilingualContentService()
   {
      if (multilingualContentService == null)
      {
         multilingualContentService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getMultilingualContentService();
      }
      return multilingualContentService;
   }

   public void setUserPreferencesBean(UserPreferencesBean userPreferencesBean)
   {
      this.userPreferencesBean = userPreferencesBean;
   }
}
