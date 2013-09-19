package org.alfresco.service.cmr.model;

import java.util.List;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.ChildAssociationRef;

/**
 * Interface to determine which sub-folders to search during deep listing.
 * 
 * @See FileFolderService
 */
@AlfrescoPublicApi
public interface SubFolderFilter
{
    /**
     * Does deep listing enter this subfolder?
     * 
     * @param subfolderRef the association
     * @return return true to enter the sub-folder, false to exclude the subfolder and all its children
     */
    public boolean isEnterSubfolder(ChildAssociationRef subfolderRef);
}
