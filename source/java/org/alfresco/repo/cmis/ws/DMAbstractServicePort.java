/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.cmis.ws;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConverter;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Michael Shavnev
 * @author Dmitry Lazurkin
 */
public class DMAbstractServicePort
{

    private static final TypeConverter TYPE_CONVERTER = DefaultTypeConverter.INSTANCE;

    private DatatypeFactory _datatypeFactory;

    protected NodeService nodeService;
    protected PersonService personService;
    protected SearchService searchService;
    protected NamespaceService namespaceService;
    protected DictionaryService dictionaryService;
    protected VersionService versionService;
    protected CheckOutCheckInService checkOutCheckInService;
    protected FileFolderService fileFolderService;
    protected AuthenticationService authenticationService;

    private DatatypeFactory getDatatypeFactory() throws RuntimeException
    {
        if (_datatypeFactory == null)
        {
            try
            {
                _datatypeFactory = DatatypeFactory.newInstance();
            }
            catch (DatatypeConfigurationException e)
            {
                // TODO: error code
                throw new RuntimeException(e.getMessage());
            }
        }
        return _datatypeFactory;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setVersionService(VersionService versionService)
    {
        this.versionService = versionService;
    }

    public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService)
    {
        this.checkOutCheckInService = checkOutCheckInService;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    /**
     * Sets properties for ObjectTypeBase object
     *
     * @param nodeRef Node reference
     * @param target ObjectTypeBase object for setting
     * @param propertyFilter filter for properties
     * @return ObjectTypeBase object
     */
    public ObjectTypeBase setObjectTypeBaseProperties(NodeRef nodeRef, ObjectTypeBase target, PropertyFilter propertyFilter) throws RuntimeException
    {
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

        target.setObjectID(OIDUtils.toOID(nodeRef));

        QName typeQName = nodeService.getType(nodeRef);
        target.setObjectTypeID(typeQName.toString());

        if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_FOLDER))
        {
            target.setBaseObjectType(ContentModel.TYPE_FOLDER.toString());
        }
        else
        {
            target.setBaseObjectType(ContentModel.TYPE_CONTENT.toString());
        }

        if (propertyFilter.allow(CmisProperty.CREATED_BY))
        {
            target.setCreatedBy(TYPE_CONVERTER.convert(String.class, properties.get(ContentModel.PROP_AUTHOR)));
        }

        if (propertyFilter.allow(CmisProperty.CREATION_DATE))
        {
            target.setCreationDate(convert(TYPE_CONVERTER.convert(Date.class, properties.get(ContentModel.PROP_CREATED))));
        }

        if (propertyFilter.allow(CmisProperty.LAST_MODIFIED_BY))
        {
            target.setLastModifiedBy(TYPE_CONVERTER.convert(String.class, properties.get(ContentModel.PROP_MODIFIER)));
        }

        Date modificationDate = TYPE_CONVERTER.convert(Date.class, properties.get(ContentModel.PROP_MODIFIED));

        if (propertyFilter.allow(CmisProperty.LAST_MODIFICATION_DATE))
        {
            target.setLastModificationDate(convert(modificationDate));
        }

        if (propertyFilter.allow(CmisProperty.CHANGE_TOKEN))
        {
            target.setChangeToken(String.valueOf(modificationDate.getTime()));
        }

