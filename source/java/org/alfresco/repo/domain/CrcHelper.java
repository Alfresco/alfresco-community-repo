
package org.alfresco.repo.domain;

import java.io.UnsupportedEncodingException;
import java.util.zip.CRC32;

import org.alfresco.util.Pair;

/**
 * Helper class to calculate CRC values for string persistence.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class CrcHelper
{
    public static final String EMPTY_STRING = ".empty";
    
    /**
     * Calculate a persistable, unique pair of values that can be persisted in a database unique
     * key and guarantee correct case-sensitivity.
     * <p>
     * While the short-string version of the value is always lowercase, the CRC is
     * calculated from the virgin string if case-sensitivity is enforced; in the case-insensitive
     * case, the CRC is calculated from a lowercase version of the string.
     * <p>
     * If the value is an empty string, then {@link #EMPTY_STRING} is used instead.  This ensures
     * that persisted values don't fall foul of the Oracle empty string comparison "behaviour" i.e
     * you should never persist an empty string in Oracle as it equates to a SQL <b>NULL</b>.
     * 
     * @param value             the raw value that will be persisted
     * @param dataLength        the maximum number of characters that can be persisted
     * @param useCharsFromStart <tt>true</tt> if the shortened string value must be made from
     *                          the first characters of the string or <tt>false</tt> to use
     *                          characters from the end of the string.
     * @param caseSensitive     <tt>true</tt> if the resulting pair must be case-sensitive or
     *                          <tt>false</tt> if the pair must be case-insensitive.
     * @return                  Return the persistable pair.  The result will never be <tt>null</tt>,
     *                          but the individual pair values will be <tt>null</tt> if the
     *                          value given is <tt>null</tt>
     */
    public static Pair<String, Long> getStringCrcPair(
            String value,
            int dataLength,
            boolean useCharsFromStart,
            boolean caseSensitive)
    {
        String valueLowerCase;
        if (value == null)
        {
            return new Pair<String, Long>(null, null);
        }
        else if (value.length() == 0)
        {
            value = CrcHelper.EMPTY_STRING;
            valueLowerCase = value;
        }
        else
        {
            valueLowerCase = value.toLowerCase();
        }
        Long valueCrc;
        try
        {
            CRC32 crc = new CRC32();
            if (caseSensitive)
            {
                crc.update(value.getBytes("UTF-8"));
            }
            else
            {
                crc.update(valueLowerCase.getBytes("UTF-8"));
            }
            valueCrc = crc.getValue();
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("UTF-8 encoding is not supported");
        }
        // Get the short value (case-sensitive or not)
        String valueShort = null;
        int valueLen = valueLowerCase.length();
        if (valueLen < dataLength)
        {
            valueShort = valueLowerCase;
        }
        else if (useCharsFromStart)
        {
            valueShort = valueLowerCase.substring(0, dataLength - 1);
        }
        else
        {
            valueShort = valueLowerCase.substring(valueLen - dataLength);
        }
        return new Pair<String, Long>(valueShort, valueCrc);
    }
}
