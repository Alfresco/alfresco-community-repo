package org.alfresco.rest.repo.resource.general;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.requests.Node;
import org.alfresco.utility.model.RepoTestModel;
import org.alfresco.utility.model.TestModel;
import org.alfresco.utility.model.UserModel;
import org.apache.commons.collections.CollectionUtils;

@SuppressWarnings({"PMD.GenericsNaming"})
public abstract class ResourceModifier<RESOURCE extends TestModel, SELF extends Modifier<RESOURCE, ?>>
    implements Modifier<RESOURCE, SELF>
{

    protected UserModel user;
    protected List<String> includes;

    protected abstract SELF self();

    @Override
    public SELF asUser(UserModel user)
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
