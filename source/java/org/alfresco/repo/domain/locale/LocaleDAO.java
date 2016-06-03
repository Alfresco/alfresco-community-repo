package org.alfresco.repo.domain.locale;

import java.util.Locale;

import org.alfresco.util.Pair;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Data abstraction layer for Locale entities.
 * 
 * @author Derek Hulley, janv
 * @since 3.4
 */
public interface LocaleDAO
{
    /**
     * @param id            the unique ID of the entity
     * @return              the locale pair (never null)
     * @throws              DataIntegrityViolationException if the ID provided is invalid
     */
    Pair<Long, Locale> getLocalePair(Long id);
    
    /**
     * @param locale            the locale to fetch or <tt>null</tt> to get the default locale
     * @return              the locale or <tt>null</tt> if no such locale exists
     */
    Pair<Long, Locale> getLocalePair(Locale locale);

    /**
     * @return              the locale pair for the default locale.  Although the <tt>Locale</tt>
     *                      object will be populated, the ID will point to an instance that generically
     *                      refers to the system's default locale i.e. the value returned can vary
     *                      depending on the executing thread's default locale.
     */
    Pair<Long, Locale> getDefaultLocalePair();
     
    /**
     * Gets the locale ID for an existing instance or creates a new entity if
     * one doesn't exist.
     * 
     * @param locale        the locale to fetch or <tt>null</tt> to get or create the default locale.
     * @return              the locale - never <tt>null</tt>
     */
    Pair<Long, Locale> getOrCreateLocalePair(Locale locale);

    /**
     * Find or create the details representing the default locale.
     * 
     * @return              the locale pair for the default locale.  Although the <tt>Locale</tt>
     *                      object will be populated, the ID will point to an instance that generically
     *                      refers to the system's default locale i.e. the value returned can vary
     *                      depending on the executing thread's default locale.
     */
    Pair<Long, Locale> getOrCreateDefaultLocalePair();
}
