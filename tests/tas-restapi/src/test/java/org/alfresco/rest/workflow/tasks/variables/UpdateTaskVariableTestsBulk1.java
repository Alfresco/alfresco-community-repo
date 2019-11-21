package org.alfresco.rest.workflow.tasks.variables;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestVariableModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TaskModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author iulia.cojocea
 */
public class UpdateTaskVariableTestsBulk1 extends RestTest
{
    private UserModel userModel;
    private UserModel adminUser;
    private SiteModel siteModel;
    private FileModel fileModel;
    private UserModel assigneeUser;
    private TaskModel taskModel;

    private RestVariableModel taskVariable;
    private RestVariableModel variableModel;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userModel = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        fileModel = dataContent.usingUser(userModel).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        assigneeUser = dataUser.createRandomTestUser();
        taskModel = dataWorkflow.usingUser(userModel).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assigneeUser);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY,
            description = "Create non-existing task variable")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    public void createTaskVariable() throws Exception
    {
        restClient.authenticateUser(adminUser);
        variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        
        taskVariable = restClient.withWorkflowAPI().usingTask(taskModel).updateTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        taskVariable.assertThat().field("scope").is(taskVariable.getScope())
                    .and().field("name").is(taskVariable.getName())
                    .and().field("type").is(taskVariable.getType())
                    .and().field("value").is(taskVariable.getValue());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY,
            description = "Update existing task variable")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    public void updateExistingTaskVariable() throws Exception
    {
        restClient.authenticateUser(adminUser);
        variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        
        taskVariable = restClient.withWorkflowAPI().usingTask(taskModel).updateTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        taskVariable.assertThat().field("scope").is(taskVariable.getScope())
                    .and().field("name").is(taskVariable.getName())
                    .and().field("type").is(taskVariable.getType())
                    .and().field("value").is(taskVariable.getValue());
        
        variableModel.setValue("updatedValue");
        taskVariable = restClient.withWorkflowAPI().usingTask(taskModel).updateTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        taskVariable.assertThat().field("value").is("updatedValue");
    }
}