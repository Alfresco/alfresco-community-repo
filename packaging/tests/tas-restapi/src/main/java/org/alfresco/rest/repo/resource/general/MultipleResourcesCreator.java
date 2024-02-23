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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.requests.Node;
import org.alfresco.utility.model.RepoTestModel;
import org.alfresco.utility.model.TestModel;
import org.alfresco.utility.model.UserModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;

public abstract class MultipleResourcesCreator<RESOURCE extends TestModel, SELF extends MultiCreator<RESOURCE, ?>>
    implements MultiCreator<RESOURCE, SELF>
{

    protected UserModel user;
    protected List<String> names;
    protected List<String> aliases;

    protected abstract SELF self();

    public SELF withNames(String... names)
    {
        this.names = Stream.of(names).collect(Collectors.toList());
        return self();
    }

    public SELF withRandomNames(String... prefixes)
    {
        this.aliases = Stream.of(prefixes).toList();
        return withNames(Stream.of(prefixes).map(this::generateRandomNameWith).toArray(String[]::new));
    }

    public SELF withRandomNames(List<String> prefixes, List<String> suffixes)
    {
        this.aliases = prefixes;
        if (CollectionUtils.isEmpty(prefixes) || CollectionUtils.isEmpty(suffixes) || prefixes.size() != suffixes.size())
        {
            throw new IllegalArgumentException("Provided suffixes size is different from prefixes size");
        }

        AtomicInteger i = new AtomicInteger();
        return withNames(prefixes.stream()
            .map(this::generateRandomNameWith)
            .map(name -> name + suffixes.get(i.getAndIncrement()))
            .toArray(String[]::new)
        );
    }

    public SELF withRandomNames(int namesCount)
    {
        return withNames(IntStream.range(0, namesCount).mapToObj(i -> generateRandomName()).toArray(String[]::new));
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

    protected <T> T getOrNull(List<T> list, int index)
    {
        if (CollectionUtils.isEmpty(list))
        {
            return null;
        }

        if (index < 0 || index >= list.size())
        {
            return null;
        }

        return list.get(index);
    }
}
