package org.alfresco.cmis;

import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetTypeDescendantsTests extends CmisTest
{
    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception
    {
        cmisApi.authenticateUser(dataUser.getAdminUser());
    }

    /**
     * Get type descendants for a valid type id and includePropertyDefinitions set to true
     * and depth set to -1 (verify that Map<String, PropertyDefinition<?>> is not empty)
     */
    @Test(groups = { TestGroup.CMIS , TestGroup.REGRESSION })
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
        description = "Verify admin can get type descendantes for valid type id and includePropertyDefinitions = true  and depth = -1")
    public void adminShouldGetTypeDescendantsValidInputCase1()
    {
        cmisApi.usingObjectType(BaseTypeId.CMIS_DOCUMENT.value())
            .withPropertyDefinitions().hasDescendantType(-1, "D:cm:dictionaryModel", "D:trx:transferLock");
    }

    /**
     * Get type descendants for a valid type id and includePropertyDefinitions set to true
     * and depth set to 1 (verify that Map<String, PropertyDefinition<?>> is not empty)
     */
    @Test(groups = { TestGroup.CMIS , TestGroup.REGRESSION })
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
        description = "Verify admin can get type descendantes for valid type id and includePropertyDefinitions = true  and depth = 1")
    public void adminShouldGetTypeDescendantsValidInputCase2()
    {
        cmisApi.usingObjectType(BaseTypeId.CMIS_DOCUMENT.value())
            .withPropertyDefinitions().hasDescendantType(1, "D:cm:dictionaryModel", "D:trx:transferLock");
    }

    /**
     * Get type descendants for a valid type id and includePropertyDefinitions set to false
     * and depth set to 1 (verify that Map<String, PropertyDefinition<?>> is not empty)
     */
    @Test(groups = { TestGroup.CMIS , TestGroup.REGRESSION })
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
        description = "Verify admin can get type descendants for valid type id and includePropertyDefinitions = false  and depth = 1")
    public void adminShouldGetTypeDescendantsValidInputCase3()
    {
        cmisApi.usingObjectType(BaseTypeId.CMIS_DOCUMENT.value())
            .withoutPropertyDefinitions().hasDescendantType(1, "D:cm:dictionaryModel", "D:trx:transferLock");
    }

    /**
     * Get type descendantes for a valid type id and includePropertyDefinitions set to false
     * and depth set to -1 (verify that Map<String, PropertyDefinition<?>> is not empty)
     */
    @Test(groups = { TestGroup.CMIS , TestGroup.REGRESSION })
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
        description = "Verify admin can get type descendantes for valid type id and includePropertyDefinitions = false  and depth = -1")
    public void adminShouldGetTypeDescendantsValidInputCase4()
    {
        cmisApi.usingObjectType(BaseTypeId.CMIS_DOCUMENT.value())
            .withoutPropertyDefinitions().hasDescendantType(-1, "D:cm:dictionaryModel", "D:trx:transferLock");
    }
    
    /**
     * Get type descendantes for an invalid type id and includePropertyDefinitions set to false
     * and depth set to -1 (verify that Map<String, PropertyDefinition<?>> is not empty)
     */
    @Test(groups = { TestGroup.CMIS , TestGroup.REGRESSION }, expectedExceptions = {CmisObjectNotFoundException.class},
            expectedExceptionsMessageRegExp="Type 'cmis:documentfake' is unknown!*")
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION, 
        description = "Verify admin cannot get type descendantes for invalid type id and includePropertyDefinitions = false  and depth = -1")
    public void adminCannotGetTypeDescendantsForInvalidType()
    {
        cmisApi.usingObjectType(BaseTypeId.CMIS_DOCUMENT.value() +"fake")
            .withoutPropertyDefinitions().hasDescendantType(-1, "D:cm:dictionaryModel", "D:trx:transferLock");
    }
    
    /**
     * Get type descendantes for a valid type id and includePropertyDefinitions set to false
     * and incorrect depth set to -2 (verify that Map<String, PropertyDefinition<?>> is not empty)
     */
    @Test(groups = { TestGroup.CMIS , TestGroup.REGRESSION }, expectedExceptions = {CmisInvalidArgumentException.class})
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION, 
        description = "Verify admin can get type descendantes for valid type id and includePropertyDefinitions = false  and incorrect depth = -2")
    public void adminShouldGetTypeDescendantsValidTypeWithIncorrectDepth()
    {
        cmisApi.usingObjectType(BaseTypeId.CMIS_DOCUMENT.value())
            .withoutPropertyDefinitions().hasDescendantType(-2, "D:cm:dictionaryModel", "D:trx:transferLock");
    }
    
    @Test(groups = { TestGroup.CMIS , TestGroup.SANITY })
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.SANITY,
        description = "Verify random user can get type descendantes for valid type id and includePropertyDefinitions = false  and depth = 1")
    public void userGetTypeDescendants() throws Exception
    {
        UserModel user = dataUser.createRandomTestUser();
        cmisApi.authenticateUser(user).usingObjectType(BaseTypeId.CMIS_DOCUMENT.value())
            .withoutPropertyDefinitions().hasDescendantType(1, "D:cm:dictionaryModel", "D:trx:transferLock");
    }

    @Bug(id="REPO-4301")
    @Test(groups = { TestGroup.CMIS , TestGroup.REGRESSION }, expectedExceptions=CmisUnauthorizedException.class)
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION, 
        description = "Verify deleted user cannot get type descendantes for valid type id and includePropertyDefinitions = false  and depth = 1")
    public void deletedUserCannotGetTypeDescendants() throws Exception
    {
        UserModel deletedUser = dataUser.createRandomTestUser();
        cmisApi.authenticateUser(deletedUser).usingObjectType(BaseTypeId.CMIS_DOCUMENT.value())
            .withoutPropertyDefinitions().hasDescendantType(1, "D:cm:dictionaryModel", "D:trx:transferLock");
        dataUser.usingAdmin().deleteUser(deletedUser);
        cmisApi.disconnect().usingObjectType(BaseTypeId.CMIS_DOCUMENT.value())
            .withoutPropertyDefinitions().hasDescendantType(1, "D:cm:dictionaryModel", "D:trx:transferLock");
    }

    @Bug(id="REPO-4301")
    @Test(groups = { TestGroup.CMIS , TestGroup.REGRESSION }, expectedExceptions=CmisUnauthorizedException.class)
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION, 
        description = "Verify disabled user cannot get type descendantes for valid type id and includePropertyDefinitions = false  and depth = 1")
    public void disabledUserCannotGetTypeDescendants() throws Exception
    {
        UserModel disabledUser = dataUser.createRandomTestUser();
        cmisApi.authenticateUser(disabledUser).usingObjectType(BaseTypeId.CMIS_DOCUMENT.value())
            .withoutPropertyDefinitions().hasDescendantType(1, "D:cm:dictionaryModel", "D:trx:transferLock");
        dataUser.usingAdmin().disableUser(disabledUser);
        cmisApi.disconnect().usingObjectType(BaseTypeId.CMIS_DOCUMENT.value())
            .withoutPropertyDefinitions().hasDescendantType(1, "D:cm:dictionaryModel", "D:trx:transferLock");
    }
}
