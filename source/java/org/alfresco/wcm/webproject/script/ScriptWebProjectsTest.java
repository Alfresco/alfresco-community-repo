package org.alfresco.wcm.webproject.script;

import java.util.HashMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ClasspathScriptLocation;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.PropertyMap;

public class ScriptWebProjectsTest extends BaseAlfrescoSpringTest {
	
    
    private static final String USER_ONE = "WebProjectTestOne";
    private static final String USER_TWO = "WebProjectTestTwo";
    private static final String USER_THREE = "WebProjectTestThree";
    
    private static final String URL_WEB_PROJECTS = "/api/wcm/webprojects";
    private AuthenticationService authenticationService;
    private PersonService personService;
    private ScriptService scriptService;
    private AuthenticationComponent authenticationComponent;
    
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
            
            this.personService.createPerson(ppOne);
        }        
    }
    
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        this.scriptService = (ScriptService)this.applicationContext.getBean("ScriptService");
        
        this.authenticationService = (AuthenticationService)this.applicationContext.getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)this.applicationContext.getBean("authenticationComponent");
        this.personService = (PersonService)this.applicationContext.getBean("PersonService");
        
        // Create users
        createUser(USER_ONE);
        createUser(USER_TWO);
        createUser(USER_THREE);
        
        // Do tests as user one
        this.authenticationComponent.setCurrentUser(USER_ONE);
    
    }
	
    public void testJSAPI() throws Exception
    {
        this.authenticationComponent.setCurrentUser("admin");
        
        ScriptLocation location = new ClasspathScriptLocation("org/alfresco/wcm/webproject/script/test_WebProjectService.js");
        this.scriptService.executeScript(location, new HashMap<String, Object>(0));
    }

}
