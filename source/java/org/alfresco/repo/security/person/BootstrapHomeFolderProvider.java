/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
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
