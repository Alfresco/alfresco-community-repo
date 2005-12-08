/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.search.impl.lucene.analysis;

import junit.framework.TestCase;

public class NumericEncodingTest extends TestCase
{

    public NumericEncodingTest()
    {
        super();
    }

    public NumericEncodingTest(String arg0)
    {
        super(arg0);
    }

    /**
     * Do an exhaustive test for integers
     * 
     */
    public void xtestAllIntegerEncodings()
    {
        String lastString = null;
        String nextString = null;
        for (long i = Integer.MIN_VALUE; i <= Integer.MAX_VALUE; i++)
        {
            nextString = NumericEncoder.encode((int) i);
            if (lastString != null)
            {
                assertFalse(lastString.compareTo(nextString) > 0);
            }
            lastString = nextString;
        }
    }

    /**
     * Do an exhaustive test for float
     * 
     */
    public void xtestAllFloatEncodings()
    {
        Float last = null;
        Float next = null;
        String lastString = null;
        String nextString = null;

        for (int sign = 1; sign >= 0; sign--)
        {
            if (sign == 0)
            {
                for (int exponent = 0; exponent <= 0xFF; exponent++)
                {
                    for (int mantissa = 0; mantissa <= 0x007FFFFF; mantissa++)
                    {
                        int bitPattern = sign << 31 | exponent << 23 | mantissa;
                        next = Float.intBitsToFloat(bitPattern);

                        if (!next.equals(Float.NaN) && (last != null) && (last.compareTo(next) > 0))
                        {
                            System.err.println(last + " > " + next);
                        }
                        if (!next.equals(Float.NaN))
                        {
                            nextString = NumericEncoder.encode(next);
                            if ((lastString != null) && (lastString.compareTo(nextString) > 0))
                            {
                                System.err.println(lastString + " > " + nextString);
                            }
                            lastString = nextString;
                        }
                        last = next;

                    }
                }
            }
            else
            {
                for (int exponent = 0xFF; exponent >= 0; exponent--)
                {
                    for (int mantissa = 0x007FFFFF; mantissa >= 0; mantissa--)
                    {
                        int bitPattern = sign << 31 | exponent << 23 | mantissa;
                        next = Float.intBitsToFloat(bitPattern);
                        if (!next.equals(Float.NaN) && (last != null) && (last.compareTo(next) > 0))
                        {
                            System.err.println(last + " > " + next);
                        }
                        if (!next.equals(Float.NaN))
                        {
                            nextString = NumericEncoder.encode(next);
                            if ((lastString != null) && (lastString.compareTo(nextString) > 0))
                            {
                                System.err.println(lastString + " > " + nextString);
                            }
                            lastString = nextString;
                        }
                        last = next;
                    }
                }
            }
        }
    }

    /*
     * Sample test for int
     */

    public void testIntegerEncoding()
    {
        assertEquals("00000000", NumericEncoder.encode(Integer.MIN_VALUE));
        assertEquals("00000001", NumericEncoder.encode(Integer.MIN_VALUE + 1));
        assertEquals("7fffffff", NumericEncoder.encode(-1));
        assertEquals("80000000", NumericEncoder.encode(0));
        assertEquals("80000001", NumericEncoder.encode(1));
        assertEquals("fffffffe", NumericEncoder.encode(Integer.MAX_VALUE - 1));
        assertEquals("ffffffff", NumericEncoder.encode(Integer.MAX_VALUE));
    }

    /*
     * Sample test for long
     */

    public void testLongEncoding()
    {
        assertEquals("0000000000000000", NumericEncoder.encode(Long.MIN_VALUE));
        assertEquals("0000000000000001", NumericEncoder.encode(Long.MIN_VALUE + 1));
        assertEquals("7fffffffffffffff", NumericEncoder.encode(-1L));
        assertEquals("8000000000000000", NumericEncoder.encode(0L));
        assertEquals("8000000000000001", NumericEncoder.encode(1L));
        assertEquals("fffffffffffffffe", NumericEncoder.encode(Long.MAX_VALUE - 1));
        assertEquals("ffffffffffffffff", NumericEncoder.encode(Long.MAX_VALUE));      
    }

    /*
     * Sample test for float
     */

    public void testFloatEncoding()
    {
        assertEquals("007fffff", NumericEncoder.encode(Float.NEGATIVE_INFINITY));
        assertEquals("00800000", NumericEncoder.encode(-Float.MAX_VALUE));
        assertEquals("7ffffffe", NumericEncoder.encode(-Float.MIN_VALUE));
        assertEquals("7fffffff", NumericEncoder.encode(-0f));
        assertEquals("80000000", NumericEncoder.encode(0f));
        assertEquals("80000001", NumericEncoder.encode(Float.MIN_VALUE));
        assertEquals("ff7fffff", NumericEncoder.encode(Float.MAX_VALUE));
        assertEquals("ff800000", NumericEncoder.encode(Float.POSITIVE_INFINITY));
        assertEquals("ffc00000", NumericEncoder.encode(Float.NaN));

    }

    /*
     * Sample test for double
     */

    public void testDoubleEncoding()
    {
        assertEquals("000fffffffffffff", NumericEncoder.encode(Double.NEGATIVE_INFINITY));
        assertEquals("0010000000000000", NumericEncoder.encode(-Double.MAX_VALUE));
        assertEquals("7ffffffffffffffe", NumericEncoder.encode(-Double.MIN_VALUE));
        assertEquals("7fffffffffffffff", NumericEncoder.encode(-0d));
        assertEquals("8000000000000000", NumericEncoder.encode(0d));
        assertEquals("8000000000000001", NumericEncoder.encode(Double.MIN_VALUE));
        assertEquals("ffefffffffffffff", NumericEncoder.encode(Double.MAX_VALUE));
        assertEquals("fff0000000000000", NumericEncoder.encode(Double.POSITIVE_INFINITY));
        assertEquals("fff8000000000000", NumericEncoder.encode(Double.NaN));

    }
}
