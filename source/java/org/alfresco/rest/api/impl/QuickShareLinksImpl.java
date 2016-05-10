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
package org.alfresco.rest.api.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.model.QuickShareModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.quickshare.QuickShareServiceImpl.QuickShareEmailRequest;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.QuickShareLinks;
import org.alfresco.rest.api.Renditions;
import org.alfresco.rest.api.model.ContentInfo;
import org.alfresco.rest.api.model.QuickShareLink;
import org.alfresco.rest.api.model.QuickShareLinkEmailRequest;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.core.exceptions.DisabledServiceException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.quickshare.InvalidSharedIdException;
import org.alfresco.service.cmr.quickshare.QuickShareDTO;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.I18NUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Centralises access to shared link (public "quick share") services and maps between representations.
 *
 * @author janv
 * @author Jamal Kaabi-Mofrad
 * 
 * @since publicapi1.0
 */
public class QuickShareLinksImpl implements QuickShareLinks, InitializingBean
{
    private static final Log logger = LogFactory.getLog(QuickShareLinksImpl.class);

    private final static String DISABLED = "QuickShare is disabled system-wide";
    private boolean enabled = true;

    private ServiceRegistry sr;
    private QuickShareService quickShareService;
    private Nodes nodes;
    private Renditions renditions;

    private NodeService nodeService;
    private PersonService personService;
    private AuthorityService authorityService;
    private MimetypeService mimeTypeService;
    private SearchService searchService;

    public void setServiceRegistry(ServiceRegistry sr)
    {
        this.sr = sr;
    }


    public void setQuickShareService(QuickShareService quickShareService)
    {
        this.quickShareService = quickShareService;
    }

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    public void setRenditions(Renditions renditions)
    {
        this.renditions = renditions;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    @Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("sr", this.sr);
        ParameterCheck.mandatory("quickShareService", this.quickShareService);
        ParameterCheck.mandatory("nodes", this.nodes);

        this.nodeService = sr.getNodeService();
        this.personService = sr.getPersonService();
        this.authorityService = sr.getAuthorityService();
        this.mimeTypeService = sr.getMimetypeService();
        this.searchService = sr.getSearchService();
    }

    /**
     * Returns limited metadata regarding the shared (content) link.
     * <p>
     * Note: does *not* require authenticated access for (public) shared link.
     */
    public QuickShareLink readById(final String sharedId, Parameters parameters)
    {
        checkEnabled();

        try
        {
            Pair<String, NodeRef> pair = quickShareService.getTenantNodeRefFromSharedId(sharedId);
            String networkTenantDomain = pair.getFirst();

            final boolean noAuth = (AuthenticationUtil.getRunAsUser() == null);

            return TenantUtil.runAsSystemTenant(new TenantUtil.TenantRunAsWork<QuickShareLink>()
            {
                public QuickShareLink doWork() throws Exception
                {
                    return getQuickShareInfo(sharedId, noAuth);
                }
            }, networkTenantDomain);
        }
        catch (InvalidSharedIdException ex)
        {
            logger.warn("Unable to find: " + sharedId);
            throw new EntityNotFoundException("Unable to find: " + sharedId);
        }
    }

    /**
     * Download content via shared link.
     * <p>
     * Note: does *not* require authenticated access for (public) shared link.
     *
     * @param sharedId
     * @param renditionId - optional
     * @param parameters {@link Parameters}
     * @return
     * @throws EntityNotFoundException
     */
    public BinaryResource readProperty(String sharedId, final String renditionId, final Parameters parameters) throws EntityNotFoundException
    {
        checkEnabled();
        checkValidShareId(sharedId);

        try
        {
            Pair<String, NodeRef> pair = quickShareService.getTenantNodeRefFromSharedId(sharedId);

            String networkTenantDomain = pair.getFirst();
            final NodeRef nodeRef = pair.getSecond();

            return TenantUtil.runAsSystemTenant(new TenantUtil.TenantRunAsWork<BinaryResource>()
            {
                public BinaryResource doWork() throws Exception
                {
                    // belt-and-braces (similar to QuickShareContentGet)
                    if (!nodeService.hasAspect(nodeRef, QuickShareModel.ASPECT_QSHARE))
                    {
                        throw new InvalidNodeRefException(nodeRef);
                    }

                    String nodeId = nodeRef.getId();

                    if (renditionId != null)
                    {
                        return renditions.getContent(nodeId, renditionId, parameters);
                    }
                    else
                    {
                        return nodes.getContent(nodeId, parameters);
                    }
                }
            }, networkTenantDomain);
        }
        catch (InvalidSharedIdException ex)
        {
            logger.warn("Unable to find: " + sharedId);
            throw new EntityNotFoundException("Unable to find: " + sharedId);
        }
        catch (InvalidNodeRefException inre)
        {
            logger.warn("Unable to find: " + sharedId + " [" + inre.getNodeRef() + "]");
            throw new EntityNotFoundException("Unable to find: " + sharedId);
        }
    }

