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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.alfresco.cmis.CMISService;
import org.alfresco.cmis.dictionary.CMISDictionaryService;
import org.alfresco.cmis.dictionary.CMISMapping;
import org.alfresco.cmis.property.CMISPropertyService;
import org.alfresco.cmis.search.CMISQueryService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cmis.PropertyFilter;
import org.alfresco.repo.cmis.ws.utils.CmisObjectsUtils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.repo.web.util.paging.Cursor;
import org.alfresco.repo.web.util.paging.Page;
import org.alfresco.repo.web.util.paging.Paging;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.namespace.QName;

/**
 * Base class for all CMIS web services
 *
 * @author Michael Shavnev
 * @author Dmitry Lazurkin
 * @author Dmitry Velichkevich
 */
public class DMAbstractServicePort
{
    private static final String BASE_TYPE_PROPERTY_NAME = "BaseType";
    protected static final String INITIAL_VERSION_DESCRIPTION = "Initial version";

    private DatatypeFactory _datatypeFactory;
    private Paging paging = new Paging();

    protected ObjectFactory cmisObjectFactory = new ObjectFactory();
    protected CMISDictionaryService cmisDictionaryService;
    protected CMISQueryService cmisQueryService;
    protected CMISService cmisService;
    protected CMISPropertyService cmisPropertyService;
    protected DescriptorService descriptorService;
    protected NodeService nodeService;
    protected VersionService versionService;
    protected FileFolderService fileFolderService;
    protected CheckOutCheckInService checkOutCheckInService;
    protected SearchService searchService;
    protected CmisObjectsUtils cmisObjectsUtils;

    
    public void setCmisService(CMISService cmisService)
    {
        this.cmisService = cmisService;
    }

    public void setCmisPropertyService(CMISPropertyService cmisPropertyService)
    {
        this.cmisPropertyService = cmisPropertyService;
    }

    public void setCmisDictionaryService(CMISDictionaryService cmisDictionaryService)
    {
        this.cmisDictionaryService = cmisDictionaryService;
    }

    public void setCmisQueryService(CMISQueryService cmisQueryService)
    {
        this.cmisQueryService = cmisQueryService;
    }

    public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setVersionService(VersionService versionService)
    {
        this.versionService = versionService;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService)
    {

        this.checkOutCheckInService = checkOutCheckInService;
    }

    public void setCmisObjectsUtils(CmisObjectsUtils cmisObjectsUtils)
    {
        this.cmisObjectsUtils = cmisObjectsUtils;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    protected static PropertyFilter createPropertyFilter(String filter) throws FilterNotValidException
    {
        return (filter == null) ? (new PropertyFilter()) : (new PropertyFilter(filter));
    }

    protected static PropertyFilter createPropertyFilter(JAXBElement<String> element) throws FilterNotValidException
    {
        String filter = null;
        if (element != null)
        {
            filter = element.getValue();
        }
        return createPropertyFilter(filter);
    }

    protected Cursor createCursor(int totalRows, BigInteger skipCount, BigInteger maxItems)
    {
        Page window = paging.createPageOrWindow(null, null, skipCount != null ? skipCount.intValue() : null, maxItems != null ? maxItems.intValue() : null);
        return paging.createCursor(totalRows, window);
    }

    /**
     * Converts Date object to XMLGregorianCalendar object
     *
     * @param date Date object
     * @return XMLGregorianCalendar object
     */
    protected XMLGregorianCalendar convert(Date date)
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return getDatatypeFactory().newXMLGregorianCalendar(calendar);
    }

    private DatatypeFactory getDatatypeFactory()
    {
        if (_datatypeFactory == null)
        {
            try
            {
                _datatypeFactory = DatatypeFactory.newInstance();
            }
            catch (DatatypeConfigurationException e)
            {
                // suppress
            }
        }
        return _datatypeFactory;
    }

    /**
     * This method converts Alfresco's <b>NodeRef</b>'s to CMIS objects those will be stored in <b>resultList</b>-parameter. Properties for returning filtering also performs
     * 
     * @param filter properties filter value for filtering objects returning properties
     * @param sourceList the list that contains all returning Node References
     * @param resultList the list of <b>CmisObjectType</b> values for end response result collecting
     * @throws InvalidArgumentException
     * @throws FilterNotValidException
     */
    protected void createCmisObjectList(PropertyFilter filter, List<NodeRef> sourceList, List<CmisObjectType> resultList) throws InvalidArgumentException, FilterNotValidException
    {
        for (NodeRef objectNodeRef : sourceList)
        {
            resultList.add(createCmisObject(objectNodeRef, filter));
        }
    }

