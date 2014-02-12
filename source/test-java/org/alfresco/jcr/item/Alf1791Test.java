package org.alfresco.jcr.item;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.nodetype.NodeType;

import org.alfresco.jcr.test.BaseJCRTest;
import org.alfresco.test_category.LegacyCategory;
import org.junit.experimental.categories.Category;

@Category(LegacyCategory.class)
public class Alf1791Test extends BaseJCRTest
{

    protected Session superuserSession;
    protected Node node;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        SimpleCredentials superuser = new SimpleCredentials("superuser", "".toCharArray());
        superuserSession = repository.login(superuser, getWorkspace());
        Node rootNode = superuserSession.getRootNode();
        node = rootNode.addNode("alf1791", "cm:content");
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        node.remove();
        superuserSession.logout();
        super.tearDown();
    }


    public void testAlf1791()
        throws Exception
    {
        final String mixPrefix = node.getSession().getNamespacePrefix("http://www.jcp.org/jcr/mix/1.0");
        final String mixReferenceable = mixPrefix + ":referenceable";
        final String sysPrefix = node.getSession().getNamespacePrefix("http://www.alfresco.org/model/system/1.0");
        final String sysReferenceable = sysPrefix + ":referenceable";

        node.addMixin(mixReferenceable);
        if (!hasMixin(node, mixReferenceable))
        {
            throw new RepositoryException("Node just made 'mix:referenceable' isn't! ('sys:referenceable'=" + hasMixin(node, sysReferenceable) + ")");
        }
    }

    
    public static final boolean hasMixin(final Node node, final String mixinName)
        throws RepositoryException
    {
        final NodeType[] mixinNodeTypes = node.getMixinNodeTypes();
        if (mixinNodeTypes == null)
            return false;
        for (NodeType mixinNodeType : mixinNodeTypes)
        {
          if (mixinNodeType == null)
              continue;
          if (mixinName.equals(mixinNodeType.getName()))
              return true;
        }
        return false;
    }
}
