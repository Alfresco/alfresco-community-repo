package org.alfresco.repo.web.scripts.node;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.Request;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Unit Tests for the Java-backed Node WebScripts
 * 
 * @since 4.1
 */
public class NodeWebScripTest extends BaseWebScriptTest
{
    private static Log logger = LogFactory.getLog(NodeWebScripTest.class);

    private String TEST_SITE_NAME = "TestNodeSite";
    private SiteInfo TEST_SITE;
    
    private MutableAuthenticationService authenticationService;
    private RetryingTransactionHelper retryingTransactionHelper;
    private PersonService personService;
    private SiteService siteService;
    private NodeService nodeService;
    
    private static final String USER_ONE = "UserOneSecondToo";
    private static final String USER_TWO = "UserTwoSecondToo";
    private static final String USER_THREE = "UserThreeStill";
    private static final String PASSWORD = "passwordTEST";
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        AbstractRefreshableApplicationContext ctx = (AbstractRefreshableApplicationContext)getServer().getApplicationContext();
        this.retryingTransactionHelper = (RetryingTransactionHelper)ctx.getBean("retryingTransactionHelper");
        this.authenticationService = (MutableAuthenticationService)ctx.getBean("AuthenticationService");
        this.personService = (PersonService)ctx.getBean("PersonService");
        this.siteService = (SiteService)ctx.getBean("SiteService");
        this.nodeService = (NodeService)ctx.getBean("NodeService");
        
        // Do the setup as admin
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        // Create a site
        TEST_SITE = createSite(TEST_SITE_NAME);
        
        // Create two users, one who's a site member
        createUser(USER_ONE, true);
        createUser(USER_TWO, false);
        
        // Do our tests by default as the first user who is a contributor
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        // Admin user required to delete users and sites
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        
        // Zap the site, and their contents
        siteService.deleteSite(TEST_SITE.getShortName());
        
