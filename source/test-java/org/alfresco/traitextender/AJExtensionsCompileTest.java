/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.traitextender;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.alfresco.traitextender.AJExtender.CompiledExtensible;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

public class AJExtensionsCompileTest extends TestCase
{
    protected void compile(Class<? extends Extensible> extensible) throws Exception
    {
        Set<Class<? extends Extensible>> extensiblesSet = new HashSet<>();
        extensiblesSet.add(extensible);
        compile(extensiblesSet);
    }

    protected void compile(Set<Class<? extends Extensible>> extensibles) throws Exception
    {
        StringBuilder errorString = new StringBuilder();
        boolean errorsFound = false;
        for (Class<? extends Extensible> extensible : extensibles)
        {
            CompiledExtensible ce = AJExtender.compile(extensible);
            if (ce.hasErrors())
            {
                errorsFound = true;
                errorString.append("Error compiling ");
                errorString.append(extensible);
                errorString.append(":\n");
                errorString.append(ce.getErrorsString());
                errorString.append(":\n");
            }
        }
        assertFalse(errorString.toString(),
                    errorsFound);
    }

    @Test
    public void testCompileExtendedServices() throws Exception
    {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true);
        provider.addIncludeFilter(new AssignableTypeFilter(Extensible.class));

        Set<BeanDefinition> components = provider.findCandidateComponents("org/alfresco/*");
        Set<Class<? extends Extensible>> extensibles = new HashSet<>();
        for (BeanDefinition component : components)
        {
            @SuppressWarnings("unchecked")
            Class<? extends Extensible> extensibleClass = (Class<? extends Extensible>) Class.forName(component
                        .getBeanClassName());
            extensibles.add(extensibleClass);

        }
        compile(extensibles);
    }
};
