package org.alfresco.rest.workflow.processes.variables;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.dataprep.CMISUtil.Priority;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestProcessModel;
import org.alfresco.rest.model.RestProcessVariableCollection;
import org.alfresco.rest.model.RestProcessVariableModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.ProcessModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AddProcessVariablesFullTests extends RestTest
{
    private FileModel document;
    private SiteModel siteModel;
    private UserModel userWhoStartsProcess, assignee, adminUser;
    private ProcessModel processModel;
    private RestProcessModel restProcessModel;
    private RestProcessVariableModel variableModel, processVariable, variableModel1, variableModel2, variableModel3;
    private RestProcessVariableCollection processVariableCollection;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userWhoStartsProcess = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userWhoStartsProcess).createPublicRandomSite();
        document = dataContent.usingUser(userWhoStartsProcess).usingSite(siteModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        processModel = dataWorkflow.usingUser(userWhoStartsProcess).usingSite(siteModel).usingResource(document).createSingleReviewerTaskAndAssignTo(assignee);
       }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Add process variables using by the user involved in the process.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void addProcessVariableByUserInvolvedTheProcess() throws Exception
    {        
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        restProcessModel = restClient.authenticateUser(assignee).withWorkflowAPI().getProcesses()
                .getProcessModelByProcessDefId(processModel.getId());
        
        processVariable = restClient.authenticateUser(assignee).withWorkflowAPI().usingProcess(restProcessModel)
                                    .addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        processVariable.assertThat().field("name").is(variableModel.getName())
                       .and().field("type").is(variableModel.getType())
                       .and().field("value").is(variableModel.getValue());

        restClient.withWorkflowAPI().usingProcess(restProcessModel).getProcessVariables()
                .assertThat().entriesListContains("name", variableModel.getName());
    }    

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Add process variables using by the user involved in the process.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void addMultipleProcessVariableByUserInvolvedTheProcess() throws Exception
    {        
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");  
        variableModel1 = RestProcessVariableModel.getRandomProcessVariableModel("d:text");       
        restProcessModel = restClient.authenticateUser(assignee).withWorkflowAPI().getProcesses()
                .getProcessModelByProcessDefId(processModel.getId());

        processVariableCollection = restClient.authenticateUser(assignee).withWorkflowAPI().usingProcess(restProcessModel)           
                                    .addProcessVariables(variableModel, variableModel1);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        processVariableCollection.getEntries().get(0).onModel()
                                 .assertThat().field("name").is(variableModel.getName())
                                 .and().field("type").is(variableModel.getType())
                                 .and().field("value").is(variableModel.getValue());
        
        processVariableCollection.getEntries().get(1).onModel()
                                 .assertThat().field("name").is(variableModel1.getName())
                                 .and().field("type").is(variableModel1.getType())
                                 .and().field("value").is(variableModel1.getValue());

        restClient.withWorkflowAPI().usingProcess(restProcessModel).getProcessVariables()
                .assertThat().entriesListContains("name",  variableModel.getName())
                .assertThat().entriesListContains("name",  variableModel1.getName());
    }    

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Add process variables using by inexistent user.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void addProcessVariableByInexistentUser() throws Exception
    {        
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");      
        restProcessModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().getProcesses()
                .getProcessModelByProcessDefId(processModel.getId());

        processVariable = restClient.authenticateUser(UserModel.getRandomUserModel()).withWorkflowAPI()
                                    .usingProcess(restProcessModel)
                                    .addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Add multiple process variables using by inexistent user.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void addMultipleProcessVariableByInexistentUser() throws Exception
    {        
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        variableModel1 = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        restProcessModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().getProcesses()
                .getProcessModelByProcessDefId(processModel.getId());
        
        processVariableCollection = restClient.authenticateUser(UserModel.getRandomUserModel()).withWorkflowAPI()
                                    .usingProcess(restProcessModel)
                                    .addProcessVariables(variableModel,variableModel1);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }   

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Adding process variables is falling in case invalid type is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void failedAddingProcessVariableIfInvalidTypeIsProvided() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:textarea");
        restProcessModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses()
                .getProcessModelByProcessDefId(processModel.getId());

        restClient.withWorkflowAPI().usingProcess(restProcessModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError().containsSummary(
                      String.format(RestErrorModel.UNSUPPORTED_TYPE, "d:textarea"))
                  .containsErrorKey(String.format(RestErrorModel.UNSUPPORTED_TYPE, "d:textarea"))
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }
    
    @Bug(id = "REPO-1938")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Adding process variables is falling in case invalid type prefix is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void failedAddingProcessVariableIfInvalidTypePrefixIsProvided() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("ddt:text");
        restProcessModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses()
                .getProcessModelByProcessDefId(processModel.getId());

        restClient.withWorkflowAPI().usingProcess(restProcessModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError()
                  .containsSummary(String.format(RestErrorModel.INVALID_NAMEPACE_PREFIX, "ddt"))
                  .containsErrorKey(RestErrorModel.API_DEFAULT_ERRORKEY)
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Adding process variables is falling in case invalid value is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void failedAddingProcessVariableIfInvalidValueIsProvided() throws Exception
    {
        RestProcessVariableModel variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:int");
        variableModel.setValue("invalidValue");
        restProcessModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses()
                .getProcessModelByProcessDefId(processModel.getId());

        restClient.withWorkflowAPI().usingProcess(restProcessModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError()
                  .containsErrorKey(RestErrorModel.API_DEFAULT_ERRORKEY)
                  .containsSummary(String.format(RestErrorModel.FOR_INPUT_STRING, "invalidValue"))
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Adding process variables is falling in case invalid variableBody (adding extra parameter in body:scope) is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void failedAddingProcessVariableIfInvalidBodyIsProvided() throws Exception
    {
        restProcessModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses()
                .getProcessModelByProcessDefId(processModel.getId());

        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, "{\"name\": \"variableName\",\"scope\": \"local\",\"value\": \"testing\",\"type\": \"d:text\"}",
                "processes/{processId}/variables", processModel.getId());
        restClient.processModel(RestProcessVariableModel.class, request);        
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError()
                  .containsSummary(String.format(RestErrorModel.NO_CONTENT, "Unrecognized field \"scope\""))
                  .containsErrorKey(String.format(RestErrorModel.NO_CONTENT, "Unrecognized field \"scope\""))
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    //@Bug(id="REPO-1985")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Adding process variables is falling in case empty name is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void failedAddingProcessVariableIfEmptyNameIsProvided() throws Exception
    {
        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, Priority.Normal);
        restProcessModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses()
                .getProcessModelByProcessDefId(processModel.getId());
        RestProcessVariableModel variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        variableModel.setName("");

        restClient.withWorkflowAPI().usingProcess(restProcessModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError()
                  .containsSummary(RestErrorModel.VARIABLE_NAME_REQUIRED)
                  .containsErrorKey(RestErrorModel.VARIABLE_NAME_REQUIRED)
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    //@Bug(id="REPO-1985")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Adding process variables is falling in case invalid name is provided: ony white spaces")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void failedAddingProcessVariableUsingOnlyWhiteSpaceInName() throws Exception
    {
        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, Priority.Normal);
        restProcessModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses()
                .getProcessModelByProcessDefId(processModel.getId());
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        variableModel.setName(" ");

        processVariable = restClient.withWorkflowAPI().usingProcess(restProcessModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        processVariable.assertThat().field("name").isNull()
                       .and().field("type").is(variableModel.getType())
                       .and().field("value").is(variableModel.getValue());

        restClient.withWorkflowAPI().usingProcess(restProcessModel).getProcessVariables()
                .assertThat().entriesListContains("value", variableModel.getValue());
    }

    @Bug(id="REPO-1987")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Adding process variables is falling in case invalid name is provided: symbols")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void failedAddingProcessVariableWithSymbolsInName() throws Exception
    {
        restProcessModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses()
                .getProcessModelByProcessDefId(processModel.getId());
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        variableModel.setName("123_%^&: õÈ,Ì,Ò");     

        processVariable = restClient.withWorkflowAPI().usingProcess(restProcessModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        processVariable.assertThat().field("name").is(variableModel.getName())
                       .and().field("type").is(variableModel.getType())
                       .and().field("value").is(variableModel.getValue());

        restClient.withWorkflowAPI().usingProcess(restProcessModel).getProcessVariables()
                .assertThat().entriesListContains("name", variableModel.getName()); 
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Add two process variables using by the user involved in the process.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void addingMultipleValidProcessVariables() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        variableModel2 = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        variableModel3 = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        restProcessModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses()
                .getProcessModelByProcessDefId(processModel.getId());

        processVariableCollection = restClient.authenticateUser(assignee).withWorkflowAPI().usingProcess(restProcessModel)
                                    .addProcessVariables(variableModel, variableModel2, variableModel3);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        processVariableCollection.assertThat().entriesListContains("name", variableModel.getName())
                                 .assertThat().entriesListContains("name", variableModel2.getName())
                                 .assertThat().entriesListContains("name", variableModel3.getName());

        processVariableCollection.getEntries().get(0).onModel().assertThat()
                                 .field("name").is(variableModel.getName()).and()
                                 .field("value").is(variableModel.getValue()).and()
                                 .field("type").is(variableModel.getType());
        processVariableCollection.getEntries().get(1).onModel().assertThat()
                                 .field("name").is(variableModel2.getName()).and()
                                 .field("value").is(variableModel2.getValue()).and()
                                 .field("type").is(variableModel2.getType());
        processVariableCollection.getEntries().get(2).onModel().assertThat()
                                 .field("name").is(variableModel3.getName()).and()
                                 .field("value").is(variableModel3.getValue()).and()
                                 .field("type").is(variableModel3.getType());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Add two process variables using by the user involved in the process.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void failedAddingMultipleProcessVariablesInvalidType() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        variableModel.setType("d:string");
        variableModel2 = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        restProcessModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses()
                .getProcessModelByProcessDefId(processModel.getId());

        processVariableCollection = restClient.authenticateUser(assignee).withWorkflowAPI().usingProcess(restProcessModel)
                                    .addProcessVariables(variableModel, variableModel2);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError().containsSummary(String.format(RestErrorModel.UNSUPPORTED_TYPE, "d:string"))
                  .containsErrorKey(String.format(RestErrorModel.UNSUPPORTED_TYPE, "d:string"))
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Bug(id = "REPO-1938")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Add two process variables using by the user involved in the process.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void failledAddingMultipleProcessVariablesInvalidTypePrefix() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        variableModel.setType("e:text");
        variableModel2 = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        restProcessModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses()
                .getProcessModelByProcessDefId(processModel.getId());

        processVariableCollection = restClient.authenticateUser(assignee).withWorkflowAPI().usingProcess(restProcessModel)
                                    .addProcessVariables(variableModel, variableModel2);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError()
                  .containsSummary(String.format(RestErrorModel.INVALID_NAMEPACE_PREFIX, "e"))
                  .containsErrorKey(RestErrorModel.API_DEFAULT_ERRORKEY)
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Adding process variables is falling in case invalid value is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void failledAddingMultipleProcessVariablesInvalidValue() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:int");
        variableModel.setValue("invalidValue");
        variableModel1 = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        variableModel2 = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        restProcessModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses()
                .getProcessModelByProcessDefId(processModel.getId());

        processVariableCollection = restClient.authenticateUser(assignee).withWorkflowAPI().usingProcess(restProcessModel)
                                              .addProcessVariables(variableModel, variableModel1, variableModel2); 
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError()
                  .containsErrorKey(RestErrorModel.API_DEFAULT_ERRORKEY)
                  .containsSummary(String.format(RestErrorModel.FOR_INPUT_STRING, "invalidValue"))
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Adding process variables is falling in case invalid value is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void failledAddingMultipleInvalidProcessVariables() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:int");
        variableModel.setValue("invalidValue");
        variableModel1 = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        variableModel1.setType("");
        restProcessModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses()
                .getProcessModelByProcessDefId(processModel.getId());

        processVariableCollection = restClient.authenticateUser(assignee).withWorkflowAPI().usingProcess(restProcessModel)
                                              .addProcessVariables(variableModel1, variableModel); 
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError()
                  .containsErrorKey(RestErrorModel.API_DEFAULT_ERRORKEY)
                  .containsSummary(String.format(RestErrorModel.FOR_INPUT_STRING, "invalidValue"))
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }        

}
