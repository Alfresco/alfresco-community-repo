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
package org.alfresco.web.bean;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.lucene.QueryParser;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Holds the context required to build a search query and can return the populated query.
 * 
 * @author Kevin Roast
 */
public final class SearchContext implements Serializable
{
   /** Search mode constants */
   public final static int SEARCH_ALL = 0;
   public final static int SEARCH_FILE_NAMES_CONTENTS = 1;
   public final static int SEARCH_FILE_NAMES = 2;
   public final static int SEARCH_SPACE_NAMES = 3;
   
   /** the search text string */
   private String text = "";
   
   /** mode for the search */
   private int mode = SearchContext.SEARCH_ALL;
   
   /** folder node location for the search */
   private String location = null;
   
   /** categories to add to the search */
   private String[] categories = new String[0];
   
   /** true to search location children as well as location */
   private boolean locationChildren = true;
   
   /** true to search category children as well as category */
   private boolean categoryChildren = true;
   
   /** content type to restrict search against */
   private String contentType = null;
   
   /** content mimetype to restrict search against */
   private String mimeType = null;
   
   /** any extra query attributes to add to the search */
   private Map<QName, String> queryAttributes = new HashMap<QName, String>(5, 1.0f);
   
   /** any additional range attribute to add to the search */
   private Map<QName, RangeProperties> rangeAttributes = new HashMap<QName, RangeProperties>(5, 1.0f);
   
   /** any additional fixed value attributes to add to the search, such as boolean or noderef */
   private Map<QName, String> queryFixedValues = new HashMap<QName, String>(5, 1.0f);
   
   /** logger */
   private static Log logger = LogFactory.getLog(SearchContext.class);
   
   
   /**
    * Build the search query string based on the current search context members.
    * 
    * @return prepared search query string
    */
   public String buildQuery()
   {
      String query;
      
      // the QName for the well known "name" attribute
      String nameAttr = Repository.escapeQName(QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "name"));
      
      // match against content text
      String text = this.text.trim();
      String fullTextQuery;
      String nameAttrQuery;
      
      if (text.indexOf(' ') == -1)
      {
         // simple single word text search
         if (text.charAt(0) != '*')
         {
            // escape characters and append the wildcard character
            String safeText = QueryParser.escape(text);
            fullTextQuery = " TEXT:" + safeText + '*';
            nameAttrQuery = " @" + nameAttr + ":" + safeText + '*';
         }
         else
         {
            // found a leading wildcard - prepend it again after escaping the other characters
            String safeText = QueryParser.escape(text.substring(1));
            fullTextQuery = " TEXT:*" + safeText + '*';
            nameAttrQuery = " @" + nameAttr + ":*" + safeText + '*';
         }
      }
      else
      {
         // multiple word search
         if (text.charAt(0) == '"' && text.charAt(text.length() - 1) == '"')
         {
            // as quoted phrase
            String quotedSafeText = '"' + QueryParser.escape(text.substring(1, text.length() - 1)) + '"';
            fullTextQuery = " TEXT:" + quotedSafeText;
            nameAttrQuery = " @" + nameAttr + ":" + quotedSafeText;
         }
         else
         {
            // as individual search terms
            StringTokenizer t = new StringTokenizer(text, " ");
            StringBuilder fullTextBuf = new StringBuilder(64);
            StringBuilder nameAttrBuf = new StringBuilder(64);
            fullTextBuf.append('(');
            nameAttrBuf.append('(');
            while (t.hasMoreTokens())
            {
               String term = t.nextToken();
               if (term.charAt(0) != '*')
               {
                  String safeTerm = QueryParser.escape(term);
                  fullTextBuf.append("TEXT:").append(safeTerm).append('*');
                  nameAttrBuf.append("@").append(nameAttr).append(":").append(safeTerm).append('*');
               }
               else
               {
                  String safeTerm = QueryParser.escape(term.substring(1));
                  fullTextBuf.append("TEXT:*").append(safeTerm).append('*');
                  nameAttrBuf.append("@").append(nameAttr).append(":*").append(safeTerm).append('*');
               }
               if (t.hasMoreTokens())
               {
                  fullTextBuf.append(" OR ");
                  nameAttrBuf.append(" OR ");
               }
            }
            fullTextBuf.append(')');
            nameAttrBuf.append(')');
            fullTextQuery = fullTextBuf.toString();
            nameAttrQuery = nameAttrBuf.toString();
         }
      }
      
      // match a specific PATH for space location or categories
      StringBuilder pathQuery = null;
      if (location != null || (categories != null && categories.length !=0))
      {
         pathQuery = new StringBuilder(128);
         if (location != null)
         {
            pathQuery.append(" PATH:\"").append(location).append("\" ");
         }
         if (categories != null && categories.length != 0)
         {
            for (int i=0; i<categories.length; i++)
            {
               if (pathQuery.length() != 0)
               {
                  pathQuery.append("OR");
               }
               pathQuery.append(" PATH:\"").append(categories[i]).append("\" "); 
            }
         }
      }
      
