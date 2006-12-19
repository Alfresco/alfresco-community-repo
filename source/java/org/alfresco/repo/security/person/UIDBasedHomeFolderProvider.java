/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
