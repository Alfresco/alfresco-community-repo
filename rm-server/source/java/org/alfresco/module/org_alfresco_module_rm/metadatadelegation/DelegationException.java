/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.metadatadelegation;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

/**
 * Generic class for any runtime exceptions related to metadata delegates.
 *
 * @author Neil Mc Erlean
 * @since 3.0.a
 */
public class DelegationException extends AlfrescoRuntimeException
{
    public DelegationException(String msgId)                  { super(msgId); }
    public DelegationException(String msgId, Throwable cause) { super(msgId, cause); }

    public static class InvalidDelegation extends DelegationException
    {
        public InvalidDelegation(String msgId)
        {
            super(msgId);
        }
    }

    /** A Metadata Delegation already exists. */
    public static class DelegationAlreadyExists extends DelegationException
    {
        private final Delegation delegation;

        public DelegationAlreadyExists(String msgId, Delegation delegation)
        {
            super(msgId);
            this.delegation = delegation;
        }
    }

    /**
     * A {@link Delegation} has not been found.
     * Remember that a Delegation is the definition of a type of link.
     */
    public static class DelegationNotFound extends DelegationException
    {
        public DelegationNotFound(String msgId)
        {
            super(msgId);
        }
    }

    /**
     * A Delegate has not been found.
     * Remember that a Delegate is an instance of a link between two nodes.
     */
    public static class DelegateNotFound extends DelegationException
    {
        public DelegateNotFound(String msgId)
        {
            super(msgId);
        }
    }

    /**
     * Exception to report that we currently do not support chained delegation.
     */
    public static class ChainedDelegationUnsupported extends DelegationException
    {
        private final List<NodeRef> nodesAlreadyDelegating;

        public ChainedDelegationUnsupported(String msgId, List<NodeRef> nodesAlreadyDelegating)
        {
            super(msgId);
            this.nodesAlreadyDelegating = nodesAlreadyDelegating;
        }

        public List<NodeRef> getNodesAlreadyDelegating()
        {
            return this.nodesAlreadyDelegating;
        }

        @Override public String toString()
        {
            StringBuilder msg = new StringBuilder();
            msg.append(this.getClass().getSimpleName()).append(" Already delegating from: ")
               .append(nodesAlreadyDelegating.toString());
            return msg.toString();
        }
    }
}
