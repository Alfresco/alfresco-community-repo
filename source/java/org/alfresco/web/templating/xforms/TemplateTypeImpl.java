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

import org.alfresco.util.TempFileProvider;
import org.alfresco.web.templating.*;
import org.alfresco.web.templating.xforms.schemabuilder.FormBuilderException;

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.xsd2inst.SampleXmlUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class TemplateTypeImpl 
    implements TemplateType
{

    private final Document schema;
    private final String name;
    private final LinkedList outputMethods = new LinkedList();

    public TemplateTypeImpl(final String name,
			    final Document schema) 
    {
	this.name = name;
	this.schema = schema;
    }

    public String getName()
    {
	return this.name;
    }

    public Document getSchema()
    {
	return this.schema;
    }

    public Document getSampleXml(final String rootTagName) 
    {
	XmlOptions xmlOptions = new XmlOptions();
	xmlOptions = xmlOptions.setLoadLineNumbers().setLoadMessageDigest();
	final XmlObject[] schemas = new XmlObject[1];
	try
	{
	    final File schemaFile = TempFileProvider.createTempFile("alfresco", ".schema");
	    TemplatingService.getInstance().writeXML(this.schema, schemaFile);
	    schemas[0] = XmlObject.Factory.parse(schemaFile, xmlOptions);
	    schemaFile.delete();
	}
	catch (Exception e)
	{
	    System.err.println("Can not load schema file: " + schema + ": ");
	    e.printStackTrace();
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
	    xmle.printStackTrace();
	    return null;
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
        
	final String result = SampleXmlUtil.createSampleForType(elem);
	try
	{
	    final TemplatingService ts = TemplatingService.getInstance();
	    return ts.parseXML(new ByteArrayInputStream(result.getBytes()));
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

    public List<TemplateInputMethod> getInputMethods()
    {
	return (List<TemplateInputMethod>)Arrays.asList(new TemplateInputMethod[] {
		new XFormsInputMethod()
	    });
    }

    public void addOutputMethod(TemplateOutputMethod output)
    {
	this.outputMethods.add(output);
    }

    public List<TemplateOutputMethod> getOutputMethods()
    {
	return this.outputMethods;
    }
}