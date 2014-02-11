/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.security.authority;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.ibatis.IdsEntity;
import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.cache.AsynchronouslyRefreshedCache;
import org.alfresco.repo.cache.RefreshableCacheEvent;
import org.alfresco.repo.cache.RefreshableCacheListener;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.cache.TransactionalCache;
import org.alfresco.repo.domain.permissions.AclDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneQueryParser;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.person.PersonServiceImpl;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService.AuthorityFilter;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.BridgeTable;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.ISO9075;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.SearchLanguageConversion;
import org.alfresco.util.registry.NamedObjectRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

public class AuthorityDAOImpl implements AuthorityDAO, NodeServicePolicies.BeforeDeleteNodePolicy, NodeServicePolicies.OnUpdatePropertiesPolicy, RefreshableCacheListener, InitializingBean
{
    private static Log logger = LogFactory.getLog(AuthorityDAOImpl.class);
    
    private static String PARENTS_OF_DELETING_CHILDREN_SET_RESOURCE = "ParentsOfDeletingChildrenSetResource";
    
    private static final NodeRef NULL_NODEREF = new NodeRef("null", "null", "null");
    private static final String CANNED_QUERY_AUTHS_LIST = "authsGetAuthoritiesCannedQueryFactory"; // see authority-services-context.xml
    private static final Collection<AuthorityType> SEARCHABLE_AUTHORITY_TYPES = new LinkedList<AuthorityType>();
    static
    {
        SEARCHABLE_AUTHORITY_TYPES.add(AuthorityType.ROLE);
        SEARCHABLE_AUTHORITY_TYPES.add(AuthorityType.GROUP);
    }
    
    private StoreRef storeRef;
    private NodeService nodeService;
    private NamespacePrefixResolver namespacePrefixResolver;
    private QName qnameAssocSystem;
    private QName qnameAssocAuthorities;
    private QName qnameAssocZones;
    private SearchService searchService;
    private DictionaryService dictionaryService;
    private PersonService personService;
    private TenantService tenantService;
    
    private SimpleCache<Pair<String, String>, NodeRef> authorityLookupCache;
    private SimpleCache<String, Set<String>> userAuthorityCache;
    private SimpleCache<Pair<String, String>, List<ChildAssociationRef>> zoneAuthorityCache;
    private SimpleCache<NodeRef, Pair<Map<NodeRef,String>, List<NodeRef>>> childAuthorityCache;
    private AsynchronouslyRefreshedCache<BridgeTable<String>> authorityBridgeTableCache;
    private SimpleCache<String, Object> singletonCache; // eg. for system container nodeRefs (authorityContainer and zoneContainer)
    private final String KEY_SYSTEMCONTAINER_NODEREF = "key.systemcontainer.noderef";
    /** Limit the number of copies of authority names floating about by keeping them in a pool **/
    private ConcurrentMap<String, String> authorityNamePool = new ConcurrentHashMap<String, String>();
    
    /** The number of authorities in a zone to pre-cache, allowing quick generation of 'first n' results. */
    private int zoneAuthoritySampleSize = 10000;

    private boolean useBridgeTable = true;
    
    private boolean useGetContainingAuthoritiesForIsAuthorityContained = true;
    
    private QNameDAO qnameDAO;
    private CannedQueryDAO cannedQueryDAO;
    private AclDAO aclDao;
    private PolicyComponent policyComponent;
    private NamedObjectRegistry<CannedQueryFactory<?>> cannedQueryRegistry;
    private AuthorityBridgeDAO authorityBridgeDAO;
    
    
    public AuthorityDAOImpl()
    {
        super();
    }

    
    /**
     * Sets number of authorities in a zone to pre-cache, allowing quick generation of 'first n' results and adaption of
     * search technique based on hit rate.
     * 
     * @param zoneAuthoritySampleSize
     *            the zoneAuthoritySampleSize to set
     */
    public void setZoneAuthoritySampleSize(int zoneAuthoritySampleSize)
    {
        this.zoneAuthoritySampleSize = zoneAuthoritySampleSize;
    }

