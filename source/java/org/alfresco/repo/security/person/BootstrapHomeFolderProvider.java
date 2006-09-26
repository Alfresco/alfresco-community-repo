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

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Provider to use in the boostrap process - does nothing
 * 
 * Probably not required as behaviour/policies are disabled during normal import.
 * 
 * @author Andy Hind
 */
public class BootstrapHomeFolderProvider extends AbstractHomeFolderProvider
{

    @Override
    protected HomeSpaceNodeRef getHomeFolder(NodeRef person)
    {
        return new HomeSpaceNodeRef(null, HomeSpaceNodeRef.Status.VALID);
    }

}
