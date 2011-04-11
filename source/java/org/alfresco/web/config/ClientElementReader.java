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

import org.alfresco.service.namespace.QName;
import org.dom4j.Element;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigException;
import org.springframework.extensions.config.xml.elementreader.ConfigElementReader;

/**
 * Custom element reader to parse config for client config values
 * 
 * @author Kevin Roast
 */
public class ClientElementReader implements ConfigElementReader
{
   public static final String ELEMENT_RECENTSPACESITEMS = "recent-spaces-items";
   public static final String ELEMENT_ERRORPAGE = "error-page";
   public static final String ELEMENT_LOGINPAGE = "login-page";
   public static final String ELEMENT_HELPURL = "help-url";
   public static final String ELEMENT_EDITLINKTYPE = "edit-link-type";
   public static final String ELEMENT_SEARCHMINIMUM = "search-minimum";
   public static final String ELEMENT_SEARCHANDTERMS = "search-and-terms";
   public static final String ELEMENT_SEARCHMAXRESULTS = "search-max-results";
   public static final String ELEMENT_SELECTORSSEARCHMAXRESULTS = "selectors-search-max-results";
   public static final String ELEMENT_INVITESEARCHMAXRESULTS = "invite-users-max-results";
   public static final String ELEMENT_TASKSCOMPLETEDMAXRESULTS = "tasks-completed-max-results";
   public static final String ELEMENT_HOMESPACEPERMISSION = "home-space-permission";
   public static final String ELEMENT_FROMEMAILADDRESS = "from-email-address";
   public static final String ELEMENT_SHELFVISIBLE = "shelf-visible";
   public static final String ELEMENT_NODESUMMARY_ENABLED = "node-summary-enabled";
   public static final String ELEMENT_INITIALLOCATION = "initial-location";
   public static final String ELEMENT_DEFAULTHOMESPACEPATH = "default-home-space-path";
   public static final String ELEMENT_CLIPBOARDSTATUS = "clipboard-status-visible";
   public static final String ELEMENT_PASTEALLANDCLEAR = "paste-all-and-clear";
   public static final String ELEMENT_GUESTCONFIG = "allow-guest-config";
   public static final String ELEMENT_SIMPLESEARCHADDITIONALATTRS = "simple-search-additional-attributes";
   public static final String ELEMENT_SIMPLESEARCHADDITIONALATTRSQNAME = "qname";
   public static final String ELEMENT_MINUSERNAMELENGTH = "username-min-length";
   public static final String ELEMENT_MINPASSWORDLENGTH = "password-min-length";
   public static final String ELEMENT_MINGROUPNAMELENGTH = "group-name-min-length";
   public static final String ELEMENT_BREADCRUMB_MODE = "breadcrumb-mode";
   public static final String ELEMENT_CIFSURLSUFFIX = "cifs-url-suffix";
   public static final String ELEMENT_LANGUAGESELECT = "language-select";
   public static final String ELEMENT_ZEROBYTEFILEUPLOADS = "zero-byte-file-uploads";
   public static final String ELEMENT_USERGROUPADMIN = "user-group-admin";
   public static final String ELEMENT_ALLOWUSERCONFIG = "allow-user-config";
   public static final String ELEMENT_PICKERSEARCHMINIMUM = "picker-search-minimum";
   public static final String ELEMENT_CHECKCONTEXTPATH = "check-context-against-path";
   public static final String ELEMENT_ALLOWUSERSCRIPTEXECUTE = "allow-user-script-execute";
   
   
   /**
    * @see org.springframework.extensions.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
    */
   @SuppressWarnings("unchecked")
   public ConfigElement parse(Element element)
   {
      ClientConfigElement configElement = null;
      
      if (element != null)
      {
         String name = element.getName();
         if (name.equals(ClientConfigElement.CONFIG_ELEMENT_ID) == false)
         {
            throw new ConfigException("ClientElementReader can only parse " +
                  ClientConfigElement.CONFIG_ELEMENT_ID + " elements, the element passed was '" + 
                  name + "'");
         }
         
         configElement = new ClientConfigElement();
         
         // get the recent space max items
         Element recentSpaces = element.element(ELEMENT_RECENTSPACESITEMS);
         if (recentSpaces != null)
         {
            configElement.setRecentSpacesItems(Integer.parseInt(recentSpaces.getTextTrim()));
         }
         
         // get the shelf component default visibility
         Element shelfVisible = element.element(ELEMENT_SHELFVISIBLE);
         if (shelfVisible != null)
         {
            configElement.setShelfVisible(Boolean.parseBoolean(shelfVisible.getTextTrim()));
         }
         
         // get the Help url
         Element helpUrl = element.element(ELEMENT_HELPURL);
         if (helpUrl != null)
         {
            configElement.setHelpUrl(helpUrl.getTextTrim());
         }
         
         // get the edit link type
         Element editLinkType = element.element(ELEMENT_EDITLINKTYPE);
         if (editLinkType != null)
         {
            configElement.setEditLinkType(editLinkType.getTextTrim());
         }
         
         // get the minimum number of characters for valid search string
         Element searchMin = element.element(ELEMENT_SEARCHMINIMUM);
         if (searchMin != null)
         {
            configElement.setSearchMinimum(Integer.parseInt(searchMin.getTextTrim()));
         }
         
         // get the search force AND terms setting
         Element searchForceAnd = element.element(ELEMENT_SEARCHANDTERMS);
         if (searchForceAnd != null)
         {
            configElement.setForceAndTerms(Boolean.parseBoolean(searchForceAnd.getTextTrim()));
         }
         
         // get the search max results size
         Element searchMaxResults = element.element(ELEMENT_SEARCHMAXRESULTS);
         if (searchMaxResults != null)
         {
            configElement.setSearchMaxResults(Integer.parseInt(searchMaxResults.getTextTrim()));
         }
         
         // get the selectors search max results size
         Element selectorsSearchMaxResults = element.element(ELEMENT_SELECTORSSEARCHMAXRESULTS);
         if (selectorsSearchMaxResults != null)
         {
            configElement.setSelectorsSearchMaxResults(
                  Integer.parseInt(selectorsSearchMaxResults.getTextTrim()));
         }
         
         // get the invite users max results size
         Element inviteUsersMaxResults = element.element(ELEMENT_INVITESEARCHMAXRESULTS);
         if (inviteUsersMaxResults != null)
         {
            configElement.setInviteUsersMaxResults(
                  Integer.parseInt(inviteUsersMaxResults.getTextTrim()));
         }

         // get the invite users max results size
         Element completedTasksMaxResults = element.element(ELEMENT_TASKSCOMPLETEDMAXRESULTS);
         if (completedTasksMaxResults != null)
         {
            configElement.setTasksCompletedMaxResults(
                  Integer.parseInt(completedTasksMaxResults.getTextTrim()));
         }
         
         // get the default permission for newly created users Home Spaces
         Element permission = element.element(ELEMENT_HOMESPACEPERMISSION);
         if (permission != null)
         {
            configElement.setHomeSpacePermission(permission.getTextTrim());
         }
         
         // get the from address to use when sending emails from the client
         Element fromEmail = element.element(ELEMENT_FROMEMAILADDRESS);
         if (fromEmail != null)
         {
            configElement.setFromEmailAddress(fromEmail.getTextTrim());
         }
         
         // get the error page
         Element errorPage = element.element(ELEMENT_ERRORPAGE);
         if (errorPage != null)
         {
            configElement.setErrorPage(errorPage.getTextTrim());
         }
         
         // get the login page
         Element loginPage = element.element(ELEMENT_LOGINPAGE);
         if (loginPage != null)
         {
            configElement.setLoginPage(loginPage.getTextTrim());
         }
         
         // get the node summary popup enabled flag
         Element ajaxEnabled = element.element(ELEMENT_NODESUMMARY_ENABLED);
         if (ajaxEnabled != null)
         {
            configElement.setNodeSummaryEnabled(Boolean.parseBoolean(ajaxEnabled.getTextTrim()));
         }
         
         // get the initial location
         Element initialLocation = element.element(ELEMENT_INITIALLOCATION);
         if (initialLocation != null)
         {
            configElement.setInitialLocation(initialLocation.getTextTrim());
         }
         
         // get the default home space path
         Element defaultHomeSpacePath = element.element(ELEMENT_DEFAULTHOMESPACEPATH);
         if (defaultHomeSpacePath != null)
         {
            configElement.setDefaultHomeSpacePath(defaultHomeSpacePath.getTextTrim());
         }
         
         // get the default visibility of the clipboard status messages
         Element clipboardStatusVisible = element.element(ELEMENT_CLIPBOARDSTATUS);
         if (clipboardStatusVisible != null)
         {
            configElement.setClipboardStatusVisible(Boolean.parseBoolean(
                  clipboardStatusVisible.getTextTrim()));
         }
         
         // get the default setting for the paste all action, should it clear the clipboard after?
         Element pasteAllAndClear = element.element(ELEMENT_PASTEALLANDCLEAR);
         if (pasteAllAndClear != null)
         {
            configElement.setPasteAllAndClearEnabled(Boolean.parseBoolean(
                  pasteAllAndClear.getTextTrim()));
         }
         
         // get allow Guest to configure start location preferences
         Element guestConfigElement = element.element(ELEMENT_GUESTCONFIG);
         if (guestConfigElement != null)
         {
            boolean allow = Boolean.parseBoolean(guestConfigElement.getTextTrim());
            configElement.setAllowGuestConfig(allow);
         }
         
         // get the additional simple search attributes
         Element simpleSearchAdditionalAttributesElement = element.element(ELEMENT_SIMPLESEARCHADDITIONALATTRS);
         if (simpleSearchAdditionalAttributesElement != null)
         {
            List<Element> attrbElements = 
               simpleSearchAdditionalAttributesElement.elements(ELEMENT_SIMPLESEARCHADDITIONALATTRSQNAME);
            if (attrbElements != null && attrbElements.size() != 0)
            {
               List<QName> simpleSearchAddtlAttrb = new ArrayList<QName>(4);
               for (Element elem : attrbElements)
               {
                  simpleSearchAddtlAttrb.add(QName.createQName(elem.getTextTrim()));
               }
               configElement.setSimpleSearchAdditionalAttributes(simpleSearchAddtlAttrb);
            }
         }
         
         // get the minimum length of usernames
         Element minUsername = element.element(ELEMENT_MINUSERNAMELENGTH);
         if (minUsername != null)
         {
            configElement.setMinUsernameLength(Integer.parseInt(minUsername.getTextTrim()));
         }
         
         // get the minimum length of passwords
         Element minPassword = element.element(ELEMENT_MINPASSWORDLENGTH);
         if (minPassword != null)
         {
            configElement.setMinPasswordLength(Integer.parseInt(minPassword.getTextTrim()));
         }

         // get the minimum length of group names
         Element minGroupName = element.element(ELEMENT_MINGROUPNAMELENGTH);
         if (minGroupName != null)
         {
            configElement.setMinGroupNameLength(Integer.parseInt(minGroupName.getTextTrim()));
         }

         // get the breadcrumb mode
         Element breadcrumbMode = element.element(ELEMENT_BREADCRUMB_MODE);
         if (breadcrumbMode != null)
         {
            configElement.setBreadcrumbMode(breadcrumbMode.getTextTrim());
         }

         // Get the CIFS URL suffix
         Element cifsSuffix = element.element(ELEMENT_CIFSURLSUFFIX);
         if (cifsSuffix != null)
         {
            String suffix = cifsSuffix.getTextTrim();
            if (suffix.startsWith(".") == false)
            {
               suffix = "." + suffix;
            }
            configElement.setCifsURLSuffix( suffix);
         }
         
         // get the language selection mode
         Element langSelect = element.element(ELEMENT_LANGUAGESELECT);
         if (langSelect != null)
         {
            configElement.setLanguageSelect(Boolean.parseBoolean(langSelect.getTextTrim()));
         }
         
         // get the zero byte file upload mode
         Element zeroByteFiles = element.element(ELEMENT_ZEROBYTEFILEUPLOADS);
         if (zeroByteFiles != null)
         {
            configElement.setZeroByteFileUploads(Boolean.parseBoolean(zeroByteFiles.getTextTrim()));
         }
         
         // get allow user group admin mode
         Element userGroupAdmin = element.element(ELEMENT_USERGROUPADMIN);
         if (userGroupAdmin != null)
         {
            configElement.setUserGroupAdmin(Boolean.parseBoolean(userGroupAdmin.getTextTrim()));
         }
         
         // get allow user config mode
         Element userConfig = element.element(ELEMENT_ALLOWUSERCONFIG);
         if (userConfig != null)
         {
            configElement.setAllowUserConfig(Boolean.parseBoolean(userConfig.getTextTrim()));
         }
         
         // get the minimum number of characters for valid picker search string
         Element pickerSearchMin = element.element(ELEMENT_PICKERSEARCHMINIMUM);
         if (pickerSearchMin != null)
         {
            configElement.setPickerSearchMinimum(Integer.parseInt(pickerSearchMin.getTextTrim()));
         }
         
         // determine whether the JavaScript setContextPath method should
         // check the path of the current URL
         Element checkContextAgainstPath = element.element(ELEMENT_CHECKCONTEXTPATH);
         if (checkContextAgainstPath != null)
         {
            configElement.setCheckContextAgainstPath(Boolean.parseBoolean(checkContextAgainstPath.getTextTrim()));
         }
         
         // get allow any user to execute javascript via the command servlet
         Element allowUserScriptExecute = element.element(ELEMENT_ALLOWUSERSCRIPTEXECUTE);
         if (allowUserScriptExecute != null)
         {
            configElement.setAllowUserScriptExecute(Boolean.parseBoolean(allowUserScriptExecute.getTextTrim()));
         }
      }
      
      return configElement;
   }
}
