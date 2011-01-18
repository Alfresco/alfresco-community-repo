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

import org.alfresco.cmis.CMISRepositoryReference;
import org.alfresco.cmis.CMISServices;
import org.alfresco.service.cmr.repository.NodeRef;


/**
 * AVM Path Reference
 * 
 * @author davidc
 */
public class AVMPathReference extends AbstractObjectReference
{
    protected String path;
    protected String[] reference;
    
    /**
     * Construct
     * 
     * @param cmisServices
     * @param repo
     * @param path (note: AVM store relative path)
     */
    public AVMPathReference(CMISServices cmisServices, CMISRepositoryReference repo, String path)
    {
        super(cmisServices, repo);
        String[] splitPath = path.split("/");
        this.reference = new String[1 + splitPath.length];
        String avmStoreName = repo.getStoreRef().getIdentifier();
        reference[0] = avmStoreName;
        this.path = avmStoreName + ":/" + path;
        System.arraycopy(splitPath, 0, reference, 1, splitPath.length);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.reference.ObjectPathReference#getNodeRef()
     */
    public NodeRef getNodeRef()
    {
        return cmisServices.getNode("avmpath", reference);
    }
    
    /**
     * @return  AVM path (eg. avmstorename:/my/path/to/a/file)
     */
    public String getPath()
    {
        return path;
    }
    
    @Override
    public String toString()
    {
        return "AVMPathReference[avmpath=" + path + "]";
    }

}
