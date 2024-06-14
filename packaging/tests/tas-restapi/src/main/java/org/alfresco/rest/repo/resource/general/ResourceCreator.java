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

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.UUID;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.requests.Node;
import org.alfresco.utility.model.RepoTestModel;
import org.alfresco.utility.model.TestModel;
import org.alfresco.utility.model.UserModel;
import org.apache.commons.lang3.RandomStringUtils;

public abstract class ResourceCreator<RESOURCE extends TestModel, SELF extends Creator<RESOURCE, ?>>
    implements Creator<RESOURCE, SELF>
{

    protected UserModel user;
    protected String name;
    protected String alias;

    protected abstract SELF self();

    @Override
    public SELF withName(String name)
    {
        this.name = name;
        return self();
    }

    public SELF withAlias(String alias)
    {
        this.alias = alias;
        return self();
    }

    public SELF withRandomName()
    {
        return withName(generateRandomName());
    }

    public SELF withRandomName(String prefix)
    {
        return withName(generateRandomNameWith(prefix));
    }

    public SELF withRandomName(String prefix, String suffix)
    {
        return withName(generateRandomNameWith(prefix, suffix));
    }

    @Override
    public <USER extends UserModel> SELF asUser(USER user)
    {
        this.user = user;
        return self();
    }

    protected String generateRandomName()
    {
        return generateRandomNameWith(RandomStringUtils.randomAlphanumeric(5) + "_");
    }

    protected String generateRandomNameWith(String prefix)
    {
        return generateRandomNameWith(prefix, EMPTY);
    }

    protected String generateRandomNameWith(String prefix, String suffix)
    {
        return prefix + UUID.randomUUID() + suffix;
    }

    protected Node buildNodeRestRequest(RestWrapper restClient, RepoTestModel node)
    {
        return restClient.authenticateUser(user).withCoreAPI().usingNode(node);
    }
}
