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

package org.alfresco.repo.search.impl.solr.facet;

import static org.alfresco.repo.security.authentication.AuthenticationUtil.getSystemUserName;

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
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.dictionary.Facetable;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.search.impl.solr.facet.Exceptions.DuplicateFacetId;
import org.alfresco.repo.search.impl.solr.facet.Exceptions.IllegalArgument;
import org.alfresco.repo.search.impl.solr.facet.Exceptions.MissingFacetId;
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
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.collections.CollectionUtils;

/**
 * Solr Facet Service Implementation.
 *
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class SolrFacetServiceImpl extends AbstractLifecycleBean
        implements SolrFacetService,
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
    private Repository repositoryHelper;
    private String facetsRootXPath;
    private String facetsRootChildName;

    private ImporterBootstrap importerBootstrap;
    private Properties bootstrapView;

    private SimpleCache<String, Object> singletonCache; // eg. for facetsHomeNodeRef
    private final String KEY_FACETS_HOME_NODEREF = "key.facetshome.noderef";
    private SimpleCache<String, NodeRef> facetNodeRefCache; // for filterID to nodeRef lookup
    private ConcurrentMap<String, SolrFacetProperties> defaultFacetsMap = new ConcurrentHashMap<>(10);

    /**
     * @param authorityService
     *            the authorityService to set
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * @param dictionaryService
     *            the dictionaryService to set
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param nodeService
     *            the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param namespaceService
     *            the namespaceService to set
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @param searchService
     *            the searchService to set
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * @param retryingTransactionHelper
     *            the retryingTransactionHelper to set
     */
    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    /**
     * @param behaviourFilter
     *            the behaviourFilter to set
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    /**
     * @param policyComponent
     *            the policyComponent to set
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public void setRepositoryHelper(Repository repository)
    {
        this.repositoryHelper = repository;
    }

    /**
     * @param facetConfig
     *            the facetConfig to set
     */
    public void setFacetConfig(SolrFacetConfig facetConfig)
    {
        this.facetConfig = facetConfig;
    }

    /**
     * @param facetsRootXPath
     *            the facetsRootXPath to set
     */
    public void setFacetsRootXPath(String facetsRootXPath)
    {
        this.facetsRootXPath = facetsRootXPath;
    }

    public void setFacetsRootChildName(String facetsRootChildName)
    {
        this.facetsRootChildName = facetsRootChildName;
    }

    public void setImporterBootstrap(ImporterBootstrap importer)
    {
        this.importerBootstrap = importer;
    }

    public void setBootstrapView(Properties bootstrapView)
    {
        this.bootstrapView = bootstrapView;
    }

    /**
     * @param singletonCache
     *            the singletonCache to set
     */
    public void setSingletonCache(SimpleCache<String, Object> singletonCache)
    {
        this.singletonCache = singletonCache;
    }

    /**
     * @param facetNodeRefCache
     *            the facetNodeRefCache to set
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

        final NodeRef facetsRoot = getFacetsRoot();
        if (facetsRoot != null)
        {
            for (ChildAssociationRef ref : nodeService.getChildAssocs(facetsRoot))
            {
                // MNT-13812 Check that child has facetField type
                if (nodeService.getType(ref.getChildRef()).equals(SolrFacetModel.TYPE_FACET_FIELD))
                {
                    result.add(getFacetProperties(ref.getChildRef()));
                }
            }
        }

        // add the default filters
        result.addAll(defaultFacetsMap.values());

        return new ArrayList<>(result);
    }

    /** Gets the filter IDs in display order. Will not return {@code null}. */
    public List<String> getFacetOrder()
    {
        final NodeRef facetsRoot = getFacetsRoot();

        return facetsRoot == null ? new ArrayList<>(facetConfig.getDefaultFacets().keySet()) : (List<String>) nodeService.getProperty(facetsRoot, SolrFacetModel.PROP_FACET_ORDER);
    }

    @Override
    public SolrFacetProperties getFacet(String filterID)
    {
        /* Note: There is no need to worry about the state of the SolrFacetProperties returned from facetConfig (getDefaultLoadedFacet), as if the FP has been modified, then we'll get it from the nodeService. */
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

            if (facetRoot != null)
            {
                facetNodeRef = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>() {
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
        }

        return facetNodeRef;
    }

    /**
     * Gets the {@link SolrFacetProperties} stored on the specified {@link NodeRef}.
     * 
     * @throws org.alfresco.service.cmr.repository.InvalidNodeRefException
     *             if the nodeRef does not exist.
     */
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
        if (defaultFacet == null)
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
        NodeRef facetRoot = getFacetsRoot();
        if (facetRoot == null)
        {
            facetRoot = createFacetsRootFolder();
        }
        final NodeRef finalFacetRoot = facetRoot;

        return AuthenticationUtil.runAs(new RunAsWork<NodeRef>() {
            @Override
            public NodeRef doWork() throws Exception
            {
                return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>() {
                    public NodeRef execute() throws Exception
                    {
                        behaviourFilter.disableBehaviour(finalFacetRoot, ContentModel.ASPECT_AUDITABLE);
                        try
                        {
                            Map<QName, Serializable> properties = createNodeProperties(facetProperties);
                            // We don't want the node to be indexed
                            properties.put(ContentModel.PROP_IS_INDEXED, false);
                            NodeRef ref = nodeService.createNode(finalFacetRoot, ContentModel.ASSOC_CONTAINS,
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
                            behaviourFilter.enableBehaviour(finalFacetRoot, ContentModel.ASPECT_AUDITABLE);
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
        Map<QName, Serializable> properties = new HashMap<>(15);

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

    /**
     * Gets the {@link NodeRef} of the {@code srft:facets} folder, if it exists.
     * 
     * @return the {@link NodeRef} if it exists, else {@code null}.
     */
    public NodeRef getFacetsRoot()
    {
        NodeRef facetHomeRef = (NodeRef) singletonCache.get(KEY_FACETS_HOME_NODEREF);
        if (facetHomeRef == null)
        {
            facetHomeRef = AuthenticationUtil.runAs(new RunAsWork<NodeRef>() {
                public NodeRef doWork() throws Exception
                {
                    return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>() {
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

            if (facetHomeRef != null)
            {
                singletonCache.put(KEY_FACETS_HOME_NODEREF, facetHomeRef);
            }
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
        for (Entry<String, SolrFacetProperties> entry : persistedProperties.entrySet())
        {
            final String facetId = entry.getKey();
            /* If the default facet has been removed from the config file and the facet was persisted as its property was modified, then, the persisted node needs to be deleted. This should be done to avoid errors when loading the facets. Also, as all the properties of the facet may not have been persisted and the default facet doesn't exist anymore, there is no way of merging the non-persisted properties. */
            if (entry.getValue().isDefault() && !defaultFP.containsKey(facetId))
            {
                AuthenticationUtil.runAs(new RunAsWork<Void>() {
                    @Override
                    public Void doWork() throws Exception
                    {
                        return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
                            public Void execute() throws Exception
                            {
                                deleteFacet(facetId);

                                logger.info("Deleted [" + facetId + "] node, as the filter has been removed from the config file!");
                                return null;
                            }
                        }, false);
                    }
                }, AuthenticationUtil.getSystemUserName());
            }
            else
            {
                // The persisted facets will override the default facets
                mergedMap.put(facetId, entry.getValue());
            }
        }

        final List<String> facetOrder = getFacetOrder();
        // Sort the merged maps
        Comparator<Entry<String, SolrFacetProperties>> entryComparator = CollectionUtils.toEntryComparator(new SolrFacetComparator(facetOrder));
        Map<String, SolrFacetProperties> sortedMap = CollectionUtils.sortMapByValue(mergedMap, entryComparator);

        if (logger.isDebugEnabled() && persistedProperties.size() > 0)
        {
            logger.debug("The facets [" + persistedProperties + "] have overridden their matched default facets.");
        }

        final Set<String> newFacetOrder = (facetOrder == null) ? new LinkedHashSet<String>(sortedMap.size()) : new LinkedHashSet<>(facetOrder);

        for (SolrFacetProperties fp : sortedMap.values())
        {
            newFacetOrder.add(fp.getFilterID());
        }

        AuthenticationUtil.runAs(new RunAsWork<Void>() {
            @Override
            public Void doWork() throws Exception
            {
                return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
                    public Void execute() throws Exception
                    {
                        reorderFacets(new ArrayList<>(newFacetOrder));
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

    /** Gets the persisted {@link SolrFacetProperties} if there are any, else an empty map. */
    private Map<String, SolrFacetProperties> getPersistedFacetProperties()
    {
        final NodeRef facetsRoot = getFacetsRoot();

        Map<String, SolrFacetProperties> facets = new HashMap<>();

        final List<ChildAssociationRef> list = facetsRoot == null ? new ArrayList<ChildAssociationRef>() : nodeService.getChildAssocs(facetsRoot);

        for (ChildAssociationRef associationRef : list)
        {
            // MNT-13812 Check that child has facetField type
            if (nodeService.getType(associationRef.getChildRef()).equals(SolrFacetModel.TYPE_FACET_FIELD))
            {
                SolrFacetProperties fp = getFacetProperties(associationRef.getChildRef());
                facets.put(fp.getFilterID(), fp);
            }
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
     * @return map containing the custom properties of the facet
     */
    private Map<QName, Serializable> getFacetCustomProperties(Map<QName, Serializable> properties)
    {
        Map<QName, Serializable> customProperties = new HashMap<>(5);

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

    @Override
    public void reorderFacets(List<String> facetIds)
    {
        // We need to validate the provided facet IDs
        if (facetIds == null)
        {
            throw new NullPointerException("Illegal null facetIds");
        }
        else if (facetIds.isEmpty())
        {
            throw new MissingFacetId("Illegal empty facetIds");
        }
        else
        {
            final List<SolrFacetProperties> existingFacets = getFacets();

            final Map<String, SolrFacetProperties> sortedFacets = new LinkedHashMap<>(); // maintains insertion order
            final List<String> removedFacetIds = new ArrayList<>();
            for (String facetId : facetIds)
            {
                final SolrFacetProperties facet = getFacet(facetId);

                if (facet == null)
                {
                    // ACE-3083
                    logger.warn("Facet with [" + facetId + "] ID does not exist. Removing it from the facets' ordering list");
                    removedFacetIds.add(facetId);
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
            if (removedFacetIds.size() > 0)
            {
                boolean result = serializableProp.removeAll(removedFacetIds);
                if (result)
                {
                    logger.info("Removed " + removedFacetIds + " from the facets' ordering list.");
                }
            }
            NodeRef facetsRoot = getFacetsRoot();

            if (facetsRoot == null)
            {
                facetsRoot = createFacetsRootFolder();
            }
            nodeService.setProperty(facetsRoot, SolrFacetModel.PROP_FACET_ORDER, serializableProp);
        }
    }

    private NodeRef createFacetsRootFolder()
    {
        return AuthenticationUtil.runAs(new RunAsWork<NodeRef>() {
            @Override
            public NodeRef doWork() throws Exception
            {
                final NodeRef companyHome = repositoryHelper.getCompanyHome();
                final QName appModel = QName.createQName("http://www.alfresco.org/model/application/1.0", "dictionary");
                final NodeRef dataDict = getSingleChildNodeRef(companyHome, appModel);

                // The name of the child-assoc to the facets root folder.
                final QName facetsRootAssocQName = QName.createQName(facetsRootChildName, namespaceService);

                NodeRef result = getSingleChildNodeRef(dataDict, facetsRootAssocQName);

                if (result == null)
                {
                    List<Properties> singletonList = new ArrayList<>();
                    singletonList.add(bootstrapView);
                    importerBootstrap.setBootstrapViews(singletonList);
                    importerBootstrap.setUseExistingStore(true);
                    importerBootstrap.bootstrap();

                    // Now to get the NodeRef we just imported. (Not using SOLR to avoid consistency effects.)
                    result = getSingleChildNodeRef(dataDict, facetsRootAssocQName);
                }

                if (logger.isDebugEnabled())
                {
                    logger.debug("Created Facets Root Folder: " + result);
                }

                return result;
            }
        }, getSystemUserName());
    }

    /**
     * Gets a child NodeRef under the specified parent NodeRef linked by a child-assoc of the specified name.
     *
     * @param parent
     *            the parent whose child is sought.
     * @param assocName
     *            the name of the child-association.
     * @return the NodeRef of the requested child, if it exists. null if there is no match.
     */
    private NodeRef getSingleChildNodeRef(NodeRef parent, QName assocName)
    {
        final List<ChildAssociationRef> assocs = nodeService.getChildAssocs(parent,
                RegexQNamePattern.MATCH_ALL,
                assocName, true);
        final NodeRef result;

        if (assocs == null || assocs.isEmpty())
        {
            result = null;
        }
        else if (assocs.size() > 1)
        {
            final StringBuilder msg = new StringBuilder();
            msg.append("Expected exactly one child node at: ")
                    .append(parent).append("/").append(assocName)
                    .append(" but found ")
                    .append(assocs == null ? "<null assocs>" : assocs.size());
            if (logger.isErrorEnabled())
            {
                logger.error(msg.toString());
            }

            result = assocs.get(0).getChildRef();
        }
        else
        {
            result = assocs.get(0).getChildRef();
        }

        return result;
    }

    @Override
    public List<PropertyDefinition> getFacetableProperties()
    {
        final List<PropertyDefinition> result = new ArrayList<>();

        final List<QName> allContentClasses = CollectionUtils.flatten(dictionaryService.getAllAspects(), dictionaryService.getAllTypes());

        for (QName contentClass : allContentClasses)
        {
            result.addAll(getFacetableProperties(contentClass));
        }

        return result;
    }

    @Override
    public List<PropertyDefinition> getFacetableProperties(QName contentClass)
    {
        final List<PropertyDefinition> result = new ArrayList<>();

        final Map<QName, PropertyDefinition> propertyDefs = dictionaryService.getPropertyDefs(contentClass);

        if (propertyDefs != null)
        {
            for (final Map.Entry<QName, PropertyDefinition> prop : propertyDefs.entrySet())
            {
                final PropertyDefinition propDef = prop.getValue();
                if (propDef.isIndexed()) // SHA-1308
                {
                    final Facetable propIsFacetable = propDef.getFacetable();

                    switch (propIsFacetable)
                    {
                    case TRUE:
                        result.add(propDef);
                        break;
                    case FALSE:
                        // The value is not facetable. Do nothing.
                        break;
                    case UNSET:
                        // These values may be facetable.
                        final DataTypeDefinition datatype = propDef.getDataType();
                        if (isNumeric(datatype) || isDateLike(datatype) || isFacetableText(datatype))
                        {
                            result.add(propDef);
                            break;
                        }
                        break;
                    default:
                        // This should never happen. If it does, it's a programming error.
                        throw new IllegalStateException("Failed to handle " + Facetable.class.getSimpleName() + " type: " + propIsFacetable);
                    }
                }
            }
        }

        return result;
    }

    @Override
    public List<SyntheticPropertyDefinition> getFacetableSyntheticProperties()
    {
        final List<SyntheticPropertyDefinition> result = new ArrayList<>();

        final List<QName> allContentClasses = CollectionUtils.flatten(dictionaryService.getAllAspects(), dictionaryService.getAllTypes());

        for (QName contentClass : allContentClasses)
        {
            result.addAll(getFacetableSyntheticProperties(contentClass));
        }

        return result;
    }

    @Override
    public List<SyntheticPropertyDefinition> getFacetableSyntheticProperties(QName contentClass)
    {
        final List<SyntheticPropertyDefinition> result = new ArrayList<>();

        final Map<QName, PropertyDefinition> propertyDefs = dictionaryService.getPropertyDefs(contentClass);

        if (propertyDefs != null)
        {
            for (final Map.Entry<QName, PropertyDefinition> prop : propertyDefs.entrySet())
            {
                final PropertyDefinition propDef = prop.getValue();

                // Only properties of type cm:content can expand to synthetic properties.
                if (DataTypeDefinition.CONTENT.equals(propDef.getDataType().getName()))
                {
                    // We do not want to treat the cm:content property itself as facetable.
                    // It is a content URL whose value is not suitable for facetting.
                    // e.g. 2010/1/22/13/14/6e228904-d5d2-4a99-b7b1-8fe7c03c71f3.bin|mimetype=application/octet-stream|size=728|encoding=UTF-8|locale=en_GB_
                    //
                    // However there are elements within that content URL which *are* facetable and are correctly treated as such by SOLR.
                    // As these are not actually Alfresco content properties, we must return artificial PropertyDefinition objects:
                    result.add(new SyntheticPropertyDefinition(propDef, "size", DataTypeDefinition.LONG));
                    result.add(new SyntheticPropertyDefinition(propDef, "mimetype", DataTypeDefinition.TEXT));
                }
                else
                {
                    // Intentionally empty. Only cm:content's size and mimetype are currently supported.
                }
            }
        }

        return result;
    }

    private boolean isNumeric(DataTypeDefinition datatype)
    {
        boolean result;
        try
        {
            Class<?> clazz = Class.forName(datatype.getJavaClassName());
            result = Number.class.isAssignableFrom(clazz);
        }
        catch (ClassNotFoundException e)
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
