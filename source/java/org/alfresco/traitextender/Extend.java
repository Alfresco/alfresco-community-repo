
package org.alfresco.traitextender;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A runtime retained annotation that marks AJ-trait-extended methods of
 * {@link Extensible} objects.<br>
 * It defines the actual circumstances in which the {@link ExtensionPoint}
 * defined using {@link #extensionAPI()} and {@link #traitAPI()} has its
 * extension invoked.<br>
 * Methods marked by this aspect are advised by an extension-routing around
 * advice in {@link RouteExtensions}. Consequently the call will be routed to a
 * method of an extension object having the same signature as the marked method.<br>
 * 
 * @author Bogdan Horje
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Extend
{
    Class<?> extensionAPI();

    Class<? extends Trait> traitAPI();
}