    public void setStoreUrl(String storeUrl)
    {
        this.storeRef = new StoreRef(storeUrl);
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
        qnameAssocSystem = QName.createQName("sys", "system", namespacePrefixResolver);
        qnameAssocAuthorities = QName.createQName("sys", "authorities", namespacePrefixResolver);
        qnameAssocZones = QName.createQName("sys", "zones", namespacePrefixResolver);
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setAuthorityLookupCache(SimpleCache<Pair<String, String>, NodeRef> authorityLookupCache)
    {
        this.authorityLookupCache = authorityLookupCache;
    }
    
    public void setUserAuthorityCache(SimpleCache<String, Set<String>> userAuthorityCache)
    {
        this.userAuthorityCache = userAuthorityCache;
    }
    
    public void setZoneAuthorityCache(SimpleCache<Pair<String, String>, List<ChildAssociationRef>> zoneAuthorityCache)
    {
        this.zoneAuthorityCache = zoneAuthorityCache;
    }

    public void setChildAuthorityCache(SimpleCache<NodeRef, Pair<Map<NodeRef,String>, List<NodeRef>>> childAuthorityCache)
    {
        this.childAuthorityCache = childAuthorityCache;
    }
    
    public void setAuthorityBridgeTableCache(AuthorityBridgeTableAsynchronouslyRefreshedCache authorityBridgeTableCache)
    {
        this.authorityBridgeTableCache = authorityBridgeTableCache;
    }
    
    /**
     * @param useBridgeTable the useBridgeTable to set
     */
    public void setUseBridgeTable(boolean useBridgeTable)
    {
        this.useBridgeTable = useBridgeTable;
    }


    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setSingletonCache(SimpleCache<String, Object> singletonCache)
    {
        this.singletonCache = singletonCache;
    }
    
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    public void setCannedQueryDAO(CannedQueryDAO cannedQueryDAO)
    {
        this.cannedQueryDAO = cannedQueryDAO;
    }

    public void setAclDAO(AclDAO aclDao)
    {
        this.aclDao = aclDao;
    }

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    public void setCannedQueryRegistry(NamedObjectRegistry<CannedQueryFactory<?>> cannedQueryRegistry)
    {
        this.cannedQueryRegistry = cannedQueryRegistry;
    }
    
    /**
     * @param useGetContainingAuthoritiesForHasAuthority the useGetContainingAuthoritiesForHasAuthority to set
     */
    public void setUseGetContainingAuthoritiesForIsAuthorityContained(boolean useGetContainingAuthoritiesForIsAuthorityContained)
    {
        this.useGetContainingAuthoritiesForIsAuthorityContained = useGetContainingAuthoritiesForIsAuthorityContained;
    }
    
    /**
     * @param authorityBridgeDAO the authorityBridgeDAO to set
     */
    public void setAuthorityBridgeDAO(AuthorityBridgeDAO authorityBridgeDAO)
    {
        this.authorityBridgeDAO = authorityBridgeDAO;
    }

    @Override
    public long getPersonCount()
    {
        /* Unboxing accepted.  See CannedQueryDAO javadoc and implementation. */
        Pair<Long, QName> qnamePair = qnameDAO.getQName(ContentModel.TYPE_PERSON);
        if (qnamePair == null)
        {
            // No results
            return 0L;
        }
        
        IdsEntity ids = new IdsEntity();
        ids.setIdOne(qnamePair.getFirst());
        Long personCount = cannedQueryDAO.executeCountQuery("alfresco.query.authorities", "select_AuthorityCount_People", ids);
        if (logger.isDebugEnabled())
        {
            logger.debug("Counted authorities (people): " + personCount);
        }
        return personCount;
    }

    @Override
    public long getGroupCount()
    {
        /* Unboxing accepted.  See CannedQueryDAO javadoc and implementation. */
        Pair<Long, QName> qnamePair = qnameDAO.getQName(ContentModel.TYPE_AUTHORITY_CONTAINER);
        if (qnamePair == null)
        {
            // No results
            return 0L;
        }
        
        IdsEntity ids = new IdsEntity();
        ids.setIdOne(qnamePair.getFirst());
        Long groupCount = cannedQueryDAO.executeCountQuery("alfresco.query.authorities", "select_AuthorityCount_Groups", ids);
        if (logger.isDebugEnabled())
        {
            logger.debug("Counted authorities (groups):" + groupCount);
        }
        return groupCount;
    }
    
    public boolean authorityExists(String name)
    {
        NodeRef ref = getAuthorityOrNull(name);
        return ref != null;
    }

    public void addAuthority(Collection<String> parentNames, String childName)
    {
        Set<NodeRef> parentRefs = new HashSet<NodeRef>(parentNames.size() * 2);
        AuthorityType authorityType = AuthorityType.getAuthorityType(childName);
        boolean isUser = authorityType.equals(AuthorityType.USER);
        boolean notUserOrGroup = !isUser && !authorityType.equals(AuthorityType.GROUP);
        for (String parentName : parentNames)
        {
            NodeRef parentRef = getAuthorityOrNull(parentName);
            if (parentRef == null)
            {
                throw new UnknownAuthorityException("An authority was not found for " + parentName);
            }
            if (notUserOrGroup
                    && !(authorityType.equals(AuthorityType.ROLE) && AuthorityType.getAuthorityType(parentName).equals(
                            AuthorityType.ROLE)))
            {
                throw new AlfrescoRuntimeException("Authorities of the type " + authorityType
                        + " may not be added to other authorities");
            }
            childAuthorityCache.remove(parentRef);
            parentRefs.add(parentRef);
        }
        NodeRef childRef = getAuthorityOrNull(childName);

        if (childRef == null)
        {
            throw new UnknownAuthorityException("An authority was not found for " + childName);
        }

        // Normalize the user name if necessary
        if (isUser)
        {
            childName = (String) nodeService.getProperty(childRef, ContentModel.PROP_USERNAME);
        }

        nodeService.addChild(parentRefs, childRef, ContentModel.ASSOC_MEMBER, QName.createQName("cm", childName,
                namespacePrefixResolver));
        if (isUser)
        {
            userAuthorityCache.remove(childName);
        }
        else
        {
            userAuthorityCache.clear();
            authorityBridgeTableCache.refresh();
        }
    }

    public void createAuthority(String name, String authorityDisplayName, Set<String> authorityZones)
    {
        HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
        //MNT-9794 fix. ContentModel.PROP_NAME is added for preventing the generation of new uid for every new authority
        // nodes with the duplicated names in 'child_node_name' field into 'alf_child_assoc' table
        props.put(ContentModel.PROP_NAME, name);
        props.put(ContentModel.PROP_AUTHORITY_NAME, name);
        props.put(ContentModel.PROP_AUTHORITY_DISPLAY_NAME, authorityDisplayName);
        NodeRef childRef;
        NodeRef authorityContainerRef = getAuthorityContainer();
        childRef = nodeService.createNode(authorityContainerRef, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", name, namespacePrefixResolver),
                ContentModel.TYPE_AUTHORITY_CONTAINER, props).getChildRef();
        if (authorityZones != null)
        {
            Set<NodeRef> zoneRefs = new HashSet<NodeRef>(authorityZones.size() * 2);
            String currentUserDomain = tenantService.getCurrentUserDomain();
            for (String authorityZone : authorityZones)
            {
                zoneRefs.add(getOrCreateZone(authorityZone));
                zoneAuthorityCache.remove(new Pair<String, String>(currentUserDomain, authorityZone));
            }
            zoneAuthorityCache.remove(new Pair<String, String>(currentUserDomain, null));
            nodeService.addChild(zoneRefs, childRef, ContentModel.ASSOC_IN_ZONE, QName.createQName("cm", name, namespacePrefixResolver));
        }
        authorityLookupCache.put(cacheKey(name), childRef);
    }
    
    private Pair<String, String> cacheKey(String authorityName)
    {
        String tenantDomain = AuthorityType.getAuthorityType(authorityName) == AuthorityType.USER ? tenantService.getDomain(authorityName) : tenantService.getCurrentUserDomain();
        return new Pair<String, String>(tenantDomain, getPooledName(authorityName));
    }
    
    private String getPooledName(String authorityName)
    {
        String pooledName = authorityNamePool.putIfAbsent(authorityName, authorityName);
        return pooledName == null ? authorityName : pooledName;
    }
    
    public void deleteAuthority(String name)
    {
        NodeRef nodeRef = getAuthorityOrNull(name);
        if (nodeRef == null)
        {
            throw new UnknownAuthorityException("An authority was not found for " + name);
        }
        String currentUserDomain = tenantService.getCurrentUserDomain();
        for (String authorityZone : getAuthorityZones(name))
        {
            zoneAuthorityCache.remove(new Pair<String, String>(currentUserDomain, authorityZone));
        }
        zoneAuthorityCache.remove(new Pair<String, String>(currentUserDomain, null));
        removeParentsFromChildAuthorityCache(nodeRef, false);
        authorityLookupCache.remove(cacheKey(name));
        userAuthorityCache.clear();
        authorityBridgeTableCache.refresh();

        nodeService.deleteNode(nodeRef);
    }
    
    public PagingResults<AuthorityInfo> getAuthoritiesInfo(AuthorityType type, String zoneName, String displayNameFilter, String sortBy, boolean sortAscending, PagingRequest pagingRequest)
    {
        checkGetAuthorityParams(type, zoneName, pagingRequest);
        
        return getAuthoritiesImpl(type, getContainerRef(zoneName), displayNameFilter, sortBy, sortAscending, pagingRequest, new PagingResultsAuthorityInfo());
    }

    public PagingResults<String> getAuthorities(AuthorityType type, String zoneName, String displayNameFilter, boolean sortByDisplayName, boolean sortAscending, PagingRequest pagingRequest)
    {
        checkGetAuthorityParams(type, zoneName, pagingRequest);
        
        if ((zoneName == null) && (type.equals(AuthorityType.USER)))
        {
            return getUserAuthoritiesImpl(displayNameFilter, sortByDisplayName, sortAscending, pagingRequest);
        }
        
        return getAuthoritiesImpl(type, getContainerRef(zoneName), displayNameFilter, (sortByDisplayName ? GetAuthoritiesCannedQuery.DISPLAY_NAME : null), sortAscending, pagingRequest, new PagingResultsString());
    }

    private void checkGetAuthorityParams(AuthorityType type, String zoneName,
            PagingRequest pagingRequest)
    {
        ParameterCheck.mandatory("pagingRequest", pagingRequest);
        
        if ((type == null) && (zoneName == null))
        {
            throw new IllegalArgumentException("Type and/or zoneName required - both cannot be null");
        }
    }

    private NodeRef getContainerRef(String zoneName)
    {
        NodeRef containerRef = null;
        if (zoneName != null)
        {
            containerRef = getZone(zoneName);
            if (containerRef == null)
            {
                throw new UnknownAuthorityException("A zone was not found for " + zoneName); 
            }
        }
        else
        {
            containerRef = getAuthorityContainer();
        }
        return containerRef;
    }
    
    private <T> PagingResults<T> getAuthoritiesImpl(AuthorityType type, NodeRef containerRef, String displayNameFilter, String sortBy, boolean sortAscending, PagingRequest pagingRequest, AbstractPagingResults<T> finalResults)
    {
        Long start = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);
        
        if (type != null)
        {
            switch (type)
            {
            case GROUP:
            case ROLE:
            case USER:
                // drop through
                break;
            default:
                throw new UnsupportedOperationException("Unexpected authority type: "+type);
            }
        }
        
        // get canned query
        GetAuthoritiesCannedQueryFactory getAuthoritiesCannedQueryFactory = (GetAuthoritiesCannedQueryFactory)cannedQueryRegistry.getNamedObject(CANNED_QUERY_AUTHS_LIST);
        CannedQuery<AuthorityInfo> cq = getAuthoritiesCannedQueryFactory.getCannedQuery(type, containerRef, displayNameFilter, sortBy, sortAscending, pagingRequest);
        
        // execute canned query
        final CannedQueryResults<AuthorityInfo> results = cq.execute();

        finalResults.setResults(results);
        
        if (start != null)
        {
            int cnt = finalResults.getPage().size();
            int skipCount = pagingRequest.getSkipCount();
            int maxItems = pagingRequest.getMaxItems();
            boolean hasMoreItems = finalResults.hasMoreItems();
            int pageNum = (skipCount / maxItems) + 1;
            
            logger.debug("getAuthoritiesByType: "+cnt+" items in "+(System.currentTimeMillis()-start)+" msecs [type="+type+",pageNum="+pageNum+",skip="+skipCount+",max="+maxItems+",hasMorePages="+hasMoreItems+",filter="+displayNameFilter+"]");
        }
        
        return finalResults;
    }
    
