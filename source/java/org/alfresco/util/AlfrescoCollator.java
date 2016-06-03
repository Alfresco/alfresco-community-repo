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
