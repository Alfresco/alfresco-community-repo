/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.service.cmr.usage;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.namespace.QName;

/**
 * This exception will be thrown when a content type-based quota has been violated.
 * 
 * @author Neil Mc Erlean
 */
public class QuotaViolationException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 1L;
    
    /** The content type that has been quota-restricted. */
    private final QName quotaRestrictedType;
    
    /** The quota limit applied to that type. */
    private final long quotaLimit;
    
    /** The usage that violated the quota limit. */
    private final long usage;
    
    public QuotaViolationException(String msgId, long usage, long quotaLimit, QName quotaRestrictedType)
    {
        this(msgId, null, usage, quotaLimit, quotaRestrictedType);
    }
    
    public QuotaViolationException(String msgId, Throwable cause, long usage, long quotaLimit, QName quotaRestrictedType)
    {
        super(msgId);
        
        this.quotaRestrictedType = quotaRestrictedType;
        this.quotaLimit = quotaLimit;
        this.usage = usage;
    }
    
    /** Gets the type of the node which has been quota restricted. */
    public QName getQuotaRestrictedType() { return quotaRestrictedType; }
    
    /** Gets the quota limit applied to the restricted type. */
    public long getQuotaLimit() { return quotaLimit; }
    
    /** Gets the usage that violated the quota limit. */
    public long getUsage() { return usage; }
    
    /** {@inheritDoc} */
    @Override public String toString()
    {
        final String restrictedType = quotaRestrictedType == null ? "null" : quotaRestrictedType.getPrefixString();
        
        StringBuilder msg = new StringBuilder();
        msg.append(this.getClass().getSimpleName())
           .append(" '").append(getMessage()).append("' ")
           .append("(").append(restrictedType).append(": ")
           .append(usage).append(" violates limit ").append(quotaLimit).append(")");
        return msg.toString();
    }
}
