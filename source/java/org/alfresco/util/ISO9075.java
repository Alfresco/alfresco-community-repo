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
package org.alfresco.util;

import java.util.Collection;

import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import com.sun.org.apache.xerces.internal.util.XMLChar;

/**
 * Support for the ISO 9075 encoding of XML element names.
 * 
 * @author Andy Hind
 */
public class ISO9075
{
    /*
     * Mask for hex encoding
     */
    private static final int MASK = (1 << 4) - 1;

    /*
     * Digits used string encoding
     */
    private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
            'f' };

    /**
     * Private constructor
     * 
     */
    private ISO9075()
    {
        super();
    }

    /**
     * Encode a string according to ISO 9075
     * 
     * @param toEncode
     * @return
     */
    public static String encode(String toEncode)
    {
        if ((toEncode == null) || (toEncode.length() == 0))
        {
            return toEncode;
        }
        else if (XMLChar.isValidName(toEncode) && (toEncode.indexOf("_x") == -1))
        {
            return toEncode;
        }
        else
        {
            StringBuilder builder = new StringBuilder(toEncode.length());
            for (int i = 0; i < toEncode.length(); i++)
            {
                char c = toEncode.charAt(i);
                // First requires special test
                if (i == 0)
                {
                    if (XMLChar.isNCNameStart(c))
                    {
                        // The first character may be the _ at the start of an
                        // encoding pattern
                        if (matchesEncodedPattern(toEncode, i))
                        {
                            // Encode the first _
                            encode('_', builder);
                        }
                        else
                        {
                            // Just append
                            builder.append(c);
                        }
                    }
                    else
                    {
                        // Encode an invalid start character for an XML element
                        // name.
                        encode(c, builder);
                    }
                }
                else if (!XMLChar.isNCName(c))
                {
                    encode(c, builder);
                }
                else
                {
                    if (matchesEncodedPattern(toEncode, i))
                    {
                        // '_' must be encoded
                        encode('_', builder);
                    }
                    else
                    {
                        builder.append(c);
                    }
                }
            }
            return builder.toString();
        }

    }

    private static boolean matchesEncodedPattern(String string, int position)
    {
        return (string.length() >= position + 6)
                && (string.charAt(position) == '_') && (string.charAt(position + 1) == 'x')
                && isHexChar(string.charAt(position + 2)) && isHexChar(string.charAt(position + 3))
                && isHexChar(string.charAt(position + 4)) && isHexChar(string.charAt(position + 5))
                && (string.charAt(position + 6) == '_');
    }

    private static boolean isHexChar(char c)
    {
        switch (c)
        {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
        case 'a':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
        case 'f':
        case 'A':
        case 'B':
        case 'C':
        case 'D':
        case 'E':
        case 'F':
            return true;
        default:
            return false;
        }
    }

    public static String decode(String toDecode)
    {
        if ((toDecode == null) || (toDecode.length() < 7) || (toDecode.indexOf("_x") < 0))
        {
            return toDecode;
        }
        StringBuffer decoded = new StringBuffer();
        for (int i = 0, l = toDecode.length(); i < l; i++)
        {
            if (matchesEncodedPattern(toDecode, i))
            {
                decoded.append(((char) Integer.parseInt(toDecode.substring(i + 2, i + 6), 16)));
                i += 6;
            }
            else
            {
                decoded.append(toDecode.charAt(i));
            }
        }
        return decoded.toString();
    }

    private static void encode(char c, StringBuilder builder)
    {
        char[] buf = new char[] { '_', 'x', '0', '0', '0', '0', '_' };
        int charPos = 6;
        do
        {
            buf[--charPos] = DIGITS[c & MASK];
            c >>>= 4;
        }
        while (c != 0);
        builder.append(buf);
    }

    public static String getXPathName(QName qName, NamespacePrefixResolver nspr)
    {

        Collection<String> prefixes = nspr.getPrefixes(qName.getNamespaceURI());
        if (prefixes.size() == 0)
        {
            throw new NamespaceException("A namespace prefix is not registered for uri " + qName.getNamespaceURI());
        }
        String prefix = prefixes.iterator().next();
        if (prefix.equals(NamespaceService.DEFAULT_PREFIX))
        {
            return ISO9075.encode(qName.getLocalName());
        }
        else
        {
            return prefix + ":" + ISO9075.encode(qName.getLocalName());
        }

    }

    public static String getXPathName(QName qName)
    {

        return "{" + qName.getNamespaceURI() + "}" + ISO9075.encode(qName.getLocalName());

    }
}
