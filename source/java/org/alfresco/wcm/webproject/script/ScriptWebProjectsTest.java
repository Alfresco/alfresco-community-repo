package org.alfresco.wcm.webproject.script;

import java.util.HashMap;

import org.alfresco.repo.jscript.ClasspathScriptLocation;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.wcm.AbstractWCMServiceImplTest;

public class ScriptWebProjectsTest extends AbstractWCMServiceImplTest 
{
    private ScriptService scriptService;
    
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.scriptService = (ScriptService)ctx.getBean("ScriptService");
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
    }
	
    public void testJSAPI() throws Exception
    {
        ScriptLocation location = new ClasspathScriptLocation("org/alfresco/wcm/webproject/script/test_WebProjectService.js");
        this.scriptService.executeScript(location, new HashMap<String, Object>(0));
    }

}
