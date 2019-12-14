package org.alfresco.rest.misc;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.dataprep.SiteService.Visibility;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestActivityModelsCollection;
import org.alfresco.rest.model.RestCommentModel;
import org.alfresco.rest.model.RestCommentModelsCollection;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestFavoriteSiteModel;
import org.alfresco.rest.model.RestPersonFavoritesModelsCollection;
import org.alfresco.rest.model.RestRatingModel;
import org.alfresco.rest.model.RestSiteMemberModel;
import org.alfresco.rest.model.RestSiteMembershipRequestModelsCollection;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.rest.model.RestTaskModel;
import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.ActivityType;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

public class FunctionalCasesTests extends RestTest
{
    private RestSiteMemberModel updatedMember;
    private RestSiteMembershipRequestModelsCollection returnedCollection;
    private RestFavoriteSiteModel restFavoriteSiteModel;
    private RestActivityModelsCollection activities;
    private FileModel file;
    
    /**
     * Scenario:
     * 1. Add a site member as Manager
     * 2. Update it's role to Collaborator
     * 3. Update it's role to Contributor
     * 4. Update it's role to Consumer
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify that manager is able to update manager with different roles and gets status code CREATED (201)")
    public void managerIsAbleToUpdateManagerWithDifferentRoles()
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteManager);
        SiteModel publicSite = dataSite.usingUser(dataUser.getAdminUser()).createPublicRandomSite();
        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingSite(publicSite).addPerson(testUser)
               .assertThat().field("id").is(testUser.getUsername())
               .and().field("role").is(testUser.getUserRole());
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        testUser.setUserRole(UserRole.SiteCollaborator);
        updatedMember = restClient.withCoreAPI()
                .usingSite(publicSite).updateSiteMember(testUser);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(testUser.getUsername()).and().field("role").is(testUser.getUserRole());
        
        testUser.setUserRole(UserRole.SiteContributor);
        updatedMember = restClient.withCoreAPI()
                .usingSite(publicSite).updateSiteMember(testUser);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(testUser.getUsername()).and().field("role").is(testUser.getUserRole());
        
        testUser.setUserRole(UserRole.SiteConsumer);
        updatedMember = restClient.withCoreAPI()
                .usingSite(publicSite).updateSiteMember(testUser);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(testUser.getUsername()).and().field("role").is(testUser.getUserRole());
    }
    
    /**
     * Scenario:
     * 1. Add a site member as Consumer
     * 2. Update it's role to Contributor
     * 3. Update it's role to Collaborator
     * 4. Update it's role to Manager
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify that manager is able to update consumer with different roles and gets status code CREATED (201)")
    public void managerIsAbleToUpdateConsumerWithDifferentRoles()
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteConsumer);
        SiteModel publicSite = dataSite.usingUser(dataUser.getAdminUser()).createPublicRandomSite();
        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingSite(publicSite).addPerson(testUser)
               .assertThat().field("id").is(testUser.getUsername())
               .and().field("role").is(testUser.getUserRole());
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        testUser.setUserRole(UserRole.SiteContributor);
        updatedMember = restClient.withCoreAPI()
                .usingSite(publicSite).updateSiteMember(testUser);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(testUser.getUsername()).and().field("role").is(testUser.getUserRole());
        
        testUser.setUserRole(UserRole.SiteCollaborator);
        updatedMember = restClient.withCoreAPI()
                .usingSite(publicSite).updateSiteMember(testUser);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(testUser.getUsername()).and().field("role").is(testUser.getUserRole());
        
        testUser.setUserRole(UserRole.SiteManager);
        updatedMember = restClient.withCoreAPI()
                .usingSite(publicSite).updateSiteMember(testUser);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(testUser.getUsername()).and().field("role").is(testUser.getUserRole());
    }
    
    /**
     * Scenario:
     * 1. Create site membership request
     * 2. Approve site membership request
     * 3. Add site to Favorites
     * 4. Delete site from Favorites
     */
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE },
            executionType = ExecutionType.REGRESSION, description = "Approve request, add site to favorites, then delete it from favorites")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void approveRequestAddAndDeleteSiteFromFavorites() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        SiteModel moderatedSite = dataSite.usingUser(dataUser.getAdminUser()).createModeratedRandomSite();
        restClient.authenticateUser(newMember).withCoreAPI().usingMe().addSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        RestTaskModel taskModel = restClient.authenticateUser(newMember).withWorkflowAPI().getTasks().getTaskModelByDescription(moderatedSite);
        workflow.approveSiteMembershipRequest(dataUser.getAdminUser().getUsername(), dataUser.getAdminUser().getPassword(), taskModel.getId(), true, "Approve");
        returnedCollection = restClient.authenticateUser(newMember).withCoreAPI().usingMe().getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat().entriesListDoesNotContain("id", moderatedSite.getId());
        
        restFavoriteSiteModel = restClient.authenticateUser(newMember).withCoreAPI().usingUser(newMember).addFavoriteSite(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restFavoriteSiteModel.assertThat().field("id").is(moderatedSite.getId());
        
        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser().removeFavoriteSite(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }
    
    /**
     * Scenario:
     * 1. Create site membership request
     * 2. Reject site membership request
     * 3. Add moderated site to Favorites
     * 4. Create site membership request again
     * 5. Approve site membership request
     * 6. Delete member from site
     */
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE },
            executionType = ExecutionType.REGRESSION, description = "Reject request, add moderated site to favorites, create request again and approve it")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void rejectRequestAddModeratedSiteToFavorites() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        SiteModel moderatedSite = dataSite.usingUser(dataUser.getAdminUser()).createModeratedRandomSite();
        restClient.authenticateUser(newMember).withCoreAPI().usingMe().addSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        RestTaskModel taskModel = restClient.authenticateUser(newMember).withWorkflowAPI().getTasks().getTaskModelByDescription(moderatedSite);
        workflow.approveSiteMembershipRequest(dataUser.getAdminUser().getUsername(), dataUser.getAdminUser().getPassword(), taskModel.getId(), false, "Rejected");
        returnedCollection = restClient.authenticateUser(newMember).withCoreAPI().usingMe().getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat().entriesListDoesNotContain("id", moderatedSite.getId());
        
        restFavoriteSiteModel = restClient.authenticateUser(newMember).withCoreAPI().usingUser(newMember).addFavoriteSite(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restFavoriteSiteModel.assertThat().field("id").is(moderatedSite.getId());
        
        restClient.authenticateUser(newMember).withCoreAPI().usingMe().addSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        taskModel = restClient.authenticateUser(newMember).withWorkflowAPI().getTasks().getTaskModelByDescription(moderatedSite);
        workflow.approveSiteMembershipRequest(dataUser.getAdminUser().getUsername(), dataUser.getAdminUser().getPassword(), taskModel.getId(), true, "Accept");
        
        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingUser(newMember).deleteSiteMember(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        
        restClient.withCoreAPI().usingSite(moderatedSite).getSiteMembers().assertThat().entriesListDoesNotContain("id", newMember.getUsername());
    }
    
    /**
     * Scenario:
     * 1. Add file
     * 2. Check file is included in person activities list
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, 
            description = "Add a file and check that activity is included in person activities")
    public void addFileThenGetPersonActivities() throws Exception
    {
        UserModel manager = dataUser.createRandomTestUser();
        SiteModel publicSite = dataSite.usingUser(dataUser.getAdminUser()).createPublicRandomSite();
        dataUser.addUserToSite(manager, publicSite, UserRole.SiteManager);
        file = dataContent.usingUser(manager).usingSite(publicSite).createContent(DocumentType.TEXT_PLAIN);
        activities = restClient.authenticateUser(manager).withCoreAPI().usingAuthUser().getPersonActivitiesUntilEntriesCountIs(3);
        activities.assertThat().entriesListIsNotEmpty()
            .and().entriesListContains("siteId", publicSite.getId())
            .and().entriesListContains("activityType", "org.alfresco.documentlibrary.file-added")
            .and().entriesListContains("activitySummary.objectId", file.getNodeRefWithoutVersion());
    }
    
    /**
     * Scenario:
     * 1. Add a comment to a file
     * 2. Check that comment is included in person activities list
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, 
            description = "Add a comment to a file and check that activity is included in person activities")
    public void addCommentThenGetPersonActivities() throws Exception
    {
        UserModel manager = dataUser.createRandomTestUser();
        SiteModel publicSite = dataSite.usingUser(dataUser.getAdminUser()).createPublicRandomSite();
        dataUser.addUserToSite(manager, publicSite, UserRole.SiteManager);
        file = dataContent.usingUser(manager).usingSite(publicSite).createContent(DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(manager).withCoreAPI().usingResource(file).addComment("new comment");
        activities = restClient.authenticateUser(manager).withCoreAPI().usingAuthUser().getPersonActivitiesUntilEntriesCountIs(4);
        activities.assertThat().entriesListIsNotEmpty()
            .and().entriesListContains("siteId", publicSite.getId())
            .and().entriesListContains("activityType", "org.alfresco.comments.comment-created")
            .and().entriesListContains("activitySummary.objectId", file.getNodeRefWithoutVersion());
    }
    
    /**
     * Scenario:
     * 1. Add file then delete it
     * 2. Check action is included in person activities list
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, 
            description = "Add a file, delete it and check that activity is included in person activities")
    public void addFileDeleteItThenGetPersonActivities() throws Exception
    {
        UserModel manager = dataUser.createRandomTestUser();
        SiteModel publicSite = dataSite.usingUser(dataUser.getAdminUser()).createPublicRandomSite();
        dataUser.addUserToSite(manager, publicSite, UserRole.SiteManager);
        file = dataContent.usingUser(manager).usingSite(publicSite).createContent(DocumentType.TEXT_PLAIN);
        dataContent.usingUser(manager).usingResource(file).deleteContent();
        activities = restClient.authenticateUser(manager).withCoreAPI().usingAuthUser().getPersonActivitiesUntilEntriesCountIs(4);
        activities.assertThat().entriesListIsNotEmpty()
            .and().entriesListContains("siteId", publicSite.getId())
            .and().entriesListContains("activityType", "org.alfresco.documentlibrary.file-deleted")
            .and().entriesListContains("activitySummary.objectId", file.getNodeRefWithoutVersion());
    }

    /**
     * 1. Post one comment
     * 2. Get comment details
     * 3. Update comment
     * 4. Get again comment details
     * 5. Delete comment
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS },
            executionType = ExecutionType.REGRESSION,
            description = "Add comment to a file, then get comment details. Update it and check that get comment returns updated details. Delete comment then check that file has no comments.")
    public void addUpdateDeleteCommentThenGetCommentDetails() throws Exception
    {
        UserModel manager = dataUser.createRandomTestUser();
        SiteModel publicSite = dataSite.usingUser(dataUser.getAdminUser()).createPublicRandomSite();
        dataUser.addUserToSite(manager, publicSite, UserRole.SiteManager);
        file = dataContent.usingUser(manager).usingSite(publicSite).createContent(DocumentType.TEXT_PLAIN);
        RestCommentModel newComment = restClient.authenticateUser(manager).withCoreAPI().usingResource(file).addComment("new comment");
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        RestCommentModelsCollection fileComments = restClient.withCoreAPI().usingResource(file).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        fileComments.assertThat().entriesListContains("content", newComment.getContent());

        RestCommentModel updatedComment = restClient.withCoreAPI().usingResource(file).updateComment(newComment, "updated comment");
        restClient.assertStatusCodeIs(HttpStatus.OK);

        fileComments = restClient.withCoreAPI().usingResource(file).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        fileComments.assertThat().entriesListContains("content", updatedComment.getContent()).assertThat().entriesListDoesNotContain("content", newComment.getContent());

        restClient.withCoreAPI().usingResource(file).deleteComment(updatedComment);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        fileComments = restClient.withCoreAPI().usingResource(file).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        fileComments.assertThat().entriesListIsEmpty();
    }

    /**
     * 1. Post one comment
     * 2. Delete comment
     * 3. Post the same comment again
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS },
            executionType = ExecutionType.REGRESSION,
            description = "Add a comment to a file, delete it, then added the same comment again.")
    public void checkThatADeletedCommentCanBePostedAgain() throws Exception
    {
        UserModel manager = dataUser.createRandomTestUser();
        SiteModel publicSite = dataSite.usingUser(dataUser.getAdminUser()).createPublicRandomSite();
        dataUser.addUserToSite(manager, publicSite, UserRole.SiteManager);
        file = dataContent.usingUser(manager).usingSite(publicSite).createContent(DocumentType.TEXT_PLAIN);
        RestCommentModel newComment = restClient.authenticateUser(manager).withCoreAPI().usingResource(file).addComment("new comment");
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        RestCommentModelsCollection fileComments = restClient.withCoreAPI().usingResource(file).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        fileComments.assertThat().entriesListContains("content", newComment.getContent());

        restClient.withCoreAPI().usingResource(file).deleteComment(newComment);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        fileComments = restClient.withCoreAPI().usingResource(file).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        fileComments.assertThat().entriesListIsEmpty();

        restClient.authenticateUser(manager).withCoreAPI().usingResource(file).addComment("new comment");
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        fileComments = restClient.withCoreAPI().usingResource(file).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        fileComments.assertThat().entriesListContains("content", newComment.getContent());
    }

    /**
     * Scenario:
     * 1. join an user to a site
     * 2. Check action is included in person activities list
     */

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, 
            description = "Create an user, join the user to a site and check that activity is included in person activities")
    public void joinUserToSiteThenGetPersonActivities() throws Exception
    {
        UserModel userJoinSite = dataUser.createRandomTestUser();
        SiteModel publicSite = dataSite.usingUser(dataUser.getAdminUser()).createPublicRandomSite();
        restClient.authenticateUser(userJoinSite).withCoreAPI().usingMe().addSiteMembershipRequest(publicSite);
        activities = restClient.withCoreAPI().usingAuthUser().getPersonActivitiesUntilEntriesCountIs(1);
        activities.assertThat().entriesListIsNotEmpty().and()
                .entriesListContains("siteId", publicSite.getId()).and()
                .entriesListContains("activityType", "org.alfresco.site.user-joined").and()
                .entriesListContains("activitySummary.memberPersonId", userJoinSite.getUsername());
    }
    
    /**
     * Scenario:
     * 1. Add user to private site
     * 2. Remove user from private site
     * 3. User creates membership request to the same private site
     */
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE },
            executionType = ExecutionType.REGRESSION, description = "Verify membership request by user after it was removed from site gets status code 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void userCanNotCreateMembershipRequestIfItWasRemovedFromPrivateSite() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        newMember.setUserRole(UserRole.SiteCollaborator);
        SiteModel privateSite = dataSite.usingUser(dataUser.getAdminUser()).createPrivateRandomSite();
        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingSite(privateSite).addPerson(newMember)
               .assertThat().field("id").is(newMember.getUsername());
        restClient.assertStatusCodeIs(HttpStatus.CREATED);   
        
        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingUser(newMember).deleteSiteMember(privateSite);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        
        restClient.authenticateUser(newMember).withCoreAPI().usingMe().addSiteMembershipRequest(privateSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                  .assertLastError().containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, newMember.getUsername(), privateSite.getTitle()));
    }
    
    /**
     * Scenario:
     * 1. User creates membership request to moderated site
     * 2. Accept membership request
     * 3. Remove user from moderated site
     * 4. Add user on moderated site
     */
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE },
            executionType = ExecutionType.REGRESSION, description = "Verify user can be added back after if was removed from site")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void userCanBeAddedAfterItWasRemovedFromSite() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        newMember.setUserRole(UserRole.SiteCollaborator);
        SiteModel moderatedSite = dataSite.usingUser(dataUser.getAdminUser()).createModeratedRandomSite();
        restClient.authenticateUser(newMember).withCoreAPI().usingMe().addSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        RestTaskModel taskModel = restClient.authenticateUser(newMember).withWorkflowAPI().getTasks().getTaskModelByDescription(moderatedSite);
        workflow.approveSiteMembershipRequest(dataUser.getAdminUser().getUsername(), dataUser.getAdminUser().getPassword(), taskModel.getId(), true, "Accept");
        
        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingUser(newMember).deleteSiteMember(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withCoreAPI().usingSite(moderatedSite).getSiteMembers().assertThat().entriesListDoesNotContain("id", newMember.getUsername());

        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingSite(moderatedSite).addPerson(newMember)
               .assertThat().field("id").is(newMember.getUsername());
        restClient.assertStatusCodeIs(HttpStatus.CREATED);   
        restClient.withCoreAPI().usingSite(moderatedSite).getSiteMembers().assertThat().entriesListContains("id", newMember.getUsername());
    }

    /**
     * Scenario:
     * 1. Create document in site
     * 2. Add comment
     * 3. Delete document
     * 4. Get comments and check if the above comment was deleted
     */
    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS },
            executionType = ExecutionType.REGRESSION, description = "Check that a comment of a document was also removed after deleting the document")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void checkTheCommentOfADocumentThatWasDeletedDoesNotExist() throws Exception
    {
        SiteModel publicSite = dataSite.usingUser(dataUser.getAdminUser()).createPublicRandomSite();
        file = dataContent.usingSite(publicSite).usingUser(dataUser.getAdminUser()).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        String newContent = "This is a new comment added by " + dataUser.getAdminUser().getUsername();

        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingResource(file).addComment(newContent)
                .assertThat().field("content").isNotEmpty()
                .and().field("content").is(newContent);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        dataContent.usingUser(dataUser.getAdminUser()).usingResource(file).deleteContent();

        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingResource(file).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary((String.format(RestErrorModel.ENTITY_WAS_NOT_FOUND, file.getNodeRefWithoutVersion())));
    }

    /**
     * Scenario:
     * 1. Add user to private site
     * 2. Add comment to a document of private site
     * 3. Remove user from site
     * 4. Get comments and check if the above comment was deleted
     */
    @Bug(id="REPO-4854")
    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS },
            executionType = ExecutionType.REGRESSION, description = "Check that a comment of a document from a private site is not deleted after user is removed")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void checkThatCommentIsNotDeletedWhenPrivateSiteMemberIsRemoved() throws Exception
    {
        UserModel newUser = dataUser.createRandomTestUser();
        newUser.setUserRole(UserRole.SiteManager);
        SiteModel privateSite = dataSite.usingUser(dataUser.getAdminUser()).createPrivateRandomSite();
        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingSite(privateSite).addPerson(newUser);
        
        file = dataContent.usingSite(privateSite).usingUser(dataUser.getAdminUser()).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        String newContent = "This is a new comment added by " + newUser.getUsername();
        
        restClient.authenticateUser(newUser).withCoreAPI().usingResource(file).addComment(newContent)
                   .assertThat().field("content").isNotEmpty()
                   .and().field("content").is(newContent);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingSite(privateSite).deleteSiteMember(newUser);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        Utility.sleep(200, 30000, () ->
                restClient.withCoreAPI().usingSite(privateSite).getSiteMembers()
                        .assertThat().entriesListDoesNotContain("id", newUser.getUsername()));
        
        RestCommentModelsCollection comments = restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingResource(file).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListContains("content", newContent)
            .and().entriesListContains("createdBy.id", newUser.getUsername());
    }

    /**
     * Scenario:
     * 1. Add one file to favorites then add a comment to this file
     */
    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS },
            executionType = ExecutionType.REGRESSION,
            description = "Add one file to favorites then add a comment to this file")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void commentAFavoriteFile() throws Exception
    {
        UserModel manager = dataUser.createRandomTestUser();
        SiteModel publicSite = dataSite.usingUser(dataUser.getAdminUser()).createPublicRandomSite();
        dataUser.addUserToSite(manager, publicSite, UserRole.SiteManager);
        file = dataContent.usingSite(publicSite).usingUser(dataUser.getAdminUser()).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(manager).withCoreAPI().usingMe().addFileToFavorites(file);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.withCoreAPI().usingAuthUser().getFavorites()
                .assertThat().entriesListContains("targetGuid", file.getNodeRefWithoutVersion());

        RestCommentModel comment = restClient.withCoreAPI().usingResource(file).addComment("new comment");
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        comment.assertThat().field("content").is("new comment");
    }

    /**
     * Scenario:
     * 1. Remove one file from favorites then add a comment to this file
     */
    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS },
            executionType = ExecutionType.REGRESSION,
            description = "Remove one file from favorites then add a comment to this file")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void commentFileRemovedFromFavorites() throws Exception
    {
        UserModel manager = dataUser.createRandomTestUser();
        SiteModel publicSite = dataSite.usingUser(dataUser.getAdminUser()).createPublicRandomSite();
        dataUser.addUserToSite(manager, publicSite, UserRole.SiteManager);
        file = dataContent.usingSite(publicSite).usingUser(dataUser.getAdminUser()).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(manager).withCoreAPI().usingMe().addFileToFavorites(file);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.withCoreAPI().usingMe().deleteFileFromFavorites(file);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        restClient.withCoreAPI().usingAuthUser().getFavorites()
                .assertThat().entriesListDoesNotContain("targetGuid", file.getNodeRefWithoutVersion());

        RestCommentModel comment = restClient.withCoreAPI().usingResource(file).addComment("new comment");
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        comment.assertThat().field("content").is("new comment");
    }

    /**
     * Scenario:
     * 1. Add public site to favorites
     * 2. Change site visibility to moderated
     * 3. Check favorites
     * 4. Change site visibility to private
     * 5. Check favorites
     */
    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES },
            executionType = ExecutionType.REGRESSION,
            description = "Check favorite sites after a favorite site visibility is changed")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void changeFavoriteSiteVisibilityThenCheckFavorites() throws Exception
    {
        UserModel manager = dataUser.createRandomTestUser();
        SiteModel publicSite = dataSite.usingUser(dataUser.getAdminUser()).createPublicRandomSite();
        dataUser.addUserToSite(manager, publicSite, UserRole.SiteManager);
        SiteModel favoriteSite = dataSite.usingUser(manager).createPublicRandomSite();
        UserModel regularUser = dataUser.createRandomTestUser();

        restClient.authenticateUser(regularUser).withCoreAPI().usingAuthUser().addSiteToFavorites(favoriteSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        RestPersonFavoritesModelsCollection userFavoriteSites = restClient.withCoreAPI().usingAuthUser().where().targetSiteExist().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        userFavoriteSites.assertThat().entriesListContains("targetGuid", favoriteSite.getGuid())
                .assertThat().entriesListContains("target.site.visibility", favoriteSite.getVisibility().name())
                .assertThat().entriesListContains("target.site.id", favoriteSite.getId());

        dataSite.usingUser(manager).updateSiteVisibility(favoriteSite, Visibility.MODERATED);

        userFavoriteSites = restClient.authenticateUser(regularUser).withCoreAPI().usingAuthUser().where().targetSiteExist().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        userFavoriteSites.assertThat().entriesListContains("targetGuid", favoriteSite.getGuid())
                .assertThat().entriesListContains("target.site.visibility", "MODERATED")
                .assertThat().entriesListContains("target.site.id", favoriteSite.getId());

        dataSite.usingUser(manager).updateSiteVisibility(favoriteSite, Visibility.PRIVATE);

        userFavoriteSites = restClient.authenticateUser(regularUser).withCoreAPI().usingAuthUser().where().targetSiteExist().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        userFavoriteSites.assertThat().entriesListIsEmpty();

        dataSite.usingUser(manager).updateSiteVisibility(favoriteSite, Visibility.PUBLIC);

        userFavoriteSites = restClient.authenticateUser(regularUser).withCoreAPI().usingAuthUser().where().targetSiteExist().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        userFavoriteSites.assertThat().entriesListContains("targetGuid", favoriteSite.getGuid())
                .assertThat().entriesListContains("target.site.visibility", favoriteSite.getVisibility().name())
                .assertThat().entriesListContains("target.site.id", favoriteSite.getId());
    }

    /**
     * Scenario:
     * 1. Regular user adds moderated site membership request
     * 2. Site manager approves the request
     * 3. Site manager updates the new member role to Manager
     * 4. New member adds comment to a file on site
     * 5. New member likes and rate one file
     * 6. New member adds tags to a file
     * 7. New member adds site to favorite
     * 8. New member adds, then deletes a site member
     */
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES },
            executionType = ExecutionType.REGRESSION,
            description = "Check that a user who joins a moderated site as manager is able to comment, rate, tag an existing file from the site, add site to favorites, add and remove site members.")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void checkNewManagerActions() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        SiteModel moderatedSite = dataSite.usingUser(dataUser.getAdminUser()).createModeratedRandomSite();
        file = dataContent.usingSite(moderatedSite).usingUser(dataUser.getAdminUser()).createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        restClient.authenticateUser(newMember).withCoreAPI().usingMe().addSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        RestTaskModel taskModel = restClient.withWorkflowAPI().getTasks().getTaskModelByDescription(moderatedSite);
        workflow.approveSiteMembershipRequest(dataUser.getAdminUser().getUsername(), dataUser.getAdminUser().getPassword(), taskModel.getId(), true, "Approve");
        returnedCollection = restClient.withCoreAPI().usingMe().getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat().entriesListDoesNotContain("id", moderatedSite.getId());

        newMember.setUserRole(UserRole.SiteManager);
        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingSite(moderatedSite).updateSiteMember(newMember)
                .assertThat().field("id").is(newMember.getUsername())
                .and().field("role").is(newMember.getUserRole());
        restClient.assertStatusCodeIs(HttpStatus.OK);

        RestCommentModel comment = restClient.authenticateUser(newMember).withCoreAPI().usingResource(file).addComment("new comment");
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        comment.assertThat().field("content").is("new comment");

        RestRatingModel returnedRatingModel = restClient.withCoreAPI().usingResource(file).likeDocument();
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedRatingModel.assertThat().field("myRating").is("true")
                .and().field("id").is("likes")
                .and().field("aggregate").isNotEmpty();

        returnedRatingModel = restClient.withCoreAPI().usingResource(file).rateStarsToDocument(5);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedRatingModel.assertThat().field("myRating").is("5")
                .and().field("id").is("fiveStar")
                .and().field("aggregate").isNotEmpty();

        RestTagModel tag = restClient.withCoreAPI().usingResource(file).addTag("filetag");
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        tag.assertThat().field("tag").is("filetag")
                .and().field("id").isNotEmpty();

        restFavoriteSiteModel = restClient.withCoreAPI().usingAuthUser().addFavoriteSite(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restFavoriteSiteModel.assertThat().field("id").is(moderatedSite.getId());

        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteManager);
        restClient.withCoreAPI().usingSite(moderatedSite).addPerson(testUser)
                .assertThat().field("id").is(testUser.getUsername())
                .and().field("role").is(testUser.getUserRole());
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.withCoreAPI().usingSite(moderatedSite).deleteSiteMember(testUser);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        restClient.withCoreAPI().usingSite(moderatedSite).getSiteMembers()
                .assertThat().entriesListDoesNotContain("id", testUser.getUsername());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }
    
    /**
     * Scenario:
     * 1. Add user to site
     * 2. Create folder
     * 3. Create document
     * 4. Add comment to the document
     * 5. Like document
     * 6. Update Comment
     * 7. Update user role
     * 8. Delete like rating
     * 9. Delete site member
     * 10. Delete comment
     * 11. Get Activities
     *
     */
    @Bug(id="REPO-1830")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.ACTIVITIES }, executionType = ExecutionType.REGRESSION, description = "Verify user gets its activities with Rest API and response is successful")
    public void userGetsItsPeopleActivities() throws Exception
    {  
        UserModel newUser = dataUser.createRandomTestUser();
        SiteModel userSiteModel = dataSite.usingUser(dataUser.getAdminUser()).createPublicRandomSite();
        
        dataUser.addUserToSite(newUser, userSiteModel, UserRole.SiteCollaborator);
        dataContent.usingUser(newUser).usingSite(userSiteModel).createFolder();

        FileModel fileInSite = dataContent.usingUser(newUser).usingSite(userSiteModel).createContent(DocumentType.TEXT_PLAIN);
        String newContent = "This is a new comment added by " + newUser.getUsername();
        RestCommentModel commentModel = restClient.authenticateUser(newUser).withCoreAPI().usingResource(fileInSite).addComment(newContent);
        restClient.authenticateUser(newUser).withCoreAPI().usingResource(fileInSite).likeDocument();
        
        restClient.withCoreAPI().usingResource(fileInSite).updateComment(commentModel, "new Content");
        newUser.setUserRole(UserRole.SiteManager);
        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingSite(userSiteModel).updateSiteMember(newUser);
        
        restClient.authenticateUser(newUser).withCoreAPI().usingResource(fileInSite).deleteLikeRating();
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withCoreAPI().usingResource(fileInSite).deleteComment(commentModel);  
        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingSite(userSiteModel).deleteSiteMember(newUser);

        RestActivityModelsCollection restActivityModelsCollection = restClient.authenticateUser(newUser).withCoreAPI().usingMe().getPersonActivitiesUntilEntriesCountIs(10);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restActivityModelsCollection.assertThat().paginationField("count").is("10");
        
        restActivityModelsCollection = restClient.authenticateUser(newUser).withCoreAPI().usingMe().getPersonActivities();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restActivityModelsCollection.assertThat().paginationField("count").is("10");
        
        restActivityModelsCollection.assertThat().entriesListContains("activityType", ActivityType.USER_JOINED.toString()).assertThat()
            .entriesListContains("activityType", ActivityType.FILE_ADDED.toString()).assertThat()
            .entriesListContains("activityType", ActivityType.FOLDER_ADDED.toString()).assertThat()
            .entriesListContains("activityType", ActivityType.COMMENT_CREATED.toString()).assertThat()
            .entriesListContains("activityType", ActivityType.FILE_LIKED.toString()).assertThat()
            .entriesListContains("activityType", ActivityType.COMMENT_UPDATED.toString()).assertThat()
            .entriesListContains("activityType", ActivityType.USER_ROLE_CHANGED.toString()).assertThat()
            .entriesListContains("activityType", ActivityType.COMMENT_DELETED.toString()).assertThat()
            .entriesListContains("activityType", ActivityType.USER_LEFT.toString());
    }

}