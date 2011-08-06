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
package org.alfresco.opencmis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.opencmis.dictionary.CMISObjectVariant;
import org.alfresco.opencmis.dictionary.DocumentTypeDefinitionWrapper;
import org.alfresco.opencmis.dictionary.FolderTypeDefintionWrapper;
import org.alfresco.opencmis.dictionary.RelationshipTypeDefintionWrapper;
import org.alfresco.opencmis.dictionary.TypeDefinitionWrapper;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.ChildAssocElement;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionDoesNotExistException;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;

public class CMISNodeInfoImpl implements CMISNodeInfo
{
    private static final GregorianCalendar DUMMY_DATE = new GregorianCalendar(2010, 4, 1);

    private CMISConnector connector;
    private String objectId; // CMIS object id
    private String currentObjectId; // CMIS object id of the latest version
    private String currentNodeId; // node ref id of the latest version
    private CMISObjectVariant objecVariant; // object variant
    private NodeRef nodeRef; // object node ref
    private String versionLabel; // version label
    private AssociationRef associationRef; // association ref
    private TypeDefinitionWrapper type; // CMIS type
    private String name;
    private boolean hasPWC;
    private Boolean isRootFolder;
    private String cmisPath;
    private VersionHistory versionHistory;
    private Version version;
    private Boolean isLatestMajorVersion;
    private Map<String, Serializable> properties;
    private List<CMISNodeInfo> parents;

    public CMISNodeInfoImpl(CMISConnector connector, String objectId)
    {
        this.connector = connector;
        this.objectId = objectId;

        analyseObjectId();
    }

    public CMISNodeInfoImpl(CMISConnector connector, NodeRef nodeRef)
    {
        this.connector = connector;
        this.nodeRef = nodeRef;

        analyseNodeRef();
    }

    public CMISNodeInfoImpl(CMISConnector connector, AssociationRef associationRef)
    {
        this.connector = connector;
        this.associationRef = associationRef;

        analyseAssociationRef();
    }

