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
package org.alfresco.repo.search;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Enum to specify how multi-lingual properties should be treate for indexing and search. Note that locale new Locale ("", "", "") is used to indicate all locales.
 * 
 * @author andyh
 */
public enum MLAnalysisMode
{
    /**
     * Only the exact locale is used.
     */
    LOCALE_ONLY
    {
        public boolean includesAll()
        {
            return false;
        }

        public boolean includesContained()
        {
            return false;
        }

        public boolean includesContaining()
        {
            return false;
        }

        public boolean includesExact()
        {
            return true;
        }

        public boolean includesAllLanguageMatches()
        {
            return false;
        }

        public boolean includesExactLanguageMatch()
        {
            return false;
        }

        public boolean includesAllCountryMatches()
        {
            return false;
        }

        public boolean includesExactCountryMatch()
        {
            return false;
        }

    },

    /**
     * Only the exact locale and no locale === locale + all languages
     */
    LOCALE_AND_ALL
    {
        public boolean includesAll()
        {
            return true;
        }

        public boolean includesContained()
        {
            return false;
        }

        public boolean includesContaining()
        {
            return false;
        }

        public boolean includesExact()
        {
            return true;
        }

        public boolean includesAllLanguageMatches()
        {
            return false;
        }

        public boolean includesExactLanguageMatch()
        {
            return false;
        }

        public boolean includesAllCountryMatches()
        {
            return false;
        }

        public boolean includesExactCountryMatch()
        {
            return false;
        }
    },

    /**
     * Expand the locale to include all the locales that contain it. So "en_GB" would be "en_GB", "en", but not all languages "".
     */
    LOCALE_AND_ALL_CONTAINING_LOCALES
    {
        public boolean includesAll()
        {
            return false;
        }

        public boolean includesContained()
        {
            return false;
        }

        public boolean includesContaining()
        {
            return true;
        }

        public boolean includesExact()
        {
            return true;
        }

        public boolean includesAllLanguageMatches()
        {
            return false;
        }

        public boolean includesExactLanguageMatch()
        {
            return false;
        }

        public boolean includesAllCountryMatches()
        {
            return false;
        }

        public boolean includesExactCountryMatch()
        {
            return false;
        }
    },

    /**
     * Expand the locale to include all the locales that contain it. "en_GB" would be "en_GB", "en", and all "".
     */
    LOCALE_AND_ALL_CONTAINING_LOCALES_AND_ALL
    {
        public boolean includesAll()
        {
            return true;
        }

        public boolean includesContained()
        {
            return false;
        }

        public boolean includesContaining()
        {
            return true;
        }

        public boolean includesExact()
        {
            return true;
        }

        public boolean includesAllLanguageMatches()
        {
            return false;
        }

        public boolean includesExactLanguageMatch()
        {
            return false;
        }

        public boolean includesAllCountryMatches()
        {
            return false;
        }

        public boolean includesExactCountryMatch()
        {
            return false;
        }
    },

    /**
     * Expand to all the locales that are contained by this. "en" would expand to "en", "en_GB", "en_US", ....
     */
    LOCALE_AND_ALL_CONTAINED_LOCALES
    {
        public boolean includesAll()
        {
            return false;
        }

        public boolean includesContained()
        {
            return true;
        }

        public boolean includesContaining()
        {
            return false;
        }

        public boolean includesExact()
        {
            return true;
        }

        public boolean includesAllLanguageMatches()
        {
            return false;
        }

        public boolean includesExactLanguageMatch()
        {
            return false;
        }

        public boolean includesAllCountryMatches()
        {
            return false;
        }

        public boolean includesExactCountryMatch()
        {
            return false;
        }
    },

    /**
     * Just the all locale, "", === new Locale("", "", "")
     */
    ALL_ONLY
    {
        public boolean includesAll()
        {
            return true;
        }

        public boolean includesContained()
        {
            return false;
        }

        public boolean includesContaining()
        {
            return false;
        }

        public boolean includesExact()
        {
            return false;
        }

        public boolean includesAllLanguageMatches()
        {
            return false;
        }

        public boolean includesExactLanguageMatch()
        {
            return false;
        }

        public boolean includesAllCountryMatches()
        {
            return false;
        }

        public boolean includesExactCountryMatch()
        {
            return false;
        }
    },

    /**
     * All language matches. Only worry about language level matches for locale.
     */

    ALL_LANGUAGES
    {
        public boolean includesAll()
        {
            return false;
        }

        public boolean includesContained()
        {
            return false;
        }

        public boolean includesContaining()
        {
            return false;
        }

        public boolean includesExact()
        {
            return false;
        }

        public boolean includesAllLanguageMatches()
        {
            return true;
        }

        public boolean includesExactLanguageMatch()
        {
            return false;
        }

        public boolean includesAllCountryMatches()
        {
            return false;
        }

        public boolean includesExactCountryMatch()
        {
            return false;
        }
    },

    /**
     * All language matches and ALL
     */

