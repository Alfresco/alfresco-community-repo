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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
