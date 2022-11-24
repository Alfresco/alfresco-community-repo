package org.alfresco.rest.actions.email;

import static java.util.Objects.requireNonNull;

import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.json.JsonObject;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.UserModel;

import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class EmailTemplateTest extends RestTest {

    public static final String MAIL_ACTION = "mail";

    private UserModel adminUser;
    private UserModel testUser;
    private FolderModel testFolder;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception {
        adminUser = dataUser.getAdminUser();

        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser)
                           .createPublicRandomSite();
        testFolder = dataContent.usingUser(testUser)
                                .usingSite(testSite)
                                .createFolder();
    }

    @Test
    public void adminCanSendEmailUsingTemplateWithModelAsString() throws Exception
    {
        String templateId = uploadEmailTemplate("simpleEmailTemplate.ftl");

        // Create the model for use with email template
        JsonObject args = JsonBodyGenerator.defineJSON()
                                           .add("args", JsonBodyGenerator.defineJSON()
                                                                         .add("name", "testname")
                                                                         .build())
                                           .build();
        String emailModel = args.toString();

        // Send an email using the template
        restClient.authenticateUser(adminUser)
                  .withCoreAPI()
                  .usingActions()
                  .executeAction(MAIL_ACTION, testFolder, createMailWithTemplateParameters(adminUser, testUser, templateId, emailModel));

        restClient.onResponse()
                  .assertThat().statusCode(HttpStatus.ACCEPTED.value())
                  .assertThat().body("entry.id", notNullValue());
    }

    private String uploadEmailTemplate(String templateName) throws IOException
    {
        final String templateContent = getTemplateContent(templateName);
        final FileModel templateToCreate = new FileModel(templateName, FileType.TEXT_PLAIN, templateContent);

        final FileModel createdTemplate = dataContent.usingAdmin()
                                                         .usingResource(testFolder)
                                                         .createContent(templateToCreate);

        return createdTemplate.getNodeRef();
    }

    private String getTemplateContent(String templateName) throws IOException
    {
        final String templateClasspathLocation = "/shared-resources/testdata/" + templateName;
        try (InputStream templateStream = getClass().getResourceAsStream(templateClasspathLocation))
        {
            requireNonNull(templateStream, "Couldn't locate `" + templateClasspathLocation + "`");
            return new String(templateStream.readAllBytes());
        }
    }

    private static Map<String, Serializable> createMailWithTemplateParameters(UserModel sender, UserModel recipient, String templateId, Serializable model)
    {
        Map<String, Serializable> parameterValues = new HashMap<>();

        parameterValues.put("from", sender.getEmailAddress());
        parameterValues.put("to", recipient.getEmailAddress());
        parameterValues.put("subject", "Test");
        parameterValues.put("template", "workspace://SpacesStore/" + templateId);
        parameterValues.put("template_model", model);

        return parameterValues;
    }
}
