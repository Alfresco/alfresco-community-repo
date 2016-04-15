package org.alfresco.repo.transfer.requisite;

import org.alfresco.repo.transfer.DeltaList;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.xml.sax.SAXException;

/**
 * A processor of the XML Transfer Requsite file to populate a DeltaList object
 * 
 * The requsite is parsed once and the delta list is available from getDeltaList at the end.
 * 
 * @author mrogers
 *
 */
public class DeltaListRequsiteProcessor implements TransferRequsiteProcessor
{

    DeltaList deltaList = null;
    
    public void missingContent(NodeRef node, QName qname, String name)
    {
        deltaList.getRequiredParts().add(name);
    }
    
    public void startTransferRequsite()
    {
        deltaList = new DeltaList();
    }
    
    public void endTransferRequsite()
    {
        // No op
    }
    
    /**
     * Get the delta list
     * @return the delta list or null if the XML provided does not contain the data.
     */
    public DeltaList getDeltaList()
    {
        return deltaList;
    }
    
}
