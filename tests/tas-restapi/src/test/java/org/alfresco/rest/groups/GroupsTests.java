package org.alfresco.rest.groups;

import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.alfresco.rest.RestTest;
import org.alfresco.utility.RetryOperation;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {TestGroup.REQUIRE_SOLR})
public class GroupsTests extends RestTest
{
    private UserModel adminUser, userModel;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {  
        adminUser = dataUser.getAdminUser();
        userModel = dataUser.createRandomTestUser();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.GROUPS, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.NODES }, executionType = ExecutionType.SANITY,
            description = "Verify creation, listing, updating and deletion of groups.")
    public void createListUpdateAndDeleteGroup() throws Exception
    {
        String groupName = "ZtestGroup" + UUID.randomUUID().toString();
        JsonObject groupBody = Json.createObjectBuilder().add("id", groupName).add("displayName", groupName).build();
        String groupBodyCreate = groupBody.toString();

        //GroupCreation:
        //-ve
        restClient.authenticateUser(userModel).withCoreAPI().usingGroups().createGroup(groupBodyCreate);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN);
        //+ve
        restClient.authenticateUser(adminUser).withCoreAPI().usingParams("include=zones").usingGroups().createGroup(groupBodyCreate)
                                              .assertThat().field("zones").contains("APP.DEFAULT")
                                              .and().field("isRoot").is(true)
                                              .and().field("displayName").is(groupName);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        //ListGroups:
        restClient.withCoreAPI().usingParams("orderBy=displayName DESC&maxItems=10").usingGroups().listGroups()
                  .assertThat().entriesListContains("id", "GROUP_"+groupName)
                  .and().entriesListDoesNotContain("zones")
                  .and().paginationField("maxItems").is("10");
        restClient.assertStatusCodeIs(HttpStatus.OK);

        groupBody = Json.createObjectBuilder().add("displayName", "Z"+groupName).build();
        String groupBodyUpdate = groupBody.toString();
        //UpdateGroup:
        restClient.withCoreAPI().usingGroups().updateGroupDetails("GROUP_"+groupName, groupBodyUpdate)
                  .assertThat().field("displayName").is("Z"+groupName)
                  .and().field("id").is("GROUP_"+groupName)
                  .and().field("zones").isNull();
        restClient.assertStatusCodeIs(HttpStatus.OK);

        //GetGroupDetails:
        restClient.withCoreAPI().usingParams("include=zones").usingGroups().getGroupDetail("GROUP_"+groupName)
                  .assertThat().field("id").is("GROUP_"+groupName)
                  .and().field("zones").contains("APP.DEFAULT")
                  .and().field("isRoot").is(true);
        restClient.assertStatusCodeIs(HttpStatus.OK);

        //DeleteGroup:
        //-ve
        restClient.authenticateUser(userModel).withCoreAPI().usingGroups().deleteGroup("GROUP_"+groupName);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN);
        //+ve
        restClient.authenticateUser(adminUser).withCoreAPI().usingGroups().deleteGroup("GROUP_"+groupName);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.GROUPS, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.NODES }, executionType = ExecutionType.SANITY,
            description = "Verify creation, listing(only for person) and deletion of group memberships. ")
    public void createListDeleteGroupMembership() throws Exception
    {
        String groupName = "ZtestGroup" + UUID.randomUUID().toString();
        JsonObject groupBody = Json.createObjectBuilder().add("id", groupName).add("displayName", groupName).build();
        String groupBodyCreate = groupBody.toString();

        //GroupCreation:
        restClient.authenticateUser(adminUser).withCoreAPI().usingParams("include=zones").usingGroups().createGroup(groupBodyCreate);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        JsonObject groupMembershipBody = Json.createObjectBuilder().add("id", userModel.getUsername()).add("memberType", "PERSON").build();
        String groupMembershipBodyCreate = groupMembershipBody.toString();
        //MembershipCreation:
        //-ve
        restClient.authenticateUser(userModel).withCoreAPI().usingGroups().createGroupMembership("GROUP_"+groupName, groupMembershipBodyCreate);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN);
        //+ve
        restClient.authenticateUser(adminUser).withCoreAPI().usingGroups().createGroupMembership("GROUP_"+groupName, groupMembershipBodyCreate);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        //ListPersonMembership
        restClient.authenticateUser(userModel).withCoreAPI().usingUser(userModel).listGroupMemberships()
                                              .assertThat().entriesListContains("id", "GROUP_"+groupName);
        restClient.assertStatusCodeIs(HttpStatus.OK);

        //DeleteGroupMembership
        //-ve
        restClient.withCoreAPI().usingGroups().deleteGroupMembership("GROUP_"+groupName, userModel.getUsername());
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN);
        //+ve
        restClient.authenticateUser(adminUser).withCoreAPI().usingGroups().deleteGroupMembership("GROUP_"+groupName, userModel.getUsername());
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        //ListAgainPersonMembership
        restClient.withCoreAPI().usingUser(userModel).listGroupMemberships()
        .assertThat().entriesListDoesNotContain("id", "GROUP_"+groupName);
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.GROUPS, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.NODES }, executionType = ExecutionType.SANITY,
            description = "Verify listing of group memberships.")
    public void listGroupMembership() throws Exception
    {
        String groupName = "testGroup" + UUID.randomUUID().toString();
        JsonObject groupBody = Json.createObjectBuilder().add("id", groupName).add("displayName", groupName).build();
        String groupBodyCreate = groupBody.toString();

        //GroupCreation:
        restClient.authenticateUser(adminUser).withCoreAPI().usingParams("include=zones").usingGroups().createGroup(groupBodyCreate);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        JsonObject groupMembershipBody = Json.createObjectBuilder().add("id", userModel.getUsername()).add("memberType", "PERSON").build();
        String groupMembershipBodyCreate = groupMembershipBody.toString();

        //MembershipCreation
        restClient.withCoreAPI().usingGroups().createGroupMembership("GROUP_"+groupName, groupMembershipBodyCreate)
                  .assertThat().field("displayName").is(userModel.getUsername())
                  .and().field("id").is(userModel.getUsername())
                  .and().field("memberType").is("PERSON");
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        //ListGroupMembership
        RetryOperation op = new RetryOperation(){
            public void execute() throws Exception{
                restClient.withCoreAPI().usingGroups().listGroupMemberships("GROUP_"+groupName)
                          .assertThat().entriesListContains("id", userModel.getUsername());
                restClient.assertStatusCodeIs(HttpStatus.OK);
            }
        };
        Utility.sleep(1000, 35000, op);// Allow indexing to complete.
    }
}