    /**
     * This method creates and configures CMIS object against appropriate Alfresco object (NodeRef or AssociationRef)
     * 
     * @param objectNodeRef the Alfresco object against those conversion must to be done
     * @param filter accepted properties filter
     * @return converted to CMIS object Alfresco object
     */
    protected CmisObjectType createCmisObject(Object identifier, PropertyFilter filter)
    {
        CmisObjectType result = new CmisObjectType();
        result.setProperties(getPropertiesType(identifier.toString(), filter));
        return result;
    }

    /**
     * Asserts "Folder with folderNodeRef exists"
     *
     * @param folderNodeRef node reference
     * @throws FolderNotValidException folderNodeRef doesn't exist or folderNodeRef isn't for folder object
     */
    protected void assertExistFolder(NodeRef folderNodeRef) throws FolderNotValidException
    {
        if (!this.cmisObjectsUtils.isFolder(folderNodeRef))
        {
            throw new FolderNotValidException("OID for non-existent object or not folder object");
        }
    }

    /**
     * Checks specified in CMIS request parameters repository Id.
     * 
     * @param repositoryId repository id
     * @throws InvalidArgumentException repository diesn't exist
     */
    protected void checkRepositoryId(String repositoryId) throws InvalidArgumentException
    {
        if (!this.descriptorService.getServerDescriptor().getId().equals(repositoryId))
        {
            throw new InvalidArgumentException("Invalid repository id");
        }
    }

    /**
     * Get CMIS properties for object
     *
     * @param nodeRef node reference
     * @param filter property filter
     * @return properties
     */
    protected CmisPropertiesType getPropertiesType(String identifier, PropertyFilter filter)
    {
        Map<String, Serializable> properties;
        if (NodeRef.isNodeRef(identifier))
        {
            properties = cmisPropertyService.getProperties(new NodeRef(identifier));
        }
        else
        {
            properties = createBaseRelationshipProperties(new AssociationRef(identifier));
        }
        return getPropertiesType(properties, filter);
    }

