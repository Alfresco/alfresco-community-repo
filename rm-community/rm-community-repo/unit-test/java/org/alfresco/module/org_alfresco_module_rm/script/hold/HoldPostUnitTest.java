package org.alfresco.module.org_alfresco_module_rm.script.hold;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.json.JSONObject;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.extensions.webscripts.DeclarativeWebScript;

/**
 * Hold ReST API POST implementation unit test.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class HoldPostUnitTest extends BaseHoldWebScriptWithContentUnitTest
{
    /** classpath location of ftl template for web script */
    private static final String WEBSCRIPT_TEMPLATE = WEBSCRIPT_ROOT_RM + "hold.post.json.ftl";

    /** HoldPost webscript instance */
    protected @Spy @InjectMocks HoldPost webScript;

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
        return WEBSCRIPT_TEMPLATE;
    }

    /**
     * Test that a record can be added to holds.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void addRecordToHolds() throws Exception
    {
        // build json to send to server
        String content = buildContent(records, holds);

        // execute web script
        JSONObject json = executeJSONWebScript(Collections.EMPTY_MAP, content);
        assertNotNull(json);

        // verify that the record was added to the holds
        verify(mockedHoldService, times(1)).addToHolds(holds, records);
    }

    /**
     * Test that a record folder can be added to holds.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void addRecordFolderToHolds() throws Exception
    {
        // build json to send to server
        String content = buildContent(recordFolders, holds);

        // execute web script
        JSONObject json = executeJSONWebScript(Collections.EMPTY_MAP, content);
        assertNotNull(json);

        // verify that the record was added to the holds
        verify(mockedHoldService, times(1)).addToHolds(holds, recordFolders);
    }
}
