/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api.tests.client.data;

import org.alfresco.rest.api.model.AssocChild;
import org.alfresco.rest.api.model.AssocTarget;
import org.alfresco.rest.api.model.ClassDefinition;
import org.alfresco.rest.api.model.NodePermissions;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Representation of a node - initially for client tests for Nodes (aka File Folder) API
 *
 * TODO push null check down into AssertUtil (making sure that no other existing tests break)
 *
 * @author janv
 */
public class Node
{
    protected String id;
    protected String name;

    protected Date createdAt;
    protected Date modifiedAt;
    protected UserInfo createdByUser;
    protected UserInfo modifiedByUser;

    // Archived info - specifically for archive (deleted) node - see Trashcan API
    protected Date archivedAt;
    protected UserInfo archivedByUser;

    // Version info - specifically for version node - see Version History API
    protected String versionComment;

    protected Boolean isFolder;
    protected Boolean isFile;
    protected Boolean isLink;
    protected Boolean isLocked;

    protected String parentId;
    protected PathInfo path;
    protected String nodeType;

    protected List<String> aspectNames;

    protected Map<String, Object> properties;

    protected ContentInfo contentInfo;

    protected List<String> allowableOperations;
    protected NodePermissions nodePermissions;
    protected ClassDefinition definition;

    // please note: these are currently only used (optionally) for node create request
    protected String relativePath;
    protected List<AssocChild> secondaryChildren;
    protected List<AssocTarget> targets;

    protected SearchResultEntry search;
    
    protected Boolean isFavorite;

    public Node()
    {
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Date getCreatedAt()
    {
        return createdAt;
    }

    public Date getModifiedAt()
    {
        return modifiedAt;
    }

    public UserInfo getCreatedByUser()
    {
        return createdByUser;
    }

    public void setCreatedByUser(UserInfo createdByUser)
    {
        this.createdByUser = createdByUser;
    }

    public UserInfo getModifiedByUser()
    {
        return modifiedByUser;
    }

    public void setModifiedByUser(UserInfo modifiedByUser)
    {
        this.modifiedByUser = modifiedByUser;
    }

    public Date getArchivedAt()
    {
        return archivedAt;
    }

    public void setArchivedAt(Date archivedAt)
    {
        this.archivedAt = archivedAt;
    }

    public UserInfo getArchivedByUser()
    {
        return archivedByUser;
    }

    public void setArchivedByUser(UserInfo archivedByUser)
    {
        this.archivedByUser = archivedByUser;
    }

    public String getVersionComment()
    {
        return versionComment;
    }

    public void setVersionComment(String versionComment)
    {
        this.versionComment = versionComment;
    }

    public Boolean getIsFolder()
    {
        return isFolder;
    }

    public void setIsFolder(Boolean isFolder)
    {
        this.isFolder = isFolder;
    }

    public Boolean getIsFile()
    {
        return isFile;
    }

    public void setIsFile(Boolean isFile)
    {
        this.isFile = isFile;
    }

    public Boolean getIsLink()
    {
        return isLink;
    }

    public void setIsLink(Boolean link)
    {
        isLink = link;
    }

    public Boolean getIsLocked()
    {
        return isLocked;
    }

    public void setIsLocked(Boolean locked)
    {
        isLocked = locked;
    }

    public String getParentId()
    {
        return parentId;
    }

    public void setParentId(String parentId)
    {
        this.parentId = parentId;
    }

    public PathInfo getPath()
    {
        return path;
    }

    public void setPath(PathInfo path)
    {
        this.path = path;
    }

    public String getNodeType()
    {
        return nodeType;
    }

    public void setNodeType(String nodeType)
    {
        this.nodeType = nodeType;
    }

    public List<String> getAspectNames()
    {
        return aspectNames;
    }

    public void setAspectNames(List<String> aspectNames)
    {
        this.aspectNames = aspectNames;
    }

    public Map<String, Object> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, Object> properties)
    {
        this.properties = properties;
    }

    public void setContent(ContentInfo contentInfo)
    {
        this.contentInfo = contentInfo;
    }

    public ContentInfo getContent()
    {
        return this.contentInfo;
    }

    public List<String> getAllowableOperations()
    {
        return allowableOperations;
    }

    public void setAllowableOperations(List<String> allowableOperations)
    {
        this.allowableOperations = allowableOperations;
    }

    public NodePermissions getPermissions()
    {
        return nodePermissions;
    }

    public void setPermissions(NodePermissions nodePermissions)
    {
        this.nodePermissions = nodePermissions;
    }

    public String getRelativePath()
    {
        return relativePath;
    }

