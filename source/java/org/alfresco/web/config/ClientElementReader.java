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
package org.alfresco.web.config;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.xml.elementreader.ConfigElementReader;
import org.dom4j.Element;

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
   public static final String ELEMENT_HOMESPACEPERMISSION = "home-space-permission";
   public static final String ELEMENT_FROMEMAILADDRESS = "from-email-address";
   public static final String ELEMENT_SHELFVISIBLE = "shelf-visible";
   public static final String ELEMENT_AJAX_ENABLED = "ajax-enabled";
   public static final String ELEMENT_INITIALLOCATION = "initial-location";
   public static final String ELEMENT_DEFAULTHOMESPACEPATH = "default-home-space-path";
   public static final String ELEMENT_CLIPBOARDSTATUS = "clipboard-status-visible";
   public static final String ELEMENT_PASTEALLANDCLEAR = "paste-all-and-clear";
   public static final String ELEMENT_GUESTCONFIG = "allow-guest-config";
   
   /**
    * @see org.alfresco.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
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
         
         // get the ajax enabled flag
         Element ajaxEnabled = element.element(ELEMENT_AJAX_ENABLED);
         if (ajaxEnabled != null)
         {
            configElement.setAjaxEnabled(Boolean.parseBoolean(ajaxEnabled.getTextTrim()));
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
      }
      
      return configElement;
   }
}
