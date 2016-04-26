package org.alfresco.repo.transfer.requisite;

import java.io.Writer;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.xml.sax.SAXException;

/**
 * Transfer Requsite Writer
 * 
 * This class formats the transfer requsite and prints it to the specified writer
 * 
 * It is a statefull object and writes one requsite at a time.
 *  
 */
public interface TransferRequsiteWriter
{
    
    /**
     * 
     */
    void startTransferRequsite() ;
       
    /**
     * 
     */
    void endTransferRequsite() ;
    
    /**
     * 
     */
    void missingContent(NodeRef nodeRef, QName qName, String name);
}
