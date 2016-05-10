/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.rest.api.quicksharelinks;

import org.alfresco.model.QuickShareModel;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.ContentInfo;
import org.alfresco.rest.api.model.QuickShareLink;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiNoAuth;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.BinaryResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.quickshare.InvalidSharedIdException;
import org.alfresco.service.cmr.quickshare.QuickShareDTO;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * An implementation of an Entity Resource for a QuickShareLink (name TBC !!)
 *
 * @author janv
 */
@EntityResource(name="quicksharelinks", title = "QuickShareLinks")
public class QuickShareLinkEntityResource implements EntityResourceAction.ReadById<QuickShareLink>,
                                                     BinaryResourceAction.Read, EntityResourceAction.Delete,
                                                     EntityResourceAction.Create<QuickShareLink>, InitializingBean
{
    // TODO move impl into QuickShare REST service (especially if & when we need to span more than one resource) ....

    private static final Log logger = LogFactory.getLog(QuickShareLinkEntityResource.class);

    private final static String DISABLED = "QuickShare is disabled system-wide";
    private boolean enabled = true;

    private QuickShareService quickShareService;
    private Nodes nodes;
    private NodeService nodeService;

	public void setQuickShareService(QuickShareService quickShareService)
	{
		this.quickShareService = quickShareService;
	}

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    @Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("quickShareService", this.quickShareService);
        ParameterCheck.mandatory("nodes", this.nodes);
        ParameterCheck.mandatory("nodeService", this.nodeService);
    }

    /**
     * Returns limited metadata regarding the sharedId.
     *
     * Note: does not require authenticated access !
     */
    @Override
    @WebApiDescription(title="Returns quick share information for given sharedId.")
    @WebApiNoAuth
    public QuickShareLink readById(String sharedId, Parameters parameters)
    {
        if (! enabled)
        {
            throw new PermissionDeniedException(DISABLED);
        }

        return getQuickShareInfo(sharedId);
    }

    /**
     * Download content via sharedId.
     *
     * Note: does not require authenticated access !
     *
     * @param sharedId
     * @param parameters {@link Parameters}
     * @return
     * @throws EntityNotFoundException
     */
    @Override
    @WebApiDescription(title = "Download content", description = "Download content")
    @WebApiNoAuth
    @BinaryProperties({"content"})
    public BinaryResource readProperty(String sharedId, final Parameters parameters) throws EntityNotFoundException
    {
        if (! enabled)
        {
            throw new PermissionDeniedException(DISABLED);
        }

        try
        {
            Pair<String, NodeRef> pair = quickShareService.getTenantNodeRefFromSharedId(sharedId);

            String networkTenantDomain = pair.getFirst();
            final NodeRef nodeRef = pair.getSecond();

            return TenantUtil.runAsSystemTenant(new TenantUtil.TenantRunAsWork<BinaryResource>()
            {
                public BinaryResource doWork() throws Exception
                {
                    // belt-and-braces (similar to QuickSjareContentGet)
                    if (! nodeService.hasAspect(nodeRef, QuickShareModel.ASPECT_QSHARE))
                    {
                        throw new InvalidNodeRefException(nodeRef);
                    }

                    return nodes.getContent(nodeRef.getId(), parameters);
                }
            }, networkTenantDomain);
        }
        catch (InvalidSharedIdException ex)
        {
            logger.warn("Unable to find: "+sharedId);
            throw new EntityNotFoundException("Unable to find: "+sharedId);
        }
        catch (InvalidNodeRefException inre){
            logger.warn("Unable to find: "+sharedId+" ["+inre.getNodeRef()+"]");
            throw new EntityNotFoundException("Unable to find: "+sharedId);
        }
    }

    /**
     * Delete the specified quick share.
     *
     * Requires authenticated access.
     *
     * @param sharedId String id of the quick share
     */
    @Override
    @WebApiDescription(title = "Delete quick share", description="Delete the quick share reference")
    public void delete(String sharedId, Parameters parameters)
    {
        if (! enabled)
        {
            throw new PermissionDeniedException(DISABLED);
        }

        try
        {
            quickShareService.unshareContent(sharedId);
        }
        catch (InvalidSharedIdException ex)
        {
            logger.warn("Unable to find: "+sharedId);
            throw new EntityNotFoundException("Unable to find: "+sharedId);
        }
        catch (InvalidNodeRefException inre){
            logger.warn("Unable to find: "+sharedId+" ["+inre.getNodeRef()+"]");
            throw new EntityNotFoundException("Unable to find: "+sharedId);
        }
    }

    /**
     * Create quick share.
     *
     * Requires authenticated access.
     *
     * @param nodeIds
     * @param parameters
     * @return
     */
    @Override
    @WebApiDescription(title="Create quick share")
    public List<QuickShareLink> create(List<QuickShareLink> nodeIds, Parameters parameters)
    {
        List<QuickShareLink> result = new ArrayList<>(nodeIds.size());

        for (QuickShareLink qs : nodeIds)
        {
            String nodeId = qs.getNodeId();
            QuickShareDTO qsDto = quickShareService.shareContent(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId));

            // TODO should we skip errors (eg. broken share) ?
            result.add(getQuickShareInfo(qsDto.getId()));
        }

        return result;
    }

    private QuickShareLink getQuickShareInfo(String sharedId)
    {
        try
        {
            Map<String, Object> map = (Map<String, Object>)quickShareService.getMetaData(sharedId).get("item");

            String nodeId = new NodeRef((String)map.get("nodeRef")).getId();

            ContentInfo contentInfo = new ContentInfo((String)map.get("mimetype"), null, (Long)map.get("size"), null);

            // note: we do not return modifier user id (to be consistent with v0 internal - limited disclosure)
            UserInfo modifier = new UserInfo(null,(String)map.get("modifierFirstName"), (String)map.get("modifierLastName"));

            // TODO other "properties" (if needed) - eg. cm:title, cm:lastThumbnailModificationData, ... thumbnail info ...

            QuickShareLink qs = new QuickShareLink(sharedId, nodeId);
            qs.setName((String)map.get("name"));
            qs.setContent(contentInfo);
            qs.setModifiedAt((Date)map.get("modified"));
            qs.setModifiedByUser(modifier);

            return qs;
        }
        catch (InvalidSharedIdException ex)
        {
            logger.warn("Unable to find: "+sharedId);
            throw new EntityNotFoundException("Unable to find: "+sharedId);
        }
        catch (InvalidNodeRefException inre){
            logger.warn("Unable to find: "+sharedId+" ["+inre.getNodeRef()+"]");
            throw new EntityNotFoundException("Unable to find: "+sharedId);
        }
    }
}
