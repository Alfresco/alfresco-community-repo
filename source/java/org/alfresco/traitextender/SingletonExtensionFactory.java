
package org.alfresco.traitextender;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates singleton extension sub classes that are extension API implementors.
 * The singleton extensions continue to exist after the extensible has been
 * collected.
 *
 * @author Bogdan Horje
 */
public class SingletonExtensionFactory<E, S extends SingletonExtension<E, T>, T extends Trait> implements
            ExtensionFactory<E>
{
    private Log logger = LogFactory.getLog(SingletonExtensionFactory.class);

    private class TraiSingletontHandler implements InvocationHandler
    {
        private S singleton;

        private T trait;

        public TraiSingletontHandler(S singleton, T trait)
        {
            super();
            this.singleton = singleton;
            this.trait = trait;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            // Subsequent trait calls might change the trait on this thread.
            // We save the current trait on the current call-stack in order
            // to restore it on the finally block.

            T stackedTrait = this.singleton.getTrait();

            this.singleton.setTrait(trait);
            try
            {
                return method.invoke(this.singleton,
                                     args);
            }
            catch (IllegalAccessException error)
            {
                throw new InvalidExtension(error);
            }
            catch (IllegalArgumentException error)
            {
                throw new InvalidExtension(error);
            }
            catch (InvocationTargetException error)
            {
                Throwable targetException = error.getTargetException();
                throw targetException;
            }
            finally
            {
                this.singleton.setTrait(stackedTrait);
            }
        }

    }

    private S singleton;

    private Class<E> extensionAPI;

    public SingletonExtensionFactory(S singleton, Class<E> extensionAPI) throws InvalidExtension
    {
        super();
        if (!extensionAPI.isAssignableFrom(singleton.getClass()))
        {
            throw new InvalidExtension(singleton.getClass() + " is not an " + extensionAPI + " extension.");
        }
        this.singleton = singleton;
        this.extensionAPI = extensionAPI;

    }

    @SuppressWarnings("unchecked")
    @Override
    public <TO extends Trait> E createExtension(TO traitObject)
    {
        T tTrait = (T) traitObject;
        // perform programmatic RTTI
        if (!singleton.acceptsTrait(traitObject))
        {
            throw new InvalidExtension("Extension factory error : " + singleton.getClass() + " does not support trait "
                        + traitObject);
        }

        if (logger.isDebugEnabled())
        {
            String traitTrace = traitObject != null ? traitObject.getClass().toString() : "<null> trait";
            logger.info("Creating singleton extension " + singleton.getClass() + "<-" + traitTrace);
        }
        else
        {
            logger.info("Creating singleton extension " + singleton.getClass());
        }

        return (E) Proxy.newProxyInstance(getClass().getClassLoader(),
                                          new Class[] { extensionAPI },
                                          new TraiSingletontHandler(singleton,
                                                                    tTrait));
    }

    @Override
    public boolean canCreateExtensionFor(ExtensionPoint<?, ?> point)
    {
        return point.getExtensionAPI().isAssignableFrom(singleton.getClass())
                    && singleton.acceptsTraitClass(point.getTraitAPI());
    }

}
