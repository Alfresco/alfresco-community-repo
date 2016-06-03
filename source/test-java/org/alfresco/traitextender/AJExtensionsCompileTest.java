
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
