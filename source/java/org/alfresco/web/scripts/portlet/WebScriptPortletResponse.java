/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.scripts.portlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;

import org.alfresco.web.scripts.WebScriptRequest;
import org.alfresco.web.scripts.WebScriptResponse;
import org.alfresco.web.ui.common.Utils;


/**
 * JSR-168 Web Script Response
 * 
 * @author davidc
 */
public class WebScriptPortletResponse implements WebScriptResponse
{
    /** Portlet response */
    private RenderResponse res;
    
    
    /**
     * Construct
     * 
     * @param res
     */
    WebScriptPortletResponse(RenderResponse res)
    {
        this.res = res;
    }

    /**
     * Gets the Portlet Render Response
     * 
     * @return  render response
     */
    public RenderResponse getRenderResponse()
    {
        return res;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#setStatus(int)
     */
    public void setStatus(int status)
    {
    }
         
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#setContentType(java.lang.String)
     */
    public void setContentType(String contentType)
    {
        res.setContentType(contentType);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#reset()
     */
    public void reset()
    {
        try
        {
            res.reset();
        }
        catch(IllegalStateException e)
        {
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#getWriter()
     */
    public Writer getWriter() throws IOException
    {
        return res.getWriter();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#getOutputStream()
     */
    public OutputStream getOutputStream() throws IOException
    {
        return res.getPortletOutputStream();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#encodeScriptUrl(java.lang.String)
     */
    public String encodeScriptUrl(String url)
    {
        WebScriptRequest req = new WebScriptPortletRequest(null, url, null);
        PortletURL portletUrl = res.createActionURL();
        portletUrl.setParameter("scriptUrl", req.getServicePath());
        String[] parameterNames = req.getParameterNames();
        for (String parameterName : parameterNames)
        {
            portletUrl.setParameter("arg." + parameterName, req.getParameter(parameterName));
        }
        return portletUrl.toString();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#getEncodeScriptUrlFunction(java.lang.String)
     */
    public String getEncodeScriptUrlFunction(String name)
    {
        PortletURL portletUrl = res.createActionURL();
        
        String func = ENCODE_FUNCTION.replace("$name$", name);
        func = func.replace("$actionUrl$", portletUrl.toString());
        return Utils.encodeJavascript(func);
    }
    
    private static final String ENCODE_FUNCTION = 
            "{ $name$: function(url) {" + 
            " var out = \"$actionUrl$\";" + 
            " var argsIndex = url.indexOf(\"?\");" + 
            " if (argsIndex == -1)" + 
            " {" + 
            "    out += \"&scriptUrl=\" + escape(url);" + 
            " }" + 
            " else" + 
            " {" + 
            "    out += \"&scriptUrl=\" + escape(url.substring(0, argsIndex));" + 
            "    var args = url.substring(argsIndex + 1).split(\"&\");" + 
            "    for (var i=0; i<args.length; i++)" + 
            "    {" + 
            "       out += \"arg.\" + args[i];" + 
            "    }" + 
            " }" + 
            " return out; } }"; 
}
