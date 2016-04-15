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
package org.alfresco.repo.security.permissions.impl.acegi;

import java.util.Iterator;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.AccessStatus;

import net.sf.acegisecurity.ConfigAttributeDefinition;
import net.sf.acegisecurity.vote.AccessDecisionVoter;
import net.sf.acegisecurity.vote.AffirmativeBased;

public class AffirmativeBasedAccessDecisionManger extends AffirmativeBased
{
    public AccessStatus pre(Object object, ConfigAttributeDefinition attr)
    {
        Iterator iter = this.getDecisionVoters().iterator();
        int deny = 0;

        while (iter.hasNext())
        {
            AccessDecisionVoter voter = (AccessDecisionVoter) iter.next();
            int result = voter.vote(AuthenticationUtil.getFullAuthentication(), object, attr);

            switch (result)
            {
            case AccessDecisionVoter.ACCESS_GRANTED:
                return AccessStatus.ALLOWED;

            case AccessDecisionVoter.ACCESS_DENIED:
                deny++;

                break;

            default:
                break;
            }
        }

        if (deny > 0)
        {
            return AccessStatus.DENIED;
        }

        // To get this far, every AccessDecisionVoter abstained
        if (this.isAllowIfAllAbstainDecisions())
        {
            return AccessStatus.ALLOWED;
        }
        else
        {
            return AccessStatus.DENIED;
        }

    }
}
