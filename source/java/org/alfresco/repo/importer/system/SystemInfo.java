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
package org.alfresco.repo.importer.system;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;


/**
 * Root data holder of Repository system information to be exported and imported
 * 
 * @author davidc
 */
public class SystemInfo
{
    public List<PatchInfo> patches = new ArrayList<PatchInfo>();
    public List<VersionCounterInfo> versionCounters = new ArrayList<VersionCounterInfo>();

    /**
     * Create System Info from XML representation
     *  
     * @param xml  xml representation of system info
     * @return  the System Info
     */
    public static SystemInfo createSystemInfo(InputStream xml)
    {
        try
        {
            IBindingFactory factory = BindingDirectory.getFactory(SystemInfo.class);
            IUnmarshallingContext context = factory.createUnmarshallingContext();
            Object obj = context.unmarshalDocument(xml, null);
            return (SystemInfo)obj;
        }
        catch(JiBXException e)
        {
            throw new DictionaryException("Failed to parse System Info", e);
        }
    }

    /**
     * Create XML representation of System Info
     * 
     * @param xml  xml representation of system info
     */
    public void toXML(OutputStream xml)
    {
        try
        {
            IBindingFactory factory = BindingDirectory.getFactory(SystemInfo.class);
            IMarshallingContext context = factory.createMarshallingContext();
            context.setIndent(4);
            context.marshalDocument(this, "UTF-8", null, xml);    
        }
        catch(JiBXException e)
        {
            throw new DictionaryException("Failed to create System Info", e);
        }
    }
    
}


