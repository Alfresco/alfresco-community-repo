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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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
         // get the node ref of the node that will contain the content
         String generatedFileName = stripExtension(fileName) + ".shtml";
         
         OutputStream fileOut = avmService.createFile(parentPath, generatedFileName);
         
         String fullAvmPath = parentPath + '/' + generatedFileName;
         
         String avmStore = parentPath.substring(0, parentPath.indexOf(":/"));
         String sandBoxUrl = AVMConstants.buildAVMStoreUrl(avmStore);
         
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("Created file node for file: " + 
                  fullAvmPath);
         
         TemplateOutputMethod tom = tt.getOutputMethods().get(0);
         OutputStreamWriter out = new OutputStreamWriter(fileOut);
         tom.generate(xml, tt, sandBoxUrl, out);
         out.close();
         
         NodeRef outputNodeRef = AVMNodeConverter.ToNodeRef(-1, fullAvmPath);
         nodeService.setProperty(outputNodeRef,
               TemplatingService.TT_QNAME,
               tt.getName());
         
         LOGGER.debug("generated " + generatedFileName + " using " + tom);
         
         NodeRef createdNodeRef = AVMNodeConverter.ToNodeRef(-1, parentPath + '/' + fileName);
         nodeService.setProperty(createdNodeRef,
               TemplatingService.TT_GENERATED_OUTPUT_QNAME,
               outputNodeRef.toString());
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
         final NodeService nodeService)
      throws Exception 
   {
      try 
      {
         final TemplatingService ts = TemplatingService.getInstance();
         final String templateTypeName = (String)
         nodeService.getProperty(nodeRef, TemplatingService.TT_QNAME);
         final TemplateType tt = ts.getTemplateType(templateTypeName);
         
         final ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
         final Document xml = ts.parseXML(reader.getContentInputStream());
         String fileName = (String)
         nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
         NodeRef generatedNodeRef = 
            new NodeRef((String)
                  nodeService.getProperty(nodeRef,
                        TemplatingService.TT_GENERATED_OUTPUT_QNAME));
         String generatedFileName = (String)
         nodeService.getProperty(generatedNodeRef, 
               ContentModel.PROP_NAME);
         String avmPath = AVMNodeConverter.ToAVMVersionPath(nodeRef).getSecond();
         String avmStore = avmPath.substring(0, avmPath.indexOf(":/"));
         String sandBoxUrl = AVMConstants.buildAVMStoreUrl(avmStore);
         
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("regenerating file node for : " + fileName + " (" +
                  nodeRef.toString() + ") to " + generatedNodeRef.toString());
         
         // get a writer for the content and put the file
         ContentWriter writer = contentService.getWriter(generatedNodeRef, 
               ContentModel.PROP_CONTENT,
               true);
         // set the mimetype and encoding
         writer.setMimetype("text/html");
         writer.setEncoding("UTF-8");
         // put a loop to generate all output methods
         TemplateOutputMethod tom = tt.getOutputMethods().get(0);
         OutputStreamWriter out = new OutputStreamWriter(writer.getContentOutputStream());
         tom.generate(xml, tt, sandBoxUrl, out);
         out.close();
         
         LOGGER.debug("generated " + fileName + " using " + tom);
      }
      catch (Exception e)
      {
         LOGGER.error(e);
         e.printStackTrace();
         throw e;
      }
   }
}
