/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.security.person;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.domain.permissions.AclDAO;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.node.getchildren.FilterProp;
import org.alfresco.repo.node.getchildren.FilterPropString;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQuery;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQueryFactory;
import org.alfresco.repo.node.getchildren.FilterPropString.FilterTypeString;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.PermissionServiceSPI;
import org.alfresco.repo.tenant.TenantDomainMismatchException;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.admin.RepoUsageStatus;
import org.alfresco.service.cmr.admin.RepoUsage.UsageType;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.invitation.InvitationException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.ModelUtil;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.registry.NamedObjectRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

public class PersonServiceImpl extends TransactionListenerAdapter implements PersonService,
                                                                             NodeServicePolicies.BeforeCreateNodePolicy,
                                                                             NodeServicePolicies.OnCreateNodePolicy,
                                                                             NodeServicePolicies.BeforeDeleteNodePolicy,
                                                                             NodeServicePolicies.OnUpdatePropertiesPolicy
                                                                             
{
    private static Log logger = LogFactory.getLog(PersonServiceImpl.class);
    
    private static final String CANNED_QUERY_PEOPLE_LIST = "peopleGetChildrenCannedQueryFactory";

    private static final String DELETE = "DELETE";
    private static final String SPLIT = "SPLIT";
    private static final String LEAVE = "LEAVE";
    public static final String SYSTEM_FOLDER_SHORT_QNAME = "sys:system";
    public static final String PEOPLE_FOLDER_SHORT_QNAME = "sys:people";
    private static final String SYSTEM_USAGE_WARN_LIMIT_USERS_EXCEEDED_VERBOSE = "system.usage.err.limit_users_exceeded_verbose";

    private static final String KEY_POST_TXN_DUPLICATES = "PersonServiceImpl.KEY_POST_TXN_DUPLICATES";
    public static final String KEY_ALLOW_UID_UPDATE = "PersonServiceImpl.KEY_ALLOW_UID_UPDATE";
    private static final String KEY_USERS_CREATED = "PersonServiceImpl.KEY_USERS_CREATED";

    private StoreRef storeRef;
    private TransactionService transactionService;
    private NodeService nodeService;
    private TenantService tenantService;
    private SearchService searchService;
    private AuthorityService authorityService;
    private MutableAuthenticationService authenticationService;
    private DictionaryService dictionaryService;
    private PermissionServiceSPI permissionServiceSPI;
    private NamespacePrefixResolver namespacePrefixResolver;
    private HomeFolderManager homeFolderManager;
    private PolicyComponent policyComponent;
    private AclDAO aclDao;
    private PermissionsManager permissionsManager;
    private RepoAdminService repoAdminService;
    private ServiceRegistry serviceRegistry;

    private boolean createMissingPeople;
    private static Set<QName> mutableProperties;
    private String defaultHomeFolderProvider;
    private boolean processDuplicates = true;
    private String duplicateMode = LEAVE;
    private boolean lastIsBest = true;
    private boolean includeAutoCreated = false;
    
    private NamedObjectRegistry<CannedQueryFactory<NodeRef>> cannedQueryRegistry;
    
    /** a transactionally-safe cache to be injected */
    private SimpleCache<String, Set<NodeRef>> personCache;
    
    /** People Container ref cache (Tennant aware) */
    private Map<String, NodeRef> peopleContainerRefs = new ConcurrentHashMap<String, NodeRef>(4);
    
    private UserNameMatcher userNameMatcher;
    
    private JavaBehaviour beforeCreateNodeValidationBehaviour;
    private JavaBehaviour beforeDeleteNodeValidationBehaviour;
    
    private boolean homeFolderCreationEager;
	
    static
    {
        Set<QName> props = new HashSet<QName>();
        props.add(ContentModel.PROP_HOMEFOLDER);
        props.add(ContentModel.PROP_FIRSTNAME);
        // Middle Name
        props.add(ContentModel.PROP_LASTNAME);
        props.add(ContentModel.PROP_EMAIL);
        props.add(ContentModel.PROP_ORGID);
        mutableProperties = Collections.unmodifiableSet(props);
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj;
    }

    @Override
    public int hashCode()
    {
        return 1;
    }

    /**
     * Spring bean init method
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "storeUrl", storeRef);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "permissionServiceSPI", permissionServiceSPI);
        PropertyCheck.mandatory(this, "authorityService", authorityService);
        PropertyCheck.mandatory(this, "authenticationService", authenticationService);
        PropertyCheck.mandatory(this, "namespacePrefixResolver", namespacePrefixResolver);
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
        PropertyCheck.mandatory(this, "personCache", personCache);
        PropertyCheck.mandatory(this, "aclDao", aclDao);
        PropertyCheck.mandatory(this, "homeFolderManager", homeFolderManager);
        PropertyCheck.mandatory(this, "repoAdminService", repoAdminService);

        beforeCreateNodeValidationBehaviour = new JavaBehaviour(this, "beforeCreateNodeValidation");
        this.policyComponent.bindClassBehaviour(
                BeforeCreateNodePolicy.QNAME,
                ContentModel.TYPE_PERSON,
                beforeCreateNodeValidationBehaviour);
        
        beforeDeleteNodeValidationBehaviour = new JavaBehaviour(this, "beforeDeleteNodeValidation");
        this.policyComponent.bindClassBehaviour(
                BeforeDeleteNodePolicy.QNAME,
                ContentModel.TYPE_PERSON,
                beforeDeleteNodeValidationBehaviour);
        
        this.policyComponent.bindClassBehaviour(
                OnCreateNodePolicy.QNAME,
                ContentModel.TYPE_PERSON,
                new JavaBehaviour(this, "onCreateNode"));
        
        this.policyComponent.bindClassBehaviour(
                BeforeDeleteNodePolicy.QNAME,
                ContentModel.TYPE_PERSON,
                new JavaBehaviour(this, "beforeDeleteNode"));
        
        this.policyComponent.bindClassBehaviour(
                OnUpdatePropertiesPolicy.QNAME,
                ContentModel.TYPE_PERSON,
                new JavaBehaviour(this, "onUpdateProperties"));
        
        this.policyComponent.bindClassBehaviour(
                OnUpdatePropertiesPolicy.QNAME,
                ContentModel.TYPE_USER,
                new JavaBehaviour(this, "onUpdatePropertiesUser"));
    }

    /**
     * {@inheritDoc}
     */
    public void setCreateMissingPeople(boolean createMissingPeople)
    {
        this.createMissingPeople = createMissingPeople;
    }

    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    public void setAuthenticationService(MutableAuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setPermissionServiceSPI(PermissionServiceSPI permissionServiceSPI)
    {
        this.permissionServiceSPI = permissionServiceSPI;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setRepoAdminService(RepoAdminService repoAdminService)
    {
        this.repoAdminService = repoAdminService;
    }
    
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    public void setStoreUrl(String storeUrl)
    {
        this.storeRef = new StoreRef(storeUrl);
    }
    
    public void setUserNameMatcher(UserNameMatcher userNameMatcher)
    {
        this.userNameMatcher = userNameMatcher;
    }

    void setDefaultHomeFolderProvider(String defaultHomeFolderProvider)
    {
        this.defaultHomeFolderProvider = defaultHomeFolderProvider;
    }

    public void setDuplicateMode(String duplicateMode)
    {
        this.duplicateMode = duplicateMode;
    }

    public void setIncludeAutoCreated(boolean includeAutoCreated)
    {
        this.includeAutoCreated = includeAutoCreated;
    }

    public void setLastIsBest(boolean lastIsBest)
    {
        this.lastIsBest = lastIsBest;
    }

    public void setProcessDuplicates(boolean processDuplicates)
    {
        this.processDuplicates = processDuplicates;
    }

    public void setHomeFolderManager(HomeFolderManager homeFolderManager)
    {
        this.homeFolderManager = homeFolderManager;
    }
    
    /**
     * Indicates if home folders should be created when the person
     * is created or delayed until first accessed.
     */
    public void setHomeFolderCreationEager(boolean homeFolderCreationEager)
    {
        this.homeFolderCreationEager = homeFolderCreationEager;
    }
    
    public void setAclDAO(AclDAO aclDao)
    {
        this.aclDao = aclDao;
    }

    public void setPermissionsManager(PermissionsManager permissionsManager)
    {
        this.permissionsManager = permissionsManager;
    }
    
    /**
     * Set the registry of {@link CannedQueryFactory canned queries}
     */
    public void setCannedQueryRegistry(NamedObjectRegistry<CannedQueryFactory<NodeRef>> cannedQueryRegistry)
    {
        this.cannedQueryRegistry = cannedQueryRegistry;
    }
    
    /**
     * Set the username to person cache.
     */
    public void setPersonCache(SimpleCache<String, Set<NodeRef>> personCache)
    {
        this.personCache = personCache;
    }
    
    /**
     * Avoid injection issues: Look it up from the Service Registry as required
     */
    private FileFolderService getFileFolderService()
    {
        return serviceRegistry.getFileFolderService();
    }
    
    /**
     * Avoid injection issues: Look it up from the Service Registry as required
     */
    private NamespaceService getNamespaceService()
    {
        return serviceRegistry.getNamespaceService();
    }
    
    /**
     * Avoid injection issues: Look it up from the Service Registry as required
     */
    private ActionService getActionService()
    {
        return serviceRegistry.getActionService();
    }
    
    /**
     * {@inheritDoc}
     */
    public NodeRef getPerson(String userName)
    {
        return getPerson(userName, true);
    }
    
    /**
     * {@inheritDoc}
     */
    public NodeRef getPerson(final String userName, final boolean autoCreateHomeFolderAndMissingPersonIfAllowed)
    {
        // MT share - for activity service system callback
        if (tenantService.isEnabled() && (AuthenticationUtil.SYSTEM_USER_NAME.equals(AuthenticationUtil.getRunAsUser())) && tenantService.isTenantUser(userName))
        {
            final String tenantDomain = tenantService.getUserDomain(userName);

            return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>()
            {
                public NodeRef doWork() throws Exception
                {
                    return getPersonImpl(userName, autoCreateHomeFolderAndMissingPersonIfAllowed);
                }
            }, tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
        }
        else
        {
            return getPersonImpl(userName, autoCreateHomeFolderAndMissingPersonIfAllowed);
        }
    }

    private NodeRef getPersonImpl(String userName, boolean autoCreateHomeFolderAndMissingPersonIfAllowed)
    {
        if(userName == null)
        {
            return null;
        }
        if(userName.length() == 0)
        {
            return null;
        }
        NodeRef personNode = getPersonOrNull(userName);
        if (personNode == null)
        {
            TxnReadState txnReadState = AlfrescoTransactionSupport.getTransactionReadState();
            if (autoCreateHomeFolderAndMissingPersonIfAllowed && createMissingPeople() &&
                txnReadState == TxnReadState.TXN_READ_WRITE)
            {
                // We create missing people AND are in a read-write txn
                return createMissingPerson(userName, true);
            }
            else
            {
                throw new NoSuchPersonException(userName);
            }
        }
        else if (autoCreateHomeFolderAndMissingPersonIfAllowed)
        {
            makeHomeFolderIfRequired(personNode);
        }
        return personNode;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean personExists(String caseSensitiveUserName)
    {
        return getPersonOrNull(caseSensitiveUserName) != null;
    }
    
    private NodeRef getPersonOrNull(String searchUserName)
    {
        Set<NodeRef> allRefs = getFromCache(searchUserName);
        boolean addToCache = false;
        if (allRefs == null)
        {
            List<ChildAssociationRef> childRefs = nodeService.getChildAssocs(
                    getPeopleContainer(),
                    ContentModel.ASSOC_CHILDREN,
                    getChildNameLower(searchUserName),
                    false);
            allRefs = new LinkedHashSet<NodeRef>(childRefs.size() * 2);
            
            for (ChildAssociationRef childRef : childRefs)
            {
                NodeRef nodeRef = childRef.getChildRef();
                allRefs.add(nodeRef);
            }
            addToCache = true;
        }
        
        List<NodeRef> refs = new ArrayList<NodeRef>(allRefs.size());
        Set<NodeRef> nodesToRemoveFromCache = new HashSet<NodeRef>();
        for (NodeRef nodeRef : allRefs)
        {
            if (nodeService.exists(nodeRef))
            {
                Serializable value = nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME);
                String realUserName = DefaultTypeConverter.INSTANCE.convert(String.class, value);
                if (userNameMatcher.matches(searchUserName, realUserName))
                {
                    refs.add(nodeRef);
                }
            }
            else
            {
                nodesToRemoveFromCache.add(nodeRef);
            }
        }

        if (!nodesToRemoveFromCache.isEmpty())
        {
            allRefs.removeAll(nodesToRemoveFromCache);
        }

        NodeRef returnRef = null;
        if (refs.size() > 1)
        {
            returnRef = handleDuplicates(refs, searchUserName);
        }
        else if (refs.size() == 1)
        {
            returnRef = refs.get(0);
            
            if (addToCache)
            {
                // Don't bother caching unless we get a result that doesn't need duplicate processing
                putToCache(searchUserName, allRefs);
            }
        }
        return returnRef;
    }

    private NodeRef handleDuplicates(List<NodeRef> refs, String searchUserName)
    {
        if (processDuplicates)
        {
            NodeRef best = findBest(refs);
            HashSet<NodeRef> toHandle = new HashSet<NodeRef>();
            toHandle.addAll(refs);
            toHandle.remove(best);
            addDuplicateNodeRefsToHandle(toHandle);
            return best;
        }
        else
        {
            String userNameSensitivity = " (user name is case-" + (userNameMatcher.getUserNamesAreCaseSensitive() ? "sensitive" : "insensitive") + ")";
            String domainNameSensitivity = "";
            if (!userNameMatcher.getDomainSeparator().equals(""))
            {
                domainNameSensitivity = " (domain name is case-" + (userNameMatcher.getDomainNamesAreCaseSensitive() ? "sensitive" : "insensitive") + ")";
            }

            throw new AlfrescoRuntimeException("Found more than one user for " + searchUserName + userNameSensitivity + domainNameSensitivity);
        }
    }

    /**
     * Get the txn-bound usernames that need cleaning up
     */
    private Set<NodeRef> getPostTxnDuplicates()
    {
        @SuppressWarnings("unchecked")
        Set<NodeRef> postTxnDuplicates = (Set<NodeRef>) AlfrescoTransactionSupport.getResource(KEY_POST_TXN_DUPLICATES);
        if (postTxnDuplicates == null)
        {
            postTxnDuplicates = new HashSet<NodeRef>();
            AlfrescoTransactionSupport.bindResource(KEY_POST_TXN_DUPLICATES, postTxnDuplicates);
        }
        return postTxnDuplicates;
    }

    /**
     * Flag a username for cleanup after the transaction.
     */
    private void addDuplicateNodeRefsToHandle(Set<NodeRef> refs)
    {
        // Firstly, bind this service to the transaction
        AlfrescoTransactionSupport.bindListener(this);
        // Now get the post txn duplicate list
        Set<NodeRef> postTxnDuplicates = getPostTxnDuplicates();
        postTxnDuplicates.addAll(refs);
    }

    /**
     * Process clean up any duplicates that were flagged during the transaction.
     */
    @Override
    public void afterCommit()
    {
        // Get the duplicates in a form that can be read by the transaction work anonymous instance
        final Set<NodeRef> postTxnDuplicates = getPostTxnDuplicates();
        if (postTxnDuplicates.size() == 0)
        {
            // Nothing to do
            return;
        }
        
        RetryingTransactionCallback<Object> processDuplicateWork = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                if (duplicateMode.equalsIgnoreCase(SPLIT))
                {
                        logger.info("Splitting " + postTxnDuplicates.size() + " duplicate person objects.");
                    // Allow UIDs to be updated in this transaction
                    AlfrescoTransactionSupport.bindResource(KEY_ALLOW_UID_UPDATE, Boolean.TRUE);
                    split(postTxnDuplicates);
                        logger.info("Split " + postTxnDuplicates.size() + " duplicate person objects.");
                }
                else if (duplicateMode.equalsIgnoreCase(DELETE))
                {
                    delete(postTxnDuplicates);
                    logger.info("Deleted duplicate person objects");
                }
                else
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Duplicate person objects exist");
                    }
                }
                
                // Done
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(processDuplicateWork, false, true);
    }

    private void delete(Set<NodeRef> toDelete)
    {
        for (NodeRef nodeRef : toDelete)
        {
            deletePerson(nodeRef);
        }
    }

    private void split(Set<NodeRef> toSplit)
    {
        for (NodeRef nodeRef : toSplit)
        {
            String userName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME);
            String newUserName = userName + GUID.generate();
            nodeService.setProperty(nodeRef, ContentModel.PROP_USERNAME, userName + GUID.generate());
            logger.info("   New person object: " + newUserName);
        }
    }

    private NodeRef findBest(List<NodeRef> refs)
    {
        // Given that we might not have audit attributes, use the assumption that the node ID increases to sort the
        // nodes
        if (lastIsBest)
        {
            Collections.sort(refs, new NodeIdComparator(nodeService, false));
        }
        else
        {
            Collections.sort(refs, new NodeIdComparator(nodeService, true));
        }

        NodeRef fallBack = null;

        for (NodeRef nodeRef : refs)
        {
            if (fallBack == null)
            {
                fallBack = nodeRef;
            }

            if (includeAutoCreated || !wasAutoCreated(nodeRef))
            {
                return nodeRef;
            }
        }

        return fallBack;
    }

    private boolean wasAutoCreated(NodeRef nodeRef)
    {
        String userName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME));

        String testString = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_FIRSTNAME));
        if ((testString == null) || !testString.equals(userName))
        {
            return false;
        }

        testString = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_LASTNAME));
        if ((testString == null) || !testString.equals(""))
        {
            return false;
        }

        testString = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_EMAIL));
        if ((testString == null) || !testString.equals(""))
        {
            return false;
        }

        testString = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_ORGID));
        if ((testString == null) || !testString.equals(""))
        {
            return false;
        }

        testString = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_HOME_FOLDER_PROVIDER));
        if ((testString == null) || !testString.equals(defaultHomeFolderProvider))
        {
            return false;
        }

        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean createMissingPeople()
    {
        return createMissingPeople;
    }
    
    /**
     * {@inheritDoc}
     */
    public Set<QName> getMutableProperties()
    {
        return mutableProperties;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setPersonProperties(String userName, Map<QName, Serializable> properties)
    {
        setPersonProperties(userName, properties, true);
    }
    
    /**
     * {@inheritDoc}
     */
    public void setPersonProperties(String userName, Map<QName, Serializable> properties, boolean autoCreateHomeFolder)
    {
        NodeRef personNode = getPersonOrNull(userName);
        if (personNode == null)
        {
            if (createMissingPeople())
            {
                personNode = createMissingPerson(userName, autoCreateHomeFolder);
            }
            else
            {
                throw new PersonException("No person found for user name " + userName);
            }
        }
        else
        {
            // Must create the home folder first as a property holds its location.
            if (autoCreateHomeFolder)
            {
                makeHomeFolderIfRequired(personNode);                
            }
            String realUserName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(personNode, ContentModel.PROP_USERNAME));
            String suggestedUserName;

            // LDAP sync: allow change of case if we have case insensitive user names and the same name in a different case
            if (getUserNamesAreCaseSensitive()
                    || (suggestedUserName = (String) properties.get(ContentModel.PROP_USERNAME)) == null
                    || !suggestedUserName.equalsIgnoreCase(realUserName))
            {
                properties.put(ContentModel.PROP_USERNAME, realUserName);
            }
        }
        Map<QName, Serializable> update = nodeService.getProperties(personNode);
        update.putAll(properties);

        nodeService.setProperties(personNode, update);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isMutable()
    {
        return true;
    }
    
    private NodeRef createMissingPerson(String userName, boolean autoCreateHomeFolder)
    {
        HashMap<QName, Serializable> properties = getDefaultProperties(userName);
        NodeRef person = createPerson(properties);
        
        // The home folder will ONLY exist after the the person is created if
        // homeFolderCreationEager == true
        if (autoCreateHomeFolder && homeFolderCreationEager == false)
        {
            makeHomeFolderIfRequired(person);                
        }

        return person;
    }
    
    private void makeHomeFolderIfRequired(NodeRef person)
    {
        if (person != null)
        {
            NodeRef homeFolder = DefaultTypeConverter.INSTANCE.convert(NodeRef.class, nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER));
            if (homeFolder == null)
            {
                final ChildAssociationRef ref = nodeService.getPrimaryParent(person);
                transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Throwable
                    {
                        makeHomeFolderAsSystem(ref);
                        return null;
                    }
                }, transactionService.isReadOnly(), transactionService.isReadOnly() ? false : AlfrescoTransactionSupport.getTransactionReadState() == TxnReadState.TXN_READ_ONLY);
            }
        }
    }
    
    private void makeHomeFolderAsSystem(final ChildAssociationRef childAssocRef)
    {
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            @Override
            public Object doWork() throws Exception
            {
                homeFolderManager.makeHomeFolder(childAssocRef);
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    private HashMap<QName, Serializable> getDefaultProperties(String userName)
    {
        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_USERNAME, userName);
        properties.put(ContentModel.PROP_FIRSTNAME, tenantService.getBaseNameUser(userName));
        properties.put(ContentModel.PROP_LASTNAME, "");
        properties.put(ContentModel.PROP_EMAIL, "");
        properties.put(ContentModel.PROP_ORGID, "");
        properties.put(ContentModel.PROP_HOME_FOLDER_PROVIDER, defaultHomeFolderProvider);

        properties.put(ContentModel.PROP_SIZE_CURRENT, 0L);
        properties.put(ContentModel.PROP_SIZE_QUOTA, -1L); // no quota

        return properties;
    }
    
    /**
     * {@inheritDoc}
     */
    public NodeRef createPerson(Map<QName, Serializable> properties)
    {
        return createPerson(properties, authorityService.getDefaultZones());
    }
    
    /**
     * {@inheritDoc}
     */
    public NodeRef createPerson(Map<QName, Serializable> properties, Set<String> zones)
    {
        ParameterCheck.mandatory("properties", properties);
        String userName = DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_USERNAME));
        if (userName == null)
        {
            throw new IllegalArgumentException("No username specified when creating the person.");
        }

        /*
         * Check restrictions on the number of users
         */
        Long maxUsers = repoAdminService.getRestrictions().getUsers();
        if (maxUsers != null)
        {
            // Get the set of users created in this transaction
            Set<String> usersCreated = TransactionalResourceHelper.getSet(KEY_USERS_CREATED);
            usersCreated.add(userName);
            AlfrescoTransactionSupport.bindListener(this);
        }

        AuthorityType authorityType = AuthorityType.getAuthorityType(userName);
        if (authorityType != AuthorityType.USER)
        {
            throw new AlfrescoRuntimeException("Attempt to create person for an authority which is not a user");
        }
        
        tenantService.checkDomainUser(userName);
        
        if (personExists(userName))
        {
            throw new AlfrescoRuntimeException("Person '" + userName + "' already exists.");
        }
        
        properties.put(ContentModel.PROP_USERNAME, userName);
        properties.put(ContentModel.PROP_SIZE_CURRENT, 0L);
        
        NodeRef personRef = null;
        try
        {
            beforeCreateNodeValidationBehaviour.disable();
            
            personRef = nodeService.createNode(
                    getPeopleContainer(),
                    ContentModel.ASSOC_CHILDREN,
                    getChildNameLower(userName), // Lowercase:
                    ContentModel.TYPE_PERSON, properties).getChildRef();
        }
        finally
        {
            beforeCreateNodeValidationBehaviour.enable();
        }
        
        if (zones != null)
        {
            for (String zone : zones)
            {
                // Add the person to an authentication zone (corresponding to an external user registry)
                // Let's preserve case on this child association
                nodeService.addChild(authorityService.getOrCreateZone(zone), personRef, ContentModel.ASSOC_IN_ZONE, QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, userName, namespacePrefixResolver));
            }
        }
        
        removeFromCache(userName);
        
        return personRef;
    }
    
    /**
     * {@inheritDoc}
     */
    public void notifyPerson(final String userName, final String password)
    {
        // Get the details of our user, or fail trying
        NodeRef noderef = getPerson(userName, false);
        Map<QName,Serializable> userProps = nodeService.getProperties(noderef);
        
        // Do they have an email set? We can't email them if not...
        String email = null;
        if (userProps.containsKey(ContentModel.PROP_EMAIL))
        {
            email = (String)userProps.get(ContentModel.PROP_EMAIL);
        }
        
        if (email == null || email.length() == 0)
        {
            if (logger.isInfoEnabled())
            {
                logger.info("Not sending new user notification to " + userName + " as no email address found");
            }
            
            return;
        }
        
        // We need a freemarker model, so turn the QNames into
        //  something a bit more freemarker friendly
        Map<String,Serializable> model = buildEmailTemplateModel(userProps);
        model.put("password", password); // Not stored on the person
        
        // Set the details of the person sending the email into the model
        NodeRef creatorNR = getPerson(AuthenticationUtil.getFullyAuthenticatedUser());
        Map<QName,Serializable> creatorProps = nodeService.getProperties(creatorNR);
        Map<String,Serializable> creator = buildEmailTemplateModel(creatorProps);
        model.put("creator", (Serializable)creator);
        
        // Set share information into the model
        String productName = ModelUtil.getProductName(repoAdminService);
        model.put(TemplateService.KEY_PRODUCT_NAME, productName);
        
        // Set the details for the action
        Map<String,Serializable> actionParams = new HashMap<String, Serializable>();
        actionParams.put(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable)model);
        actionParams.put(MailActionExecuter.PARAM_TO, email);
        actionParams.put(MailActionExecuter.PARAM_FROM, creatorProps.get(ContentModel.PROP_EMAIL));
        actionParams.put(MailActionExecuter.PARAM_SUBJECT, 
                    I18NUtil.getMessage("invitation.notification.person.email.subject", productName));
        
        // Pick the appropriate localised template
        actionParams.put(MailActionExecuter.PARAM_TEMPLATE, getNotifyEmailTemplateNodeRef());
        
        // Ask for the email to be sent asynchronously
        Action mailAction = getActionService().createAction(MailActionExecuter.NAME, actionParams);
        getActionService().executeAction(mailAction, noderef, false, true);
    }
    
    /**
     * Finds the email template and then attempts to find a localized version
     */
    private NodeRef getNotifyEmailTemplateNodeRef()
    {
        // Find the new user email template
        String xpath = "app:company_home/app:dictionary/app:email_templates/cm:invite/cm:new-user-email.html.ftl";
        try
        {
            NodeRef rootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
            List<NodeRef> nodeRefs = searchService.selectNodes(
                    rootNodeRef,
                    xpath,
                    null,
                    getNamespaceService(),
                    false);
            if (nodeRefs.size() > 1)
            {
                logger.error("Found too many email templates using: " + xpath);
                nodeRefs = Collections.singletonList(nodeRefs.get(0));
            }
            else if (nodeRefs.size() == 0)
            {
                throw new InvitationException("Cannot find the email template using " + xpath);
            }
            // Now localise this
            NodeRef base = nodeRefs.get(0);
            NodeRef local = getFileFolderService().getLocalizedSibling(base);
            return local;
        }
        catch (SearcherException e)
        {
            throw new InvitationException("Cannot find the email template!", e);
        }
    }
    
    private Map<String,Serializable> buildEmailTemplateModel(Map<QName,Serializable> props)
    {
        Map<String,Serializable> model = new HashMap<String, Serializable>((int)(props.size()*1.5));
        for (QName qname : props.keySet())
        {
            model.put(qname.getLocalName(), props.get(qname));
            model.put(qname.getLocalName().toLowerCase(), props.get(qname));
        }
        return model;
    }
    
    /**
     * {@inheritDoc}
     */
    public NodeRef getPeopleContainer()
    {
        String cacheKey = tenantService.getCurrentUserDomain();
        NodeRef peopleNodeRef = peopleContainerRefs.get(cacheKey);
        if (peopleNodeRef == null)
        {
            NodeRef rootNodeRef = nodeService.getRootNode(tenantService.getName(storeRef));
            List<ChildAssociationRef> children = nodeService.getChildAssocs(rootNodeRef, RegexQNamePattern.MATCH_ALL,
                    QName.createQName(SYSTEM_FOLDER_SHORT_QNAME, namespacePrefixResolver), false);

            if (children.size() == 0)
            {
                throw new AlfrescoRuntimeException("Required people system path not found: "
                        + SYSTEM_FOLDER_SHORT_QNAME);
            }

            NodeRef systemNodeRef = children.get(0).getChildRef();

            children = nodeService.getChildAssocs(systemNodeRef, RegexQNamePattern.MATCH_ALL, QName.createQName(
                    PEOPLE_FOLDER_SHORT_QNAME, namespacePrefixResolver), false);

            if (children.size() == 0)
            {
                throw new AlfrescoRuntimeException("Required people system path not found: "
                        + PEOPLE_FOLDER_SHORT_QNAME);
            }

            peopleNodeRef = children.get(0).getChildRef();
            peopleContainerRefs.put(cacheKey, peopleNodeRef);
        }
        return peopleNodeRef;
    }
    
    /**
     * {@inheritDoc}
     */
    public void deletePerson(String userName)
    {
        // Normalize the username to avoid case sensitivity issues
        userName = getUserIdentifier(userName);
        if (userName == null)
        {
            return;
        }
        
        NodeRef personRef = getPersonOrNull(userName);
        
        deletePersonImpl(userName, personRef);
    }
    
    /**
     * {@inheritDoc}
     */
    public void deletePerson(NodeRef personRef)
    {
        QName typeQName = nodeService.getType(personRef);
        if (typeQName.equals(ContentModel.TYPE_PERSON))
        {
            String userName = (String) this.nodeService.getProperty(personRef, ContentModel.PROP_USERNAME);
            deletePersonImpl(userName, personRef);  
        }
        else
        {
            throw new AlfrescoRuntimeException("deletePerson: invalid type of node "+personRef+" (actual="+typeQName+", expected="+ContentModel.TYPE_PERSON+")");
        }
    }
    
    private void deletePersonImpl(String userName, NodeRef personRef)
    {
        if (userName != null)
        {
            // Remove internally-stored password information, if any
            try
            {
                authenticationService.deleteAuthentication(userName);
            }
            catch (AuthenticationException e)
            {
                // Ignore this - externally authenticated user
            }
            
            // Invalidate all that user's tickets
            try
            {
                authenticationService.invalidateUserSession(userName);
            }
            catch (AuthenticationException e)
            {
                // Ignore this
            }
            
            // remove any user permissions
            permissionServiceSPI.deletePermissions(userName);
        }
        
        // delete the person
        if (personRef != null)
        {
            try
            {
                beforeDeleteNodeValidationBehaviour.disable();
                
                nodeService.deleteNode(personRef);
            }
            finally
            {
                beforeDeleteNodeValidationBehaviour.enable();
            }
        }
              
        /*
         * Kick off the transaction listener for create user.   It has the side-effect of 
         * recalculating the number of users.
         */
        Long maxUsers = repoAdminService.getRestrictions().getUsers();
        if (maxUsers != null)
        {    
            AlfrescoTransactionSupport.bindListener(this);
        }
  
    }
    
    /**
     * {@inheritDoc}
     * 
     * @deprecated see getPeople
     */
    public Set<NodeRef> getAllPeople()
    {
        List<PersonInfo> personInfos = getPeople(null, true, null, new PagingRequest(Integer.MAX_VALUE, null)).getPage();
        Set<NodeRef> refs = new HashSet<NodeRef>(personInfos.size());
        for (PersonInfo personInfo : personInfos)
        {
            refs.add(personInfo.getNodeRef());
        }
        return refs;
    }
    
    /**
     * {@inheritDoc}
     */
    public PagingResults<PersonInfo> getPeople(List<Pair<QName, String>> stringPropFilters, boolean filterIgnoreCase, List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest)
    {
        ParameterCheck.mandatory("pagingRequest", pagingRequest);
        
        Long start = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);
        
        NodeRef contextNodeRef = getPeopleContainer();
        
        Set<QName> childTypeQNames = new HashSet<QName>(1);
        childTypeQNames.add(ContentModel.TYPE_PERSON);
        
        // get canned query
        GetChildrenCannedQueryFactory getChildrenCannedQueryFactory = (GetChildrenCannedQueryFactory)cannedQueryRegistry.getNamedObject(CANNED_QUERY_PEOPLE_LIST);
        
        List<FilterProp> filterProps = null;
        if (stringPropFilters != null)
        {
            filterProps = new ArrayList<FilterProp>(stringPropFilters.size());
            for (Pair<QName, String> filterProp : stringPropFilters)
            {
                String filterStr = filterProp.getSecond();
                if ((filterStr == null) || (filterStr.equals("")) || (filterStr.equals("*")))
                {
                   // The wildcard means no filtering is needed on this property
                   continue;
                }
                else if (filterStr.endsWith("*"))
                {
                   // The trailing * is implicit
                   filterStr = filterStr.substring(0, filterStr.length()-1);
                }
                
                // Turn this into a canned query filter
                filterProps.add(new FilterPropString(filterProp.getFirst(), filterStr, (filterIgnoreCase ? FilterTypeString.STARTSWITH_IGNORECASE : FilterTypeString.STARTSWITH)));
            }
        }
        
        GetChildrenCannedQuery cq = (GetChildrenCannedQuery)getChildrenCannedQueryFactory.getCannedQuery(contextNodeRef, null, childTypeQNames, filterProps, sortProps, pagingRequest);
        
        // execute canned query
        final CannedQueryResults<NodeRef> results = cq.execute();
        
        final List<NodeRef> nodeRefs;
        if (results.getPageCount() > 0)
        {
            nodeRefs = results.getPages().get(0);
        }
        else
        {
            nodeRefs = Collections.emptyList();
        }
        
        // set total count
        final Pair<Integer, Integer> totalCount;
        if (pagingRequest.getRequestTotalCountMax() > 0)
        {
            totalCount = results.getTotalResultCount();
        }
        else
        {
            totalCount = null;
        }
        
        if (start != null)
        {
            int cnt = results.getPagedResultCount();
            int skipCount = pagingRequest.getSkipCount();
            int maxItems = pagingRequest.getMaxItems();
            boolean hasMoreItems = results.hasMoreItems();
            int pageNum = (skipCount / maxItems) + 1;
            
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "getPeople: "+cnt+" items in "+(System.currentTimeMillis()-start)+" msecs " +
                        		"[pageNum="+pageNum+",skip="+skipCount+",max="+maxItems+",hasMorePages="+hasMoreItems+
                        		",totalCount="+totalCount+",filters="+stringPropFilters+
                        		",filtersIgnoreCase="+filterIgnoreCase+"]");
            }
        }
        
        final List<PersonInfo> personInfos = new ArrayList<PersonInfo>(nodeRefs.size());
        for (NodeRef nodeRef : nodeRefs)
        {
            Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
            personInfos.add(new PersonInfo(nodeRef, 
                                           (String)props.get(ContentModel.PROP_USERNAME), 
                                           (String)props.get(ContentModel.PROP_FIRSTNAME),
                                           (String)props.get(ContentModel.PROP_LASTNAME)));
        }
        
        return new PagingResults<PersonInfo>()
        {
            @Override
            public String getQueryExecutionId()
            {
                return results.getQueryExecutionId();
            }
            @Override
            public List<PersonInfo> getPage()
            {
                return personInfos;
            }
            @Override
            public boolean hasMoreItems()
            {
                return results.hasMoreItems();
            }
            @Override
            public Pair<Integer, Integer> getTotalResultCount()
            {
                return totalCount;
            }
        };
    }
    
    /**
     * {@inheritDoc}
     * 
     * @deprecated see getPeople
     */
    public Set<NodeRef> getPeopleFilteredByProperty(QName propertyKey, Serializable propertyValue)
    {
        // check that given property key is defined for content model type 'cm:person'
        // and throw exception if it isn't
        if (this.dictionaryService.getProperty(ContentModel.TYPE_PERSON, propertyKey) == null)
        {
            throw new AlfrescoRuntimeException("Property '" + propertyKey + "' is not defined " + "for content model type cm:person");
        }
        
        List<Pair<QName, String>> filterProps = new ArrayList<Pair<QName, String>>(1);
        filterProps.add(new Pair<QName, String>(propertyKey, (String)propertyValue));
        
        PagingRequest pagingRequest = new PagingRequest(Integer.MAX_VALUE, null);
        List<PersonInfo> personInfos = getPeople(filterProps, true, null, pagingRequest).getPage();
        
        Set<NodeRef> refs = new HashSet<NodeRef>(personInfos.size());
        for (PersonInfo personInfo : personInfos)
        {
            refs.add(personInfo.getNodeRef());
        }
        
        return refs;
    }

    // Policies
    
    /**
     * {@inheritDoc}
     */
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        NodeRef personRef = childAssocRef.getChildRef();
        
        String userName = (String) this.nodeService.getProperty(personRef, ContentModel.PROP_USERNAME);
        
        if (getPeopleContainer().equals(childAssocRef.getParentRef()))
        {
            removeFromCache(userName);
        }
        
        permissionsManager.setPermissions(personRef, userName, userName);
        
        // Make sure there is an authority entry - with a DB constraint for uniqueness
        // aclDao.createAuthority(username);
        
        if (homeFolderCreationEager)
        {
            makeHomeFolderAsSystem(childAssocRef);
        }
    }
    
    private QName getChildNameLower(String userName)
    {
        return QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, userName.toLowerCase(), namespacePrefixResolver);
    }
    
    public void beforeCreateNode(
            NodeRef parentRef,
            QName assocTypeQName,
            QName assocQName,
            QName nodeTypeQName)
    {
        // NOOP
    }
    
    public void beforeCreateNodeValidation(
            NodeRef parentRef,
            QName assocTypeQName,
            QName assocQName,
            QName nodeTypeQName)
    {
        if (getPeopleContainer().equals(parentRef))
        {
            throw new AlfrescoRuntimeException("beforeCreateNode: use PersonService to create person");
        }
        else
        {
            logger.info("Person node is not being created under the people container (actual="+parentRef+", expected="+getPeopleContainer()+")");
        }
    }
    
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        String userName = (String) this.nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME);
        if (this.authorityService.isGuestAuthority(userName))
        {
            throw new AlfrescoRuntimeException("The " + userName + " user cannot be deleted.");
        }
        
        NodeRef parentRef = null;
        ChildAssociationRef parentAssocRef = nodeService.getPrimaryParent(nodeRef);
        if (parentAssocRef != null)
        {
            parentRef = parentAssocRef.getParentRef();
            if (getPeopleContainer().equals(parentRef))
            {
                removeFromCache(userName);
            }
        }
    }
    
    public void beforeDeleteNodeValidation(NodeRef nodeRef)
    {
        NodeRef parentRef = null;
        ChildAssociationRef parentAssocRef = nodeService.getPrimaryParent(nodeRef);
        if (parentAssocRef != null)
        {
            parentRef = parentAssocRef.getParentRef();
        }
        
        if (getPeopleContainer().equals(parentRef))
        {
            throw new AlfrescoRuntimeException("beforeDeleteNode: use PersonService to delete person");
        }
        else
        {
            logger.info("Person node that is being deleted is not under the parent people container (actual="+parentRef+", expected="+getPeopleContainer()+")");
        }
    }
    
    private Set<NodeRef> getFromCache(String userName)
    {
        return this.personCache.get(userName.toLowerCase());
    }
    
    private void putToCache(String userName, Set<NodeRef> refs)
    {
        this.personCache.put(userName.toLowerCase(), refs);
    }
    
    private void removeFromCache(String userName)
    {
        this.personCache.remove(userName.toLowerCase());
    }

    /**
     * {@inheritDoc}
     */
    public String getUserIdentifier(String caseSensitiveUserName)
    {
        NodeRef nodeRef = getPersonOrNull(caseSensitiveUserName);
        if ((nodeRef != null) && nodeService.exists(nodeRef))
        {
            String realUserName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME));
            return realUserName;
        }
        return null;
    }

    public static class NodeIdComparator implements Comparator<NodeRef>
    {
        private NodeService nodeService;

        boolean ascending;

        NodeIdComparator(NodeService nodeService, boolean ascending)
        {
            this.nodeService = nodeService;
            this.ascending = ascending;
        }

        public int compare(NodeRef first, NodeRef second)
        {
            Long firstId = DefaultTypeConverter.INSTANCE.convert(Long.class, nodeService.getProperty(first, ContentModel.PROP_NODE_DBID));
            Long secondId = DefaultTypeConverter.INSTANCE.convert(Long.class, nodeService.getProperty(second, ContentModel.PROP_NODE_DBID));

            if (firstId != null)
            {
                if (secondId != null)
                {
                    return firstId.compareTo(secondId) * (ascending ? 1 : -1);
                }
                else
                {
                    return ascending ? -1 : 1;
                }
            }
            else
            {
                if (secondId != null)
                {
                    return ascending ? 1 : -1;
                }
                else
                {
                    return 0;
                }
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean getUserNamesAreCaseSensitive()
    {
        return userNameMatcher.getUserNamesAreCaseSensitive();
    }

    /**
     * When a uid is changed we need to create an alias for the old uid so permissions are not broken. This can happen
     * when an already existing user is updated via LDAP e.g. migration to LDAP, or when a user is auto created and then
     * updated by LDAP This is probably less likely after 3.2 and sync on missing person See
     * https://issues.alfresco.com/jira/browse/ETWOTWO-389 (non-Javadoc)
     */
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        String uidBefore = DefaultTypeConverter.INSTANCE.convert(String.class, before.get(ContentModel.PROP_USERNAME));
        if (uidBefore == null)
        {
            // Node has just been created; nothing to do
            return;
        }
        String uidAfter = DefaultTypeConverter.INSTANCE.convert(String.class, after.get(ContentModel.PROP_USERNAME));
        if (!EqualsHelper.nullSafeEquals(uidBefore, uidAfter))
        {
            // Only allow UID update if we are in the special split processing txn or we are just changing case
            if (AlfrescoTransactionSupport.getResource(KEY_ALLOW_UID_UPDATE) != null || uidBefore.equalsIgnoreCase(uidAfter))
            {
                if (uidBefore != null)
                {
                    // Fix any ACLs
                    aclDao.renameAuthority(uidBefore, uidAfter);
                }
                
                // Fix primary association local name
                QName newAssocQName = getChildNameLower(uidAfter);
                ChildAssociationRef assoc = nodeService.getPrimaryParent(nodeRef);
                nodeService.moveNode(nodeRef, assoc.getParentRef(), assoc.getTypeQName(), newAssocQName);
                
                // Fix other non-case sensitive parent associations
                QName oldAssocQName = QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, uidBefore, namespacePrefixResolver);
                newAssocQName = QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, uidAfter, namespacePrefixResolver);
                for (ChildAssociationRef parent : nodeService.getParentAssocs(nodeRef))
                {
                    if (!parent.isPrimary() && parent.getQName().equals(oldAssocQName))
                    {
                        nodeService.removeChildAssociation(parent);
                        nodeService.addChild(parent.getParentRef(), parent.getChildRef(), parent.getTypeQName(), newAssocQName);
                    }
                }
                
                // Fix cache
                removeFromCache(uidBefore);
            }
            else
            {
                throw new UnsupportedOperationException("The user name on a person can not be changed");
            }
        }
    }
    
    /**
     * Track the {@link ContentModel#PROP_ENABLED enabled/disabled} flag on {@link ContentModel#TYPE_USER <b>cm:user</b>}.
     */
    public void onUpdatePropertiesUser(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        String userName = (String) after.get(ContentModel.PROP_USER_USERNAME);
        if (userName == null)
        {
            // Won't find user
            return;
        }
        // Get the person
        NodeRef personNodeRef = getPersonOrNull(userName);
        if (personNodeRef == null)
        {
            // Don't attempt to maintain enabled/disabled flag
            return;
        }
        
        // Check the enabled/disabled flag
        Boolean enabled = (Boolean) after.get(ContentModel.PROP_ENABLED);
        if (enabled == null || enabled.booleanValue())
        {
            nodeService.removeAspect(personNodeRef, ContentModel.ASPECT_PERSON_DISABLED);
        }
        else
        {
            nodeService.addAspect(personNodeRef, ContentModel.ASPECT_PERSON_DISABLED, null);
        }
        
        // Do post-commit user counting, if required
        Set<String> usersCreated = TransactionalResourceHelper.getSet(KEY_USERS_CREATED);
        usersCreated.add(userName);
        AlfrescoTransactionSupport.bindListener(this);
    }
    
    /**
     * {@inheritDoc}
     */
    public void beforeCommit(boolean readOnly)
    {
        // check whether max users has been exceeded
        RunAsWork<Long> getMaxUsersWork = new RunAsWork<Long>()
        {
            @Override
            public Long doWork() throws Exception
            {
                return repoAdminService.getRestrictions().getUsers();
            }
        };
        Long maxUsers = AuthenticationUtil.runAs(getMaxUsersWork, AuthenticationUtil.getSystemUserName());
        if(maxUsers == null)
        {
            return;
        }
    
        Long users = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Long>()
        {
            public Long doWork() throws Exception
            {
                repoAdminService.updateUsage(UsageType.USAGE_USERS);
                if(logger.isDebugEnabled())
                {
                    logger.debug("Number of users is " + repoAdminService.getUsage().getUsers());
                }
                return repoAdminService.getUsage().getUsers();
            }
        } , AuthenticationUtil.getSystemUserName());

        // Get the set of users created in this transaction
        Set<String> usersCreated = TransactionalResourceHelper.getSet(KEY_USERS_CREATED);
        
        // If we exceed the limit, generate decent message about which users were being created, etc.
        if (users > maxUsers)
        {
            List<String> usersMsg = new ArrayList<String>(5);
            int i = 0;
            for (String userCreated : usersCreated)
            {
                i++;
                if (i > 5)
                {
                    usersMsg.add(" ... more");
                    break;
                }
                else
                {
                    usersMsg.add(userCreated);
                }
            }
            if (logger.isDebugEnabled())
            {
                logger.debug("Maximum number of users exceeded: " + usersCreated);
            }
            throw AlfrescoRuntimeException.create(SYSTEM_USAGE_WARN_LIMIT_USERS_EXCEEDED_VERBOSE, maxUsers, usersMsg);
        }
        
        // Get the usages and log any warnings
        RepoUsageStatus usageStatus = repoAdminService.getUsageStatus();
        usageStatus.logMessages(logger);
    }
    
    /**
     * Helper for when creating new users and people:
     * Updates the supplied username with any required tenant
     *  details, and ensures that the tenant domains match.
     * If Multi-Tenant is disabled, returns the same username.
     */
    public static String updateUsernameForTenancy(String username, TenantService tenantService) 
            throws TenantDomainMismatchException
    {
        if(! tenantService.isEnabled())
        {
            // Nothing to do if not using multi tenant
            return username;
        }
        
        String currentDomain = tenantService.getCurrentUserDomain();
        if (! currentDomain.equals(TenantService.DEFAULT_DOMAIN))
        {
            if (! tenantService.isTenantUser(username))
            {
                // force domain onto the end of the username
                username = tenantService.getDomainUser(username, currentDomain);
                logger.warn("Added domain to username: " + username);
            }
            else
            {
                // Check the user's domain matches the current domain
                // Throws a TenantDomainMismatchException if they don't match
                tenantService.checkDomainUser(username);
            }
        }
        return username;
    }
}