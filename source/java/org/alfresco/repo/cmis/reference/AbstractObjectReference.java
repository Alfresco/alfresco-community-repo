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
package org.alfresco.repo.cmis.reference;

import org.alfresco.cmis.CMISObjectReference;
import org.alfresco.cmis.CMISRepositoryReference;
import org.alfresco.cmis.CMISServices;


/**
 * Abstract Object Reference
 * 
 * @author davidc
 */
public abstract class AbstractObjectReference implements CMISObjectReference
{
    protected CMISServices cmisServices;
    protected CMISRepositoryReference repo;
 
    /**
     * Construct
     * 
     * @param cmisServices
     * @param repo
     */
    public AbstractObjectReference(CMISServices cmisServices, CMISRepositoryReference repo)
    {
        this.cmisServices = cmisServices;
        this.repo = repo;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISObjectReference#getRepositoryReference()
     */
    public CMISRepositoryReference getRepositoryReference()
    {
        return repo;
    }
}
