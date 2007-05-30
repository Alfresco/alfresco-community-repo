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
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.UserPreferencesBean;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.Utils;

/**
 * Dialog bean to add a new translation without content. I means, a new node is created
 * but it doesn't content the propertie <code>{http://www.alfresco.org/model/content/1.0}content</code>
 *
 * @author yanipig
 */
public class AddTranslationWithoutContentDialog extends BaseDialogBean
{
   private MultilingualContentService multilingualContentService;
   private UserPreferencesBean userPreferencesBean;

   // the translation being to be created
   protected NodeRef newTranslation;

   private String name;
   private String title;
   private String description;
   private String author;
   private String language;


   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);

      name = null;
      title = null;
      description = null;
      author = null;
      language = null;
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // get the reference node
      NodeRef refNode = this.browseBean.getDocument().getNodeRef();

      // get the required properties to create the new node and to make it multilingual
      Locale locale = I18NUtil.parseLocale(language);

      // add the empty translation
      newTranslation = multilingualContentService.addEmptyTranslation(refNode, name, locale);

      // set the properties
      nodeService.setProperty(newTranslation, ContentModel.PROP_DESCRIPTION, description);
      nodeService.setProperty(newTranslation, ContentModel.PROP_AUTHOR, author);
      nodeService.setProperty(newTranslation, ContentModel.PROP_TITLE, title);

      // Get the content data of the 
      ContentData newTranslationContentData = fileFolderService.getReader(newTranslation).getContentData();
      
      // set the current browse node
      Node browse = new Node(newTranslation);

      Map<String, Object> browseProp = browse.getProperties();
      browseProp.put("size", newTranslationContentData.getSize());
      browseProp.put("mimetype", newTranslationContentData.getMimetype());
      browseProp.put("cm:content", newTranslationContentData);
      browseProp.put("fileType32", Utils.getFileTypeImage(name, false));
      browseProp.put("url", DownloadContentServlet.generateDownloadURL(newTranslation, name));

      this.browseBean.setDocument(browse);

      return outcome;
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
    * @return the name
    */
   public String getName()
   {
      return name;
   }

   /**
    * @param name the name to set
    */
   public void setName(String name)
   {
      this.name = name;
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
}
