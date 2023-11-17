package org.alfresco.rest.repo.resource.general;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.requests.Node;
import org.alfresco.utility.model.RepoTestModel;
import org.alfresco.utility.model.TestModel;
import org.alfresco.utility.model.UserModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;

@SuppressWarnings({"PMD.GenericsNaming"})
public abstract class ResourceCreator<RESOURCE extends TestModel, SELF extends Creator<RESOURCE, ?>>
    implements Creator<RESOURCE, SELF>
{

    protected UserModel user;
    protected List<String> includes;
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
        return withRandomName(prefix, EMPTY);
    }

    public SELF withRandomName(String prefix, String suffix)
    {
        return withName(generateRandomNameWith(prefix) + suffix);
    }

    @Override
    public <USER extends UserModel> SELF asUser(USER user)
    {
        this.user = user;
        return self();
    }

    @Override
    public SELF include(String... includes)
    {
        this.includes = Stream.of(includes).collect(Collectors.toList());
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
        Node restRequest = restClient.authenticateUser(user).withCoreAPI().usingNode(node);
        if (CollectionUtils.isNotEmpty(includes))
        {
            restRequest.include(includes.toArray(String[]::new));
        }

        return restRequest;
    }
}
