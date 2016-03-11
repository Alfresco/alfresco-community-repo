package org.alfresco.module.org_alfresco_module_rm.jscript.app.evaluator;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.jscript.app.BaseEvaluator;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Split EMail action evaluator
 *
 * @author Roy Wetherall
 */
public class SplitEmailActionEvaluator extends BaseEvaluator
{
    @Override
    protected boolean evaluateImpl(NodeRef nodeRef)
    {
        boolean result = false;
        if (!recordService.isDeclared(nodeRef))
        {
            ContentData contentData = (ContentData)nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
            if (contentData != null)
            {
                String mimetype = contentData.getMimetype();
                if (mimetype != null &&
                    (MimetypeMap.MIMETYPE_RFC822.equals(mimetype) ||
                     MimetypeMap.MIMETYPE_OUTLOOK_MSG.equals(mimetype)))
                {
                    result = true;
                }
            }
        }
        return result;
    }
}
