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

/**
 * Manifest Processor
 * 
 * Interface called when parsing the transfer manifest file
 * 
 * When Parsing the manifest file, the startTransferManifest will be called first, then 
 * processHeader, then mulpiple calls of processTransferManifestNode, one for each node,
 * then endTransferManifest
 *
 * @author Mark Rogers
 */
public interface TransferManifestProcessor
{
    /**
     * Signals the start of a transfer manifest
     */
    public void startTransferManifest();
    
    /**
     * Gives the header to be proceessed
     * @param header, the header
     */
    public void processTransferManifiestHeader(TransferManifestHeader header);
        
    /**
     * Gives a manifest node to be processed
     * @param node, the node
     */
    public void processTransferManifestNode(TransferManifestNormalNode node); 
    
    /**
     * Gives a deleted manifest node to be processed
     * @param node, the node
     */
    public void processTransferManifestNode(TransferManifestDeletedNode node); 
    
    /**
     * Signals the end of a transfer manifest
     */
    public void endTransferManifest();
    
}
