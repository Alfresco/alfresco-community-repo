/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.client;

import java.io.InputStream;
import java.math.BigInteger;

import org.alfresco.repo.jscript.ScriptableHashMap;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Policy;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.springframework.extensions.surf.util.InputStreamContent;

public class CMISHelper
{
    /**
     * Creates a properties map.
     */
    public ScriptableHashMap<String, Object> createMap()
    {
        return new ScriptableHashMap<String, Object>();
    }

    /**
     * Creates a ContentStream object.
     */
    public ContentStreamImpl createContentStream(String filename, long length, String mimetype, InputStream stream)
    {
        return new ContentStreamImpl(filename, length < 0 ? null : BigInteger.valueOf(length), mimetype, stream);
    }

    /**
     * Creates a ContentStream object.
     */
    public ContentStreamImpl createContentStream(String filename, InputStreamContent content)
    {
        if (content == null)
        {
            throw new IllegalArgumentException("No content!");
        }

        return createContentStream(filename, content.getSize(), content.getMimetype(), content.getInputStream());
    }

    public boolean isDocument(CmisObject object)
    {
        return (object instanceof Document);
    }

    public boolean isFolder(CmisObject object)
    {
        return (object instanceof Folder);
    }

    public boolean isPolicy(CmisObject object)
    {
        return (object instanceof Policy);
    }

    public boolean isRelationship(CmisObject object)
    {
        return (object instanceof Relationship);
    }
}
