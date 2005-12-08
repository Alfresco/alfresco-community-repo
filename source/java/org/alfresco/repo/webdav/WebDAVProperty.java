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