      // match any extra query attribute values specified
      StringBuilder attributeQuery = null;
      if (queryAttributes.size() != 0)
      {
         attributeQuery = new StringBuilder(queryAttributes.size() << 6);
         for (QName qname : queryAttributes.keySet())
         {
            String escapedName = Repository.escapeQName(qname);
            String value = QueryParser.escape(queryAttributes.get(qname));
            attributeQuery.append(" +@").append(escapedName)
                          .append(":").append(value).append('*');
         }
      }
      
      // match any extra fixed value attributes specified
      if (queryFixedValues.size() != 0)
      {
         if (attributeQuery == null)
         {
            attributeQuery = new StringBuilder(queryFixedValues.size() << 6);
         }
         for (QName qname : queryFixedValues.keySet())
         {
            String escapedName = Repository.escapeQName(qname);
            String value = queryFixedValues.get(qname);
            attributeQuery.append(" +@").append(escapedName)
                          .append(":\"").append(value).append('"');
         }
      }
      
      // range attributes are a special case also
      if (rangeAttributes.size() != 0)
      {
         if (attributeQuery == null)
         {
            attributeQuery = new StringBuilder(rangeAttributes.size() << 6);
         }
         for (QName qname : rangeAttributes.keySet())
         {
            String escapedName = Repository.escapeQName(qname);
            RangeProperties rp = rangeAttributes.get(qname);
            String value1 = QueryParser.escape(rp.lower);
            String value2 = QueryParser.escape(rp.upper);
            attributeQuery.append(" +@").append(escapedName)
                          .append(":").append(rp.inclusive ? "[" : "{").append(value1)
                          .append(" TO ").append(value2).append(rp.inclusive ? "]" : "}");
         }
      }
      
      // mimetype is a special case - it is indexed as a special attribute it comes from the combined
      // ContentData attribute of cm:content - ContentData string cannot be searched directly
      if (mimeType != null && mimeType.length() != 0)
      {
         if (attributeQuery == null)
         {
            attributeQuery = new StringBuilder(64);
         }
         String escapedName = Repository.escapeQName(QName.createQName(ContentModel.PROP_CONTENT + ".mimetype"));
         attributeQuery.append(" +@").append(escapedName)
                       .append(":").append(mimeType);
      }
      
      // match against appropriate content type
      String fileTypeQuery;
      if (contentType != null)
      {
         fileTypeQuery = " +TYPE:\"" + contentType + "\" ";
      }
      else
      {
         // default to cm:content
         fileTypeQuery = " +TYPE:\"{" + NamespaceService.CONTENT_MODEL_1_0_URI + "}content\" ";
      }
      
      // match against FOLDER type
      String folderTypeQuery = " +TYPE:\"{" + NamespaceService.CONTENT_MODEL_1_0_URI + "}folder\" ";
      
      switch (mode)
      {
         case SearchContext.SEARCH_ALL:
            query = '(' + fileTypeQuery + " AND " + '(' + nameAttrQuery + fullTextQuery + ')' + ')' + " OR " +
                    '(' + folderTypeQuery + " AND " + nameAttrQuery + ')';
            break;
         
         case SearchContext.SEARCH_FILE_NAMES:
            query = fileTypeQuery + " AND " + nameAttrQuery;
            break;
         
         case SearchContext.SEARCH_FILE_NAMES_CONTENTS:
            query = fileTypeQuery + " AND " + '(' + nameAttrQuery + fullTextQuery + ')';
            break;
         
         case SearchContext.SEARCH_SPACE_NAMES:
            query = folderTypeQuery + " AND " + nameAttrQuery;
            break;
         
         default:
            throw new IllegalStateException("Unknown search mode specified: " + mode);
      }
      
      // match entire query against any additional attributes specified
      if (attributeQuery != null)
      {
         query = attributeQuery + " AND (" + query + ')';
      }
      
      // match entire query against any specified paths
      if (pathQuery != null)
      {
         query = "(" + pathQuery + ") AND (" + query + ')';
      }
      
      if (logger.isDebugEnabled())
         logger.debug("Query: " + query);
      
