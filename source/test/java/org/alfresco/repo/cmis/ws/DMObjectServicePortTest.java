package org.alfresco.repo.cmis.ws;


import java.math.BigInteger;

import org.alfresco.repo.cmis.ws.OIDUtils;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @see org.alfresco.cmis.ws.ObjectServicePortDM
 *
 * @author Dmitry Lazurkin
 *
 */
public class DMObjectServicePortTest extends BaseServicePortContentTest
{
    private ObjectServicePort objectServicePort;

    @Override
    protected void onSetUp() throws Exception
    {
        super.onSetUp();

        objectServicePort = (ObjectServicePort) applicationContext.getBean("dmObjectService");
    }

    public void testGetExistentFolderProperties() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetProperties request = new GetProperties();
        request.setObjectId(OIDUtils.toOID(L0_FOLDER_0_NODEREF));

        GetPropertiesResponse response = objectServicePort.getProperties(request);
        assertNotNull(response);
        assertEquals(OIDUtils.toOID(rootNodeRef), response.getObject().getParent());
        assertEquals(L0_FOLDER_0, response.getObject().getName());
    }

    public void testGetExistentDocumentPropertiesThis() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetProperties request = new GetProperties();
        request.setObjectId(OIDUtils.toOID(L1_FILE_VERSION_1_0_NODEREF));

        GetPropertiesResponse response = objectServicePort.getProperties(request);
        assertNotNull(response);
        assertEquals(response.getObject().getCheckinComment(), "1.0");
        assertEquals(response.getObject().isIsMajorVersion(), Boolean.TRUE);
        assertEquals(response.getObject().isIsLatestMajorVersion(), Boolean.FALSE);
        assertEquals(response.getObject().isIsLatestVersion(), Boolean.FALSE);
    }

    public void testGetExistentDocumentPropertiesLatestMajor() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetProperties request = new GetProperties();
        request.setReturnVersion(VersionEnum.LATEST_MAJOR);
        request.setObjectId(OIDUtils.toOID(L1_FILE_VERSION_1_0_NODEREF));

        GetPropertiesResponse response = objectServicePort.getProperties(request);
        assertNotNull(response);
        assertEquals(response.getObject().getCheckinComment(), "2.0");
        assertEquals(response.getObject().isIsMajorVersion(), Boolean.TRUE);
        assertEquals(response.getObject().isIsLatestMajorVersion(), Boolean.TRUE);
        assertEquals(response.getObject().isIsLatestVersion(), Boolean.FALSE);
    }

    public void testGetExistentDocumentPropertiesLatest() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetProperties request = new GetProperties();
        request.setReturnVersion(VersionEnum.LATEST);
        request.setObjectId(OIDUtils.toOID(L1_FILE_VERSION_2_0_NODEREF));

        GetPropertiesResponse response = objectServicePort.getProperties(request);
        assertNotNull(response);
        assertEquals(response.getObject().getCheckinComment(), "2.1");
        assertEquals(response.getObject().isIsMajorVersion(), Boolean.FALSE);
        assertEquals(response.getObject().isIsLatestMajorVersion(), Boolean.FALSE);
        assertEquals(response.getObject().isIsLatestVersion(), Boolean.TRUE);
    }

    public void testGetExistentRelationshipProperties() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetProperties request = new GetProperties();
        request.setObjectId(OIDUtils.toOID(L0_FILE_1_TO_L0_FILE_0_ASSOCREF));

        GetPropertiesResponse response = objectServicePort.getProperties(request);
        assertNotNull(response);
        assertEquals(OIDUtils.toOID(L0_FILE_1_TO_L0_FILE_0_ASSOCREF.getSourceRef()), response.getObject().getSourceOID());
        assertEquals(OIDUtils.toOID(L0_FILE_1_TO_L0_FILE_0_ASSOCREF.getTargetRef()), response.getObject().getTargetOID());
        fail("Not implement");
    }

    public void testGetNonExistentObjectProperties() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetProperties request = new GetProperties();
        request.setObjectId(OIDUtils.toOID(L0_NONEXISTENT_NODEREF));
        try
        {
            objectServicePort.getProperties(request);
        }
        catch (ObjectNotFoundException e)
        {
            return;
        }

        fail("Expects exception");
    }

    public void testGetNonExistentRelationshipProperties() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetProperties request = new GetProperties();
        request.setObjectId(OIDUtils.toOID(NONEXISTENT_ASSOCREF));

        try
        {
            objectServicePort.getProperties(request);
        }
        catch (ObjectNotFoundException e)
        {
            return;
        }

        fail("Expects exception");
    }

    public void testGetPropertiesForInvalidOID() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetProperties request = new GetProperties();
        request.setObjectId("invalid OID");

        try
        {
            objectServicePort.getProperties(request);
        }
        catch (InvalidArgumentException e)
        {
            return;
        }

        fail("Expects exception");
    }

    public void testGetPropertiesCheckedout() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());
        NodeRef workingCopyNodeRef = checkOutCheckInService.checkout(L0_FILE_0_NODEREF);

        GetProperties request = new GetProperties();
        request.setObjectId(OIDUtils.toOID(L0_FILE_0_NODEREF));

        GetPropertiesResponse response = objectServicePort.getProperties(request);
        assertNotNull(response);
        assertNull(response.getObject().getCheckinComment());
        assertTrue(response.getObject().isIsMajorVersion());
        assertTrue(response.getObject().isIsLatestMajorVersion());
        assertTrue(response.getObject().isIsLatestVersion());
        assertTrue(response.getObject().isVersionSeriesIsCheckedOut());
        assertEquals(response.getObject().getVersionSeriesCheckedOutBy(), authenticationComponent.getSystemUserName());
        assertEquals(response.getObject().getVersionSeriesCheckedOutOID(), OIDUtils.toOID(workingCopyNodeRef));

        request = new GetProperties();
        request.setObjectId(OIDUtils.toOID(workingCopyNodeRef));

        response = objectServicePort.getProperties(request);
        assertNotNull(response);
        assertNull(response.getObject().getCheckinComment());
        assertFalse(response.getObject().isIsMajorVersion());
        assertFalse(response.getObject().isIsLatestMajorVersion());
        assertFalse(response.getObject().isIsLatestVersion());
        assertTrue(response.getObject().isVersionSeriesIsCheckedOut());
        assertEquals(response.getObject().getVersionSeriesCheckedOutBy(), authenticationComponent.getSystemUserName());
        assertEquals(response.getObject().getVersionSeriesCheckedOutOID(), OIDUtils.toOID(workingCopyNodeRef));
    }

    public void testGetContentStream() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        String documentId = OIDUtils.toOID(L0_FILE_0_NODEREF);
        for (int offset = 0; offset < 10; offset++)
        {
            for (int length = 0; length < 10; length++)
            {
                BigInteger offsetBig = new BigInteger(String.valueOf(offset));
                BigInteger lengthBig = new BigInteger(String.valueOf(length));

                byte[] result = objectServicePort.getContentStream(documentId, offsetBig, lengthBig);

                assertNotNull(result);
                if (result.length > length)
                {
                    fail();
                }
            }

        }

        byte[] result = objectServicePort.getContentStream(documentId, null, null);
        assertNotNull(result);

        try
        {result = objectServicePort.getContentStream("Invalid", null, null);}
        catch (InvalidArgumentException e) {}
        catch (Throwable e){fail();}

        try
        {result = objectServicePort.getContentStream(documentId + "s", null, null);}
        catch (ObjectNotFoundException e) {}
        catch (Throwable e){fail();}

        try
        {result = objectServicePort.getContentStream(OIDUtils.toOID(L0_FOLDER_0_NODEREF), null, null);}
        catch (StreamNotSupportedException e) {}
        catch (Throwable e){fail();}

    }
}
