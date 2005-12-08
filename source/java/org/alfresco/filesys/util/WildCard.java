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
package org.alfresco.filesys.util;

/**
 * Wildcard Utility Class.
 * <p>
 * The WildCard class may be used to check Strings against a wildcard pattern using the SMB/CIFS
 * wildcard rules.
 * <p>
 * A number of static convenience methods are also provided.
 * 
 * @author GKSpencer
 */
public final class WildCard
{

    // Multiple character wildcard

    public static final int MULTICHAR_WILDCARD = '*';

    // Single character wildcard

    public static final int SINGLECHAR_WILDCARD = '?';

    // Unicode wildcards
    //
    // The spec states :-
    // translate '?' to '>'
    // translate '.' to '"' if followed by a '?' or '*'
    // translate '*' to '<' if followed by a '.'

    public static final int SINGLECHAR_UNICODE_WILDCARD = '>';
    public static final int DOT_UNICODE_WILDCARD = '"';
    public static final int MULTICHAR_UNICODE_WILDCARD = '<';

    // Wildcard types

    public static final int WILDCARD_NONE = 0; // no wildcard characters present in pattern
    public static final int WILDCARD_ALL = 1; // '*.*' and '*'
    public static final int WILDCARD_NAME = 2; // '*.ext'
    public static final int WILDCARD_EXT = 3; // 'name.*'
    public static final int WILDCARD_COMPLEX = 4; // complex wildcard

    public static final int WILDCARD_INVALID = -1;

    // Wildcard pattern and type

    private String m_pattern;
    private int m_type;

    // Start/end string to match for name/extension matching

    private String m_matchPart;
    private boolean m_caseSensitive;

    // Complex wildcard pattern

    private char[] m_patternChars;

    /**
     * Default constructor
     */
    public WildCard()
    {
        setType(WILDCARD_INVALID);
    }

    /**
     * Class constructor
     * 
     * @param pattern String
     * @param caseSensitive boolean
     */
    public WildCard(String pattern, boolean caseSensitive)
    {
        setPattern(pattern, caseSensitive);
    }

    /**
     * Return the wildcard pattern type
     * 
     * @return int
     */
    public final int isType()
    {
        return m_type;
    }

    /**
     * Check if case sensitive matching is enabled
     * 
     * @return boolean
     */
    public final boolean isCaseSensitive()
    {
        return m_caseSensitive;
    }

    /**
     * Return the wildcard pattern string
     * 
     * @return String
     */
    public final String getPattern()
    {
        return m_pattern;
    }

    /**
     * Return the match part for wildcard name and wildcard extension type patterns
     * 
     * @return String
     */
    public final String getMatchPart()
    {
        return m_matchPart;
    }

    /**
     * Determine if the string matches the wildcard pattern
     * 
     * @param str String
     * @return boolean
     */
    public final boolean matchesPattern(String str)
    {

        // Check the pattern type and compare the string

        boolean sts = false;

        switch (isType())
        {

        // Match all wildcard

        case WILDCARD_ALL:
            sts = true;
            break;

        // Match any name

        case WILDCARD_NAME:
            if (isCaseSensitive())
            {

                // Check if the string ends with the required file extension

                sts = str.endsWith(m_matchPart);
            }
            else
            {

                // Normalize the string and compare

                String upStr = str.toUpperCase();
                sts = upStr.endsWith(m_matchPart);
            }
            break;

        // Match any file extension

        case WILDCARD_EXT:
            if (isCaseSensitive())
            {

                // Check if the string starts with the required file name

                sts = str.startsWith(m_matchPart);
            }
            else
            {

                // Normalize the string and compare

                String upStr = str.toUpperCase();
                sts = upStr.startsWith(m_matchPart);
            }
            break;

        // Complex wildcard matching

        case WILDCARD_COMPLEX:
            if (isCaseSensitive())
                sts = matchComplexWildcard(str);
            else
            {

                // Normalize the string and compare

                String upStr = str.toUpperCase();
                sts = matchComplexWildcard(upStr);
            }
            break;

        // No wildcard characters in pattern, compare strings

        case WILDCARD_NONE:
            if (isCaseSensitive())
            {
                if (str.compareTo(m_pattern) == 0)
                    sts = true;
            }
            else if (str.equalsIgnoreCase(m_pattern))
                sts = true;
            break;
        }

        // Return the wildcard match status

        return sts;
    }

