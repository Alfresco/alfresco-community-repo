/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.template;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.TemplateNode;
import org.alfresco.service.namespace.QName;

import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * @author Kevin Roast
 * 
 * Custom FreeMarker Template language method.
 * <p>
 * Method returns whether a TemplateNode has a particular aspect applied to it. The aspect
 * name can be either the fully qualified QName or the short prefixed name string.
 * <p>
 * Usage: hasAspect(TemplateNode node, String aspect)
 */
public final class HasAspectMethod implements TemplateMethodModelEx
{
    /**
     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
     */
    public Object exec(List args) throws TemplateModelException
    {
        int result = 0;
        
        if (args.size() == 2)
        {
            // arg 0 must be a wrapped TemplateNode object
            BeanModel arg0 = (BeanModel)args.get(0);
            
            // arg 1 can be either wrapped QName object or a String 
            String arg1String = null;
            Object arg1 = args.get(1);
            if (arg1 instanceof BeanModel)
            {
                arg1String = ((BeanModel)arg1).getWrappedObject().toString();
            }
            else if (arg1 instanceof TemplateScalarModel)
            {
                arg1String = ((TemplateScalarModel)arg1).getAsString();
            }
            if (arg0.getWrappedObject() instanceof TemplateNode)
            {
                // test to see if this node has the aspect
                if ( ((TemplateNode)arg0.getWrappedObject()).hasAspect(arg1String) )
                {
                    result = 1;
                }
            }
        }
        
        return Integer.valueOf(result);
    }
}
