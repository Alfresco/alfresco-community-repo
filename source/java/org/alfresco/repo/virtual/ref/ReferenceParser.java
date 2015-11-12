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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

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