    protected void analyseObjectId()
    {
        currentNodeId = objectId;
        currentObjectId = objectId;
        versionLabel = null;
        nodeRef = null;
        hasPWC = false;

        if (objectId == null)
        {
            objecVariant = CMISObjectVariant.INVALID_ID;
            return;
        }

        try
        {
            // is it a version?
            int sepIndex = objectId.lastIndexOf(CMISConnector.ID_SEPERATOR);
            if (sepIndex > -1)
            {
                currentNodeId = objectId.substring(0, sepIndex);
                versionLabel = objectId.substring(sepIndex + 1);
            }

            if (NodeRef.isNodeRef(currentNodeId))
            {
                nodeRef = new NodeRef(currentNodeId);

                // check for existence
                if (!connector.getNodeService().exists(nodeRef))
                {
                    objecVariant = CMISObjectVariant.NOT_EXISTING;
                    return;
                }

                // check PWC
                if (connector.getCheckOutCheckInService().isWorkingCopy(nodeRef))
                {
                    NodeRef checkedOut = connector.getCheckOutCheckInService().getCheckedOut(nodeRef);
                    objecVariant = CMISObjectVariant.PWC;
                    currentObjectId = connector.createObjectId(checkedOut);
                    currentNodeId = checkedOut.toString();
                    versionLabel = CMISConnector.PWC_VERSION_LABEL;
                    hasPWC = true;
                    return;
                }

                if (versionLabel == null)
                {
                    if (isFolder())
                    {
                        objecVariant = CMISObjectVariant.FOLDER;
                    } else if (isDocument())
                    {
                        objecVariant = CMISObjectVariant.CURRENT_VERSION;
                        objectId = currentNodeId + CMISConnector.ID_SEPERATOR + CMISConnector.UNVERSIONED_VERSION_LABEL;
                        versionLabel = CMISConnector.UNVERSIONED_VERSION_LABEL;
                        currentObjectId = objectId;
                        hasPWC = (connector.getLockService().getLockType(nodeRef) == LockType.READ_ONLY_LOCK);
                    } else
                    {
                        objecVariant = CMISObjectVariant.NOT_A_CMIS_OBJECT;
                    }
                    return;
                }

                // check if it has PWC label
                if (versionLabel.equals(CMISConnector.PWC_VERSION_LABEL))
                {
                    NodeRef pwcNodeRef = connector.getCheckOutCheckInService().getWorkingCopy(nodeRef);
                    if (pwcNodeRef == null)
                    {
                        objecVariant = CMISObjectVariant.NOT_EXISTING;
                        return;
                    }

                    objecVariant = CMISObjectVariant.PWC;
                    currentObjectId = connector.createObjectId(nodeRef);
                    currentNodeId = nodeRef.toString();
                    hasPWC = true;
                    nodeRef = pwcNodeRef;
                    return;
                }

                // check version
                versionHistory = connector.getVersionService().getVersionHistory(nodeRef);
                if (versionHistory == null)
                {
                    if (versionLabel.equals(CMISConnector.UNVERSIONED_VERSION_LABEL))
                    {
                        objecVariant = CMISObjectVariant.CURRENT_VERSION;

                    } else
                    {
                        objecVariant = CMISObjectVariant.NOT_EXISTING;
                    }

                    return;
                }

                try
                {
                    currentObjectId = currentNodeId + CMISConnector.ID_SEPERATOR
                            + versionHistory.getHeadVersion().getVersionLabel();

                    version = versionHistory.getVersion(versionLabel);

                    if (versionLabel.equals(versionHistory.getHeadVersion().getVersionLabel()))
                    {
                        objecVariant = CMISObjectVariant.CURRENT_VERSION;
                    } else
                    {
                        nodeRef = version.getFrozenStateNodeRef();
                        objecVariant = CMISObjectVariant.VERSION;
                    }
                } catch (VersionDoesNotExistException e)
                {
                    objecVariant = CMISObjectVariant.NOT_EXISTING;
                }

                // check if checked out
                hasPWC = connector.getCheckOutCheckInService().isCheckedOut(getCurrentNodeNodeRef());
            } else if (objectId.startsWith(CMISConnector.ASSOC_ID_PREFIX))
            {
                // check the association id
                Long assocId = null;
                try
                {
                    assocId = new Long(objectId.substring(CMISConnector.ASSOC_ID_PREFIX.length()));
                } catch (NumberFormatException nfe)
                {
                    objecVariant = CMISObjectVariant.INVALID_ID;
                    return;
                }

                // check the association
                associationRef = connector.getNodeService().getAssoc(assocId);
                if (associationRef == null)
                {
                    objecVariant = CMISObjectVariant.NOT_EXISTING;
                } else
                {
                    objecVariant = CMISObjectVariant.ASSOC;
                }
            } else
            {
                objecVariant = CMISObjectVariant.INVALID_ID;
            }
        } catch (AccessDeniedException e)
        {
            objecVariant = CMISObjectVariant.PERMISSION_DENIED;
        }
    }

