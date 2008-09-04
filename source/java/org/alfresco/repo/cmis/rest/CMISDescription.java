/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.rest;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.web.scripts.DescriptionExtension;
import org.alfresco.web.scripts.WebScriptException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


/**
 * Web Script Descriptor Extension
 * 
 * <cmis version="x"/>
 * 
 * @author davidc
 */
public class CMISDescription implements DescriptionExtension
{

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.DescriptionExtension#parseExtensions(java.lang.String, java.io.InputStream)
     */
    public Map<String, Serializable> parseExtensions(String serviceDescPath, InputStream serviceDesc)
    {
        SAXReader reader = new SAXReader();
        try
        {
            Map<String, Serializable> extensions = null;
            Document document = reader.read(serviceDesc);
            Element rootElement = document.getRootElement();
            Element cmisElement = rootElement.element("cmis");
            if (cmisElement != null)
            {
                extensions = new HashMap<String, Serializable>();
                String version = cmisElement.attributeValue("version");
                if (version == null || version.length() == 0)
                {
                    throw new WebScriptException("Expected 'version' attribute on <cmis> element");
                }
                extensions.put("cmis_version", version);
            }
            return extensions;
        }
        catch(DocumentException e)
        {
            throw new WebScriptException("Failed to parse web script description document " + serviceDescPath, e);
        }
    }

}
