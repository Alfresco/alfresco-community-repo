/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.relationship;

import static org.alfresco.util.ParameterCheck.mandatory;
import static org.alfresco.util.ParameterCheck.mandatoryString;

import java.io.Serializable;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Relationship implementation
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RelationshipImpl implements Relationship, Serializable
{
        /** serial UID */
    private static final long serialVersionUID = 9120649510198344978L;

    /** The unique name of the relationship */
    private String uniqueName;

    /** The source of the relationship */
    private NodeRef source;

    /** The target of the relationship */
    private NodeRef target;

    /**
     * Constructor for creating a relationship
     *
     * @param uniqueName The unique name of the relationship
     * @param source The source of the relationship
     * @param target The target of the relationship
     */
    public RelationshipImpl(String uniqueName, NodeRef source, NodeRef target)
    {
        mandatoryString("uniqueName", uniqueName);
        mandatory("source", source);
        mandatory("target", target);

        setUniqueName(uniqueName);
        setSource(source);
        setTarget(target);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.relationship.Relationship#getUniqueName()
     */
    @Override
    public String getUniqueName()
    {
        return uniqueName;
    }

    /**
     * Sets the unique name of the relationship
     *
     * @param uniqueName The unique name of the relationship
     */
    private void setUniqueName(String uniqueName)
    {
        this.uniqueName = uniqueName;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.relationship.Relationship#getSource()
     */
    @Override
    public NodeRef getSource()
    {
        return source;
    }

    /**
     * Sets the source of the relationship
     *
     * @param source The source of the relationship
     */
    private void setSource(NodeRef source)
    {
        this.source = source;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.relationship.Relationship#getTarget()
     */
    @Override
    public NodeRef getTarget()
    {
        return target;
    }

    /**
     * Sets the target of the relationship
     *
     * @param target The target of the relationship
     */
    private void setTarget(NodeRef target)
    {
        this.target = target;
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof Relationship)
        {
            RelationshipImpl that = (RelationshipImpl) obj;
            return (this.uniqueName.equals(that.uniqueName)
                    && this.source.equals(that.source)
                    && this.target.equals(that.target));
        }
        else
        {
            return false;
        }
    }
    
    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        int prime = 31;
        int result = prime + uniqueName.hashCode();
        result = (prime*result) + source.hashCode();
        return (prime*result) + target.hashCode();
    }
}
