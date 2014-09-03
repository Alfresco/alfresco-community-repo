package org.alfresco.repo.security.permissions.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.permissions.AccessControlListDAO;
import org.alfresco.repo.domain.permissions.AclDAO;
import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.repo.search.impl.lucene.ADMLuceneIndexer;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.repo.security.permissions.PermissionServiceSPI;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

public class AbstractReadPermissionTest extends TestCase
{
    protected static ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();

    protected static final String ROLE_AUTHENTICATED = "ROLE_AUTHENTICATED";

    protected NodeService nodeService;

    protected DictionaryService dictionaryService;

    protected PermissionServiceSPI permissionService;

    protected MutableAuthenticationService authenticationService;
    
    protected MutableAuthenticationDao authenticationDAO;

    protected LocalSessionFactoryBean sessionFactory;

    protected NodeRef rootNodeRef;

    protected NamespacePrefixResolver namespacePrefixResolver;

    protected ServiceRegistry serviceRegistry;

    protected NodeRef systemNodeRef;

    protected AuthenticationComponent authenticationComponent;

    protected ModelDAO permissionModelDAO;
    
    protected PersonService personService;
    
    protected AuthorityService authorityService;
    
    protected AuthorityDAO authorityDAO;

    protected NodeDAO nodeDAO;
    
    protected AclDAO aclDaoComponent;

    protected ADMLuceneIndexer admLuceneIndexer;
    
    protected RetryingTransactionHelper retryingTransactionHelper;

    protected TransactionService transactionService;

    protected AccessControlListDAO accessControlListDao;
        
    protected FileFolderService fileFolderService;
    
    protected OwnableService ownableService;
    
    protected UserTransaction testTX;
    
	protected IndexerAndSearcher fIndexerAndSearcher;

	protected boolean logToFile = false;

	protected String[] webAuthorities = new String[] {"Web1", "Web2", "Web3", "Web4", "Web5"};

	protected String[] authorities = new String[] {"Dynamic","1000","1001","Y","Z","X","10_1","100","10","1","01","001","0001"};

	final int WEB_COUNT = 100;

//	protected final String TEST_RUN = ""+System.currentTimeMillis();

	protected class Counter
	{
		int i = 0;
		void increment()
		{
			i++;
		}
		int count()
		{
			return i;
		}
	}

	protected int COUNT = 10;
	protected Counter c01 = new Counter();
	protected Counter c001 = new Counter();
	protected Counter c0001 = new Counter();