    ALL_LANGUAGES_AND_ALL
    {
        public boolean includesAll()
        {
            return true;
        }

        public boolean includesContained()
        {
            return false;
        }

        public boolean includesContaining()
        {
            return false;
        }

        public boolean includesExact()
        {
            return false;
        }

        public boolean includesAllLanguageMatches()
        {
            return true;
        }

        public boolean includesExactLanguageMatch()
        {
            return false;
        }

        public boolean includesAllCountryMatches()
        {
            return false;
        }

        public boolean includesExactCountryMatch()
        {
            return false;
        }
    },

    /**
     * Exact language matches (do not include all sub varients of the language)
     */

    EXACT_LANGUAGE
    {
        public boolean includesAll()
        {
            return false;
        }

        public boolean includesContained()
        {
            return false;
        }

        public boolean includesContaining()
        {
            return false;
        }

        public boolean includesExact()
        {
            return false;
        }

        public boolean includesAllLanguageMatches()
        {
            return false;
        }

        public boolean includesExactLanguageMatch()
        {
            return true;
        }

        public boolean includesAllCountryMatches()
        {
            return false;
        }

        public boolean includesExactCountryMatch()
        {
            return false;
        }
    },

    /**
     * Exact language matches (do not include all sub varients of the language) and ALL
     */

    EXACT_LANGUAGE_AND_ALL
    {
        public boolean includesAll()
        {
            return true;
        }

        public boolean includesContained()
        {
            return false;
        }

        public boolean includesContaining()
        {
            return false;
        }

        public boolean includesExact()
        {
            return false;
        }

        public boolean includesAllLanguageMatches()
        {
            return false;
        }

        public boolean includesExactLanguageMatch()
        {
            return true;
        }

        public boolean includesAllCountryMatches()
        {
            return false;
        }

        public boolean includesExactCountryMatch()
        {
            return false;
        }
    },

    /**
     * Exact country matches (do not include all sub varients of the country)
     */

    EXACT_COUNRTY
    {
        public boolean includesAll()
        {
            return false;
        }

        public boolean includesContained()
        {
            return false;
        }

        public boolean includesContaining()
        {
            return false;
        }

        public boolean includesExact()
        {
            return false;
        }

        public boolean includesAllLanguageMatches()
        {
            return false;
        }

        public boolean includesExactLanguageMatch()
        {
            return false;
        }

        public boolean includesAllCountryMatches()
        {
            return false;
        }

        public boolean includesExactCountryMatch()
        {
            return true;
        }
    },

    /**
     * Exact country matches (do not include all sub varients of the country) and ALL
     */

    EXACT_COUNTRY_AND_ALL
    {
        public boolean includesAll()
        {
            return true;
        }

        public boolean includesContained()
        {
            return false;
        }

        public boolean includesContaining()
        {
            return false;
        }

        public boolean includesExact()
        {
            return false;
        }

        public boolean includesAllLanguageMatches()
        {
            return false;
        }

        public boolean includesExactLanguageMatch()
        {
            return false;
        }

        public boolean includesAllCountryMatches()
        {
            return false;
        }

        public boolean includesExactCountryMatch()
        {
            return true;
        }
    },

    /**
     * All country matches
     */

    ALL_COUNTRIES
    {
        public boolean includesAll()
        {
            return false;
        }

        public boolean includesContained()
        {
            return false;
        }

        public boolean includesContaining()
        {
            return false;
        }

        public boolean includesExact()
        {
            return false;
        }

        public boolean includesAllLanguageMatches()
        {
            return false;
        }

        public boolean includesExactLanguageMatch()
        {
            return false;
        }

        public boolean includesAllCountryMatches()
        {
            return true;
        }

        public boolean includesExactCountryMatch()
        {
            return false;
        }
    },

    /**
     * All countries and ALL
     */

    ALL_COUNTRIES_AND_ALL
    {
        public boolean includesAll()
        {
            return true;
        }

        public boolean includesContained()
        {
            return false;
        }

        public boolean includesContaining()
        {
            return false;
        }

        public boolean includesExact()
        {
            return false;
        }

        public boolean includesAllLanguageMatches()
        {
            return false;
        }

        public boolean includesExactLanguageMatch()
        {
            return false;
        }

        public boolean includesAllCountryMatches()
        {
            return true;
        }

        public boolean includesExactCountryMatch()
        {
            return false;
        }
    };

    public static MLAnalysisMode getMLAnalysisMode(String mode)
    {
        for (MLAnalysisMode test : MLAnalysisMode.values())
        {
            if (test.toString().equalsIgnoreCase(mode))
            {
                return test;
            }
        }
        throw new AlfrescoRuntimeException("Unknown ML Analysis mode " + mode);
    }

    public abstract boolean includesAll();

    public abstract boolean includesContained();

    public abstract boolean includesContaining();

    public abstract boolean includesExact();

    public abstract boolean includesAllLanguageMatches();

    public abstract boolean includesExactLanguageMatch();

    public abstract boolean includesAllCountryMatches();

    public abstract boolean includesExactCountryMatch();

