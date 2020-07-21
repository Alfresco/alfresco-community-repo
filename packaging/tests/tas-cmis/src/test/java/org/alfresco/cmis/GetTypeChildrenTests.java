package org.alfresco.cmis;


import org.alfresco.utility.exception.DataPreparationException;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 10/6/2016.
 */
public class GetTypeChildrenTests extends CmisTest
{
    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception
    {
        cmisApi.authenticateUser(dataUser.getAdminUser());
    }

    /**
     * Get type children for a valid type id and includePropertyDefinitions set to false
     * (verify that Map<String, PropertyDefinition<?>> is empty)
     */
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.SANITY,
            description = "Verify admin can get type children for BaseTypeId.CMIS_DOCUMENT and includePropertyDefinitions = false")
    @Test(groups = { TestGroup.CMIS, TestGroup.SANITY })
    public void getTypeChildrenWithoutPropertyDefinitions()
    {
        cmisApi.authenticateUser(dataUser.getAdminUser())
            .usingObjectType(BaseTypeId.CMIS_DOCUMENT.value())
                .withoutPropertyDefinitions()
                    .hasChildren("D:srft:facetField").propertyDefinitionIsEmpty();
    }

    /**
     * Get type children for a valid type id and includePropertyDefinitions set to true
     * (verify that Map<String, PropertyDefinition<?>> is not empty)
     */
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.SANITY,
            description = "Verify admin can get type children for valid type id and includePropertyDefinitions = true")
    @Test(groups = { TestGroup.CMIS, TestGroup.SANITY })
    public void getTypeChildrenWithPropertyDefinitions()
    {
        cmisApi.authenticateUser(dataUser.getAdminUser()).usingObjectType(BaseTypeId.CMIS_FOLDER.value())
                .withPropertyDefinitions()
                    .hasChildren("F:pub:DeliveryChannel").propertyDefinitionIsNotEmpty();
        cmisApi.usingObjectType(BaseTypeId.CMIS_FOLDER.value()).withPropertyDefinitions()
            .doesNotHaveChildren("D:srft:facetField");
    }
    
    /**
     * Get invalid type children for a valid type id and includePropertyDefinitions set to true
     * (verify that Map<String, PropertyDefinition<?>> is not empty)
     */
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin can get invalid type children for valid type id")
    @Test(groups = { TestGroup.CMIS, TestGroup.REGRESSION })
    public void getInvalidTypeChildrenForATypeId()
    {
        cmisApi.authenticateUser(dataUser.getAdminUser()).usingObjectType(BaseTypeId.CMIS_FOLDER.value())
            .withPropertyDefinitions()
                .doesNotHaveChildren("F:pub:invalidDeliveryChannelv");
    }
    
    /**
     * Get valid type children for a invalid type id and includePropertyDefinitions set to true
     * (verify that Map<String, PropertyDefinition<?>> is not empty)
     */
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin can get valid type children for invalid type id")
    @Test(groups = { TestGroup.CMIS, TestGroup.REGRESSION }, expectedExceptions = {CmisObjectNotFoundException.class},
            expectedExceptionsMessageRegExp="Type 'cmis:invalidfolder' is unknown!*")
    public void getValidTypeChildrenForInvalidTypeId()
    {
        cmisApi.authenticateUser(dataUser.getAdminUser()).usingObjectType("cmis:invalidfolder")
            .withPropertyDefinitions()
                .hasChildren("F:pub:DeliveryChannel").propertyDefinitionIsNotEmpty();;
    }
    
    /**
     * Deleted user is not authorized to get type children for a valid type id
     * (verify that Map<String, PropertyDefinition<?>> is not empty)
     * @throws DataPreparationException 
     */
    @Bug(id="REPO-4301")
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify deleted user is not authorized to get type children for valid type id")
    @Test(groups = { TestGroup.CMIS, TestGroup.REGRESSION }, expectedExceptions = {CmisUnauthorizedException.class})
    public void getTypeChildrenWithWithDeletedUser() throws DataPreparationException
    {
        UserModel deletedUser = dataUser.createRandomTestUser();
        cmisApi.authenticateUser(deletedUser)
            .usingObjectType(BaseTypeId.CMIS_FOLDER.value())
            .withPropertyDefinitions()
                .hasChildren("F:pub:DeliveryChannel").propertyDefinitionIsNotEmpty();
        dataUser.deleteUser(deletedUser);
        cmisApi.disconnect()
                .usingObjectType(BaseTypeId.CMIS_FOLDER.value()).withPropertyDefinitions().doesNotHaveChildren("D:srft:facetField");
    }

    @Bug(id="REPO-4301")
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify disabled user is not authorized to get type children for valid type id")
    @Test(groups = { TestGroup.CMIS, TestGroup.REGRESSION }, expectedExceptions = {CmisUnauthorizedException.class})
    public void getTypeChildrenWithWithDisabledUser() throws DataPreparationException
    {
        UserModel disabledUser = dataUser.createRandomTestUser();
        cmisApi.authenticateUser(disabledUser)
            .usingObjectType(BaseTypeId.CMIS_FOLDER.value())
            .withPropertyDefinitions()
                .hasChildren("F:pub:DeliveryChannel").propertyDefinitionIsNotEmpty();
        dataUser.usingAdmin().disableUser(disabledUser);
        cmisApi.disconnect()
                .usingObjectType(BaseTypeId.CMIS_FOLDER.value()).withPropertyDefinitions().doesNotHaveChildren("D:srft:facetField");
    }
}
