
package org.alfresco.repo.virtual.ref;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A {@link VirtualProtocol} extension that uses a scripted processor virtual
 * template in order to process a so-called vanilla JSON static template
 * definition on template execution.<br>
 * Vanilla references store have an extra {@link ResourceParameter} for the
 * vanilla-JSON template.
 * 
 * @author Bogdan Horje
 */
public class VanillaProtocol extends VirtualProtocol
{
    /**
     * 
     */
    private static final long serialVersionUID = -7192024582935232081L;

    public static final int VANILLA_TEMPLATE_PARAM_INDEX = 2;

    public VanillaProtocol()
    {
        super("vanilla");
    }

    @Override
    public <R> R dispatch(ProtocolMethod<R> method, Reference reference) throws ProtocolMethodException
    {
        return method.execute(this,
                              reference);
    }

    public Reference newReference(String vanillaProcessorClasspath, String templatePath, NodeRef actualNodeRef,
                NodeRef templateRef)
    {
        return this
                    .newReference(new ClasspathResource(vanillaProcessorClasspath),
                                  templatePath,
                                  actualNodeRef,
                                  Arrays
                                              .<Parameter> asList(new ResourceParameter(new RepositoryResource(new RepositoryNodeRef(templateRef)))));
    }

    public Reference newReference(Encoding encoding, Resource virtualTemplateResource, String templatePath,
                Resource actualResource, Resource vanillTemplateResource, List<Parameter> extraParameters)
    {
        List<Parameter> parameters = new ArrayList<>(2);
        parameters.add(new ResourceParameter(vanillTemplateResource));
        parameters.addAll(extraParameters);
        return this.newReference(encoding,
                                 virtualTemplateResource,
                                 templatePath,
                                 actualResource,
                                 parameters);
    }

    public Resource getVanillaTemplateResource(Reference reference)
    {
        ResourceParameter vanillaTemplateParamter = (ResourceParameter) reference
                    .getParameters()
                        .get(VANILLA_TEMPLATE_PARAM_INDEX);
        Resource resource = vanillaTemplateParamter.getValue();

        return resource;
    }

    public Reference newReference(String vanillaProcessorClasspath, String templatePath, NodeRef actualNodeRef,
                String templateSysPath) throws ProtocolMethodException
    {
        Resource templateResource = createSystemPathResource(templateSysPath);

        if (templateResource != null)
        {
            return this.newReference(new ClasspathResource(vanillaProcessorClasspath),
                                     templatePath,
                                     actualNodeRef,
                                     Arrays.<Parameter> asList(new ResourceParameter(templateResource)));
        }
        else
        {
            throw new ProtocolMethodException("Invalid template system path : " + templatePath);
        }
    }
}