    // delegate to PersonService.getPeople
    private PagingResults<String> getUserAuthoritiesImpl(String displayNameFilter, boolean sortByDisplayName, boolean sortAscending, PagingRequest pagingRequest)
    {
        List<Pair<QName,String>> filter = null;
        if (displayNameFilter != null)
        {
            filter = new ArrayList<Pair<QName,String>>();
            filter.add(new Pair<QName, String>(ContentModel.PROP_USERNAME, displayNameFilter));
        }
        
        List<Pair<QName,Boolean>> sort = null;
        if (sortByDisplayName)
        {
            sort = new ArrayList<Pair<QName,Boolean>>();
            sort.add(new Pair<QName, Boolean>(ContentModel.PROP_USERNAME, sortAscending));
        }
        
        final PagingResults<PersonInfo> ppr = personService.getPeople(filter, true, sort, pagingRequest);
        
        List<PersonInfo> result = ppr.getPage();
        final List<String> auths = new ArrayList<String>(result.size());
        
        for (PersonInfo person : result)
        {
            auths.add(getPooledName(person.getUserName()));
        }
        
        return new PagingResults<String>()
        {
            @Override
            public String getQueryExecutionId()
            {
                return ppr.getQueryExecutionId();
            }
            @Override
            public List<String> getPage()
            {
                return auths;
            }
            @Override
            public boolean hasMoreItems()
            {
                return ppr.hasMoreItems();
            }
            @Override
            public Pair<Integer, Integer> getTotalResultCount()
            {
                return ppr.getTotalResultCount();
            }
        };
    }
    
    public Set<String> getRootAuthorities(AuthorityType type, String zoneName)
    {
        NodeRef container = (zoneName == null ? getAuthorityContainer() : getZone(zoneName));
        if (container == null)
        {
            // The zone doesn't even exist so there are no root authorities
            return Collections.emptySet();
        }
        
        return getRootAuthoritiesUnderContainer(container, type);
    }
    
    public Set<String> findAuthorities(AuthorityType type, String parentAuthority, boolean immediate,
            String displayNamePattern, String zoneName)
    {
        Long start = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);
        
        Pattern pattern = displayNamePattern == null ? null : Pattern.compile(SearchLanguageConversion.convert(
                SearchLanguageConversion.DEF_LUCENE, SearchLanguageConversion.DEF_REGEX, displayNamePattern),
                Pattern.CASE_INSENSITIVE);
        
        // Use SQL to determine root authorities
        Set<String> rootAuthorities = null;
        if (parentAuthority == null && immediate)
        {
            rootAuthorities = getRootAuthorities(type, zoneName);
            if (pattern == null)
            {
                if (start != null)
                {
                    logger.debug("findAuthorities (rootAuthories): "+rootAuthorities.size()+" items in "+(System.currentTimeMillis()-start)+" msecs [type="+type+",zone="+zoneName+"]");
                }
                
                return rootAuthorities;
            }
        }
        
