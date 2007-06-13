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

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.UserPreferencesBean;
import org.alfresco.web.bean.content.AddContentDialog;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.Utils;

/**
 * Dialog bean to upload a new document and to add it to an existing MLContainer.
 *
 * @author yanipig
 */
public class AddTranslationDialog extends AddContentDialog
{
   private MultilingualContentService multilingualContentService;
   private UserPreferencesBean userPreferencesBean;

   // the multilingual container where to add this translation
   protected NodeRef mlTranslation;

   // Locale of the new translation
   private String language;

   //  languages available in the ML container yet
   private SelectItem[] unusedLanguages;


   /* (non-Javadoc)
    * @see org.alfresco.web.bean.content.AddContentDialog#init(java.util.Map)
    */
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);

      this.language = null;
      this.mlTranslation = this.browseBean.getDocument().getNodeRef();
      setFileName(null);
      unusedLanguages = null;
   }

   /* (non-Javadoc)
    * @see org.alfresco.web.bean.content.AddContentDialog#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // add the new file to the repository
      outcome = super.finishImpl(context, outcome);
      
      // add a new translation
      multilingualContentService.addTranslation(this.createdNode, this.mlTranslation, I18NUtil.parseLocale(this.language));
      
      // Get the content data of the translation
      ContentData contentData = fileFolderService.getReader(this.createdNode).getContentData();
      
      Node createdNode = new Node(this.createdNode);
      
      Map<String, Object> browseProp = createdNode.getProperties();
      browseProp.put("size", contentData.getSize());
      browseProp.put("mimetype", contentData.getMimetype());
      browseProp.put("cm:content", contentData);
      browseProp.put("fileType32", Utils.getFileTypeImage(createdNode.getName(), false));
      browseProp.put("url", DownloadContentServlet.generateDownloadURL(this.createdNode, createdNode.getName()));
      
      this.browseBean.setDocument(createdNode);
      
      return outcome;
   }

   @Override
   protected String getDefaultFinishOutcome()
   {
      return "showDocDetails";
   }

   @Override
   public String cancel()
   {
      super.cancel();

      return getDefaultFinishOutcome();
   }

   public boolean finishButtonDisabled()
   {
      return author == null || author.length() < 1 || language == null;
   }

   /**
    * @return the locale of this new translation
    */
   public String getLanguage()
   {
      return language;
   }

   /**
    * @param language the locale of this new translation
    */
   public void setLanguage(String language)
   {
      this.language = language;
   }

   /**
    * @param unusedLanguages
    */
   public void setUnusedLanguages(SelectItem[] unusedLanguages)
   {
      this.unusedLanguages = unusedLanguages;
   }

   /**
    * Method calls by the dialog to limit the list of languages.
    *
    * @return the list of availables translation in the MLContainer
    */

   public SelectItem[] getUnusedLanguages()
   {
      if(unusedLanguages == null)
      {
         unusedLanguages = userPreferencesBean.getAvailablesContentFilterLanguages(this.mlTranslation, false);
      }

      return unusedLanguages;
   }

   public MultilingualContentService getMultilingualContentService()
   {
      return multilingualContentService;
   }

   public void setMultilingualContentService(MultilingualContentService multilingualContentService)
   {
      this.multilingualContentService = multilingualContentService;
   }

   public UserPreferencesBean getUserPreferencesBean()
   {
      return userPreferencesBean;
   }

   public void setUserPreferencesBean(UserPreferencesBean userPreferencesBean)
   {
      this.userPreferencesBean = userPreferencesBean;
   }
}
