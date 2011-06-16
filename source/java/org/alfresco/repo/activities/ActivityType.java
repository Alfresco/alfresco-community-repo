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
package org.alfresco.repo.activities;

public interface ActivityType
{
    // pre-defined alfresco activity types

    // generic fallback (if specific template is missing)
    public final String GENERIC_FALLBACK = "org.alfresco.generic";

    // site membership
    public final String SITE_USER_JOINED = "org.alfresco.site.user-joined";
    public final String SITE_USER_REMOVED = "org.alfresco.site.user-left";
    public final String SITE_USER_ROLE_UPDATE = "org.alfresco.site.user-role-changed";
    public final String SITE_GROUP_ADDED = "org.alfresco.site.group-added";
    public final String SITE_GROUP_REMOVED = "org.alfresco.site.group-removed";
    public final String SITE_GROUP_ROLE_UPDATE = "org.alfresco.site.group-role-changed";
    public final String SUBSCRIPTIONS_SUBSCRIBE = "org.alfresco.subscriptions.subscribed";
    public final String SUBSCRIPTIONS_FOLLOW = "org.alfresco.subscriptions.followed";
}
