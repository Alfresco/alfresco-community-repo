package org.alfresco.service.cmr.view;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Importer / Exporter Location
 * 
 * @author David Caruana
 */
public class Location
{
    private StoreRef storeRef = null;
    private NodeRef nodeRef = null;
    private NodeRef[] nodeRefs = null;
    private String path = null;
    private QName childAssocType = null;
    

    /**
     * Construct
     * 
     * @param nodeRef NodeRef
     */
    public Location(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("Node Ref", nodeRef);
        this.storeRef = nodeRef.getStoreRef();
        this.nodeRef = nodeRef;
    }

    /**
     * Construct
     * 
     * @param nodeRefs NodeRef[]
     */
    public Location(NodeRef[] nodeRefs)
    {
        ParameterCheck.mandatory("Node Refs", nodeRefs);
        this.storeRef = nodeRefs[0].getStoreRef();
        this.nodeRefs = nodeRefs;
    }


    /**
     * Construct
     * 
     * @param storeRef StoreRef
     */
    public Location(StoreRef storeRef)
    {
        ParameterCheck.mandatory("Store Ref", storeRef);
        this.storeRef = storeRef;
    }

    /**
     * @return  the store ref
     */
    public StoreRef getStoreRef()
    {
        return storeRef;
    }
    
    /**
     * @return  the node ref
     */
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }
    
    /**
     * @return  the node refs
     */
    public NodeRef[] getNodeRefs()
    {
        return nodeRefs;
    }
    
    public void setNodeRefs(NodeRef[] nodeRefs)
    {
        this.nodeRef = null;
        this.nodeRefs = nodeRefs;   
    }
    
    /**
     * Sets the location to the specified path
     *  
     * @param path  path relative to store or node reference
     */
    public void setPath(String path)
    {
        this.path = path;
    }
    
    /**
     * @return  the location
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Sets the child association type
     * 
     * @param childAssocType  child association type
     */
    public void setChildAssocType(QName childAssocType)
    {
        this.childAssocType = childAssocType;
    }
    
    /**
     * @return  the child association type
     */
    public QName getChildAssocType()
    {
        return childAssocType;
    }
}
