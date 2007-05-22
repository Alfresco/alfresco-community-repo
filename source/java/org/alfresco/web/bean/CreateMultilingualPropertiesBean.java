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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;

public class CreateMultilingualPropertiesBean
{
 /** binding with edit of Other Options */
 private String    language           = null;
 private String    newlanguage        = null;
 private String    title              = null;
 private String    description        = null;
 protected boolean add_new_properties = false;

 /** The NodeService to be used by the bean */
 protected NodeService nodeService;
 /** The user preferences bean reference */
 protected UserPreferencesBean preferences;


  /**
   * @param nodeService The NodeService to set.
   */
  public void setNodeService(NodeService nodeService)
  {
     this.nodeService = nodeService;
  }

  public String getLanguage()
  {
   return this.language;
  }

  public void setLanguage(String x)
  {
   this.language = x;
   Application.setLanguage(FacesContext.getCurrentInstance(), this.language);
  }

  public void setNewlanguage(String x)
  {
   this.newlanguage = x;
  }

  public String getNewlanguage()
  {
   return this.newlanguage;
  }


  public void setUserPreferencesBean(UserPreferencesBean preferences)
  {
    this.preferences = preferences;
  }

  public UserPreferencesBean getUserPreferencesBean()
  {
    return preferences;
  }

  public void setTitle(String x)
  {
   this.title = x;
  }

  public void setDescription(String x)
  {
   this.description = x;
  }

  public String getTitle()
  {
   return this.title;
  }

  public String getDescription()
  {
   return this.description;
  }


  // Display the list of all multilingual properties
  public SelectItem[] getListAllDescriptionsProperties()
  {
   MLPropertyInterceptor.setMLAware(true);
   BrowseBean browseBean = (BrowseBean) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "BrowseBean");

   // create the space (just create a folder for now)
   NodeRef nodeRef = browseBean.getDocument().getNodeRef();

   if(nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION) != null)
   {
    List<SelectItem> sel = new ArrayList<SelectItem>();

    if (nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION) instanceof MLText)
    {
     MLText descriptions = (MLText) nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION);

     for(Map.Entry<Locale, String> description : descriptions.entrySet())
     {
      sel.add(new SelectItem(description.getKey().toString(), description.getValue().toString()));
     }

     MLPropertyInterceptor.setMLAware(false);

     // Create the table
     SelectItem[] items = new SelectItem[sel.size()];

     // Copy into table
     sel.toArray(items);

     if(sel.size() > 0)
     {
     return items;
     }
     else
     {
        SelectItem[] elements = { new SelectItem("","")}; return elements;
     }
    }
    else
    {
     SelectItem[] elements = { new SelectItem("","")}; return elements;
    }
   }
   else
   {
   SelectItem[] elements = { new SelectItem("")}; return elements;
   }
  }

  // Display the list of all multilingual properties
  public SelectItem[] getListAllTitlesProperties()
  {
   MLPropertyInterceptor.setMLAware(true);
   BrowseBean browseBean = (BrowseBean) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "BrowseBean");

   // create the space (just create a folder for now)
   NodeRef nodeRef = browseBean.getDocument().getNodeRef();

   if(nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION) != null)
   {
    List<SelectItem> sel = new ArrayList<SelectItem>();

    if (nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION) instanceof MLText)
    {
     MLText descriptions = (MLText) nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);

     for(Map.Entry<Locale, String> description : descriptions.entrySet())
     {
      sel.add(new SelectItem(description.getKey().toString(), description.getValue()));
     }

     MLPropertyInterceptor.setMLAware(false);
     // Create the table
     SelectItem[] items = new SelectItem[sel.size()];
     // Copy into table
     sel.toArray(items);

     if(sel.size() > 0)
     {
      return items;
     }
     else
     {
      SelectItem[] elements = { new SelectItem("","")}; return elements;
     }
    }
    else
    {
     SelectItem[] elements = { new SelectItem("","")}; return elements;
    }
   }
   else
   {
    SelectItem[] elements = { new SelectItem("","")}; return elements;
   }
  }

  public SelectItem[] getContentFilterLanguages()
  {
     return preferences.getContentFilterLanguages(false);
  }
  public boolean isAdd_new_properties()          {return this.add_new_properties;}
  public void    setAdd_new_properties(boolean x){this.add_new_properties = x;}
 }

