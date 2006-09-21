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

import java.io.*;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import org.alfresco.service.cmr.repository.NodeRef;

import org.alfresco.util.TempFileProvider;
import org.alfresco.web.templating.*;
import org.alfresco.web.templating.xforms.schemabuilder.FormBuilderException;

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.xsd2inst.SampleXmlUtil;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.alfresco.model.ContentModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TemplateTypeImpl 
    implements TemplateType
{
    private static final Log LOGGER = LogFactory.getLog(TemplateTypeImpl.class);

    private transient Document schema;
    private final NodeRef schemaNodeRef;
    private final String name;
    private final LinkedList<TemplateOutputMethod> outputMethods = 
	new LinkedList<TemplateOutputMethod>();
    private final static LinkedList<TemplateInputMethod> INPUT_METHODS = 
	new LinkedList<TemplateInputMethod>();

    static 
    {
	INPUT_METHODS.add(new XFormsInputMethod());
    }
    
    public TemplateTypeImpl(final String name,
			    final NodeRef schemaNodeRef) 
    {
	this.name = name;
	this.schemaNodeRef = schemaNodeRef;
    }

    public String getName()
    {
	return this.name;
    }

    public Document getSchema()
    {
	if (this.schema == null)
	{
	    final TemplatingService ts = TemplatingService.getInstance();
	    try
	    {
		this.schema = ts.parseXML(this.schemaNodeRef);
	    }
	    catch (Exception e)
	    {
		LOGGER.error(e);
	    }
	}
	return this.schema;
    }

    public Document getSampleXml(final String rootTagName) 
    {
	XmlOptions xmlOptions = new XmlOptions();
	xmlOptions = xmlOptions.setLoadLineNumbers().setLoadMessageDigest();
	final XmlObject[] schemas = new XmlObject[1];
	try
	{
	    schemas[0] = XmlObject.Factory.parse(this.getSchema(), xmlOptions);
	}
	catch (XmlException xmle)
	{
	    LOGGER.error(xmle);
	}

	final XmlOptions compileOptions = new XmlOptions();
	compileOptions.setCompileDownloadUrls();
	compileOptions.setCompileNoPvrRule();
	compileOptions.setCompileNoUpaRule();

	SchemaTypeSystem sts = null;
	try
	{
	    sts = XmlBeans.compileXsd(schemas, 
				      XmlBeans.getBuiltinTypeSystem(), 
				      compileOptions);
	}
	catch (XmlException xmle)
        {
	    LOGGER.error(xmle);
	}
	    
	if (sts == null)
	{
	    throw new NullPointerException("No Schemas to process.");
	}
	final SchemaType[] globalElems = sts.documentTypes();
	SchemaType elem = null;
	for (int i = 0; i < globalElems.length; i++)
	{
	    if (rootTagName.equals(globalElems[i].getDocumentElementName().getLocalPart()))
	    {
		elem = globalElems[i];
		break;
	    }
	}

	if (elem == null)
	    throw new NullPointerException("Could not find a global element with name \"" + rootTagName + "\"");
        
	final String xmlString = SampleXmlUtil.createSampleForType(elem);
	try
	{
	    final TemplatingService ts = TemplatingService.getInstance();
	    final Document d = ts.parseXML(new ByteArrayInputStream(xmlString.getBytes()));
	    LOGGER.debug("sample xml:");
	    LOGGER.debug(ts.writeXMLToString(d));

	    TemplateTypeImpl.cleanUpSampleXml(d.getDocumentElement());
	    LOGGER.debug("cleaned up xml:");
	    LOGGER.debug(ts.writeXMLToString(d));
	    return d;
	}
	catch (ParserConfigurationException pce)
	{
	    assert false : pce.getMessage();
	    return null;
	}
	catch (SAXException saxe)
        {
	    assert false : saxe.getMessage();
	    return null;
	}
	catch (IOException ioe)
	{
	    assert false : ioe.getMessage();
	    return null;
	}
    }

    private static void cleanUpSampleXml(final Node n)
    {
	if (n instanceof CharacterData)
	{
	    //	    System.out.println("replacing data " + ((CharacterData)n).getData());
	    ((CharacterData)n).setData(" ");
	}
	else if (n instanceof Element)
	{
	    final NamedNodeMap attrs = n.getAttributes();
	    for (int i = 0; i < attrs.getLength(); i++)
	    {
		//		System.out.println("not replacing data " + ((Attr)n).getValue());
		//		((Attr)attrs.item(i)).setValue("");
	    }
	}
	final NodeList nl = n.getChildNodes();
	for (int i = 0; i < nl.getLength(); i++)
        {
	    TemplateTypeImpl.cleanUpSampleXml(nl.item(i));
	}
    }

    public List<TemplateInputMethod> getInputMethods()
    {
	return INPUT_METHODS;
    }

    public void addOutputMethod(TemplateOutputMethod output)
    {
	this.outputMethods.add(output);
    }

    public List<TemplateOutputMethod> getOutputMethods()
    {
	return this.outputMethods;
    }

    public int hashCode() 
    {
	return this.getName().hashCode();
    }
}