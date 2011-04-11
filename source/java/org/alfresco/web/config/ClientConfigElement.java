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
package org.alfresco.web.config;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.config.JNDIConstants;
import org.alfresco.mbeans.VirtServerRegistry;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ExpiringValueCache;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.element.ConfigElementAdapter;

/**
 * Custom config element that represents config values for the client
 * 
 * @author Kevin Roast
 */
public class ClientConfigElement extends ConfigElementAdapter
{
   private static final long serialVersionUID = 825650925215057110L;

   private static Log logger = LogFactory.getLog(ClientConfigElement.class);
   
   public static final String CONFIG_ELEMENT_ID = "client";
   public static final String BREADCRUMB_PATH = "path";
   public static final String BREADCRUMB_LOCATION = "location";
   
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
   private int inviteUsersMaxResults = 500;
   private int tasksCompletedMaxResults = 100;
   private String helpUrl = null;
   private String editLinkType = "http";
   private String homeSpacePermission = null;
   private boolean nodeSummaryEnabled = true;
   private String initialLocation = "myalfresco";
   private ExpiringValueCache<String> wcmDomain = new ExpiringValueCache<String>(1000*10L);
   private ExpiringValueCache<String> wcmPort = new ExpiringValueCache<String>(1000*10L);
   private String defaultHomeSpacePath = "/app:company_home/app:user_homes";
   private boolean clipboardStatusVisible = true;
   private boolean pasteAllAndClear = true;
   private boolean allowGuestConfig = false;
   private List<QName> simpleSearchAdditionalAttributes = null;
   private int minUsernameLength = 2;
   private int minPasswordLength = 3;
   private int minGroupNameLength = 3;
   private String breadcrumbMode = BREADCRUMB_PATH;
   private String cifsURLSuffix = null;
   private boolean languageSelect = true;
   private boolean zeroByteFileUploads = true;
   private boolean userGroupAdmin = true;
   private boolean allowUserConfig = true;
   private int pickerSearchMinimum = 2;
   private boolean checkContextAgainstPath = false;
   private boolean allowUserScriptExecute = false;
   
   
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
    * @see org.springframework.extensions.config.element.ConfigElementAdapter#combine(org.alfresco.config.ConfigElement)
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
      
      if (newElement.getCifsURLSuffix() == null)
      {
         combinedElement.setCifsURLSuffix(this.cifsURLSuffix);
      }
      else
      {
         combinedElement.setCifsURLSuffix(newElement.getCifsURLSuffix());
      }
      
      if (newElement.getSimpleSearchAdditionalAttributes() == null)
      {
          combinedElement.setSimpleSearchAdditionalAttributes(this.simpleSearchAdditionalAttributes);
      }
      else
      {
          if (this.simpleSearchAdditionalAttributes == null)
          {
              // there aren't any existing attributes so just use the new set
              combinedElement.setSimpleSearchAdditionalAttributes(newElement.getSimpleSearchAdditionalAttributes());
          }
          else
          {
              // get the current list and add the additional attributes to it
              List<QName> newAttrs = newElement.getSimpleSearchAdditionalAttributes();
              List<QName> combinedAttrs = new ArrayList<QName>(
                          this.simpleSearchAdditionalAttributes.size() + newAttrs.size());
              combinedAttrs.addAll(this.simpleSearchAdditionalAttributes);
              combinedAttrs.addAll(newAttrs);
              combinedElement.setSimpleSearchAdditionalAttributes(combinedAttrs);
          }
      }
      
      // override default values if they have changed
      if (newElement.getDefaultHomeSpacePath() != null &&
          newElement.getDefaultHomeSpacePath().equals(combinedElement.getDefaultHomeSpacePath()) == false)
      {
         combinedElement.setDefaultHomeSpacePath(newElement.getDefaultHomeSpacePath());
      }
      
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
      
      if (newElement.getInviteUsersMaxResults() != combinedElement.getInviteUsersMaxResults())
      {
         combinedElement.setInviteUsersMaxResults(newElement.getInviteUsersMaxResults());
      }

