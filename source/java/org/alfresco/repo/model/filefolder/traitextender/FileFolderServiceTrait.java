/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.repo.model.filefolder.traitextender;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderServiceType;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.traitextender.Trait;
import org.alfresco.util.Pair;

public interface FileFolderServiceTrait extends Trait
{
    FileInfo createFileInfo(NodeRef nodeRef, QName typeQName, boolean isFolder, boolean isHidden,
                Map<QName, Serializable> properties);

    FileFolderServiceType getType(QName typeQName);

    List<FileInfo> list(NodeRef contextNodeRef);

    PagingResults<FileInfo> list(final NodeRef contextNodeRef, boolean files, boolean folders, String pattern,
                Set<QName> ignoreQNames, List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest);

    PagingResults<FileInfo> list(NodeRef contextNodeRef, boolean files, boolean folders, Set<QName> ignoreQNames,
                List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest);

    PagingResults<FileInfo> list(NodeRef rootNodeRef, Set<QName> searchTypeQNames, Set<QName> ignoreAspectQNames,
                List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest);

    List<FileInfo> search(NodeRef contextNodeRef, String namePattern, boolean fileSearch, boolean folderSearch,
                boolean includeSubFolders);

    Pair<Set<QName>, Set<QName>> buildSearchTypesAndIgnoreAspects(boolean files, boolean folders,
                Set<QName> ignoreQNameTypes);

    FileInfo rename(NodeRef sourceNodeRef, String newName) throws FileExistsException, FileNotFoundException;
}
