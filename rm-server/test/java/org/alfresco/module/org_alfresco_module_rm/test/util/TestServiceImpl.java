/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
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