    /**
     * Match a complex wildcard pattern with the specified string
     * 
     * @param str String
     * @return boolean
     */
    protected final boolean matchComplexWildcard(String str)
    {

        // Convert the string to a char array for matching

        char[] strChars = str.toCharArray();

        // Compare the string to the wildcard pattern

        int wpos = 0;
        int wlen = m_patternChars.length;

        int spos = 0;
        int slen = strChars.length;

        char patChar;
        boolean matchFailed = false;

        while (matchFailed == false && wpos < m_patternChars.length)
        {

            // Match the current pattern character

            patChar = m_patternChars[wpos++];

            switch (patChar)
            {

            // Match single character

            case SINGLECHAR_WILDCARD:
                if (spos < slen)
                    spos++;
                else
                    matchFailed = true;
                break;

            // Match zero or more characters

            case MULTICHAR_WILDCARD:

                // Check if there is another character in the wildcard pattern

                if (wpos < wlen)
                {

                    // Check if the character is not a wildcard character

                    patChar = m_patternChars[wpos];
                    if (patChar != SINGLECHAR_WILDCARD && patChar != MULTICHAR_WILDCARD)
                    {

                        // Find the required character in the string

                        while (spos < slen && strChars[spos] != patChar)
                            spos++;
                        if (spos >= slen)
                            matchFailed = true;
                    }
                }
                else
                {

                    // Multi character wildcard at the end of the pattern, match all remaining
                    // characters

                    spos = slen;
                }
                break;

            // Match the pattern and string character

            default:
                if (spos >= slen || strChars[spos] != patChar)
                    matchFailed = true;
                else
                    spos++;
                break;
            }
        }

        // Check if the match was successul and return status

        if (matchFailed == false && spos == slen)
            return true;
        return false;
    }

    /**
     * Set the wildcard pattern string
     * 
     * @param pattern String
     * @param caseSensitive boolean
     */
    public final void setPattern(String pattern, boolean caseSensitive)
    {

        // Save the pattern string and case sensitive flag

        m_pattern = pattern;
        m_caseSensitive = caseSensitive;

        setType(WILDCARD_INVALID);

        // Check if the pattern string is valid

        if (pattern == null || pattern.length() == 0)
            return;

        // Check for the match all wildcard

        if (pattern.compareTo("*.*") == 0 || pattern.compareTo("*") == 0)
        {
            setType(WILDCARD_ALL);
            return;
        }

        // Check for a name wildcard, ie. '*.ext'

        if (pattern.startsWith("*."))
        {

            // Split the string to get the extension string

            if (pattern.length() > 2)
                m_matchPart = pattern.substring(1);
            else
                m_matchPart = "";

            // If matching is case insensitive then normalize the string

            if (isCaseSensitive() == false)
                m_matchPart = m_matchPart.toUpperCase();

            // If the file extension contains wildcards we will need to use a regular expression

            if (containsWildcards(m_matchPart) == false)
            {
                setType(WILDCARD_NAME);
                return;
            }
        }

        // Check for a file extension wildcard

        if (pattern.endsWith(".*"))
        {

            // Split the string to get the name string

            if (pattern.length() > 2)
                m_matchPart = pattern.substring(0, pattern.length() - 2);
            else
                m_matchPart = "";

            // If matching is case insensitive then normalize the string

            if (isCaseSensitive() == false)
                m_matchPart = m_matchPart.toUpperCase();

            // If the file name contains wildcards we will need to use a regular expression

            if (containsWildcards(m_matchPart) == false)
            {
                setType(WILDCARD_EXT);
                return;
            }
        }

        // Save the complex wildcard pattern as a char array for later pattern matching

        if (isCaseSensitive() == false)
            m_patternChars = m_pattern.toUpperCase().toCharArray();
        else
            m_patternChars = m_pattern.toCharArray();

        setType(WILDCARD_COMPLEX);
    }

    /**
     * Set the wildcard type
     * 
     * @param typ int
     */
    private final void setType(int typ)
    {
        m_type = typ;
    }

