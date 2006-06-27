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
 * This contains a single merged from-to relationship.
 * @author britt
 */
class MergeLinkImpl implements MergeLink, Serializable
{
    private static final long serialVersionUID = 6672271083042424944L;

    /**
     * The node that was merged from.
     */
    private AVMNode fFrom;
    
    /**
     * The node that was merged to.
     */
    private AVMNode fTo;
    
    /**
     * Set the from part.
     * @param from
     */
    public void setMfrom(AVMNode from)
    {
        fFrom = from;
    }

    /**
     * Get the from part.
     * @return The from part.
     */
    public AVMNode getMfrom()
    {
        return fFrom;
    }

    /**
     * Set the to part.
     * @param to
     */
    public void setMto(AVMNode to)
    {
        fTo = to;
    }

    /**
     * Get the to part.
     * @return The to part.
     */
    public AVMNode getMto()
    {
        return fTo;
    }

    /**
     * Override of equals.
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
        if (!(obj instanceof MergeLink))
        {
            return false;
        }
        MergeLink o = (MergeLink)obj;
        return fFrom.equals(o.getMfrom()) && fTo.equals(o.getMto());
    }

    /**
     * Get the hash code.
     * @return The hash code.
     */
    @Override
    public int hashCode()
    {
        return fFrom.hashCode() + fTo.hashCode();
    }
}
