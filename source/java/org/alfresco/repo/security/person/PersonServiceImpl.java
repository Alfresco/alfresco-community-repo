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
import java.util.StringTokenizer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.cache.TransactionalCache;
import org.alfresco.repo.domain.permissions.AclDAO;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
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
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.admin.RepoUsage.UsageType;
import org.alfresco.service.cmr.admin.RepoUsageStatus;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.invitation.InvitationException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PermissionService;
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

public class PersonServiceImpl extends TransactionListenerAdapter implements PersonService,
                                                                             NodeServicePolicies.BeforeCreateNodePolicy,
                                                                             NodeServicePolicies.OnCreateNodePolicy,
                                                                             NodeServicePolicies.BeforeDeleteNodePolicy,
                                                                             NodeServicePolicies.OnUpdatePropertiesPolicy
                                                                             
{
    private static Log logger = LogFactory.getLog(PersonServiceImpl.class);
    
    private static final String CANNED_QUERY_PEOPLE_LIST = "getPeopleCannedQueryFactory";

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
    
    // note: cache is tenant-aware (if using EhCacheAdapter shared cache)
    private SimpleCache<String, Object> singletonCache; // eg. for peopleContainerNodeRef
    private final String KEY_PEOPLECONTAINER_NODEREF = "key.peoplecontainer.noderef";
    
    private UserNameMatcher userNameMatcher;
    
    private JavaBehaviour beforeCreateNodeValidationBehaviour;
    private JavaBehaviour beforeDeleteNodeValidationBehaviour;
    
    private boolean homeFolderCreationEager;
    
    private boolean homeFolderCreationDisabled = false; // if true then home folders creation is disabled (ie. home folders are not created - neither eagerly nor lazily)
    
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
    
    public void setSingletonCache(SimpleCache<String, Object> singletonCache)
    {
        this.singletonCache = singletonCache;
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
    
    /**
     * Indicates if home folder creation should be disabled.
     */
    public void setHomeFolderCreationDisabled(boolean homeFolderCreationDisabled)
    {
        this.homeFolderCreationDisabled = homeFolderCreationDisabled;
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
    public PersonInfo getPerson(NodeRef personRef) throws NoSuchPersonException
    {
        Map<QName, Serializable> props = null;
        try
        {
            props = nodeService.getProperties(personRef);
        }
        catch (InvalidNodeRefException inre)
        {
            throw new NoSuchPersonException(personRef.toString());
        }
        
        String username  = (String)props.get(ContentModel.PROP_USERNAME);
        if (username == null)
        {
            throw new NoSuchPersonException(personRef.toString());
        }
        
        return new PersonInfo(personRef, 
                              username, 
                              (String)props.get(ContentModel.PROP_FIRSTNAME),
                              (String)props.get(ContentModel.PROP_LASTNAME));
    }
    
    /**
     * {@inheritDoc}
     */
    public NodeRef getPersonOrNull(String userName)
    {
        return getPersonImpl(userName, false, false);
    }
    
    /**
     * {@inheritDoc}
     */
    public NodeRef getPerson(final String userName, final boolean autoCreateHomeFolderAndMissingPersonIfAllowed)
    {
        return getPersonImpl(userName, autoCreateHomeFolderAndMissingPersonIfAllowed, true);
    }
    
    
    private NodeRef getPersonImpl(
            final String userName,
            final boolean autoCreateHomeFolderAndMissingPersonIfAllowed,
            final boolean exceptionOrNull)
    {
        if (userName == null || userName.length() == 0)
        {
            return null;
        }
        final NodeRef personNode = getPersonOrNullImpl(userName);
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
                if (exceptionOrNull)
                {
                    throw new NoSuchPersonException(userName);
                }
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
        NodeRef person = getPersonOrNullImpl(caseSensitiveUserName); 
        if (person != null)
        {
            // re: THOR-293
            return permissionServiceSPI.hasPermission(person, PermissionService.READ) == AccessStatus.ALLOWED;
        }
        return false;
    }
    
    private NodeRef getPersonOrNullImpl(String searchUserName)
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
                putToCache(searchUserName, allRefs, false);
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
        NodeRef personNode = getPersonOrNullImpl(userName);
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
        if ((person != null) && (homeFolderCreationDisabled == false))
        {
            NodeRef homeFolder = DefaultTypeConverter.INSTANCE.convert(NodeRef.class, nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER));
            if (homeFolder == null)
            {
                final ChildAssociationRef ref = nodeService.getPrimaryParent(person);
                RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
                txnHelper.setForceWritable(true);
                boolean requiresNew = false;
                if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_READ_WRITE)
                {
                    // We can be in a read-only transaction, so force a new transaction
                    // Note that the transaction will *always* be in read-only mode if the server read-only veto is there 
                    requiresNew = true;
                }
                txnHelper.doInTransaction(new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Throwable
                    {
                        makeHomeFolderAsSystem(ref);
                        return null;
                    }
                }, false, requiresNew);
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
        
        removeFromCache(userName, false);
        
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
        actionParams.put(MailActionExecuter.PARAM_SUBJECT, "invitation.notification.person.email.subject");
        actionParams.put(MailActionExecuter.PARAM_SUBJECT_PARAMS, new Object[] {productName});
        
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
        NodeRef peopleNodeRef = (NodeRef)singletonCache.get(KEY_PEOPLECONTAINER_NODEREF);
        if (peopleNodeRef == null)
        {
            NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
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
            singletonCache.put(KEY_PEOPLECONTAINER_NODEREF, peopleNodeRef);
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
        
        NodeRef personRef = getPersonOrNullImpl(userName);
        
        deletePersonAndAuthenticationImpl(userName, personRef);
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
            deletePersonAndAuthenticationImpl(userName, personRef);  
        }
        else
        {
            throw new AlfrescoRuntimeException("deletePerson: invalid type of node "+personRef+" (actual="+typeQName+", expected="+ContentModel.TYPE_PERSON+")");
        }
    }

    /**
     * {@inheritDoc} 
     */
    public void deletePerson(NodeRef personRef, boolean deleteAuthentication)
    {
        QName typeQName = nodeService.getType(personRef);
        if (typeQName.equals(ContentModel.TYPE_PERSON))
        {
            if (deleteAuthentication)
            {
                String userName = (String) this.nodeService.getProperty(personRef, ContentModel.PROP_USERNAME);
                deletePersonAndAuthenticationImpl(userName, personRef);
            }
            else
            {
                deletePersonImpl(personRef);
            }
        }
        else
        {
            throw new AlfrescoRuntimeException("deletePerson: invalid type of node "+personRef+" (actual="+typeQName+", expected="+ContentModel.TYPE_PERSON+")");
        }
    }
    
    
    private void deletePersonAndAuthenticationImpl(String userName, NodeRef personRef)
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
        
        deletePersonImpl(personRef);
    }
    
    private void deletePersonImpl(NodeRef personRef)
    {
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
        List<PersonInfo> personInfos = getPeople(null, null, null, new PagingRequest(Integer.MAX_VALUE, null)).getPage();
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
    public PagingResults<PersonInfo> getPeople(String pattern, List<QName> filterStringProps, List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest)
    {
        return getPeople(pattern, filterStringProps, null, null, true, sortProps, pagingRequest);
    }
    
    /**
     * {@inheritDoc}
     */
    public PagingResults<PersonInfo> getPeople(String pattern, List<QName> filterStringProps, Set<QName> inclusiveAspects, Set<QName> exclusiveAspects, boolean includeAdministraotrs, List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest)
    {
        ParameterCheck.mandatory("pagingRequest", pagingRequest);
        
        Long start = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);
        
        CannedQueryResults<NodeRef> cqResults = null;
        
        NodeRef contextNodeRef = getPeopleContainer();
        
        // get canned query
        GetPeopleCannedQueryFactory getPeopleCannedQueryFactory = (GetPeopleCannedQueryFactory)cannedQueryRegistry.getNamedObject(CANNED_QUERY_PEOPLE_LIST);
        
        GetPeopleCannedQuery cq = (GetPeopleCannedQuery)getPeopleCannedQueryFactory.getCannedQuery(contextNodeRef, pattern, filterStringProps, inclusiveAspects, exclusiveAspects, includeAdministraotrs, sortProps, pagingRequest);
        
        // execute canned query
        cqResults = cq.execute();
        
        final CannedQueryResults<NodeRef> results = cqResults;
        
        boolean nonFinalHasMoreItems = results.hasMoreItems();
        List<NodeRef> nodeRefs;
        if (results.getPageCount() > 0)
        {
            nodeRefs = results.getPages().get(0);
            if (nodeRefs.size() > pagingRequest.getMaxItems())
            {
                // eg. since hasMoreItems added one (for a pre-paged result)
                nodeRefs = nodeRefs.subList(0, pagingRequest.getMaxItems());
                nonFinalHasMoreItems = true;
            }
        }
        else
        {
            nodeRefs = Collections.emptyList();
        }
        
        final boolean hasMoreItems = nonFinalHasMoreItems;
        
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
            int cnt = nodeRefs.size();
            int skipCount = pagingRequest.getSkipCount();
            int maxItems = pagingRequest.getMaxItems();
            int pageNum = (skipCount / maxItems) + 1;
            
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "getPeople: "+cnt+" items in "+(System.currentTimeMillis()-start)+" msecs " +
                                "[pageNum="+pageNum+",skip="+skipCount+",max="+maxItems+",hasMorePages="+hasMoreItems+
                                ",totalCount="+totalCount+",pattern="+pattern+",filterStringProps="+filterStringProps+
                                ",sortProps="+sortProps+"]");
            }
        }
        
        final List<PersonInfo> personInfos = new ArrayList<PersonInfo>(nodeRefs.size());
        for (NodeRef nodeRef : nodeRefs)
        {
            if (nodeService.exists(nodeRef))
            {
                Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
                personInfos.add(new PersonInfo(nodeRef, 
                                               (String)props.get(ContentModel.PROP_USERNAME), 
                                               (String)props.get(ContentModel.PROP_FIRSTNAME),
                                               (String)props.get(ContentModel.PROP_LASTNAME)));
            }
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
                return hasMoreItems;
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
     * @deprecated see getPeople(String pattern, List<QName> filterProps, List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest)
     */
    public PagingResults<PersonInfo> getPeople(List<Pair<QName, String>> stringPropFilters, boolean filterIgnoreCase, List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest)
    {
        ParameterCheck.mandatory("pagingRequest", pagingRequest);
        
        if (stringPropFilters == null)
        {
            return getPeople(null, null, sortProps, pagingRequest);
        }
        
        String firstName = "";
        String lastName = "";
        String userName = "";
        for (Pair<QName, String> item : stringPropFilters)
        {
            if (ContentModel.PROP_FIRSTNAME.equals(item.getFirst()))
            {
                firstName = item.getSecond().trim();
            }
            if (ContentModel.PROP_LASTNAME.equals(item.getFirst()))
            {
                lastName = item.getSecond().trim();
            }
            if (ContentModel.PROP_USERNAME.equals(item.getFirst()))
            {
                userName = item.getSecond().trim();
            }
        }
        String searchStr = "";
        boolean useCQ = false;
        if (userName.length() == 0)
        {
            if (firstName.equalsIgnoreCase(lastName))
            {
                searchStr = firstName;
                useCQ = true;
            }
            else
            {
                searchStr = firstName + " " + lastName;
            }
        }
        else
        {
            searchStr = userName;
            useCQ = searchStr.split(" ").length == 1;
        }
        
        PagingResults<PersonInfo> result = null;
        if (useCQ)
        {
            List<QName> filterProps = new ArrayList<QName>(3);
            filterProps.add(ContentModel.PROP_FIRSTNAME);
            filterProps.add(ContentModel.PROP_LASTNAME);
            filterProps.add(ContentModel.PROP_USERNAME);
            sortProps = sortProps == null ? new ArrayList<Pair<QName, Boolean>>(1) : new ArrayList<Pair<QName, Boolean>>(sortProps);
            sortProps.add(new Pair<QName, Boolean>(ContentModel.PROP_USERNAME, true)); 
            result = getPeople(searchStr, filterProps, sortProps, pagingRequest);
            
            // Fall back to FTS if no results. For case:  First Name: Gerard, Last Name: Perez Winkler
            if (result.getPage().size() == 0)
            {
                result = null;
            }
        }
        
        if (result == null)
        {
            result = getPeopleFts(searchStr, pagingRequest);
        }
        
        return result;
    }
    
    /**
     * Get paged list of people optionally filtered and/or sorted using FTS
     * 
     * @param pattern - String to search
     * @param pagingRequest
     * @return
     */
    private PagingResults<PersonInfo> getPeopleFts(String pattern, PagingRequest pagingRequest)
    {
        Long start = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);
        List<NodeRef> people = null;
        try
        {
            people = getPeopleFtsList(pattern, pagingRequest);
        }
        catch (Throwable e1)
        {
            // search is failed
        }

        List<NodeRef> nodeRefs;
        boolean nonFinalHasMoreItems = false;
        if (people != null && people.size() > 0)
        {
            nodeRefs = people;
            if (nodeRefs.size() > pagingRequest.getMaxItems())
            {
                // eg. since hasMoreItems added one (for a pre-paged result)
                nodeRefs = nodeRefs.subList(0, pagingRequest.getMaxItems());
                nonFinalHasMoreItems = true;
            }
        }
        else
        {
            nodeRefs = Collections.emptyList();
        }
        if (people == null || people.size() == 0)
        {
            nodeRefs = Collections.emptyList();
        }
        
        final boolean hasMoreItems = nonFinalHasMoreItems;

        // set total count
        final Pair<Integer, Integer> totalCount;
        if (pagingRequest.getRequestTotalCountMax() > 0)
        {
            int size = people != null ? people.size() : 0;
            totalCount = new Pair<Integer, Integer>(size, size);
        }
        else
        {
            totalCount = null;
        }
        
        if (start != null)
        {
            int cnt = nodeRefs.size();
            int skipCount = pagingRequest.getSkipCount();
            int maxItems = pagingRequest.getMaxItems();
            int pageNum = (skipCount / maxItems) + 1;
            
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "getPeople: " + cnt + " items in " + (System.currentTimeMillis() - start) + " msecs " +
                        "[pageNum=" + pageNum + ",skip=" + skipCount + ",max="+ maxItems + ",hasMorePages=" + hasMoreItems +
                        ",totalCount=" + totalCount + ",pattern=" + pattern + "]");
            }
        }
        
        final List<PersonInfo> personInfos = new ArrayList<PersonInfo>(nodeRefs.size());
        for (NodeRef nodeRef : nodeRefs)
        {
            try
            {
                Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
                personInfos.add(new PersonInfo(nodeRef,
                                (String)props.get(ContentModel.PROP_USERNAME),
                                (String) props.get(ContentModel.PROP_FIRSTNAME),
                                (String) props.get(ContentModel.PROP_LASTNAME)));
            }
            catch (InvalidNodeRefException e)
            {
                logger.warn("Stale search result", e);
            }
        }

        return new PagingResults<PersonInfo>()
        {
            @Override
            public String getQueryExecutionId()
            {
                // it's FTS search
                return null;
            }

            @Override
            public List<PersonInfo> getPage()
            {
                return personInfos;
            }

            @Override
            public boolean hasMoreItems()
            {
                return hasMoreItems;
            }

            @Override
            public Pair<Integer, Integer> getTotalResultCount()
            {
                return totalCount;
            }
        };
    }
    
    private List<NodeRef> getPeopleFtsList(String pattern, PagingRequest pagingRequest) throws Throwable
    {
        // Think this code is based on org.alfresco.repo.jscript.People.getPeopleImplSearch(String, StringTokenizer, int, int)
        List<NodeRef> people = null;

        SearchParameters params = new SearchParameters();
        params.addQueryTemplate("_PERSON", "|%firstName OR |%lastName OR |%userName");
        params.setDefaultFieldName("_PERSON");

        StringBuilder query = new StringBuilder(256);

        query.append("TYPE:\"").append(ContentModel.TYPE_PERSON).append("\" AND (");

        StringTokenizer t = new StringTokenizer(pattern, " ");

        if (t.countTokens() == 1)
        {
            // fts-alfresco property search i.e. location:"maidenhead"
            query.append('"').append(pattern).append("*\"");
        }
        else
        {
            // multiple terms supplied - look for first and second name etc.
            // assume first term is first name, any more are second i.e.
            // "Fraun van de Wiels"
            // also allow fts-alfresco property search to reduce results
            params.setDefaultOperator(SearchParameters.Operator.AND);
            StringBuilder multiPartNames = new StringBuilder(pattern.length());
            int numOfTokens = t.countTokens();
            int counter = 1;
            String term = null;
            // MNT-8539, in order to support firstname and lastname search
            while (t.hasMoreTokens())
            {
                term = t.nextToken();
                // ALF-11311, in order to support multi-part
                // firstNames/lastNames, we need to use the whole tokenized term for both
                // firstName and lastName
                if (term.endsWith("*"))
                {
                    term = term.substring(0, term.lastIndexOf("*"));
                }
                multiPartNames.append("\"");
                multiPartNames.append(term);
                multiPartNames.append("*\"");
                if (numOfTokens > counter)
                {
                    multiPartNames.append(' ');
                }
                counter++;
            }
            // ALF-11311, in order to support multi-part firstNames/lastNames,
            // we need to use the whole tokenized term for both firstName and lastName.
            // e.g. "john junior lewis martinez", where "john junior" is the first
            // name and "lewis martinez" is the last name.
            if (multiPartNames.length() > 0)
            {
                query.append("firstName:");
                query.append(multiPartNames);
                query.append(" OR lastName:");
                query.append(multiPartNames);
            }
        }
        query.append(")");

        // define the search parameters
        params.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        params.addStore(this.storeRef);
        params.setQuery(query.toString());
        if (pagingRequest.getMaxItems() > 0)
        {
            params.setLimitBy(LimitBy.FINAL_SIZE);
            params.setLimit(pagingRequest.getMaxItems());
        }

        ResultSet results = null;
        try
        {
            results = searchService.query(params);
            people = results.getNodeRefs();
        }
        catch (Throwable err)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Failed to execute people search: " + query.toString(), err);
            }

            throw err;
        }
        finally
        {
            if (results != null)
            {
                results.close();
            }
        }

        return people;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Set<NodeRef> getPeopleFilteredByProperty(QName propertyKey, Serializable propertyValue, int count)
    {
        if (count > 1000)
        {
            throw new IllegalArgumentException("Only 1000 results are allowed but got a request for " + count + ". Use getPeople.");
        }
        
        // check that given property key is defined for content model type 'cm:person'
        // and throw exception if it isn't
        if (this.dictionaryService.getProperty(ContentModel.TYPE_PERSON, propertyKey) == null)
        {
            throw new AlfrescoRuntimeException("Property '" + propertyKey + "' is not defined " + "for content model type cm:person");
        }
        if (!propertyKey.equals(ContentModel.PROP_FIRSTNAME) &&
            !propertyKey.equals(ContentModel.PROP_LASTNAME) &&
            !propertyKey.equals(ContentModel.PROP_USERNAME))
        {
            logger.warn("PersonService.getPeopleFilteredByProperty() is being called to find people by "+propertyKey+
                    ". Only PROP_FIRSTNAME, PROP_LASTNAME, PROP_USERNAME are now used in the search, so fewer nodes may " +
                    "be returned than expected of there are more than "+count+" users in total.");
        }
        
        List<Pair<QName, String>> filterProps = new ArrayList<Pair<QName, String>>(1);
        filterProps.add(new Pair<QName, String>(propertyKey, (String)propertyValue));
        
        PagingRequest pagingRequest = new PagingRequest(count, null);
        List<PersonInfo> personInfos = getPeople(filterProps, true, null, pagingRequest).getPage();
        
        Set<NodeRef> refs = new HashSet<NodeRef>(personInfos.size());
        for (PersonInfo personInfo : personInfos)
        {
            NodeRef nodeRef = personInfo.getNodeRef();
            String value = (String) this.nodeService.getProperty(nodeRef, propertyKey);
            if (EqualsHelper.nullSafeEquals(value, propertyValue))
            {
                refs.add(nodeRef);
            }
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
            // The value is stale.  However, we have already made the data change and
            // therefore do not need to lock the removal from further changes.
            removeFromCache(userName, false);
        }
        
        permissionsManager.setPermissions(personRef, userName, userName);
        
        // Make sure there is an authority entry - with a DB constraint for uniqueness
        // aclDao.createAuthority(username);
        
        if ((homeFolderCreationEager) && (homeFolderCreationDisabled == false))
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
                // Remove the cache entry.
                // Note that the associated node has not been deleted and is therefore still
                // visible to any other code that attempts to see it.  We therefore need to
                // prevent the value from being added back before the node is actually
                // deleted.
                removeFromCache(userName, true);
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

    /**
     * Put a value into the {@link #setPersonCache(SimpleCache) personCache}, optionally
     * locking the value against any changes.
     */
    private void putToCache(String userName, Set<NodeRef> refs, boolean lock)
    {
        String key = userName.toLowerCase();
        this.personCache.put(key, refs);
        if (lock && personCache instanceof TransactionalCache)
        {
            TransactionalCache<String, Set<NodeRef>> personCacheTxn = (TransactionalCache<String, Set<NodeRef>>) personCache;
            personCacheTxn.lockValue(key);
        }
    }
    
    /**
     * Remove a value from the {@link #setPersonCache(SimpleCache) personCache}, optionally
     * locking the value against any changes.
     */
    private void removeFromCache(String userName, boolean lock)
    {
        String key = userName.toLowerCase();
        personCache.remove(key);
        if (lock && personCache instanceof TransactionalCache)
        {
            TransactionalCache<String, Set<NodeRef>> personCacheTxn = (TransactionalCache<String, Set<NodeRef>>) personCache;
            personCacheTxn.lockValue(key);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getUserIdentifier(String caseSensitiveUserName)
    {
        NodeRef nodeRef = getPersonOrNullImpl(caseSensitiveUserName);
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
                // We are going to be pessimistic here.  Even though the properties have changed and
                // should always be seen correctly by other policy listeners, we are not entirely sure
                // that there won't be some sort of corruption i.e. the behaviour was pessimistic before
                // this change so I'm leaving it that way.
                removeFromCache(uidBefore, true);
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
        NodeRef personNodeRef = getPersonOrNullImpl(userName);
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
    
    public int countPeople()
    {
        NodeRef peopleContainer = getPeopleContainer();
        return nodeService.countChildAssocs(peopleContainer, true);
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
    
    @Override
    public boolean isEnabled(String userName)
    {
        NodeRef noderef = getPerson(userName, false);
        
        for (QName aspectName : nodeService.getAspects(noderef))
        {
            if (ContentModel.ASPECT_PERSON_DISABLED.isMatch(aspectName))
            {
                return false;
            }
        }

        return true;
    }
}