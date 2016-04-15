
package org.alfresco.repo.model.filefolder.traitextender;

import java.util.List;
import java.util.Set;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

public interface FileFolderServiceExtension
{
    List<FileInfo> list(NodeRef contextNodeRef);

    PagingResults<FileInfo> list(NodeRef contextNodeRef, boolean files, boolean folders, String pattern,
                Set<QName> ignoreQNames, List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest);

    PagingResults<FileInfo> list(NodeRef contextNodeRef, boolean files, boolean folders, Set<QName> ignoreQNames,
                List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest);

    PagingResults<FileInfo> list(NodeRef rootNodeRef, Set<QName> searchTypeQNames, Set<QName> ignoreAspectQNames,
                List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest);

    List<FileInfo> search(NodeRef contextNodeRef, String namePattern, boolean includeSubFolders);

    List<FileInfo> search(NodeRef contextNodeRef, String namePattern, boolean fileSearch, boolean folderSearch,
                boolean includeSubFolders);

    FileInfo rename(NodeRef sourceNodeRef, String newName) throws FileExistsException, FileNotFoundException;
}
