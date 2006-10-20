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
import freemarker.template.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.templating.*;
import org.alfresco.web.templating.extension.ExtensionFunctions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chiba.xml.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Implementation of a form data renderer for processing xml instance data
 * using a freemarker template.
 *
 * @author Ariel Backenroth
 */
public class FreeMarkerOutputMethod
   extends AbstractFormDataRenderer
{

   private static final Log LOGGER = LogFactory.getLog(FreeMarkerOutputMethod.class);

   public FreeMarkerOutputMethod(final NodeRef nodeRef,
                                 final NodeService nodeService,
                                 final ContentService contentService)
   {
      super(nodeRef, nodeService, contentService);
   }

   /**
    * Generates the rendition using the configured freemarker template.  This
    * provides a root map to the freemarker template which places the xml document, and 
    * a variable named alfresco at the root.  the alfresco variable contains a hash of 
    * all parameters and all extension functions.
    */
   public void generate(final Document xmlContent,
                        final TemplateType tt,
                        final Map<String, String> parameters,
                        final Writer out)
      throws IOException,
      TemplateException
   {
      final ContentReader contentReader = 
         this.contentService.getReader(this.getNodeRef(), ContentModel.TYPE_CONTENT);
      final Reader reader = new InputStreamReader(contentReader.getContentInputStream());
      final Configuration cfg = new Configuration();
      cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
      
      final Template t = new Template("freemarker_template", reader, cfg);
      
      // wrap the xml instance in a model
      final TemplateHashModel instanceDataModel = NodeModel.wrap(xmlContent);

      // build models for each of the extension functions
      final TemplateModel getXMLDocumentModel = new TemplateMethodModel()
      {
         public Object exec(final List args)
            throws TemplateModelException
         {
            try 
            {
               final ExtensionFunctions ef = FreeMarkerOutputMethod.getExtensionFunctions();
               final String path = FreeMarkerOutputMethod.toAVMPath(parameters.get("parent_path"), 
                                                                    (String)args.get(0));
               return ef.getXMLDocument(path);
            }
            catch (Exception e)
            {
               throw new TemplateModelException(e);
            }
         }
      };
      final TemplateModel getXMLDocumentsModel = new TemplateMethodModel()
      {
         public Object exec(final List args)
            throws TemplateModelException
         {
            try 
            {
               final ExtensionFunctions ef = FreeMarkerOutputMethod.getExtensionFunctions();
               final String path = FreeMarkerOutputMethod.toAVMPath(parameters.get("parent_path"), 
                                                                    args.size() == 1 ? "" : (String)args.get(1));
               final Map<String, Document> resultMap = ef.getXMLDocuments((String)args.get(0), path);
               LOGGER.debug("received " + resultMap.size() + " documents in " + path);
               final List<NodeModel> result = new ArrayList<NodeModel>(resultMap.size());
               for (Map.Entry<String, Document> e : resultMap.entrySet())
               {
                  final Document d = e.getValue();
                  final Element documentEl = d.getDocumentElement();
                  documentEl.setAttribute("xmlns:" + ALFRESCO_NS_PREFIX, ALFRESCO_NS); 
                  documentEl.setAttributeNS(ALFRESCO_NS, 
                                            ALFRESCO_NS_PREFIX + ":file-name", 
                                            e.getKey());
                  result.add(NodeModel.wrap(d));
               }
               return result;
            }
            catch (Exception e)
            {
               throw new TemplateModelException(e);
            }
         }
      };

      // build a wrapper for the parameters.  this also wraps the extension functions
      // so they appear in the namespace alfresco.
      final TemplateHashModel parameterModel = new SimpleHash(parameters)
      {
         public TemplateModel get(final String key)
            throws TemplateModelException
         {
            if ("getXMLDocument".equals(key))
            {
                return getXMLDocumentModel;
            }
            if ("getXMLDocuments".equals(key))
            {
               return getXMLDocumentsModel;
            }
            return super.get(key);
         }
      };
      
      // build the root model.  anything not in the falsey alfresco namespace will be 
      // retrieved from the xml file in order to make it behave as close as possible to
      // the xsl environment
      final TemplateHashModel rootModel = new TemplateHashModel()
      {
         public TemplateModel get(final String key)
            throws TemplateModelException
         {
            return ALFRESCO_NS_PREFIX.equals(key) ? parameterModel : instanceDataModel.get(key);
         }

         public boolean isEmpty()
         {
            return false;
         }
      };

      // process the form
      t.process(rootModel, out);
      out.flush();
   }
}
