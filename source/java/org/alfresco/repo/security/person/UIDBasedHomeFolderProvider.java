/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.util.FileNameValidator;

/**
 * Create home spaces based on the UID of the user.
 * 
 * If a suitable space is found it is reused, if not it will be made.
 * 
 * @author Andy Hind
 */
public class UIDBasedHomeFolderProvider extends ExistingPathBasedHomeFolderProvider
{
    private String templatePath;

    private NodeRef templateNodeRef;

    public UIDBasedHomeFolderProvider()
    {
        super();
    }

    public void setTemplatePath(String templatePath)
    {
        this.templatePath = templatePath;
    }

    protected HomeSpaceNodeRef getHomeFolder(NodeRef person)
    {
        FileFolderService fileFolderService = getServiceRegistry().getFileFolderService();
        NodeService nodeService = getServiceRegistry().getNodeService();

        NodeRef existingHomeFolder = DefaultTypeConverter.INSTANCE.convert(
                NodeRef.class, nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER));
        if (existingHomeFolder == null)
        {
            String uid = DefaultTypeConverter.INSTANCE.convert(
                    String.class,
                    nodeService.getProperty(person, ContentModel.PROP_USERNAME));
            
            if((uid == null) || (uid.length() == 0))
            {
                throw new PersonException("Can not create a home space when the uid is null or empty");
            }
            
            // ETHREEOH-1612: Convert the username to file- and folder-safe names
            String homeFolderName = FileNameValidator.getValidFileName(uid);
            
            FileInfo fileInfo;

            // Test if it already exists

            NodeRef exising = fileFolderService.searchSimple(getPathNodeRef(), homeFolderName);
            if (exising != null)
            {
                fileInfo = fileFolderService.getFileInfo(exising);
            }
            else
            {
                if (templatePath == null)
                {
                    fileInfo = fileFolderService.create(
                            getPathNodeRef(),
                            homeFolderName,
                            ContentModel.TYPE_FOLDER);
                }
                else
                {
                    try
                    {
                        fileInfo = fileFolderService.copy(
                                getTemplateNodeRef(),
                                getPathNodeRef(),
                                homeFolderName);
                    }
                    catch (FileNotFoundException e)
                    {
                        throw new PersonException("Invalid template to create home space");
                    }
                }
            }
            NodeRef homeFolderNodeRef = fileInfo.getNodeRef();
            return new HomeSpaceNodeRef(homeFolderNodeRef, HomeSpaceNodeRef.Status.CREATED);
        }
        else
        {
            return new HomeSpaceNodeRef(existingHomeFolder, HomeSpaceNodeRef.Status.VALID);
        }
    }

    protected synchronized NodeRef getTemplateNodeRef()
    {
        if (templateNodeRef == null)
        {
            templateNodeRef = resolvePath(templatePath);
        }
        return templateNodeRef;
    }

}
