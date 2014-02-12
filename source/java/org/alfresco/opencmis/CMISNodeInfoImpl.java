/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
import java.util.Collections;
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
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.repo.version.VersionModel;
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

/**
 * CMIS representation of a node.
 * 
 * Tries to avoid getting the node's version history where possible (because it's not very performant).
 * 
 */
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

    public CMISNodeInfoImpl()
    {
    }
    
    public CMISNodeInfoImpl(CMISConnector connector, String objectId)
    {
        this.connector = connector;
        this.objectId = connector.constructObjectId(objectId);

        analyseObjectId();
    }

    public CMISNodeInfoImpl(CMISConnector connector, NodeRef nodeRef, VersionHistory versionHistory)
    {
        this.connector = connector;
        this.nodeRef = nodeRef;
        this.versionHistory = versionHistory;

        analyseNodeRef();
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

    private boolean isCurrentNode()
    {
        return objecVariant != CMISObjectVariant.VERSION;
    }

    protected void analyseVersionNode()
    {
        // check version
        versionHistory = getVersionHistory();
        if (versionHistory == null)
        {
            objecVariant = CMISObjectVariant.CURRENT_VERSION;
            objectId = connector.constructObjectId(nodeRef, CMISConnector.UNVERSIONED_VERSION_LABEL);
            versionLabel = CMISConnector.UNVERSIONED_VERSION_LABEL;
            currentObjectId = objectId;
            hasPWC = connector.getCheckOutCheckInService().isCheckedOut(nodeRef);
        }
        else
        {
            Version headVersion = versionHistory.getHeadVersion();

            versionLabel = (String) connector.getNodeService().getProperty(nodeRef, ContentModel.PROP_VERSION_LABEL);
            objectId = connector.constructObjectId(headVersion.getVersionedNodeRef(), versionLabel);
            currentObjectId = connector.constructObjectId(headVersion.getVersionedNodeRef(), headVersion.getVersionLabel());
            currentNodeId = headVersion.getVersionedNodeRef().toString();

            objecVariant = (headVersion.getVersionLabel().equals(versionLabel) ? CMISObjectVariant.CURRENT_VERSION
                    : CMISObjectVariant.VERSION);
            hasPWC = connector.getCheckOutCheckInService().isCheckedOut(headVersion.getVersionedNodeRef());
        }
    }

    protected void analyseCurrentVersion(NodeRef nodeRef)
    {
        if(connector.getVersionService().isVersioned(nodeRef))
        {
            versionLabel = (String) connector.getNodeService().getProperty(nodeRef, ContentModel.PROP_VERSION_LABEL);
            if(versionLabel == null)
            {
                versionLabel = CMISConnector.UNVERSIONED_VERSION_LABEL;
            }
            objectId = connector.constructObjectId(nodeRef, versionLabel);
            currentObjectId = objectId;
            currentNodeId = nodeRef.toString();
            objecVariant = CMISObjectVariant.CURRENT_VERSION;
            hasPWC = connector.getCheckOutCheckInService().isCheckedOut(nodeRef);
        }
        else
        {
            setUnversioned(nodeRef);
        }
    }
    
    protected void setUnversioned(NodeRef nodeRef)
    {
        objecVariant = CMISObjectVariant.CURRENT_VERSION;
        objectId = connector.constructObjectId(nodeRef, CMISConnector.UNVERSIONED_VERSION_LABEL);
        versionLabel = CMISConnector.UNVERSIONED_VERSION_LABEL;
        currentObjectId = objectId;                    
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

        	if (objectId.startsWith(CMISConnector.ASSOC_ID_PREFIX))
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
        	}
        	else
        	{
        		if(NodeRef.isNodeRef(objectId))
        		{
        			NodeRef tmpNodeRef = new NodeRef(objectId);
        			objectId = connector.constructObjectId(tmpNodeRef, null);
        		}

        		if(!NodeRef.isNodeRef(currentNodeId))
        		{
        			currentNodeId = connector.getRootStoreRef() + "/" + currentNodeId;                	
        		}

        		// nodeRef is a "live" node, the version label identifies the specific version of the node
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
        			if(connector.filter(nodeRef))
        			{
        				objecVariant = CMISObjectVariant.NOT_EXISTING;
        			}
        			else
        			{
        				objecVariant = CMISObjectVariant.PWC;
        			}
        			currentObjectId = connector.createObjectId(checkedOut);
        			currentNodeId = checkedOut.toString();
        			versionLabel = CMISConnector.PWC_VERSION_LABEL;
        			hasPWC = true;
        			return;
        		}

        		if (isFolder())
        		{
        			// folders can't be versioned, so no need to check
        			if(connector.filter(nodeRef))
        			{
        				objecVariant = CMISObjectVariant.NOT_EXISTING;
        			}
        			else
        			{
        				objecVariant = CMISObjectVariant.FOLDER;
        			}
        			return;
        		}

        		if (versionLabel == null)
        		{
        			if (isDocument())
        			{
        				// for a document, absence of a version label implies the current (head) version
        				if(connector.filter(nodeRef))
        				{
        					objecVariant = CMISObjectVariant.NOT_EXISTING;
        				}
        				else
        				{
        					objecVariant = CMISObjectVariant.CURRENT_VERSION;
        				}

        				// Is it un-versioned, or currently versioned?
        				Version currentVersion = connector.getVersionService().getCurrentVersion(nodeRef);
        				if (currentVersion != null)
        				{
        					versionLabel = currentVersion.getVersionLabel();
        					versionHistory = connector.getVersionService().getVersionHistory(nodeRef);
        				}
        				else
        				{
        					versionLabel = CMISConnector.UNVERSIONED_VERSION_LABEL;
        				}

        				objectId = connector.constructObjectId(objectId, versionLabel);
        				currentObjectId = objectId;
        				hasPWC = connector.getCheckOutCheckInService().isCheckedOut(nodeRef);
        			}
        			else
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
	        		else if(connector.filter(nodeRef))
	        		{
	        			objecVariant = CMISObjectVariant.NOT_EXISTING;
	        		}
	        		else
	        		{
	        			objecVariant = CMISObjectVariant.PWC;
	        		}
	        		currentObjectId = connector.createObjectId(nodeRef);
	        		currentNodeId = nodeRef.toString();
	        		hasPWC = true;
	        		nodeRef = pwcNodeRef;
	        		return;
	        	}
	
	        	// check version
	        	if(!connector.getVersionService().isVersioned(nodeRef))
	        	{
	        		// the node isn't versioned
	        		if(connector.filter(nodeRef))
	        		{
	        			objecVariant = CMISObjectVariant.NOT_EXISTING;
	        		}
	        		else if (versionLabel.equals(CMISConnector.UNVERSIONED_VERSION_LABEL))
	        		{
	        			objecVariant = CMISObjectVariant.CURRENT_VERSION;
	        		} else
	        		{
	        			objecVariant = CMISObjectVariant.NOT_EXISTING;
	        		}
	        	}
	        	else
	        	{
	        		// the node is versioned, determine whether the versionLabel refers to the head version or a
	        		// specific non-head version
	        		String headVersionLabel = (String)connector.getNodeService().getProperty(nodeRef, ContentModel.PROP_VERSION_LABEL);
	        		currentObjectId = connector.constructObjectId(currentNodeId, headVersionLabel);

	        		if (versionLabel.equals(headVersionLabel))
	        		{
	        			// the version label refers to the current head version
	        			objecVariant = CMISObjectVariant.CURRENT_VERSION;
	        		}
	        		else
	        		{
	        			// the version label refers to a specific non-head version, find the nodeRef
	        			// of the version node from the version history
	        			versionHistory = connector.getVersionService().getVersionHistory(nodeRef);
	        			if (versionHistory == null)
	        			{
	        				// unexpected null versionHistory, assume not versioned
	        				if (versionLabel.equals(CMISConnector.UNVERSIONED_VERSION_LABEL))
	        				{
	        					objecVariant = CMISObjectVariant.CURRENT_VERSION;
	
	        				}
	        				else
	        				{
	        					objecVariant = CMISObjectVariant.NOT_EXISTING;
	        				}
	        			}
	        			else
	        			{
	        				try
	        				{
	        					version = versionHistory.getVersion(versionLabel);
	        					nodeRef = version.getFrozenStateNodeRef();
	        					objecVariant = CMISObjectVariant.VERSION;
	        				}
	        				catch (VersionDoesNotExistException e)
	        				{
	        					objecVariant = CMISObjectVariant.NOT_EXISTING;
	        				}
	        			}
	        		}
	        	}
	
	        	// check if current node(not specified version) checked out
	        	hasPWC = connector.getCheckOutCheckInService().isCheckedOut(getCurrentNodeNodeRef());
        	}
        }
        catch (AccessDeniedException e)
        {
            objecVariant = CMISObjectVariant.PERMISSION_DENIED;
        }
        // TODO: Somewhere this has not been wrapped correctly
        catch (net.sf.acegisecurity.AccessDeniedException e)
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
        
        if (connector.filter(nodeRef))
        {
            objecVariant = CMISObjectVariant.NOT_EXISTING;
            return;
        }
        
        if (isFolder())
        {
            objecVariant = CMISObjectVariant.FOLDER;
            objectId = connector.constructObjectId(nodeRef, null);
            currentObjectId = objectId;
            return;
        }
        else if (getType() == null)
        {
            objecVariant = CMISObjectVariant.NOT_A_CMIS_OBJECT;
            return;
        }
        
        // check PWC
        if (connector.getCheckOutCheckInService().isWorkingCopy(nodeRef))
        {
            NodeRef checkedOut = connector.getCheckOutCheckInService().getCheckedOut(nodeRef);
            if (checkedOut == null)
            {
                // catch a rare audit case
                checkedOut = nodeRef;
            }

            objecVariant = CMISObjectVariant.PWC;

            objectId = connector.constructObjectId(checkedOut, CMISConnector.PWC_VERSION_LABEL);

            versionLabel = CMISConnector.PWC_VERSION_LABEL;
            currentObjectId = connector.createObjectId(checkedOut);
            currentNodeId = checkedOut.toString();
            hasPWC = true;
            return;
        }

        // check version
        if(connector.getVersionService().isAVersion(nodeRef))
        {
            analyseVersionNode();
        }
        else
        {
            analyseCurrentVersion(nodeRef);
        }

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

        if((objecVariant == CMISObjectVariant.INVALID_ID) || (objecVariant == CMISObjectVariant.NOT_A_CMIS_OBJECT) || (objecVariant == CMISObjectVariant.NOT_EXISTING) || (objecVariant == CMISObjectVariant.PERMISSION_DENIED))
        {
            return;
        }
        
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
        return isCurrentVersion();
    }

    public boolean isLatestMajorVersion()
    {
        if (isLatestMajorVersion == null)
        {
            isLatestMajorVersion = Boolean.FALSE;
            if (!isPWC())
            {
                // MNT-10223. It should not be a version and not a minor version
                Version version = getVersion();
                if (isCurrentNode() && version != null && version.getVersionType() == VersionType.MAJOR)
                {
                    isLatestMajorVersion = Boolean.TRUE;
                }
                else
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
                                // ALF-11116: the current node (in the main store) and the frozen node (in the version store) are both represented as CMISNodeInfos
                                // but are indistinguishable apart from their storeRef (their objectVariant can be the same).
                                isLatestMajorVersion = (nodeRef.getStoreRef().getIdentifier().equals(Version2Model.STORE_ID) || nodeRef.getStoreRef().getIdentifier().equals(VersionModel.STORE_ID)) ?
                                        currentVersion.getFrozenStateNodeRef().equals(nodeRef) : currentVersion.getVersionedNodeRef().equals(nodeRef);
                                break;
                            }
                            currentVersion = versionHistory.getPredecessor(currentVersion);
                        }
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
        if((objecVariant == CMISObjectVariant.INVALID_ID) || (objecVariant == CMISObjectVariant.NOT_A_CMIS_OBJECT) || (objecVariant == CMISObjectVariant.NOT_EXISTING) || (objecVariant == CMISObjectVariant.PERMISSION_DENIED))
        {
            return null;
        }
        
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
        if((objecVariant == CMISObjectVariant.INVALID_ID) || (objecVariant == CMISObjectVariant.NOT_A_CMIS_OBJECT) || (objecVariant == CMISObjectVariant.NOT_EXISTING) || (objecVariant == CMISObjectVariant.PERMISSION_DENIED))
        {
            return null;
        }
        
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
//        if (isPWC())
//        {
//            return nodeRef;
//        } else if (hasPWC())
//        {
//            return connector.getCheckOutCheckInService().getWorkingCopy(getCurrentNodeNodeRef());
//        } else
//        {
            return getCurrentNodeNodeRef();
//        }
    }

    // TODO lock here??
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
    
    public void deleteNode()
    {
        Version version = getVersion();

        if (getVersionHistory().getPredecessor(version) == null)
        {
            connector.getNodeService().deleteNode(nodeRef);
        }
        else
        {
            connector.getVersionService().deleteVersion(nodeRef, version);
        }
    }

    public void deleteVersion()
    {
        Version version = getVersion();
        connector.getVersionService().deleteVersion(nodeRef, version);
    }

    protected Version getVersion()
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
        if((objecVariant == CMISObjectVariant.INVALID_ID) || (objecVariant == CMISObjectVariant.NOT_A_CMIS_OBJECT) || (objecVariant == CMISObjectVariant.NOT_EXISTING) || (objecVariant == CMISObjectVariant.PERMISSION_DENIED))
        {
            return Collections.<CMISNodeInfo>emptyList();
        }
        
        if (parents == null)
        {
            parents = new ArrayList<CMISNodeInfo>();

            NodeRef nodeRefForParent = (isCurrentVersion() ? getCurrentNodeNodeRef() : nodeRef);

            List<ChildAssociationRef> nodeParents = connector.getNodeService().getParentAssocs(nodeRefForParent,
                    ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
            if (nodeParents != null)
            {
                for (ChildAssociationRef parent : nodeParents)
                {
                    if (connector.getType(parent.getParentRef()) instanceof FolderTypeDefintionWrapper)
                    {
                        parents.add(new CMISNodeInfoImpl(connector, parent.getParentRef()));
                    }
                }
            }
        }

        return parents;
    }
}
