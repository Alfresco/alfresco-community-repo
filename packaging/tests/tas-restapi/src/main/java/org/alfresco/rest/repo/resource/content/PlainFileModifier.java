/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.rest.repo.resource.content;

import java.util.Map;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.repo.resource.general.ContentModifier;
import org.alfresco.rest.repo.resource.general.Modifier;
import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;

public class PlainFileModifier
    extends ContentModifier<FileModel, Modifier.FileModifier>
    implements Modifier.FileModifier
{
    private final Map<String, FileModel> filesCache;

    public PlainFileModifier(DataContent dataContent, RestWrapper restClient, FileModel file, Map<String, FileModel> filesCache)
    {
        super(dataContent, restClient, file);
        this.filesCache = filesCache;
    }

    @Override
    protected FileModifier self()
    {
        return this;
    }

    @Override
    public FileModel get(String id)
    {
        return super.get(id, FileModel::new);
    }

    @Override
    public FileModel copyTo(FolderModel target)
    {
        return super.copyTo(target, FileModel::new);
    }

    @Override
    public void delete()
    {
        filesCache.remove(contentModel.getNodeRef());
        super.delete();
    }
}
