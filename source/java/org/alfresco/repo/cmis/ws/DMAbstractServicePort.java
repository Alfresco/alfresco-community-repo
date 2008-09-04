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

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.alfresco.cmis.CMISService;
import org.alfresco.cmis.dictionary.CMISDictionaryService;
import org.alfresco.cmis.dictionary.CMISMapping;
import org.alfresco.cmis.property.CMISPropertyNameMapping;
import org.alfresco.cmis.property.CMISPropertyService;
import org.alfresco.cmis.search.CMISQueryService;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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
 */
public class DMAbstractServicePort
{
    private DatatypeFactory _datatypeFactory;

    protected CMISDictionaryService cmisDictionaryService;
    protected CMISQueryService cmisQueryService;
    protected CMISService cmisService;
    protected CMISPropertyService cmisPropertyService;
    protected DescriptorService descriptorService;
    protected NodeService nodeService;
    protected VersionService versionService;
    protected FileFolderService fileFolderService;

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

    protected NodeRef getNodeRefFromOID(String oid) throws InvalidArgumentException
    {
        NodeRef nodeRef;

        try
        {
            nodeRef = new NodeRef(oid);
        }
        catch (AlfrescoRuntimeException e)
        {
            // TODO: error code
            throw new  InvalidArgumentException("Invalid OID value", ExceptionUtils.createBasicFault(null, "Invalid OID value"));
        }

        return nodeRef;
    }

    private void addBooleanProperty(PropertiesType properties, PropertyFilter filter, String name, NodeRef nodeRef)
    {
        Serializable value = cmisPropertyService.getProperty(nodeRef, name);
        if (filter.allow(name) && value != null)
        {
            PropertyBooleanType propBoolean = new PropertyBooleanType();
            propBoolean.setName(CMISPropertyNameMapping.getWebServiceName(name));
            propBoolean.setValue((Boolean) value);
            properties.getPropertyBoolean().add(propBoolean);
        }
    }

    private void addDateTimeProperty(PropertiesType properties, PropertyFilter filter, String name, NodeRef nodeRef)
    {
        Serializable value = cmisPropertyService.getProperty(nodeRef, name);
        if (filter.allow(name) && value != null)
        {
            PropertyDateTimeType propDateTime = new PropertyDateTimeType();
            propDateTime.setName(CMISPropertyNameMapping.getWebServiceName(name));
            propDateTime.setValue(convert((Date) value));
            properties.getPropertyDateTime().add(propDateTime);
        }
    }

    private void addIDProperty(PropertiesType properties, PropertyFilter filter, String name, NodeRef nodeRef)
    {
        Serializable value = cmisPropertyService.getProperty(nodeRef, name);
        if (filter.allow(name) && value != null)
        {
            PropertyIDType propID = new PropertyIDType();
            propID.setName(CMISPropertyNameMapping.getWebServiceName(name));
            propID.setValue(value.toString());
            properties.getPropertyID().add(propID);
        }
    }

    private void addIntegerProperty(PropertiesType properties, PropertyFilter filter, String name, NodeRef nodeRef)
    {
        Serializable value = cmisPropertyService.getProperty(nodeRef, name);
        if (filter.allow(name) && value != null)
        {
            PropertyIntegerType propInteger = new PropertyIntegerType();
            propInteger.setName(CMISPropertyNameMapping.getWebServiceName(name));
            propInteger.setValue(BigInteger.valueOf((Long) value));
            properties.getPropertyInteger().add(propInteger);
        }
    }

    private void addStringProperty(PropertiesType properties, PropertyFilter filter, String name, NodeRef nodeRef)
    {
        Serializable value = cmisPropertyService.getProperty(nodeRef, name);
        if (filter.allow(name) && value != null)
        {
            PropertyStringType propString = new PropertyStringType();
            propString.setName(CMISPropertyNameMapping.getWebServiceName(name));
            propString.setValue(value.toString());
            properties.getPropertyString().add(propString);
        }
    }

    private void addStringProperty(PropertiesType properties, PropertyFilter filter, String name, String value)
    {
        if (filter.allow(name) && value != null)
        {
            PropertyStringType propString = new PropertyStringType();
            propString.setName(name);
            propString.setValue(value);
            properties.getPropertyString().add(propString);
        }
    }

