/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.imap.config;

import java.util.Iterator;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.xml.elementreader.ConfigElementReader;
import org.alfresco.repo.imap.config.ImapConfigElement.ImapConfig;
import org.dom4j.Element;

public class ImapElementReader implements ConfigElementReader
{

    private static final String ELEMENT_IMAP_CONFIG = "imapConfig";
    private static final String ELEMENT_IMAP = "imap";
    private static final String ELEMENT_STORE = "store";
    private static final String ELEMENT_ROOTPATH = "rootPath";

    private static final String ATTR_NAME = "name";
    private static final String ATTR_MODE = "mode";

    public ConfigElement parse(Element element)
    {
        ImapConfigElement configElement = null;

        if (element != null)
        {
            String elementName = element.getName();
            if (elementName.equals(ELEMENT_IMAP_CONFIG) == false)
            {
                throw new ConfigException("ImapElementReader can parse '" + ELEMENT_IMAP_CONFIG + "' elements only, the element passed is '" + elementName + "'");
            }

            configElement = new ImapConfigElement();

            for (Iterator<Element> items = element.elementIterator(ELEMENT_IMAP); items.hasNext();)
            {
                Element item = items.next();

                String name = item.attributeValue(ATTR_NAME);
                String mode = item.attributeValue(ATTR_MODE);
                String store = item.element(ELEMENT_STORE).getStringValue();
                String rootPath = item.element(ELEMENT_ROOTPATH).getStringValue();

                ImapConfig imapConfig = new ImapConfig();
                imapConfig.setName(name);
                imapConfig.setMode(mode);
                imapConfig.setStore(store);
                imapConfig.setRootPath(rootPath);

                configElement.addImapConfig(imapConfig);
            }
        }
        return configElement;
    }

}
