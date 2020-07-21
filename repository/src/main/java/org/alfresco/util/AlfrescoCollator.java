/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.util;

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Locale;
/**
 * Can be used to overwrite a RuleBaseCollator instance rules 
 * 
 * @author sergey.shcherbovich
 */
public class AlfrescoCollator 
{
    public static Collator getInstance(Locale locale)
    {
        return updateCollatorRules(Collator.getInstance(locale));
    }

    // update RuleBasedCollator rules to change order of characters during comparison
    // MNT-10169 fix
    private static synchronized Collator updateCollatorRules(Collator collator)
    {
        if (collator instanceof RuleBasedCollator)
        {
            try
            {
                // get current collator rules
                String collatorRules = ((RuleBasedCollator)collator).getRules();
                // we shoudn't ignore space character in character comparison - put it before u0021 character
                String newCollatorRules = collatorRules.replaceAll("<'\u0021'", "<'\u0020'<'\u0021'");
                // create new collator with overridden rules
                return new RuleBasedCollator(newCollatorRules);
            }
            catch(ParseException e)
            {
                return collator;
            }
        }
        return collator;
    }
}
