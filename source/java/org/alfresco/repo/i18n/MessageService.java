package org.alfresco.repo.i18n;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import org.alfresco.repo.tenant.TenantDeployer;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * Utility class providing methods to access the Locale of the current thread and to get
 * Localised strings. These strings may be loaded from resource bundles deployed in the Repository.
 */
public interface MessageService extends TenantDeployer
{
    /**
     * Set the locale for the current thread.
     *
     * @param locale    the locale
     */
    public void setLocale(Locale locale);

    /**
     * Get the general local for the current thread, will revert to the default locale if none
     * specified for this thread.
     *
     * @return  the general locale
     */
    public Locale getLocale();

    /**
     * Set the <b>content locale</b> for the current thread.
     *
     * @param locale    the content locale
     */
    public void setContentLocale(Locale locale);


    /**
     * Get the content local for the current thread.<br/>
     * This will revert to {@link #getLocale()} if no value has been defined.
     *
     * @return  Returns the content locale
     */
    public Locale getContentLocale();

    /**
     * Searches for the nearest locale from the available options.  To match any locale, pass in
     * <tt>null</tt>.
     *
     * @param templateLocale the template to search for or <tt>null</tt> to match any locale
     * @param options the available locales to search from
     * @return Returns the best match from the available options, or the <tt>null</tt> if
     *      all matches fail
     */
    public Locale getNearestLocale(Locale templateLocale, Set<Locale> options);

    /**
     * Factory method to create a Locale from a <tt>lang_country_variant</tt> string.
     *
     * @param localeStr e.g. fr_FR
     * @return Returns the locale instance, or the {@link Locale#getDefault() default} if the
     *      string is invalid
     */
    public Locale parseLocale(String localeStr);

    /**
     * Register a resource bundle.
     * <p>
     * This should be the bundle base path 
     * eg, alfresco/messages/errors
     * or, workspace://SpaceStore/app:company_home/app:dictionary/app:labels/cm:errors
     * <p>
     * Once registered the messages will be available via getMessage, assuming the
     * bundle resource exists at the given path location.
     *
     * @param bundleBaseName    the bundle base path
     */
    public void registerResourceBundle(String bundleBasePath);
    
    /**
     * Get message from registered resource bundle.
     *
     * @param messageKey    message key
     * @return              localised message string, null if not found
     */
    public String getMessage(String messageKey);

    /**
     * Get a localised message string
     *
     * @param messageKey        the message key
     * @param locale            override the current locale
     * @return                  the localised message string, null if not found
     */
    public String getMessage(final String messageKey, final Locale locale);

    /**
     * Get a localised message string, parameterized using standard MessageFormatter.
     *
     * @param messageKey    message key
     * @param params        format parameters
     * @return              the localised string, null if not found
     */
    public String getMessage(String messageKey, Object ... params);
    
    /**
     * Get a localised message string, parameterized using standard MessageFormatter.
     *
     * @param messageKey        the message key
     * @param locale            override current locale
     * @param params            the localised message string
     * @return                  the localised string, null if not found
     */
    public String getMessage(String messageKey, Locale locale, Object ... params);

    /**
     * Unregister a resource bundle
     * <p>
     * This should be the bundle base path 
     * eg alfresco/messages/errors
     * or workspace://SpaceStore/app:company_home/app:dictionary/app:labels/cm:errors
     * <p>
     * Once unregistered the messages will no longer be available via getMessage
     *
     * @param bundleBaseName    the bundle base path
     */
    public void unregisterResourceBundle(String resBundlePath);
    
    /**
     * Get message resource bundle from the repository
     * 
     * note: also used by Web Client (ResourceBundleWrapper)
     * 
     * @param storeRef  store ref
     * @param path      repository path (XPath)
     * @param locale    locale
     * @return          input stream
     */
    public ResourceBundle getRepoResourceBundle(StoreRef storeRef, String path, Locale locale) throws IOException;
    
    /**
     * Get set of registered message resource bundles
     * 
     * @return set of registered bundles
     */
    public Set<String> getRegisteredBundles();
    
    /**
     * Register message deployer with message service
     * 
     * @param messageDeployer
     */
    public void register(MessageDeployer messageDeployer);
    
    public String getBaseBundleName(String resourceName);
    
}
