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

public class TemplatingService
{
    private final static TemplatingService INSTANCE = new TemplatingService();

    private ArrayList<TemplateType> templateTypes = 
	new ArrayList<TemplateType>();

    private TemplatingService()
    {
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
    }

    public TemplateType newTemplateType(final String name,
					final Document schema)
    {
	return new TemplateTypeImpl(name, schema);
    }

    public Document newDocument()
	throws ParserConfigurationException,
	       SAXException,
	       IOException
    {
	final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	dbf.setNamespaceAware(true);
	dbf.setValidating(false);
	final DocumentBuilder db = dbf.newDocumentBuilder();
	return db.newDocument();
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

    public Document parseXML(final String source)
	throws ParserConfigurationException,
	       SAXException,
	       IOException
    {
	return this.parseXML(new ByteArrayInputStream(source.getBytes()));
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
