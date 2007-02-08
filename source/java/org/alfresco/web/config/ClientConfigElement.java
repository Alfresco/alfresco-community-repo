/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.web.config;

import javax.faces.context.FacesContext;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.element.ConfigElementAdapter;
import org.alfresco.mbeans.VirtServerRegistry;
import org.alfresco.repo.cache.ExpiringValueCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * Custom config element that represents config values for the client
 * 
 * @author Kevin Roast
 */
public class ClientConfigElement extends ConfigElementAdapter
{
   private static Log logger = LogFactory.getLog(ClientConfigElement.class);
   
   public static final String CONFIG_ELEMENT_ID = "client";
   
   private static final String BEAN_VIRT_SERVER_REGISTRY = "VirtServerRegistry";
   private static final String DEFAULT_VSERVER_IP = "127-0-0-1.ip.alfrescodemo.net";
   private static final int DEFAULT_VSERVER_PORT = 8180;
   private static final String DEFAULT_FROM_ADDRESS = "alfresco@alfresco.org";
   
   private String fromEmailAddress = DEFAULT_FROM_ADDRESS;   
   private String errorPage = null;
   private String loginPage = null;
   private int recentSpacesItems = 6;
   private boolean shelfVisible = true;
   private int searchMinimum = 3;
   private boolean forceAndTerms = false;
   private int searchMaxResults = -1;
   private int selectorsSearchMaxResults = 500;
   private String helpUrl = null;
   private String editLinkType = "http";
   private String homeSpacePermission = null;
   private boolean ajaxEnabled = false;
   private String initialLocation = "myalfresco";
   private ExpiringValueCache<String> wcmDomain = new ExpiringValueCache<String>(1000*10L);
   private ExpiringValueCache<String> wcmPort = new ExpiringValueCache<String>(1000*10L);
   private String defaultHomeSpacePath = "/app:company_home";
   private boolean clipboardStatusVisible = true;
   private boolean pasteAllAndClear = true;
   private boolean allowGuestConfig = false;
   
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
      
      if (newElement.getSelectorsSearchMaxResults() != combinedElement.getSelectorsSearchMaxResults())
      {
         combinedElement.setSelectorsSearchMaxResults(newElement.getSelectorsSearchMaxResults());
      }
      
      if (newElement.isShelfVisible() != combinedElement.isShelfVisible())
      {
         combinedElement.setShelfVisible(newElement.isShelfVisible());
      }
      
      if (newElement.isClipboardStatusVisible() != combinedElement.isClipboardStatusVisible())
      {
         combinedElement.setClipboardStatusVisible(newElement.isClipboardStatusVisible());
      }
      
      if (newElement.isPasteAllAndClearEnabled() != combinedElement.isPasteAllAndClearEnabled())
      {
         combinedElement.setPasteAllAndClearEnabled(newElement.isPasteAllAndClearEnabled());
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
      
      if (newElement.getAllowGuestConfig() != combinedElement.getAllowGuestConfig())
      {
         combinedElement.setAllowGuestConfig(newElement.getAllowGuestConfig());
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
    * @return Returns if the clipboard status messages are visible by default.
    */
   public boolean isClipboardStatusVisible()
   {
      return this.clipboardStatusVisible;
   }

   /**
    * @param clipboardStatusVisible True if the clipboard status messages should be visible.
    */
   /*package*/ void setClipboardStatusVisible(boolean clipboardStatusVisible)
   {
      this.clipboardStatusVisible = clipboardStatusVisible;
   }
   
   /**
    * @return Returns if the clipboard paste all action should clear the clipboard.
    */
   public boolean isPasteAllAndClearEnabled()
   {
      return this.pasteAllAndClear;
   }

   /**
    * @param pasteAllAndClear Sets whether the paste all action should clear all items
    *        from the clipboard.
    */
   /*package*/ void setPasteAllAndClearEnabled(boolean pasteAllAndClear)
   {
      this.pasteAllAndClear = pasteAllAndClear;
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
       return this.searchMaxResults;
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
    * If positive, this will limit the size of the result set from the search
    * used in selector components.
    * 
    * @return The maximum number of results to display
    */
   public int getSelectorsSearchMaxResults()
   {
       return this.selectorsSearchMaxResults;
   }

   /**
    * Set if the the result set from a search for the selector components
    * will be of limited size. If negative it is unlimited, by default, 
    * this is set to 500.
    * 
    * @param selectorsSearchMaxResults
    */
   /*package*/ void setSelectorsSearchMaxResults(int selectorsSearchMaxResults)
   {
       this.selectorsSearchMaxResults = selectorsSearchMaxResults;
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
    * @return Returns the default Home Space path.
    */
   public String getDefaultHomeSpacePath()
   {
      return this.defaultHomeSpacePath;
   }
   
   /**
    * @param defaultHomeSpacePath The default Home Space path to set.
    */
   /*package*/ void setDefaultHomeSpacePath(String defaultHomeSpacePath)
   {
      this.defaultHomeSpacePath = defaultHomeSpacePath;
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

   /**
    * @return Returns the WCM Domain obtained from the Virtualisation Server registry.
    */
   public String getWCMDomain()
   {
      String value = this.wcmDomain.get();
      if (value == null)
      {
         VirtServerRegistry vServerRegistry = (VirtServerRegistry)FacesContextUtils.getRequiredWebApplicationContext(
               FacesContext.getCurrentInstance()).getBean(BEAN_VIRT_SERVER_REGISTRY);
         value = vServerRegistry.getVirtServerFQDN();
         if (value == null)
         {
            value = DEFAULT_VSERVER_IP;
            logger.warn("Virtualisation Server not started - reverting to default IP: " + value);
         }
         this.wcmDomain.put(value);
      }
      return value;
   }

   /**
    * @return Returns the WCM Port obtained from the Virtualisation Server registry.
    */
   public String getWCMPort()
   {
      String value = this.wcmPort.get();
      if (value == null)
      {
         VirtServerRegistry vServerRegistry = (VirtServerRegistry)FacesContextUtils.getRequiredWebApplicationContext(
               FacesContext.getCurrentInstance()).getBean(BEAN_VIRT_SERVER_REGISTRY);
         Integer iValue = vServerRegistry.getVirtServerHttpPort();
         if (iValue == null)
         {
            iValue = DEFAULT_VSERVER_PORT;
            logger.warn("Virtualisation Server not started - reverting to default port: " + iValue);
         }
         value = iValue.toString();
         this.wcmPort.put(value);
      }
      return value;
   }
   
   /*package*/ void setAllowGuestConfig(boolean allow)
   {
      this.allowGuestConfig = allow;
   }
   
   public boolean getAllowGuestConfig()
   {
      return this.allowGuestConfig;
   }
}
