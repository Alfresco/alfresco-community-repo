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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.webdav;

/**
 * Class to represent a WebDAV property
 * 
 * @author gavinc
 */
public class WebDAVProperty
{
   private String m_strName = null;
   private String m_strNamespaceUri = WebDAV.DEFAULT_NAMESPACE_URI;
   private String m_strNamespaceName = null;
   private String m_strValue = null;
   
   /**
    * Constructs a WebDAVProperty
    * 
    * @param strName
    * @param strNamespaceUri
    * @param strNamespaceName
    * @param strValue
    */
   public WebDAVProperty(String strName, String strNamespaceUri, String strNamespaceName, String strValue)
   {
      this(strName, strNamespaceUri, strNamespaceName);
      m_strValue = strValue;
   }
   
   /**
    * Constructs a WebDAVProperty
    * 
    * @param strName
    * @param strNamespaceUri
    * @param strNamespaceName
    * @param strValue
    */
   public WebDAVProperty(String strName, String strNamespaceUri, String strNamespaceName)
   {
      this(strName);
      
      m_strNamespaceUri = strNamespaceUri;
      m_strNamespaceName = strNamespaceName;
   }
   
   /**
    * Constructs a WebDAVProperty with the default namespace details
    * 
    * @param strName
    */
   public WebDAVProperty(String strName)
   {
      m_strName = strName;
   }
   
   /**
    * Returns the name of the property
    * 
    * @return The name of the property
    */
   public String getName()
   {
      return m_strName;
   }
   
   /**
    * Returns the namespace URI for this property
    * 
    * @return The namespace URI for this property
    */
   public String getNamespaceUri()
   {
      return m_strNamespaceUri;
   }
   
   /**
    * Determine if the property has a namespace
    * 
    * @return boolean
    */
   public final boolean hasNamespaceName()
   {
       return m_strNamespaceName != null ? true : false;
   }
   
   /**
    * Returns the namespace name for this property
    * 
    * @return The namespace name for this property
    */
   public String getNamespaceName()
   {
      return m_strNamespaceName;
   }
   
   /**
    * Returns the value of this property
    * 
    * @return The value of this property
    */
   public String getValue()
   {
      return m_strValue;
   }
   
   /**
    * Sets the property's value
    * 
    * @param strValue The new value
    */
   public void setValue(String strValue)
   {
      m_strValue = strValue;
   }
   
   /**
    * Return the property as a string
    * 
    * @return String
    */
   public String toString()
   {
       StringBuilder str = new StringBuilder();
       
       str.append("[");
       
       str.append(getName());
       str.append("=");
       str.append(getValue());
       str.append(",URI=");
       str.append(getNamespaceUri());
       
       if ( hasNamespaceName())
       {
           str.append(",");
           str.append(getNamespaceName());
       }
       
       return str.toString();
   }
}