        // Use a Lucene search for other criteria
        Set<String> authorities = new TreeSet<String>();
        SearchParameters sp = new SearchParameters();
        sp.addStore(this.storeRef);
        sp.setLanguage("lucene");
        StringBuilder query = new StringBuilder(500);
        if (type == null || type == AuthorityType.USER)
        {
            if (type == null)
            {
                query.append("((");
            }
            query.append("TYPE:\"").append(ContentModel.TYPE_PERSON).append("\"");
            if (displayNamePattern != null)
            {
                query.append(" AND @").append(
                        AbstractLuceneQueryParser.escape("{" + ContentModel.PROP_USERNAME.getNamespaceURI() + "}"
                                + ISO9075.encode(ContentModel.PROP_USERNAME.getLocalName()))).append(":\"").append(
                                        AbstractLuceneQueryParser.escape(displayNamePattern)).append("\"");

            }
            if (type == null)
            {
                query.append(") OR (");
            }            
        }
        if (type != AuthorityType.USER)
        {
            query.append("TYPE:\"").append(ContentModel.TYPE_AUTHORITY_CONTAINER).append("\"");
            if (displayNamePattern != null)
            {
                query.append(" AND (");
                if (!displayNamePattern.startsWith("*"))
                {
                    // Allow for the appropriate type prefix in the authority name
                    Collection<AuthorityType> authorityTypes = type == null ? SEARCHABLE_AUTHORITY_TYPES
                            : Collections.singleton(type);
                    boolean first = true;
                    for (AuthorityType subType: authorityTypes)
                    {
                        if (first)
                        {
                            first = false;
                        }
                        else
                        {
                            query.append(" OR ");
                        }
                        query.append("@").append(
                        AbstractLuceneQueryParser.escape("{" + ContentModel.PROP_AUTHORITY_NAME.getNamespaceURI() + "}"
                                        + ISO9075.encode(ContentModel.PROP_AUTHORITY_NAME.getLocalName()))).append(":\"");
                        query.append(getName(subType, AbstractLuceneQueryParser.escape(displayNamePattern))).append("\"");
                        
                    }
                }
                else
                {
                    query.append("@").append(
                            AbstractLuceneQueryParser.escape("{" + ContentModel.PROP_AUTHORITY_NAME.getNamespaceURI() + "}"
                                    + ISO9075.encode(ContentModel.PROP_AUTHORITY_NAME.getLocalName()))).append(":\"");
                    query.append(getName(type, AbstractLuceneQueryParser.escape(displayNamePattern))).append("\"");
                }
                query.append(" OR @").append(
                        AbstractLuceneQueryParser.escape("{" + ContentModel.PROP_AUTHORITY_DISPLAY_NAME.getNamespaceURI() + "}"
                                + ISO9075.encode(ContentModel.PROP_AUTHORITY_DISPLAY_NAME.getLocalName()))).append(
                        ":\"").append(AbstractLuceneQueryParser.escape(displayNamePattern)).append("\")");
            }
            if (type == null)
            {
                query.append("))");
            }            
        }
        if (parentAuthority != null)
        {
           if(immediate)
           {
               // use PARENT
               NodeRef parentAuthorityNodeRef = getAuthorityNodeRefOrNull(parentAuthority); 
               if(parentAuthorityNodeRef != null)
               {
                   query.append(" AND PARENT:\"").append(AbstractLuceneQueryParser.escape(parentAuthorityNodeRef.toString())).append("\""); 
               }
               else
               {
                   throw new UnknownAuthorityException("An authority was not found for " + parentAuthority);
               }     
           }
           else
           {
               // use PATH
               query.append(" AND PATH:\"/sys:system/sys:authorities/cm:").append(ISO9075.encode(parentAuthority));
               query.append("//*\"");
           }
        }
        if (zoneName != null)
        {
            // Zones are all direct links to those within so it is safe to use PARENT to look them up
            NodeRef zoneNodeRef = getZone(zoneName);
            if (zoneNodeRef != null)
            {
                query.append(" AND PARENT:\"").append(AbstractLuceneQueryParser.escape(zoneNodeRef.toString())).append("\"");
            } 
            else
            {
                throw new UnknownAuthorityException("A zone was not found for " + zoneName);
            } 
        }
        sp.setQuery(query.toString());
        sp.setMaxItems(100);
        ResultSet rs = null;
        try
        {
            rs = searchService.query(sp);
            
            for (ResultSetRow row : rs)
            {
                NodeRef nodeRef = row.getNodeRef();
                QName idProp = dictionaryService.isSubClass(nodeService.getType(nodeRef),
                        ContentModel.TYPE_AUTHORITY_CONTAINER) ? ContentModel.PROP_AUTHORITY_NAME
                        : ContentModel.PROP_USERNAME;
                addAuthorityNameIfMatches(authorities, DefaultTypeConverter.INSTANCE.convert(String.class, nodeService
                        .getProperty(nodeRef, idProp)), type, pattern);
            }
            
            // If we asked for root authorities, we must do an intersection with the set of root authorities
            if (rootAuthorities != null)
            {
                authorities.retainAll(rootAuthorities);
            }
            
            if (start != null)
            {
                logger.debug("findAuthorities: "+authorities.size()+" items in "+(System.currentTimeMillis()-start)+" msecs [type="+type+",zone="+zoneName+",parent="+parentAuthority+",immediate="+immediate+",filter="+displayNamePattern+"]");
            }
            
            return authorities;
        }
        finally
        {
            if (rs != null)
            {
                rs.close();
            }
        }
    }
    
    public Set<String> getContainedAuthorities(AuthorityType type, String parentName, boolean immediate)
    {
        AuthorityType parentAuthorityType = AuthorityType.getAuthorityType(parentName); 
        if (parentAuthorityType == AuthorityType.USER)
        {
            // Users never contain other authorities
            return Collections.<String> emptySet();
        }
        else
        {
            NodeRef nodeRef = getAuthorityOrNull(parentName);
            if (nodeRef == null)
            {
                throw new UnknownAuthorityException("An authority was not found for " + parentName);
            }
            
            Set<String> authorities = new TreeSet<String>();
            listAuthorities(type, nodeRef, authorities, false, !immediate, false);
            return authorities;
        }
    }

    public void removeAuthority(String parentName, String childName)
    {
        NodeRef parentRef = getAuthorityOrNull(parentName);
        if (parentRef == null)
        {
            throw new UnknownAuthorityException("An authority was not found for " + parentName);
        }
        NodeRef childRef = getAuthorityOrNull(childName);
        if (childRef == null)
        {
            throw new UnknownAuthorityException("An authority was not found for " + childName);
        }
        nodeService.removeChild(parentRef, childRef);
        childAuthorityCache.remove(parentRef);
        if (AuthorityType.getAuthorityType(childName) == AuthorityType.USER)
        {
            userAuthorityCache.remove(childName);
        }
        else
        {
            userAuthorityCache.clear();
            authorityBridgeTableCache.refresh();
        }
    }

    /**
     * Explicitly use the bridge table to list authorities.
     */
    private void listAuthoritiesByBridgeTable(Set<String> authorities, String name)
    {
        BridgeTable<String> bridgeTable = authorityBridgeTableCache.get();
        
        AuthorityType type = AuthorityType.getAuthorityType(name);
        switch(type)
        {
        case WILDCARD:
            // Dual use of the enum means that this value should not be received
            logger.warn("Found an authority with type '" + AuthorityType.WILDCARD + "': " + name);
        case ADMIN:
        case GUEST:
        case USER:
        case EVERYONE:
            NodeRef authRef = getAuthorityOrNull(name);
            List<AuthorityBridgeLink> parents = authorityBridgeDAO.getDirectAuthoritiesForUser(authRef);
            for(AuthorityBridgeLink parent : parents)
            {
                authorities.add(getPooledName(parent.getParentName()));
                for (String ancestor : bridgeTable.getAncestors(parent.getParentName()))
                {
                    authorities.add(getPooledName(ancestor));
                }
            }
            break;
        case GROUP:
        case OWNER:
        case ROLE:
            for (String ancestor : bridgeTable.getAncestors(name))
            {
                authorities.add(getPooledName(ancestor));
            }
            break;
        }        
    }
    
    @Override
    public Set<String> getContainingAuthorities(AuthorityType type, String name, boolean immediate)
    {
        // Optimize for the case where we want all the authorities that a user belongs to
        if (!immediate && AuthorityType.getAuthorityType(name) == AuthorityType.USER)
        {
            // Get the unfiltered set of authorities from the cache or generate it
            Set<String> authorities = userAuthorityCache.get(name);
            if (authorities == null)
            {
                authorities = new TreeSet<String>();
                if(useBridgeTable)
                {
                    listAuthoritiesByBridgeTable(authorities, name);
                }
                else
                {
                    listAuthorities(null, name, authorities, true, true);
                }
                // Add the set back to the cache.  If the value is locked then nothing will happen.
                userAuthorityCache.put(name, Collections.unmodifiableSet(authorities));
            }
            // If we wanted the unfiltered set we are done
            if (type == null)
            {
                return authorities;
            }
            // Apply the filtering by type
            Set<String> filteredAuthorities = new TreeSet<String>();
            for (String authority : authorities)
            {
                addAuthorityNameIfMatches(filteredAuthorities, authority, type);
            }
            return filteredAuthorities;
        }
        // Otherwise, crawl the DB for the answer
        else
        {
            Set<String> authorities = new TreeSet<String>();
            listAuthorities(type, name, authorities, true, !immediate);
            return authorities;
        }
    }

    public Set<String> getContainingAuthoritiesInZone(AuthorityType type, String authority, final String zoneName, AuthorityFilter filter, int size)
    {
        // Retrieved the cached 'sample' of authorities in the zone
        String currentUserDomain = tenantService.getCurrentUserDomain();
        Pair<String, String> cacheKey = new Pair<String, String>(currentUserDomain, zoneName);
        List<ChildAssociationRef> zoneAuthorities = zoneAuthorityCache.get(cacheKey);
        final int maxToProcess = Math.max(size, zoneAuthoritySampleSize);
        if (zoneAuthorities == null)
        {
            zoneAuthorities = AuthenticationUtil.runAs(new RunAsWork<List<ChildAssociationRef>>()
            {
                @Override
                public List<ChildAssociationRef> doWork() throws Exception
                {
                    NodeRef root = zoneName == null ? getAuthorityContainer() : getZone(zoneName);
                    if (root == null)
                    {
                        return Collections.emptyList();
                    }
                    return nodeService.getChildAssocs(root, null, null, maxToProcess, false);
                }
            }, tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), currentUserDomain));
            zoneAuthorityCache.put(cacheKey, zoneAuthorities);
        }
        
        // Now search each for the required authority. If the number of results is greater than or close to the size
        // limit, then this will be the most efficient route
        Set<String> result = new TreeSet<String>();
        Set<String> positiveHits = new TreeSet<String>();
        Set<String> negativeHits = new TreeSet<String>();
        final int maxResults = size > 0 ? size : Integer.MAX_VALUE;
        int hits = 0, processed = 0;
        for (ChildAssociationRef groupAssoc : zoneAuthorities)
        {
            String containing = groupAssoc.getQName().getLocalName();
            AuthorityType containingType = AuthorityType.getAuthorityType(containing);
            processed++;
            // Cache the authority by key, if appropriate
            switch (containingType)
            {
            case USER:
            case ADMIN:
            case GUEST:
                break;
                default:
                    Pair <String, String> containingKey = cacheKey(containing);
                    if (!authorityLookupCache.contains(containingKey))
                    {
                        authorityLookupCache.put(containingKey, groupAssoc.getChildRef());
                    }
            }
            if ((type == null || containingType == type)
                    && (authority == null || isAuthorityContained(groupAssoc.getChildRef(), containing, authority, positiveHits, negativeHits))
                    && (filter == null || filter.includeAuthority(containing)))
            {
                result.add(getPooledName(containing));
                if (++hits == maxResults)
                {
                    break;
                }
            }

            // If this top down search is not providing an adequate hit count then resort to a naiive unlimited search
            if (processed >= maxToProcess)
            {
                Set<String> unfilteredResult;
                boolean filterZone;
                if (authority == null)
                {
                    unfilteredResult = new HashSet<String>(getAuthorities(type, zoneName, null, false, true, new PagingRequest(0, filter == null ? maxResults : Integer.MAX_VALUE, null)).getPage());
                    if (filter == null)
                    {
                        return unfilteredResult;
                    }
                    filterZone = false;
                }
                else
                {
                    unfilteredResult = getContainingAuthorities(type, authority, false);
                    filterZone = zoneName != null;
                }
                Set<String> newResult = new TreeSet<String>(result);
                int i=newResult.size();
                for (String container : unfilteredResult)
                {
                    // Do not call the filter multiple times on the same result in case it is 'stateful'
                    if (!result.contains(container) && (filter == null || filter.includeAuthority(container))
                            && (!filterZone || getAuthorityZones(container).contains(zoneName)))
                    {
                        newResult.add(container);
                        if (++i >= maxResults)
                        {
                            break;
                        }
                    }
                }
                result = newResult;
                break;
            }
        }
        return result;        
    }
    
    public String getShortName(String name)
    {
        AuthorityType type = AuthorityType.getAuthorityType(name);
        if (type.isFixedString())
        {
            return "";
        }
        else if (type.isPrefixed())
        {
            return name.substring(type.getPrefixString().length());
        }
        else
        {
            return name;
        }
    }

    public String getName(AuthorityType type, String shortName)
    {
        if (type.isFixedString())
        {
            return type.getFixedString();
        }
        else if (type.isPrefixed())
        {
            return type.getPrefixString() + shortName;
        }
        else
        {
            return shortName;
        }
    }

    protected void addAuthorityNameIfMatches(Set<String> authorities, String authorityName, AuthorityType type)
    {
        if (type == null || AuthorityType.getAuthorityType(authorityName).equals(type))
        {
            authorities.add(getPooledName(authorityName));
        }
    }
    
    protected void addAuthorityNameIfMatches(Set<String> authorities, String authorityName, AuthorityType type, Pattern pattern)
    {
        if (type == null || AuthorityType.getAuthorityType(authorityName).equals(type))
        {
            if (pattern == null)
            {
                authorities.add(getPooledName(authorityName));
            }
            else
            {
                if (pattern.matcher(getShortName(authorityName)).matches())
                {
                    authorities.add(getPooledName(authorityName));
                }
                else
                {
                    String displayName = getAuthorityDisplayName(authorityName);
                    if (displayName != null && pattern.matcher(displayName).matches())
                    {
                        authorities.add(getPooledName(authorityName));
                    }
                }
            }
        }
    }
    
    private void listAuthorities(AuthorityType type, String name, Set<String> authorities, boolean parents, boolean recursive)
    {
        AuthorityType localType = AuthorityType.getAuthorityType(name);
        if (localType.equals(AuthorityType.GUEST))
        {
            // Nothing to do
        }
        else
        {
            NodeRef ref = getAuthorityOrNull(name);
            
            if (ref != null)
            {
                listAuthorities(type, ref, authorities, parents, recursive, false);
            }
            else if (!localType.equals(AuthorityType.USER))
            {
                // Don't worry about missing person objects. It might be the system user or a user yet to be
                // auto-created
                throw new UnknownAuthorityException("An authority was not found for " + name);
            }
        }
    }
    
    private void listAuthorities(AuthorityType type, NodeRef nodeRef, Set<String> authorities, boolean parents, boolean recursive, boolean includeNode)
    {
        Set<String> unfilteredAuthorities = new TreeSet<String>();
        listAuthoritiesUnfiltered(nodeRef, unfilteredAuthorities, parents, recursive, includeNode);
        for (String authorityName : unfilteredAuthorities)
        {
            addAuthorityNameIfMatches(authorities, authorityName, type);
        }
    }
    
    private void listAuthoritiesUnfiltered(NodeRef nodeRef, Set<String> authorities, boolean parents, boolean recursive, boolean includeNode)
    {
        if (includeNode)
        {
            String authorityName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService
                    .getProperty(nodeRef, dictionaryService.isSubClass(nodeService.getType(nodeRef),
                            ContentModel.TYPE_AUTHORITY_CONTAINER) ? ContentModel.PROP_AUTHORITY_NAME
                            : ContentModel.PROP_USERNAME));
            if (!authorities.add(authorityName))
            {
                // Stop recursing if we've already been here
                return;
            }
        }
        
        // Loop over children if we want immediate children or are in recursive mode
        if (!includeNode || recursive)
        {
            if (parents)
            {
                List<ChildAssociationRef> cars = nodeService.getParentAssocs(nodeRef, ContentModel.ASSOC_MEMBER, RegexQNamePattern.MATCH_ALL);
                
                for (ChildAssociationRef car : cars)
                {
                    listAuthoritiesUnfiltered(car.getParentRef(), authorities, true, recursive, true);
                }
            }
            else
            {
                Pair<Map<NodeRef, String>, List<NodeRef>> childAuthorities = getChildAuthorities(nodeRef);
                Map<NodeRef, String> childAuthorityMap = childAuthorities.getFirst();
                // Recurse on non-leaves
                if (recursive)
                {
                    for (NodeRef childRef : childAuthorities.getSecond())
                    {
                        String childAuthorityName = childAuthorityMap.get(childRef);
                        if (authorities.add(childAuthorityName))
                        {
                            listAuthoritiesUnfiltered(childRef, authorities, false, true, false);
                        }
                    }
                }
                // Add leaves
                authorities.addAll(childAuthorityMap.values());
            }
        }
    }


    public boolean isAuthorityContained(String authority, String authorityToFind, Set<String> positiveHits, Set<String> negativeHits)
    {
        if (positiveHits.contains(authority))
        {
            return true;
        }
        if (negativeHits.contains(authority))
        {
            return false;
        }
        
        NodeRef authorityNodeRef = getAuthorityNodeRefOrNull(authority);
        if (authorityNodeRef == null)
        {
            negativeHits.add(getPooledName(authority));
            return false;
        }
        if(useGetContainingAuthoritiesForIsAuthorityContained)
        {
            if(authorityBridgeTableCache.isUpToDate())
            {
                return getContainingAuthorities(null, authorityToFind, false).contains(authority);
            }
            else
            {
                return isAuthorityContained(authorityNodeRef, getPooledName(authority), authorityToFind, positiveHits, negativeHits);
            }
        }
        else
        {
            return isAuthorityContained(authorityNodeRef, getPooledName(authority), authorityToFind, positiveHits, negativeHits);
        }
    }
    
    private boolean isAuthorityContained(NodeRef authorityNodeRef, String authority, String authorityToFind, Set<String> positiveHits, Set<String> negativeHits)
    {
        // Look up the desired authority using case sensitivity rules appropriate for the authority type
        NodeRef authorityToFindRef = getAuthorityOrNull(authorityToFind);
        if (authorityToFindRef == null)
        {
            // No such authority so it won't be contained anywhere
            negativeHits.add(authority);
            return false;
        }
        // Now we can just search for the NodeRef
        return isAuthorityContainedImpl(authorityNodeRef, authority, authorityToFindRef, positiveHits, negativeHits);
    }

    /**
     * @param authorityNodeRef              a containing authority
     * @param authorityToFindRef            an authority to find in the hierarchy
     * @return                              Returns <tt>true</tt> if the authority to find occurs
     *                                      in the hierarchy and is reachable via the {@link ContentModel#ASSOC_MEMBER}
     *                                      association.
     */
    private boolean isAuthorityContainedImpl(NodeRef authorityNodeRef, String authority, NodeRef authorityToFindRef,
            Set<String> positiveHits, Set<String> negativeHits)
    {
        if (positiveHits.contains(authority))
        {
            return true;
        }
        if (negativeHits.contains(authority))
        {
            return false;
        }
        Pair<Map<NodeRef, String>, List<NodeRef>> childAuthorities = getChildAuthorities(authorityNodeRef);
        Map<NodeRef, String> childAuthorityMap = childAuthorities.getFirst();

        // Is the authority we are looking for in the set provided (NodeRef is lookup)
        if (childAuthorityMap.containsKey(authorityToFindRef))
        {
            positiveHits.add(authority);
            return true;
        }

        // Recurse on non-user authorities
        for (NodeRef nodeRef : childAuthorities.getSecond())
        {
            if (isAuthorityContainedImpl(nodeRef, childAuthorityMap.get(nodeRef),
                    authorityToFindRef, positiveHits, negativeHits))
            {
                positiveHits.add(authority);
                return true;
            }
        }
        negativeHits.add(authority);
        return false;
    }
    
    /**
     * Remove entries for the parents of the given node.
     * 
     * @param lock          <tt>true</tt> if the cache modifications need to be locked
     *                      i.e. if the caller is handling a <b>beforeXYZ</b> callback.
     */
    private void removeParentsFromChildAuthorityCache(NodeRef nodeRef, boolean lock)
    {
        // Get the transactional version of the cache if we need locking
        TransactionalCache<NodeRef, Pair<Map<NodeRef,String>, List<NodeRef>>> childAuthorityCacheTxn = null;
        if (lock && childAuthorityCache instanceof TransactionalCache)
        {
            childAuthorityCacheTxn = (TransactionalCache<NodeRef, Pair<Map<NodeRef,String>, List<NodeRef>>>) childAuthorityCache;
        }
        
        // Iterate over all relevant parents of the given node
        for (ChildAssociationRef car: nodeService.getParentAssocs(nodeRef))
        {
            NodeRef parentRef = car.getParentRef();
            if (dictionaryService.isSubClass(nodeService.getType(parentRef), ContentModel.TYPE_AUTHORITY_CONTAINER))
            {
                TransactionalResourceHelper.getSet(PARENTS_OF_DELETING_CHILDREN_SET_RESOURCE).add(parentRef);
                childAuthorityCache.remove(parentRef);
                if (childAuthorityCacheTxn != null)
                {
                    childAuthorityCacheTxn.lockValue(parentRef);
                }
            }
        }
    }
    
    private NodeRef getAuthorityOrNull(final String name)
    {
        try
        {
            final AuthorityType authType = AuthorityType.getAuthorityType(name);
            switch (authType)
            {
                case USER:
                    return personService.getPerson(name, false);
                case GUEST:
                case ADMIN:
                case EVERYONE:
                case OWNER:
                    return null;
                default:
                {
                    Pair <String, String> cacheKey = cacheKey(name);
                    NodeRef result = authorityLookupCache.get(cacheKey);
                    if (result == null)
                    {
                        List<ChildAssociationRef> results = nodeService.getChildAssocs(getAuthorityContainer(),
                                ContentModel.ASSOC_CHILDREN, QName.createQName("cm", name, namespacePrefixResolver), false);
                        result = results.isEmpty() ? NULL_NODEREF :results.get(0).getChildRef(); 
                        authorityLookupCache.put(cacheKey, result);
                    }
                    return result.equals(NULL_NODEREF) ? null : result;
                }
            }
        }
        catch (NoSuchPersonException e)
        {
            return null;
        }
    }

    /**
     * @return Returns the authority container, <b>which must exist</b>
     */
    private NodeRef getAuthorityContainer()
    {
        return getSystemContainer(qnameAssocAuthorities);
    }

    /**
     * @return Returns the zone container, <b>which must exist</b>
     */
    private NodeRef getZoneContainer()
    {
        return getSystemContainer(qnameAssocZones);
    }

    /**
     * Return the system container for the specified assoc name.
     * The containers are cached in a thread safe Tenant aware cache.
     *
     * @param assocQName
     *
     * @return System container, <b>which must exist</b>
     */
    private NodeRef getSystemContainer(QName assocQName)
    {
        final String cacheKey = KEY_SYSTEMCONTAINER_NODEREF + "." + assocQName.toString();
        NodeRef systemContainerRef = (NodeRef)singletonCache.get(cacheKey);
        if (systemContainerRef == null)
        {
            NodeRef rootNodeRef = nodeService.getRootNode(this.storeRef);
            List<ChildAssociationRef> results = nodeService.getChildAssocs(rootNodeRef, RegexQNamePattern.MATCH_ALL, qnameAssocSystem, false);
            if (results.size() == 0)
            {
                throw new AlfrescoRuntimeException("Required system path not found: " + qnameAssocSystem);
            }
            NodeRef sysNodeRef = results.get(0).getChildRef();
            results = nodeService.getChildAssocs(sysNodeRef, RegexQNamePattern.MATCH_ALL, assocQName, false);
            if (results.size() == 0)
            {
                throw new AlfrescoRuntimeException("Required path not found: " + assocQName);
            }
            systemContainerRef = results.get(0).getChildRef();
            singletonCache.put(cacheKey, systemContainerRef);
        }
        return systemContainerRef;
    }

    public NodeRef getAuthorityNodeRefOrNull(String name)
    {
        return getAuthorityOrNull(name);
    }

    public String getAuthorityName(NodeRef authorityRef)
    {
        String name = null;
        if (nodeService.exists(authorityRef))
        {
            QName type = nodeService.getType(authorityRef);
            if (dictionaryService.isSubClass(type, ContentModel.TYPE_AUTHORITY_CONTAINER))
            {
                name = (String) nodeService.getProperty(authorityRef, ContentModel.PROP_AUTHORITY_NAME);
            }
            else if (dictionaryService.isSubClass(type, ContentModel.TYPE_PERSON))
            {
                name = (String) nodeService.getProperty(authorityRef, ContentModel.PROP_USERNAME);
            }
        }
        return getPooledName(name);
    }

    public String getAuthorityDisplayName(String authorityName)
    {
        NodeRef ref = getAuthorityOrNull(authorityName);
        if (ref == null)
        {
            return null;
        }
        Serializable value = nodeService.getProperty(ref, ContentModel.PROP_AUTHORITY_DISPLAY_NAME);
        if (value == null)
        {
            return null;
        }
        return DefaultTypeConverter.INSTANCE.convert(String.class, value);
    }

    public void setAuthorityDisplayName(String authorityName, String authorityDisplayName)
    {
        NodeRef ref = getAuthorityOrNull(authorityName);
        if (ref == null)
        {
            return;
        }
        nodeService.setProperty(ref, ContentModel.PROP_AUTHORITY_DISPLAY_NAME, authorityDisplayName);

    }

    public NodeRef getOrCreateZone(String zoneName)
    {
        return getOrCreateZone(zoneName, true);
    }

    private NodeRef getOrCreateZone(String zoneName, boolean create)
    {
        NodeRef zoneContainerRef = getZoneContainer();
        QName zoneQName = QName.createQName("cm", zoneName, namespacePrefixResolver);
        List<ChildAssociationRef> results = nodeService.getChildAssocs(zoneContainerRef, ContentModel.ASSOC_CHILDREN, zoneQName, false);
        if (results.isEmpty())
        {
            if (create)
            {
                HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
                props.put(ContentModel.PROP_NAME, zoneName);
                return nodeService.createNode(zoneContainerRef, ContentModel.ASSOC_CHILDREN, zoneQName, ContentModel.TYPE_ZONE, props).getChildRef();
            }
            else
            {
                return null;
            }
        }
        else
        {
            return results.get(0).getChildRef();
        }
    }

    public NodeRef getZone(String zoneName)
    {
        return getOrCreateZone(zoneName, false);
    }

    public Set<String> getAuthorityZones(String name)
    {
        Set<String> zones = new TreeSet<String>();
        NodeRef childRef = getAuthorityOrNull(name);
        if (childRef == null)
        {
            return null;
        }
        List<ChildAssociationRef> results = nodeService.getParentAssocs(childRef, ContentModel.ASSOC_IN_ZONE, RegexQNamePattern.MATCH_ALL);
        if (results.isEmpty())
        {
            return zones;
        }

        for (ChildAssociationRef current : results)
        {
            NodeRef zoneRef = current.getParentRef();
            Serializable value = nodeService.getProperty(zoneRef, ContentModel.PROP_NAME);
            if (value == null)
            {
                continue;
            }
            else
            {
                String zone = DefaultTypeConverter.INSTANCE.convert(String.class, value);
                zones.add(zone);
            }
        }
        return zones;
    }
    
    public Set<String> getAllAuthoritiesInZone(String zoneName, AuthorityType type)
    {
        NodeRef zoneRef = getZone(zoneName);
        if (zoneRef == null)
        {
            return Collections.emptySet();
        }
        return new HashSet<String>(getAuthoritiesImpl(type, zoneRef, null, null, false,
                new PagingRequest(0, Integer.MAX_VALUE, null), new PagingResultsString()).getPage());
    }
    
    public void addAuthorityToZones(String authorityName, Set<String> zones)
    {
        if ((zones != null) && (zones.size() > 0))
        {
            Set<NodeRef> zoneRefs = new HashSet<NodeRef>(zones.size() * 2);
            for (String authorityZone : zones)
            {
                zoneRefs.add(getOrCreateZone(authorityZone));
            }
            NodeRef authRef = getAuthorityOrNull(authorityName);
            if (authRef != null)
            {
                // Normalize the user name if necessary
                if (AuthorityType.getAuthorityType(authorityName) == AuthorityType.USER)
                {
                    authorityName = (String) nodeService.getProperty(authRef, ContentModel.PROP_USERNAME);
                }
                
                nodeService.addChild(zoneRefs, authRef, ContentModel.ASSOC_IN_ZONE, QName.createQName("cm", authorityName, namespacePrefixResolver));
            }
        }
    }
    
    public void removeAuthorityFromZones(String authorityName, Set<String> zones)
    {
        if ((zones != null) && (zones.size() > 0))
        {
            NodeRef authRef = getAuthorityOrNull(authorityName);
            List<ChildAssociationRef> results = nodeService.getParentAssocs(authRef, ContentModel.ASSOC_IN_ZONE, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef current : results)
            {
                NodeRef zoneRef = current.getParentRef();
                Serializable value = nodeService.getProperty(zoneRef, ContentModel.PROP_NAME);
                if (value == null)
                {
                    continue;
                }
                else
                {
                    String testZone = DefaultTypeConverter.INSTANCE.convert(String.class, value);
                    if (zones.contains(testZone))
                    {
                        nodeService.removeChildAssociation(current);
                    }
                }
            }
        }
    }
        
    private Set<String> getRootAuthoritiesUnderContainer(NodeRef container, AuthorityType type)
    {
        if (type != null && type.equals(AuthorityType.USER))
        {
            return Collections.<String> emptySet();
        }
        Collection<ChildAssociationRef> childRefs = nodeService.getChildAssocsWithoutParentAssocsOfType(container, ContentModel.ASSOC_MEMBER);
        Set<String> authorities = new TreeSet<String>();
        for (ChildAssociationRef childRef : childRefs)
        {
            addAuthorityNameIfMatches(authorities, childRef.getQName().getLocalName(), type);
        }
        return authorities;
    }
    
    /**
     * Listen out for person removals so that we can clear cached authorities.
     */
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        String authorityName = getAuthorityName(nodeRef);
        userAuthorityCache.remove(authorityName);
        if (userAuthorityCache instanceof TransactionalCache)
        {
            /*
             * We lock the removal for the duration of the transaction as the node has not
             * yet been deleted, leaving scope for some other code to come along and add the
             * value back before the deletion can actually take place.
             */
            TransactionalCache<String, Set<String>> userAuthorityCacheTxn = (TransactionalCache<String, Set<String>>) userAuthorityCache;
            userAuthorityCacheTxn.lockValue(authorityName);
        }
        // Remove cache elements for the parents, ensuring that we lock because the data still exists
        removeParentsFromChildAuthorityCache(nodeRef, true);
    }

    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        boolean isAuthority = dictionaryService.isSubClass(nodeService.getType(nodeRef),
                ContentModel.TYPE_AUTHORITY_CONTAINER);
        QName idProp = isAuthority ? ContentModel.PROP_AUTHORITY_NAME  : ContentModel.PROP_USERNAME;
        String authBefore = DefaultTypeConverter.INSTANCE.convert(String.class, before.get(idProp));
        if (authBefore == null)
        {
            // Node has just been created; nothing to do
            return;
        }
        String authAfter = DefaultTypeConverter.INSTANCE.convert(String.class, after.get(idProp));
        if (!EqualsHelper.nullSafeEquals(authBefore, authAfter))
        {
            if (AlfrescoTransactionSupport.getResource(PersonServiceImpl.KEY_ALLOW_UID_UPDATE) != null || authBefore.equalsIgnoreCase(authAfter))
            {
                if (isAuthority)
                {
                    if (authBefore != null)
                    {
                        // Fix any ACLs
                        aclDao.renameAuthority(authBefore, authAfter);
                    }

                    // Fix primary association local name
                    QName newAssocQName = QName.createQName("cm", authAfter, namespacePrefixResolver);
                    ChildAssociationRef assoc = nodeService.getPrimaryParent(nodeRef);
                    nodeService.moveNode(nodeRef, assoc.getParentRef(), assoc.getTypeQName(), newAssocQName);

                    // Fix other non-case sensitive parent associations
                    QName oldAssocQName = QName.createQName("cm", authBefore, namespacePrefixResolver);
                    newAssocQName = QName.createQName("cm", authAfter, namespacePrefixResolver);
                    for (ChildAssociationRef parent : nodeService.getParentAssocs(nodeRef))
                    {
                        if (!parent.isPrimary() && parent.getQName().equals(oldAssocQName))
                        {
                            nodeService.removeChildAssociation(parent);
                            nodeService.addChild(parent.getParentRef(), parent.getChildRef(), parent.getTypeQName(),
                                    newAssocQName);
                        }
                    }
                    authorityLookupCache.clear();
                    authorityBridgeTableCache.refresh();
                    
                    // Cache is out of date
                    userAuthorityCache.clear();
                }
                else
                {
                    userAuthorityCache.remove(authBefore);
                }
                // Remove cache entires for the parents.  No need to lock because the data has already been updated.
                removeParentsFromChildAuthorityCache(nodeRef, false);
            }
            else
            {
                throw new UnsupportedOperationException("The name of an authority can not be changed");
            }
        }
    }

    public void init()
    {
        // Listen out for person removals so that we can clear cached authorities
        this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"), ContentModel.TYPE_PERSON, new JavaBehaviour(
                this, "beforeDeleteNode"));
        // Listen out for updates to persons and authority containers to handle renames
        this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"), ContentModel.TYPE_AUTHORITY, new JavaBehaviour(
                this, "onUpdateProperties"));
    }
    
    /**
     * @param parentNodeRef         the parent authority
     * @return                      Returns authorities reachable by the {@link ContentModel#ASSOC_MEMBER} association
     */
    private Pair<Map<NodeRef, String>, List<NodeRef>> getChildAuthorities(NodeRef parentNodeRef)
    {
        Pair<Map<NodeRef,String>, List<NodeRef>> result = childAuthorityCache.get(parentNodeRef);
        if (result == null)
        {
            List<ChildAssociationRef> cars = nodeService.getChildAssocs(
                    parentNodeRef,
                    ContentModel.ASSOC_MEMBER,
                    RegexQNamePattern.MATCH_ALL,
                    false);
            if (cars.isEmpty())
            {
                // ALF-17702: BM-0013: Soak: Run 02: getCachedChildAuthorities is not caching results
                //            Don't return here.  We need to cache the miss.
                result = new Pair<Map<NodeRef, String>, List<NodeRef>>(Collections.<NodeRef, String> emptyMap(),
                        Collections.<NodeRef> emptyList());
            }
            else
            {
                Map<NodeRef,String> lookup = new HashMap<NodeRef, String>(cars.size() * 2);
                List<NodeRef> parents = new LinkedList<NodeRef>();
                for (ChildAssociationRef car : cars)
                {
                    NodeRef memberNodeRef = car.getChildRef();
                    String memberName = getPooledName(car.getQName().getLocalName());
                    lookup.put(memberNodeRef, memberName);
                    AuthorityType authorityType = AuthorityType.getAuthorityType(memberName);
                    if (authorityType == AuthorityType.GROUP || authorityType == AuthorityType.ROLE)
                    {
                        parents.add(memberNodeRef);
                    }
                }
                result = new Pair<Map<NodeRef, String>, List<NodeRef>>(lookup, parents);
            }
            // Cache whatever we have
            if(!TransactionalResourceHelper.getSet(PARENTS_OF_DELETING_CHILDREN_SET_RESOURCE).contains(parentNodeRef))
            {
                childAuthorityCache.put(parentNodeRef, result);
            }
        }
        return result;
    }
   
    private abstract class AbstractPagingResults<R> implements PagingResults<R>
    {
        protected CannedQueryResults<AuthorityInfo> results;

        public void setResults(CannedQueryResults<AuthorityInfo> results)
        {
            this.results = results;
        }

        @Override
        public String getQueryExecutionId()
        {
            return results.getQueryExecutionId();
        }

        @Override
        public boolean hasMoreItems()
        {
            return results.hasMoreItems();
        }

        @Override
        public Pair<Integer, Integer> getTotalResultCount()
        {
            return results.getTotalResultCount();
        }
    }
    
    private class PagingResultsString extends AbstractPagingResults<String>
    {
        @Override
        public List<String> getPage()
        {
            List<String> auths = new ArrayList<String>(results.getPageCount());
            for (AuthorityInfo authInfo : results.getPage())
            {
                auths.add((String) getPooledName(authInfo.getAuthorityName()));
            }
            return auths;
        }
    };

    private class PagingResultsAuthorityInfo extends AbstractPagingResults<AuthorityInfo>
    {
        @Override
        public List<AuthorityInfo> getPage()
        {
            List<AuthorityInfo> auths = new ArrayList<AuthorityInfo>(results.getPageCount());
            for (AuthorityInfo authInfo : results.getPage())
            {
                auths.add((AuthorityInfo) authInfo);
            }
            return auths;
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.cache.RefreshableCacheListener#onRefreshableCacheEvent(org.alfresco.repo.cache.RefreshableCacheEvent)
     */
    @Override
    public void onRefreshableCacheEvent(RefreshableCacheEvent refreshableCacheEvent)
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("Bridge Table cache triggering userAuthorityCache.clear()");
        }
        userAuthorityCache.clear();
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.cache.RefreshableCacheListener#getCacheId()
     */
    @Override
    public String getCacheId()
    {
        return AuthorityDAOImpl.class.getName();
    }


    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "aclDao", aclDao);
        PropertyCheck.mandatory(this, "authorityBridgeDAO", authorityBridgeDAO);
        PropertyCheck.mandatory(this, "authorityBridgeTableCache", authorityBridgeTableCache);
        PropertyCheck.mandatory(this, "authorityLookupCache", authorityLookupCache);
        PropertyCheck.mandatory(this, "cannedQueryRegistry", cannedQueryRegistry);
        PropertyCheck.mandatory(this, "childAuthorityCache", childAuthorityCache);
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "namespacePrefixResolver", namespacePrefixResolver);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "personService", personService);
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
        PropertyCheck.mandatory(this, "searchService", searchService);
        PropertyCheck.mandatory(this, "storeRef", storeRef);
        PropertyCheck.mandatory(this, "tenantService", tenantService);
        PropertyCheck.mandatory(this, "userAuthorityCache", userAuthorityCache);
        PropertyCheck.mandatory(this, "zoneAuthorityCache", zoneAuthorityCache);
        PropertyCheck.mandatory(this, "storeRef", storeRef);
        PropertyCheck.mandatory(this, "storeRef", storeRef);
        authorityBridgeTableCache.register(this);
        
    };
}
