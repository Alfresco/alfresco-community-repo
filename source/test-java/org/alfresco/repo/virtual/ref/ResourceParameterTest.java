
package org.alfresco.repo.virtual.ref;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

import junit.framework.TestCase;

public class ResourceParameterTest extends TestCase
{
    @Test
    public void testResourceParameter() throws Exception
    {
        RepositoryResource repoNodeRefResource = new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/0029-222-333-444")));
        RepositoryResource repoPathResource = new RepositoryResource(new RepositoryPath("/Foo/Bar"));
        ClasspathResource classpathResource = new ClasspathResource("/org/alfresco/");
        ResourceParameter repoNodeRefResourceParam = new ResourceParameter(repoNodeRefResource);
        ResourceParameter repoPathResourceParam = new ResourceParameter(repoPathResource);
        ResourceParameter classpathResourceParam = new ResourceParameter(classpathResource);

        //
        assertEquals(repoNodeRefResource,
                     repoNodeRefResourceParam.getValue());

        String repoNodeRefResourceParamStrRepresentation = repoNodeRefResourceParam.stringify(new PlainStringifier());

        assertEquals("r:repository:node:workspace:SpacesStore:0029-222-333-444",
                     repoNodeRefResourceParamStrRepresentation);

        //
        assertEquals(repoPathResource,
                     repoPathResourceParam.getValue());

        String repoPathResourceParamStrRepresentation = repoPathResourceParam.stringify(new PlainStringifier());

        assertEquals("r:repository:path:/Foo/Bar",
                     repoPathResourceParamStrRepresentation);

        //

        assertEquals(classpathResource,
                     classpathResourceParam.getValue());

        String classpathResourceParamStrRepresentation = classpathResourceParam.stringify(new PlainStringifier());

        assertEquals("r:classpath:/org/alfresco/",
                     classpathResourceParamStrRepresentation);
    }
}
