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

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.alfresco.web.templating.xforms.*;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.model.ContentModel;
import org.alfresco.util.TempFileProvider;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.model.*;
import org.alfresco.service.namespace.NamespaceService;
import javax.faces.context.FacesContext;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;

public final class TemplatingService
    implements Serializable
{

    ////////////////////////////////////////////////////////////////////////////

    private static class Configuration
    {
	private final static File CONFIG_FILE  = 
	    new File(TempFileProvider.getTempDir(), "templating_configuration.xml");
	
	public static boolean loaded = false;

	private static NodeRef getConfigFile()
	{
	    final TemplatingService ts = TemplatingService.INSTANCE;
	    LOGGER.debug("loading config file");
	    // get the template from the special Email Templates folder
	    FacesContext fc = FacesContext.getCurrentInstance();
	    String xpath = (Application.getRootPath(fc) + "/" + 
			    Application.getGlossaryFolderName(fc));
	    NodeRef rootNodeRef = ts.nodeService.getRootNode(Repository.getStoreRef());
	    List<NodeRef> results = ts.searchService.selectNodes(rootNodeRef, xpath, null, ts.namespaceService, false);
	    if (results.size() != 1)
		throw new RuntimeException("expected one result for " + xpath);
	    NodeRef dataDictionaryNodeRef =  results.get(0);
	    LOGGER.debug("loaded data dictionary " + dataDictionaryNodeRef);
	    NodeRef configFileNodeRef = null;
	    try
	    {
		configFileNodeRef = ts.fileFolderService.create(dataDictionaryNodeRef,
								"templating_configuration.xml",
								ContentModel.TYPE_CONTENT).getNodeRef();
	    }
	    catch (FileExistsException fee)
	    {
		List<FileInfo> l = ts.fileFolderService.search(dataDictionaryNodeRef,
							       "templating_configuration.xml",
							       true,
							       false,
							       false);
		if (l.size() != 1)
		{
		    throw new RuntimeException("expected one templating_configuration.xml in " + dataDictionaryNodeRef);
		}
		configFileNodeRef= l.get(0).getNodeRef();
	    }
	    LOGGER.debug("loaded config file " + configFileNodeRef);
	    return configFileNodeRef;
	}

	public static void load()
	    throws IOException
	{
	    final TemplatingService ts = TemplatingService.INSTANCE;
	    final NodeRef configFileNodeRef = getConfigFile();
	    FacesContext fc = FacesContext.getCurrentInstance();
	    final InputStream contentIn = ts.contentService.getReader(configFileNodeRef, ContentModel.TYPE_CONTENT).getContentInputStream();
	    final ObjectInputStream in = new ObjectInputStream(contentIn);
	    try
	    {
		while (true)
		{
		    try
		    {
			final TemplateType tt = (TemplateType)in.readObject();
			TemplatingService.INSTANCE.registerTemplateType(tt);
		    }
		    catch (EOFException eof)
		    {
			break;
		    }

		}
		in.close();
	    }
	    catch (ClassNotFoundException cnfe)
	    {
	    assert false : cnfe;
		TemplatingService.LOGGER.error(cnfe);
	    }
	    loaded = true;
	}
	
	public static void save()
	    throws IOException
	{
	    final TemplatingService ts = TemplatingService.INSTANCE;
	    FacesContext fc = FacesContext.getCurrentInstance();
	    final NodeRef configFileNodeRef = getConfigFile();
	    final OutputStream contentOut = ts.contentService.getWriter(configFileNodeRef, ContentModel.TYPE_CONTENT, true).getContentOutputStream();
	    if (!CONFIG_FILE.exists())
		CONFIG_FILE.createNewFile();
	    final ObjectOutputStream out = new ObjectOutputStream(contentOut);
	    for (TemplateType tt : TemplatingService.INSTANCE.getTemplateTypes())
	    {
		out.writeObject(tt);
	    }
	    out.close();
	}
    }

    ////////////////////////////////////////////////////////////////////////////


    public static final org.alfresco.service.namespace.QName TT_QNAME = 
	org.alfresco.service.namespace.QName.createQName(org.alfresco.service.namespace.NamespaceService.CONTENT_MODEL_1_0_URI, "tt");

    public static final org.alfresco.service.namespace.QName TT_GENERATED_OUTPUT_QNAME = 
	org.alfresco.service.namespace.QName.createQName(org.alfresco.service.namespace.NamespaceService.CONTENT_MODEL_1_0_URI, "tt_generated_output");

    private static final Log LOGGER = LogFactory.getLog(TemplatingService.class);
    private static TemplatingService INSTANCE;

    private HashMap<String, TemplateType> templateTypes = 
	new HashMap<String, TemplateType>();
    private final ContentService contentService;
    private final NodeService nodeService;
    private final FileFolderService fileFolderService;
    private final DictionaryService dictionaryService;
    private final NamespaceService namespaceService;
    private final SearchService searchService;

    public TemplatingService(final ContentService contentService,
			     final NodeService nodeService,
			     final FileFolderService fileFolderService,
			     final DictionaryService dictionaryService,
			     final NamespaceService namespaceService,
			     final SearchService searchService)
    {
	this.contentService = contentService;
	this.nodeService = nodeService;
	this.fileFolderService = fileFolderService;
	this.dictionaryService = dictionaryService;
	this.namespaceService = namespaceService;
	this.searchService = searchService;
	if (INSTANCE == null)
	{
	    INSTANCE = this;
	}
    }

    public static TemplatingService getInstance()
    {
	if (!Configuration.loaded)
	{
	    LOGGER.debug("loading configuration");
	    try
	    {
		Configuration.load();
	    }
	    catch (Throwable t)
	    {
		LOGGER.error(t);
		t.printStackTrace();
	    }
	}

	return TemplatingService.INSTANCE;
    }

    public Collection<TemplateType> getTemplateTypes()
    {
	return this.templateTypes.values();
    }

    public TemplateType getTemplateType(final String name)
    {
	return this.templateTypes.get(name);
    }

    public void registerTemplateType(final TemplateType tt)
    {
	this.templateTypes.put(tt.getName(), tt);
	try
	{
	    Configuration.save();
	}
	catch (IOException ioe)
	{
	    LOGGER.error(ioe);
	}
    }

    public TemplateType newTemplateType(final String name,
					final NodeRef schemaNodeRef)
    {
	return new TemplateTypeImpl(name, schemaNodeRef);
    }

    public Document newDocument()
    {
	try
	{
	    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    dbf.setNamespaceAware(true);
	    dbf.setValidating(false);
	    final DocumentBuilder db = dbf.newDocumentBuilder();
	    return db.newDocument();
	}
	catch (ParserConfigurationException pce)
	{
	assert false : pce;
	    LOGGER.error(pce);
	    return null;
	}
//	catch (SAXException saxe)
//	{
//	    LOGGER.error(saxe);
//	}
//	catch (IOException ioe)
//	{
//	    LOGGER.error(ioe);
//	}
    }

    public void writeXML(final Node n, final Writer output)
    {
	try 
	{
	    System.out.println("writing out a document for " + n.getNodeName() + 
			       " to " + output);
	    final TransformerFactory tf = TransformerFactory.newInstance();
	    final Transformer t = tf.newTransformer();
	    t.transform(new DOMSource(n), new StreamResult(output));
	}
	catch (TransformerException te)
        {
	    te.printStackTrace();
	assert false : te.getMessage();
	}
    }

    public void writeXML(final Node n, final File output)
	throws IOException
    {
	this.writeXML(n, new FileWriter(output));
    }

    public String writeXMLToString(final Node n)
    {
	final StringWriter result = new StringWriter();
	this.writeXML(n, result);
	return result.toString();
    }

    public Document parseXML(final String source)
	throws ParserConfigurationException,
	SAXException,
	IOException
    {
	return this.parseXML(new ByteArrayInputStream(source.getBytes()));
    }

    public Document parseXML(final NodeRef nodeRef)
	throws ParserConfigurationException,
	SAXException,
	IOException
    {
	final ContentReader contentReader = 
	    this.contentService.getReader(nodeRef, ContentModel.TYPE_CONTENT);
	final InputStream in = contentReader.getContentInputStream();
	return this.parseXML(in);
    }

    public Document parseXML(final File source)
	throws ParserConfigurationException,
	SAXException,
	IOException
    {
	return this.parseXML(new FileInputStream(source));
    }

    public Document parseXML(final InputStream source)
	throws ParserConfigurationException,
	SAXException,
	IOException
    {
	final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	dbf.setNamespaceAware(true);
	dbf.setValidating(false);
	final DocumentBuilder db = dbf.newDocumentBuilder();
	final Document result = db.parse(source);
	source.close();
	return result;
    }
}
