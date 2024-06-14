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
package org.alfresco.rest.repo.resource.general;

import java.util.function.Function;

import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;

public abstract class ContentCreator<CONTENT extends ContentModel, SELF extends Creator.ContentCreator<CONTENT, ?>>
    extends ResourceCreator<CONTENT, SELF>
    implements Creator.ContentCreator<CONTENT, SELF>
{
    private static final FolderModel DOCUMENT_LIBRARY = null;

    protected SiteModel site;
    protected final CONTENT contentModel;
    protected FolderModel parent = DOCUMENT_LIBRARY;

    public ContentCreator(CONTENT contentModel)
    {
        super();
        this.contentModel = contentModel;
    }

    protected abstract SELF self();

    @Override
    public SELF withName(String name)
    {
        contentModel.setName(name);
        return self();
    }

    @Override
    public SELF withTitle(String title)
    {
        contentModel.setTitle(title);
        return self();
    }

    @Override
    public SELF withDescription(String description)
    {
        contentModel.setDescription(description);
        return self();
    }

    @Override
    public <FOLDER extends FolderModel> SELF underFolder(FOLDER parent)
    {
        this.parent = parent;
        return self();
    }

    @Override
    public <SITE extends SiteModel> SELF withinSite(SITE site)
    {
        this.site = site;
        return self();
    }

    protected CONTENT create(DataContent dataContent, Function<CONTENT, CONTENT> creator)
    {
        if (site != null)
        {
            dataContent.usingSite(site);
        }
        if (parent != null)
        {
            dataContent.usingResource(parent);
        }
        if (user != null)
        {
            dataContent.usingUser(user);
        }

        return creator.apply(contentModel);
    }
}
