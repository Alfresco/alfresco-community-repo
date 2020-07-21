/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.virtual.ref;

import org.alfresco.repo.virtual.ref.ReferenceParser.Cursor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * Base class for custom protocol hash encoded {@link ReferenceParser}s.
 */
public abstract class ProtocolHashParser implements HashEncodingArtefact
{
    protected abstract Reference parse(Cursor cursor) throws ReferenceParseException;

    private NodeRefHasher nodeRefHasher = NodeRefRadixHasher.RADIX_36_HASHER;

    private PathHasher classpathHasher;

    private PathHasher repositoryPathHasher;

    public ProtocolHashParser(HashStore classpathHashStore)
    {
        this.classpathHasher = new StoredPathHasher(classpathHashStore);
        this.repositoryPathHasher = new StoredPathHasher(classpathHashStore);
    }

    protected Resource parseResource(Cursor cursor) throws ReferenceParseException
    {
        String token = cursor.nextToken();
        if (REPOSITORY_NODEREF_RESOURCE_CODE.equals(token))
        {

            String nodeRefHash = cursor.nextToken();
            String storeHash = nodeRefHash.substring(0,
                                                     2);
            String nodeIdHash = nodeRefHash.substring(2);
            NodeRef nodeRef = nodeRefHasher.lookup(new Pair<String, String>(storeHash,
                                                                            nodeIdHash));
            return new RepositoryResource(new RepositoryNodeRef(nodeRef));
        }
        else if (HASHED_CLASSPATH_RESOUCE_CODE.equals(token))
        {
            String cp = classpathHasher.lookup(new Pair<String, String>(cursor.nextToken(),
                                                                        null));
            return new ClasspathResource(cp);
        }
        else if (MIXED_CLASSPATH_RESOUCE_CODE.equals(token))
        {
            String cp = classpathHasher.lookup(new Pair<String, String>(cursor.nextToken(),
                                                                        cursor.nextToken()));
            return new ClasspathResource(cp);
        }
        else if (CLASSPATH_RESOUCE_CODE.equals(token))
        {
            String cp = classpathHasher.lookup(new Pair<String, String>(null,
                                                                        cursor.nextToken()));
            return new ClasspathResource(cp);
        }
        else if (HASHED_REPOSITORY_PATH_CODE.equals(token))
        {
            String path = repositoryPathHasher.lookup(new Pair<String, String>(cursor.nextToken(),
                                                                               null));

            return new RepositoryResource(new RepositoryPath(path));
        }
        else if (MIXED_REPOSITORY_PATH_CODE.equals(token))
        {
            String path = repositoryPathHasher.lookup(new Pair<String, String>(cursor.nextToken(),
                                                                               cursor.nextToken()));

            return new RepositoryResource(new RepositoryPath(path));
        }
        else if (REPOSITORY_PATH_CODE.equals(token))
        {
            String path = repositoryPathHasher.lookup(new Pair<String, String>(null,
                                                                               cursor.nextToken()));

            return new RepositoryResource(new RepositoryPath(path));
        }
        else
        {
            throw new ReferenceParseException("Unknown resource token " + token + " at " + (cursor.i--));
        }
    }

}
