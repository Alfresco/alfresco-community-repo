/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.util.test;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import static junit.framework.TestCase.assertEquals;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import junit.framework.TestCase;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.imap.LoadTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Suite.SuiteClasses;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

public class OmittedTestClassFinderUnitTest
{
    /** The set of test classes which we don't want to execute from test suites. */
    private Set<String> TEST_CLASSES_TO_IGNORE = Sets.newHashSet(
            // We don't want automated load testing as part of our CI.
            LoadTester.class
    ).stream().map(clazz -> clazz.getCanonicalName()).collect(toSet());

    @Test
    public void checkTestClassesReferencedInTestSuites()
    {
        Reflections reflections = new Reflections("org.alfresco", new MethodAnnotationsScanner(), new TypeAnnotationsScanner(), new SubTypesScanner());
        Set<String> classesWithTestAnnotations = Stream.of(Test.class, Before.class, After.class)
                                                       .map(annotation -> findClassesWithMethodAnnotation(reflections, annotation))
                                                       .flatMap(Set::stream)
                                                       .collect(toSet());
        findClassesWithMethodAnnotation(reflections, Test.class);

        Set<String> classesExtendingTestCase = reflections.getSubTypesOf(TestCase.class).stream().map(testClass -> testClass.getCanonicalName()).collect(toSet());

        Set<String> testClasses = Sets.union(classesWithTestAnnotations, classesExtendingTestCase).stream()
                                      // Exclude test suite classes.
                                      .filter(className -> !className.endsWith("Suite"))
                                      // Exclude classes that are specifically referenced to be ignored.
                                      .filter(className -> !TEST_CLASSES_TO_IGNORE.contains(className))
                                      .collect(toSet());

        Set<String> classesReferencedByTestSuites = new HashSet<>();
        for (Class testSuite : reflections.getTypesAnnotatedWith(SuiteClasses.class))
        {
            SuiteClasses testSuiteAnnotation = (SuiteClasses) testSuite.getAnnotation(SuiteClasses.class);
            Arrays.stream(testSuiteAnnotation.value())
                  .map(testClass -> testClass.getCanonicalName())
                  // Exclude nested test suite classes.
                  .filter(className -> !className.endsWith("Suite"))
                  .forEach(classesReferencedByTestSuites::add);
        }

        SetView<String> unreferencedTests = Sets.difference(testClasses, classesReferencedByTestSuites);

        // Filter out tests which are in Maven modules that don't use test suites (alfresco-core and alfresco-data-model).
        // Also filter any test classes contained in test dependencies (*.jar).
        // Filter any abstract classes too.
        Set<Class> unreferencedTestClasses = unreferencedTests.stream()
                                                              .map(this::classFromCanonicalName)
                                                              .filter(clazz -> {
                                                                  String path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
                                                                  return !path.endsWith("/data-model/target/test-classes/")
                                                                      && !path.endsWith("/core/target/test-classes/")
                                                                      && ! path.endsWith(".jar");
                                                              })
                                                              .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                                                              .collect(toSet());

        System.out.println("Unreferenced test class count: " + unreferencedTestClasses.size());
        unreferencedTestClasses.forEach(System.out::println);

        assertEquals("Found test classes which are not referenced by any test suite.", emptySet(), unreferencedTestClasses);
    }

    /**
     * Find the names of classes with the given annotation.
     * @param reflections The Reflections object used to provide information about the classes.
     * @param annotation The class of the annotation to look for.
     * @return The set of canonical names of classes containing methods annotated with the annotation.
     */
    private Set<String> findClassesWithMethodAnnotation(Reflections reflections, Class annotation)
    {
        return reflections.getStore()
                          .get(MethodAnnotationsScanner.class, annotation.getName())
                          .stream()
                          // Get the class name from the method name.
                          .map(methodName -> methodName.split("\\.[^\\.]+\\(")[0])
                          .collect(toSet());
    }

    /**
     * Find the Class corresponding to a canonical class name.
     * @param name The name of the class.
     * @return The Class object.
     */
    private Class<?> classFromCanonicalName(String name)
    {
        try
        {
            return Class.forName(name, false, getClass().getClassLoader());
        }
        catch (ClassNotFoundException e)
        {
            throw new AlfrescoRuntimeException("Couldn't find test class for name.", e);
        }
    }
}