    public void setRelativePath(String relativePath)
    {
        this.relativePath = relativePath;
    }

    public List<AssocChild> getSecondaryChildren()
    {
        return secondaryChildren;
    }

    public void setSecondaryChildren(List<AssocChild> secondaryChildren)
    {
        this.secondaryChildren = secondaryChildren;
    }

    public List<AssocTarget> getTargets()
    {
        return targets;
    }

    public void setTargets(List<AssocTarget> targets)
    {
        this.targets = targets;
    }

    public ClassDefinition getDefinition()
    {
        return definition;
    }

    public void setDefinition(ClassDefinition definition)
    {
        this.definition = definition;
    }

    // note: can be child or peer (latter has assocType only)
    protected Association association;

    public Association getAssociation()
    {
        return association;
    }

    public void setAssociation(Association association)
    {
        this.association = association;
    }

    public SearchResultEntry getSearch()
    {
        return search;
    }

    public void setSearch(SearchResultEntry search)
    {
        this.search = search;
    }
    
    public Boolean getIsFavorite()
    {
        return isFavorite;
    }

    public void setIsFavorite(Boolean isFavorite)
    {
        this.isFavorite = isFavorite;
    }

    public void expected(Object o)
    {
        Node other = (Node) o;

        if (id != null)
        {
            AssertUtil.assertEquals("id", id, other.getId());
        }
        else
        {
            assertNotNull(other.id);
        }

        AssertUtil.assertEquals("parentId", parentId, other.getParentId());
        AssertUtil.assertEquals("name", name, other.getName());
        AssertUtil.assertEquals("nodeType", nodeType, other.getNodeType());

        if (createdAt != null)
        {
            AssertUtil.assertEquals("createdAt", createdAt, other.getCreatedAt());
        }
        else
        {
            assertNotNull(other.createdAt);
        }

        if (createdByUser != null)
        {
            createdByUser.expected(other.getCreatedByUser());
        }
        else
        {
            assertNotNull(other.createdByUser);
        }

        if (modifiedAt != null)
        {
            assertTrue(modifiedAt.before(other.getModifiedAt()) || modifiedAt.equals(other.getModifiedAt()));
        }
        else
        {
            assertNotNull(other.modifiedAt);
        }

        if (modifiedByUser != null)
        {
            modifiedByUser.expected(other.getModifiedByUser());
        }
        else
        {
            assertNotNull(other.modifiedByUser);
        }

        if (aspectNames != null)
        {
            assertNotNull(other.getAspectNames());
            assertEquals("Expected: "+aspectNames+", actual: "+other.getAspectNames(), aspectNames.size(), other.getAspectNames().size());
            for (String aspectName : aspectNames)
            {
                assertTrue(other.getAspectNames().contains(aspectName));
            }
        }
        else
        {
            assertNull(other.getAspectNames());
        }

        if (properties != null)
        {
            assertNotNull(other.getProperties());
            assertEquals(properties.size(), other.getProperties().size());
            for (Map.Entry<String,Object> e : properties.entrySet())
            {
                Object otherObj = other.getProperties().get(e.getKey());
                assertEquals(e.getValue(), otherObj); // TODO fix !
            }
        }
        else
        {
            assertNull(other.getProperties());
        }

        AssertUtil.assertEquals("isFolder", isFolder, other.getIsFolder());
        AssertUtil.assertEquals("isFile", isFile, other.getIsFile());
        AssertUtil.assertEquals("isLink", isLink, other.getIsLink());
        AssertUtil.assertEquals("isLocked", isLocked, other.getIsLocked());

        if (path != null)
        {
            path.expected(other.getPath());
        }
        else
        {
            assertNull(other.getPath());
        }

        if (contentInfo != null)
        {
            contentInfo.expected(other.getContent());
        }
        else
        {
            assertNull(other.getContent());
        }

        if (search != null)
        {
            search.expected(other.getSearch());
        }
        else
        {
            assertNull(other.getSearch());
        }

        if (allowableOperations != null)
        {
            assertNotNull(other.getAllowableOperations());
            assertEquals("Expected: "+allowableOperations+", actual: "+other.getAllowableOperations(), allowableOperations.size(), other.getAllowableOperations().size());
            for (String allowableOperation : allowableOperations)
            {
                assertTrue(other.getAllowableOperations().contains(allowableOperation));
            }
        }
        else
        {
            assertNull(other.getAllowableOperations());
        }

        if (relativePath != null)
        {
            assertEquals(relativePath, other.getRelativePath());
        }
        else
        {
            assertNull(other.getRelativePath());
        }
    }
}