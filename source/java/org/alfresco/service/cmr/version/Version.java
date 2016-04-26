package org.alfresco.service.cmr.version;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Version interface.
 * 
 * Allows access to version property values and frozen state node references.
 * The version history tree can also be navigated.
 * 
 * @author Roy Wetherall, janv
 */
@AlfrescoPublicApi
public interface Version extends Serializable
{
    /**
     * Names of the system version properties
     */
    public static final String PROP_DESCRIPTION = VersionBaseModel.PROP_DESCRIPTION;
    
    /**
     * Helper method to get the created date from the version property data.
     * 
     * @return  the date the version was created
     * @deprecated use getFrozenModifiedDate
     */
    public Date getCreatedDate();
    
    /**
     * Helper method to get the creator of the version.
     * 
     * @return  the creator of the version
     * @deprecated use getFrozenModifier
     */
    public String getCreator();
    
    /**
     * Helper method to get the frozen (original) modified date for this version of the node
     * 
     * @return  the modified date
     */
    public Date getFrozenModifiedDate();
    
    
    /**
     * Helper method to get the frozen (original) modifier for this version of the node
     * 
     * @return  the modifier
     */
    public String getFrozenModifier();

    /**
     * Helper method to get the version label from the version property data.
     * 
     * @return  the version label
     */
    public String getVersionLabel();
    
    /**
     * Helper method to get the version type.
     * 
     * @return  the value of the version type as an enum value
     */
    public VersionType getVersionType();
    
    /**
     * Helper method to get the version description.
     * 
     * @return the version description
     */
    public String getDescription();

    /**
     * Get the map containing the version property values
     * 
     * @return  the map containing the version properties
     */
    public Map<String, Serializable> getVersionProperties();
    
    /**
     * Gets the value of a named version property.
     * 
     * @param name  the name of the property
     * @return      the value of the property
     * 
     */
    public Serializable getVersionProperty(String name);

    /**
     * Gets a reference to the node that this version was created from.
     * <p>
     * Note that this reference will be to the current state of the versioned
     * node which may now correspond to a later version.
     * 
     * @return  a node reference (to the versioned node in the 'live' store)
     */
    public NodeRef getVersionedNodeRef();
    
    /**
     * Gets the reference to the node that contains the frozen state of the
     * version.
     * 
     * @return  a node reference (to the version node in the 'version' store)
     */
    public NodeRef getFrozenStateNodeRef();
}