        return target;
    }

    /**
     * Sets properties for FolderObjectType object
     *
     * @param nodeRef Node reference
     * @param target FolderObjectType object for setting
     * @param propertyFilter filter for properties
     * @return FolderObjectType object
     */
    public FolderObjectType setFolderObjectTypeProperties(NodeRef folderNodeRef, FolderObjectType target, PropertyFilter propertyFilter) throws RuntimeException
    {
        setObjectTypeBaseProperties(folderNodeRef, target, propertyFilter);

        if (propertyFilter.allow(CmisProperty.NAME))
        {
            target.setName((String) nodeService.getProperty(folderNodeRef, ContentModel.PROP_NAME));
        }

        if (propertyFilter.allow(CmisProperty.PARENT))
        {
            target.setParent(OIDUtils.toOID(nodeService.getPrimaryParent(folderNodeRef).getParentRef()));
        }

        if (propertyFilter.allow(CmisProperty.ALLOWED_CHILD_OBJECT_TYPES))
        {
            // TODO: not set this property
        }

        return target;
    }

    /**
     * Sets properties for DocumentOrFolderObjectType object
     *
     * @param nodeRef Node reference
     * @param target DocumentOrFolderObjectType object for setting
     * @param propertyFilter filter for properties
     * @return DocumentOrFolderObjectType object
     */
    public DocumentOrFolderObjectType setFolderObjectTypeProperties(NodeRef folderNodeRef, DocumentOrFolderObjectType target, PropertyFilter propertyFilter) throws RuntimeException
    {
        setObjectTypeBaseProperties(folderNodeRef, target, propertyFilter);

        if (propertyFilter.allow(CmisProperty.NAME))
        {
            target.setName((String) nodeService.getProperty(folderNodeRef, ContentModel.PROP_NAME));
        }

        if (propertyFilter.allow(CmisProperty.PARENT))
        {
            target.setParent(OIDUtils.toOID(nodeService.getPrimaryParent(folderNodeRef).getParentRef()));
        }

        if (propertyFilter.allow(CmisProperty.ALLOWED_CHILD_OBJECT_TYPES))
        {
            // TODO: not set this property
        }

        return target;
    }

    /**
     * Sets properties for DocumentObjectType object
     *
     * @param nodeRef Node reference
     * @param target DocumentObjectType object for setting
     * @param propertyFilter filter for properties
     * @return DocumentObjectType object
     */
    public DocumentObjectType setDocumentObjectTypeProperties(NodeRef documentNodeRef, DocumentObjectType target, PropertyFilter propertyFilter) throws RuntimeException
    {
        setObjectTypeBaseProperties(documentNodeRef, target, propertyFilter);

        NodeRef workingCopy = null;

        if (nodeService.hasAspect(documentNodeRef, ContentModel.ASPECT_WORKING_COPY))
        {
            workingCopy = documentNodeRef;

            if (propertyFilter.allow(CmisProperty.IS_LATEST_VERSION))
            {
                target.setIsLatestVersion(false);
            }

            if (propertyFilter.allow(CmisProperty.IS_MAJOR_VERSION))
            {
                target.setIsMajorVersion(false);
            }

            if (propertyFilter.allow(CmisProperty.IS_LATEST_MAJOR_VERSION))
            {
                target.setIsLatestMajorVersion(false);
            }
        }
        else
        {
            target.setName((String) nodeService.getProperty(documentNodeRef, ContentModel.PROP_NAME));

            workingCopy = checkOutCheckInService.getWorkingCopy(documentNodeRef);

            Version version = versionService.getCurrentVersion(documentNodeRef);

            if (nodeService.hasAspect(documentNodeRef, ContentModel.ASPECT_VERSIONABLE) && version != null)
            {
                if (propertyFilter.allow(CmisProperty.IS_LATEST_VERSION))
                {
                    target.setIsLatestVersion(version.getVersionedNodeRef().equals(documentNodeRef));
                }

                if (propertyFilter.allow(CmisProperty.IS_MAJOR_VERSION))
                {
                    target.setIsMajorVersion(version.getVersionType().equals(VersionType.MAJOR));
                }

                if (propertyFilter.allow(CmisProperty.IS_LATEST_MAJOR_VERSION))
                {
                    NodeRef latestMajorNodeRef = getLatestVersionNodeRef(documentNodeRef, true);
                    target.setIsLatestMajorVersion(latestMajorNodeRef.equals(documentNodeRef));
                }

                if (propertyFilter.allow(CmisProperty.CHECKIN_COMMENT) && workingCopy == null)
                {
                    target.setCheckinComment(version.getDescription());
                }
            }
            else
            {
                if (propertyFilter.allow(CmisProperty.IS_LATEST_VERSION))
                {
                    target.setIsLatestVersion(true);
                }
                if (propertyFilter.allow(CmisProperty.IS_MAJOR_VERSION))
                {
                    target.setIsMajorVersion(true);
                }
                if (propertyFilter.allow(CmisProperty.IS_LATEST_MAJOR_VERSION))
                {
                    target.setIsLatestMajorVersion(true);
                }
            }
        }

        if (propertyFilter.allow(CmisProperty.VERSION_SERIES_IS_CHECKED_OUT))
        {
            target.setVersionSeriesIsCheckedOut(workingCopy != null);
        }

        if (propertyFilter.allow(CmisProperty.VERSION_SERIES_CHECKED_OUT_BY) && workingCopy != null)
        {
            target.setVersionSeriesCheckedOutBy((String) nodeService.getProperty(workingCopy, ContentModel.PROP_WORKING_COPY_OWNER));
        }

        if (propertyFilter.allow(CmisProperty.VERSION_SERIES_CHECKED_OUT_OID) && workingCopy != null)
        {
            if (AuthenticationUtil.getCurrentUserName().equals((String) nodeService.getProperty(workingCopy, ContentModel.PROP_WORKING_COPY_OWNER)))
            {
                target.setVersionSeriesCheckedOutOID(OIDUtils.toOID(workingCopy));
            }
        }

        ContentReader contentReader = null;

        if (nodeService.getType(documentNodeRef).equals(ContentModel.TYPE_LINK))
        {
            NodeRef destRef = (NodeRef) nodeService.getProperty(documentNodeRef, ContentModel.PROP_LINK_DESTINATION);
            if (nodeService.exists(destRef))
            {
                contentReader = fileFolderService.getReader(destRef);
            }
        }
        else
        {
            contentReader = fileFolderService.getReader(documentNodeRef);
        }

        if (contentReader != null)
        {
            if (propertyFilter.allow(CmisProperty.CONTENT_STREAM_LENGTH))
            {
                target.setContentStreamLength(BigInteger.valueOf(contentReader.getSize()));
            }
            if (propertyFilter.allow(CmisProperty.CONTENT_STREAM_MIME_TYPE))
            {
                target.setContentStreamMimeType(contentReader.getMimetype());
            }
            if (propertyFilter.allow(CmisProperty.CONTENT_STREAM_FILENAME))
            {
                target.setContentStreamFilename(target.getName()); // TODO: right on not?
            }
        }

        return target;
    }

    /**
     * Sets properties for RelationshipObjectType object
     *
     * @param associationRef Association reference
     * @param target RelationshipObjectType object for setting
     * @param propertyFilter filter for properties
     * @return RelationshipObjectType object
     */
    public RelationshipObjectType setRelationshipObjectTypeProperties(AssociationRef associationRef, RelationshipObjectType target, PropertyFilter propertyFilter)
    {
        target.setSourceOID(OIDUtils.toOID(associationRef.getSourceRef()));
        target.setTargetOID(OIDUtils.toOID(associationRef.getTargetRef()));
        // TODO: other properties
        return target;
    }

    /**
     * Sets properties for DocumentFolderOrRelationshipObjectType object
     *
     * @param associationRef Association reference
     * @param target DocumentFolderOrRelationshipObjectType object for setting
     * @param propertyFilter filter for properties
     * @return DocumentFolderOrRelationshipObjectType object
     */
    public DocumentFolderOrRelationshipObjectType setRelationshipObjectTypeProperties(AssociationRef associationRef, DocumentFolderOrRelationshipObjectType target, PropertyFilter propertyFilter)
    {
        target.setSourceOID(OIDUtils.toOID(associationRef.getSourceRef()));
        target.setTargetOID(OIDUtils.toOID(associationRef.getTargetRef()));
        // TODO: other properties
        return target;
    }

    /**
     * @param associationRef a reference to the association to look for
     * @return Returns true if the association exists, otherwise false
     */
    public boolean exists(AssociationRef associationRef)
    {
        if (nodeService.exists(associationRef.getSourceRef()))
        {
            return nodeService.getTargetAssocs(associationRef.getSourceRef(), associationRef.getTypeQName()).contains(associationRef);
        }

        return false;
    }

    /**
     * @param nodeRef
     * @return
     */
    public boolean isFolderType(NodeRef nodeRef)
    {
        QName typeQName = nodeService.getType(nodeRef);

        return dictionaryService.isSubClass(typeQName, ContentModel.TYPE_FOLDER);
    }

    /**
     * @param nodeRef
     * @return
     */
    public boolean isDocumentType(NodeRef nodeRef)
    {
        QName typeQName = nodeService.getType(nodeRef);

        return dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT);
    }

    /**
     * Returns latest minor or major version of document
     *
     * @param documentNodeRef document node reference
     * @param major need latest major version
     * @return latest version node reference
     */
    public NodeRef getLatestVersionNodeRef(NodeRef documentNodeRef, boolean major)
    {
        Version currentVersion = versionService.getCurrentVersion(documentNodeRef);
        NodeRef latestVersionNodeRef = documentNodeRef;

        if (currentVersion != null)
        {
            latestVersionNodeRef = currentVersion.getVersionedNodeRef();

            if (major)
            {
                Version latestVersion = versionService.getCurrentVersion(latestVersionNodeRef);

                if (latestVersion.getVersionType().equals(VersionType.MAJOR) == false)
                {
                    VersionHistory versionHistory = versionService.getVersionHistory(currentVersion.getVersionedNodeRef());

                    do
                    {
                        latestVersion = versionHistory.getPredecessor(latestVersion);
                    } while (latestVersion.getVersionType().equals(VersionType.MAJOR) == false);

                    latestVersionNodeRef = latestVersion.getFrozenStateNodeRef();
                }
            }
        }

        return latestVersionNodeRef;
    }

    /**
     * Converts Date object to XMLGregorianCalendar object
     *
     * @param date Date object
     * @return XMLGregorianCalendar object
     * @throws RuntimeException
     */
    private XMLGregorianCalendar convert(Date date) throws RuntimeException
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return getDatatypeFactory().newXMLGregorianCalendar(calendar);
    }

}
