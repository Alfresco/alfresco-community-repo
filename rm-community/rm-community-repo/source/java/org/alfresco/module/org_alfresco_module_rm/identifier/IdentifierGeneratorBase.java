 
package org.alfresco.module.org_alfresco_module_rm.identifier;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * @author Roy Wetherall
 */
public abstract class IdentifierGeneratorBase implements IdentifierGenerator
{
    /** Identifier service */
    private IdentifierService identifierService;
    
    /** Node service */
    protected NodeService nodeService;
    
    /** Content type */
    private QName type;    
    
    /**
     * Initialisation method
     */
    public void init()
    {
        identifierService.register(this);
    }
    
    /**
     * Set identifier service.
     * 
     * @param identifierService     identifier service
     */
    public void setIdentifierService(IdentifierService identifierService)
    {
        this.identifierService = identifierService;
    }
    
    /**
     * Set the node service
     * 
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set type.
     * 
     * @param type  content type
     */
    public void setTypeAsString(String type)
    {
        this.type = QName.createQName(type);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierGenerator#getType()
     */
    @Override
    public QName getType()
    {
        return type;
    }
    
    /**
     * Function to pad a string with zero '0' characters to the required length
     * 
     * @param s String to pad with leading zero '0' characters
     * @param len Length to pad to
     * @return padded string or the original if already at >=len characters
     */
    protected String padString(String s, int len)
    {
        String result = s;

        for (int i = 0; i < (len - s.length()); i++)
        {
            result = "0" + result;
        }

        return result;
    }
}
