/*
 * Copyright (C) 2006 Alfresco, Inc.
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

package org.alfresco.repo.avm;

import java.io.Serializable;

/**
 * Holds a ancestor-descendent relationship.
 * @author britt
 */
class HistoryLinkImpl implements HistoryLink, Serializable
{
    private static final long serialVersionUID = -430859344980137718L;

    /**
     * The ancestor.
     */
    private AVMNode fAncestor;
    
    /**
     * The descendent.
     */
    private AVMNode fDescendent;
    
    /**
     * Set the ancestor part of this.
     * @param ancestor
     */
    public void setAncestor(AVMNode ancestor)
    {
        fAncestor = ancestor;
    }

    /**
     * Get the ancestor part of this.
     * @return The ancestor.
     */
    public AVMNode getAncestor()
    {
        return fAncestor;
    }

    /**
     * Set the descendent part of this.
     * @param descendent
     */
    public void setDescendent(AVMNode descendent)
    {
        fDescendent = descendent;
    }

    /**
     * Get the descendent part of this.
     * @return The descendent.
     */
    public AVMNode getDescendent()
    {
        return fDescendent;
    }

    /**
     * Equals override.
     * @param obj
     * @return Equality.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof HistoryLink))
        {
            return false;
        }
        HistoryLink o = (HistoryLink)obj;
        return fAncestor.equals(o.getAncestor()) && fDescendent.equals(o.getDescendent());
    }

    /**
     * Get the hashcode.
     * @return The hashcode.
     */
    @Override
    public int hashCode()
    {
        return fAncestor.hashCode() + fDescendent.hashCode();
    }
}
