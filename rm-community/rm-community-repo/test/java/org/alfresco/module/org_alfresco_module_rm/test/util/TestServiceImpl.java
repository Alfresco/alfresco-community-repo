package org.alfresco.module.org_alfresco_module_rm.test.util;

import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * @author Roy Wetherall
 * @since 2.1
 */
public class TestServiceImpl extends ServiceBaseImpl implements TestService
{
    @Override
    public void testMethodOne(NodeRef nodeRef)
    {
    }

    @Override
    public void testMethodTwo(NodeRef nodeRef)
    {
    }
    
    public boolean doInstanceOf(NodeRef nodeRef, QName ofClassName)
    {
        return instanceOf(nodeRef, ofClassName);
    }
    
    public int doGetNextCount(NodeRef nodeRef)
    {
        return getNextCount(nodeRef);
    }

    public Set<QName> doGetTypeAndApsects(NodeRef nodeRef)
    {
        return getTypeAndApsects(nodeRef);
    }
}