    public static Set<Locale> getLocales(MLAnalysisMode mlAnalaysisMode, Locale locale, boolean withWildcards)
    {
        HashSet<Locale> locales = new HashSet<Locale>();

        boolean l = locale.getLanguage().length() != 0;
        boolean c = locale.getCountry().length() != 0;
        boolean v = locale.getVariant().length() != 0;

        if (mlAnalaysisMode.includesAll())
        {
            if (withWildcards)
            {
                locales.add(new Locale("", "", ""));
                locales.add(new Locale("*", "", ""));
            }
            else
            {
                locales.add(new Locale("", "", ""));
            }

        }

        if (mlAnalaysisMode.includesExact())
        {
            locales.add(locale);
        }

        if (mlAnalaysisMode.includesContaining())
        {
            if (v)
            {
                Locale noVarient = new Locale(locale.getLanguage(), locale.getCountry(), "");
                locales.add(noVarient);

                Locale noCountry = new Locale(locale.getLanguage(), "", "");
                locales.add(noCountry);
            }
            if (c)
            {
                Locale noCountry = new Locale(locale.getLanguage(), "", "");
                locales.add(noCountry);
            }
        }

        if (mlAnalaysisMode.includesContained())
        {
            // varients have not contained
            if (!v)
            {
                if (!c)
                {
                    if (!l)
                    {
                        // All
                        if (withWildcards)
                        {
                            locales.add(new Locale("", "", ""));
                            locales.add(new Locale("*", "", ""));
                        }
                        else
                        {
                            for (Locale toAdd : Locale.getAvailableLocales())
                            {
                                locales.add(toAdd);
                            }
                        }
                    }
                    else
                    {
                        // All that match language
                        if (withWildcards)
                        {
                            locales.add(new Locale(locale.getLanguage(), "", ""));
                            locales.add(new Locale(locale.getLanguage(), "*", ""));
                        }
                        else
                        {
                            for (Locale toAdd : Locale.getAvailableLocales())
                            {
                                if (locale.getLanguage().equals("") || locale.getLanguage().equals(toAdd.getLanguage()))
                                {
                                    locales.add(toAdd);
                                }
                            }
                        }
                    }
                }
                else
                {
                    if (withWildcards)
                    {
                        locales.add(new Locale(locale.getLanguage(), locale.getCountry(), ""));
                        locales.add(new Locale(locale.getLanguage(), locale.getCountry(), "*"));
                    }
                    else
                    {
                        // All that match language and country
                        for (Locale toAdd : Locale.getAvailableLocales())
                        {
                            if ((locale.getLanguage().equals("") || locale.getLanguage().equals(toAdd.getLanguage()))
                                    && (locale.getCountry().equals("") || locale.getCountry()
                                            .equals(toAdd.getCountry())))
                            {
                                locales.add(toAdd);
                            }
                        }
                    }
                }
            }
        }

        if (mlAnalaysisMode.includesAllLanguageMatches())
        {
            if (withWildcards)
            {
                locales.add(new Locale(locale.getLanguage(), "", ""));
                locales.add(new Locale(locale.getLanguage(), "*", ""));
            }
            else
            {
                // All that match language
                for (Locale toAdd : Locale.getAvailableLocales())
                {
                    if (locale.getLanguage().equals("") || locale.getLanguage().equals(toAdd.getLanguage()))
                    {
                        locales.add(toAdd);
                    }
                }
            }
        }

        if (mlAnalaysisMode.includesExactLanguageMatch())
        {
            if (withWildcards)
            {
                locales.add(new Locale(locale.getLanguage(), "", ""));
            }
            else
            {
                locales.add(new Locale(locale.getLanguage(), "", ""));
            }
        }

        if (mlAnalaysisMode.includesAllCountryMatches())
        {
            if (withWildcards)
            {
                locales.add(new Locale(locale.getLanguage(), locale.getCountry(), ""));
                if (locale.getCountry().equals(""))
                {
                    locales.add(new Locale(locale.getLanguage(), "*", ""));
                }
                else
                {
                    locales.add(new Locale(locale.getLanguage(), locale.getCountry(), "*"));
                }
            }
            else
            {
                // All that match language
                for (Locale toAdd : Locale.getAvailableLocales())
                {
                    if ((locale.getLanguage().equals("") || locale.getLanguage().equals(toAdd.getLanguage()))
                            && (locale.getCountry().equals("") || locale.getCountry().equals(toAdd.getCountry())))
                    {
                        locales.add(toAdd);
                    }
                }
            }
        }

        if (mlAnalaysisMode.includesExactCountryMatch())
        {
            if (withWildcards)
            {
                if (locale.getCountry().equals(""))
                {
                    locales.add(new Locale(locale.getLanguage(), "", ""));
                    locales.add(new Locale(locale.getLanguage(), "*", ""));
                }
                else
                {
                    locales.add(new Locale(locale.getLanguage(), locale.getCountry(), ""));
                }

            }
            else
            {
                if (locale.getCountry().equals(""))
                {
                    for (Locale toAdd : Locale.getAvailableLocales())
                    {
                        if (locale.getLanguage().equals("") || locale.getLanguage().equals(toAdd.getLanguage()))
                        {
                            locales.add(toAdd);
                        }
                    }
                }
                else
                {
                    locales.add(new Locale(locale.getLanguage(), locale.getCountry(), ""));
                }
            }
        }

        return locales;
    }
}
