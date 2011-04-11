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
package org.alfresco.repo.usage;

import org.alfresco.service.cmr.admin.RepoUsage;
import org.alfresco.service.cmr.admin.RepoUsageStatus;
import org.alfresco.service.cmr.admin.RepoUsage.UsageType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Low-level interface to answer repository usage queries
 * 
 * @author Derek Hulley
 * @since 3.5
 */
public interface RepoUsageComponent
{
    public static final Long LOCK_TTL = 60000L;
    public static final QName LOCK_USAGE = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "RepoUsageComponent");
    public static final QName LOCK_USAGE_USERS = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "RepoUsageComponent.Users");
    public static final QName LOCK_USAGE_DOCUMENTS = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "RepoUsageComponent.Documents");
    
    public static final String KEY_USAGE_ROOT = ".repoUsages";
    public static final String KEY_USAGE_CURRENT = "current";
    public static final String KEY_USAGE_LAST_UPDATE_USERS = "lastUpdateUsers";
    public static final String KEY_USAGE_USERS = "users";
    public static final String KEY_USAGE_LAST_UPDATE_DOCUMENTS = "lastUpdateDocuments";
    public static final String KEY_USAGE_DOCUMENTS = "documents";
    
    /**
     * Observe when restrictions change
     */
    void observeRestrictions(RestrictionObserver observer);
    
    /**
     * Interface for observers of repository restriction changes
     */
    public interface RestrictionObserver
    {
        /**
         * Called when restrictions are changed
         * 
         * @param restrictions          the new restrictions
         */
        void onChangeRestriction(RepoUsage restrictions);
    };
    
    /**
     * Record changes to the restrictions imposed on the repository.  These <i>may</i> be cached
     * for fast access.  This method should only be called if the {@link #getRestrictions() current restrictions}
     * have changed.
     * 
     * @param restrictions          the new restrictions imposed on the repository
     */
    void setRestrictions(RepoUsage restrictions);
    
    /**
     * @return                      Returns the restrictions currently in play for the repository
     */
    RepoUsage getRestrictions();
    
    /**
     * Force an update of the current repository usage.  Usage data will be gathered and
     * stored as required.
     * 
     * @param usageType             the type of usage data that must be updated
     * @return                      <tt>true</tt> if the update succeeded or <tt>false</tt> if
     *                              some other client was already performing the same update
     */
    boolean updateUsage(UsageType usageType);
    
    /**
     * Get the current repository usage data.  This will not trigger an update of the data if it
     * is not available; only {@link #updateUsage() pre-loaded data} will be used.
     * 
     * @return                      Returns the repository-specific current usage data.
     */
    RepoUsage getUsage();
    
    /**
     * Calculate and retrieve full status alerts based on the usage and license expiry state.
     * 
     * @return              Returns the usage status bean
     */
    RepoUsageStatus getUsageStatus();
}
