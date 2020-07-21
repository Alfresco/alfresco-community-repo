package org.alfresco.cmis;

import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.data.AclCapabilities;
import org.apache.chemistry.opencmis.commons.data.RepositoryCapabilities;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class RepositoryInfoTests extends CmisTest
{
    UserModel testUser;
    UserModel unauthorizedUser;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        unauthorizedUser = dataUser.createRandomTestUser();
    }

    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.SANITY, description = "Verify that valid user can get repositories")
    public void validUserCanGetRepositories() throws Exception
    {
        Assert.assertNotNull(cmisApi.authenticateUser(testUser).getSession());
    }

    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS}, expectedExceptions = CmisUnauthorizedException.class)
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.SANITY, description = "Verify that valid user with invalid password cannot get repositories")
    public void unauthorizedUserCannotGetRepositories() throws Exception
    {
        unauthorizedUser.setPassword("invalidPass");
        cmisApi.authenticateUser(unauthorizedUser);
    }
    
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.SANITY, description = "Verify that valid user can get the repository informations")
    public void validUserCanGetRepositoryInfo() throws Exception
    {
        RepositoryInfo repoInfo = cmisApi.authenticateUser(testUser).getRepositoryInfo();
        Assert.assertNotNull(repoInfo.getRootFolderId());
        Assert.assertNotNull(repoInfo.getProductVersion());
        Assert.assertNotNull(repoInfo.getRootFolderId());
        Assert.assertNotNull(repoInfo.getId());
        Assert.assertEquals(repoInfo.getProductName(), String.format("Alfresco %s", serverHealth.getAlfrescoEdition()));
        Assert.assertEquals(repoInfo.getVendorName(), "Alfresco");
        RepositoryCapabilities capabilities = repoInfo.getCapabilities();
        Assert.assertNotNull(capabilities);
        Assert.assertEquals(capabilities.getContentStreamUpdatesCapability().value(), "anytime", "Verify repository capability: capabilityContentStreamUpdatability");
        Assert.assertEquals(capabilities.getRenditionsCapability().value(), "read", "Verify repository capability: capabilityRenditions");
        Assert.assertTrue(capabilities.isGetDescendantsSupported(), "Verify repository capability: capabilityGetDescendants");
        Assert.assertTrue(capabilities.isGetFolderTreeSupported(), "Verify repository capability: capabilityGetFolderTree");
        Assert.assertTrue(capabilities.isMultifilingSupported(), "Verify repository capability: capabilityMultifiling");
        Assert.assertFalse(capabilities.isUnfilingSupported(), "Verify repository capability: capabilityUnfiling");
        Assert.assertFalse(capabilities.isVersionSpecificFilingSupported(), "Verify repository capability: capabilityVersionSpecificFiling");
        Assert.assertFalse(capabilities.isPwcSearchableSupported(), "Verify repository capability: capabilityPWCSearchable");
        Assert.assertTrue(capabilities.isPwcUpdatableSupported(), "Verify repository capability: capabilityPWCUpdatable");
        Assert.assertFalse(capabilities.isAllVersionsSearchableSupported(), "Verify repository capability: capabilityAllVersionsSearchable");
        Assert.assertEquals(capabilities.getQueryCapability().value(), "bothcombined", "Verify repository capability: capabilityQuery");
        Assert.assertEquals(capabilities.getJoinCapability().value(), "none", "Verify repository capability: capabilityJoin");
        Assert.assertEquals(capabilities.getAclCapability().value(), "manage", "Verify repository capability: capabilityACL");
    }
    
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.SANITY, description = "Verify that valid user can get Acl Capabilities")
    public void validUserCanGetAclCapabilities() throws Exception
    {
        AclCapabilities aclCapabilities = cmisApi.authenticateUser(testUser).getAclCapabilities();
        Assert.assertNotNull(aclCapabilities);
        Assert.assertFalse(aclCapabilities.getPermissions().isEmpty(), "Verify acl capabilities: getPermissions");
        Assert.assertEquals(aclCapabilities.getSupportedPermissions().value(), "both", "Verify acl capabilities: getSupportedPermissions");
        Assert.assertEquals(aclCapabilities.getAclPropagation().value(), "propagate", "Verify acl capabilities: getAclPropagation");
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = CmisUnauthorizedException.class)
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION, description = "Verify that invalid user cannot get repositories")
    public void invalidUserCannotGetRepositories() throws Exception
    {
        UserModel invalidUser = UserModel.getRandomUserModel();
        cmisApi.authenticateUser(invalidUser).getRepositoryInfo();
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisConnectionException.class})
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION, description = "Verify that invalid user cannot get repositories")
    public void userCannotGetRepositoriesUsingWrongBrowserUrl() throws Exception
    {
        String wrongUrlPath = "//alfresco//api//-default-//public//cmis//versions//1.1";
        cmisApi.authUserUsingBrowserUrlAndBindingType(testUser, wrongUrlPath, BindingType.BROWSER.value()).getRepositoryInfo();
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {IllegalArgumentException.class, CmisRuntimeException.class}, expectedExceptionsMessageRegExp = "Invalid binding type!")
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION, description = "Verify that invalid user cannot get repositories")
    public void userCannotGetRepositoriesUsingWrongBindingType() throws Exception
    {
        String wrongBindingType = BindingType.BROWSER.value() + "w";
        cmisApi.authUserUsingBrowserUrlAndBindingType(testUser, cmisApi.cmisProperties.envProperty().getFullServerUrl() + cmisApi.cmisProperties.getBasePath(), wrongBindingType).getRepositoryInfo();
    }
    
    @Bug(id="REPO-4301")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = CmisUnauthorizedException.class)
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION, description = "Verify that disabled user cannot get repositories")
    public void disabledUserCannotGetRepositories() throws Exception
    {
        UserModel disabledUser = dataUser.createRandomTestUser();
        Assert.assertNotNull(cmisApi.authenticateUser(disabledUser).getRepositoryInfo());
        dataUser.usingAdmin().disableUser(disabledUser);
        cmisApi.authenticateUser(disabledUser).getRepositoryInfo();
    }
}