    protected void analyseNodeRef()
    {
        objectId = null;
        currentNodeId = nodeRef.toString();
        currentObjectId = null;
        versionLabel = null;
        hasPWC = false;

        // check for existence
        if (!connector.getNodeService().exists(nodeRef))
        {
            objecVariant = CMISObjectVariant.NOT_EXISTING;
            return;
        }

        if (isFolder())
        {
            objecVariant = CMISObjectVariant.FOLDER;
            objectId = nodeRef.toString();
            currentObjectId = objectId;
            return;
        } else if (getType() == null)
        {
            objecVariant = CMISObjectVariant.NOT_A_CMIS_OBJECT;
            return;
        }

        // check PWC
        if (connector.getCheckOutCheckInService().isWorkingCopy(nodeRef))
        {
            NodeRef checkedOut = connector.getCheckOutCheckInService().getCheckedOut(nodeRef);
            objecVariant = CMISObjectVariant.PWC;
            objectId = checkedOut.toString() + CMISConnector.ID_SEPERATOR + CMISConnector.PWC_VERSION_LABEL;
            versionLabel = CMISConnector.PWC_VERSION_LABEL;
            currentObjectId = connector.createObjectId(checkedOut);
            currentNodeId = checkedOut.toString();
            hasPWC = true;
            return;
        }

        // check version
        versionHistory = connector.getVersionService().getVersionHistory(nodeRef);
        if (versionHistory == null)
        {
            objecVariant = CMISObjectVariant.CURRENT_VERSION;
            objectId = nodeRef.toString() + CMISConnector.ID_SEPERATOR + CMISConnector.UNVERSIONED_VERSION_LABEL;
            versionLabel = CMISConnector.UNVERSIONED_VERSION_LABEL;
            currentObjectId = objectId;
        } else
        {
            Version headVersion = versionHistory.getHeadVersion();

            versionLabel = (String) connector.getNodeService().getProperty(nodeRef, ContentModel.PROP_VERSION_LABEL);
            objectId = headVersion.getVersionedNodeRef().toString() + CMISConnector.ID_SEPERATOR + versionLabel;
            currentObjectId = headVersion.getVersionedNodeRef().toString() + CMISConnector.ID_SEPERATOR
                    + headVersion.getVersionLabel();
            currentNodeId = headVersion.getVersionedNodeRef().toString();

            objecVariant = (headVersion.getVersionLabel().equals(versionLabel) ? CMISObjectVariant.CURRENT_VERSION
                    : CMISObjectVariant.VERSION);
        }

        hasPWC = connector.getCheckOutCheckInService().isCheckedOut(getCurrentNodeNodeRef());
    }

    protected void analyseAssociationRef()
    {
        objectId = null;
        currentNodeId = null;
        currentObjectId = null;
        versionLabel = null;
        hasPWC = false;

        if (associationRef == null)
        {
            objecVariant = CMISObjectVariant.NOT_EXISTING;
            return;
        }

        objecVariant = CMISObjectVariant.ASSOC;
        objectId = CMISConnector.ASSOC_ID_PREFIX + associationRef.getId();
    }

    private void determineType()
    {
        type = null;

        if (nodeRef != null)
        {
            QName typeQName = connector.getNodeService().getType(nodeRef);
            type = connector.getOpenCMISDictionaryService().findNodeType(typeQName);
        } else if (associationRef != null)
        {
            QName typeQName = associationRef.getTypeQName();
            type = connector.getOpenCMISDictionaryService().findAssocType(typeQName);
        }
    }

    public String getObjectId()
    {
        return objectId;
    }

    public CMISObjectVariant getObjectVariant()
    {
        return objecVariant;
    }

    public boolean isVariant(CMISObjectVariant var)
    {
        return objecVariant == var;
    }

    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    public String getCurrentNodeId()
    {
        return currentNodeId;
    }

    public NodeRef getCurrentNodeNodeRef()
    {
        return new NodeRef(currentNodeId);
    }

    public String getCurrentObjectId()
    {
        return currentObjectId;
    }

    public boolean isCurrentVersion()
    {
        return objecVariant == CMISObjectVariant.CURRENT_VERSION;
    }

    public boolean isPWC()
    {
        return objecVariant == CMISObjectVariant.PWC;
    }

    public boolean hasPWC()
    {
        return hasPWC;
    }

    public boolean isVersion()
    {
        return objecVariant == CMISObjectVariant.VERSION;
    }

    public boolean isLatestVersion()
    {
        return isPWC() || (isCurrentVersion() && !hasPWC());
    }

    public boolean isLatestMajorVersion()
    {
        if (isLatestMajorVersion == null)
        {
            isLatestMajorVersion = Boolean.FALSE;
            if (!isPWC())
            {
                VersionHistory versionHistory = getVersionHistory();
                if (versionHistory == null)
                {
                    isLatestMajorVersion = Boolean.TRUE;
                } else
                {
                    Version currentVersion = versionHistory.getHeadVersion();
                    while (currentVersion != null)
                    {
                        if (currentVersion.getVersionType() == VersionType.MAJOR)
                        {
                            isLatestMajorVersion = currentVersion.getFrozenStateNodeRef().equals(nodeRef);
                            break;
                        }
                        currentVersion = versionHistory.getPredecessor(currentVersion);
                    }
                }
            }
        }

        return isLatestMajorVersion.booleanValue();
    }

