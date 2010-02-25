/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.transfer.manifest;

import java.io.Writer;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

/**
 * Transfer Manifest Writer
 * 
 * This class formats the transfer manifest and prints it to the specified writer
 * 
 * It is a statefull object and writes one manifest at a time.
 * 
 * Call start once, then write the header, then one or more nodes, then end.
 * 
 */
public interface TransferManifestWriter
{
    /**
     * 
     * @param writer
     * @throws SAXException
     */
    void startTransferManifest(Writer writer)  throws SAXException;
    
    /**
     * 
     * @param header
     * @throws SAXException
     */
    void writeTransferManifestHeader(TransferManifestHeader header)  throws SAXException;
    
    /**
     * 
     * @param node
     * @throws SAXException
     */
    void writeTransferManifestNode(TransferManifestNode node)  throws SAXException;
        
    /**
     * 
     * @throws SAXException
     */
    void endTransferManifest() throws SAXException;
}
