/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.search.impl.lucene.analysis;

/**
 * Support to encode numeric types in the lucene index.
 * 
 * To support range queries in the lucene index numeric types need to be indexed
 * specially. This has been addressed for int and long types for lucene and
 * limited support (via scaling) for float and double.
 * 
 * The implementation splits an int, long, float or double into the sign bit,
 * optional exponent and mantissa either from the int or long format or its IEEE
 * 754 byte representation.
 * 
 * To index content so small negative numbers are indexed correctly and are
 * after big negative numbers in range queries.
 * 
 * The algorithm finds the sign, if the number is negative, then the mantissa
 * and exponent are XORed against the appropriate masks. This reverses the
 * order. As negative numbers appear first in the list their sign bit is 0 and
 * positive numbers are 1.
 * 
 * @author Andy Hind
 */
public class NumericEncoder
{
    /*
     * Constants for integer encoding
     */

    static int INTEGER_SIGN_MASK = 0x80000000;

    /*
     * Constants for long encoding
     */

    static long LONG_SIGN_MASK = 0x8000000000000000L;

    /*
     * Constants for float encoding
     */

    static int FLOAT_SIGN_MASK = 0x80000000;

    static int FLOAT_EXPONENT_MASK = 0x7F800000;

    static int FLOAT_MANTISSA_MASK = 0x007FFFFF;

    /*
     * Constants for double encoding
     */

    static long DOUBLE_SIGN_MASK = 0x8000000000000000L;

    static long DOUBLE_EXPONENT_MASK = 0x7FF0000000000000L;

    static long DOUBLE_MANTISSA_MASK = 0x000FFFFFFFFFFFFFL;

    private NumericEncoder()
    {
        super();
    }

    /**
     * Encode an integer into a string that orders correctly using string
     * comparison Integer.MIN_VALUE encodes as 00000000 and MAX_VALUE as
     * ffffffff.
     * 
     * @param intToEncode
     * @return
     */
    public static String encode(int intToEncode)
    {
        int replacement = intToEncode ^ INTEGER_SIGN_MASK;
        return encodeToHex(replacement);
    }

    /**
     * Encode a long into a string that orders correctly using string comparison
     * Long.MIN_VALUE encodes as 0000000000000000 and MAX_VALUE as
     * ffffffffffffffff.
     * 
     * @param longToEncode
     * @return
     */
    public static String encode(long longToEncode)
    {
        long replacement = longToEncode ^ LONG_SIGN_MASK;
        return encodeToHex(replacement);
    }

    /**
     * Encode a float into a string that orders correctly according to string
     * comparison. Note that there is no negative NaN but there are codings that
     * imply this. So NaN and -Infinity may not compare as expected.
     * 
     * @param floatToEncode
     * @return
     */
    public static String encode(float floatToEncode)
    {
        int bits = Float.floatToIntBits(floatToEncode);
        int sign = bits & FLOAT_SIGN_MASK;
        int exponent = bits & FLOAT_EXPONENT_MASK;
        int mantissa = bits & FLOAT_MANTISSA_MASK;
        if (sign != 0)
        {
            exponent ^= FLOAT_EXPONENT_MASK;
            mantissa ^= FLOAT_MANTISSA_MASK;
        }
        sign ^= FLOAT_SIGN_MASK;
        int replacement = sign | exponent | mantissa;
        return encodeToHex(replacement);
    }

    /**
     * Encode a double into a string that orders correctly according to string
     * comparison. Note that there is no negative NaN but there are codings that
     * imply this. So NaN and -Infinity may not compare as expected.
     * 
     * @param doubleToEncode
     * @return
     */
    public static String encode(double doubleToEncode)
    {
        long bits = Double.doubleToLongBits(doubleToEncode);
        long sign = bits & DOUBLE_SIGN_MASK;
        long exponent = bits & DOUBLE_EXPONENT_MASK;
        long mantissa = bits & DOUBLE_MANTISSA_MASK;
        if (sign != 0)
        {
            exponent ^= DOUBLE_EXPONENT_MASK;
            mantissa ^= DOUBLE_MANTISSA_MASK;
        }
        sign ^= DOUBLE_SIGN_MASK;
        long replacement = sign | exponent | mantissa;
        return encodeToHex(replacement);
    }

    private static String encodeToHex(int i)
    {
        char[] buf = new char[] { '0', '0', '0', '0', '0', '0', '0', '0' };
        int charPos = 8;
        do
        {
            buf[--charPos] = DIGITS[i & MASK];
            i >>>= 4;
        }
        while (i != 0);
        return new String(buf);
    }

    private static String encodeToHex(long l)
    {
        char[] buf = new char[] { '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0' };
        int charPos = 16;
        do
        {
            buf[--charPos] = DIGITS[(int) l & MASK];
            l >>>= 4;
        }
        while (l != 0);
        return new String(buf);
    }

    private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
            'f' };

    private static final int MASK = (1 << 4) - 1;
}
