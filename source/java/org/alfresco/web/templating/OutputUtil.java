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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigService;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.templating.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import java.io.OutputStreamWriter;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.w3c.dom.Document;
import org.alfresco.repo.avm.*;

public class OutputUtil
{
    private static final Log LOGGER = LogFactory.getLog(OutputUtil.class);

    public static void generate(NodeRef createdNode,
				Document xml, 
				TemplateType tt, 
				String fileName,
				NodeRef containerNodeRef,
				FileFolderService fileFolderService,
				ContentService contentService,
				NodeService nodeService)
	throws Exception 
    {
	try 
	{
	    // get the node ref of the node that will contain the content
	    fileName = fileName + "-generated.html";
	    FileInfo fileInfo = 
		fileFolderService.create(containerNodeRef,
					 fileName,
					 ContentModel.TYPE_CONTENT);
	    NodeRef fileNodeRef = fileInfo.getNodeRef();
	
	    if (LOGGER.isDebugEnabled())
		LOGGER.debug("Created file node for file: " + 
			     fileName);
	
	    // get a writer for the content and put the file
	    ContentWriter writer = contentService.getWriter(fileNodeRef, 
							    ContentModel.PROP_CONTENT, true);
	    // set the mimetype and encoding
	    writer.setMimetype("text/html");
	    writer.setEncoding("UTF-8");
	    TemplateOutputMethod tom = tt.getOutputMethods().get(0);
	    OutputStreamWriter out = 
		new OutputStreamWriter(writer.getContentOutputStream());
	    tom.generate(xml, tt, out);
	    out.close();
	    nodeService.setProperty(fileNodeRef,
				    TemplatingService.TT_QNAME,
				    tt.getName());

	    LOGGER.debug("generated " + fileName + " using " + tom);

	    if (createdNode != null)
	    {
		nodeService.setProperty(createdNode,
					TemplatingService.TT_GENERATED_OUTPUT_QNAME,
					fileNodeRef.toString());
	    }

	    AVMService avmService = AVMContext.fgInstance.getAVMService();
	    String parentPath = "repo-1:/repo-1/alice/appBase/avm_webapps/my_webapp";
	    try 
	    {
		out =  new OutputStreamWriter(avmService.createFile(parentPath, fileName));
	    }
	    catch (AVMExistsException e)
	    {
		out = new OutputStreamWriter(avmService.getFileOutputStream(parentPath + "/" + fileName));
	    }
	    LOGGER.debug("generating " + fileName + " to avm");
	    tom.generate(xml, tt, out);
	    out.close();
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
	    TemplateOutputMethod tom = tt.getOutputMethods().get(0);
	    OutputStreamWriter out = 
		new OutputStreamWriter(writer.getContentOutputStream());
	    tom.generate(xml, tt, out);
	    out.close();

	    LOGGER.debug("generated " + fileName + " using " + tom);

	    AVMService avmService = AVMContext.fgInstance.getAVMService();
	    String parentPath = "repo-1:/repo-1/alice/appBase/avm_webapps/ROOT/home-insurance";
	    try 
	    {
		out =  new OutputStreamWriter(avmService.createFile(parentPath, generatedFileName));
	    }
	    catch (AVMExistsException e)
	    {
		out = new OutputStreamWriter(avmService.getFileOutputStream(parentPath + "/" + generatedFileName));
	    }
	    LOGGER.debug("generating " + generatedFileName + " to avm");
	    tom.generate(xml, tt, out);
	    out.close();
	}
	catch (Exception e)
	{
	    LOGGER.error(e);
	    e.printStackTrace();
	    throw e;
	}
    }
}