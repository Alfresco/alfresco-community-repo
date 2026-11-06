/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch.query.fingerprint;

import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.CONTENT_FINGERPRINT;

import java.util.Optional;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.opensearch.client.opensearch.core.GetRequest;
import org.opensearch.client.opensearch.core.GetResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.Pair;

/**
 * Looks up the indexed {@code FINGERPRINT} of a reference node, after checking that the current user can read it.
 */
public class ElasticsearchFingerprintResolver implements FingerprintResolver
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchFingerprintResolver.class);
    private static final Pattern DBID = Pattern.compile("\\d+");

    /** Indexed document source, restricted to the fingerprint field. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record FingerprintSource(@JsonProperty(CONTENT_FINGERPRINT) String fingerprint)
    {}

    private final ElasticsearchHttpClientFactory clientFactory;
    private final PermissionService permissionService;
    private final NodeDAO nodeDAO;
    private boolean enabled;

    public ElasticsearchFingerprintResolver(ElasticsearchHttpClientFactory clientFactory, PermissionService permissionService, NodeDAO nodeDAO)
    {
        this.clientFactory = clientFactory;
        this.permissionService = permissionService;
        this.nodeDAO = nodeDAO;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    @Override
    public Optional<String> resolve(String nodeIdentifier)
    {
        try
        {
            NodeRef nodeRef = toNodeRef(nodeIdentifier);
            if (nodeRef == null || permissionService.hasReadPermission(nodeRef) != AccessStatus.ALLOWED)
            {
                return Optional.empty();
            }
            GetRequest request = new GetRequest.Builder()
                    .index(clientFactory.getIndexName())
                    .id(nodeRef.getId())
                    .sourceIncludes(CONTENT_FINGERPRINT)
                    .build();
            GetResponse<FingerprintSource> response = clientFactory.getElasticsearchClient().get(request, FingerprintSource.class);
            if (!response.found() || response.source() == null)
            {
                return Optional.empty();
            }
            return Optional.ofNullable(response.source().fingerprint());
        }
        catch (Exception e) // missing node, invalid id, access/IO errors -> behave as "no fingerprint"
        {
            LOGGER.debug("Unable to resolve fingerprint for node {}", nodeIdentifier, e);
            return Optional.empty();
        }
    }

    private NodeRef toNodeRef(String id)
    {
        if (NodeRef.isNodeRef(id))
        {
            return new NodeRef(id);
        }
        if (DBID.matcher(id).matches())
        {
            Pair<Long, NodeRef> pair = nodeDAO.getNodePair(Long.valueOf(id));
            return pair == null ? null : pair.getSecond();
        }
        return new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
    }
}
