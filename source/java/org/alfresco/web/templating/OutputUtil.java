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

import java.io.OutputStreamWriter;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMContext;
import org.alfresco.service.cmr.avm.AVMExistsException;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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
    private static final String PARENT_AVM_PATH = 
	"repo-1:/repo-1/alice/appBase/avm_webapps/ROOT";

    private static String stripExtension(String s)
    {
	return s.replaceAll("(.+)\\..*", "$1");
    }

    private static String getAVMParentPath(NodeRef nodeRef,
					   NodeService nodeService)
	throws Exception
    {
	ChildAssociationRef caf = nodeService.getPrimaryParent(nodeRef);
	final String parentName = (String)
	    nodeService.getProperty(caf.getParentRef(), ContentModel.PROP_NAME);
	LOGGER.debug("computed avm path " + PARENT_AVM_PATH + "/" + parentName);
	final String result = PARENT_AVM_PATH + "/" + parentName;
	AVMService avmService = (AVMService)AVMContext.fgInstance.fAppContext.getBean("avmService");
	if (avmService.lookup(-1, result) != null)
    {
	    return result;
	}
    else
	{
	    //	    avmService.createDirectory(PARENT_AVM_PATH, parentName);
	    return PARENT_AVM_PATH;
	}
    }

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
	    String generatedFileName = stripExtension(fileName) + ".shtml";
	    FileInfo fileInfo = 
		fileFolderService.create(containerNodeRef,
					 generatedFileName,
					 ContentModel.TYPE_CONTENT);
	    NodeRef fileNodeRef = fileInfo.getNodeRef();
	
	    if (LOGGER.isDebugEnabled())
		LOGGER.debug("Created file node for file: " + 
			     generatedFileName);
	
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

	    LOGGER.debug("generated " + generatedFileName + " using " + tom);

	    if (createdNode != null)
	    {
		nodeService.setProperty(createdNode,
					TemplatingService.TT_GENERATED_OUTPUT_QNAME,
					fileNodeRef.toString());
	    }

	    AVMService avmService = (AVMService)AVMContext.fgInstance.fAppContext.getBean("avmService");
	    final String parentAVMPath = getAVMParentPath(createdNode, nodeService);
	    try 
	    {
		out =  new OutputStreamWriter(avmService.createFile(parentAVMPath, generatedFileName));
	    }
	    catch (AVMExistsException e)
	    {
		out = new OutputStreamWriter(avmService.getFileOutputStream(parentAVMPath + "/" + generatedFileName));
	    }
	    LOGGER.debug("generating " + generatedFileName + " to avm");
	    tom.generate(xml, tt, out);
	    out.close();
	    try 
	    {
		out =  new OutputStreamWriter(avmService.createFile(parentAVMPath, generatedFileName));
	    }
	    catch (AVMExistsException e)
	    {
		out = new OutputStreamWriter(avmService.getFileOutputStream(parentAVMPath + "/" + generatedFileName));
	    }
	    LOGGER.debug("generating " + generatedFileName + " to avm");
	    tom.generate(xml, tt, out);
	    out.close();

	    try 
	    {
		out =  new OutputStreamWriter(avmService.createFile(parentAVMPath, fileName));
	    }
	    catch (AVMExistsException e)
	    {
		out = new OutputStreamWriter(avmService.getFileOutputStream(parentAVMPath + "/" + fileName));
	    }
	    LOGGER.debug("writing xml " + fileName + " to avm");
	    final TemplatingService ts = TemplatingService.getInstance();
	    ts.writeXML(xml, out);
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
	    // put a loop to generate all output methods
	    TemplateOutputMethod tom = tt.getOutputMethods().get(0);
	    OutputStreamWriter out = 
		new OutputStreamWriter(writer.getContentOutputStream());
	    tom.generate(xml, tt, out);
	    out.close();

	    LOGGER.debug("generated " + fileName + " using " + tom);

	    AVMService avmService = (AVMService)AVMContext.fgInstance.fAppContext.getBean("avmService");
	    final String parentAVMPath = getAVMParentPath(nodeRef, nodeService);
	    try 
	    {
		out =  new OutputStreamWriter(avmService.createFile(parentAVMPath, generatedFileName));
	    }
	    catch (AVMExistsException e)
	    {
		out = new OutputStreamWriter(avmService.getFileOutputStream(parentAVMPath + "/" + generatedFileName));
	    }
	    LOGGER.debug("generating " + generatedFileName + " to avm");
	    tom.generate(xml, tt, out);
	    out.close();

	    try 
	    {
		out =  new OutputStreamWriter(avmService.createFile(parentAVMPath, fileName));
	    }
	    catch (AVMExistsException e)
	    {
		out = new OutputStreamWriter(avmService.getFileOutputStream(parentAVMPath + "/" + fileName));
	    }
	    LOGGER.debug("writing xml " + fileName + " to avm");
	    ts.writeXML(xml, out);
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