    protected CmisPropertiesType getPropertiesType(Map<String, Serializable> alfrescoProperties, PropertyFilter filter)
    {
        CMISMapping cmisMapping = cmisDictionaryService.getCMISMapping();
        String objectTypeId = (String) alfrescoProperties.get(CMISMapping.PROP_OBJECT_TYPE_ID);
        QName cmisType = cmisMapping.getCmisTypeId(objectTypeId).getQName();

        CmisPropertiesType properties = new CmisPropertiesType();

        if (cmisMapping.isValidCmisDocument(cmisType))
        {
            addBooleanProperty(properties, filter, CMISMapping.PROP_IS_IMMUTABLE, alfrescoProperties);
            addBooleanProperty(properties, filter, CMISMapping.PROP_IS_LATEST_VERSION, alfrescoProperties);
            addBooleanProperty(properties, filter, CMISMapping.PROP_IS_MAJOR_VERSION, alfrescoProperties);
            addBooleanProperty(properties, filter, CMISMapping.PROP_IS_LATEST_MAJOR_VERSION, alfrescoProperties);
            addBooleanProperty(properties, filter, CMISMapping.PROP_IS_VERSION_SERIES_CHECKED_OUT, alfrescoProperties);
            addDateTimeProperty(properties, filter, CMISMapping.PROP_CREATION_DATE, alfrescoProperties);
            addDateTimeProperty(properties, filter, CMISMapping.PROP_LAST_MODIFICATION_DATE, alfrescoProperties);
            addIDProperty(properties, filter, CMISMapping.PROP_OBJECT_ID, alfrescoProperties);
            addIDProperty(properties, filter, CMISMapping.PROP_VERSION_SERIES_ID, alfrescoProperties);
            addIDProperty(properties, filter, CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID, alfrescoProperties);
            addIntegerProperty(properties, filter, CMISMapping.PROP_CONTENT_STREAM_LENGTH, alfrescoProperties);
            addStringProperty(properties, filter, CMISMapping.PROP_NAME, alfrescoProperties);
            addStringProperty(properties, filter, BASE_TYPE_PROPERTY_NAME, "document");
            addStringProperty(properties, filter, CMISMapping.PROP_OBJECT_TYPE_ID, alfrescoProperties);
            addStringProperty(properties, filter, CMISMapping.PROP_CREATED_BY, alfrescoProperties);
            addStringProperty(properties, filter, CMISMapping.PROP_LAST_MODIFIED_BY, alfrescoProperties);
            addStringProperty(properties, filter, CMISMapping.PROP_CONTENT_STREAM_MIME_TYPE, alfrescoProperties);
            addStringProperty(properties, filter, CMISMapping.PROP_CONTENT_STREAM_FILENAME, alfrescoProperties);
            addStringProperty(properties, filter, CMISMapping.PROP_VERSION_LABEL, alfrescoProperties);
            addStringProperty(properties, filter, CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY, alfrescoProperties);
            addStringProperty(properties, filter, CMISMapping.PROP_CHECKIN_COMMENT, alfrescoProperties);
            addURIProperty(properties, filter, CMISMapping.PROP_CONTENT_STREAM_URI, alfrescoProperties);
        }
        else if (cmisMapping.isValidCmisFolder(cmisType))
        {
            addDateTimeProperty(properties, filter, CMISMapping.PROP_CREATION_DATE, alfrescoProperties);
            addDateTimeProperty(properties, filter, CMISMapping.PROP_LAST_MODIFICATION_DATE, alfrescoProperties);
            addIDProperty(properties, filter, CMISMapping.PROP_OBJECT_ID, alfrescoProperties);
            addIDProperty(properties, filter, CMISMapping.PROP_PARENT_ID, alfrescoProperties);
            addStringProperty(properties, filter, CMISMapping.PROP_NAME, alfrescoProperties);
            addStringProperty(properties, filter, BASE_TYPE_PROPERTY_NAME, "folder");
            addStringProperty(properties, filter, CMISMapping.PROP_OBJECT_TYPE_ID, alfrescoProperties);
            addStringProperty(properties, filter, CMISMapping.PROP_CREATED_BY, alfrescoProperties);
            addStringProperty(properties, filter, CMISMapping.PROP_LAST_MODIFIED_BY, alfrescoProperties);
        }
        else if (cmisMapping.isValidCmisRelationship(cmisType))
        {
            addStringProperty(properties, filter, CMISMapping.PROP_OBJECT_TYPE_ID, alfrescoProperties);
            addIDProperty(properties, filter, CMISMapping.PROP_OBJECT_ID, alfrescoProperties);
            addStringProperty(properties, filter, BASE_TYPE_PROPERTY_NAME, alfrescoProperties);
            addStringProperty(properties, filter, CMISMapping.PROP_CREATED_BY, alfrescoProperties);
            addDateTimeProperty(properties, filter, CMISMapping.PROP_CREATION_DATE, alfrescoProperties);
            addIDProperty(properties, filter, CMISMapping.PROP_SOURCE_ID, alfrescoProperties);
            addIDProperty(properties, filter, CMISMapping.PROP_TARGET_ID, alfrescoProperties);
        }

        return properties;
    }

    private Map<String, Serializable> createBaseRelationshipProperties(AssociationRef association)
    {
        Map<String, Serializable> result = new HashMap<String, Serializable>();
        result.put(CMISMapping.PROP_OBJECT_TYPE_ID, cmisDictionaryService.getCMISMapping().getCmisType(association.getTypeQName()));
        result.put(CMISMapping.PROP_OBJECT_ID, association.toString());
        result.put(BASE_TYPE_PROPERTY_NAME, CMISMapping.RELATIONSHIP_TYPE_ID.getTypeId());
        result.put(CMISMapping.PROP_CREATED_BY, AuthenticationUtil.getFullyAuthenticatedUser());
        result.put(CMISMapping.PROP_CREATION_DATE, new Date());
        result.put(CMISMapping.PROP_SOURCE_ID, association.getSourceRef());
        result.put(CMISMapping.PROP_TARGET_ID, association.getTargetRef());
        return result;
    }

    protected Map<String, Serializable> createVersionProperties(String versionDescription, VersionType versionType)
    {
        Map<String, Serializable> result = new HashMap<String, Serializable>();
        result.put(Version.PROP_DESCRIPTION, versionDescription);
        result.put(VersionModel.PROP_VERSION_TYPE, versionType);
        return result;
    }
    
