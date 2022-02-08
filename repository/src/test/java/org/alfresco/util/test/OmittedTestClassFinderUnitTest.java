/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
import static org.reflections.scanners.Scanners.MethodsAnnotated;
import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.scanners.Scanners.TypesAnnotated;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import junit.framework.TestCase;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.testing.category.NonBuildTests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.Suite.SuiteClasses;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.Scanners;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

public class OmittedTestClassFinderUnitTest
{
    /**
     * Test to look for tests which are unintentionally skipped by our CI.
     * <p>
     * In particular we look for classes that contain @Test methods or extend TestCase and which are not referenced by TestSuites.  There
     * are a few subtleties to this:
     * <ul>
     *   <li>alfresco-core and alfresco-data-model don't use test suites, and some @Test methods are executed via inheritance;</li>
     *   <li>some tests are explicitly marked as NonBuildTests;</li>
     *   <li>we assume that all test suite classes have names ending in "TestSuite".</li>
     * </ul>
     */
    @Test
    public void checkTestClassesReferencedInTestSuites()
    {
        // We assume that all of our tests are in org.alfresco.
        Reflections reflections = new Reflections("org.alfresco", MethodsAnnotated, TypesAnnotated, SubTypes);

        // Find the test classes which are not in test suites.
        Set<String> testClasses =  getTestClassesOnPath(reflections);
        Set<String> classesReferencedByTestSuites = getClassesReferencedByTestSuites(reflections);
        SetView<String> unreferencedTests = Sets.difference(testClasses, classesReferencedByTestSuites);

        // Filter out tests which are in Maven modules that don't use test suites (alfresco-core and alfresco-data-model).
        // Also filter any test classes contained in test dependencies (*.jar).
        Set<Class> unreferencedTestClasses = unreferencedTests.stream()
                                                              .map(this::classFromCanonicalName)
                                                              .filter(clazz -> {
                                                                  String path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
                                                                  return !path.endsWith("/data-model/target/test-classes/")
                                                                      && !path.endsWith("/core/target/test-classes/")
                                                                      && !path.endsWith(".jar");
                                                              })
                                                              .collect(toSet());

        System.out.println("Unreferenced test class count: " + unreferencedTestClasses.size());
        unreferencedTestClasses.forEach(System.out::println);

        assertEquals("Found test classes which are not referenced by any test suite.", emptySet(), unreferencedTestClasses);
    }

    /**
     * Find all test classes.  We define a class to be a test class if it contains a Test, Before or After annotation, is not a test suite,
     * is not abstract and is not a "non-build" test (e.g. the test class is marked as a performance test).
     * @param reflections The Reflections object used to provide information about the classes.
     * @return A set of canonical names for the test classes.
     */
    private Set<String> getTestClassesOnPath(Reflections reflections)
    {
        Set<String> classesWithTestAnnotations = Stream.of(Test.class, Before.class, After.class)
                                                       .map(annotation -> findClassesWithMethodAnnotation(reflections, annotation))
                                                       .flatMap(Set::stream)
                                                       .collect(toSet());

        Set<String> classesExtendingTestCase = reflections.getSubTypesOf(TestCase.class).stream().map(testClass -> testClass.getCanonicalName()).collect(toSet());

        return Sets.union(classesWithTestAnnotations, classesExtendingTestCase).stream()
                   // Exclude test suite classes.
                   .filter(className -> !className.endsWith("Suite"))
                   // Exclude abstract classes.
                   .filter(className -> !Modifier.isAbstract(classFromCanonicalName(className).getModifiers()))
                   // Exclude test classes which are explicitly marked as "non-build" test classes.
                   .filter(className -> !markedAsNonBuildTest(classFromCanonicalName(className)))
                   .collect(toSet());
    }

    /**
     * Several tests are intentionally excluded from the build. These are marked with the {@link Category} annotation referencing an
     * interface that extends {@link NonBuildTests}. This is useful for e.g. performance testing or to help with debugging.
     * @param clazz The test class to check.
     * @return true if the test class has been marked with a NonBuildTests category.
     */
    private boolean markedAsNonBuildTest(Class<?> clazz)
    {
        Category category = clazz.getAnnotation(Category.class);
        if (category == null)
        {
            return false;
        }
        return Arrays.stream(category.value())
                      .anyMatch(value -> NonBuildTests.class.isAssignableFrom(value));
    }

    /**
     * Get all the test classes referenced from test suites.
     * @param reflections The Reflections object used to provide information about the classes.
     * @return The set of canonical names of test classes referenced by test suites.
     */
    private Set<String> getClassesReferencedByTestSuites(Reflections reflections)
    {
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
        return classesReferencedByTestSuites;
    }

    /**
     * Find the names of classes with the given annotation.
     * @param reflections The Reflections object used to provide information about the classes.
     * @param annotation The class of the annotation to look for.
     * @return The set of canonical names of classes containing methods annotated with the annotation.
     */
    private Set<String> findClassesWithMethodAnnotation(Reflections reflections, Class<? extends Annotation> annotation)
    {
        return reflections.getMethodsAnnotatedWith(annotation)
                          .stream()
                          .map(Method::getDeclaringClass)
                          .flatMap(c -> Stream.concat(Stream.of(c), reflections.getSubTypesOf(c).stream()))
                          .map(Class::getCanonicalName)
                          .filter(Objects::nonNull)
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
