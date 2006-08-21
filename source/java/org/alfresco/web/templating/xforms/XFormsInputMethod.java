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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;

import org.alfresco.web.templating.*;
import org.alfresco.web.templating.xforms.schemabuilder.*;
import org.alfresco.web.bean.ajax.XFormsBean;
import org.chiba.xml.util.DOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.alfresco.web.app.servlet.FacesHelper;
import org.chiba.xml.xforms.exception.XFormsException;

public class XFormsInputMethod
    implements TemplateInputMethod
{
    private static final Log LOGGER = LogFactory.getLog(XFormsInputMethod.class); 

    public XFormsInputMethod()
    {
    }

    public void generate(final InstanceData instanceData, 
			 final TemplateType tt,
			 final Writer out)
    {
	final TemplatingService ts = TemplatingService.getInstance();
	final FacesContext fc = FacesContext.getCurrentInstance();
	final XFormsBean xforms = (XFormsBean)
	    FacesHelper.getManagedBean(fc, "XFormsBean");
	xforms.setInstanceData(instanceData);
	xforms.setTemplateType(tt);
	try
        {
	    xforms.init();
	}
	catch (XFormsException xfe)
        {
	    LOGGER.error(xfe);
	}
 
	final String cp = fc.getExternalContext().getRequestContextPath();

	final Document result = ts.newDocument();
	final Element div = result.createElement("div");
	div.setAttribute("id", "alf-ui");
	div.setAttribute("style", "width: 100%; border: solid 1px orange;");
	result.appendChild(div);

	Element e = result.createElement("script");
	e.appendChild(result.createTextNode("djConfig = { isDebug: true };\n" +
					    "var WEBAPP_CONTEXT = \"" + cp + "\";\n"));
	div.appendChild(e);
	e = result.createElement("script");
	e.setAttribute("type", "text/javascript");
	e.setAttribute("src", cp + "/scripts/ajax/dojo.js");
	e.appendChild(result.createTextNode("\n"));
	div.appendChild(e);
	e = result.createElement("script");
	e.setAttribute("type", "text/javascript");
	e.setAttribute("src", cp + "/scripts/ajax/xforms.js");
	e.appendChild(result.createTextNode("\n"));
	div.appendChild(e);
	ts.writeXML(result, out);
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
 
    public Document getXForm(Document xmlContent, final TemplateType tt) 
	throws FormBuilderException
    {
	if (xmlContent == null)
	    xmlContent = tt.getSampleXml(tt.getName());
	final TemplatingService ts = TemplatingService.getInstance();
	final FacesContext fc = FacesContext.getCurrentInstance();
	final HttpServletRequest request = (HttpServletRequest)
	    fc.getExternalContext().getRequest();
	final String baseUrl = (request.getScheme() + "://" + 
				request.getServerName() + ':' + 
				request.getServerPort());
	LOGGER.debug("using baseUrl " + baseUrl + " for schemaformbuilder");

	final SchemaFormBuilder builder = 
	    new BaseSchemaFormBuilder(tt.getName(),
				      xmlContent,
				      request.getContextPath() + "/ajax/invoke/XFormsBean.handleAction",
				      SchemaFormBuilder.SUBMIT_METHOD_POST,
				      new XHTMLWrapperElementsBuilder(),
				      null,
				      baseUrl,
				      true);
	LOGGER.debug("building xform for schema " + tt.getName());
	final Document result = builder.buildForm(tt); //schemaFile.getPath());
	//	xmlContentFile.delete();
	//	schemaFile.delete();
	return result;
    }
}
