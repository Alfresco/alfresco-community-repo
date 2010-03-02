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
package org.alfresco.repo.webdav;

import org.alfresco.service.namespace.QName;

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
    * Creates QName of the property
    * 
    * @return QName
    */
   public QName createQName()
   {
       return QName.createQName(m_strNamespaceUri, m_strName);
   }
   
   /**
    * Returns true if property is protected according to the WebDav specification
    * 
    * @return boolean
    */
   public boolean isProtected()
   {
       return WebDAV.XML_GET_CONTENT_LENGTH.equals(m_strName) ||
               WebDAV.XML_GET_ETAG.equals(m_strName) ||
               WebDAV.XML_GET_LAST_MODIFIED.equals(m_strName) ||
               WebDAV.XML_LOCK_DISCOVERY.equals(m_strName) ||
               WebDAV.XML_RESOURCE_TYPE.equals(m_strName) ||
               WebDAV.XML_SUPPORTED_LOCK.equals(m_strName);
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
