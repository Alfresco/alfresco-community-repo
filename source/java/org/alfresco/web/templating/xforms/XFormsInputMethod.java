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
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.alfresco.util.TempFileProvider;
import org.alfresco.web.templating.*;
import org.alfresco.web.templating.xforms.schemabuilder.*;
import org.chiba.xml.util.DOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XFormsInputMethod
    implements TemplateInputMethod
{
    private static final Log LOGGER = LogFactory.getLog(XFormsInputMethod.class); 

    public XFormsInputMethod()
    {
    }

    public String getInputURL(final Document xmlContent, final TemplateType tt)
    {
	try
	{
	    final Document xform = this.getXForm(xmlContent, tt);
	    final String id = getDocumentElementNameNoNS(xmlContent);
	    //	    this.saveInChiba(id, xform);
	    final File xformFile = TempFileProvider.createTempFile("alfresco", ".xform");
	    final TemplatingService ts = TemplatingService.getInstance();
	    ts.writeXML(xform, xformFile);
	    final FacesContext fc = FacesContext.getCurrentInstance();
	    final String cp =
		fc.getExternalContext().getRequestContextPath();
	    return cp + "/XFormsServlet?form=" + xformFile.toURI().toString();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    return null;
	}
    }

    public String getSchemaInputURL(final TemplateType tt)
    {
	try
	{
//	    final Document xform = this.getXFormForSchema(tt);
//	    final File xformFile = TempFileProvider.createTempFile("alfresco", ".xform");
//	    final TemplatingService ts = TemplatingService.getInstance();
//	    ts.writeXML(tt.getSchema(), xformFile);
//	    final FacesContext fc = FacesContext.getCurrentInstance();
//	    final String cp =
//		fc.getExternalContext().getRequestContextPath();
//	    return cp + "/XFormsServlet?form=" + xformFile.toURI().toString();
	    return null;
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    return null;
	}
    }

    private static String getDocumentElementNameNoNS(final Document d)
    {
	final Node n = d.getDocumentElement();
	String name = n.getNodeName();
	return name;
//	String prefix = n.getPrefix();
//	String namespace = n.getNamespaceURI();
//	System.out.println("name " + name + " prefix " + prefix + " ns uri " + namespace);
//	return name.replaceAll(".+\\:", "");
    }

//    public Document getXFormForSchema(final TemplateType tt)
//	throws FormBuilderException
//    {
//	final TemplatingService ts = TemplatingService.getInstance();
//
//	final File schemaFile = TempFileProvider.createTempFile("alfresco", ".schema");
//	ts.writeXML(tt.getSchema(), schemaFile);
//	final FacesContext fc = FacesContext.getCurrentInstance();
//	final String cp =
//	    fc.getExternalContext().getRequestContextPath();
//
//	final SchemaFormBuilder builder = 
//	    new BaseSchemaFormBuilder("schema",
//				      schemaFile.toURI().toString(),
//				      cp + "/jsp/content/xforms/form/debug-instance.jsp",
//				      "post",
//				      new XHTMLWrapperElementsBuilder(),
//				      null,
//				      null,
//				      true);
//	System.out.println("building xform for schema " + schemaFile.getPath());
//	final Document result = builder.buildForm("/Users/arielb/Documents/alfresco/xsd/XMLSchema.xsd");
//	//	xmlContentFile.delete();
//	//	schemaFile.delete();
//	return result;
//    }
 
    public Document getXForm(final Document xmlContent, final TemplateType tt) 
	throws FormBuilderException
    {
	final TemplatingService ts = TemplatingService.getInstance();
	final File schemaFile = TempFileProvider.createTempFile("alfresco", ".schema");
	try
        {
	    ts.writeXML(tt.getSchema(), schemaFile);
	}
	catch (IOException ioe)
        {
	    assert false : ioe.getMessage();
	    LOGGER.error(ioe);
	}
	final FacesContext fc = FacesContext.getCurrentInstance();
	final String cp =
	    fc.getExternalContext().getRequestContextPath();
	
	final SchemaFormBuilder builder = 
	    new BaseSchemaFormBuilder(getDocumentElementNameNoNS(xmlContent),
				      xmlContent,
				      "http://localhost:8080" + cp + "/jsp/content/xforms/debug-instance.jsp",
				      "post",
				      new XHTMLWrapperElementsBuilder(),
				      null,
				      null,
				      true);
	System.out.println("building xform for schema " + schemaFile.getPath());
	final Document result = builder.buildForm(schemaFile.getPath());
	//	xmlContentFile.delete();
	//	schemaFile.delete();
	return result;
    }

//    private void saveInChiba(final String fileName, final Document d)
//	throws IOException
//    {
//	final ServletContext myContext = (ServletContext)
//	    FacesContext.getCurrentInstance().getExternalContext().getContext();
//	final ServletContext chiba = myContext.getContext("/chiba");
//	final File outputFile = new File(new File(chiba.getRealPath("/forms")),
//					 fileName + ".xhtml");
//	TemplatingService.getInstance().writeXML(d.getDocumentElement(), outputFile);
//    }
}
