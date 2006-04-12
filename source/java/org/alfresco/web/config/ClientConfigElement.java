/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.config;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.element.ConfigElementAdapter;

/**
 * Custom config element that represents config values for the client
 * 
 * @author Kevin Roast
 */
public class ClientConfigElement extends ConfigElementAdapter
{
   public static final String CONFIG_ELEMENT_ID = "client";
   
   private String fromEmailAddress = "alfresco@alfresco.org";   
   private String errorPage = null;
   private String loginPage = null;
   private int recentSpacesItems = 6;
   private boolean shelfVisible = true;
   private int searchMinimum = 3;
   private boolean forceAndTerms = false;
   private int searchMaxResults = -1;
   private String helpUrl = null;
   private String editLinkType = "http";
   private String homeSpacePermission = null;
   
   /**
    * Default Constructor
    */
   public ClientConfigElement()
   {
      super(CONFIG_ELEMENT_ID);
   }
   
   /**
    * Constructor
    * 
    * @param name Name of the element this config element represents
    */
   public ClientConfigElement(String name)
   {
      super(name);
   }

   /**
    * @see org.alfresco.config.element.ConfigElementAdapter#combine(org.alfresco.config.ConfigElement)
    */
   public ConfigElement combine(ConfigElement configElement)
   {
      ClientConfigElement existingElement = (ClientConfigElement)configElement;
      ClientConfigElement newElement = new ClientConfigElement();
      
      // set those values that have changed
      if (existingElement.getEditLinkType() == null)
      {
         newElement.setEditLinkType(this.editLinkType);
      }
      else
      {
         newElement.setEditLinkType(existingElement.getEditLinkType());
      }
      
      if (existingElement.getErrorPage() == null)
      {
         newElement.setErrorPage(this.errorPage);
      }
      else
      {     
         newElement.setErrorPage(existingElement.getErrorPage());
      }
      
      if (existingElement.getLoginPage() == null)
      {
         newElement.setLoginPage(this.loginPage);
      }
      else
      {
         newElement.setLoginPage(existingElement.getLoginPage());
      }
      
      if (existingElement.getHelpUrl() == null )
      {
         newElement.setHelpUrl(this.helpUrl);
      }
      else
      {
         newElement.setHelpUrl(existingElement.getHelpUrl());
      }
      
      if (existingElement.getHomeSpacePermission() == null)
      {
         newElement.setHomeSpacePermission(this.homeSpacePermission);
      }
      else
      {
         newElement.setHomeSpacePermission(existingElement.getHomeSpacePermission());
      }
      
      // override default values if they have changed
      if (existingElement.getRecentSpacesItems() != newElement.getRecentSpacesItems())
      {
         newElement.setRecentSpacesItems(existingElement.getRecentSpacesItems());
      }
      
      if (existingElement.getSearchMinimum() != newElement.getSearchMinimum())
      {
         newElement.setSearchMinimum(existingElement.getSearchMinimum());
      }
      
      if (existingElement.getForceAndTerms() != newElement.getForceAndTerms())
      {
         newElement.setForceAndTerms(existingElement.getForceAndTerms());
      }
      
      if (existingElement.getSearchMaxResults() != newElement.getSearchMaxResults())
      {
         newElement.setSearchMaxResults(existingElement.getSearchMaxResults());
      }
      
      if (existingElement.isShelfVisible() != newElement.isShelfVisible())
      {
         newElement.setShelfVisible(existingElement.isShelfVisible());
      }
      
      if (existingElement.getFromEmailAddress() != null && 
          (existingElement.getFromEmailAddress().equals(newElement.getFromEmailAddress()) == false))
      {
         newElement.setFromEmailAddress(existingElement.getFromEmailAddress());
      }
      
      return newElement;
   }
   
   /**
    * @return Returns the recentSpacesItems.
    */
   public int getRecentSpacesItems()
   {
      return this.recentSpacesItems;
   }

   /**
    * @param recentSpacesItems The recentSpacesItems to set.
    */
   /*package*/ void setRecentSpacesItems(int recentSpacesItems)
   {
      this.recentSpacesItems = recentSpacesItems;
   }
   
   /**
    * @return Returns if the shelf component is visible by default.
    */
   public boolean isShelfVisible()
   {
      return this.shelfVisible;
   }

   /**
    * @param shelfVisible True if the shelf component is visible by default.
    */
   /*package*/ void setShelfVisible(boolean shelfVisible)
   {
      this.shelfVisible = shelfVisible;
   }

   /**
    * @return The error page the application should use
    */
   public String getErrorPage()
   {
      return this.errorPage;
   }

   /**
    * @param errorPage Sets the error page
    */
   /*package*/ void setErrorPage(String errorPage)
   {
      this.errorPage = errorPage;
   }
   
   /**
    * @return Returns the login Page.
    */
   public String getLoginPage()
   {
      return this.loginPage;
   }
   
   /**
    * @param loginPage The login Page to set.
    */
   /*package*/ void setLoginPage(String loginPage)
   {
      this.loginPage = loginPage;
   }
   
   /**
    * @return Returns the help Url.
    */
   public String getHelpUrl()
   {
      return this.helpUrl;
   }

   /**
    * @param helpUrl The help Url to set.
    */
   /*package*/ void setHelpUrl(String helpUrl)
   {
      this.helpUrl = helpUrl;
   }
   
   /**
    * @return Returns the from email address, if one has not been set 
    *         alfresco@alfresco.org will be returned
    */
   public String getFromEmailAddress()
   {
      return this.fromEmailAddress;
   }

   /**
    * @param fromEmailAddress The from email address to set
    */
   /*package*/ void setFromEmailAddress(String fromEmailAddress)
   {
      this.fromEmailAddress = fromEmailAddress;
   }
   
   /**
    * @return Returns the edit link type.
    */
   public String getEditLinkType()
   {
      return this.editLinkType;
   }

   /**
    * @param editLinkType The edit link type to set.
    */
   /*package*/ void setEditLinkType(String editLinkType)
   {
      this.editLinkType = editLinkType;
   }

   /**
    * @return Returns the search minimum number of characters.
    */
   public int getSearchMinimum()
   {
      return this.searchMinimum;
   }

   /**
    * @param searchMinimum The searchMinimum to set.
    */
   /*package*/ void setSearchMinimum(int searchMinimum)
   {
      this.searchMinimum = searchMinimum;
   }
   
   /**
    * @return If true enables AND text terms for simple/advanced search by default.
    */
   public boolean getForceAndTerms()
   {
      return this.forceAndTerms;
   }

   /**
    * @param forceAndTerms True to enable AND text terms for simple/advanced search by default.
    */
   /*package*/ void setForceAndTerms(boolean forceAndTerms)
   {
      this.forceAndTerms = forceAndTerms;
   }

   /**
    * If positive, this will limit the size of the result set from the search.
    * 
    * @return
    */
   
   public int getSearchMaxResults()
   {
       return searchMaxResults;
   }

   /**
    * Set if the the result set from a search will be of limited size.
    * If negative it is unlimited, by convention, this is set to -1.
    * 
    * @param searchMaxResults
    */
   /*package*/ void setSearchMaxResults(int searchMaxResults)
   {
       this.searchMaxResults = searchMaxResults;
   }

   /**
    * @return Returns the default Home Space permissions.
    */
   public String getHomeSpacePermission()
   {
      return this.homeSpacePermission;
   }

   /**
    * @param homeSpacePermission The default Home Space permission to set.
    */
   /*package*/ void setHomeSpacePermission(String homeSpacePermission)
   {
      this.homeSpacePermission = homeSpacePermission;
   }
}
