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
   private boolean ajaxEnabled = false;
   private String initialLocation = null;
   
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
      ClientConfigElement newElement = (ClientConfigElement)configElement;
      ClientConfigElement combinedElement = new ClientConfigElement();
      
      // set those values that have changed
      if (newElement.getEditLinkType() == null)
      {
         combinedElement.setEditLinkType(this.editLinkType);
      }
      else
      {
         combinedElement.setEditLinkType(newElement.getEditLinkType());
      }
      
      if (newElement.getErrorPage() == null)
      {
         combinedElement.setErrorPage(this.errorPage);
      }
      else
      {     
         combinedElement.setErrorPage(newElement.getErrorPage());
      }
      
      if (newElement.getLoginPage() == null)
      {
         combinedElement.setLoginPage(this.loginPage);
      }
      else
      {
         combinedElement.setLoginPage(newElement.getLoginPage());
      }
      
      if (newElement.getHelpUrl() == null )
      {
         combinedElement.setHelpUrl(this.helpUrl);
      }
      else
      {
         combinedElement.setHelpUrl(newElement.getHelpUrl());
      }
      
      if (newElement.getHomeSpacePermission() == null)
      {
         combinedElement.setHomeSpacePermission(this.homeSpacePermission);
      }
      else
      {
         combinedElement.setHomeSpacePermission(newElement.getHomeSpacePermission());
      }
      
      // override default values if they have changed
      if (newElement.getRecentSpacesItems() != combinedElement.getRecentSpacesItems())
      {
         combinedElement.setRecentSpacesItems(newElement.getRecentSpacesItems());
      }
      
      if (newElement.getSearchMinimum() != combinedElement.getSearchMinimum())
      {
         combinedElement.setSearchMinimum(newElement.getSearchMinimum());
      }
      
      if (newElement.getForceAndTerms() != combinedElement.getForceAndTerms())
      {
         combinedElement.setForceAndTerms(newElement.getForceAndTerms());
      }
      
      if (newElement.getSearchMaxResults() != combinedElement.getSearchMaxResults())
      {
         combinedElement.setSearchMaxResults(newElement.getSearchMaxResults());
      }
      
      if (newElement.isShelfVisible() != combinedElement.isShelfVisible())
      {
         combinedElement.setShelfVisible(newElement.isShelfVisible());
      }
      
      if (newElement.getFromEmailAddress() != null && 
          (newElement.getFromEmailAddress().equals(combinedElement.getFromEmailAddress()) == false))
      {
         combinedElement.setFromEmailAddress(newElement.getFromEmailAddress());
      }
      
      if (newElement.isAjaxEnabled() != combinedElement.isAjaxEnabled())
      {
         combinedElement.setAjaxEnabled(newElement.isAjaxEnabled());
      }
      
      if (newElement.getInitialLocation() != null &&
          newElement.getInitialLocation().equals(combinedElement.getInitialLocation()) == false)
      {
         combinedElement.setInitialLocation(newElement.getInitialLocation());
      }
      
      return combinedElement;
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
   
   /**
    * @return Returns whether AJAX support is enabled in the client
    */
   public boolean isAjaxEnabled()
   {
      return this.ajaxEnabled;
   }
   
   /**
    * Sets whether AJAX support is enabled in the client
    * 
    * @param ajaxEnabled
    */
   /*package*/ void setAjaxEnabled(boolean ajaxEnabled)
   {
      this.ajaxEnabled = ajaxEnabled;
   }
   
   /**
    * @return Returns the default initial location for the user.
    */
   public String getInitialLocation()
   {
      return this.initialLocation;
   }

   /**
    * @param initialLocation  The initial location to set.
    */
   /*package*/ void setInitialLocation(String initialLocation)
   {
      this.initialLocation = initialLocation;
   }
}
