package org.alfresco.repo.security.permissions;

import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;

/**
 * Marker interface for objects that have already passed permission checking.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public interface PermissionCheckedValue
{
    /**
     * Helper 'introduction' to allow simple addition of the {@link PermissionCheckedValue} interface to
     * existing objects.
     *
     * @author Derek Hulley
     * @since 4.0
     */
    @SuppressWarnings("serial")
    public static class PermissionCheckedValueMixin extends DelegatingIntroductionInterceptor implements PermissionCheckedValue
    {
        private PermissionCheckedValueMixin()
        {
            super();
        }
        /**
         * Helper method to create a {@link PermissionCheckedValue} from an existing <code>Object</code>.
         * 
         * @param object        the <code>Object</code> to proxy
         * @return                  a <code>Object</code> of the same type but including the
         *                          {@link PermissionCheckedValue} interface
         */
        @SuppressWarnings("unchecked")
        public static final <T extends Object> T create(T object)
        {
            // Create the mixin
            DelegatingIntroductionInterceptor mixin = new PermissionCheckedValueMixin();
            // Create the advisor
            IntroductionAdvisor advisor = new DefaultIntroductionAdvisor(mixin, PermissionCheckedValue.class);
            // Proxy
            ProxyFactory pf = new ProxyFactory(object);
            pf.addAdvisor(advisor);
            Object proxiedObject = pf.getProxy();
            
            // Done
            return (T) proxiedObject;
        }
    }
}