    public boolean isMajorVersion()
    {
        if (isPWC())
        {
            return false;
        }
        if (CMISConnector.UNVERSIONED_VERSION_LABEL.equals(versionLabel))
        {
            return true;
        }

        Version version = getVersion();
        if (version == null)
        {
            return true;
        }

        return version.getVersionType() == VersionType.MAJOR;
    }

    public String getVersionLabel()
    {
        return versionLabel;
    }

    public String getCheckinComment()
    {
        if (!isDocument() || isPWC())
        {
            return null;
        }

        Version version = getVersion();
        if (version != null)
        {
            return getVersion().getDescription();
        }

        return null;
    }

    public AssociationRef getAssociationRef()
    {
        return associationRef;
    }

    public TypeDefinitionWrapper getType()
    {
        if (type == null)
        {
            determineType();
        }

        return type;
    }

    public boolean isFolder()
    {
        return getType() instanceof FolderTypeDefintionWrapper;
    }

    public boolean isRootFolder()
    {
        if (isRootFolder == null)
        {
            isRootFolder = isFolder() && connector.getRootNodeRef().equals(nodeRef);
        }

        return isRootFolder.booleanValue();
    }

    public boolean isDocument()
    {
        return getType() instanceof DocumentTypeDefinitionWrapper;
    }

    public boolean isRelationship()
    {
        return getType() instanceof RelationshipTypeDefintionWrapper;
    }

    public String getName()
    {
        if (name == null)
        {
            if (isRelationship())
            {
                name = associationRef.toString();
            } else
            {
                Object nameObj = connector.getNodeService().getProperty(nodeRef, ContentModel.PROP_NAME);
                name = (nameObj instanceof String ? (String) nameObj : "");
            }
        }

        return name;
    }

    public String getPath()
    {
        if (cmisPath == null)
        {
            StringBuilder displayPath = new StringBuilder(64);

            Path path = connector.getNodeService().getPath(nodeRef);
            NodeRef rootNode = connector.getRootNodeRef();
            int i = 0;
            while (i < path.size())
            {
                Path.Element element = path.get(i);
                if (element instanceof ChildAssocElement)
                {
                    ChildAssociationRef assocRef = ((ChildAssocElement) element).getRef();
                    NodeRef node = assocRef.getChildRef();
                    if (node.equals(rootNode))
                    {
                        break;
                    }
                }
                i++;
            }

            if (i == path.size())
            {
                // TODO:
                // throw new AlfrescoRuntimeException("Path " + path +
                // " not in CMIS root node scope");
            }

            if (path.size() - i == 1)
            {
                // render root path
                displayPath.append("/");
            } else
            {
                // render CMIS scoped path
                i++;
                while (i < path.size() - 1)
                {
                    Path.Element element = path.get(i);
                    if (element instanceof ChildAssocElement)
                    {
                        ChildAssociationRef assocRef = ((ChildAssocElement) element).getRef();
                        NodeRef node = assocRef.getChildRef();
                        displayPath.append("/");
                        displayPath.append(connector.getNodeService().getProperty(node, ContentModel.PROP_NAME));
                    }
                    i++;
                }
                displayPath.append("/");
                displayPath.append(getName());
            }

            cmisPath = displayPath.toString();
        }

        return cmisPath;
    }

    public Serializable getCreationDate()
    {
        if (isDocument())
        {
            if (isCurrentVersion() || isPWC())
            {
                return connector.getNodeService().getProperty(nodeRef, ContentModel.PROP_CREATED);
            } else
            {
                return getVersion().getVersionProperty(VersionBaseModel.PROP_CREATED_DATE);
            }
        } else if (isFolder())
        {
            return connector.getNodeService().getProperty(nodeRef, ContentModel.PROP_CREATED);
        } else
        {
            return DUMMY_DATE;
        }
    }

