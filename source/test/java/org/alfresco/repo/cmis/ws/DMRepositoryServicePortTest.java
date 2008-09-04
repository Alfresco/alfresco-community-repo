package org.alfresco.repo.cmis.ws;


import org.alfresco.repo.cmis.ws.OIDUtils;

/**
 * @see org.alfresco.cmis.ws.RepositoryServicePortDM
 *
 * @author Dmitry Lazurkin
 *
 */
public class DMRepositoryServicePortTest extends BaseServicePortTest
{
    private RepositoryServicePort repositoryServicePort;

    @Override
    protected void onSetUp() throws Exception
    {
        super.onSetUp();

        repositoryServicePort = (RepositoryServicePort) applicationContext.getBean("dmRepositoryService");
    }

    public void testGetRootFolder() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetRootFolder request = new GetRootFolder();
        request.setFilter("*");

        GetRootFolderResponse response = repositoryServicePort.getRootFolder(request);
        FolderObjectType rootFolder = response.getRootFolder();
        assertEquals(rootNodeRef, OIDUtils.OIDtoNodeRef(rootFolder.getObjectID()));
    }

}
