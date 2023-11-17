/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.rest.repo.resource.general;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.requests.Node;
import org.alfresco.utility.model.RepoTestModel;
import org.alfresco.utility.model.TestModel;
import org.alfresco.utility.model.UserModel;

@SuppressWarnings({"PMD.GenericsNaming"})
public abstract class ResourceModifier<RESOURCE extends TestModel, SELF extends Modifier<RESOURCE, ?>>
    implements Modifier<RESOURCE, SELF>
{

    protected UserModel user;

    protected abstract SELF self();

    @Override
    public SELF asUser(UserModel user)
    {
        this.user = user;
        return self();
    }

    protected Node buildNodeRestRequest(RestWrapper restClient, RepoTestModel node)
    {
        return restClient.authenticateUser(user).withCoreAPI().usingNode(node);
    }
}
