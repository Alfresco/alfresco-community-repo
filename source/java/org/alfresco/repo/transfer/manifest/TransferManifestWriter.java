/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.transfer.manifest;

import java.io.Writer;

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
