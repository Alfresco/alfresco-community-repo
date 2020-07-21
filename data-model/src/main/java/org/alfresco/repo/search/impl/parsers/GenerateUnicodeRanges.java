/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.repo.search.impl.parsers;


/**
 * @author Andy
 */
public class GenerateUnicodeRanges
{

    /**
     * @param args String[]
     */
    public static void main(String[] args)
    {

        int  start = 0;
        int  last = 0;
        for (int i = 0; i < 0xFFFF; i++)
        {
            //if (Character.isSpaceChar(i))
            switch(Character.getType(i))
            {
            case Character.LOWERCASE_LETTER:  // V1 V2 V3-SW
            case Character.MODIFIER_LETTER:   // V1 V2 V3-SW
            case Character.OTHER_LETTER:      // V1 V2 V3-SW
            case Character.TITLECASE_LETTER:  // V1 V2 V3-SW
            case Character.UPPERCASE_LETTER:  // V1 V2 V3-SW
            case Character.COMBINING_SPACING_MARK: // V2 V3-SW
            case Character.ENCLOSING_MARK: // V2 V3-SW
            case Character.NON_SPACING_MARK: // V2 V3-SW
            case Character.DECIMAL_DIGIT_NUMBER:  // V1 V2 V3-SW
            case Character.LETTER_NUMBER: // V2 V3-SW
            case Character.OTHER_NUMBER: // V2 V3-SW
            case Character.CURRENCY_SYMBOL: // V2 V3-SW
            case Character.OTHER_SYMBOL: // V2 V3-SW
                
            case Character.CONNECTOR_PUNCTUATION: // V3-W
            case Character.DASH_PUNCTUATION: // V3-W
            case Character.OTHER_PUNCTUATION: // V3-W
            case Character.MATH_SYMBOL: // V3-W
                if (last == 0)
                {
                    start = i;
                }

                if ((last + 1 == i) || (start == i))
                {
                    last = i;
                }
                else
                {
                    if (start > 0)
                    {
                        if (start == (i - 1))
                        {
                            System.out.println(String.format("        | '\\u%04x'", Integer.valueOf(start)));
                        }
                        else
                        {
                            System.out.println(String.format("        | '\\u%04x'..'\\u%04x'", Integer.valueOf(start), Integer.valueOf(last)));
                        }
                        start = i;
                        last = i;
                    }
                }
                break;
           
                
            case Character.CONTROL: // X
            case Character.FORMAT:  // X
            case Character.PRIVATE_USE: // X
            case Character.SURROGATE: // X
            //case Character.CONNECTOR_PUNCTUATION: // V3-W
            //case Character.DASH_PUNCTUATION: // V3-W
            case Character.END_PUNCTUATION: // X
            case Character.FINAL_QUOTE_PUNCTUATION: //X
            case Character.INITIAL_QUOTE_PUNCTUATION: // X
            //case Character.OTHER_PUNCTUATION: // V3-W
            case Character.START_PUNCTUATION: // X
            case Character.MODIFIER_SYMBOL: // X
            //case Character.MATH_SYMBOL: // V3-W
            case Character.LINE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
            case Character.SPACE_SEPARATOR:
                if (start > 0)
                {
                    if (start == (i - 1))
                    {
                        System.out.println(String.format("        | '\\u%04x'", Integer.valueOf(start)));
                    }
                    else
                    {
                        System.out.println(String.format("        | '\\u%04x'..'\\u%04x'", Integer.valueOf(start), Integer.valueOf(last)));
                    }
                    start = 0;
                    last = 0;
                }
                break;
            }

        }

    }
}
