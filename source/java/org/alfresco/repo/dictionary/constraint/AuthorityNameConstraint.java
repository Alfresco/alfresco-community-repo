/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.dictionary.constraint;

import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.cmr.security.AuthorityType;

public class AuthorityNameConstraint extends AbstractConstraint
{

    private static final String ERR_INVALID_AUTHORITY_NAME = "d_dictionary.constraint.authority_name.invalid_authority_name";
    private static final String ERR_NON_STRING = "d_dictionary.constraint.authority_name.non_string";

    @Override
    protected void evaluateSingleValue(Object value)
    {
        // ensure that the value can be converted to a String
        String checkValue = null;
        try
        {
            checkValue = DefaultTypeConverter.INSTANCE.convert(String.class, value);
        }
        catch (TypeConversionException e)
        {
            throw new ConstraintException(ERR_NON_STRING, value);
        }
        
        AuthorityType type = AuthorityType.getAuthorityType(checkValue);
        if((type != AuthorityType.GROUP) && (type != AuthorityType.ROLE))
        {
            throw new ConstraintException(ERR_INVALID_AUTHORITY_NAME, value, type);
        }
    }

    public void initialize()
    {

    }

}
