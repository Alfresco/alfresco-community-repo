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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

public final class TemplatingService
    implements Serializable
{

    ////////////////////////////////////////////////////////////////////////////

    private static class Configuration
    {
	private final static File CONFIG_FILE  = 
	    new File(TempFileProvider.getTempDir(), "templating_configuration.xml");
	
	public static void load()
	    throws IOException
	{
	    if (!CONFIG_FILE.exists())
		return;
	    final TemplatingService ts = TemplatingService.getInstance();
	    final ObjectInputStream out = new ObjectInputStream(new FileInputStream(CONFIG_FILE));
	    try
	    {
		final List<TemplateType> tt = (List<TemplateType>)out.readObject();
		for (TemplateType t : tt)
		    {
			ts.registerTemplateType(t);
		    }
		out.close();
	    }
	    catch (ClassNotFoundException cnfe)
	    {
		assert false : cnfe;
		TemplatingService.LOGGER.error(cnfe);
	    }
	}
	
	public static void save()
	    throws IOException
	{
	    if (!CONFIG_FILE.exists())
		CONFIG_FILE.createNewFile();
	    final TemplatingService ts = TemplatingService.getInstance();
	    final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(CONFIG_FILE));
	    out.writeObject(ts.getTemplateTypes());
	    out.close();
	}
    }

    ////////////////////////////////////////////////////////////////////////////


    public static final org.alfresco.service.namespace.QName TT_QNAME = 
	org.alfresco.service.namespace.QName.createQName(org.alfresco.service.namespace.NamespaceService.CONTENT_MODEL_1_0_URI, "tt");

    private static final Log LOGGER = LogFactory.getLog(TemplatingService.class);
    private static TemplatingService INSTANCE;

    private ArrayList<TemplateType> templateTypes = 
	new ArrayList<TemplateType>();
    private final ContentService contentService;

    public TemplatingService(final ContentService contentService)
    {
	this.contentService = contentService;
	if (INSTANCE == null)
	{
	    INSTANCE = this;
	    try
	    {
		Configuration.load();
	    }
	    catch (IOException ioe)
	    {
		LOGGER.error(ioe);
	    }
	}
    }

    public static TemplatingService getInstance()
    {
	return TemplatingService.INSTANCE;
    }

    public List<TemplateType> getTemplateTypes()
    {
	return this.templateTypes;
    }

    public TemplateType getTemplateType(final String name)
    {
	final Iterator it = this.templateTypes.iterator();
	while (it.hasNext())
	{
	    final TemplateType tt = (TemplateType)it.next();
	    if (tt.getName().equals(name))
		return tt;
	}
	return null;
    }

    public void registerTemplateType(final TemplateType tt)
    {
	this.templateTypes.add(tt);
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
