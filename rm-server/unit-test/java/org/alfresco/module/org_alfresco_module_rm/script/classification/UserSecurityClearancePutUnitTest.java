package org.alfresco.module.org_alfresco_module_rm.script.classification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevel;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException;
import org.alfresco.module.org_alfresco_module_rm.classification.ClearanceLevel;
import org.alfresco.module.org_alfresco_module_rm.classification.SecurityClearance;
import org.alfresco.module.org_alfresco_module_rm.classification.SecurityClearanceService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseWebScriptUnitTest;
import org.alfresco.service.cmr.security.PersonService;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.WebScriptException;

public class UserSecurityClearancePutUnitTest extends BaseWebScriptUnitTest
{
    /**
     * Classpath location of ftl template for web script
     */
    private static final String WEBSCRIPT_TEMPLATE = WEBSCRIPT_ROOT_RM + "classification/usersecurityclearance.put.json.ftl";
    private static final String USERNAME = "username";
    private static final String CLEARANCE_ID = "clearanceId";

    /**
     * User security clearance webscript instance
     */
    private @Spy @InjectMocks
    UserSecurityClearancePut webscript;

    /**
     * Mock Security Clearance Service
     */
    private @Mock
    SecurityClearanceService mockSecurityClearanceService;

    /**
     * {@inheritDoc}
     */
    @Override
    protected DeclarativeWebScript getWebScript()
    {
        return webscript;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getWebScriptTemplate()
    {
        return WEBSCRIPT_TEMPLATE;
    }

    /**
     * Test the Security Clearance webscript
     *
     * @throws Exception
     */
    @Test
    public void testExecuteImpl() throws Exception
    {
        String username = "user1";
        String clearanceId = "Top Secret";
        String clearanceDisplay = "Don't tell anyone";
        String firstName = "Firstname";
        String lastName = "Lastname";
        PersonService.PersonInfo personInfo = new PersonService.PersonInfo(generateNodeRef(), username, firstName, lastName);
        ClassificationLevel classificationLevel = new ClassificationLevel(clearanceId, clearanceDisplay);
        ClearanceLevel clearanceLevel = new ClearanceLevel(classificationLevel, clearanceDisplay);

        SecurityClearance securityClearance = new SecurityClearance(personInfo, clearanceLevel);

        // Setup web script parameters
        Map<String, String> parameters = buildParameters(USERNAME, username, CLEARANCE_ID, clearanceId);

        when(mockSecurityClearanceService.setUserSecurityClearance(username, clearanceId)).thenReturn(securityClearance);

        // Execute web script
        JSONObject json = executeJSONWebScript(parameters);
        assertNotNull(json);

        // check the JSON result using Jackson to allow easy equality testing.
        ObjectMapper mapper = new ObjectMapper();
        String expectedJSONString = "{\"data\":{\"firstName\":\"Firstname\",\"lastName\":\"Lastname\",\"completeName\":\"Firstname Lastname (user1)\",\"fullName\":\"Firstname Lastname\",\"clearanceLabel\":\"Don't tell anyone\",\"userName\":\"user1\",\"classificationId\":\"Top Secret\"}}";
        JsonNode expected = mapper.readTree(expectedJSONString);
        assertEquals(expected, mapper.readTree(json.toString()));
    }

    /**
     * Test the Security Clearance webscript can't be called by a user with insufficient clearance
     *
     * @throws Exception
     */
    @Test (expected = WebScriptException.class)
    public void testNonClearedUser() throws Exception
    {
        String username = "user1";
        String clearanceId = "Top Secret";

        // Setup web script parameters
        Map<String, String> parameters = buildParameters(USERNAME, username, CLEARANCE_ID, clearanceId);

        when(mockSecurityClearanceService.setUserSecurityClearance(username, clearanceId))
            .thenThrow(new ClassificationServiceException.LevelIdNotFound(clearanceId));

        // Execute web script - this should throw the expected exception.
        executeJSONWebScript(parameters);
    }
}