package org.alfresco.repo.importer;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


/**
 * Description of parent for node to import.
 * 
 * @author David Caruana
 *
 */
public interface ImportParent
{
    /**
     * @return  the parent ref
     */    
    public NodeRef getParentRef();
    
    /**
     * @return  the child association type
     */
    public QName getAssocType();

    /**
     * Re-set parent reference
     * 
     * @param parentRef  parent reference
     */
    public void setParentRef(NodeRef parentRef);
    
    /**
     * Re-set association type
     * 
     * @param  assocType  association type
     */
    public void setAssocType(QName assocType);
    
}
