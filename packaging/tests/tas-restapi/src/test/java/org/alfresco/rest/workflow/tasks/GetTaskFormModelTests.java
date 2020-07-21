package org.alfresco.rest.workflow.tasks;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.*;
import org.alfresco.utility.model.*;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 2/3/2017.
 */
public class GetTaskFormModelTests extends RestTest
{
    UserModel userModel, adminUser;
    SiteModel siteModel;
    FileModel fileModel;
    TaskModel taskModel;
    RestFormModelsCollection returnedCollection;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userModel = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        fileModel = dataContent.usingUser(userModel).usingSite(siteModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS },
            executionType = ExecutionType.SANITY, description = "Verify admin user gets all task form models with Rest API and response is successful (200)")
    public void adminGetsTaskFormModels() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userModel).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(userModel);
        returnedCollection = restClient.authenticateUser(dataUser.getAdminUser()).withWorkflowAPI().usingTask(taskModel).getTaskFormModel();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat().entriesListIsNotEmpty();

        String[] qualifiedNames = {
                "{http://www.alfresco.org/model/bpm/1.0}percentComplete",
                "{http://www.alfresco.org/model/bpm/1.0}context",
                "{http://www.alfresco.org/model/bpm/1.0}completedItems",
                "{http://www.alfresco.org/model/content/1.0}name",
                "{http://www.alfresco.org/model/bpm/1.0}packageActionGroup",
                "{http://www.alfresco.org/model/bpm/1.0}reassignable",
                "{http://www.alfresco.org/model/content/1.0}owner",
                "{http://www.alfresco.org/model/bpm/1.0}outcome",
                "{http://www.alfresco.org/model/bpm/1.0}taskId",
                "{http://www.alfresco.org/model/bpm/1.0}packageItemActionGroup",
                "{http://www.alfresco.org/model/bpm/1.0}completionDate"};

        for(String formQualifiedName :  qualifiedNames)
        {
            returnedCollection.assertThat().entriesListContains("qualifiedName", formQualifiedName);
        }
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS },
            executionType = ExecutionType.SANITY, description = "Verify user involved in task gets all the task form models with Rest API and response is successful (200)")
    public void involvedUserGetsTaskFormModels() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userModel).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(userModel);
        returnedCollection = restClient.authenticateUser(userModel).withWorkflowAPI().usingTask(taskModel).getTaskFormModel();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat().entriesListIsNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS },
            executionType = ExecutionType.REGRESSION,
            description = "Verify that non involved user in task cannot get form models with Rest API and response is FORBIDDEN (403)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void nonInvolvedUserCannotGetTaskFormModels() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userModel)
                .usingSite(siteModel)
                .usingResource(fileModel).createNewTaskAndAssignTo(userModel);
        UserModel nonInvolvedUser = dataUser.createRandomTestUser();
        restClient.authenticateUser(nonInvolvedUser);
        restClient.withWorkflowAPI().usingTask(taskModel).getTaskFormModel();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
                .assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS },
            executionType = ExecutionType.REGRESSION,
            description = "Verify user involved in task cannot get task form models with invalid task id")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void getTaskFormModelsInvalidTaskId() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userModel)
                .usingSite(siteModel)
                .usingResource(fileModel).createNewTaskAndAssignTo(userModel);
        taskModel.setId("0000");
        restClient.authenticateUser(userModel);
        restClient.withWorkflowAPI().usingTask(taskModel).getTaskFormModel();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "0000"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS },
            executionType = ExecutionType.REGRESSION,
            description = "Verify user involved in task cannot get task form models with invalid task id")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void getTaskFormModelsEmptyTaskId() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userModel)
                .usingSite(siteModel)
                .usingResource(fileModel).createNewTaskAndAssignTo(userModel);
        taskModel.setId("");
        restClient.authenticateUser(userModel);
        restClient.withWorkflowAPI().usingTask(taskModel).getTaskFormModel();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, ""));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS },
            executionType = ExecutionType.REGRESSION,
            description = "Verify user involved in task can get completed task form models")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void getTaskFormModelsForCompletedTask() throws Exception
    {
        UserModel assignedUser = dataUser.createRandomTestUser();
        taskModel = dataWorkflow.usingUser(userModel)
                .usingSite(siteModel)
                .usingResource(fileModel).createNewTaskAndAssignTo(assignedUser);
        dataWorkflow.usingUser(assignedUser).taskDone(taskModel);
        dataWorkflow.usingUser(userModel).taskDone(taskModel);
        restClient.authenticateUser(userModel);
        returnedCollection = restClient.withWorkflowAPI().usingTask(taskModel).getTaskFormModel();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat().entriesListIsNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS },
            executionType = ExecutionType.REGRESSION, description = "Verify admin user gets all task form models with properties parameter applied and response is successful (200)")
    public void adminGetsTaskFormModelsWithPropertiesParameter() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userModel).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(userModel);
        returnedCollection = restClient.authenticateUser(adminUser).withParams("properties=qualifiedName,required").withWorkflowAPI().usingTask(taskModel).getTaskFormModel();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat().entriesListIsNotEmpty();
        returnedCollection.getOneRandomEntry().onModel()
                .assertThat()
                    .field("qualifiedName").isNotEmpty().and()
                    .field("required").isNotEmpty().and()
                    .field("dataType").isNull().and()
                    .field("name").isNull().and()
                    .field("title").isNull().and()
                    .field("defaultValue").isNull().and()
                    .field("allowedValues").isNull().and()
                    .fieldsCount().is(2);
        }

    @Bug(id = "MNT-17438")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin gets task form model with valid skipCount parameter applied using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void getTaskFormModelWithValidSkipCount() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userModel).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(userModel);
        returnedCollection = restClient.authenticateUser(adminUser)
                .withWorkflowAPI().usingTask(taskModel).getTaskFormModel();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        RestFormModel firstTaskFormModel = returnedCollection.getEntries().get(0).onModel();
        RestFormModel secondTaskFormModel = returnedCollection.getEntries().get(1).onModel();

        RestFormModelsCollection formModelsWithSkipCount = restClient.withParams("skipCount=2").withWorkflowAPI().usingTask(taskModel).getTaskFormModel();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        formModelsWithSkipCount
                .assertThat().entriesListDoesNotContain("name", firstTaskFormModel.getName())
                .assertThat().entriesListDoesNotContain("name", secondTaskFormModel.getName())
                .assertThat().entriesListCountIs(returnedCollection.getEntries().size()-2);
        formModelsWithSkipCount.assertThat().paginationField("skipCount").is("2");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin doesn't get task form model with negative skipCount parameter applied using REST API")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void getTaskFormModelWithNegativeSkipCount() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userModel).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(userModel);
        restClient.authenticateUser(adminUser).withParams("skipCount=-1").withWorkflowAPI().usingTask(taskModel).getTaskFormModel();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(RestErrorModel.NEGATIVE_VALUES_SKIPCOUNT)
                .containsErrorKey(RestErrorModel.NEGATIVE_VALUES_SKIPCOUNT)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE)
                .statusCodeIs(HttpStatus.BAD_REQUEST);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin doesn't get task form model with non numeric skipCount parameter applied using REST API")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void getTaskFormModelWithNonNumericSkipCount() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userModel).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(userModel);
        restClient.authenticateUser(adminUser).withParams("skipCount=A").withWorkflowAPI().usingTask(taskModel).getTaskFormModel();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.INVALID_SKIPCOUNT, "A"));
    }

    @Bug(id = "MNT-17438")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin gets task form model with valid maxItems parameter applied using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void getTaskFormModelWithValidMaxItems() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userModel).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(userModel);
        returnedCollection = restClient.authenticateUser(adminUser)
                .withWorkflowAPI().usingTask(taskModel).getTaskFormModel();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        RestFormModel firstTaskFormModel = returnedCollection.getEntries().get(0).onModel();
        RestFormModel secondTaskFormModel = returnedCollection.getEntries().get(1).onModel();

        RestFormModelsCollection formModelsWithMaxItems = restClient.withParams("maxItems=2").withWorkflowAPI().usingTask(taskModel).getTaskFormModel();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        formModelsWithMaxItems
                .assertThat().entriesListContains("name", firstTaskFormModel.getName())
                .assertThat().entriesListContains("name", secondTaskFormModel.getName())
                .assertThat().entriesListCountIs(2);
        formModelsWithMaxItems.assertThat().paginationField("maxItems").is("2");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin doesn't get task form model with negative maxItems parameter applied using REST API")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void getTaskFormModelWithNegativeMaxItems() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userModel).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(userModel);
        restClient.authenticateUser(adminUser).withParams("maxItems=-1").withWorkflowAPI().usingTask(taskModel).getTaskFormModel();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                .containsErrorKey(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE)
                .statusCodeIs(HttpStatus.BAD_REQUEST);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin doesn't get task form model with non numeric maxItems parameter applied using REST API")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void getTaskFormModelWithNonNumericMaxItems() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userModel).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(userModel);
        restClient.authenticateUser(adminUser).withParams("maxItems=A").withWorkflowAPI().usingTask(taskModel).getTaskFormModel();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.INVALID_MAXITEMS, "A"));
    }
}
