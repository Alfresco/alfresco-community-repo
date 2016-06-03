
package org.alfresco.traitextender;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.alfresco.traitextender.AJExtender.ExtensionBypass;
import org.alfresco.util.ParameterCheck;

/**
 * Java {@link Proxy} {@link InvocationHandler} to be used in conjuction with
 * asprctJ extended traits.<br>
 * Method calls will be delegated to a given {@link Extensible} object method
 * having the same signature within an {@link ExtensionBypass} context.
 *
 * @author Bogdan Horje
 */
public class AJProxyTrait implements InvocationHandler
{

    /**
     * {@link Trait} {@link Proxy} factory method.
     * 
     * @param extensible the {@link Extensible} object that defines the given
     *            {@link Trait}
     * @param traitAPI the trait interface part that the given
     *            {@link Extensible} object defines
     * @return a {@link Proxy} object for the given trait API interface with an
     *         {@link AJProxyTrait} attached. All method calls performed on the
     *         returned proxy will be delegated to a given {@link Extensible}
     *         object method having the same signature within an
     *         {@link ExtensionBypass} context.
     */
    @SuppressWarnings("unchecked")
    public static <M extends Trait> M create(Object extensibleInterface, Class<M> traitAPI)
    {
        return (M) Proxy.newProxyInstance(AJProxyTrait.class.getClassLoader(),
                                          new Class[] { traitAPI },
                                          new AJProxyTrait(extensibleInterface));
    }

    private Object extensibleInterface;

    private AJProxyTrait(Object extensibleInterface)
    {
        ParameterCheck.mandatory("extensible",
                                 extensibleInterface);
        this.extensibleInterface = extensibleInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, final Object[] args) throws Throwable
    {
        final Method traitMethod = extensibleInterface.getClass().getMethod(method.getName(),
                                                                   method.getParameterTypes());

        if (AJExtender.isLocalProceeder(method))
        {
            return AJExtender.localProceed(args);
        }
        else
        {
            Class<?>[] exTypes = traitMethod.getExceptionTypes();

            return AJExtender.run(new AJExtender.ExtensionBypass<Object>()
                                  {
                                      @Override
                                      public Object run() throws Throwable
                                      {
                                          try
                                          {
                                              return traitMethod.invoke(extensibleInterface,
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
                                      }

                                  },
                                  exTypes);
        }
    }

    @Override
    public String toString()
    {
        return "AJAutoTrait@" + System.identityHashCode(this) + " of " + extensibleInterface.getClass().getSimpleName() + "@"
                    + System.identityHashCode(extensibleInterface);
    }

}
