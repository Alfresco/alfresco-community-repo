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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;

/**
 * Set a home space from a simple path.
 * 
 * @author Andy Hind
 */
public class ExistingPathBasedHomeFolderProvider extends AbstractHomeFolderProvider
{

    public ExistingPathBasedHomeFolderProvider()
    {
        super();
    }

    protected HomeSpaceNodeRef getHomeFolder(NodeRef person)
    {
        NodeRef existingHomeFolder = DefaultTypeConverter.INSTANCE.convert(NodeRef.class, getServiceRegistry().getNodeService().getProperty(
                person, ContentModel.PROP_HOMEFOLDER));
        if (existingHomeFolder == null)
        {
            return new HomeSpaceNodeRef(getPathNodeRef(), HomeSpaceNodeRef.Status.REFERENCED);
        }
        else
        {
            return new HomeSpaceNodeRef(existingHomeFolder, HomeSpaceNodeRef.Status.VALID);
        }
    }

}
