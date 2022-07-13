package org.alfresco.email.action.access;

import org.alfresco.email.EmailTest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;

public class FormProcAdminAccessRestrictionTest extends EmailTest {

    private static final String ACTION_FORM_PROCESSOR_ENDPOINT = "alfresco/service/api/action/%s/formprocessor";


    private UserModel adminUser;
    private UserModel testUser;

    @Autowired
    protected RestWrapper restClient;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception {
        adminUser = dataUser.getAdminUser();
        testUser = dataUser.createRandomTestUser();
    }

    //TODO implement tests

    //restClient.configureRequestSpec().addHeader("Content-Type", "application/json");
    //required unique command

    //body is prefixed "prep_" + parameter key, good to use iterating over mail parameters Entries (getEntries etc.)
}
