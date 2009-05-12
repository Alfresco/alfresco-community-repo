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

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.element.ConfigElementAdapter;

public class ImapConfigElement extends ConfigElementAdapter
{
    private static final long serialVersionUID = -6911139959296875159L;

    public static final String CONFIG_ELEMENT_ID = "imapConfig";

    private Map<String, ImapConfig> imapConfigs = new LinkedHashMap<String, ImapConfig>(8, 10f);

    public ImapConfigElement()
    {
        super(CONFIG_ELEMENT_ID);
    }

    public ImapConfigElement(String name)
    {
        super(name);
    }

    @Override
    public ConfigElement combine(ConfigElement configElement)
    {
        ImapConfigElement combined = new ImapConfigElement();

        // add all the imapConfigs from this element
        for (ImapConfig imapConfig : getImapConfigs().values())
        {
            combined.addImapConfig(imapConfig);
        }

        // add all the imapConfigs from the given element
        for (ImapConfig imapConfig : ((ImapConfigElement) configElement).getImapConfigs().values())
        {
            combined.addImapConfig(imapConfig);
        }

        return combined;
    }

    public Map<String, ImapConfig> getImapConfigs()
    {
        return imapConfigs;
    }

    public ImapConfig getImapConfig(String name)
    {
        return imapConfigs.get(name);
    }

    void addImapConfig(ImapConfig imapConfig)
    {
        imapConfigs.put(imapConfig.getName(), imapConfig);
    }

    public static class ImapConfig implements Serializable
    {
        private static final long serialVersionUID = 424330549937129149L;

        private String name;
        private String mode;
        private String store;
        private String rootPath;

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getMode()
        {
            return mode;
        }

        public void setMode(String mode)
        {
            this.mode = mode;
        }

        public String getStore()
        {
            return store;
        }

        public void setStore(String store)
        {
            this.store = store;
        }

        public String getRootPath()
        {
            return rootPath;
        }

        public void setRootPath(String rootPath)
        {
            this.rootPath = rootPath;
        }

        public static long getSerialVersionUID()
        {
            return serialVersionUID;
        }

    }

}
