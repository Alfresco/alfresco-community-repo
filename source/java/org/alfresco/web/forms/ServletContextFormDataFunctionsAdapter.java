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
package org.alfresco.web.forms;

import org.alfresco.jndi.AVMFileDirContext;
import org.alfresco.util.JNDIPath;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.Map;

public class ServletContextFormDataFunctionsAdapter
   extends FormDataFunctions
{

   private final ServletContext servletContext;

   public ServletContextFormDataFunctionsAdapter(final ServletContext servletContext)
   {
      super(AVMFileDirContext.getAVMRemote());
      this.servletContext = servletContext;
   }

   private String toAVMPath(String path)
   {
      // The "real path" will look something like:
      //   /media/alfresco/cifs/v/mysite--bob/VERSION/v-1/DATA/www/avm_webapps/ROOT/media/releases/content

      path = this.servletContext.getRealPath(path);
      try
      {
         // The corresponding AVM path will look something like:
         //   mysite--bob:/www/avm_webapps/ROOT/media/releases/content

         final JNDIPath jndiPath = new JNDIPath(AVMFileDirContext.getAVMFileDirMountPoint(), path);
         return jndiPath.getAvmPath();
      }
      catch (Exception e)
      {
         System.err.println(e.getMessage());
         return path;
      }
   }

   public Document parseXMLDocument(final String path)
      throws IOException,
      SAXException
   {
      return super.parseXMLDocument(this.toAVMPath(path));
   }

   public Map<String, Document> parseXMLDocuments(final String formName,
                                                  final String path)
      throws IOException,
      SAXException
   {
      return super.parseXMLDocuments(formName, this.toAVMPath(path));
   }
}