    private Map<QName, Serializable> createPersonProperties(String userName)
    {
        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_USERNAME, userName);
        return properties;
    }
    
    protected void createAuthentication(String name)
    {
    	if(authenticationDAO.userExists(name))
        {
            authenticationService.deleteAuthentication(name);
        }
        authenticationService.createAuthentication(name, name.toCharArray());
        if(personService.personExists(name))
        {
            personService.deletePerson(name);
        }
        personService.createPerson(createPersonProperties(name));
    }

    protected void createGroup(String name)
    {
    	authorityService.createAuthority(AuthorityType.GROUP, name);
    }

    protected void runAs(String userName)
    {
        authenticationService.authenticate(userName, userName.toCharArray());
        assertNotNull(authenticationService.getCurrentUserName());
        // for(GrantedAuthority authority : woof.getAuthorities())
        // {
        // System.out.println("Auth = "+authority.getAuthority());
        // }

    }
    
	protected NodeRef[] build1000Nodes(final String authority, final int returnNodes, final boolean inherit)
	{
		return build1000Nodes(authority, PermissionService.READ, returnNodes, inherit);
	}
	
	protected NodeRef[] buildOwnedNodes(final String authority, final int returnNodes)
	{
		runAs("admin");
		
		final NodeRef[] nodes = new NodeRef[returnNodes];

		RetryingTransactionCallback<Void> cb = new RetryingTransactionCallback<Void>()
		{
			public Void execute() throws Throwable
			{
				int i = 0;
				int k = returnNodes > 0 ? 1000/returnNodes : 0;
				String namePrefix = "simple" + System.currentTimeMillis();

				NodeRef folder = fileFolderService.create(rootNodeRef, namePrefix, ContentModel.TYPE_FOLDER).getNodeRef();

				NodeRef folder_1000 = fileFolderService.create(folder, namePrefix + "-1000-", ContentModel.TYPE_FOLDER).getNodeRef();
				permissionService.setInheritParentPermissions(folder_1000, false);
				permissionService.setPermission(folder_1000, authority, PermissionService.READ, true);
				for(int j = 0; j < 1000; j++)
				{
					NodeRef file = fileFolderService.create(folder_1000, namePrefix + "-1000-"+j, ContentModel.TYPE_CONTENT).getNodeRef();
					ownableService.setOwner(file, authority);

					if(returnNodes > 0)
					{
						if(j % k == 0)
						{
							nodes[i++] = file;
						}
					}
				}

				return null;
			}
		};
		retryingTransactionHelper.doInTransaction(cb, false, false);

		return nodes;
	}
	
	protected void buildNodes(final String user, final String permission, final int n, final boolean inherit)
	{
	    RetryingTransactionCallback<Void> cb = new RetryingTransactionCallback<Void>()
	    {
	        public Void execute() throws Throwable
	        {
	            String namePrefix = "simple" + System.currentTimeMillis();

	            NodeRef folder = fileFolderService.create(rootNodeRef, namePrefix, ContentModel.TYPE_FOLDER).getNodeRef();

	            NodeRef folder_n = fileFolderService.create(folder, namePrefix + "-n", ContentModel.TYPE_FOLDER).getNodeRef();
	            permissionService.setInheritParentPermissions(folder_n, false);
	            permissionService.setPermission(folder_n, user, PermissionService.READ, true);
	            for(int j = 0; j < n; j++)
	            {
	                NodeRef file = fileFolderService.create(folder_n, namePrefix + "-n-"+j, ContentModel.TYPE_CONTENT).getNodeRef();
	                if(!inherit)
	                {
	                    permissionService.setInheritParentPermissions(file, false);
	                    if(permission != null)
	                    {
	                        permissionService.setPermission(file, user, permission, true);
	                    }
	                }
	            }

	            return null;
	        }
	    };
	    retryingTransactionHelper.doInTransaction(cb, false, false);
	}
	
	protected NodeRef[] build1000Nodes(final String authority, final String permission, final int returnNodes, final boolean inherit)
	{
		runAs("admin");
		
		final NodeRef[] nodes = new NodeRef[returnNodes];

		RetryingTransactionCallback<Void> cb = new RetryingTransactionCallback<Void>()
		{
			public Void execute() throws Throwable
			{
				int i = 0;
				int k = returnNodes > 0 ? 1000/returnNodes : 0;
				String namePrefix = "simple" + System.currentTimeMillis();

				NodeRef folder = fileFolderService.create(rootNodeRef, namePrefix, ContentModel.TYPE_FOLDER).getNodeRef();

				NodeRef folder_1000 = fileFolderService.create(folder, namePrefix + "-1000-", ContentModel.TYPE_FOLDER).getNodeRef();
				permissionService.setInheritParentPermissions(folder_1000, false);
				permissionService.setPermission(folder_1000, authority, permission, true);
				for(int j = 0; j < 1000; j++)
				{
					NodeRef file = fileFolderService.create(folder_1000, namePrefix + "-1000-"+j, ContentModel.TYPE_CONTENT).getNodeRef();
					if(!inherit)
					{
						permissionService.setInheritParentPermissions(file, false);
						permissionService.setPermission(file, authority, permission, true);
					}
					if(returnNodes > 0)
					{
						if(j % k == 0)
						{
							nodes[i++] = file;
						}
					}
				}

				return null;
			}
		};
		retryingTransactionHelper.doInTransaction(cb, false, false);

		return nodes;
	}

	protected NodeRef[] build1000Nodes(final String authority, final String permission, final boolean inherit)
	{
		return build1000Nodes(authority, permission, 0, inherit);
	}
	
	protected void build1000NodesReadDenied(final String authority)
	{
		runAs("admin");

		RetryingTransactionCallback<Void> cb = new RetryingTransactionCallback<Void>()
		{
			public Void execute() throws Throwable
			{
				String name = "simple" + System.currentTimeMillis();

				NodeRef folder = fileFolderService.create(rootNodeRef, name, ContentModel.TYPE_FOLDER).getNodeRef();

				NodeRef folder_1001 = fileFolderService.create(folder, name + "-1001", ContentModel.TYPE_FOLDER).getNodeRef();
				permissionService.setPermission(folder_1001, authority, PermissionService.READ, true);
				permissionService.setInheritParentPermissions(folder_1001, false);
				for(int j = 0; j < 1000; j++)
				{
					NodeRef file = fileFolderService.create(folder_1001, name + "-1001-"+j, ContentModel.TYPE_CONTENT).getNodeRef();
					permissionService.setInheritParentPermissions(file, false);
					permissionService.setPermission(file, authority, PermissionService.READ, false);
				}

				return null;
			}
		};
		retryingTransactionHelper.doInTransaction(cb, false, false);
	}

	protected void buildNodes()
	{
		final Random random = new Random(42);

		runAs("admin");

		permissionService.setPermission(rootNodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.READ, true);

		for(int ii = 0; ii < COUNT; ii++)
		{
			final String namePrefix = "name" + System.currentTimeMillis() + "-";
			final int i = ii;
			System.out.println("Loop " + i);
			RetryingTransactionCallback<Void> cb = new RetryingTransactionCallback<Void>()
			{

				public Void execute() throws Throwable
				{
					NodeRef folder = fileFolderService.create(rootNodeRef, namePrefix + i, ContentModel.TYPE_FOLDER).getNodeRef();

					NodeRef folder_1000 = fileFolderService.create(folder, namePrefix + "1000-"+i, ContentModel.TYPE_FOLDER).getNodeRef();
					permissionService.setPermission(folder_1000, "1000", PermissionService.READ, true);
					permissionService.setInheritParentPermissions(folder_1000, false);
					for(int j = 0; j < 1000; j++)
					{
						NodeRef file = fileFolderService.create(folder_1000, namePrefix + "1000-"+i+"-"+j, ContentModel.TYPE_CONTENT).getNodeRef();
						//permissionService.setInheritParentPermissions(file, false);
						//permissionService.setPermission(file, "1000", PermissionService.READ, true);
					}

					NodeRef folder_100 = fileFolderService.create(folder, namePrefix + "100-"+i, ContentModel.TYPE_FOLDER).getNodeRef();
					permissionService.setPermission(folder_100, "100", PermissionService.READ, true);
					permissionService.setInheritParentPermissions(folder_100, false);
					for(int j = 0; j < 100; j++)
					{
						NodeRef file = fileFolderService.create(folder_100, namePrefix + "100-"+i+"-"+j, ContentModel.TYPE_CONTENT).getNodeRef();
					}

					NodeRef folder_10 = fileFolderService.create(folder, namePrefix + "10-"+i, ContentModel.TYPE_FOLDER).getNodeRef();
					permissionService.setPermission(folder_10, "10", PermissionService.READ, true);
					permissionService.setInheritParentPermissions(folder_10, false);
					for(int j = 0; j < 10; j++)
					{
						NodeRef file = fileFolderService.create(folder_10, namePrefix + "10-"+i+"-"+j, ContentModel.TYPE_CONTENT).getNodeRef();
					}

					NodeRef folder_10_1 = fileFolderService.create(folder, namePrefix + "10_1-"+i, ContentModel.TYPE_FOLDER).getNodeRef();
					permissionService.setPermission(folder_10_1, "GROUP_X", PermissionService.READ, true);
					permissionService.setInheritParentPermissions(folder_10_1, false);
					for(int j = 0; j < 10; j++)
					{
						NodeRef file = fileFolderService.create(folder_10_1, "namePrefix + 10_1-"+i+"-"+j, ContentModel.TYPE_CONTENT).getNodeRef();
					}

					NodeRef folder_1 = fileFolderService.create(folder, namePrefix + "1"+i, ContentModel.TYPE_FOLDER).getNodeRef();
					permissionService.setPermission(folder_1, "1", PermissionService.READ, true);
					permissionService.setInheritParentPermissions(folder_1, false);
					NodeRef file = fileFolderService.create(folder_1, namePrefix + "1-1-1", ContentModel.TYPE_CONTENT).getNodeRef();

					double rn = random.nextDouble();

					if(rn < 0.1)
					{
						NodeRef rf = fileFolderService.create(folder, namePrefix + "0.1", ContentModel.TYPE_CONTENT).getNodeRef();
						//permissionService.setPermission(rf, "01", PermissionService.READ, true);
						//permissionService.setInheritParentPermissions(rf, false);
						c01.increment();
					}

					if(rn < 0.01)
					{
						NodeRef rf = fileFolderService.create(folder, namePrefix + "0.01", ContentModel.TYPE_CONTENT).getNodeRef();
						//permissionService.setPermission(rf, "001", PermissionService.READ, true);
						//permissionService.setInheritParentPermissions(rf, false);
						c001.increment();
					}

					if(rn < 0.001)
					{
						NodeRef rf = fileFolderService.create(folder, namePrefix + "0.001", ContentModel.TYPE_CONTENT).getNodeRef();
						//permissionService.setPermission(rf, "0001", PermissionService.READ, true);
						//permissionService.setInheritParentPermissions(rf, false);
						c0001.increment();
					}
					return null;
				}
			};
			retryingTransactionHelper.doInTransaction(cb, false, false);

		}
	}
	protected void deleteAuthentication(String name)
	{
		if(authenticationDAO.userExists(name))
		{
			authenticationService.deleteAuthentication(name);
		}
		if(personService.personExists(name))
		{
			personService.deletePerson(name);
		}
	}

	public void setUp() throws Exception
	{
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_NONE)
        {
            throw new AlfrescoRuntimeException(
                    "A previous tests did not clean up transaction: " +
                    AlfrescoTransactionSupport.getTransactionId());
        }
        
        nodeService = (NodeService) applicationContext.getBean("nodeService");
        dictionaryService = (DictionaryService) applicationContext.getBean(ServiceRegistry.DICTIONARY_SERVICE
                .getLocalName());
        permissionService = (PermissionServiceSPI) applicationContext.getBean("permissionService");
        namespacePrefixResolver = (NamespacePrefixResolver) applicationContext
                .getBean(ServiceRegistry.NAMESPACE_SERVICE.getLocalName());
        authenticationService = (MutableAuthenticationService) applicationContext.getBean("authenticationService");
        authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
        serviceRegistry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
        permissionModelDAO = (ModelDAO) applicationContext.getBean("permissionsModelDAO");
        personService = (PersonService) applicationContext.getBean("personService");
        authorityService = (AuthorityService) applicationContext.getBean("authorityService");
        authorityDAO = (AuthorityDAO) applicationContext.getBean("authorityDAO");
        accessControlListDao = (AccessControlListDAO) applicationContext.getBean("admNodeACLDAO");
        fileFolderService = (FileFolderService)applicationContext.getBean("fileFolderService");

        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());
        authenticationDAO = (MutableAuthenticationDao) applicationContext.getBean("authenticationDao");
        nodeDAO = (NodeDAO) applicationContext.getBean("nodeDAO");
        aclDaoComponent = (AclDAO) applicationContext.getBean("aclDAO");
        
        retryingTransactionHelper = (RetryingTransactionHelper) applicationContext.getBean("retryingTransactionHelper");
        
        transactionService = (TransactionService) applicationContext.getBean("transactionComponent");
        
        ownableService = (OwnableService) applicationContext.getBean("ownableService");
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();

        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.nanoTime());
        rootNodeRef = nodeService.getRootNode(storeRef);

