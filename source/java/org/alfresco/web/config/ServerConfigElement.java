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

import java.util.List;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.element.ConfigElementAdapter;

/**
 * @author David Caruana
 */
public class ServerConfigElement extends ConfigElementAdapter
{
   public static final String CONFIG_ELEMENT_ID = "server";
   
   private String scheme = null;
   private String hostname = null;
   private Integer port = null;
      
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
   
   /**
    * @see org.alfresco.config.element.ConfigElementAdapter#getChildren()
    */
   public List<ConfigElement> getChildren()
   {
      throw new ConfigException("Reading the Server config via the generic interfaces is not supported");
   }
   
   /**
    * @see org.alfresco.config.element.ConfigElementAdapter#combine(org.alfresco.config.ConfigElement)
    */
   public ConfigElement combine(ConfigElement configElement)
   {
      ServerConfigElement newElement = (ServerConfigElement)configElement;
      ServerConfigElement combinedElement = new ServerConfigElement();
      
      combinedElement.setScheme(newElement.getScheme());
      combinedElement.setHostName(newElement.getHostName());
      combinedElement.setPort(newElement.getPort());
      
      return combinedElement;
   }
   
   /**
    * @return  server scheme
    */
   public String getScheme()
   {
      return scheme;
   }

   /**
    * @param scheme
    */
   public void setScheme(String scheme)
   {
      this.scheme = scheme;
   }
      
   /**
    * @return  server hostname
    */
   public String getHostName()
   {
      return hostname;
   }

   /**
    * @param hostname
    */
   public void setHostName(String hostname)
   {
      this.hostname = hostname;
   }

   /**
    * @return  server port
    */
   public Integer getPort()
   {
      return port;
   }

   /**
    * @param port
    */
   public void setPort(Integer port)
   {
      this.port = port;
   }

}
