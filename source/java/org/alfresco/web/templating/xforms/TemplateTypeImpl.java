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
package org.alfresco.web.templating.xforms;

import java.io.*;
import java.net.URI;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.templating.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class TemplateTypeImpl 
    implements TemplateType
{
   private static final Log LOGGER = LogFactory.getLog(TemplateTypeImpl.class);
   
   private transient Document schema;
   private final NodeRef schemaNodeRef;
   private final String name;
   private final String rootTagName;
   private final LinkedList<TemplateOutputMethod> outputMethods = 
      new LinkedList<TemplateOutputMethod>();
   private final static LinkedList<TemplateInputMethod> INPUT_METHODS = 
      new LinkedList<TemplateInputMethod>();
   
   static 
   {
      INPUT_METHODS.add(new XFormsInputMethod());
   }
   
   public TemplateTypeImpl(final String name,
                           final NodeRef schemaNodeRef,
                           final String rootTagName) 
   {
      this.name = name;
      this.schemaNodeRef = schemaNodeRef;
      this.rootTagName = rootTagName;
   }
   
   public String getName()
   {
      return this.name;
   }

   public String getRootTagName()
   {
      return this.rootTagName;
   }

   public Document getSchema()
   {
      if (this.schema == null)
      {
         final TemplatingService ts = TemplatingService.getInstance();
         try
         {
            //XXXarielb maybe cloneNode instead?
            return /* this.schema = */ ts.parseXML(this.schemaNodeRef);
         }
         catch (Exception e)
         {
            LOGGER.error(e);
         }
      }
      return this.schema;
   }

   public List<TemplateInputMethod> getInputMethods()
   {
      return INPUT_METHODS;
   }

   public void addOutputMethod(TemplateOutputMethod output)
   {
      this.outputMethods.add(output);
   }

   public List<TemplateOutputMethod> getOutputMethods()
   {
      return this.outputMethods;
   }

   public int hashCode() 
   {
      return this.getName().hashCode();
   }
}