    /**
     * Return the wildcard as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();
        str.append("[");
        str.append(getPattern());
        str.append(",");
        str.append(isType());
        str.append(",");

        if (m_matchPart != null)
            str.append(m_matchPart);

        if (isCaseSensitive())
            str.append(",Case");
        else
            str.append(",NoCase");
        str.append("]");

        return str.toString();
    }

    /**
     * Check if the string contains any wildcard characters.
     * 
     * @return boolean
     * @param str java.lang.String
     */
    public final static boolean containsWildcards(String str)
    {

        // Check the string for wildcard characters

        if (str.indexOf(MULTICHAR_WILDCARD) != -1)
            return true;

        if (str.indexOf(SINGLECHAR_WILDCARD) != -1)
            return true;

        // No wildcards found in the string

        return false;
    }

    /**
     * Check if a string contains any of the Unicode wildcard characters
     * 
     * @param str String
     * @return boolean
     */
    public final static boolean containsUnicodeWildcard(String str)
    {

        // Check if the string contains any of the Unicode wildcards

        if (str.indexOf(SINGLECHAR_UNICODE_WILDCARD) != -1 || str.indexOf(MULTICHAR_UNICODE_WILDCARD) != -1
                || str.indexOf(DOT_UNICODE_WILDCARD) != -1)
            return true;
        return false;
    }

    /**
     * Convert the Unicode wildcard string to a standard DOS wildcard string
     * 
     * @param str String
     * @return String
     */
    public final static String convertUnicodeWildcardToDOS(String str)
    {

        // Create a buffer for the new wildcard string

        StringBuffer newStr = new StringBuffer(str.length());

        // Convert the Unicode wildcard string to a DOS wildcard string

        for (int i = 0; i < str.length(); i++)
        {

            // Get the current character

            char ch = str.charAt(i);

            // Check for a Unicode wildcard character

            if (ch == SINGLECHAR_UNICODE_WILDCARD)
            {

                // Translate to the DOS single character wildcard character

                ch = SINGLECHAR_WILDCARD;
            }
            else if (ch == MULTICHAR_UNICODE_WILDCARD)
            {

                // Check if the current character is followed by a '.', if so then translate to the
                // DOS multi character
                // wildcard

                if (i < (str.length() - 1) && str.charAt(i + 1) == '.')
                    ch = MULTICHAR_WILDCARD;
            }
            else if (ch == DOT_UNICODE_WILDCARD)
            {

                // Check if the current character is followed by a DOS single/multi character
                // wildcard

                if (i < (str.length() - 1))
                {
                    char nextCh = str.charAt(i + 1);
                    if (nextCh == SINGLECHAR_WILDCARD || nextCh == MULTICHAR_WILDCARD
                            || nextCh == SINGLECHAR_UNICODE_WILDCARD)
                        ch = '.';
                }
            }

            // Append the character to the translated wildcard string

            newStr.append(ch);
        }

        // Return the translated wildcard string

        return newStr.toString();
    }

    /**
     * Convert a wildcard string to a regular expression
     * 
     * @param path String
     * @return String
     */
    public final static String convertToRegexp(String path)
    {

        // Convert the path to characters, check if the wildcard string ends with a single character
        // wildcard

        char[] smbPattern = path.toCharArray();
        boolean endsWithQ = smbPattern[smbPattern.length - 1] == '?';

        // Build up the regular expression

        StringBuffer sb = new StringBuffer();
        sb.append('^');

        for (int i = 0; i < smbPattern.length; i++)
        {

            // Process the current character

            switch (smbPattern[i])
            {

            // Multi character wildcard

            case '*':
                sb.append(".*");
                break;

            // Single character wildcard

            case '?':
                if (endsWithQ)
                {
                    boolean restQ = true;
                    for (int j = i + 1; j < smbPattern.length; j++)
                    {
                        if (smbPattern[j] != '?')
                        {
                            restQ = false;
                            break;
                        }
                    }
                    if (restQ)
                        sb.append(".?");
                    else
                        sb.append('.');
                }
                else
                    sb.append('.');
                break;

            // Escape regular expression special characters

            case '.':
            case '+':
            case '\\':
            case '[':
            case ']':
            case '^':
            case '$':
            case '(':
            case ')':
                sb.append('\\');
                sb.append(smbPattern[i]);
                break;

            // Normal characters, just pass through

            default:
                sb.append(smbPattern[i]);
                break;
            }
        }
        sb.append('$');

        // Return the regular expression string

        return sb.toString();
    }
}