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
package org.alfresco.module.org_alfresco_module_rm.referredmetadata;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

/**
 * Generic class for any runtime exceptions related to metadata referrals.
 *
 * @author Neil Mc Erlean
 * @since 2.4.a
 */
public class ReferredMetadataException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -6059777070036571486L;

    public ReferredMetadataException(String msgId)                  { super(msgId); }
    public ReferredMetadataException(String msgId, Throwable cause) { super(msgId, cause); }

    /** This exception may be thrown when a {@link MetadataReferral} was incorrectly initialised. */
    public static class InvalidMetadataReferral extends ReferredMetadataException
    {
        private static final long serialVersionUID = 8507076314709440295L;

        public InvalidMetadataReferral(String msgId)
        {
            super(msgId);
        }
    }

    /** This exception may be thrown when a {@link MetadataReferral} already exists. */
    public static class MetadataReferralAlreadyExists extends ReferredMetadataException
    {
        private static final long serialVersionUID = 8119954252195817706L;

        private final MetadataReferral metadataReferral;

        public MetadataReferralAlreadyExists(String msgId, MetadataReferral metadataReferral)
        {
            super(msgId);
            this.metadataReferral = metadataReferral;
        }

        public MetadataReferral getMetadataReferral()
        {
            return this.metadataReferral;
        }
    }

    /** A {@link MetadataReferral} has not been found. */
    public static class MetadataReferralNotFound extends ReferredMetadataException
    {
        private static final long serialVersionUID = 8648089074801662142L;

        public MetadataReferralNotFound(String msgId)
        {
            super(msgId);
        }
    }

    /** A referent Node has not been found. */
    public static class ReferentNodeNotFound extends ReferredMetadataException
    {
        private static final long serialVersionUID = -6003487925958374458L;

        public ReferentNodeNotFound(String msgId)
        {
            super(msgId);
        }
    }

    /** Exception to report that chains of metadata referral are not currently supported. */
    public static class ChainedMetadataReferralUnsupported extends ReferredMetadataException
    {
        private static final long serialVersionUID = -2293262325447442964L;

        private final List<NodeRef> existingReferrers;

        public ChainedMetadataReferralUnsupported(String msgId, List<NodeRef> existingReferrers)
        {
            super(msgId);
            this.existingReferrers = existingReferrers;
        }

        public List<NodeRef> getExistingReferrers()
        {
            return this.existingReferrers;
        }

        @Override public String toString()
        {
            StringBuilder msg = new StringBuilder();
            msg.append(this.getClass().getSimpleName()).append(" Already referring from: ")
               .append(existingReferrers.toString());
            return msg.toString();
        }
    }

    /** Exception to report that metadata referral is not supported for metadata defined on content types. */
    public static class TypeMetadataReferralUnsupported extends ReferredMetadataException
    {
        private static final long serialVersionUID = 7498707640089715503L;

        public TypeMetadataReferralUnsupported(String msgId)
        {
            super(msgId);
        }
    }
}
