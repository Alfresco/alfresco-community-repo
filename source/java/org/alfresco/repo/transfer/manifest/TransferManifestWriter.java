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
