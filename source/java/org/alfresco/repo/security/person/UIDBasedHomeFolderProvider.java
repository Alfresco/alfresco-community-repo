/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.security.person;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;

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
        NodeRef existingHomeFolder = DefaultTypeConverter.INSTANCE.convert(NodeRef.class, getServiceRegistry()
                .getNodeService().getProperty(person, ContentModel.PROP_HOMEFOLDER));
        if (existingHomeFolder == null)
        {
            String uid = DefaultTypeConverter.INSTANCE.convert(String.class, getServiceRegistry().getNodeService()
                    .getProperty(person, ContentModel.PROP_USERNAME));
            
            if((uid == null) || (uid.length() == 0))
            {
                throw new PersonException("Can not create a home space when the uid is null or empty");
            }
            
            FileInfo fileInfo;

            // Test if it already exists

            NodeRef exising = getServiceRegistry().getFileFolderService().searchSimple(getPathNodeRef(), uid);
            if (exising != null)
            {
                fileInfo = getServiceRegistry().getFileFolderService().getFileInfo(exising);
            }
            else
            {

                if (templatePath == null)
                {
                    fileInfo = getServiceRegistry().getFileFolderService().create(getPathNodeRef(), uid,
                            ContentModel.TYPE_FOLDER);

                }
                else
                {
                    try
                    {
                        fileInfo = getServiceRegistry().getFileFolderService().copy(getTemplateNodeRef(),
                                getPathNodeRef(), uid);
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
