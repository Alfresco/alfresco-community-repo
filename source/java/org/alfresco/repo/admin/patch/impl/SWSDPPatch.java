package org.alfresco.repo.admin.patch.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.model.filefolder.HiddenAspect;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.extensions.surf.util.I18NUtil;

public class SWSDPPatch extends AbstractPatch
{
    private static final String MSG_SITE_PATCHED = "patch.swsdpPatch.success";
    private static final String MSG_SKIPPED = "patch.swsdpPatch.skipped";
    private static final String MSG_MISSING_SURFCONFIG = "patch.swsdpPatch.missingSurfConfig";

    private SiteService siteService;
    private HiddenAspect hiddenAspect;

	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

	public void setNodeService(NodeService nodeService)
	{
		this.nodeService = nodeService;
	}

	public void setHiddenAspect(HiddenAspect hiddenAspect)
	{
		this.hiddenAspect = hiddenAspect;
	}

    @Override
    protected String applyInternal() throws Exception
    {
    	SiteInfo siteInfo = siteService.getSite("swsdp");
    	if(siteInfo != null)
    	{
	    	NodeRef nodeRef = siteInfo.getNodeRef();
	    	NodeRef surfConfigNodeRef = nodeService.getChildByName(nodeRef, ContentModel.ASSOC_CONTAINS, "surf-config");
	    	if(surfConfigNodeRef == null)
	    	{
	            return I18NUtil.getMessage(MSG_MISSING_SURFCONFIG);
	    	}
	    	else
	    	{
		    	for(ChildAssociationRef childRef : nodeService.getChildAssocs(surfConfigNodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL))
		    	{
		    		hiddenAspect.showNode(childRef.getChildRef(), true);
		    	}
	    	}

	        return I18NUtil.getMessage(MSG_SITE_PATCHED);
    	}
    	else
    	{
	        return I18NUtil.getMessage(MSG_SKIPPED);
    	}
    }
}
