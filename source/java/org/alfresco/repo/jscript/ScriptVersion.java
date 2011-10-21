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
package org.alfresco.repo.jscript;

import java.io.Serializable;
import java.util.Date;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
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
    private static ValueConverter converter = new ValueConverter();
    

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
        return version.getCreatedDate();
    }
    
    /**
     * Gets the creator of the version
     * 
     * @return  the creator of the version
     */
    public String getCreator()
    {
        return version.getCreator();
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
    
}
