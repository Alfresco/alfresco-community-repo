package org.alfresco.repo.workflow.jbpm;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.scripts.ScriptException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.Token;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class AlfrescoJavaScriptIntegrationTest extends BaseAlfrescoSpringTest
{
	private static final QName fooName = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "Foo");
	private static final QName barName = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "Bar");
	private static final QName docName = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "Doc");
	private static final String BASIC_USER = "basic"+GUID.generate();
	private static String systemUser = AuthenticationUtil.getSystemUserName();
	
	private ServiceRegistry services;
	private ExecutionContext context;
	private HashMap<String, Object> variables;
	private PersonService personService;
    private Repository repository;

	/**
	 * Test that JavaScript can still be run even if no Authentication is provided.
	 * This can occur if, e.g. the action is executed as part of an asynchronous task.
	 * @throws Exception
	 */
	public void testRunsWithoutAuthentication() throws Exception {
		NodeRef systemNode = personService.getPerson(systemUser);
		NodeRef baseUserNode = personService.getPerson(BASIC_USER);
		TestUserStore userStore = new TestUserStore();
		variables.put("userStore", userStore);
		Element script = buildScript("userStore.storeUsers(person)");
		
		// Check authentication cleared.
		AuthenticationUtil.clearCurrentSecurityContext();
		assertNull(AuthenticationUtil.getFullyAuthenticatedUser());
		assertNull(AuthenticationUtil.getRunAsUser());
		
		// Check uses system user when no authentication set and no task assignee.
		AlfrescoJavaScript scriptHandler = new AlfrescoJavaScript();
		scriptHandler.setScript(script);
		scriptHandler.execute(context);
		assertEquals(systemUser, userStore.runAsUser);
		assertEquals(systemUser, userStore.fullUser);
		assertEquals(systemNode, userStore.person.getNodeRef());

		// Check authentication is correctly reset.
		assertNull(AuthenticationUtil.getFullyAuthenticatedUser());
		assertNull(AuthenticationUtil.getRunAsUser());

		// Check that when a task assignee exists, then he/she is used for authentication.
		TaskInstance taskInstance = mock(TaskInstance.class);
		when(taskInstance.getActorId()).thenReturn(BASIC_USER);
		when(context.getTaskInstance()).thenReturn(taskInstance);
		scriptHandler = new AlfrescoJavaScript();
		scriptHandler.setScript(script);
		scriptHandler.execute(context);
		assertEquals(BASIC_USER, userStore.runAsUser);
		assertEquals(BASIC_USER, userStore.fullUser);
		assertEquals(baseUserNode, userStore.person.getNodeRef());
		
		// Check authentication is correctly reset.
		assertNull(AuthenticationUtil.getFullyAuthenticatedUser());
		assertNull(AuthenticationUtil.getRunAsUser());
	}
	
	/**
	 * See Jira issue ALF-657.
	 * @throws Exception
	 */
	public void testRunAsAdminMoveContent() throws Exception {
		NodeRef fooFolder = nodeService.createNode(rootNodeRef,
				ContentModel.ASSOC_CONTAINS,
				fooName,
				ContentModel.TYPE_FOLDER).getChildRef();
		
		NodeRef barFolder = nodeService.createNode(rootNodeRef,
				ContentModel.ASSOC_CONTAINS,
				barName,
				ContentModel.TYPE_FOLDER).getChildRef();
		
		NodeRef doc = nodeService.createNode(fooFolder,
				ContentModel.ASSOC_CONTAINS,
				docName,
				ContentModel.TYPE_CONTENT).getChildRef();
		PermissionService permissions = services.getPermissionService();
		permissions.setPermission(doc, BASIC_USER, PermissionService.ALL_PERMISSIONS, true);
		AuthenticationUtil.setFullyAuthenticatedUser(BASIC_USER);

		Element script = buildScript("doc.move(bar)");
		
		variables.put("doc", new JBPMNode(doc, services));
		variables.put("bar", new JBPMNode(barFolder, services));
		assertEquals(fooFolder, nodeService.getPrimaryParent(doc).getParentRef());
		try
		{
			AlfrescoJavaScript scriptHandler = new AlfrescoJavaScript();
			scriptHandler.setScript(script);
			scriptHandler.execute(context);
			fail("The user should not have permission to write to bar!");
		} catch(ScriptException e)
		{
			// Do nothing.
		}
		
		assertEquals(fooFolder, nodeService.getPrimaryParent(doc).getParentRef());
		
		AlfrescoJavaScript scriptHandler = new AlfrescoJavaScript();
		scriptHandler.setScript(script);
		scriptHandler.setRunas(AuthenticationUtil.getAdminUserName());
		scriptHandler.execute(context);
		assertEquals(barFolder, nodeService.getPrimaryParent(doc).getParentRef());
	}
	
	/**
	 * See Jira issue ALF-5346.
	 * @throws Exception
	 */
	public void testRunAsAdminMoveContentBetweenSites() throws Exception {
	    SiteService siteService = services.getSiteService();
	    FileFolderService fileFolderService = services.getFileFolderService();
	    
	    String siteAName = "siteA"+GUID.generate();
	    String siteBName = "siteB"+GUID.generate();
	    
	    AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        siteService.createSite(
                    "testSitePreset", siteAName, "title", "description", SiteVisibility.PRIVATE);
        
        siteService.createSite(
                    "testSitePreset", siteBName, "title", "description", SiteVisibility.PRIVATE);
        
        NodeRef docLibA = siteService.createContainer(siteAName, "documentLibrary", ContentModel.TYPE_FOLDER, null);
        NodeRef docLibB = siteService.createContainer(siteBName, "documentLibrary", ContentModel.TYPE_FOLDER, null);

        FileInfo docInfo = fileFolderService.create(docLibA, "test.txt", ContentModel.TYPE_CONTENT);
        NodeRef doc = docInfo.getNodeRef();
        ContentWriter writer = fileFolderService.getWriter(doc);
        writer.putContent("Just some old content that doesn't mean anything");

	    AuthenticationUtil.setFullyAuthenticatedUser(BASIC_USER);
	    
	    Element script = buildScript("doc.move(companyhome.childByNamePath(\"Sites/"+ siteBName +"/documentLibrary\"))");
	    
	    NodeRef companyHome = repository.getCompanyHome();
	    variables.put("companyhome", new JBPMNode(companyHome, services));
	    variables.put("doc", new JBPMNode(doc, services));
	    assertEquals(docLibA, nodeService.getPrimaryParent(doc).getParentRef());
	    try
	    {
	        AlfrescoJavaScript scriptHandler = new AlfrescoJavaScript();
	        scriptHandler.setScript(script);
	        scriptHandler.execute(context);
	        fail("The user should not have permission to write to Site B!");
	    } catch(ScriptException e)
	    {
	        // Do nothing.
	    }
	    
	    assertEquals(docLibA, nodeService.getPrimaryParent(doc).getParentRef());
	    
	    AlfrescoJavaScript scriptHandler = new AlfrescoJavaScript();
	    scriptHandler.setScript(script);
	    scriptHandler.setRunas(AuthenticationUtil.getAdminUserName());
	    scriptHandler.execute(context);
	    assertEquals(docLibB, nodeService.getPrimaryParent(doc).getParentRef());
	}

	public void testScopeVariables() throws Exception
    {
	    String admin = AuthenticationUtil.getAdminUserName();
	    AuthenticationUtil.setFullyAuthenticatedUser(admin);
        NodeRef person = personService.getPerson(admin);
        Serializable userHome = nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER);
        
        AlfrescoJavaScript scriptHandler = new AlfrescoJavaScript();
        String key = "result";
        
        // Check person node set.
        Element script = buildScript("executionContext.setVariable('" + key + "', person)");
        scriptHandler.setScript(script);
        scriptHandler.execute(context);
        ScriptNode value = (ScriptNode) variables.get(key);
        assertEquals(person, value.getNodeRef());
        
        // Check user home set.
        script = buildScript("executionContext.setVariable('" + key + "', userhome)");
        scriptHandler.setScript(script);
        scriptHandler.execute(context);
        value = (ScriptNode) variables.get(key);
        assertEquals(userHome, value.getNodeRef());
        
        // Check company home set.
        NodeRef companyHome = repository.getCompanyHome();
        script = buildScript("executionContext.setVariable('" + key + "', companyhome)");
        scriptHandler.setScript(script);
        scriptHandler.execute(context);
        value = (ScriptNode) variables.get(key);
        assertEquals(companyHome, value.getNodeRef());
    }
	
	private Element buildScript(String expression) {
		Element script = DocumentHelper.createElement("script");
		script.setText(expression);
		return script;
	}

	@Override
	@SuppressWarnings("deprecation")
	protected void onSetUp() throws Exception {
		super.onSetUp();
		
		this.services = (ServiceRegistry) applicationContext.getBean("ServiceRegistry");
		repository = (Repository) applicationContext.getBean("repositoryHelper");
		personService = services.getPersonService();
		createUser(BASIC_USER);
		
		// Sets up the Execution Context
		context = mock(ExecutionContext.class);
		ContextInstance contextInstance = mock(ContextInstance.class);
		when(context.getContextInstance()).thenReturn(contextInstance);
		variables = new HashMap<String, Object>();
		when(contextInstance.getVariables()).thenReturn(variables);
		when(contextInstance.getVariables( any(Token.class))).thenReturn(variables);
		when(context.getVariable(anyString())).thenAnswer(new Answer<Object>()
        {
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                String key = (String)invocation.getArguments()[0];
                return variables.get(key);
            }
        });
		doAnswer(new Answer<Void>()
        {
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                String key = (String)invocation.getArguments()[0];
                Object value= invocation.getArguments()[1];
                variables.put(key, value);
                return null;
            }
        }).when(context).setVariable(anyString(), any());
	}
	
    private void createUser(String userName)
    {
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            this.authenticationService.createAuthentication(userName, "PWD".toCharArray());
            
            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");
            personService.createPerson(ppOne);
        }
    }
    
    public static class TestUserStore {
    	private String runAsUser;
    	private String fullUser;
    	private ScriptNode person = null;
    	
		public void storeUsers(ScriptNode user)
    	{
    		fullUser = AuthenticationUtil.getFullyAuthenticatedUser();
    		runAsUser = AuthenticationUtil.getRunAsUser();
    		this.person = user;
    	}
    }
}
