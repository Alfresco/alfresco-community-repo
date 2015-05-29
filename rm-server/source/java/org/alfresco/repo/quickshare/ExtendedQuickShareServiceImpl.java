package org.alfresco.repo.quickshare;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevelManager;
import org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel;
import org.alfresco.service.cmr.quickshare.QuickShareDTO;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * Extend the QuickShareService to check that content isn't classified before sharing it.
 *
 * @author David Webster
 */
public class ExtendedQuickShareServiceImpl extends QuickShareServiceImpl
{
    private NodeService nodeService;

    /**
     * Set node service locally as inherited instance is private.
     *
     * @param nodeService the nodeService used to check node props and aspects
     */
    @Override
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
        super.setNodeService(nodeService);
    }

    @Override
    public QuickShareDTO shareContent(final NodeRef nodeRef)
    {
        if (!nodeService.hasAspect(nodeRef, ClassifiedContentModel.ASPECT_CLASSIFIED) || nodeService.getProperty(nodeRef, ClassifiedContentModel.PROP_CURRENT_CLASSIFICATION)
            .equals(ClassificationLevelManager.UNCLASSIFIED_ID))
        {
            return super.shareContent(nodeRef);
        }
        else
        {
            throw new IllegalStateException("Unable to share classified content");
        }
    }
}