/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.security.person;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.FileNameValidator;

/**
 * Creates home folders directly under the root path, based on the username of the user.
 * 
 * @author Alan Davis (based on UIDBasedHomeFolderProvider)
 */
public class UsernameHomeFolderProvider extends AbstractHomeFolderProvider2
{
    private String templatePath;

    private NodeRef templateNodeRef;

    public void setTemplatePath(String templatePath)
    {
        this.templatePath = templatePath;
    }

    public synchronized NodeRef getTemplateNodeRef()
    {
        if (templateNodeRef == null && templatePath != null)
        {
            templateNodeRef = getHomeFolderManager().resolvePath(this, templatePath);
        }
        return templateNodeRef;
    }

    public List<String> getHomeFolderPath(NodeRef person)
    {
        List<String> path = new ArrayList<String>(1);
        path.add(FileNameValidator.getValidFileName(
                getHomeFolderManager().getPersonProperty(person, ContentModel.PROP_USERNAME)));
        return path;
    }

    public HomeSpaceNodeRef getHomeFolder(NodeRef person)
    {
        return getHomeFolderManager().getHomeFolder(this, person, false);
    }
}
