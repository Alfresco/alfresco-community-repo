package org.alfresco.rest.groups;

import java.util.UUID;

import jakarta.json.Json;
import jakarta.json.JsonObject;

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
    public void createListUpdateAndDeleteGroup() {
        String groupName = "ZtestGroup" + UUID.randomUUID();
        String subGroupName = "ZtestSubgroup" + UUID.randomUUID();
        String groupDescription = "ZtestGroup description" + UUID.randomUUID();
        JsonObject groupBody = Json.createObjectBuilder().add("id", groupName).add("displayName", groupName).add("description", groupDescription).build();
        JsonObject subgroupBody = Json.createObjectBuilder().add("id", subGroupName).add("displayName", subGroupName).build();
        String groupBodyCreate = groupBody.toString();
        String subgroupBodyCreate = subgroupBody.toString();

        //GroupCreation:
        //-ve
        restClient.authenticateUser(userModel).withCoreAPI().usingGroups().createGroup(groupBodyCreate);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN);
        //+ve
        restClient.authenticateUser(adminUser).withCoreAPI().usingParams("include=zones,hasSubgroups,description").usingGroups().createGroup(groupBodyCreate)
                                              .assertThat().field("zones").contains("APP.DEFAULT")
                                              .and().field("isRoot").is(true)
                                              .and().field("displayName").is(groupName)
                                              .and().field("description").is(groupDescription)
                                              .and().field("hasSubgroups").is(false);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        //AddChildGroup
        restClient.authenticateUser(adminUser).withCoreAPI().usingParams("include=zones").usingGroups().createGroup(subgroupBodyCreate);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        //LinkChildGroupToParent
        JsonObject groupMembershipGroupBody = Json.createObjectBuilder().add("id", "GROUP_"+subGroupName).add("memberType", "GROUP").build();
        String groupMembershipGroupBodyCreate = groupMembershipGroupBody.toString();
        restClient.authenticateUser(adminUser).withCoreAPI().usingGroups().createGroupMembership("GROUP_"+groupName, groupMembershipGroupBodyCreate);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        //ListGroups:
        restClient.withCoreAPI().usingParams("orderBy=displayName DESC&maxItems=10").usingGroups().listGroups()
                  .assertThat().entriesListContains("id", "GROUP_"+groupName)
                  .and().entriesListContains("id", "GROUP_"+subGroupName)
                  .and().entriesListDoesNotContain("zones")
                  .and().paginationField("maxItems").is("10");
        restClient.assertStatusCodeIs(HttpStatus.OK);

        groupBody = Json.createObjectBuilder().add("displayName", "Z"+groupName).add("description", "Z"+groupDescription).build();
        String groupBodyUpdate = groupBody.toString();
        //UpdateGroup:
        restClient.withCoreAPI().usingGroups().updateGroupDetails("GROUP_"+groupName, groupBodyUpdate)
                  .assertThat().field("displayName").is("Z"+groupName)
                  .and().field("description").is("Z"+groupDescription)
                  .and().field("id").is("GROUP_"+groupName)
                  .and().field("zones").isNull();
        restClient.assertStatusCodeIs(HttpStatus.OK);

        //GetGroupDetails:
        restClient.withCoreAPI().usingParams("include=zones,hasSubgroups,description").usingGroups().getGroupDetail("GROUP_"+groupName)
                  .assertThat().field("id").is("GROUP_"+groupName)
                  .and().field("zones").contains("APP.DEFAULT")
                  .and().field("isRoot").is(true)
                  .and().field("hasSubgroups").is(true);
        restClient.assertStatusCodeIs(HttpStatus.OK);

        //DeleteChildGroup:
        restClient.authenticateUser(adminUser).withCoreAPI().usingGroups().deleteGroup("GROUP_"+subGroupName);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        //VerifyIfParentHasNoSubgroups:
        restClient.withCoreAPI().usingParams("include=zones,hasSubgroups").usingGroups().getGroupDetail("GROUP_"+groupName)
                .assertThat().field("id").is("GROUP_"+groupName)
                .and().field("hasSubgroups").is(false);
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
    public void createListDeleteGroupMembership() {
        String groupName = "ZtestGroup" + UUID.randomUUID();
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
        String groupName = "testGroup" + UUID.randomUUID();
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
        RetryOperation op = () -> {
            restClient.withCoreAPI().usingGroups().listGroupMemberships("GROUP_"+groupName)
                      .assertThat().entriesListContains("id", userModel.getUsername());
            restClient.assertStatusCodeIs(HttpStatus.OK);
        };
        Utility.sleep(500, 35000, op);// Allow indexing to complete.
    }
}
