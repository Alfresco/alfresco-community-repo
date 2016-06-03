
package org.alfresco.repo.publishing;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transfer.manifest.TransferManifestNormalNode;
import org.alfresco.service.cmr.publishing.NodeSnapshot;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;

/**
 * @author Brian
 * @author Nick Smith
 * @since 4.0
 */
public class NodeSnapshotTransferImpl implements NodeSnapshot
{
    private final TransferManifestNormalNode transferNode; 
    
    /**
     * @param transferNode TransferManifestNormalNode
     */
    public NodeSnapshotTransferImpl(TransferManifestNormalNode transferNode)
    {
        this.transferNode = transferNode;
    }

    public List<ChildAssociationRef> getAllParentAssocs()
    {
        if (transferNode == null)
        {
            return Collections.emptyList();
        }
        return transferNode.getParentAssocs();
    }

    /**
    * {@inheritDoc}
     */
    public Set<QName> getAspects()
    {
        if (transferNode == null)
        {
            return Collections.emptySet();
        }
        return transferNode.getAspects();
    }

    /**
     * {@inheritDoc}
      */
    public NodeRef getNodeRef()
    {
        if (transferNode == null)
        {
            return null;
        }
        return transferNode.getNodeRef();
    }

    /**
     * @return List<AssociationRef>
     */
    public List<AssociationRef> getOutboundPeerAssociations()
    {
        if (transferNode == null)
        {
            return Collections.emptyList();
        }
        return transferNode.getTargetAssocs();
    }

    /**
     * @return ChildAssociationRef
     */
    public ChildAssociationRef getPrimaryParentAssoc()
    {
        if (transferNode == null)
        {
            return null;
        }
        return transferNode.getPrimaryParentAssoc();
    }

    /**
     * @return Path
     */
    public Path getPrimaryPath()
    {
        if (transferNode == null)
        {
            return null;
        }
        return transferNode.getParentPath();
    }

    /**
     * {@inheritDoc}
     */
    public Map<QName, Serializable> getProperties()
    {
        if (transferNode == null)
        {
            return Collections.emptyMap();
        }
        return transferNode.getProperties();
    }

    /**
     * {@inheritDoc}
     */
    public QName getType()
    {
        if (transferNode == null)
        {
            return null;
        }
        return transferNode.getType();
    }

    /**
    * {@inheritDoc}
    */
    public String getVersion()
    {
        return (String) getProperties().get(ContentModel.PROP_VERSION_LABEL);
    }
    
    /**
     * @return the transferNode
     */
    public TransferManifestNormalNode getTransferNode()
    {
        return transferNode;
    }
}
