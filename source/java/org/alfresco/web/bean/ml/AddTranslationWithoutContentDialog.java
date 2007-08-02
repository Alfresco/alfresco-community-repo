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
package org.alfresco.web.bean.ml;

import java.util.Locale;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.bean.UserPreferencesBean;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;

/**
 * Dialog bean to add a new translation without content. I means, a new node is created
 * but it doesn't content the propertie <code>{http://www.alfresco.org/model/content/1.0}content</code>
 *
 * @author Yannick Pignot
 */
public class AddTranslationWithoutContentDialog extends BaseDialogBean
{
   private MultilingualContentService multilingualContentService;
   private UserPreferencesBean userPreferencesBean;

   // the translation being to be created
   protected NodeRef newTranslation;

   private String title;
   private String description;
   private String author;
   private String language;
   private boolean showOtherProperties;

   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);

      title = null;
      description = null;
      author = null;
      language = null;
      showOtherProperties = true;
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // get the reference node
      NodeRef refNode = this.browseBean.getDocument().getNodeRef();

      // get the required properties to create the new node and to make it multilingual
      Locale locale = I18NUtil.parseLocale(language);

      // add the empty translation
      newTranslation = multilingualContentService.addEmptyTranslation(refNode, null, locale);

      // set the properties
      nodeService.setProperty(newTranslation, ContentModel.PROP_DESCRIPTION, description);
      nodeService.setProperty(newTranslation, ContentModel.PROP_AUTHOR, author);
      nodeService.setProperty(newTranslation, ContentModel.PROP_TITLE, title);

      // redirect the user according the value of (show other properties)
      if(showOtherProperties)
      {
          this.browseBean.setDocument(new Node(this.newTranslation));
          return "dialog:setContentProperties";
      }
      else
      {
          return "browse";
      }
   }

   /**
    * @param userPreferencesBean the userPreferencesBean to set
    */
   public void setUserPreferencesBean(UserPreferencesBean userPreferencesBean)
   {
      this.userPreferencesBean = userPreferencesBean;
   }

   /**
    * @param multilingualContentService the multilingualContentService to set
    */
   public void setMultilingualContentService(MultilingualContentService multilingualContentService)
   {
      this.multilingualContentService = multilingualContentService;
   }

   /**
    * @return the author
    */
   public String getAuthor()
   {
      return author;
   }

   /**
    * @param author the author to set
    */
   public void setAuthor(String author)
   {
      this.author = author;
   }

   /**
    * @return the description
    */
   public String getDescription()
   {
      return description;
   }

   /**
    * @param description the description to set
    */
   public void setDescription(String description)
   {
      this.description = description;
   }

   /**
    * @return the language
    */
   public String getLanguage()
   {
      return language;
   }

   /**
    * @param language the language to set
    */
   public void setLanguage(String language)
   {
      this.language = language;
   }

   /**
    * @return the title
    */
   public String getTitle()
   {
      return title;
   }

   /**
    * @param title the title to set
    */
   public void setTitle(String title)
   {
      this.title = title;
   }

   /**
    * @return the unusedLanguages
    */
   public SelectItem[] getUnusedLanguages()
   {
      return userPreferencesBean.getAvailablesContentFilterLanguages(this.browseBean.getDocument().getNodeRef(), false);
   }

   /**
    * @return the showOtherProperties
    */
   public boolean isShowOtherProperties()
   {
       return showOtherProperties;
       }

   /**
    * @param showOtherProperties the showOtherProperties to set
    */
   public void setShowOtherProperties(boolean showOtherProperties)
   {
       this.showOtherProperties = showOtherProperties;
   }
}
