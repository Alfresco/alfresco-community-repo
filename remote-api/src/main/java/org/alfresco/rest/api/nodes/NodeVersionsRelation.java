/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.rest.api.nodes;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.directurl.DirectAccessUrlDisabledException;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.rest.api.DirectAccessUrlHelper;
import org.alfresco.rest.api.model.DirectAccessUrlRequest;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.api.model.VersionOptions;
import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.Operation;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.core.exceptions.DisabledServiceException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceBinaryAction;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.DirectAccessUrl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;

import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Node Versions - version history
 * 
 * @author janv
 */
@RelationshipResource(name = "versions", entityResource = NodesEntityResource.class, title = "Node Versions")
public class NodeVersionsRelation extends AbstractNodeRelation implements
        RelationshipResourceAction.Read<Node>,
        RelationshipResourceAction.ReadById<Node>,
        RelationshipResourceBinaryAction.Read,
        RelationshipResourceAction.Delete,
        InitializingBean
{
    protected VersionService versionService;
    protected BehaviourFilter behaviourFilter;
    private DirectAccessUrlHelper directAccessUrlHelper;

    public void setDirectAccessUrlHelper(DirectAccessUrlHelper directAccessUrlHelper)
    {
        this.directAccessUrlHelper = directAccessUrlHelper;
    }
    
    @Override
    public void afterPropertiesSet()
    {
        PropertyCheck.mandatory(this, "serviceRegistry", sr);
        ParameterCheck.mandatory("nodes", this.nodes);

        this.versionService = sr.getVersionService();
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }
    
    /**
     * List version history
     *
     * @param nodeId String id of (live) node
     */
    @Override
    @WebApiDescription(title = "Return version history as a paged list of version node infos")
    public CollectionWithPagingInfo<Node> readAll(String nodeId, Parameters parameters)
    {
        NodeRef nodeRef = nodes.validateOrLookupNode(nodeId, null);

        VersionHistory vh = versionService.getVersionHistory(nodeRef);

        Map<String, UserInfo> mapUserInfo = new HashMap<>(10);
        List<String> includeParam = parameters.getInclude();
        
        List<Node> collection = null;
        if (vh != null)
        {
            collection = new ArrayList<>(vh.getAllVersions().size());
            for (Version v : vh.getAllVersions())
            {
                Node node = nodes.getFolderOrDocument(v.getFrozenStateNodeRef(), null, null, includeParam, mapUserInfo);
                mapVersionInfo(v, node);
                collection.add(node);
            }
        }
        
        return listPage(collection, parameters.getPaging());
    }

    private void mapVersionInfo(Version v, Node aNode)
    {
        mapVersionInfo(v, aNode, new NodeRef("", "", v.getVersionLabel()));
    }

    public void mapVersionInfo(Version v, Node aNode, NodeRef nodeRef)
    {
        aNode.setNodeRef(nodeRef);
        aNode.setVersionComment(v.getDescription());

        Map<String, Object> props = aNode.getProperties();
        if (props != null)
        {
            // special case (as per Version2Service)
            props.put("cm:"+Version2Model.PROP_VERSION_TYPE, v.getVersionProperty(Version2Model.PROP_VERSION_TYPE));
        }

        //Don't show parentId, createdAt, createdByUser
        aNode.setParentId(null);
        aNode.setCreated(null);
        aNode.setCreatedByUser(null);
    }

    @Override
    @WebApiDescription(title="Get version node info", description = "Return metadata for a specific version node")
    public Node readById(String nodeId, String versionId, Parameters parameters)
    {
        Version version = findVersion(nodeId, versionId);

        if (version != null)
        {
            Node node = nodes.getFolderOrDocumentFullInfo(version.getFrozenStateNodeRef(), null, null, parameters, null);
            mapVersionInfo(version, node);
            return node;
        }

        throw new EntityNotFoundException(nodeId+"-"+versionId);
    }

    @WebApiDescription(title = "Download version content", description = "Download version content")
    @BinaryProperties({ "content" })
    @Override
    public BinaryResource readProperty(String nodeId, String versionId, Parameters parameters)
    {
        Version version = findVersion(nodeId, versionId);

        if (version != null)
        {
            NodeRef versionNodeRef = version.getFrozenStateNodeRef();
            return nodes.getContent(versionNodeRef, parameters, true); // TODO should we record version downloads ?
        }

        throw new EntityNotFoundException(nodeId+"-"+versionId);
    }

    @Operation("revert")
    @WebApiDescription(title = "Revert Version",
            description="Reverts (ie. promotes) specified version to become a new, most recent, version",
            successStatus = HttpServletResponse.SC_OK)
    public Node revertById(String nodeId, String versionId, VersionOptions versionOptions, Parameters parameters, WithResponse withResponse)
    {
        Version version = findVersion(nodeId, versionId);

        if (version != null)
        {
            CheckOutCheckInService cociService = sr.getCheckOutCheckInService();

            NodeRef nodeRef = version.getVersionedNodeRef();

            String versionComment = versionOptions.getComment();

            VersionType versionType = VersionType.MINOR;
            Boolean versionMajor = versionOptions.getMajorVersion();
            if ((versionMajor != null) && (versionMajor))
            {
                versionType = VersionType.MAJOR;
            }

            Map<String, Serializable> versionProperties = new HashMap<>(2);
            versionProperties.put(VersionModel.PROP_VERSION_TYPE, versionType);
            if (versionComment != null)
            {
                versionProperties.put(VersionModel.PROP_DESCRIPTION, versionComment);
            }

            //cancel editing if we want to revert
            if (sr.getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY))
            {
                nodeRef = cociService.cancelCheckout(nodeRef);
            }

            // TODO review default for deep and/or whether we should make it an option
            versionService.revert(nodeRef, version, false);

            // Checkout/Checkin the node - to store the new version in version history
            NodeRef wcNodeRef = cociService.checkout(nodeRef);
            cociService.checkin(wcNodeRef, versionProperties);

            // get latest version
            version = versionService.getVersionHistory(nodeRef).getHeadVersion();

            Node node = nodes.getFolderOrDocumentFullInfo(version.getFrozenStateNodeRef(), null, null, parameters, null);
            mapVersionInfo(version, node);
            return node;
        }

        throw new EntityNotFoundException(nodeId+"-"+versionId);
    }

    @Override
    @WebApiDescription(title = "Delete version")
    public void delete(String nodeId, String versionId, Parameters parameters)
    {
        Version version = findVersion(nodeId, versionId);

        // live (aka versioned) nodeRef
        NodeRef nodeRef = version.getVersionedNodeRef();

        if (sr.getPermissionService().hasPermission(nodeRef, PermissionService.DELETE) != AccessStatus.ALLOWED)
        {
            throw new PermissionDeniedException("Cannot delete version");
        }

        versionService.deleteVersion(nodeRef, version);

        Map<QName, Serializable> props = sr.getNodeService().getProperties(nodeRef);
        if (props.get(ContentModel.PROP_VERSION_LABEL) == null)
        {
            // attempt to delete last version - we do not yet support this (see REPO-835 & REPO-834)
            // note: alternatively, the client can remove the "cm:versionable" aspect (if permissions allow) to clear the version history and disable versioning
            throw new IntegrityException("Cannot delete last version (did you mean to disable versioning instead ?) ["+nodeId+","+versionId+"]", null);
            
            /*
            if (props.get(ContentModel.PROP_VERSION_TYPE) != null)
            {
                // minor fix up to versionable aspect - ie. remove versionType
                behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_VERSIONABLE);
                behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
                try
                {
                    sr.getNodeService().removeProperty(nodeRef, ContentModel.PROP_VERSION_TYPE);
                }
                finally
                {
                    behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
                    behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_VERSIONABLE);
                }
            }
            */
        }
    }

    public Version findVersion(String nodeId, String versionLabelId)
    {
        NodeRef nodeRef = nodes.validateOrLookupNode(nodeId, null);
        VersionHistory vh = versionService.getVersionHistory(nodeRef);
        if (vh != null) 
        {
            return vh.getVersion(versionLabelId);
        }
        return null;
    }

    @Operation("request-direct-access-url")
    @WebApiParam (name = "directAccessUrlRequest", title = "Request direct access url", description = "Options for direct access url request", kind = ResourceParameter.KIND.HTTP_BODY_OBJECT)
    @WebApiDescription(title = "Request content url",
            description="Generates a direct access URL.",
            successStatus = HttpServletResponse.SC_OK)
    public DirectAccessUrl requestContentDirectUrl(String nodeId, String versionId, DirectAccessUrlRequest directAccessUrlRequest, Parameters parameters, WithResponse withResponse)
    {
        boolean attachment = directAccessUrlHelper.getAttachment(directAccessUrlRequest);
        Long validFor = directAccessUrlHelper.getDefaultExpiryTimeInSec();
        Version version = findVersion(nodeId, versionId);
        if (version != null)
        {
            NodeRef versionNodeRef = version.getFrozenStateNodeRef();

            DirectAccessUrl directAccessUrl;
            try
            {
                directAccessUrl = nodes.requestContentDirectUrl(versionNodeRef, attachment, validFor);
            }
            catch (DirectAccessUrlDisabledException ex)
            {
                throw new DisabledServiceException(ex.getMessage());
            }
            return directAccessUrl;
        }
        throw new EntityNotFoundException(nodeId+"-"+versionId);
    }
}
