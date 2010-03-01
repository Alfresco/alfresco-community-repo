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
