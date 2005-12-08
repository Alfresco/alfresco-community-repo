/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.service.cmr.version;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;


/**
 * Version interface.
 * 
 * Allows access to version property values and frozen state node references.
 * The version history tree can also be navigated.
 * 
 * @author Roy Wetherall
 */
public interface Version extends Serializable
{
    /**
     * Names of the system version properties
     */
    public static final String PROP_DESCRIPTION = "description";
    
    /**
     * Helper method to get the created date from the version property data.
     * 
     * @return  the date the version was created
     */
    public Date getCreatedDate();
    
    /**
     * Helper method to get the creator of the version.
     * 
     * @return  the creator of the version
     */
    public String getCreator();

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
     * @return  a node reference
     */
    public NodeRef getVersionedNodeRef();
    
    /**
     * Gets the reference to the node that contains the frozen state of the
     * version.
     * 
     * @return  a node reference
     */
    public NodeRef getFrozenStateNodeRef();
}
