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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.wcm.AVMConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

/**
 * temporary home of generate and regenerate functionality until i figure
 * out a more general way of triggering generate in TemplateOutputMethod
 * every time the xml file is saved.
 */
public class OutputUtil
{
   private static final Log LOGGER = LogFactory.getLog(OutputUtil.class);
   
   private static String stripExtension(String s)
   {
      return s.replaceAll("(.+)\\..*", "$1");
   }
   
   public static void generate(String parentPath,
                               Document xml, 
                               TemplateType tt, 
                               String fileName,
                               ContentService contentService,
                               NodeService nodeService,
                               AVMService avmService)
      throws Exception 
   {
      try 
      {
         for (TemplateOutputMethod tom : tt.getOutputMethods())
         {
            // get the node ref of the node that will contain the content
            final String generatedFileName = stripExtension(fileName) + "." + tom.getFileExtension();
            final OutputStream fileOut = avmService.createFile(parentPath, generatedFileName);
            final String fullAvmPath = parentPath + '/' + generatedFileName;
            final String avmStore = parentPath.substring(0, parentPath.indexOf(":/"));
            final String sandBoxUrl = AVMConstants.buildAVMStoreUrl(avmStore);
         
            if (LOGGER.isDebugEnabled())
               LOGGER.debug("Created file node for file: " + 
                            fullAvmPath);
            final OutputStreamWriter out = new OutputStreamWriter(fileOut);

            final HashMap<String, String> parameters =
               getOutputMethodParameters(sandBoxUrl, fileName, generatedFileName, parentPath);
            tom.generate(xml, tt, parameters, out);
            out.close();
            
            NodeRef outputNodeRef = AVMNodeConverter.ToNodeRef(-1, fullAvmPath);
            Map<QName, Serializable> props = new HashMap<QName, Serializable>();
            props.put(WCMModel.PROP_TEMPLATE_DERIVED_FROM, tt.getNodeRef());
            props.put(WCMModel.PROP_TEMPLATE_DERIVED_FROM_NAME, tt.getName());
            nodeService.addAspect(outputNodeRef, WCMModel.ASPECT_TEMPLATE_DERIVED, props);

            props = new HashMap<QName, Serializable>(1, 1.0f);
            props.put(ContentModel.PROP_TITLE, fileName);
            nodeService.addAspect(outputNodeRef, ContentModel.ASPECT_TITLED, props);
            
            LOGGER.debug("generated " + generatedFileName + " using " + tom);
         }
      }
      catch (Exception e)
      {
         LOGGER.error(e);
         e.printStackTrace();
         throw e;
      }
   }
   
   public static void regenerate(final NodeRef nodeRef, 
                                 final ContentService contentService,
                                 final NodeService nodeService,
                                 final AVMService avmService)
      throws Exception 
   {
      try 
      {
         final TemplatingService ts = TemplatingService.getInstance();
         final NodeRef templateTypeNodeRef = (NodeRef)
            nodeService.getProperty(nodeRef, WCMModel.PROP_TEMPLATE_DERIVED_FROM);

         final TemplateType tt = ts.getTemplateType(templateTypeNodeRef);
         
         final ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
         final Document xml = ts.parseXML(reader.getContentInputStream());
         final String fileName = (String)
            nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
         final String avmPath = AVMNodeConverter.ToAVMVersionPath(nodeRef).getSecond();
         final String avmStore = avmPath.substring(0, avmPath.indexOf(":/"));
         final String sandBoxUrl = AVMConstants.buildAVMStoreUrl(avmStore);
         final String parentPath = AVMNodeConverter.SplitBase(avmPath)[0];
         for (TemplateOutputMethod tom : tt.getOutputMethods())
         {
            final String generatedFileName = stripExtension(fileName) + "." + tom.getFileExtension();

            if (LOGGER.isDebugEnabled())
               LOGGER.debug("regenerating file node for : " + fileName + " (" +
                            nodeRef.toString() + ") to " + parentPath + "/" + generatedFileName);
            
            // get a writer for the content and put the file
            OutputStream out = null;
            try
            {
               out = avmService.getFileOutputStream(parentPath + "/" + generatedFileName);
            }
            catch (AVMNotFoundException e)
            {
               out = avmService.createFile(parentPath, generatedFileName);
            }

            final OutputStreamWriter writer = new OutputStreamWriter(out);
            final HashMap<String, String> parameters =
               getOutputMethodParameters(sandBoxUrl, fileName, generatedFileName, parentPath);
            tom.generate(xml, tt, parameters, writer);
            writer.close();
            LOGGER.debug("generated " + fileName + " using " + tom);
         }
      }
      catch (Exception e)
      {
         LOGGER.error(e);
         e.printStackTrace();
         throw e;
      }
   }

   private static HashMap<String, String> getOutputMethodParameters(final String sandBoxUrl,
                                                                    final String fileName,
                                                                    final String generatedFileName,
                                                                    final String parentPath)
   {
      final HashMap<String, String> parameters = new HashMap<String, String>();      
      parameters.put("avm_store_url", sandBoxUrl);
      parameters.put("derived_from_file_name", fileName);
      parameters.put("generated_file_name", generatedFileName);
      parameters.put("parent_path", parentPath);
      return parameters;
   }
}
