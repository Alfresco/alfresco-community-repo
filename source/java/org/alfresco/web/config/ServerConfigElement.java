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
 * Custom config element that represents the config data for the server
 * 
 * @author gavinc
 */
public class ServerConfigElement extends ConfigElementAdapter
{
   public static final String CONFIG_ELEMENT_ID = "server";
   
   private String errorPage;
   private String loginPage;
   
   /**
    * Default constructor
    */
   public ServerConfigElement()
   {
      super(CONFIG_ELEMENT_ID);
   }
   
   /**
    * Constructor
    * 
    * @param name Name of the element this config element represents
    */
   public ServerConfigElement(String name)
   {
      super(name);
   }
   
   public ConfigElement combine(ConfigElement configElement)
   {
      // NOTE: combining these would simply override the values so we just need
      //       to return a new instance of the given config element
      
      ServerConfigElement combined = new ServerConfigElement();
      combined.setErrorPage(((ServerConfigElement)configElement).getErrorPage());
      combined.setLoginPage(((ServerConfigElement)configElement).getLoginPage());
      return combined;
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
   public void setErrorPage(String errorPage)
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
   public void setLoginPage(String loginPage)
   {
      this.loginPage = loginPage;
   }
}
