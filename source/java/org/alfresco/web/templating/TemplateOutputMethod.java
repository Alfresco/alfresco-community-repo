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
package org.alfresco.web.templating;

import java.io.Serializable;
import java.io.Writer;
import java.util.Map;
import org.w3c.dom.Document;

/**
 * Serializes the xml data to a writer.
 */
public interface TemplateOutputMethod
   extends Serializable
{

   /**
    * Serializes the xml data in to a presentation format.
    *
    * @param xmlContent the xml content to serialize
    * @param tt the template type that collected the xml content.
    * @param sandBoxUrl the url of the current sandbox
    * @param out the writer to serialize to.
    */
   public void generate(final Document xmlContent,
                        final TemplateType tt,
                        final Map<String, String> parameters,
                        final Writer out)
      throws Exception;

   /**
    * Returns the file extension to use when generating content for this
    * output method.
    *
    * @return the file extension to use when generating content for this
    * output method, such as html, rss, pdf.
    */
   public String getFileExtension();
}
