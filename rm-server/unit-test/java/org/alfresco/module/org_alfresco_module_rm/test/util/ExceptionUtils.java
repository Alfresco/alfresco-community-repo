/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Utility class to help with Java exceptions, particularly in test code.
 *
 * @since 3.0
 * @author Neil Mc Erlean
 */
public class ExceptionUtils
{
    /** This represents a situation where a throwable of an unexpected type was thrown. */
    public static class UnexpectedThrowableException extends RuntimeException
    {
        private final Class<? extends Throwable> expected;
        private final Throwable                  actual;

        public UnexpectedThrowableException(Class<? extends Throwable> expected, Throwable actual)
        {
            this.expected = expected;
            this.actual   = actual;
        }

        public Class<? extends Throwable> getExpected() { return this.expected; }
        public Throwable getActual() { return this.actual; }

        @Override public String toString()
        {
            return String.join("", "Expected ", expected.getSimpleName(), " but ",
                               actual.getClass().getSimpleName(), " was thrown.");
        }
    }

    /** This represents a situation where an expected throwable was not thrown. */
    public static class MissingThrowableException extends RuntimeException
    {
        private final Class<? extends Throwable> expected;

        public MissingThrowableException(Class<? extends Throwable> expected)
        {
            this.expected = expected;
        }

        public Class<? extends Throwable> getExpected() { return this.expected; }
        @Override public String toString()
        {
            return String.join("", "Expected ", expected.getSimpleName(), " but nothing was thrown.");
        }
    }

    /**
     * Utility method to help with expected exceptions in test code. This can be used in place of {@code try/catch}
     * blocks within test code and can sometimes make code more readable.
     * A single expected exception would usually be let escape from the test method and be handled e.g. by JUnit's
     * {@code @Test(expected="Exception.class")} pattern.
     * However if you have multiple expected exceptions in a sequence, you need to either add a sequence of
     * {@code try/catch} or use this method.
     * <p/>
     * Examples:
     * <ul>
     *     <li>
     *         Calling a local method which throws a {@code RuntimeException}. (An expression lambda)
     *         <pre>
     * intercept(RuntimeException.class, () -> badMethod() );
     *         </pre>
     *     </li>
     *     <li>
     *         Executing a block of code. (Requires return statement)
     *         <pre>
     * intercept(RuntimeException.class, () -> {
     *   for (int i = 0; i < 10; i++) {
     *     goodMethod();
     *   }
     *   badMethod();
     *   return "result";
     * });
     *         </pre>
     *     </li>
     *     <li>
     *         Examining the expected exception e.g. to assert the root cause is correct.
     *         <pre>
     * UnsupportedOperationException e = intercept(UnsupportedOperationException.class, () -> badMethod2() );
     * assertEquals(RuntimeException.class, e.getCause().getClass());
     *         </pre>
     *     </li>
     *     <li>
     *         Note that if your lambda expression returns 'void' then you cannot use an expression
     *         and must explicitly return null from a lambda block.
     *         <pre>
     * intercept(Exception.class, () -> { methodReturningVoid(); return null; } );
     * intercept(Exception.class, () -> { methodReturningVoid("parameter"); return null; } );
     *         </pre>
     *     </li>
     * </ul>
     *
     * @param expected the class of the expected throwable (subtypes will also match).
     * @param code     a lambda containing the code block which should throw the expected throwable.
     * @param <R>      the return type of the code block (which should not matter as it should not complete).
     * @param <T>      the type of the expected throwable (subtypes will also match).
     * @return the expected throwable object if it was thrown.
     * @throws UnexpectedThrowableException if a non-matching throwable was thrown out of the code block.
     * @throws MissingThrowableException if the expected throwable was not thrown out of the code block.
     */
    public static <R, T extends Throwable> T intercept(final Class<T> expected, final Supplier<R> code)
    {
        // The code block may throw an exception or it may not.
        Optional<Throwable> maybeThrownByCode;

        try
        {
            // evaluate the lambda
            code.get();

            // It didn't throw an exception.
            maybeThrownByCode = Optional.empty();
        }
        catch (Throwable t)
        {
            maybeThrownByCode = Optional.of(t);
        }

        Throwable thrownByCode = maybeThrownByCode.orElseThrow(() -> new MissingThrowableException(expected));

        if (expected.isAssignableFrom(thrownByCode.getClass()))
        {
            return (T)thrownByCode;
        }
        else
        {
            throw new UnexpectedThrowableException(expected, thrownByCode);
        }
    }
}