    private void addURIProperty(PropertiesType properties, PropertyFilter filter, String name, NodeRef nodeRef)
    {
        Serializable value = cmisPropertyService.getProperty(nodeRef, name);
        if (filter.allow(name) && value != null)
        {
            PropertyURIType propString = new PropertyURIType();
            propString.setName(CMISPropertyNameMapping.getWebServiceName(name));
            propString.setValue(value.toString());
            properties.getPropertyURI().add(propString);
        }
    }

    /**
     * Get CMIS properties for object
     *
     * @param nodeRef node reference
     * @param filter property filter
     * @return properties
     */
    public PropertiesType getPropertiesType(NodeRef nodeRef, PropertyFilter filter)
    {
        CMISMapping cmisMapping = cmisDictionaryService.getCMISMapping();
        QName cmisType = cmisMapping.getCmisType(nodeService.getType(nodeRef));

        PropertiesType properties = new PropertiesType();

        if (cmisMapping.isValidCmisDocument(cmisType))
        {
            addBooleanProperty(properties, filter, CMISMapping.PROP_IS_IMMUTABLE, nodeRef);
            addBooleanProperty(properties, filter, CMISMapping.PROP_IS_LATEST_VERSION, nodeRef);
            addBooleanProperty(properties, filter, CMISMapping.PROP_IS_MAJOR_VERSION, nodeRef);
            addBooleanProperty(properties, filter, CMISMapping.PROP_IS_LATEST_MAJOR_VERSION, nodeRef);
            addBooleanProperty(properties, filter, CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT, nodeRef);
            addDateTimeProperty(properties, filter, CMISMapping.PROP_CREATION_DATE, nodeRef);
            addDateTimeProperty(properties, filter, CMISMapping.PROP_LAST_MODIFICATION_DATE, nodeRef);
            addIDProperty(properties, filter, CMISMapping.PROP_OBJECT_ID, nodeRef);
            addIDProperty(properties, filter, CMISMapping.PROP_VERSION_SERIES_ID, nodeRef);
            addIDProperty(properties, filter, CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID, nodeRef);
            addIntegerProperty(properties, filter, CMISMapping.PROP_CONTENT_STREAM_LENGTH, nodeRef);
            addStringProperty(properties, filter, CMISMapping.PROP_NAME, nodeRef);
            addStringProperty(properties, filter, "baseType", "document");
            addIDProperty(properties, filter, CMISMapping.PROP_OBJECT_TYPE_ID, nodeRef);
            addStringProperty(properties, filter, CMISMapping.PROP_CREATED_BY, nodeRef);
            addStringProperty(properties, filter, CMISMapping.PROP_LAST_MODIFIED_BY, nodeRef);
            addStringProperty(properties, filter, CMISMapping.PROP_CONTENT_STREAM_MIME_TYPE, nodeRef);
            addStringProperty(properties, filter, CMISMapping.PROP_CONTENT_STREAM_FILENAME, nodeRef);
            addStringProperty(properties, filter, CMISMapping.PROP_VERSION_LABEL, nodeRef);
            addStringProperty(properties, filter, CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY, nodeRef);
            addStringProperty(properties, filter, CMISMapping.PROP_CHECKIN_COMMENT, nodeRef);
            addURIProperty(properties, filter, CMISMapping.PROP_CONTENT_STREAM_URI, nodeRef);
        }
        else if (cmisMapping.isValidCmisFolder(cmisType))
        {
            addDateTimeProperty(properties, filter, CMISMapping.PROP_CREATION_DATE, nodeRef);
            addDateTimeProperty(properties, filter, CMISMapping.PROP_LAST_MODIFICATION_DATE, nodeRef);
            addIDProperty(properties, filter, CMISMapping.PROP_OBJECT_ID, nodeRef);
            addIDProperty(properties, filter, CMISMapping.PROP_PARENT, nodeRef);
            addStringProperty(properties, filter, CMISMapping.PROP_NAME, nodeRef);
            addStringProperty(properties, filter, "baseType", "folder");
            addIDProperty(properties, filter, CMISMapping.PROP_OBJECT_TYPE_ID, nodeRef);
            addStringProperty(properties, filter, CMISMapping.PROP_CREATED_BY, nodeRef);
            addStringProperty(properties, filter, CMISMapping.PROP_LAST_MODIFIED_BY, nodeRef);
        }

        return properties;
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
                    }
                    while (latestVersion.getVersionType().equals(VersionType.MAJOR) == false);

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
     */
    private XMLGregorianCalendar convert(Date date)
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return getDatatypeFactory().newXMLGregorianCalendar(calendar);
    }

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

}