    /**
     * Delete the shared link.
     * <p>
     * Once deleted, the shared link will no longer exist hence get/download will no longer work (ie. return 404).
     * If the link is later re-created then a new unique shared id will be generated.
     * <p>
     * Requires authenticated access.
     *
     * @param sharedId String id of the quick share
     */
    public void delete(String sharedId, Parameters parameters)
    {
        checkEnabled();
        checkValidShareId(sharedId);

        try
        {
            NodeRef nodeRef = quickShareService.getTenantNodeRefFromSharedId(sharedId).getSecond();
            String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();

            // TODO site check - see ACE-XXX
            //String siteName = getSiteName(nodeRef);

            String sharedBy = (String) nodeService.getProperty(nodeRef, QuickShareModel.PROP_QSHARE_SHAREDBY);

            if ((!currentUser.equals(sharedBy)) && (!authorityService.isAdminAuthority(currentUser)))
            {
                throw new PermissionDeniedException("Can't perform unshare action: " + sharedId);
            }

            quickShareService.unshareContent(sharedId);
        }
        catch (InvalidSharedIdException ex)
        {
            logger.warn("Unable to find: " + sharedId);
            throw new EntityNotFoundException("Unable to find: " + sharedId);
        }
        catch (InvalidNodeRefException inre)
        {
            logger.warn("Unable to find: " + sharedId + " [" + inre.getNodeRef() + "]");
            throw new EntityNotFoundException("Unable to find: " + sharedId);
        }
    }

    /**
     * Create quick share.
     * <p>
     * Requires authenticated access.
     *
     * @param nodeIds
     * @param parameters
     * @return
     */
    public List<QuickShareLink> create(List<QuickShareLink> nodeIds, Parameters parameters)
    {
        checkEnabled();

        List<QuickShareLink> result = new ArrayList<>(nodeIds.size());

        boolean noAuth = (AuthenticationUtil.getRunAsUser() == null);

        for (QuickShareLink qs : nodeIds)
        {
            String nodeId = qs.getNodeId();

            if (nodeId == null)
            {
                throw new InvalidArgumentException("A valid nodeId must be specified !");
            }

            NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);

            try
            {
                // Note: will throw InvalidNodeRefException (=> 404) if node does not exist
                String sharedId = (String) nodeService.getProperty(nodeRef, QuickShareModel.PROP_QSHARE_SHAREDID);
                if (sharedId != null)
                {
                    throw new InvalidArgumentException("sharedId already exists: "+nodeId+" ["+sharedId+"]");
                }

                // Note: will throw AccessDeniedException (=> 403) via QuickShareService (when NodeService tries to getAspects)
                // Note: since we already check node exists above, we can assume that InvalidNodeRefException (=> 404) here means not content (see type check)
                try
                {
                    QuickShareDTO qsDto = quickShareService.shareContent(nodeRef);
                    result.add(getQuickShareInfo(qsDto.getId(), noAuth));
                }
                catch (InvalidNodeRefException inre)
                {
                    throw new InvalidArgumentException("Unable to create shared link to non-file content: " + nodeId);
                }
            }
            catch (AccessDeniedException ade)
            {
                throw new PermissionDeniedException("Unable to create shared link to node that does not exist: " + nodeId);
            }
            catch (InvalidNodeRefException inre)
            {
                logger.warn("Unable to create shared link: [" + nodeRef + "]");
                throw new EntityNotFoundException("Unable to create shared link: " + nodeId);
            }
        }

