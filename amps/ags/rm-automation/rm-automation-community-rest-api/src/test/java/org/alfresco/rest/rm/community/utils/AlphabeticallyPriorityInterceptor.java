package org.alfresco.rest.rm.community.utils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

public class AlphabeticallyPriorityInterceptor implements IMethodInterceptor
{
    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods,
        ITestContext context)
    {
        return methods.stream().sorted(Comparator.comparing(el -> el.getMethod().getTestClass().toString()))
            .collect(Collectors.toList());
    }
}