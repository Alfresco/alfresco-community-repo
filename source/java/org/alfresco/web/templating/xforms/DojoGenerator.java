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

import org.chiba.tools.xslt.UIGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import org.chiba.xml.xforms.exception.XFormsException;

public class DojoGenerator
    implements UIGenerator
{
    
    private Node inputNode;
    private final HashMap parameters = new HashMap();
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    
    public DojoGenerator(HttpServletRequest request,
			 HttpServletResponse response)
    {
	this.request = request;
	this.response = response;
    }
    
    public void setInputNode(final Node inputNode)
    {
	this.inputNode = inputNode;
    }

    public void setParameter(final String key, final Object value) 
    {
	this.parameters.put(key, value);
    }

    public void setOutput(Object output)
    {
	//	this.output = output;
    }

    public void generate()
	throws XFormsException
    {
	try 
        {
	    request.setAttribute("xform", this.inputNode);
	    final RequestDispatcher rd = request.getRequestDispatcher("/jsp/content/xforms/dojo-generator.jsp");
	    rd.include(request, response);
	}
	catch (Exception e)
        {
	    throw new XFormsException(e);
	}
    }
}
	       