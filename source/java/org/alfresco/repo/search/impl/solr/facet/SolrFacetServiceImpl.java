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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.dictionary.Facetable;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.search.impl.solr.facet.Exceptions.DuplicateFacetId;
import org.alfresco.repo.search.impl.solr.facet.Exceptions.IllegalArgument;
import org.alfresco.repo.search.impl.solr.facet.Exceptions.MissingFacetId;
import org.alfresco.repo.search.impl.solr.facet.Exceptions.UnrecognisedFacetId;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetProperties.CustomProperties;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
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
import org.alfresco.util.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Solr Facet Service Implementation.
 *
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class SolrFacetServiceImpl extends AbstractLifecycleBean implements SolrFacetService,
            NodeServicePolicies.OnCreateNodePolicy,
            NodeServicePolicies.BeforeDeleteNodePolicy
{
    private static final Log logger = LogFactory.getLog(SolrFacetServiceImpl.class);
    /**
     * The authority that needs to contain the users allowed to administer the faceted-search config.
     */
    public static final String ALFRESCO_SEARCH_ADMINISTRATORS_AUTHORITY = "ALFRESCO_SEARCH_ADMINISTRATORS";
    public static final String GROUP_ALFRESCO_SEARCH_ADMINISTRATORS_AUTHORITY = PermissionService.GROUP_PREFIX
                + ALFRESCO_SEARCH_ADMINISTRATORS_AUTHORITY;

    /** The store where facets are kept */
    private static final StoreRef FACET_STORE = new StoreRef("workspace://SpacesStore");

    private AuthorityService authorityService;
    private DictionaryService dictionaryService;
    protected NodeService nodeService;
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
    private ConcurrentMap<String, SolrFacetProperties> defaultFacetsMap = new ConcurrentHashMap<String, SolrFacetProperties>(10);

    /**
     * @param authorityService the authorityService to set
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * @param dictionaryService the dictionaryService to set
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
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
    public List<SolrFacetProperties> getFacets()
    {
        // Sort the facets into display order
        final SolrFacetComparator comparator = new SolrFacetComparator(getFacetOrder());

        SortedSet<SolrFacetProperties> result = new TreeSet<>(comparator);
        List<ChildAssociationRef> children = nodeService.getChildAssocs(getFacetsRoot());

        for (ChildAssociationRef ref : children)
        {
            result.add(getFacetProperties(ref.getChildRef()));
        }
        // add the default filters
        result.addAll(defaultFacetsMap.values());

        return new ArrayList<>(result);
    }

    public List<String> getFacetOrder()
    {
        final NodeRef facetContainer = getFacetsRoot();

        @SuppressWarnings("unchecked")
        final List<String> facetOrder = (List<String>) nodeService.getProperty(facetContainer, SolrFacetModel.PROP_FACET_ORDER);
        return facetOrder;
    }

    @Override
    public SolrFacetProperties getFacet(String filterID)
    {
        /*
         * Note: There is no need to worry about the state of the SolrFacetProperties returned from
         * facetConfig (getDefaultLoadedFacet), as if the FP has been modified, then we'll get it from
         * the nodeService.
         */
        NodeRef nodeRef = getFacetNodeRef(filterID);
        return (nodeRef == null) ? defaultFacetsMap.get(filterID) : getFacetProperties(nodeRef);
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
        boolean isDefault = (Boolean) properties.get(SolrFacetModel.PROP_IS_DEFAULT);

        SolrFacetProperties defaultFacet = defaultFacetsMap.get(filterID);
        if(defaultFacet == null)
        {
            defaultFacet = new SolrFacetProperties.Builder().build();
        }

        QName fieldQName = getDefaultIfNull(defaultFacet.getFacetQName(), (QName) properties.get(SolrFacetModel.PROP_FIELD_TYPE));
        String displayName = getDefaultIfNull(defaultFacet.getDisplayName(), (String) properties.get(SolrFacetModel.PROP_FIELD_LABEL));
        String displayControl = getDefaultIfNull(defaultFacet.getDisplayControl(), (String) properties.get(SolrFacetModel.PROP_DISPLAY_CONTROL));
        int maxFilters = getDefaultIfNull(defaultFacet.getMaxFilters(), (Integer) properties.get(SolrFacetModel.PROP_MAX_FILTERS));
        int hitThreshold = getDefaultIfNull(defaultFacet.getHitThreshold(), (Integer) properties.get(SolrFacetModel.PROP_HIT_THRESHOLD));
        int minFilterValueLength = getDefaultIfNull(defaultFacet.getMinFilterValueLength(), (Integer) properties.get(SolrFacetModel.PROP_MIN_FILTER_VALUE_LENGTH));
        String sortBy = getDefaultIfNull(defaultFacet.getSortBy(), (String) properties.get(SolrFacetModel.PROP_SORT_BY));
        String scope = getDefaultIfNull(defaultFacet.getScope(), (String) properties.get(SolrFacetModel.PROP_SCOPE));
        Boolean isEnabled = getDefaultIfNull(defaultFacet.isEnabled(), (Boolean) properties.get(SolrFacetModel.PROP_IS_ENABLED));
        @SuppressWarnings("unchecked")
        List<String> scSites = (List<String>) properties.get(SolrFacetModel.PROP_SCOPED_SITES);
        Set<String> scopedSites = getDefaultIfNull(defaultFacet.getScopedSites(), (scSites == null) ? null : new HashSet<>(scSites));

        Set<CustomProperties> extraProps = null;
        Map<QName, Serializable> customProperties = getFacetCustomProperties(properties);
        boolean hasAspect = nodeService.hasAspect(nodeRef, SolrFacetModel.ASPECT_CUSTOM_PROPERTIES);
        if (!hasAspect && customProperties.isEmpty())
        {
            extraProps = defaultFacet.getCustomProperties();
        }
        else
        {
            extraProps = new HashSet<>(customProperties.size());
            for (Entry<QName, Serializable> cp : customProperties.entrySet())
            {
                extraProps.add(new CustomProperties(cp.getKey(), cp.getValue()));
            }
        }
        // Construct the FacetProperty object
        SolrFacetProperties fp = new SolrFacetProperties.Builder()
                    .filterID(filterID)
                    .facetQName(fieldQName)
                    .displayName(displayName)
                    .displayControl(displayControl)
                    .maxFilters(maxFilters)
                    .hitThreshold(hitThreshold)
                    .minFilterValueLength(minFilterValueLength)
                    .sortBy(sortBy)
                    .scope(scope)
                    .isEnabled(isEnabled)
                    .isDefault(isDefault)
                    .scopedSites(scopedSites)
                    .customProperties(extraProps).build();

        return fp;
    }

    private <T> T getDefaultIfNull(T defaultValue, T newValue)
    {
        return (newValue == null) ? defaultValue : newValue;
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
        // We need to check the bootstrapped Facet properties (i.e loaded from properties file(s)) as well,
        // in order to not allow the user to create a new facet with the same filterID as the bootstrapped FP.
        if (facetNodeRef != null || (checkDefaultFP && defaultFacetsMap.get(filterID) != null))
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
                            Map<QName, Serializable> properties = createNodeProperties(facetProperties);
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
            SolrFacetProperties fp = defaultFacetsMap.get(filterID);
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
            Map<QName, Serializable> properties = createNodeProperties(facetProperties);
            // Set the updated properties back onto the facet node reference
            for (Entry<QName, Serializable> prop : properties.entrySet())
            {
                this.nodeService.setProperty(facetNodeRef, prop.getKey(), prop.getValue());
            }
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
        if (facetNodeRef == null)
        {
            throw new SolrFacetConfigException("The [" + filterID + "] facet cannot be found.");
        }

        SolrFacetProperties defaultFP = defaultFacetsMap.get(filterID);
        if (defaultFP != null)
        {
            throw new SolrFacetConfigException("The default [" + filterID + "] facet cannot be deleted. It can only be disabled.");
        }
        nodeService.deleteNode(facetNodeRef);
        if (logger.isDebugEnabled())
        {
            logger.debug("Deleted [" + filterID + "] facet.");
        }
    }

    private Map<QName, Serializable> createNodeProperties(SolrFacetProperties facetProperties)
    {
        if (facetProperties.getFilterID() == null)
        {
            throw new SolrFacetConfigException("Filter Id cannot be null.");
        }

        boolean isDefaultFP = defaultFacetsMap.containsKey(facetProperties.getFilterID());
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(15);

        properties.put(ContentModel.PROP_NAME, facetProperties.getFilterID());
        properties.put(SolrFacetModel.PROP_IS_DEFAULT, isDefaultFP);

        addNodeProperty(properties, SolrFacetModel.PROP_FIELD_TYPE, facetProperties.getFacetQName());
        addNodeProperty(properties, SolrFacetModel.PROP_FIELD_LABEL, facetProperties.getDisplayName());
        addNodeProperty(properties, SolrFacetModel.PROP_DISPLAY_CONTROL, facetProperties.getDisplayControl());
        addNodeProperty(properties, SolrFacetModel.PROP_MAX_FILTERS, facetProperties.getMaxFilters());
        addNodeProperty(properties, SolrFacetModel.PROP_HIT_THRESHOLD, facetProperties.getHitThreshold());
        addNodeProperty(properties, SolrFacetModel.PROP_MIN_FILTER_VALUE_LENGTH, facetProperties.getMinFilterValueLength());
        addNodeProperty(properties, SolrFacetModel.PROP_SCOPE, facetProperties.getScope());
        addNodeProperty(properties, SolrFacetModel.PROP_SORT_BY, facetProperties.getSortBy());
        addNodeProperty(properties, SolrFacetModel.PROP_SCOPED_SITES, (Serializable) facetProperties.getScopedSites());
        addNodeProperty(properties, SolrFacetModel.PROP_IS_ENABLED, facetProperties.isEnabled());

        Set<CustomProperties> customProperties = facetProperties.getCustomProperties();
        if (customProperties != null)
        {
            properties.put(SolrFacetModel.PROP_EXTRA_INFORMATION, new ArrayList<>(customProperties));
        }

        return properties;
    }

    private void addNodeProperty(Map<QName, Serializable> properties, QName qname, Serializable propValue)
    {
        if (propValue == null)
        {
            return;
        }
        if (propValue instanceof Integer && ((Integer) propValue) < 0)
        {
            return;
        }
        if (propValue instanceof Collection<?> && ((Collection<?>) propValue).isEmpty())
        {
            return;
        }

        properties.put(qname, propValue);
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

        // Filter before deletion
        this.policyComponent.bindClassBehaviour(
                    BeforeDeleteNodePolicy.QNAME,
                    SolrFacetModel.TYPE_FACET_FIELD,
                    new JavaBehaviour(this, "beforeDeleteNode"));

        Map<String, SolrFacetProperties> mergedMap = new HashMap<>(100);
        // Loaded facets
        Map<String, SolrFacetProperties> defaultFP = facetConfig.getDefaultFacets();
        defaultFacetsMap.putAll(defaultFP); // add the default facets to a ConcurrentHashMap for performance reasons
        mergedMap.putAll(defaultFP);

        // Persisted facets
        Map<String, SolrFacetProperties> persistedProperties = getPersistedFacetProperties();
        // The persisted facets will override the default facets
        mergedMap.putAll(persistedProperties);

        List<String> facetOrder = getFacetOrder();
        // Sort the merged maps
        Comparator<Entry<String, SolrFacetProperties>> entryComparator = CollectionUtils.toEntryComparator(new SolrFacetComparator(facetOrder));
        Map<String, SolrFacetProperties> sortedMap = CollectionUtils.sortMapByValue(mergedMap, entryComparator);

        if (logger.isDebugEnabled() && persistedProperties.size() > 0)
        {
            logger.debug("The facets [" + persistedProperties + "] have overridden their matched default facets.");
        }

        final Set<String> newFacetOrder = (facetOrder == null) ? new LinkedHashSet<String>(sortedMap.size()) : new LinkedHashSet<String>(facetOrder);

        for (SolrFacetProperties fp : sortedMap.values())
        {
            newFacetOrder.add(fp.getFilterID());
        }

        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Exception
                    {
                        reorderFacets(new ArrayList<String>(newFacetOrder));
                        return null;
                    }
                }, false);
            }
        }, AuthenticationUtil.getSystemUserName());

        if (logger.isDebugEnabled())
        {
            logger.debug("The facets order [" + newFacetOrder + "] have been persisted.");
        }
    }

    private Map<String, SolrFacetProperties> getPersistedFacetProperties()
    {
        List<ChildAssociationRef> list = nodeService.getChildAssocs(getFacetsRoot());

        Map<String, SolrFacetProperties> facets = new HashMap<>(list.size());
        for (ChildAssociationRef associationRef : list)
        {
            SolrFacetProperties fp = getFacetProperties(associationRef.getChildRef());
            facets.put(fp.getFilterID(), fp);
        }
        return facets;
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // nothing to do
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        SolrFacetProperties fp = getFacetProperties(childAssocRef.getChildRef());
        this.facetNodeRefCache.put(fp.getFilterID(), childAssocRef.getChildRef());

        // We must also add the new filterID to the facetOrder property.
        final NodeRef facetsRoot = getFacetsRoot();

        @SuppressWarnings("unchecked")
        ArrayList<String> facetOrder = (ArrayList<String>) nodeService.getProperty(facetsRoot, SolrFacetModel.PROP_FACET_ORDER);
        if (facetOrder == null)
        {
            List<SolrFacetProperties> facets = getFacets();
            facetOrder = new ArrayList<String>(facets.size());
            for (SolrFacetProperties facet : facets)
            {
                facetOrder.add(facet.getFilterID());
            }
        }
        // We'll put it at the end (arbitrarily).
        facetOrder.add(fp.getFilterID());

        nodeService.setProperty(facetsRoot, SolrFacetModel.PROP_FACET_ORDER, facetOrder);
    }

    @Override
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        String filterID = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        this.facetNodeRefCache.remove(filterID);

        // We must also remove the filterID from the facetOrder property.
        final NodeRef facetsRoot = getFacetsRoot();

        @SuppressWarnings("unchecked")
        ArrayList<String> facetOrder = (ArrayList<String>) nodeService.getProperty(facetsRoot, SolrFacetModel.PROP_FACET_ORDER);

        if (facetOrder.remove(filterID))
        {
            nodeService.setProperty(facetsRoot, SolrFacetModel.PROP_FACET_ORDER, facetOrder);
        }
    }

    /**
     * Gets a map containing the facet's custom properties
     * 
     * @return Map<QName, Serializable> map containing the custom properties of the facet
     */
    private Map<QName, Serializable> getFacetCustomProperties(Map<QName, Serializable> properties)
    {
        Map<QName, Serializable> customProperties = new HashMap<QName, Serializable>(5);

        for (Map.Entry<QName, Serializable> entry : properties.entrySet())
        {
            if (SolrFacetModel.SOLR_FACET_CUSTOM_PROPERTY_URL.equals(entry.getKey().getNamespaceURI()))
            {
                Serializable values = entry.getValue();
                if (SolrFacetModel.PROP_EXTRA_INFORMATION.equals(entry.getKey()) && values instanceof List)
                {

                    @SuppressWarnings("unchecked")
                    List<CustomProperties> list = (List<CustomProperties>) values;
                    for (CustomProperties cp : list)
                    {
                        customProperties.put(cp.getName(), cp.getValue());
                    }
                }
                else
                {
                    customProperties.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return customProperties;
    }

    @Override public void reorderFacets(List<String> facetIds)
    {
        // We need to validate the provided facet IDs
        if (facetIds == null)        { throw new NullPointerException("Illegal null facetIds"); }
        else if (facetIds.isEmpty()) { throw new MissingFacetId("Illegal empty facetIds"); }
        else
        {
            final List<SolrFacetProperties> existingFacets = getFacets();
            
            final Map<String, SolrFacetProperties> sortedFacets = new LinkedHashMap<>(); // maintains insertion order
            for (String facetId : facetIds)
            {
                SolrFacetProperties facet = getFacet(facetId);
                
                if (facet == null)
                {
                    throw new UnrecognisedFacetId("Cannot reorder facets as ID not recognised:", facetId);
                }
                else if (sortedFacets.containsKey(facetId))
                {
                    throw new DuplicateFacetId("Cannot reorder facets as sequence contains duplicate entry for ID:", facetId);
                }
                else
                {
                    sortedFacets.put(facetId, facet);
                }
            }
            if (existingFacets.size() != sortedFacets.size())
            {
                throw new IllegalArgument("Cannot reorder facets. Expected " + existingFacets.size() +
                                          " IDs but only received " + sortedFacets.size());
            }
            
            // We can now safely apply the updates to the facet ID sequence.
            //
            // Put them in an ArrayList to ensure the collection is Serializable.
            // The alternative is changing the service API to look like <T extends Serializable & List<String>>
            // which is a bit verbose for an API.
            ArrayList<String> serializableProp = new ArrayList<>(facetIds);
            nodeService.setProperty(getFacetsRoot(), SolrFacetModel.PROP_FACET_ORDER, serializableProp);
        }
    }
    
    @Override public Set<FacetablePropertyData> getFacetableProperties()
    {
        final Set<FacetablePropertyData> result = new HashSet<>();
        
        final List<QName> allContentClasses = CollectionUtils.flatten(dictionaryService.getAllAspects(), dictionaryService.getAllTypes());
        
        for (QName contentClass : allContentClasses)
        {
            result.addAll(getFacetableProperties(contentClass));
        }
        
        return result;
    }
    
    @Override public Set<FacetablePropertyData> getFacetableProperties(QName contentClass)
    {
        final Set<FacetablePropertyData> result = new HashSet<>();
        
        final Map<QName, PropertyDefinition> propertyDefs = dictionaryService.getPropertyDefs(contentClass);
        
        if (propertyDefs != null)
        {
            for (Map.Entry<QName, PropertyDefinition> prop : propertyDefs.entrySet())
            {
                final Facetable propIsFacetable  = prop.getValue().getFacetable();
                
                switch (propIsFacetable)
                {
                case TRUE:
                    result.add(toFacetablePropertyData(prop.getValue()));
                    break;
                case FALSE:
                    // The value is not facetable. Do nothing.
                    break;
                case UNSET:
                    // These values may be facetable.
                    final DataTypeDefinition datatype = prop.getValue().getDataType();
                    if (isNumeric(datatype) || isDateLike(datatype) || isFacetableText(datatype))
                    {
                        result.add(toFacetablePropertyData(prop.getValue()));
                        break;
                    }
                    break;
                default:
                    // This should never happen. If it does, it's a programming error.
                    throw new IllegalStateException("Failed to handle " + Facetable.class.getSimpleName() + " type: " + propIsFacetable);
                }
            }
        }
        
        return result;
    }
    
    /** This method returns a {@link FacetablePropertyData} for the specified {@link PropertyDefinition}. */
    private FacetablePropertyData toFacetablePropertyData(PropertyDefinition propDef)
    {
        String title = propDef.getTitle(dictionaryService);
        return new FacetablePropertyData(propDef, title);
    }
    
    // TODO Consider moving into dictionary code.
    private boolean isNumeric(DataTypeDefinition datatype)
    {
        boolean result;
        try
        {
            Class<?> clazz = Class.forName(datatype.getJavaClassName());
            result = Number.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e)
        {
            result = false;
        }
        return result;
    }
    
    private boolean isDateLike(DataTypeDefinition datatype)
    {
        return DataTypeDefinition.DATE.equals(datatype.getName()) ||
               DataTypeDefinition.DATETIME.equals(datatype.getName());
    }
    
    private boolean isFacetableText(DataTypeDefinition datatype)
    {
        // For now at least, we're excluding MLTEXT
        return DataTypeDefinition.TEXT.equals(datatype.getName());
    }
}
