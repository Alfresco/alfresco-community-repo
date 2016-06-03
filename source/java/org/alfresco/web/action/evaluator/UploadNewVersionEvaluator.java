package org.alfresco.web.action.evaluator;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.bean.coci.EditOfflineDialog;
import org.alfresco.web.bean.repository.Node;

/**
 * UI Action Evaluator - Upload new version
 */
public class UploadNewVersionEvaluator extends BaseActionEvaluator
{
    /**
     * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
     */
    public boolean evaluate(Node node)
    {
        return (node.hasAspect(ContentModel.ASPECT_WORKING_COPY) &&
                EditOfflineDialog.OFFLINE_EDITING.equals(node.getProperties().get(ContentModel.PROP_WORKING_COPY_MODE)) &&
                node.hasPermission(PermissionService.CHECK_IN));
    }
}