//        QName children = ContentModel.ASSOC_CHILDREN;
//        QName system = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "system");
//        QName container = ContentModel.TYPE_CONTAINER;
//        QName types = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "people");

//        systemNodeRef = nodeService.createNode(rootNodeRef, children, system, container).getChildRef();
//        NodeRef typesNodeRef = nodeService.createNode(systemNodeRef, children, types, container).getChildRef();
//        Map<QName, Serializable> props = createPersonProperties("andy");
//        nodeService.createNode(typesNodeRef, children, ContentModel.TYPE_PERSON, container, props).getChildRef();
//        props = createPersonProperties("lemur");
//        nodeService.createNode(typesNodeRef, children, ContentModel.TYPE_PERSON, container, props).getChildRef();

        // create an authentication object e.g. the user
        if(authenticationDAO.userExists("andy"))
        {
            authenticationService.deleteAuthentication("andy");
        }
        authenticationService.createAuthentication("andy", "andy".toCharArray());

        if(authenticationDAO.userExists("lemur"))
        {
            authenticationService.deleteAuthentication("lemur");
        }
        authenticationService.createAuthentication("lemur", "lemur".toCharArray());
        
        if(authenticationDAO.userExists(AuthenticationUtil.getAdminUserName()))
        {
            authenticationService.deleteAuthentication(AuthenticationUtil.getAdminUserName());
        }
        authenticationService.createAuthentication(AuthenticationUtil.getAdminUserName(), "admin".toCharArray());
     
		fIndexerAndSearcher = (IndexerAndSearcher)applicationContext.getBean("indexerAndSearcherFactory");

		for(String authority : authorities)
		{
			createAuthentication(authority);
		}

		for(String authority : webAuthorities)
		{
			createAuthentication(authority);
		}

		// TODO define permission group to include Read in permissionDefinitions
		// assign user to new permission group - should be able to read any node?

		createGroup("X");
		authorityService.addAuthority(authorityService.getName(AuthorityType.GROUP, "X"), "10_1");

		authenticationComponent.clearCurrentSecurityContext();
	}

	protected void tearDown() throws Exception
	{
		try
		{
            testTX.rollback();
        }
		catch (Throwable e)
		{
		    e.printStackTrace();
		}
        AuthenticationUtil.clearCurrentSecurityContext();
        super.tearDown();
	}

}