        return result;
    }

    @Override
    public void emailSharedLink(String nodeId, QuickShareLinkEmailRequest emailRequest, Parameters parameters)
    {
        checkEnabled();

        try
        {   NodeRef nodeRef = nodes.validateNode(nodeId);
            final String nodeName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

            QuickShareEmailRequest request = new QuickShareEmailRequest();
            request.setSharedNodeName(nodeName);
            request.setSharedNodeURL(emailRequest.getSharedNodeUrl());
            request.setSenderMessage(emailRequest.getMessage());
            request.setLocale(I18NUtil.parseLocale(emailRequest.getLocale()));
            request.setTemplateId(emailRequest.getTemplateId());
            request.setToEmails(emailRequest.getRecipientEmails());
            request.setSendFromDefaultEmail(emailRequest.getIsSendFromDefaultEmail());
            request.setIgnoreSendFailure(emailRequest.getIsIgnoreSendFailure());
            quickShareService.sendEmailNotification(request);
        }
        catch (Exception ex)
        {
            String errorMsg = ex.getMessage();
            if (errorMsg == null)
            {
                errorMsg = "";
            }
            throw new InvalidArgumentException("Couldn't send an email. " + errorMsg);
        }
    }

    // Helper find (search) method

    public CollectionWithPagingInfo<QuickShareLink> findLinks(Parameters parameters)
    {
        checkEnabled();

        /*
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();

        String queryString =
                "ASPECT:\"" + QuickShareModel.ASPECT_QSHARE.toString() +
                        "\" +@\\{http\\://www.alfresco.org/model/content/1.0\\}" + QuickShareModel.PROP_QSHARE_SHAREDBY.getLocalName() + ":\"" + currentUser + "\"";
                        */

        String queryString =
                "ASPECT:\"" + QuickShareModel.ASPECT_QSHARE.toString() + "\"";

        SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(queryString);
        sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

        Paging paging = parameters.getPaging();
        PagingRequest pagingRequest = Util.getPagingRequest(paging);

        sp.setSkipCount(pagingRequest.getSkipCount());
        sp.setMaxItems(pagingRequest.getMaxItems());

        // TODO is perf ok with Solr 4 paging/sorting ? (otherwise make this optional via orderBy and leave default sort as undefined)
        sp.addSort("@" + ContentModel.PROP_MODIFIED, false);

        ResultSet results = searchService.query(sp);

        List<QuickShareLink> qsLinks = new ArrayList<>(results.length());

        for (ResultSetRow row : results)
        {
            NodeRef nodeRef = row.getNodeRef();
            qsLinks.add(getQuickShareInfo(nodeRef, false));
        }

        results.close();

        return CollectionWithPagingInfo.asPaged(paging, qsLinks, results.hasMore(), new Long(results.getNumberFound()).intValue());
    }

    private QuickShareLink getQuickShareInfo(String sharedId, boolean noAuth)
    {
        checkValidShareId(sharedId);

        Map<String, Object> map = (Map<String, Object>) quickShareService.getMetaData(sharedId).get("item");
        NodeRef nodeRef = new NodeRef((String) map.get("nodeRef"));

        return getQuickShareInfo(nodeRef, map, noAuth);
    }

    private QuickShareLink getQuickShareInfo(NodeRef nodeRef, boolean noAuth)
    {
        Map<String, Object> map = (Map<String, Object>) quickShareService.getMetaData(nodeRef).get("item");
        return getQuickShareInfo(nodeRef, map , noAuth);
    }

    private QuickShareLink getQuickShareInfo(NodeRef nodeRef, Map<String, Object> map, boolean noAuth)
    {
        String sharedId = (String)map.get("sharedId");

        try
        {
            Map<QName, Serializable> nodeProps = nodeService.getProperties(nodeRef);
            ContentData cd = (ContentData)nodeProps.get(ContentModel.PROP_CONTENT);

            String mimeType = cd.getMimetype();
            String mimeTypeName = mimeTypeService.getDisplaysByMimetype().get(mimeType);
            ContentInfo contentInfo = new ContentInfo(mimeType, mimeTypeName, cd.getSize(), cd.getEncoding());

            //
            // modifiedByUser
            //
            UserInfo modifiedByUser = null;
            if (noAuth)
            {
                // note: if not authenticated then we do not currently return userids (to be consistent with v0 internal - limited disclosure)
                modifiedByUser = new UserInfo(null, (String) map.get("modifierFirstName"), (String) map.get("modifierLastName"));
            }
            else
            {
                String modifiedByUserId = (String)nodeProps.get(ContentModel.PROP_MODIFIER);
                modifiedByUser = new UserInfo(modifiedByUserId, (String) map.get("modifierFirstName"), (String) map.get("modifierLastName"));
            }

            //
            // sharedByUser
            // TODO review - should we return for authenticated users only ?? (not exposed by V0 but needed for "find")
            //
            UserInfo sharedByUser = null;
            String sharedByUserId = (String)nodeProps.get(QuickShareModel.PROP_QSHARE_SHAREDBY);
            if (sharedByUserId != null)
            {
                NodeRef pRef = personService.getPerson(sharedByUserId);
                if (pRef != null)
                {
                    PersonService.PersonInfo pInfo = personService.getPerson(pRef);
                    if (pInfo != null)
                    {
                        if (noAuth)
                        {
                            // note: if not authenticated then we do not currently return userids (to be consistent with v0 internal - limited disclosure)
                            sharedByUser = new UserInfo(null, pInfo.getFirstName(), pInfo.getLastName());
                        }
                        else
                        {
                            sharedByUser = new UserInfo(sharedByUserId, pInfo.getFirstName(), pInfo.getLastName());
                        }
                    }
                }
            }

            // TODO other "properties" (if needed) - eg. cm:title, cm:lastThumbnailModificationData, ... thumbnail info ...

            QuickShareLink qs = new QuickShareLink(sharedId, nodeRef.getId());
            qs.setName((String) map.get("name"));
            qs.setContent(contentInfo);
            qs.setModifiedAt((Date) map.get("modified"));
            qs.setModifiedByUser(modifiedByUser);
            qs.setSharedByUser(sharedByUser);

            return qs;
        }
        catch (InvalidSharedIdException ex)
        {
            logger.warn("Unable to find: " + sharedId);
            throw new EntityNotFoundException("Unable to find: " + sharedId);
        }
        catch (InvalidNodeRefException inre)
        {
            logger.warn("Unable to find: " + sharedId + " [" + inre.getNodeRef() + "]");
            throw new EntityNotFoundException("Unable to find: " + sharedId);
        }
    }

    private void checkEnabled()
    {
        if (!enabled)
        {
            throw new DisabledServiceException(DISABLED);
        }
    }

    private void checkValidShareId(String sharedId)
    {
        if (sharedId==null)
        {
            throw new InvalidArgumentException("A valid sharedId must be specified !");
        }
    }
}