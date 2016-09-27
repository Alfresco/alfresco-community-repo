/**
 *
 */
package org.alfresco.repo.model.filefolder;

import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * @author Roy Wetherall
 */
public class ExtendedFileFolderServiceImpl extends FileFolderServiceImpl
{
    protected RecordService recordService;

    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    @Override
    public FileInfo create(NodeRef parentNodeRef, String name, QName typeQName)
    {
        FileInfo result = null;

        recordService.disablePropertyEditableCheck();
        try
        {
            result = super.create(parentNodeRef, name, typeQName);
        }
        finally
        {
            recordService.enablePropertyEditableCheck();
            recordService.disablePropertyEditableCheck(result.getNodeRef());
        }

        return result;
    }

    @Override
    public FileInfo create(NodeRef parentNodeRef, String name, QName typeQName, QName assocQName)
    {
        FileInfo result = null;

        recordService.disablePropertyEditableCheck();
        try
        {
            result = super.create(parentNodeRef, name, typeQName, assocQName);
        }
        finally
        {
            recordService.enablePropertyEditableCheck();
            recordService.disablePropertyEditableCheck(result.getNodeRef());
        }

        return result;
    }
}
