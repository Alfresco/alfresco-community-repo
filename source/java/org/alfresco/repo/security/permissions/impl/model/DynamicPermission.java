package org.alfresco.repo.security.permissions.impl.model;

import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.dom4j.Element;

/**
 * The definition of a required permission
 * 
 * @author andyh
 */
public class DynamicPermission extends AbstractPermission implements XMLModelInitialisable
{
    /**
     * 
     */
    private static final long serialVersionUID = 8060533686472973313L;

    private static final String EVALUATOR = "evaluator";
    
    private String evaluatorFullyQualifiedClassName;
    
    public DynamicPermission(QName typeQName)
    {
        super(typeQName);
    }

    public void initialise(Element element, NamespacePrefixResolver nspr, PermissionModel permissionModel)
    {
        super.initialise(element, nspr, permissionModel);
        evaluatorFullyQualifiedClassName = element.attributeValue(EVALUATOR);   
    }

    public String getEvaluatorFullyQualifiedClassName()
    {
        return evaluatorFullyQualifiedClassName;
    }
}
