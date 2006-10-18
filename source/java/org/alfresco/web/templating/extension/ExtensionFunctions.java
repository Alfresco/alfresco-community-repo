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
package org.alfresco.web.templating.extension;

import org.alfresco.model.WCMModel;
import org.alfresco.repo.avm.AVMRemote;
import org.alfresco.repo.avm.AVMRemoteInputStream;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import java.io.*;
import java.util.Map;
import java.util.HashMap;

public class ExtensionFunctions
{
   private static final Log LOGGER = LogFactory.getLog(ExtensionFunctions.class);

   private static DocumentBuilder documentBuilder;

   private final AVMRemote avmRemote;

   public ExtensionFunctions(final AVMRemote avmRemote)
   {
      this.avmRemote = avmRemote;
   }

   public Document getXMLDocument(final String avmPath)
      throws IOException,
      SAXException
   {
      final DocumentBuilder db = this.getDocumentBuilder();
      final InputStream istream = 
         new AVMRemoteInputStream(this.avmRemote.getInputHandle(-1, avmPath), 
                                  this.avmRemote);

      Document result;
      try 
      {
         return db.parse(istream);
      }
      finally
      {
         istream.close();
      }
   }

   public Map<String, Document> getXMLDocuments(final String templateTypeName, final String avmPath)
      throws IOException,
      SAXException
   {
       final Map<String, AVMNodeDescriptor> entries = 
          this.avmRemote.getDirectoryListing(-1, avmPath);
      final DocumentBuilder db = this.getDocumentBuilder();
      final Map<String, Document> result = new HashMap<String, Document>();
      for (Map.Entry<String, AVMNodeDescriptor> entry : entries.entrySet())
      {
         final String entryName = entry.getKey();
         AVMNodeDescriptor entryNode = entry.getValue();
         if (entryNode.isFile())
         {
            final PropertyValue pv = 
               this.avmRemote.getNodeProperty(-1, 
                                              avmPath + '/' + entryName,
                                              WCMModel.PROP_TEMPLATE_DERIVED_FROM_NAME);
            if (pv != null && 
                pv.getStringValue() != null && 
                ((String)pv.getStringValue()).equals(templateTypeName))
            {
               
               InputStream istream = null;
               try
               {
                  istream = new AVMRemoteInputStream(this.avmRemote.getInputHandle(-1, avmPath + '/' + entryName), 
                                                     this.avmRemote);
               }
               catch (AVMNotFoundException avmnfe)
               {
                  // this is most likely happening because this is the current file we're generating
                  // and the avm is telling us that it has no content yet.  we won't hit this once
                  // we have a way of distinguishing templateoutputmethodgenerated
                  // from templategenerated
                  LOGGER.debug("skipping "+ entryName, avmnfe);
               }
               try
               {
                  result.put(entryName, db.parse(istream));
               }
               catch (SAXException sax)
               {
                  // this is most likely happening because we have the same property for defined 
                  // for tempalteoutputmethodderived and templatederived so we can't distinguish them right now
                  // need to clean this up
                  LOGGER.debug("error parsing " + entryName+ "...  skipping", sax);
               }
               finally
               {
                  istream.close();
               }
            }
         }
      }
      return result;
   }

   private static DocumentBuilder getDocumentBuilder()
   {
      if (ExtensionFunctions.documentBuilder == null)
      {
         try
         {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setValidating(false);
            ExtensionFunctions.documentBuilder = dbf.newDocumentBuilder();
         }
         catch (ParserConfigurationException pce)
         {
            LOGGER.error(pce);
         }
      }
      return ExtensionFunctions.documentBuilder;
   }
}