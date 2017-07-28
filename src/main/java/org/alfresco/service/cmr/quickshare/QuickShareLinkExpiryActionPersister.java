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

package org.alfresco.service.cmr.quickshare;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * This interface defines the persistence and the retrieval of {@link QuickShareLinkExpiryAction} actions.
 *
 * @author Jamal Kaabi-Mofrad
 */
public interface QuickShareLinkExpiryActionPersister
{
    /**
     * Serializes the {@link QuickShareLinkExpiryAction} and stores it in
     * the repository. The {@link QuickShareLinkExpiryAction}s saved in this way maybe
     * retrieved using the <code>load()</code> method.
     *
     * @param linkExpiryAction The {@link QuickShareLinkExpiryAction} to be persisted.
     */
    void saveQuickShareLinkExpiryAction(QuickShareLinkExpiryAction linkExpiryAction);

    /**
     * Retrieves a {@link QuickShareLinkExpiryAction} that has been stored
     * in the repository using the <code>save()</code> method. If no
     * {@link QuickShareLinkExpiryAction} exists in the repository with the specified
     * QName then this method returns null.
     *
     * @param linkExpiryActionName The unique identifier used to specify the
     *                             {@link QuickShareLinkExpiryAction} to retrieve.
     * @return The NodeRef of the specified {@link QuickShareLinkExpiryAction} or null.
     */
    NodeRef getQuickShareLinkExpiryActionNode(QName linkExpiryActionName);

    /**
     * Retrieves a {@link QuickShareLinkExpiryAction} that has been stored
     * in the repository using the <code>save()</code> method. If no
     * {@link QuickShareLinkExpiryAction} exists in the repository with the specified
     * QName then this method returns null.
     *
     * @param linkExpiryActionName The unique identifier used to specify the
     *                             {@link QuickShareLinkExpiryAction} to retrieve.
     * @return The specified {@link QuickShareLinkExpiryAction} or null.
     */
    QuickShareLinkExpiryAction loadQuickShareLinkExpiryAction(QName linkExpiryActionName);

    /**
     * Retrieves a {@link QuickShareLinkExpiryAction} that has been stored
     * in the repository using the <code>save()</code> method. If no
     * {@link QuickShareLinkExpiryAction} exists in the repository with the specified
     * QName then this method returns null.
     *
     * @param linkExpiryActionNodeRef The nodeRef of the
     *                                {@link QuickShareLinkExpiryAction} to retrieve.
     * @return The specified {@link QuickShareLinkExpiryAction} or null.
     */
    QuickShareLinkExpiryAction loadQuickShareLinkExpiryAction(NodeRef linkExpiryActionNodeRef);

    /**
     * Removes the previously serialized {@link QuickShareLinkExpiryAction}
     * from the repository. The {@link QuickShareLinkExpiryAction} will then no longer
     * be available using the load methods.
     *
     * @param linkExpiryAction The {@link QuickShareLinkExpiryAction} to be deleted.
     */
    void deleteQuickShareLinkExpiryAction(QuickShareLinkExpiryAction linkExpiryAction);
}
