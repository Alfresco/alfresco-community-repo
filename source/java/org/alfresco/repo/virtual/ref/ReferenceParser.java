
package org.alfresco.repo.virtual.ref;

import java.io.Serializable;

public interface ReferenceParser extends Serializable
{
    /**
     * Helper class used in parsing string reference.
     */
    class Cursor
    {

        /**
         * Tokens obtained by splitting the reference string using
         * {@link PlainEncoding#DELIMITER}
         */
        String[] tokens;

        /**
         * Current processed Token
         */
        int i;

        Cursor(String[] tokens, int i)
        {
            super();
            this.tokens = tokens;
            this.i = i;
        }

        String currentToken()
        {
            return tokens[i];
        }

        String nextToken()
        {
            String c = tokens[i];
            i++;
            return c;
        }

        boolean hasNext()
        {
            return i < tokens.length;
        }

    }

    /**
     * Parses a string reference into a {@link Reference} object
     * 
     * @param referenceString
     * @return A reference of {@link Reference}
     * @throws ReferenceParseException
     */
    Reference parse(String referenceString) throws ReferenceParseException;
}
