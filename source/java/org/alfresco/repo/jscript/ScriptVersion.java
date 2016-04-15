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
package org.alfresco.repo.jscript;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.util.ParameterCheck;
import org.mozilla.javascript.Scriptable;

/**
 * Scriptable Version
 *
 * @author davidc
 */
public final class ScriptVersion implements Serializable
{
    private static final long serialVersionUID = 3896177303419746778L;

    /** Root scope for this object */
    private Scriptable scope;
    private ServiceRegistry services;
    private Version version;

    /**
     * Construct
     */
    public ScriptVersion(Version version, ServiceRegistry services, Scriptable scope)
    {
        this.version = version;
        this.services = services;
        this.scope = scope;
    }

    /**
     * Gets the date the version was created
     *
     * @return  the date the version was created
     */
    public Date getCreatedDate()
    {
        return version.getFrozenModifiedDate();
    }

    /**
     * Gets the creator of the version
     *
     * @return  the creator of the version
     */
    public String getCreator()
    {
        return version.getFrozenModifier();
    }

    /**
     * Gets the version label
     *
     * @return  the version label
     */
    public String getLabel()
    {
        return version.getVersionLabel();
    }

    /**
     * Gets the version type
     *
     * @return  "MAJOR", "MINOR"
     */
    public String getType()
    {
        if (version.getVersionType() != null)
        {
            return version.getVersionType().name();
        }
        else
        {
            return "";
        }
    }

    /**
     * Gets the version description (or checkin comment)
     *
     * @return the version description
     */
    public String getDescription()
    {
        String desc = version.getDescription();
        return (desc == null) ? "" : desc;
    }

    /**
     * Gets the node ref represented by this version
     *
     * @return  node ref
     */
    public NodeRef getNodeRef()
    {
        return version.getVersionedNodeRef();
    }

    /**
     * Gets the node represented by this version
     *
     * @return  node
     */
    public ScriptNode getNode()
    {
        return new ScriptNode(version.getFrozenStateNodeRef(), services, scope);
    }

    /**
     * Get the map containing the version property values
     *
     * @return  the map containing the version properties
     */
    public Map<String, Serializable> getVersionProperties()
    {
        return version.getVersionProperties();
    }

    /**
     * Gets the value of a named version property.
     *
     * @param name  the name of the property
     * @return      the value of the property
     */
    public Serializable getVersionProperty(String name)
    {
        ParameterCheck.mandatoryString("name", name);
        return version.getVersionProperty(name);
    }
}