      if (newElement.getTasksCompletedMaxResults() != combinedElement.getTasksCompletedMaxResults())
      {
         combinedElement.setTasksCompletedMaxResults(newElement.getTasksCompletedMaxResults());
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
      
      if (newElement.isNodeSummaryEnabled() != combinedElement.isNodeSummaryEnabled())
      {
         combinedElement.setNodeSummaryEnabled(newElement.isNodeSummaryEnabled());
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
      
      if (newElement.getMinUsernameLength() != combinedElement.getMinUsernameLength())
      {
         combinedElement.setMinUsernameLength(newElement.getMinUsernameLength());
      }
      
      if (newElement.getMinPasswordLength() != combinedElement.getMinPasswordLength())
      {
         combinedElement.setMinPasswordLength(newElement.getMinPasswordLength());
      }

      if (newElement.getMinGroupNameLength() != combinedElement.getMinGroupNameLength())
      {
         combinedElement.setMinGroupNameLength(newElement.getMinGroupNameLength());
      }

      if (newElement.getBreadcrumbMode() != null &&
          newElement.getBreadcrumbMode().equals(combinedElement.getBreadcrumbMode()) == false)
      {
         combinedElement.setBreadcrumbMode(newElement.getBreadcrumbMode());
      }
      
      if (newElement.isLanguageSelect() != combinedElement.isLanguageSelect())
      {
         combinedElement.setLanguageSelect(newElement.isLanguageSelect());
      }
      
      if (newElement.isZeroByteFileUploads() != combinedElement.isZeroByteFileUploads())
      {
         combinedElement.setZeroByteFileUploads(newElement.isZeroByteFileUploads());
      }
      
      if (newElement.getAllowUserConfig() != combinedElement.getAllowUserConfig())
      {
         combinedElement.setAllowUserConfig(newElement.getAllowUserConfig());
      }
      
      if (newElement.isUserGroupAdmin() != combinedElement.isUserGroupAdmin())
      {
         combinedElement.setUserGroupAdmin(newElement.isUserGroupAdmin());
      }
      
      if (newElement.getPickerSearchMinimum() != combinedElement.getPickerSearchMinimum())
      {
         combinedElement.setPickerSearchMinimum(newElement.getPickerSearchMinimum());
      }

      if (newElement.getCheckContextAgainstPath() != combinedElement.getCheckContextAgainstPath())
      {
         combinedElement.setCheckContextAgainstPath(newElement.getCheckContextAgainstPath());
      }
      
      if (newElement.getAllowUserScriptExecute() != combinedElement.getAllowUserScriptExecute())
      {
         combinedElement.setAllowUserScriptExecute(newElement.getAllowUserScriptExecute());
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
    * @return The maximum number of results to show
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
    * If positive, this will limit the size of the result set from the 
    * invite users wizard.
    * 
    * @return The maximum number of results to display
    */
   public int getInviteUsersMaxResults()
   {
       return this.inviteUsersMaxResults;
   }

   /**
    * Set if the the result set from a search for the invite users wizard
    * will be of limited size. If negative it is unlimited, by default, 
    * this is set to 500.
    * 
    * @param inviteUsersMaxResults
    */
   /*package*/ void setInviteUsersMaxResults(int inviteUsersMaxResults)
   {
       this.inviteUsersMaxResults = inviteUsersMaxResults;
   }

   /**
    * If positive, this will limit the size of the result set of the
    * completed tasks.
    *
    * @return The maximum number of completed tasks to display
    */
   public int getTasksCompletedMaxResults()
   {
       return tasksCompletedMaxResults;
   }

   /**
    * Set if the the number of completed tasks displayed shall be limited.
    * If negative it is unlimited, by default, this is set to 100.
    *
    * @param tasksCompletedMaxResults
    */
   /*package*/ void setTasksCompletedMaxResults(int tasksCompletedMaxResults)
   {
       this.tasksCompletedMaxResults = tasksCompletedMaxResults;
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
   public boolean isNodeSummaryEnabled()
   {
      return this.nodeSummaryEnabled;
   }
   
   /**
    * Sets whether AJAX support is enabled in the client
    * 
    * @param nodeSummaryEnabled
    */
   /*package*/ void setNodeSummaryEnabled(boolean ajaxEnabled)
   {
      this.nodeSummaryEnabled = ajaxEnabled;
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
         VirtServerRegistry vServerRegistry = Repository.getServiceRegistry(
                 FacesContext.getCurrentInstance()).getVirtServerRegistry();
         value = vServerRegistry.getVirtServerFQDN();
         if (value == null)
         {
            value = JNDIConstants.DEFAULT_VSERVER_IP;
            if (logger.isDebugEnabled())
            {
                logger.debug("Virtualisation Server not started - reverting to default IP: " + value);
            }
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
         VirtServerRegistry vServerRegistry = Repository.getServiceRegistry(
                 FacesContext.getCurrentInstance()).getVirtServerRegistry();
         Integer iValue = vServerRegistry.getVirtServerHttpPort();
         if (iValue == null)
         {
            iValue = JNDIConstants.DEFAULT_VSERVER_PORT;
            if (logger.isDebugEnabled())
            {
               logger.debug("Virtualisation Server not started - reverting to default port: " + iValue);
            }
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
   
   /**
    * @return Returns the additional attributes to search on a simple search
    */
   public List<QName> getSimpleSearchAdditionalAttributes()
   {
      return this.simpleSearchAdditionalAttributes;
   }

   /**
    * @param simpleSearchAdditionalAttributes  The additional simple search attributes
    */
   /*package*/ void setSimpleSearchAdditionalAttributes(List<QName> simpleSearchAdditionalAttributes)
   {
      this.simpleSearchAdditionalAttributes = simpleSearchAdditionalAttributes;
   }
   
   /**
    * @return Returns the minimum length for a username.
    */
   public int getMinUsernameLength()
   {
      return this.minUsernameLength;
   }

   /**
    * @param minUsernameLength The minimum length of a username
    */
   /*package*/ void setMinUsernameLength(int minUsernameLength)
   {
      this.minUsernameLength = minUsernameLength;
   }
   
   /**
    * @return Returns the minimum length for a password.
    */
   public int getMinPasswordLength()
   {
      return this.minPasswordLength;
   }

   /**
    * @param minPasswordLength The minimum length of a password
    */
   /*package*/ void setMinPasswordLength(int minPasswordLength)
   {
      this.minPasswordLength = minPasswordLength;
   }

   /**
    * @return Returns the minimum length for a group name.
    */
   public int getMinGroupNameLength()
   {
      return this.minGroupNameLength;
   }

   /**
    * @param minGroupNameLength The minimum length of a group name
    */
   /*package*/ void setMinGroupNameLength(int minGroupNameLength)
   {
      this.minGroupNameLength = minGroupNameLength;
   }

   /**
    * Get the breadcrumb mode
    * 
    * @return String
    */
   public final String getBreadcrumbMode()
   {
      return breadcrumbMode;
   }
   
   /**
    * Set the breadcrumb mode
    * 
    * @param mode String
    */
   void setBreadcrumbMode(String mode)
   {
      // make sure it's being set to a valid option
      if (BREADCRUMB_PATH.equals(mode) || BREADCRUMB_LOCATION.equals(mode))
      {
         breadcrumbMode = mode;
      }
   }

   /**
    * Get the CIFs URL suffix
    * 
    * @return String
    */
   public final String getCifsURLSuffix()
   {
      return cifsURLSuffix;
   }

   /**
    * Set the CIFS URL suffix
    * 
    * @param suffix String
    */
   void setCifsURLSuffix(String suffix)
   {
      cifsURLSuffix = suffix;
   }

   /**
    * @return the language select flag - true to display language selection, false to
    *         get the language from the client browser locale instead
    */
   public final boolean isLanguageSelect()
   {
      return this.languageSelect;
   }

   /**
    * @param value  the language select flag
    */
   /*package*/ void setLanguageSelect(boolean value)
   {
      this.languageSelect = value;
   }

   /**
    * @return true if zero byte file uploads are allowed, false otherwise
    */
   public boolean isZeroByteFileUploads()
   {
      return this.zeroByteFileUploads;
   }

   /**
    * @param zeroByteFileUploads    true if zero byte file uploads are allowed, false otherwise
    */
   /*package*/ void setZeroByteFileUploads(boolean zeroByteFileUploads)
   {
      this.zeroByteFileUploads = zeroByteFileUploads;
   }

   /**
    * @return true if allowing User Group administration by an admin user
    */
   public boolean isUserGroupAdmin()
   {
      return this.userGroupAdmin;
   }

   /**
    * @param userGroupAdmin   true to allow User Group administration by an admin user
    */
   /*package*/ void setUserGroupAdmin(boolean userGroupAdmin)
   {
      this.userGroupAdmin = userGroupAdmin;
   }

   /**
    * @return true to allow users to modify their personal settings in the User Console screen
    */
   public boolean getAllowUserConfig()
   {
      return this.allowUserConfig;
   }

   /**
    * @param allowUserConfig  true to allow users to modify their personal settings in the User Console screen
    */
   /*package*/ void setAllowUserConfig(boolean allowUserConfig)
   {
      this.allowUserConfig = allowUserConfig;
   }
   
   /**
    * @return Returns the minimum number of characters for a picker search.
    */
   public int getPickerSearchMinimum()
   {
      return this.pickerSearchMinimum;
   }

   /**
    * @param searchMinimum The minimum number of characters for a picker search.
    */
   /*package*/ void setPickerSearchMinimum(int searchMinimum)
   {
      this.pickerSearchMinimum = searchMinimum;
   }
   
   /**
    * @return true if the context path should be checked against the path in the current URL
    */
   public boolean getCheckContextAgainstPath()
   {
      return this.checkContextAgainstPath;
   }

   /**
    * @param checkContextAgainstPath true to check the context path against the path in the current URL
    */
   /*package*/ void setCheckContextAgainstPath(boolean checkContextAgainstPath)
   {
      this.checkContextAgainstPath = checkContextAgainstPath;
   }
   
   /**
    * @return true if any user can execute JavaScript via the command servlet
    */
   public boolean getAllowUserScriptExecute()
   {
      return this.allowUserScriptExecute;
   }

   /**
    * @param allowUserScriptExecute true to allow any user to execute JavaScript via the command servlet
    */
   /*package*/ void setAllowUserScriptExecute(boolean allowUserScriptExecute)
   {
      this.allowUserScriptExecute = allowUserScriptExecute;
   }
}
