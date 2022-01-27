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

package org.alfresco.module.org_alfresco_module_rm.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Utility class with policy helper methods.
 *
 * @author Roy Wetherall
 */
public final class PoliciesUtil
{
    private PoliciesUtil()
    {
        // Will not be called
    }

    /**
     * Get all aspect and node type qualified names
     *
     * @param nodeRef
     *            the node we are interested in
     * @return Returns a set of qualified names containing the node type and all
     *         the node aspects, or null if the node no longer exists
     */
    public static Set<QName> getTypeAndAspectQNames(final NodeService nodeService, final NodeRef nodeRef)
    {
        return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Set<QName>>()
        {
            public Set<QName> doWork()
            {
                Set<QName> qnames = null;
                try
                {
                    Set<QName> aspectQNames = nodeService.getAspects(nodeRef);

                    QName typeQName = nodeService.getType(nodeRef);

                    qnames = new HashSet<>(aspectQNames.size() + 1);
                    qnames.addAll(aspectQNames);
                    qnames.add(typeQName);
                }
                catch (InvalidNodeRefException e)
                {
                    qnames = Collections.emptySet();
                }
                // done
                return qnames;
            }
        });
    }
}
