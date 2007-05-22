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
package org.alfresco.web.bean.spaces;

import java.io.Serializable;

import javax.faces.context.FacesContext;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.PropertyMap;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.CreateMultilingualPropertiesBean;
import org.alfresco.web.bean.UserPreferencesBean;

public class CreateMultilingualPropertiesWizard extends CreateSpaceWizard
{
      @Override
      protected String finishImpl(FacesContext context, String outcome) throws Exception
      {
        MLText title, description;

         // bind to the bean
         CreateMultilingualPropertiesBean createMultilingualPropertiesBean = (CreateMultilingualPropertiesBean) FacesHelper.getManagedBean(context, "CreateMultilingualPropertiesBean");
         UserPreferencesBean              userPreferencesBean              = (UserPreferencesBean)              FacesHelper.getManagedBean(context, "UserPreferencesBean");

          if (this.createFrom.equals("scratch"))
          {
          // create the space (just create a folder for now)
          NodeRef nodeRef = this.browseBean.getDocument().getNodeRef();
            // Add aspect (PropertyMap is a extension of HashMap with key LOCALE)
            PropertyMap properties = new PropertyMap();

            // Modification du casting de l objet r�cup�r� des getters de MLText en Text
            MLPropertyInterceptor.setMLAware(true);

            // MLText is a HashMap composed by key and description The Function addValue is the same that the function put() but the key is the Locale value
            Serializable oTitle       = nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
            Serializable oDescription = nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION);


            if(oTitle instanceof MLText)
            {
             title = (MLText) oTitle;
            }
            else
            {
             title = new MLText();
             title.addValue(I18NUtil.parseLocale(userPreferencesBean.getContentFilterLanguage()), oTitle.toString());
            }



            if(oDescription instanceof MLText)
            {
             description = (MLText) oDescription;
            }
            else
            {
             description = new MLText();
             title.addValue(I18NUtil.parseLocale(userPreferencesBean.getContentFilterLanguage()), oDescription.toString());
            }

             title.addValue(I18NUtil.parseLocale(createMultilingualPropertiesBean.getNewlanguage()), createMultilingualPropertiesBean.getTitle());
             description.addValue(I18NUtil.parseLocale(createMultilingualPropertiesBean.getNewlanguage()), createMultilingualPropertiesBean.getDescription());

             properties.put(ContentModel.PROP_TITLE, title);
            properties.put(ContentModel.PROP_DESCRIPTION, description);


            // Ajout de l'aspect multilingue sur le titre
            this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_TITLED, properties);
/*
             MLText descriptionss = (MLText) nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION);

             List<SelectItem> sel = new ArrayList();

             for(Map.Entry<Locale, String> dd : descriptionss.entrySet())
             {
             sel.add(new SelectItem(dd.getKey().toString(), dd.getValue()));
             }

             SelectItem[] items = new SelectItem[sel.size()];
             sel.toArray(items);
*/
             // Modification du casting de l objet r�cup�r� des getters de MLText en Text
             MLPropertyInterceptor.setMLAware(false);
          }
          else if (this.createFrom.equals("existing"))
          {
          }
          else if (this.createFrom.equals("template"))
          {
          }


          if (createMultilingualPropertiesBean.isAdd_new_properties())
          {
          createMultilingualPropertiesBean.setTitle("");
          createMultilingualPropertiesBean.setDescription("");
          createMultilingualPropertiesBean.setAdd_new_properties(false);

          return "dialog:createMultilingualProperties";
          }
          else
          {
          return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME + AlfrescoNavigationHandler.OUTCOME_SEPARATOR + "browse";
          }
      }


      /**
       * @param preferences   The UserPreferencesBean to set
       */
      public void setUserPreferencesBean(UserPreferencesBean preferences)
      {
         this.preferences = preferences;
      }

      /**
       *
       * @return the preferences of the user
       */
      public UserPreferencesBean getUserPreferencesBean()
      {
         return preferences;
      }


      /** The user preferences bean reference */
      protected UserPreferencesBean              preferences;
      protected CreateMultilingualPropertiesBean createMultilingualPropertiesBean;
}