    public Serializable getModificationDate()
    {
        if (isDocument())
        {
            if (isCurrentVersion() || isPWC())
            {
                return connector.getNodeService().getProperty(nodeRef, ContentModel.PROP_MODIFIED);
            } else
            {
                return getVersion().getVersionProperty(ContentModel.PROP_MODIFIED.getLocalName());
            }
        } else if (isFolder())
        {
            return connector.getNodeService().getProperty(nodeRef, ContentModel.PROP_MODIFIED);
        } else
        {
            return DUMMY_DATE;
        }
    }

    public NodeRef getLatestVersionNodeRef(boolean major)
    {
        if (!major)
        {
            return getLatestNonMajorVersionNodeRef();
        }

        VersionHistory versionHistory = getVersionHistory();

        // if there is no history, return the current version
        if (versionHistory == null)
        {
            // there are no versions
            return getLatestNonMajorVersionNodeRef();
        }

        // find the latest major version
        for (Version version : versionHistory.getAllVersions())
        {
            if (version.getVersionType() == VersionType.MAJOR)
            {
                return version.getFrozenStateNodeRef();
            }
        }

        throw new CmisObjectNotFoundException("There is no major version!");
    }

    private NodeRef getLatestNonMajorVersionNodeRef()
    {
        if (isPWC())
        {
            return nodeRef;
        } else if (hasPWC())
        {
            return connector.getCheckOutCheckInService().getWorkingCopy(getCurrentNodeNodeRef());
        } else
        {
            return getCurrentNodeNodeRef();
        }
    }

    public VersionHistory getVersionHistory()
    {
        if (versionHistory == null && isDocument())
        {
            try
            {
                versionHistory = connector.getVersionService().getVersionHistory(nodeRef);
            } catch (Exception e)
            {
            }
        }

        return versionHistory;
    }

    public Version getVersion()
    {
        if (version == null && isDocument())
        {
            try
            {
                VersionHistory versionHistory = getVersionHistory();
                version = versionHistory.getVersion(versionLabel);
            } catch (Exception e)
            {
            }
        }

        return version;
    }

    public void checkIfUseful(String what)
    {
        switch (objecVariant)
        {
        case INVALID_ID:
            throw new CmisInvalidArgumentException(what + " id is invalid: " + objectId);
        case NOT_EXISTING:
            throw new CmisObjectNotFoundException(what + " not found: " + objectId);
        case NOT_A_CMIS_OBJECT:
            throw new CmisObjectNotFoundException(what + " is not a CMIS object: " + objectId);
        case PERMISSION_DENIED:
            throw new CmisPermissionDeniedException("Permission denied!");
        }
    }

    public void checkIfFolder(String what)
    {
        checkIfUseful(what);
        if (objecVariant != CMISObjectVariant.FOLDER)
        {
            throw new CmisInvalidArgumentException(what + " is not a folder!");
        }
    }

    @Override
    public Serializable getPropertyValue(String id)
    {
        if (properties == null)
        {
            return null;
        }

        return properties.get(id);
    }

    @Override
    public boolean containsPropertyValue(String id)
    {
        if (properties == null)
        {
            return false;
        }

        return properties.containsKey(id);
    }

    @Override
    public void putPropertyValue(String id, Serializable value)
    {
        if (properties == null)
        {
            properties = new HashMap<String, Serializable>();
        }

        properties.put(id, value);
    }

    @Override
    public String toString()
    {
        return getObjectId() + " (" + getNodeRef() + ")";
    }

    @Override
    public List<CMISNodeInfo> getParents()
    {
        if (parents == null)
        {
            parents = new ArrayList<CMISNodeInfo>();

            List<ChildAssociationRef> nodeParents = connector.getNodeService().getParentAssocs(nodeRef,
                    ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
            if (nodeParents != null)
            {
                for (ChildAssociationRef parent : nodeParents)
                {
                    parents.add(new CMISNodeInfoImpl(connector, parent.getParentRef()));
                }
            }
        }

        return parents;
    }
}