        // Delete users
        for (String user : new String[] {USER_ONE, USER_TWO, USER_THREE})
        {
            // Delete the user, as admin
            AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
            if(personService.personExists(user))
            {
               personService.deletePerson(user);
            }
            if(this.authenticationService.authenticationExists(user))
            {
               this.authenticationService.deleteAuthentication(user);
            }
        }
    }
    
    private SiteInfo createSite(final String shortName)
    {
        return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<SiteInfo>()
           {
              @Override
              public SiteInfo execute() throws Throwable
              {
                  if (siteService.getSite(shortName) != null)
                  {
                      // Tidy up after failed earlier run
                      siteService.deleteSite(shortName);
                  }
                  
                  // Do the create
                  SiteInfo site = siteService.createSite("Testing", shortName, shortName, null, SiteVisibility.PUBLIC);
                  
                  // Ensure we have a doclib
                  siteService.createContainer(shortName, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);
                  
                  // All done
                  return site;
              }
           }, false, true
        ); 
    }
    
    private void createUser(final String userName, boolean contributor)
    {
        // Make sure a new user is created every time
        // This ensures a predictable password etc
        if(this.personService.personExists(userName))
        {
           this.personService.deletePerson(userName);
        }
        if(this.authenticationService.authenticationExists(userName))
        {
           this.authenticationService.deleteAuthentication(userName);
        }
        
        
        // Create a fresh user
        authenticationService.createAuthentication(userName, PASSWORD.toCharArray());

        // create person properties
        PropertyMap personProps = new PropertyMap();
        personProps.put(ContentModel.PROP_USERNAME, userName);
        personProps.put(ContentModel.PROP_FIRSTNAME, "First");
        personProps.put(ContentModel.PROP_LASTNAME, "Last");
        personProps.put(ContentModel.PROP_EMAIL, "FirstName123.LastName123@email.com");
        personProps.put(ContentModel.PROP_JOBTITLE, "JobTitle123");
        personProps.put(ContentModel.PROP_JOBTITLE, "Organisation123");

        // create person node for user
        personService.createPerson(personProps);

        // Set site permissions as needed
        if (contributor)
        {
            this.siteService.setMembership(TEST_SITE_NAME, userName, SiteModel.SITE_CONTRIBUTOR);
        }
        else
        {
            this.siteService.setMembership(TEST_SITE_NAME, userName, SiteModel.SITE_CONSUMER);
        }
    }
    
    private JSONObject asJSON(Response response) throws Exception
    {
        String json = response.getContentAsString();
        JSONParser p = new JSONParser();
        Object o = p.parse(json);
        
        if (o instanceof JSONObject)
        {
            return (JSONObject)o; 
        }
        throw new IllegalArgumentException("Expected JSONObject, got " + o + " from " + json);
    }
    
    
    @SuppressWarnings("unchecked")
    public void testFolderCreation() throws Exception
    {
        // Create a folder within the DocLib
        NodeRef siteDocLib = siteService.getContainer(TEST_SITE.getShortName(), SiteService.DOCUMENT_LIBRARY);
        
        String testFolderName = "testing";
        Map<QName,Serializable> testFolderProps = new HashMap<QName, Serializable>();
        testFolderProps.put(ContentModel.PROP_NAME, testFolderName);
        NodeRef testFolder = nodeService.createNode(siteDocLib, ContentModel.ASSOC_CONTAINS, 
                QName.createQName("testing"), ContentModel.TYPE_FOLDER, testFolderProps).getChildRef();
        
        String testNodeName = "aNEWfolder";
        String testNodeTitle = "aTITLEforAfolder";
        String testNodeDescription = "DESCRIPTIONofAfolder";
        JSONObject jsonReq = null;
        JSONObject json = null;
        NodeRef folder = null;
        
        
        // By NodeID
        Request req = new Request("POST", "/api/node/folder/"+testFolder.getStoreRef().getProtocol()+"/"+
                                   testFolder.getStoreRef().getIdentifier()+"/"+testFolder.getId());
        jsonReq = new JSONObject();
        jsonReq.put("name", testNodeName);
        req.setBody(jsonReq.toString().getBytes());
        req.setType(MimetypeMap.MIMETYPE_JSON);
        
        json = asJSON( sendRequest(req, Status.STATUS_OK) );
        assertNotNull(json.get("nodeRef"));

        folder = new NodeRef((String)json.get("nodeRef"));
        assertEquals(true, nodeService.exists(folder));
        assertEquals(testNodeName, nodeService.getProperty(folder, ContentModel.PROP_NAME));
        assertEquals(testNodeName, nodeService.getProperty(folder, ContentModel.PROP_TITLE));
        assertEquals(null, nodeService.getProperty(folder, ContentModel.PROP_DESCRIPTION));
        
        assertEquals(testFolder, nodeService.getPrimaryParent(folder).getParentRef());
        assertEquals(ContentModel.TYPE_FOLDER, nodeService.getType(folder));
        
        nodeService.deleteNode(folder);

        
        // In a Site Container
        req = new Request("POST", "/api/site/folder/"+TEST_SITE_NAME+"/"+SiteService.DOCUMENT_LIBRARY);
        jsonReq = new JSONObject();
        jsonReq.put("name", testNodeName);
        jsonReq.put("description", testNodeDescription);
        req.setBody(jsonReq.toString().getBytes());

        json = asJSON( sendRequest(req, Status.STATUS_OK) );
        assertNotNull(json.get("nodeRef"));

        folder = new NodeRef((String)json.get("nodeRef"));
        assertEquals(true, nodeService.exists(folder));
        assertEquals(testNodeName, nodeService.getProperty(folder, ContentModel.PROP_NAME));
        assertEquals(testNodeName, nodeService.getProperty(folder, ContentModel.PROP_TITLE));
        assertEquals(testNodeDescription, nodeService.getProperty(folder, ContentModel.PROP_DESCRIPTION));
        
        assertEquals(siteDocLib, nodeService.getPrimaryParent(folder).getParentRef());
        assertEquals(ContentModel.TYPE_FOLDER, nodeService.getType(folder));
        
        nodeService.deleteNode(folder);

        
        // A Child of a Site Container
        req = new Request("POST", "/api/site/folder/"+TEST_SITE_NAME+"/"+SiteService.DOCUMENT_LIBRARY+"/"+testFolderName);
        jsonReq = new JSONObject();
        jsonReq.put("name", testNodeName);
        jsonReq.put("title", testNodeTitle);
        jsonReq.put("description", testNodeDescription);
        req.setBody(jsonReq.toString().getBytes());

        json = asJSON( sendRequest(req, Status.STATUS_OK) );
        assertNotNull(json.get("nodeRef"));

        folder = new NodeRef((String)json.get("nodeRef"));
        assertEquals(true, nodeService.exists(folder));
        assertEquals(testNodeName, nodeService.getProperty(folder, ContentModel.PROP_NAME));
        assertEquals(testNodeTitle, nodeService.getProperty(folder, ContentModel.PROP_TITLE));
        assertEquals(testNodeDescription, nodeService.getProperty(folder, ContentModel.PROP_DESCRIPTION));
        
        assertEquals(testFolder, nodeService.getPrimaryParent(folder).getParentRef());
        assertEquals(ContentModel.TYPE_FOLDER, nodeService.getType(folder));

        nodeService.deleteNode(folder);

        
        // Type needs to be a subtype of folder
        
        // explicit cm:folder
        req = new Request("POST", "/api/site/folder/"+TEST_SITE_NAME+"/"+SiteService.DOCUMENT_LIBRARY+"/"+testFolderName);
        jsonReq = new JSONObject();
        jsonReq.put("name", testNodeName);
        jsonReq.put("type", "cm:folder");
        req.setBody(jsonReq.toString().getBytes());

        json = asJSON( sendRequest(req, Status.STATUS_OK) );
        assertNotNull(json.get("nodeRef"));

        folder = new NodeRef((String)json.get("nodeRef"));
        assertEquals(true, nodeService.exists(folder));
        assertEquals(testNodeName, nodeService.getProperty(folder, ContentModel.PROP_NAME));
        assertEquals(ContentModel.TYPE_FOLDER, nodeService.getType(folder));

        nodeService.deleteNode(folder);

        
        // cm:systemfolder extends from cm:folder
        req = new Request("POST", "/api/site/folder/"+TEST_SITE_NAME+"/"+SiteService.DOCUMENT_LIBRARY+"/"+testFolderName);
        jsonReq = new JSONObject();
        jsonReq.put("name", testNodeName);
        jsonReq.put("type", "cm:systemfolder");
        req.setBody(jsonReq.toString().getBytes());

        json = asJSON( sendRequest(req, Status.STATUS_OK) );
        assertNotNull(json.get("nodeRef"));

        folder = new NodeRef((String)json.get("nodeRef"));
        assertEquals(true, nodeService.exists(folder));
        assertEquals(testNodeName, nodeService.getProperty(folder, ContentModel.PROP_NAME));
        assertEquals(ContentModel.TYPE_SYSTEM_FOLDER, nodeService.getType(folder));

        nodeService.deleteNode(folder);

        
        // cm:content isn't allowed
        req = new Request("POST", "/api/site/folder/"+TEST_SITE_NAME+"/"+SiteService.DOCUMENT_LIBRARY+"/"+testFolderName);
        jsonReq = new JSONObject();
        jsonReq.put("name", testNodeName);
        jsonReq.put("type", "cm:content");
        req.setBody(jsonReq.toString().getBytes());

        sendRequest(req, Status.STATUS_BAD_REQUEST);
        
        
        // Check permissions - need to be Contributor
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        req = new Request("POST", "/api/node/folder/"+testFolder.getStoreRef().getProtocol()+"/"+
                                  testFolder.getStoreRef().getIdentifier()+"/"+testFolder.getId());
        jsonReq = new JSONObject();
        jsonReq.put("name", testNodeName);
        req.setBody(jsonReq.toString().getBytes());
        req.setType(MimetypeMap.MIMETYPE_JSON);

        json = asJSON( sendRequest(req, Status.STATUS_OK) );
        assertNotNull(json.get("nodeRef"));

        folder = new NodeRef((String)json.get("nodeRef"));
        assertEquals(true, nodeService.exists(folder));
        nodeService.deleteNode(folder);

        
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        sendRequest(req, Status.STATUS_FORBIDDEN);
    }
}