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

import java.util.Map;

import org.alfresco.cmis.CMISObjectReference;
import org.alfresco.cmis.CMISRelationshipReference;
import org.alfresco.cmis.CMISRepositoryReference;
import org.alfresco.cmis.CMISServices;
import org.alfresco.service.cmr.repository.StoreRef;


/**
 * Reference Factory
 * 
 * @author davidc
 */
public class ReferenceFactory
{
    private CMISServices cmisService;

    /**
     * @param cmisService
     */
    public void setCMISService(CMISServices cmisService)
    {
        this.cmisService = cmisService;
    }
    
    /**
     * Create CMIS Repository Reference from URL segments
     * 
     * @param args  url arguments
     * @param templateArgs  url template arguments
     * @return  Repository Reference  (or null, in case of bad url)
     */
    public CMISRepositoryReference createRepoReferenceFromUrl(Map<String, String> args, Map<String, String> templateArgs)
    {
        String store_type = templateArgs.get("store_type");
        String store_id = templateArgs.get("store_id");
        if (store_type != null && store_id != null)
        {
            return new StoreRepositoryReference(cmisService, store_type + ":" + store_id);
        }
        
        String store = templateArgs.get("store");
        if (store != null)
        {
            return new StoreRepositoryReference(cmisService, store);
        }
        
        String avmPath = templateArgs.get("avmpath");
        if (avmPath != null && store_id != null)
        {
            return new StoreRepositoryReference(cmisService, StoreRef.PROTOCOL_AVM + ":" + store_id);
        }
        
        // TODO: repository id
//      String repoId = templateArgs.get("repo");
//        else if (repoId != null)
//        {
//        }
        
        return new DefaultRepositoryReference(cmisService);
    }

    /**
     * Create CMIS Object Reference from URL segments
     * 
     * @param args  url arguments
     * @param templateArgs  url template arguments
     * @return  Repository Reference  (or null, in case of bad url)
     */
    public CMISObjectReference createObjectReferenceFromUrl(Map<String, String> args, Map<String, String> templateArgs)
    {
        // Despite the name of this argument, it is included in the "Object by ID" URL template and actually accepts a
        // value in object ID format (including version label suffix) so should be parsed as an object ID rather than a
        // NodeRef
        String objectId = args.get("noderef");
        if (objectId != null)
        {
            return new ObjectIdReference(cmisService, objectId);
        }
        
        CMISRepositoryReference repo = createRepoReferenceFromUrl(args, templateArgs);
        String id = templateArgs.get("id");
        if (id != null)
        {
            return new NodeIdReference(cmisService, repo, id);
        }
        
        String path = templateArgs.get("path");
        if (path == null)
        {
            path = args.get("path");
        }
        if (path != null)
        {
            return new ObjectPathReference(cmisService, repo, path);
        }
        
        String nodepath = templateArgs.get("nodepath");
        if (nodepath == null)
        {
            nodepath = args.get("nodepath");
        }
        if (nodepath != null)
        {
            return new NodePathReference(cmisService, repo, nodepath);
        }
        
        String avmPath = templateArgs.get("avmpath");
        if (avmPath != null)
        {
            return new AVMPathReference(cmisService, repo, avmPath);
        }
        
        return null;
    }

    /**
     * Create CMIS Relationship Reference from URL segments
     * 
     * @param args  url arguments
     * @param templateArgs  url template arguments
     * @return  Repository Reference  (or null, in case of bad url)
     */
    public CMISRelationshipReference createRelationshipReferenceFromUrl(Map<String, String> args, Map<String, String> templateArgs)
    {
        String assocId = templateArgs.get("assoc_id");
        if (assocId != null)
        {
            return new AssociationIdRelationshipReference(cmisService, assocId);
        }
        return null;
    }
    
}
