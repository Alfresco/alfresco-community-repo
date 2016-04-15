/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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
