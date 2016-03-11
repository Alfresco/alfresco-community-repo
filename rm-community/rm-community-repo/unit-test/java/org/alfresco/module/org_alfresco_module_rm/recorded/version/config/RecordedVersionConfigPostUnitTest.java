package org.alfresco.module.org_alfresco_module_rm.recorded.version.config;

import static org.alfresco.module.org_alfresco_module_rm.script.slingshot.RecordedVersionConfigPost.RECORDED_VERSION;
import static org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionPolicy.ALL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;

import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.script.slingshot.RecordedVersionConfigPost;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionPolicy;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.springframework.extensions.webscripts.DeclarativeWebScript;

/**
 * Recorded Version Config REST API POST implementation unit test.
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RecordedVersionConfigPostUnitTest extends BaseRecordedVersionConfigTest
{
    /** RecordedVersionConfigPost webscript instance */
    protected @InjectMocks RecordedVersionConfigPost webScript;

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseWebScriptUnitTest#getWebScript()
     */
    @Override
    protected DeclarativeWebScript getWebScript()
    {
        return webScript;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseWebScriptUnitTest#getWebScriptTemplate()
     */
    @Override
    protected String getWebScriptTemplate()
    {
        return RECORDED_VERSION_CONFIG_WEBSCRIPT_ROOT + "recorded-version-config.post.json.ftl";
    }

    @Test
    public void setRecordedVersionConfig() throws Exception
    {
        // Build the content
        String content = buildContent(ALL);

        // Build parameters
        Map<String, String> parameters = buildParameters();

        // Test document should not have any recordable version policy set
        doReturn(null).when(mockedNodeService).getProperty(testdoc, PROP_RECORDABLE_VERSION_POLICY);

        // execute web script
        JSONObject json = executeJSONWebScript(parameters, content);

        // Do checks
        assertNotNull(json);
        assertEquals(json.length(), 0);

        // Test document must have recordable version policy "ALL" set
        doReturn(ALL).when(mockedNodeService).getProperty(testdoc, PROP_RECORDABLE_VERSION_POLICY);
    }

    /**
     * Helper method to build the content for the POST request
     * @param policy The recordable version policy
     *
     * @return Content for the build request
     */
    private String buildContent(RecordableVersionPolicy policy)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"");
        sb.append(RECORDED_VERSION);
        sb.append("\":\"");
        sb.append(policy.toString());
        sb.append("\"}");
        return sb.toString();
    }
}
