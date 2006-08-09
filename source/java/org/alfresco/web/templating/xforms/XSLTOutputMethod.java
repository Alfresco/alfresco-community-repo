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
import org.alfresco.web.templating.*;
import org.chiba.xml.util.DOMUtil;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class XSLTOutputMethod
    implements TemplateOutputMethod
{

    private final File file;

    public XSLTOutputMethod(final File f)
    {
	this.file = f;
    }

    public void generate(final Document xmlContent,
			 final TemplateType tt,
			 final Writer out)
	throws ParserConfigurationException,
	       TransformerConfigurationException,
	       TransformerException,
	       SAXException,
	       IOException
    {
	TransformerFactory tf = TransformerFactory.newInstance();
	TemplatingService ts = TemplatingService.getInstance();
	DOMSource source = new DOMSource(ts.parseXML(this.file));
	final Templates templates = tf.newTemplates(source);
	final Transformer t = templates.newTransformer();
	final StreamResult result = new StreamResult(out);
	t.transform(new DOMSource(xmlContent), result);
    }
}
