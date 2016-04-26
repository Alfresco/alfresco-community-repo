package org.alfresco.repo.web.scripts.quickshare;

import java.io.IOException;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.springframework.extensions.webscripts.GUID;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;

/**
 * This class tests QuickShare REST API with disabled QuickShare feature
 * 
 * @author sergey.shcherbovich
 * @since 4.2
 */
public class QuickShareTurnedOffRestApiTest extends BaseWebScriptTest 
{
    private static final String APPLICATION_JSON = "application/json";
    private final static String SHARE_URL = "/api/internal/shared/share/{node_ref}";
    
    @Override
    protected void setUp() throws Exception 
    {
        setCustomContext("classpath:alfresco/quick-share-turned-off-test-context.xml");
        
        super.setUp();
        
        getServer().getApplicationContext();
    }
    
    public void testQuickShareDisabled() throws IOException
    {
        String testNodeRef = "workspace://SpacesStore/" + GUID.generate();
        sendRequest(new PostRequest(SHARE_URL.replace("{node_ref}", testNodeRef), "", APPLICATION_JSON), 403, AuthenticationUtil.getAdminUserName());
    }
}
