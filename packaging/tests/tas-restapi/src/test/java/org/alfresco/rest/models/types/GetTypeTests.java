package org.alfresco.rest.models.types;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestTypeModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetTypeTests extends RestTest
{

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        restClient.authenticateUser(dataUser.createRandomTestUser());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.MODEL, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.MODEL }, executionType = ExecutionType.REGRESSION,
            description = "Verify inexistent type and status code is Not Found (404)")
    public void getInexistentType() throws Exception
    {
        String unknownType = "unknown:type";
        restClient.withModelAPI().getType(unknownType);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_WAS_NOT_FOUND, unknownType));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.MODEL, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.MODEL }, executionType = ExecutionType.REGRESSION,
            description = "Verify Type Info and status code is OK (200)")
    public void getType() throws Exception
    {
        RestTypeModel type = restClient.withModelAPI().getType("cm:content");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        type.assertThat().field("associations").isEmpty().and()
                .field("mandatoryAspects").isNotEmpty().and()
                .field("properties").isNotEmpty().and()
                .field("includedInSupertypeQuery").is(true).and()
                .field("isArchive").is(true).and()
                .field("isContainer").is(false).and()
                .field("id").is("cm:content").and()
                .field("description").is("Base Content Object").and()
                .field("title").is("Content").and()
                .field("model.id").is("cm:contentmodel").and()
                .field("model.author").is("Alfresco").and()
                .field("model.description").is("Alfresco Content Domain Model").and()
                .field("model.namespaceUri").is("http://www.alfresco.org/model/content/1.0").and()
                .field("model.namespacePrefix").is("cm");
    }
}