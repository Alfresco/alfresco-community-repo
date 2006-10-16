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

import freemarker.ext.dom.NodeModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.templating.*;
import org.chiba.xml.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class FreeMarkerOutputMethod
   implements TemplateOutputMethod
{

   private final NodeRef nodeRef;
   private final NodeService nodeService;
   private final ContentService contentService;

   public FreeMarkerOutputMethod(final NodeRef nodeRef,
                                 final NodeService nodeService,
                                 final ContentService contentService)
   {
      this.nodeRef = nodeRef;
      this.nodeService = nodeService;
      this.contentService = contentService;
   }

   public void generate(final Document xmlContent,
                        final TemplateType tt,
                        final Map<String, String> parameters,
                        final Writer out)
      throws IOException,
      TemplateException
   {
      final ContentReader contentReader = 
         this.contentService.getReader(nodeRef, ContentModel.TYPE_CONTENT);
      final Reader reader = new InputStreamReader(contentReader.getContentInputStream());
      final Configuration cfg = new Configuration();
      cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
      
      final Template t = new Template("freemarker_template", reader, cfg);
      final Map<String, Object> root = new HashMap<String, Object>();
      root.put("doc", NodeModel.wrap(xmlContent));
      t.process(root, out);
      out.flush();
   }

   public String getFileExtension()
   {
      return (String)
         this.nodeService.getProperty(this.nodeRef, 
                                      WCMModel.PROP_TEMPLATE_OUTPUT_METHOD_DERIVED_FILE_EXTENSION);
   }
}
