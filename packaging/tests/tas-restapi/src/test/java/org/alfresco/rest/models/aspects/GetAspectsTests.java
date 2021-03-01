package org.alfresco.rest.models.aspects;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestAbstractClassModel;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetAspectsTests extends RestTest
{

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        restClient.authenticateUser(dataUser.createRandomTestUser());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify inexistent aspect and status code is Not Found (404)")
    public void getInexistentAspect() throws Exception
    {
        String unknownAspect = "unknown:aspect";
        restClient.withModelAPI().getAspect(unknownAspect);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_WAS_NOT_FOUND, unknownAspect));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Aspect Info and status code is OK (200)")
    public void getAspect() throws Exception
    {
        RestAbstractClassModel aspect = restClient.withModelAPI().getAspect("cm:titled");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        aspect.assertThat().field("associations").isEmpty().and()
                .field("mandatoryAspects").isEmpty().and()
                .field("properties").isNotEmpty().and()
                .field("includedInSupertypeQuery").is(true).and()
                .field("isContainer").is(false).and()
                .field("id").is("cm:titled").and()
                .field("description").is("Titled").and()
                .field("title").is("Titled").and()
                .field("model.id").is("cm:contentmodel").and()
                .field("model.author").is("Alfresco").and()
                .field("model.description").is("Alfresco Content Domain Model").and()
                .field("model.namespaceUri").is("http://www.alfresco.org/model/content/1.0").and()
                .field("model.namespacePrefix").is("cm");
    }
}