      return query;
   }
   
   /**
    * Generate a search XPATH pointing to the specified node, optionally return an XPATH
    * that includes the child nodes.
    *  
    * @param id         Of the node to generate path too
    * @param children   Whether to include children of the node
    * 
    * @return the path
    */
   /*package*/ static String getPathFromSpaceRef(NodeRef ref, boolean children)
   {
      FacesContext context = FacesContext.getCurrentInstance();
      Path path = Repository.getServiceRegistry(context).getNodeService().getPath(ref);
      NamespaceService ns = Repository.getServiceRegistry(context).getNamespaceService();
      StringBuilder buf = new StringBuilder(64);
      for (int i=0; i<path.size(); i++)
      {
         String elementString = "";
         Path.Element element = path.get(i);
         if (element instanceof Path.ChildAssocElement)
         {
            ChildAssociationRef elementRef = ((Path.ChildAssocElement)element).getRef();
            if (elementRef.getParentRef() != null)
            {
               Collection prefixes = ns.getPrefixes(elementRef.getQName().getNamespaceURI());
               if (prefixes.size() >0)
               {
                  elementString = '/' + (String)prefixes.iterator().next() + ':' + ISO9075.encode(elementRef.getQName().getLocalName());
               }
            }
         }
         
         buf.append(elementString);
      }
      if (children == true)
      {
         // append syntax to get all children of the path
         buf.append("//*");
      }
      else
      {
         // append syntax to just represent the path, not the children
         buf.append("/*");
      }
      
      return buf.toString();
   }
   
   /**
    * @return Returns the categories to use for the search
    */
   public String[] getCategories()
   {
      return this.categories;
   }
   
   /**
    * @param categories    The categories to set as a list of search XPATHs
    */
   public void setCategories(String[] categories)
   {
      if (categories != null)
      {
         this.categories = categories;
      }
   }
   
   /**
    * @return Returns the node to search from or null for all.
    */
   public String getLocation()
   {
      return this.location;
   }
   
   /**
    * @param location      The node to search from or null for all..
    */
   public void setLocation(String location)
   {
      this.location = location;
   }
   
   /**
    * @return Returns the mode to use during the search (see constants)
    */
   public int getMode()
   {
      return this.mode;
   }
   
   /**
    * @param mode The mode to use during the search (see constants)
    */
   public void setMode(int mode)
   {
      this.mode = mode;
   }
   
   /**
    * @return Returns the search text string.
    */
   public String getText()
   {
      return this.text;
   }
   
   /**
    * @param text       The search text string.
    */
   public void setText(String text)
   {
      this.text = text;
   }

   /**
    * @return Returns the contentType.
    */
   public String getContentType()
   {
      return this.contentType;
   }

   /**
    * @param contentType The content type to restrict attribute search against.
    */
   public void setContentType(String contentType)
   {
      this.contentType = contentType;
   }
   
   /**
    * @return Returns the mimeType.
    */
   public String getMimeType()
   {
      return this.mimeType;
   }
   /**
    * @param mimeType The mimeType to set.
    */
   public void setMimeType(String mimeType)
   {
      this.mimeType = mimeType;
   }

   /**
    * @return Returns true to search location children, false for just the specified location.
    */
   public boolean getLocationChildren()
   {
      return this.locationChildren;
   }
   
   /**
    * @param locationChildren    True to search location children, false for just the specified location.
    */
   public void setLocationChildren(boolean locationChildren)
   {
      this.locationChildren = locationChildren;
   }
   
   /**
    * @return Returns true to search category children, false for just the specified category.
    */
   public boolean getCategoryChildren()
   {
      return this.categoryChildren;
   }
   
   /**
    * @param categoryChildren    True to search category children, false for just the specified category.
    */
   public void setCategoryChildren(boolean categoryChildren)
   {
      this.categoryChildren = categoryChildren;
   }
   
   /**
    * Add an additional attribute to search against
    * 
    * @param qname      QName of the attribute to search against
    * @param value      Value of the attribute to use
    */
   public void addAttributeQuery(QName qname, String value)
   {
      this.queryAttributes.put(qname, value);
   }
   
   /**
    * Add an additional range attribute to search against
    * 
    * @param qname      QName of the attribute to search against
    * @param lower      Lower value for range
    * @param upper      Upper value for range
    * @param inclusive  True for inclusive within the range, false otherwise
    */
   public void addRangeQuery(QName qname, String lower, String upper, boolean inclusive)
   {
      this.rangeAttributes.put(qname, new RangeProperties(qname, lower, upper, inclusive));
   }
   
   /**
    * Add an additional fixed value attribute to search against
    * 
    * @param qname      QName of the attribute to search against
    * @param value      Fixed value of the attribute to use
    */
   public void addFixedValueQuery(QName qname, String value)
   {
      this.queryFixedValues.put(qname, value);
   }
   
   
   /**
    * Simple wrapper class for range query attribute properties 
    */
   private static class RangeProperties
   {
      QName qname;
      String lower;
      String upper;
      boolean inclusive;
      
      RangeProperties(QName qname, String lower, String upper, boolean inclusive)
      {
         this.qname = qname;
         this.lower = lower;
         this.upper = upper;
         this.inclusive = inclusive;
      }
   }
}
