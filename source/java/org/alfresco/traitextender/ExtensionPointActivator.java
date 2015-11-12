
package org.alfresco.traitextender;

public class ExtensionPointActivator
{
    private ExtensionBundle bundle;

    private ExtensionPoint<?, ?> extensionPoint;

    private ExtensionFactory<?> extensionFactory;

    public <E> ExtensionPointActivator(ExtensionBundle bundle, ExtensionPoint<E, ?> extensionPoint,
                ExtensionFactory<E> extensionFactory)
    {
        super();
        this.bundle = bundle;
        this.extensionPoint = extensionPoint;
        this.extensionFactory = extensionFactory;
    }

}
