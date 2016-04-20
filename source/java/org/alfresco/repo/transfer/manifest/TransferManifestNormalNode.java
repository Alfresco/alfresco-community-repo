package org.alfresco.repo.transfer.manifest;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;

/**
 * Data value object - part of the transfer manifest
 *
 * Represents a single node and either a create or an update.
 *
 * @author Mark Rogers
 */
public class TransferManifestNormalNode implements TransferManifestNode
{
    private NodeRef nodeRef;
    private ChildAssociationRef primaryParentAssoc;
    private String uuid;
    private QName type;
    private QName ancestorType;
    private Map<QName,Serializable> properties;
    private Set<QName> aspects;
    private List<ChildAssociationRef> childAssocs;
    private List<ChildAssociationRef> parentAssocs;
    private List<AssociationRef> sourceAssocs;
    private List<AssociationRef> targetAssocs;
    private Path parentPath;
    private ManifestAccessControl accessControl;
    
    // NodeRef is noderef of type d:category  ManifestCategory provides the extra meta-data
    private Map<NodeRef, ManifestCategory> categories;

    public void setNodeRef(NodeRef nodeRef)
    {
        this.nodeRef = nodeRef;
    }

    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    public String getUuid()
    {
        return uuid;
    }

    /**
     * Gets all properties for the node
     *
     * @return the properties
     */
    public Map<QName,Serializable> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<QName,Serializable> properties)
    {
        this.properties = properties;
    }

    public void setAspects(Set<QName> aspects)
    {
        this.aspects = aspects;
    }

    public Set<QName> getAspects()
    {
        return aspects;
    }

    public void setType(QName type)
    {
        this.type = type;
        if (ancestorType == null)
        {
            ancestorType = type;
        }
    }

    public QName getType()
    {
        return type;
    }

    public void setChildAssocs(List<ChildAssociationRef> childAssocs)
    {
        this.childAssocs = childAssocs;
    }

    public List<ChildAssociationRef> getChildAssocs()
    {
        return childAssocs;
    }

    public void setParentAssocs(List<ChildAssociationRef> parentAssocs)
    {
        this.parentAssocs = parentAssocs;
    }

    public List<ChildAssociationRef> getParentAssocs()
    {
        return parentAssocs;
    }

    public void setParentPath(Path parentPath)
    {
        this.parentPath = parentPath;
    }

    public Path getParentPath()
    {
        return parentPath;
    }

    public void setSourceAssocs(List<AssociationRef> sourceAssocs)
    {
        this.sourceAssocs = sourceAssocs;
    }

    public List<AssociationRef> getSourceAssocs()
    {
        return sourceAssocs;
    }

    public void setTargetAssocs(List<AssociationRef> targetAssocs)
    {
        this.targetAssocs = targetAssocs;
    }

    public List<AssociationRef> getTargetAssocs()
    {
        return targetAssocs;
    }

    public void setPrimaryParentAssoc(ChildAssociationRef primaryParentAssoc)
    {
        this.primaryParentAssoc = primaryParentAssoc;
    }

    public ChildAssociationRef getPrimaryParentAssoc()
    {
        return primaryParentAssoc;
    }

    public void setAccessControl(ManifestAccessControl accessControl)
    {
        this.accessControl = accessControl;
    }

    public ManifestAccessControl getAccessControl()
    {
        return accessControl;
    }

    public QName getAncestorType()
    {
        return ancestorType;
    }

    public void setAncestorType(QName ancestorType)
    {
        this.ancestorType = ancestorType;
    }
    
    public void setManifestCategories(Map<NodeRef, ManifestCategory> categories)
    {
    	this.categories = categories;
    }
    
    public Map<NodeRef, ManifestCategory> getManifestCategories()
    {
    	return this.categories;
    }
    
}
