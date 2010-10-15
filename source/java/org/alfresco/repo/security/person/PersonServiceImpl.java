/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.domain.permissions.AclDAO;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.PermissionServiceSPI;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
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
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PersonServiceImpl extends TransactionListenerAdapter implements PersonService,
                                                                             NodeServicePolicies.BeforeCreateNodePolicy,
                                                                             NodeServicePolicies.OnCreateNodePolicy,
                                                                             NodeServicePolicies.BeforeDeleteNodePolicy,
                                                                             NodeServicePolicies.OnUpdatePropertiesPolicy
{
    private static Log logger = LogFactory.getLog(PersonServiceImpl.class);

    private static final String DELETE = "DELETE";

    private static final String SPLIT = "SPLIT";

    private static final String LEAVE = "LEAVE";

    public static final String SYSTEM_FOLDER_SHORT_QNAME = "sys:system";

    public static final String PEOPLE_FOLDER_SHORT_QNAME = "sys:people";

    // IOC

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
    
    private BehaviourFilter policyBehaviourFilter;

    private boolean createMissingPeople;

    private static Set<QName> mutableProperties;

    private String defaultHomeFolderProvider;

    private boolean processDuplicates = true;

    private String duplicateMode = LEAVE;

    private boolean lastIsBest = true;

    private boolean includeAutoCreated = false;

    private AclDAO aclDao;

    private PermissionsManager permissionsManager;
    
    /** a transactionally-safe cache to be injected */
    private SimpleCache<String, Set<NodeRef>> personCache;
    
    /** People Container ref cache (Tennant aware) */
    private Map<String, NodeRef> peopleContainerRefs = new ConcurrentHashMap<String, NodeRef>(4);
    
    private UserNameMatcher userNameMatcher;
    
    private JavaBehaviour beforeCreateNodeValidationBehaviour;
    private JavaBehaviour beforeDeleteNodeValidationBehaviour;
    
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
        PropertyCheck.mandatory(this, "searchService", searchService);
        PropertyCheck.mandatory(this, "permissionServiceSPI", permissionServiceSPI);
        PropertyCheck.mandatory(this, "authorityService", authorityService);
        PropertyCheck.mandatory(this, "authenticationService", authenticationService);
        PropertyCheck.mandatory(this, "namespacePrefixResolver", namespacePrefixResolver);
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
        PropertyCheck.mandatory(this, "personCache", personCache);
        PropertyCheck.mandatory(this, "aclDao", aclDao);
        PropertyCheck.mandatory(this, "homeFolderManager", homeFolderManager);
        
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
    }

    public UserNameMatcher getUserNameMatcher()
    {
        return userNameMatcher;
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
    
    public void setAclDAO(AclDAO aclDao)
    {
        this.aclDao = aclDao;
    }

    public void setPermissionsManager(PermissionsManager permissionsManager)
    {
        this.permissionsManager = permissionsManager;
    }

    /**
     * Set the username to person cache.
     * 
     * @param personCache
     *            a transactionally safe cache
     */
    public void setPersonCache(SimpleCache<String, Set<NodeRef>> personCache)
    {
        this.personCache = personCache;
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
    public NodeRef getPerson(final String userName, final boolean autoCreate)
    {
        // MT share - for activity service system callback
        if (tenantService.isEnabled() && (AuthenticationUtil.SYSTEM_USER_NAME.equals(AuthenticationUtil.getRunAsUser())) && tenantService.isTenantUser(userName))
        {
            final String tenantDomain = tenantService.getUserDomain(userName);

            return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>()
            {
                public NodeRef doWork() throws Exception
                {
                    return getPersonImpl(userName, autoCreate);
                }
            }, tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
        }
        else
        {
            return getPersonImpl(userName, autoCreate);
        }
    }

    private NodeRef getPersonImpl(String userName, boolean autoCreate)
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
            if (autoCreate && createMissingPeople() && txnReadState == TxnReadState.TXN_READ_WRITE)
            {
                // We create missing people AND are in a read-write txn
                return createMissingPerson(userName);
            }
            else
            {
                throw new NoSuchPersonException(userName);
            }
        }
        else if (autoCreate)
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
        for (NodeRef nodeRef : allRefs)
        {
            Serializable value = nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME);
            String realUserName = DefaultTypeConverter.INSTANCE.convert(String.class, value);
            if (userNameMatcher.matches(searchUserName, realUserName))
            {
                refs.add(nodeRef);
            }
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

    private static final String KEY_POST_TXN_DUPLICATES = "PersonServiceImpl.KEY_POST_TXN_DUPLICATES";
    private static final String KEY_ALLOW_UID_UPDATE  = "PersonServiceImpl.KEY_ALLOW_UID_UPDATE";

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
        
        RetryingTransactionCallback<Object> processDuplicateWork = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                try
                {
                    policyBehaviourFilter.disableBehaviour(ContentModel.TYPE_PERSON);
                    
                    if (duplicateMode.equalsIgnoreCase(SPLIT))
                    {
                        // Allow UIDs to be updated in this transaction
                        AlfrescoTransactionSupport.bindResource(KEY_ALLOW_UID_UPDATE, Boolean.TRUE);
                        split(postTxnDuplicates);
                        logger.info("Split duplicate person objects");
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
                }
                finally
                {
                    policyBehaviourFilter.enableBehaviour(ContentModel.TYPE_PERSON);
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
            nodeService.deleteNode(nodeRef);
        }
    }

    private void split(Set<NodeRef> toSplit)
    {
        for (NodeRef nodeRef : toSplit)
        {
            String userName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME));
            nodeService.setProperty(nodeRef, ContentModel.PROP_USERNAME, userName + GUID.generate());
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
    public void setPersonProperties(String userName, Map<QName, Serializable> properties, boolean autoCreate)
    {
        NodeRef personNode = getPersonOrNull(userName);
        if (personNode == null)
        {
            if (createMissingPeople())
            {
                personNode = createMissingPerson(userName);
            }
            else
            {
                throw new PersonException("No person found for user name " + userName);
            }
        }
        else
        {
            if (autoCreate)
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
    
    private NodeRef createMissingPerson(String userName)
    {
        HashMap<QName, Serializable> properties = getDefaultProperties(userName);
        NodeRef person = createPerson(properties);
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
                        homeFolderManager.makeHomeFolder(ref);
                        return null;
                    }
                }, transactionService.isReadOnly(), transactionService.isReadOnly() ? false : AlfrescoTransactionSupport.getTransactionReadState() == TxnReadState.TXN_READ_ONLY);
                //homeFolder = DefaultTypeConverter.INSTANCE.convert(NodeRef.class, nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER));
                //assert(homeFolder != null);
            }
        }
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
        String userName = DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_USERNAME));
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
    }
    
    /**
     * {@inheritDoc}
     */
    public Set<NodeRef> getAllPeople()
    {
        List<ChildAssociationRef> childRefs = nodeService.getChildAssocs(getPeopleContainer(),
                ContentModel.ASSOC_CHILDREN, RegexQNamePattern.MATCH_ALL, false);
        Set<NodeRef> refs = new HashSet<NodeRef>(childRefs.size()*2);
        for (ChildAssociationRef childRef : childRefs)
        {
            refs.add(childRef.getChildRef());
        }
        return refs;
    }
    
    /**
     * {@inheritDoc}
     */
    public Set<NodeRef> getPeopleFilteredByProperty(QName propertyKey, Serializable propertyValue)
    {
        // check that given property key is defined for content model type 'cm:person'
        // and throw exception if it isn't
        if (this.dictionaryService.getProperty(ContentModel.TYPE_PERSON, propertyKey) == null)
        {
            throw new AlfrescoRuntimeException("Property '" + propertyKey + "' is not defined " + "for content model type cm:person");
        }

        LinkedHashSet<NodeRef> people = new LinkedHashSet<NodeRef>();

        //
        // Search for people using the given property
        //

        SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("@cm\\:" + propertyKey.getLocalName() + ":\"" + propertyValue + "\"");
        sp.addStore(tenantService.getName(storeRef));
        sp.excludeDataInTheCurrentTransaction(false);

        ResultSet rs = null;

        try
        {
            rs = searchService.query(sp);

            for (ResultSetRow row : rs)
            {
                NodeRef nodeRef = row.getNodeRef();
                if (nodeService.exists(nodeRef))
                {
                    people.add(nodeRef);
                }
            }
        }
        finally
        {
            if (rs != null)
            {
                rs.close();
            }
        }

        return people;
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
        
        // work around for policy bug ...
        homeFolderManager.onCreateNode(childAssocRef);
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

    // IOC Setters
    
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

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter)
    {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }

    public void setStoreUrl(String storeUrl)
    {
        this.storeRef = new StoreRef(storeUrl);
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

    /*
     * When a uid is changed we need to create an alias for the old uid so permissions are not broken. This can happen
     * when an already existing user is updated via LDAP e.g. migration to LDAP, or when a user is auto created and then
     * updated by LDAP This is probably less likely after 3.2 and sync on missing person See
     * https://issues.alfresco.com/jira/browse/ETWOTWO-389 (non-Javadoc)
     * 
     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef,
     *      java.util.Map, java.util.Map)
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
}
