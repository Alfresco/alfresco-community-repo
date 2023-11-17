package org.alfresco.rest.repo.resource.general;

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

@SuppressWarnings({"PMD.GenericsNaming"})
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
        return prefix + UUID.randomUUID();
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