    private void addBooleanProperty(CmisPropertiesType properties, PropertyFilter filter, String name, Map<String, Serializable> alfrescoProperties)
    {
        Serializable value = alfrescoProperties.get(name);
        if (filter.allow(name) && value != null)
        {
            CmisPropertyBoolean propBoolean = new CmisPropertyBoolean ();
            propBoolean.setName(PropertyUtil.getCMISPropertyName(name));
            propBoolean.setValue((Boolean) value);
            properties.getProperty().add(propBoolean);
        }
    }

    private void addDateTimeProperty(CmisPropertiesType properties, PropertyFilter filter, String name, Map<String, Serializable> alfrescoProperties)
    {
        Serializable value = alfrescoProperties.get(name);
        if (filter.allow(name) && value != null)
        {
            CmisPropertyDateTime propDateTime = new CmisPropertyDateTime();
            propDateTime.setName(PropertyUtil.getCMISPropertyName(name));
            propDateTime.setValue(convert((Date) value));
            properties.getProperty().add(propDateTime);
        }
    }

    private void addIDProperty(CmisPropertiesType properties, PropertyFilter filter, String name, Map<String, Serializable> alfrescoProperties)
    {
        Serializable value = alfrescoProperties.get(name);
        if (filter.allow(name) && value != null)
        {
            CmisPropertyId propID = new CmisPropertyId();
            propID.setName(PropertyUtil.getCMISPropertyName(name));
            propID.setValue(value.toString());
            properties.getProperty().add(propID);
        }
    }

    private void addIntegerProperty(CmisPropertiesType properties, PropertyFilter filter, String name, Map<String, Serializable> alfrescoProperties)
    {
        Serializable value = alfrescoProperties.get(name);
        if (filter.allow(name) && value != null)
        {
            CmisPropertyInteger propInteger = new CmisPropertyInteger();
            propInteger.setName(PropertyUtil.getCMISPropertyName(name));
            propInteger.setValue(BigInteger.valueOf((Long) value));
            properties.getProperty().add(propInteger);
        }
    }

    private void addStringProperty(CmisPropertiesType properties, PropertyFilter filter, String name, Map<String, Serializable> alfrescoProperties)
    {
        Serializable value = alfrescoProperties.get(name);
        if (filter.allow(name) && value != null)
        {
            CmisPropertyString propString = new CmisPropertyString();
            propString.setName(PropertyUtil.getCMISPropertyName(name));
            propString.setValue(value.toString());
            properties.getProperty().add(propString);
        }
    }

    private void addStringProperty(CmisPropertiesType properties, PropertyFilter filter, String name, String value)
    {
        if (filter.allow(name) && value != null)
        {
            CmisPropertyString propString = new CmisPropertyString();
            propString.setName(name);
            propString.setValue(value);
            properties.getProperty().add(propString);
        }
    }

    private void addURIProperty(CmisPropertiesType properties, PropertyFilter filter, String name, Map<String, Serializable> alfrescoProperties)
    {
        Serializable value = alfrescoProperties.get(name);
        if (filter.allow(name) && value != null)
        {
            CmisPropertyUri propString = new CmisPropertyUri();
            propString.setName(PropertyUtil.getCMISPropertyName(name));
            propString.setValue(value.toString());
            properties.getProperty().add(propString);
        }
    }
    
    /**
     * Sets all <i>properties</i>' fields for specified node
     * 
     * @param nodeRef the <b>NodeRef</b> for node for those properties must be setted
     * @param properties all necessary properties fields
     */
    protected void setProperties(NodeRef nodeRef, CmisPropertiesType properties)
    {
        // TODO: properties setting

        String name = (String) PropertyUtil.getProperty(properties, CMISMapping.PROP_NAME);
        if (name != null)
        {
            nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, name);
        }
    }

    /**
     * Returns latest minor or major version of document
     *
     * @param documentNodeRef document node reference
     * @param major need latest major version
     * @return latest version node reference
     */
    protected NodeRef getLatestNode(NodeRef documentNodeRef, boolean major)
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
    
    protected NodeRef checkoutNode(NodeRef documentNodeReference)
    {
        if (!this.nodeService.hasAspect(documentNodeReference, ContentModel.ASPECT_VERSIONABLE))
        {
            this.versionService.createVersion(documentNodeReference, createVersionProperties(INITIAL_VERSION_DESCRIPTION, VersionType.MAJOR));
        }
        return checkOutCheckInService.checkout(documentNodeReference);
    }
    
}
