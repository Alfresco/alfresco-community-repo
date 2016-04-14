package org.alfresco.module.org_alfresco_module_rm.model.rma.type;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.mockito.InjectMocks;

/**
 * Unit test for RecordsManagementContainerType
 * @author Ana Bozianu
 * @since 2.4
 */
public class RecordsManagementContainerTypeTest extends BaseUnitTest
{
    /** test object */
    private @InjectMocks RecordsManagementContainerType recordManagementContainerType;

    /**
     * Having the Unfilled Record container and a folder
     * When adding a child association between the folder and the container
     * Then the folder type shouldn't be renamed
     */
    @Test
    public void testAddFolderToRMContainer()
    {
        /* Having a RM container and a folder */
        NodeRef rmContainer = generateRMContainer();
        NodeRef rmFolder = generateFolderNode(false);

        /*
         * When adding a child association between the folder and the container
         */
        ChildAssociationRef childAssoc = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, rmContainer, ContentModel.ASSOC_CONTAINS, rmFolder);
        recordManagementContainerType.onCreateChildAssociation(childAssoc, true);

        /* Then the node type should not be changed to TYPE_RECORD_FOLDER */
        verify(mockedNodeService).setType(rmFolder, TYPE_RECORD_FOLDER);
        verify(mockedRecordFolderService).setupRecordFolder(rmFolder);
    }

    /**
     * Having the Unfilled Record container and a folder having the aspect ASPECT_HIDDEN
     * When adding a child association between the folder and the container
     * Then the folder type shouldn't be renamed
     */
     @Test
    public void testAddHiddenFolderToRMContainer()
    {
        /* Having a RM container and a folder with ASPECT_HIDDEN applied */
        NodeRef rmContainer = generateRMContainer();
        NodeRef rmFolder = generateFolderNode(true);

        /*
         * When adding a child association between the folder and the container
         */
        ChildAssociationRef childAssoc = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, rmContainer, ContentModel.ASSOC_CONTAINS, rmFolder);
        recordManagementContainerType.onCreateChildAssociation(childAssoc, true);

        /* Then the node type should not be changed to TYPE_RECORD_FOLDER */
        verify(mockedNodeService, never()).setType(rmFolder, TYPE_RECORD_FOLDER);
        verify(mockedRecordFolderService, never()).setupRecordFolder(rmFolder);
    }

    /**
     * Generates a record management container
     * @return reference to the generated container
     */
    private NodeRef generateRMContainer()
    {
        NodeRef rmContainer = generateNodeRef();
        when(mockedNodeService.getType(rmContainer)).thenReturn(RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER);
        when(mockedDictionaryService.isSubClass(RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER, TYPE_FILE_PLAN)).thenReturn(false);
        return rmContainer;
    }

    /**
     * Generates a folder node
     * @param hasHiddenAspect does the folder node have the aspect ASPECT_HIDDEN
     * @return reference to the created folder
     */
    private NodeRef generateFolderNode(boolean hasHiddenAspect)
    {
        NodeRef rmFolder = generateNodeRef();
        when(mockedDictionaryService.isSubClass(ContentModel.TYPE_FOLDER, ContentModel.TYPE_FOLDER)).thenReturn(true);
        when(mockedDictionaryService.isSubClass(ContentModel.TYPE_FOLDER, ContentModel.TYPE_SYSTEM_FOLDER)).thenReturn(false);
        when(mockedNodeService.getType(rmFolder)).thenReturn(ContentModel.TYPE_FOLDER);
        when(mockedNodeService.exists(rmFolder)).thenReturn(true);
        when(mockedNodeService.hasAspect(rmFolder, ContentModel.ASPECT_HIDDEN)).thenReturn(hasHiddenAspect);
        when(mockedNodeService.hasAspect(rmFolder, ASPECT_FILE_PLAN_COMPONENT)).thenReturn(false);
        return rmFolder;
    }
}
