/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

package org.alfresco.repo.search.impl.solr.facet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdateNodePolicy;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * 
 *
 * @author Jamal Kaabi-Mofrad
 */
public class SolrFacetServiceImpl extends AbstractLifecycleBean implements SolrFacetService,
            NodeServicePolicies.OnCreateNodePolicy,
            NodeServicePolicies.OnUpdateNodePolicy,
            NodeServicePolicies.BeforeDeleteNodePolicy
{
    private static final Log logger = LogFactory.getLog(SolrFacetServiceImpl.class);
    /**
     * The authority that needs to contain the users allowed to administer the faceted-search config.
     */
    private static final String ALFRESCO_SEARCH_ADMINISTRATORS_AUTHORITY = "ALFRESCO_SEARCH_ADMINISTRATORS";
    private static final String GROUP_ALFRESCO_SEARCH_ADMINISTRATORS_AUTHORITY = PermissionService.GROUP_PREFIX
                + ALFRESCO_SEARCH_ADMINISTRATORS_AUTHORITY;
    
    /** The store where facets are kept */
    private static final StoreRef FACET_STORE = new StoreRef("workspace://SpacesStore");

    private AuthorityService authorityService;
    private NodeService nodeService;
    private NamespaceService namespaceService;
    private SearchService searchService;
    private RetryingTransactionHelper retryingTransactionHelper;
    private BehaviourFilter behaviourFilter;
    private PolicyComponent policyComponent;
    private SolrFacetConfig facetConfig;    
    private String facetsRootXPath;
    private SimpleCache<String, Object> singletonCache; // eg. for facetsHomeNodeRef
    private final String KEY_FACETS_HOME_NODEREF = "key.facetshome.noderef";
    private SimpleCache<String, NodeRef> facetNodeRefCache; // for filterID to nodeRef lookup
    private ConcurrentMap<String, SolrFacetProperties> facetsMap = new ConcurrentHashMap<>();

    /**
     * @param authorityService the authorityService to set
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param namespaceService the namespaceService to set
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @param searchService the searchService to set
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * @param retryingTransactionHelper the retryingTransactionHelper to set
     */
    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    /**
     * @param behaviourFilter the behaviourFilter to set
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    /**
     * @param policyComponent the policyComponent to set
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * @param facetConfig the facetConfig to set
     */
    public void setFacetConfig(SolrFacetConfig facetConfig)
    {
        this.facetConfig = facetConfig;
    }

    /**
     * @param facetsRootXPath the facetsRootXPath to set
     */
    public void setFacetsRootXPath(String facetsRootXPath)
    {
        this.facetsRootXPath = facetsRootXPath;
    }

    /**
     * @param singletonCache the singletonCache to set
     */
    public void setSingletonCache(SimpleCache<String, Object> singletonCache)
    {
        this.singletonCache = singletonCache;
    }

    /**
     * @param facetNodeRefCache the facetNodeRefCache to set
     */
    public void setFacetNodeRefCache(SimpleCache<String, NodeRef> facetNodeRefCache)
    {
        this.facetNodeRefCache = facetNodeRefCache;
    }

    @Override
    public boolean isSearchAdmin(String userName)
    {
        if (userName == null)
        {
            return false;
        }
        return this.authorityService.isAdminAuthority(userName)
                    || this.authorityService.getAuthoritiesForUser(userName).contains(GROUP_ALFRESCO_SEARCH_ADMINISTRATORS_AUTHORITY);
    }

    @Override
    public Map<String, SolrFacetProperties> getFacets()
    {
        return Collections.unmodifiableMap(facetsMap);
    }

    @Override
    public SolrFacetProperties getFacet(String filterID)
    {
        return facetsMap.get(filterID);
    }

    @Override
    public NodeRef getFacetNodeRef(final String filterID)
    {
        ParameterCheck.mandatory("filterID", filterID);

        NodeRef facetNodeRef = facetNodeRefCache.get(filterID);
        if (facetNodeRef != null)
        {
            // test for existence - and remove from cache if no longer exists
            if (!this.nodeService.exists(facetNodeRef))
            {
                facetNodeRefCache.remove(filterID);
                facetNodeRef = null;
            }
        }
        else
        {
            // not in cache - find and store
            final NodeRef facetRoot = getFacetsRoot();
            facetNodeRef = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>()
            {
                public NodeRef doWork() throws Exception
                {
                    // the filterID directly maps to the cm:name property
                    NodeRef nodeRef = nodeService.getChildByName(facetRoot, ContentModel.ASSOC_CONTAINS, filterID);
                    // cache the result if found
                    if (nodeRef != null)
                    {
                        facetNodeRefCache.put(filterID, nodeRef);
                    }
                    return nodeRef;
                }
            }, AuthenticationUtil.getSystemUserName());
        }

        return facetNodeRef;
    }

    private SolrFacetProperties getFacetProperties(NodeRef nodeRef)
    {
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        if (properties.isEmpty())
        {
            return new SolrFacetProperties.Builder().build();
        }

        String filterID = (String) properties.get(ContentModel.PROP_NAME);
        QName fieldQName = (QName) properties.get(SolrFacetModel.PROP_FIELD_TYPE);
        String displayName = (String) properties.get(SolrFacetModel.PROP_FIELD_LABEL);
        int maxFilters = (Integer) properties.get(SolrFacetModel.PROP_MAX_FILTERS);
        int hitThreshold = (Integer) properties.get(SolrFacetModel.PROP_HIT_THRESHOLD);
        int minFilterValueLength = (Integer) properties.get(SolrFacetModel.PROP_MIN_FILTER_VALUE_LENGTH);
        String sortBy = (String) properties.get(SolrFacetModel.PROP_SORT_BY);
        String scope = (String) properties.get(SolrFacetModel.PROP_SCOPE);
        int index = (Integer) properties.get(SolrFacetModel.PROP_INDEX);
        boolean isEnabled = (Boolean) properties.get(SolrFacetModel.PROP_IS_ENABLED);
        @SuppressWarnings("unchecked")
        List<String> scSites = (List<String>) properties.get(SolrFacetModel.PROP_SCOPED_SITES);
        Set<String> scopedSites = (scSites == null) ? null : new HashSet<>(scSites);

        // Construct the FacetProperty object
        SolrFacetProperties fp = new SolrFacetProperties.Builder()
                    .filterID(filterID)
                    .facetQName(fieldQName)
                    .displayName(displayName)
                    .maxFilters(maxFilters)
                    .hitThreshold(hitThreshold)
                    .minFilterValueLength(minFilterValueLength)
                    .sortBy(sortBy)
                    .scope(scope)
                    .index(index)
                    .isEnabled(isEnabled)
                    .scopedSites(scopedSites).build();

        return fp;
    }

    @Override
    public NodeRef createFacetNode(SolrFacetProperties facetProperties)
    {
        return createFacetNodeImpl(facetProperties, true);
    }

    private NodeRef createFacetNodeImpl(final SolrFacetProperties facetProperties, boolean checkDefaultFP)
    {

        final String filterID = facetProperties.getFilterID();
        NodeRef facetNodeRef = getFacetNodeRef(filterID);
        // We need to check the bootstrapped Facet properties as well, in order
        // to not allow the user to create a new facet with the same filterID as the bootstrapped FP.
        if (facetNodeRef != null || (checkDefaultFP && getFacet(filterID) != null))
        {
            throw new SolrFacetConfigException("Unable to create facet because the filterID [" + filterID + "] is already in use.");
        }

        // Get the facet root node reference
        final NodeRef facetRoot = getFacetsRoot();
        if (facetRoot == null)
        {
            throw new SolrFacetConfigException("Facets root folder does not exist.");
        }

       return facetNodeRef = AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
        {
            @Override
            public NodeRef doWork() throws Exception
            {
                return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Exception
                    {
                        behaviourFilter.disableBehaviour(facetRoot, ContentModel.ASPECT_AUDITABLE);
                        try
                        {
                            Map<QName, Serializable> properties = createNodeProperties(facetProperties, true);
                            // We don't want the node to be indexed
                            properties.put(ContentModel.PROP_IS_INDEXED, false);
                            NodeRef ref = nodeService.createNode(facetRoot, ContentModel.ASSOC_CONTAINS,
                                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, filterID),
                                        SolrFacetModel.TYPE_FACET_FIELD, properties).getChildRef();
                            if (logger.isDebugEnabled())
                            {
                                logger.debug("Created [" + filterID + "] facet node with properties: [" + properties + "]");
                            }
                            return ref;
                        }
                        finally
                        {
                            behaviourFilter.enableBehaviour(facetRoot, ContentModel.ASPECT_AUDITABLE);
                        }
                    }
                }, false);

            }
        }, AuthenticationUtil.getSystemUserName());
    }

    @Override
    public void updateFacet(SolrFacetProperties facetProperties)
    {
        final String filterID = facetProperties.getFilterID();

        NodeRef facetNodeRef = getFacetNodeRef(filterID);
        if (facetNodeRef == null)
        {
            SolrFacetProperties fp = getFacet(filterID);
            if (fp != null)
            {
                // As we don't create nodes for the bootstrapped FP on server
                // startup, we need to create a node here, when a user tries to
                // update the default properties for the first time. 
                createFacetNodeImpl(facetProperties, false);
            }
            else
            {
                throw new SolrFacetConfigException("Cannot update facet [" + filterID + "] as it does not exist.");
            }
        }
        else
        {
            Map<QName, Serializable> properties = createNodeProperties(facetProperties, false);
            // Set the updated properties back onto the facet node reference
            this.nodeService.setProperties(facetNodeRef, properties);
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("Updated [" + filterID + "] facet node. Properties: [" + facetProperties + "]");
        }
    }

    @Override
    public void deleteFacet(String filterID)
    {
        NodeRef facetNodeRef = getFacetNodeRef(filterID);
        SolrFacetProperties defaultFP = facetConfig.getDefaultFacets().get(filterID);
        if(defaultFP != null)
        {
            throw new SolrFacetConfigException("The default [" + filterID + "] facet cannot be deleted. It can only be disabled.");
        }

        if(facetNodeRef == null)
        {
            throw new SolrFacetConfigException("The [" + filterID + "] facet cannot be found.");
        }
        
        nodeService.deleteNode(facetNodeRef);
        if (logger.isDebugEnabled())
        {
            logger.debug("Deleted [" + filterID + "] facet.");
        }
    }

    private Map<QName, Serializable> createNodeProperties(SolrFacetProperties facetProperties, boolean withFilterId)
    {
        if (facetProperties.getFilterID() == null)
        {
            throw new SolrFacetConfigException("Filter Id cannot be null.");
        }

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(11);

        if (withFilterId)
        {
            properties.put(ContentModel.PROP_NAME, facetProperties.getFilterID());
        }
        properties.put(SolrFacetModel.PROP_FIELD_TYPE, facetProperties.getFacetQName());
        properties.put(SolrFacetModel.PROP_FIELD_LABEL, facetProperties.getDisplayName());
        properties.put(SolrFacetModel.PROP_MAX_FILTERS, facetProperties.getMaxFilters());
        properties.put(SolrFacetModel.PROP_HIT_THRESHOLD, facetProperties.getHitThreshold());
        properties.put(SolrFacetModel.PROP_MIN_FILTER_VALUE_LENGTH, facetProperties.getMinFilterValueLength());
        properties.put(SolrFacetModel.PROP_SCOPE, facetProperties.getScope());
        properties.put(SolrFacetModel.PROP_SORT_BY, facetProperties.getSortBy());
        properties.put(SolrFacetModel.PROP_SCOPED_SITES, (Serializable) facetProperties.getScopedSites());
        properties.put(SolrFacetModel.PROP_INDEX, facetProperties.getIndex());
        properties.put(SolrFacetModel.PROP_IS_ENABLED, facetProperties.isEnabled());

        return properties;
    }

    public NodeRef getFacetsRoot()
    {
        NodeRef facetHomeRef = (NodeRef) singletonCache.get(KEY_FACETS_HOME_NODEREF);
        if (facetHomeRef == null)
        {
            facetHomeRef = AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
            {
                public NodeRef doWork() throws Exception
                {
                    return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
                    {
                        public NodeRef execute() throws Exception
                        {
                            NodeRef result = null;

                            // Get the root 'facets' folder
                            NodeRef rootNodeRef = nodeService.getRootNode(FACET_STORE);
                            List<NodeRef> results = searchService.selectNodes(rootNodeRef, facetsRootXPath, null,
                                        namespaceService, false, SearchService.LANGUAGE_XPATH);
                            if (results.size() != 0)
                            {
                                result = results.get(0);
                            }

                            return result;
                        }
                    }, true);
                }
            }, AuthenticationUtil.getSystemUserName());

            singletonCache.put(KEY_FACETS_HOME_NODEREF, facetHomeRef);
        }
        return facetHomeRef;
    }
    

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // Filter creation
        this.policyComponent.bindClassBehaviour(
                    OnCreateNodePolicy.QNAME,
                    SolrFacetModel.TYPE_FACET_FIELD,
                    new JavaBehaviour(this, "onCreateNode"));

        // Filter update
        this.policyComponent.bindClassBehaviour(
                    OnUpdateNodePolicy.QNAME,
                    SolrFacetModel.TYPE_FACET_FIELD,
                    new JavaBehaviour(this, "onUpdateNode"));

        // Filter deletion
        this.policyComponent.bindClassBehaviour(
                    BeforeDeleteNodePolicy.QNAME,
                    SolrFacetModel.TYPE_FACET_FIELD,
                    new JavaBehaviour(this, "beforeDeleteNode"));

        Map<String, SolrFacetProperties> defaultFP = facetConfig.getDefaultFacets();
        for(Entry<String, SolrFacetProperties> fpEntry : defaultFP.entrySet())
        {
            facetsMap.put(fpEntry.getKey(), fpEntry.getValue());
        }
        
        List<SolrFacetProperties> persistedProperties = getPersistedFacetProperties();
        // The persisted facets will override the default facets
        for(SolrFacetProperties fp : persistedProperties)
        {
            facetsMap.put(fp.getFilterID(), fp);
        }

        if (logger.isDebugEnabled() && persistedProperties.size() > 0)
        {
            logger.debug("The facets [" + persistedProperties + "] have overridden their matched default facets.");
        }
    }
    
    private List<SolrFacetProperties> getPersistedFacetProperties()
    {
        List<ChildAssociationRef> list = nodeService.getChildAssocs(getFacetsRoot());

        List<SolrFacetProperties> facets = new ArrayList<>(list.size());
        for (ChildAssociationRef associationRef : list)
        {
            SolrFacetProperties fp = getFacetProperties(associationRef.getChildRef());
            facets.add(fp);
        }
        return facets;
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // nothing to do
    }

    @Override
    public void onUpdateNode(NodeRef nodeRef)
    {
        SolrFacetProperties fp = getFacetProperties(nodeRef);
        this.facetsMap.put(fp.getFilterID(), fp);
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        SolrFacetProperties fp = getFacetProperties(childAssocRef.getChildRef());
        this.facetsMap.put(fp.getFilterID(), fp);
    }

    @Override
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        String filterID = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        this.facetsMap.remove(filterID);
    }
}
