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

import freemarker.ext.dom.NodeModel;
import freemarker.template.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.bean.wcm.AVMConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Implementation of a form data renderer for processing xml instance data
 * using a freemarker template.
 *
 * @author Ariel Backenroth
 */
public class FreeMarkerRenderingEngine
   extends AbstractRenderingEngine
{

   private static final Log LOGGER = LogFactory.getLog(FreeMarkerRenderingEngine.class);

   public FreeMarkerRenderingEngine()
   {
      super();
   }

   public String getName()
   {
      return "FreeMarker";
   }

   public String getDefaultTemplateFileExtension() 
   {
      return "ftl";
   }

   /**
    * Renders the rendition using the configured freemarker template.  This
    * provides a root map to the freemarker template which places the xml document, and 
    * a variable named alfresco at the root.  the alfresco variable contains a hash of 
    * all parameters and all extension functions.
    */
   public void render(final FormInstanceData formInstanceData,
                      final RenderingEngineTemplate ret,
                      final Rendition rendition)
      throws IOException,
      RenderingEngine.RenderingException,
      SAXException
   {
      final Map<String, String> parameters = 
         this.getStandardParameters(formInstanceData, rendition);

      final Configuration cfg = new Configuration();
      cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

      final Template t = new Template("freemarker_template", 
                                      new InputStreamReader(ret.getInputStream()),
                                      cfg);
      
      // wrap the xml instance in a model
      final TemplateHashModel instanceDataModel = NodeModel.wrap(formInstanceData.getDocument());

      // build models for each of the extension functions
      final HashMap<String, TemplateMethodModel> methodModels =
         new HashMap<String, TemplateMethodModel>(3, 1.0f);

      methodModels.put("parseXMLDocument", new TemplateMethodModel()
      {
         public Object exec(final List args)
            throws TemplateModelException
         {
            try 
            {
               final FormDataFunctions ef = FreeMarkerRenderingEngine.getFormDataFunctions();
               final String path = 
                  AVMConstants.buildPath(parameters.get("parent_path"), 
                                         (String)args.get(0),
                                         AVMConstants.PathRelation.WEBAPP_RELATIVE);
               final Document d = ef.parseXMLDocument(path);
               return d != null ? d.getDocumentElement() : null;
            }
            catch (Exception e)
            {
               throw new TemplateModelException(e);
            }
         }
      });

      methodModels.put("parseXMLDocuments", new TemplateMethodModel()
      {
         public Object exec(final List args)
            throws TemplateModelException
         {
            try 
            {
               final FormDataFunctions ef = FreeMarkerRenderingEngine.getFormDataFunctions();
               final String path = 
                  AVMConstants.buildPath(parameters.get("parent_path"), 
                                         args.size() == 1 ? "" : (String)args.get(1),
                                         AVMConstants.PathRelation.WEBAPP_RELATIVE);
               final Map<String, Document> resultMap = ef.parseXMLDocuments((String)args.get(0), path);
               LOGGER.debug("received " + resultMap.size() + " documents in " + path);

               // create a root document for rooting all the results.  we do this
               // so that each document root element has a common parent node
               // and so that xpath axes work properly
               final Document rootNodeDocument = XMLUtil.newDocument();
               final Element rootNodeDocumentEl = 
                  rootNodeDocument.createElementNS(ALFRESCO_NS,
                                                   ALFRESCO_NS_PREFIX + ":file_list");
               rootNodeDocumentEl.setAttribute("xmlns:" + ALFRESCO_NS_PREFIX, ALFRESCO_NS); 
               rootNodeDocument.appendChild(rootNodeDocumentEl);
               
               final List<NodeModel> result = new ArrayList<NodeModel>(resultMap.size());
               for (Map.Entry<String, Document> e : resultMap.entrySet())
               {
                  final Element documentEl = e.getValue().getDocumentElement();
                  documentEl.setAttribute("xmlns:" + ALFRESCO_NS_PREFIX, ALFRESCO_NS); 
                  documentEl.setAttributeNS(ALFRESCO_NS, 
                                            ALFRESCO_NS_PREFIX + ":file_name", 
                                            e.getKey());
                  final Node n = rootNodeDocument.importNode(documentEl, true);
                  rootNodeDocumentEl.appendChild(n);
                  result.add(NodeModel.wrap(n));
               }
               return result;
            }
            catch (Exception e)
            {
               throw new TemplateModelException(e);
            }
         }
      });

      // for debugging
      methodModels.put("_getAVMPath", new TemplateMethodModel()
      {
         public Object exec(final List args)
            throws TemplateModelException
         {
            try 
            {
               return AVMConstants.buildPath(parameters.get("parent_path"), 
                                             (String)args.get(0),
                                             AVMConstants.PathRelation.WEBAPP_RELATIVE);
            }
            catch (Exception e)
            {
               throw new TemplateModelException(e);
            }
         }
      });

      // build a wrapper for the parameters.  this also wraps the extension functions
      // so they appear in the namespace alfresco.
      final TemplateHashModel parameterModel = new SimpleHash(parameters)
      {

         public TemplateModel get(final String key)
            throws TemplateModelException
         {
            return (methodModels.containsKey(key)
                    ? methodModels.get(key)
                    : super.get(key));
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
            return (ALFRESCO_NS_PREFIX.equals(key) 
                    ? parameterModel 
                    : instanceDataModel.get(key));
         }

         public boolean isEmpty()
         {
            return false;
         }
      };

      // process the form
      final Writer writer = new OutputStreamWriter(rendition.getOutputStream());
      try
      {
         t.process(rootModel, writer);
      }
      catch (final TemplateException te)
      {
         LOGGER.debug(te);
         throw new RenderingEngine.RenderingException(te);
      }
      finally
      {
         writer.flush();
         writer.close();
      }
   }
}
