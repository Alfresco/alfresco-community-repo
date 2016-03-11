package org.alfresco.module.org_alfresco_module_rm.recorded.version.config;

import static org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionPolicy.ALL;
import static org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionPolicy.MAJOR_ONLY;
import static org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionPolicy.NONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.script.slingshot.RecordedVersionConfigGet;
import org.alfresco.module.org_alfresco_module_rm.script.slingshot.Version;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionPolicy;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.springframework.extensions.webscripts.DeclarativeWebScript;

/**
 * Recorded Version Config REST API GET implementation unit test.
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RecordedVersionConfigGetUnitTest extends BaseRecordedVersionConfigTest
{
    /** RecordedVersionConfigGet webscript instance */
    protected @InjectMocks RecordedVersionConfigGet webScript;

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.recorded.version.config.BaseRecordedVersionConfigTest#getWebScript()
     */
    @Override
    protected DeclarativeWebScript getWebScript()
    {
        return webScript;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.recorded.version.config.BaseRecordedVersionConfigTest#getWebScriptTemplate()
     */
    @Override
    protected String getWebScriptTemplate()
    {
        return RECORDED_VERSION_CONFIG_WEBSCRIPT_ROOT + "recorded-version-config.get.json.ftl";
    }

    @Test
    public void getRecordedVersionConfig() throws Exception
    {
        // Build parameters
        Map<String, String> parameters = buildParameters();

        // Test document should not have any recordable version policy set
        doReturn(null).when(mockedNodeService).getProperty(testdoc, PROP_RECORDABLE_VERSION_POLICY);

        // Setup versions
        List<Version> versions = Arrays.asList(
                new Version(NONE.toString(), true),
                new Version(MAJOR_ONLY.toString(), false),
                new Version(ALL.toString(), false));

        // Stub getVersions
        doReturn(versions).when(mockedRecordableVersionConfigService).getVersions(testdoc);

        // Execute web script
        JSONObject json = executeJSONWebScript(parameters);

        // Do checks
        assertNotNull(json);

        assertTrue(json.has("data"));
        JSONObject data = json.getJSONObject("data");
        assertNotNull(data);

        assertTrue(data.has("recordableVersions"));
        JSONArray recordableVersions = data.getJSONArray("recordableVersions");
        assertNotNull(recordableVersions);
        assertEquals(recordableVersions.length(), 3);

        List<RecordableVersionPolicy> policies = new ArrayList<RecordableVersionPolicy>();
        boolean isSelected = false;
        int selectedOnce = 0;
        for (int i = 0; i < recordableVersions.length(); i++)
        {
            JSONObject jsonObject = recordableVersions.getJSONObject(i);
            String policy = jsonObject.getString("policy");
            policies.add(RecordableVersionPolicy.valueOf(policy));
            boolean selected = Boolean.valueOf(jsonObject.getString("selected")).booleanValue();
            if (selected)
            {
                isSelected = true;
                selectedOnce++;
            }
        }
        assertEquals(policies, Arrays.asList(RecordableVersionPolicy.values()));
        assertTrue(isSelected);
        assertEquals(selectedOnce, 1);

        // Test document should still not have any recordable version policy set
        doReturn(null).when(mockedNodeService).getProperty(testdoc, PROP_RECORDABLE_VERSION_POLICY);
    }
}
