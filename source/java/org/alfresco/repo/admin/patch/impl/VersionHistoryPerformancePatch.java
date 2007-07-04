package org.alfresco.repo.admin.patch.impl;

import java.util.List;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;

public class VersionHistoryPerformancePatch  extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.versionHistoryPerformance.result";
    
    private VersionService versionService;
    
    public void setVersionService(VersionService versionService)
    {
        this.versionService = versionService;
    }

    @Override
    protected String applyInternal() throws Exception
    {
        // Set the aspect on the root node of the version store
        StoreRef versionStoreRef = this.versionService.getVersionStoreReference();
        NodeRef rootNodeRef = this.nodeService.getRootNode(versionStoreRef);
        this.nodeService.addAspect(rootNodeRef, VersionModel.ASPECT_VERSION_STORE_ROOT, null);
        
        List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(rootNodeRef);
        int updateCount = 0;

        for(ChildAssociationRef childAssocRef : assocs)
        {
            NodeRef nodeRef = childAssocRef.getChildRef();
            if (VersionModel.TYPE_QNAME_VERSION_HISTORY.equals(this.nodeService.getType(nodeRef)) == true)
            {            
                // Get the id
                String versionedNodeId = (String)this.nodeService.getProperty(nodeRef, VersionModel.PROP_QNAME_VERSIONED_NODE_ID);
                
                if (versionedNodeId != null)
                {
                	// Set the cm:name
                	this.nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, versionedNodeId);
                
                	// Move the node
                	this.nodeService.moveNode(  nodeRef, 
                								rootNodeRef, 
                								VersionModel.CHILD_QNAME_VERSION_HISTORIES, 
                								QName.createQName(VersionModel.NAMESPACE_URI, versionedNodeId));
                }
                
                updateCount++;
            }
        }
        
        // Build the result message
        return I18NUtil.getMessage(MSG_SUCCESS, updateCount);
    }

}
