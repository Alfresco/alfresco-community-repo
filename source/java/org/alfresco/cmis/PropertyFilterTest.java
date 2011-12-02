/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.cmis;

import junit.framework.TestCase;


/**
 * http://docs.oasis-open.org/cmis/CMIS/v1.0/os/cmis-spec-v1.0.htm
 * 2.1.2.1 Property
 * All properties MUST supply a String queryName attribute which is used for query and filter operations on object-types.
 * This is an opaque String with limitations. This string SHOULD NOT contain any characters that negatively interact with the BNF grammar.
 *
 * The string MUST NOT contain:
 *         whitespace “ “,
 *         comma “,”
 *         double quotes ‘”’
 *         single quotes “’”
 *         backslash “\”
 *         the period “.” character or,
 *         the open “(“ or close “)” parenthesis characters.
 * 
 * @author Dmitry Velichkevich
 * @author Arseny Kovalchuk
 */
public class PropertyFilterTest extends TestCase
{
    private static final String NAME_TOKEN = "name";

    private static final String[] FILTER_TOKENS = new String[] { NAME_TOKEN, "objectId" };
    private static final String[] TOKENS_THAT_ARE_NOT_ALLOWED = new String[] { "ParentId", "parentId", "ParEnTiD", "IsMajorVersion", "iSmAJORvERSION" };

    private static final String VALID_MATCHE_ALL_FILTER = "*";
    private static final String VALID_FILTER_WITH_NAME = NAME_TOKEN;
    private static final String LONG_VALID_FILTER_WITH_SEVERAL_TOKENS = "objectId,name,CreationDate,Created;By";
    private static final String VALID_FILTER_CMIS_WORKBANCH_ALFRESCO_3_4 = "cmis:parentId, cmis:objectId, name, objectId";
    private static final String VALID_FILTER_WITH_SPACES = " name, objectId,CreationDate, CreatedBy , ModifiedBy , LastModifiedBy ";

    private static final String INVALID_MATCHE_ALL_FILTER = "*,";
    private static final String INVALID_FILTER_WITH_NAME = "*name,";
    private static final String INVALID_FILTER_WITH_SEVERAL_TOKENS = "name,,objectId";
    private static final String LONG_INVALID_FILTER_WITH_SEVERAL_TOKENS = "objectId, name CreationDate, CreatedBy*";
    private static final String INVALID_FILTER_WITH_SEVERAL_TOKENS_WITHOUT_BREAKS = ",name,objectId,CreationDate";
    private static final String INVALID_FILTER_WITH_DENIED_SYMBOL = "objectId\"name";
    private static final String INVALID_FILTER_WITH_LAST_INVALID_SYMBOL = "objectId,name\\";

    public void testValidFilters() throws Exception
    {
        allTokensValidAssertion(new PropertyFilter(null));
        allTokensValidAssertion(new PropertyFilter(VALID_MATCHE_ALL_FILTER));

        onlyNameTokensAssertionValid(new PropertyFilter(VALID_FILTER_WITH_NAME));

        nameAndObjectIdTokensAssertionValid(new PropertyFilter(LONG_VALID_FILTER_WITH_SEVERAL_TOKENS));
        nameAndObjectIdTokensAssertionValid(new PropertyFilter(VALID_FILTER_CMIS_WORKBANCH_ALFRESCO_3_4));
        nameAndObjectIdTokensAssertionValid(new PropertyFilter(VALID_FILTER_WITH_SPACES));
    }

    public void testInvalidFilters() throws Exception
    {
        invalidFilterAssertion(INVALID_MATCHE_ALL_FILTER);
        invalidFilterAssertion(INVALID_FILTER_WITH_NAME);
        invalidFilterAssertion(INVALID_FILTER_WITH_SEVERAL_TOKENS);
        invalidFilterAssertion(LONG_INVALID_FILTER_WITH_SEVERAL_TOKENS);
        invalidFilterAssertion(INVALID_FILTER_WITH_SEVERAL_TOKENS_WITHOUT_BREAKS);
        invalidFilterAssertion(INVALID_FILTER_WITH_DENIED_SYMBOL);
        invalidFilterAssertion(INVALID_FILTER_WITH_LAST_INVALID_SYMBOL);
    }

    private void nameAndObjectIdTokensAssertionValid(PropertyFilter propertyFilter)
    {
        for (String token : FILTER_TOKENS)
        {
            assertTrue(token + " should be allowed", propertyFilter.allow(token));
        }

        for (String token : TOKENS_THAT_ARE_NOT_ALLOWED)
        {
            assertFalse(token + " should not be allowed", propertyFilter.allow(token));
        }
    }

    private void onlyNameTokensAssertionValid(PropertyFilter propertyFilter)
    {
        for (String token : FILTER_TOKENS)
        {
            if (!token.equals(NAME_TOKEN))
            {
                break;
            }

            assertTrue(propertyFilter.allow(token));
        }

        for (String token : TOKENS_THAT_ARE_NOT_ALLOWED)
        {
            assertFalse(propertyFilter.allow(token));
        }
    }

    private void allTokensValidAssertion(PropertyFilter propertyFilter)
    {
        for (String token : FILTER_TOKENS)
        {
            assertTrue(propertyFilter.allow(token));
        }

        for (String token : TOKENS_THAT_ARE_NOT_ALLOWED)
        {
            assertTrue(propertyFilter.allow(token));
        }
    }

    private void invalidFilterAssertion(String filterValue)
    {
        try
        {
            new PropertyFilter(filterValue);

            fail("Invalid filter \"" + filterValue + "\" was interpreted as valid");
        }
        catch (CMISFilterNotValidException e)
        {
            // Success
        }
    }

}
