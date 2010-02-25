/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.cmis;

import junit.framework.TestCase;

import org.alfresco.repo.cmis.ws.CmisException;
import org.alfresco.repo.cmis.ws.EnumServiceException;

/**
 * @author Dmitry Velichkevich
 */
public class PropertyFilterTest extends TestCase
{
    private static final String NAME_TOKEN = "name";

    private static final String[] FILTER_TOKENS = new String[] { "Name", NAME_TOKEN, "nAmE", "ObjectId", "ObjectID", "objectId" };
    private static final String[] TOKENS_THAT_ARE_NOT_ALLOWED = new String[] { "ParentId", "parentId", "ParEnTiD", "IsMajorVersion", "iSmAJORvERSION" };

    private static final String VALID_MATCHE_ALL_FILTER = "*";
    private static final String VALID_MATCHE_ALL_EMPTY_FILTER = "";
    private static final String VALID_FILTER_WITH_NAME = NAME_TOKEN;
    private static final String VALID_FILTER_WITH_SEVERAL_TOKENS = "name, ObjectId";
    private static final String LONG_VALID_FILTER_WITH_SEVERAL_TOKENS = "ObjectId, name, CreationDate, CreatedBy";
    private static final String VALID_FILTER_WITH_SEVERAL_TOKENS_WITHOUT_BREAKS = "name,Objectid,CreationDate";
    private static final String VALID_FILTER_WITH_SEVERAL_TOKENS_AND_WITH_BREAKS_IN_SOME_PLACES = "name, Objectid,CreationDate,CreatedBy, ModifiedBy, LastModifiedBy";
    private static final String VALID_FILTER_WITH_SEVERAL_TOKENS_AND_WITH_SEVERAL_BREAKS_IN_SOME_PLACES = "name, Objectid,     CreationDate,CreatedBy,   ModifiedBy, LastModifiedBy";

    private static final String INVALID_MATCHE_ALL_FILTER = "*,";
    private static final String INVALID_FILTER_WITH_NAME = "*name,";
    private static final String INVALID_FILTER_WITH_SEVERAL_TOKENS = "name ,ObjectId";
    private static final String LONG_INVALID_FILTER_WITH_SEVERAL_TOKENS = "ObjectId, name CreationDate, CreatedBy*";
    private static final String INVALID_FILTER_WITH_SEVERAL_TOKENS_WITHOUT_BREAKS = ",name,Objectid,CreationDate";
    private static final String INVALID_FILTER_WITH_SEVERAL_TOKENS_AND_WITH_BREAKS_IN_SOME_PLACES = " name, Objectid,CreationDate CreatedBy ModifiedBy, LastModifiedBy";
    private static final String INVALID_FILTER_WITH_FIRST_BREAK_SYMBOL = " name, Objectid,CreationDate, CreatedBy, ModifiedBy, LastModifiedBy";
    private static final String INVALID_FILTER_WITH_DENIED_SYMBOL = "ObjectId; name";
    private static final String INVALID_FILTER_WITH_LAST_INVALID_SYMBOL = "ObjectId, name*";

    public void testValidFilters() throws Exception
    {
        try
        {
            allTokensValidAssertion(new PropertyFilter());
            allTokensValidAssertion(new PropertyFilter(VALID_MATCHE_ALL_EMPTY_FILTER));
            allTokensValidAssertion(new PropertyFilter(VALID_MATCHE_ALL_FILTER));

            onlyNameTokensAssertionValid(new PropertyFilter(VALID_FILTER_WITH_NAME));

            nameAndObjectIdTokensAssertionValid(new PropertyFilter(VALID_FILTER_WITH_SEVERAL_TOKENS));
            nameAndObjectIdTokensAssertionValid(new PropertyFilter(LONG_VALID_FILTER_WITH_SEVERAL_TOKENS));
            nameAndObjectIdTokensAssertionValid(new PropertyFilter(VALID_FILTER_WITH_SEVERAL_TOKENS_WITHOUT_BREAKS));
            nameAndObjectIdTokensAssertionValid(new PropertyFilter(VALID_FILTER_WITH_SEVERAL_TOKENS_AND_WITH_BREAKS_IN_SOME_PLACES));
            nameAndObjectIdTokensAssertionValid(new PropertyFilter(VALID_FILTER_WITH_SEVERAL_TOKENS_AND_WITH_SEVERAL_BREAKS_IN_SOME_PLACES));
        }
        catch (Throwable e)
        {
            fail(e.getMessage());
        }
    }

    public void testInvalidFilters() throws Exception
    {
        invalidFilterAssertion(INVALID_MATCHE_ALL_FILTER);
        invalidFilterAssertion(INVALID_FILTER_WITH_NAME);
        invalidFilterAssertion(INVALID_FILTER_WITH_SEVERAL_TOKENS);
        invalidFilterAssertion(LONG_INVALID_FILTER_WITH_SEVERAL_TOKENS);
        invalidFilterAssertion(INVALID_FILTER_WITH_SEVERAL_TOKENS_WITHOUT_BREAKS);
        invalidFilterAssertion(INVALID_FILTER_WITH_SEVERAL_TOKENS_AND_WITH_BREAKS_IN_SOME_PLACES);
        invalidFilterAssertion(INVALID_FILTER_WITH_FIRST_BREAK_SYMBOL);
        invalidFilterAssertion(INVALID_FILTER_WITH_DENIED_SYMBOL);
        invalidFilterAssertion(INVALID_FILTER_WITH_LAST_INVALID_SYMBOL);
    }

    private void nameAndObjectIdTokensAssertionValid(PropertyFilter propertyFilter)
    {
        for (String token : FILTER_TOKENS)
        {
            assertTrue(propertyFilter.allow(token));
        }

        for (String token : TOKENS_THAT_ARE_NOT_ALLOWED)
        {
            assertFalse(propertyFilter.allow(token));
        }
    }

    private void onlyNameTokensAssertionValid(PropertyFilter propertyFilter)
    {
        for (String token : FILTER_TOKENS)
        {
            if (!token.equalsIgnoreCase(NAME_TOKEN))
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
        catch (CmisException e)
        {
            assertEquals(("Unexpected exception type was thrown: " + e.getClass().getName()), EnumServiceException.FILTER_NOT_VALID, e.getFaultInfo().getType());
        }
    }

}
