/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.template;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.alfresco.repo.processor.BaseProcessor;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateException;
import org.alfresco.service.cmr.repository.TemplateProcessor;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.XMLUtil;
import org.apache.bsf.BSFManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.utils.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import freemarker.cache.TemplateLoader;

public class XSLTProcessor extends BaseProcessor implements TemplateProcessor
{
    private static final Log log = LogFactory.getLog(XSLTProcessor.class);
    
    private static final String LOCALE_SEPARATOR = "_";

    private final static String MSG_ERROR_NO_TEMPLATE   = "error_no_template";
    private static final String MSG_UNABLE_TO_READ_TEMPLATE = "template.xslt.read_error";
    private static final String MSG_UNABLE_TO_PARSE_TEMPLATE = "template.xslt.parse_error";

    public final static QName ROOT_NAMESPACE = QName.createQName(null, "root_namespace");

    private String defaultEncoding = "UTF-8";
    private TemplateLoader templateLoader;

    public void register()
    {
        super.register();
        templateLoader = new ClassPathRepoTemplateLoader(this.services.getNodeService(), this.services
                .getContentService(), defaultEncoding);
    }

    public void process(String template, Object model, Writer out)
    {
        TemplateSource templateSource;
        try
        {
            templateSource = (TemplateSource) templateLoader.findTemplateSource(template);
        }
        catch (IOException ex)
        {
            throw new TemplateException(MSG_UNABLE_TO_READ_TEMPLATE, new Object[] { ex.getMessage() }, ex);
        }
        process(templateSource, model, out);
    }

    public void processString(final String template, Object model, Writer out)
    {
        TemplateSource stringTemplateSource = new TemplateSource()
        {
            public long lastModified()
            {
                return System.currentTimeMillis();
            }

            public InputStream getResource(String name)
            {
                return null;
            }

            public Reader getReader(String encoding) throws IOException
            {
                return new StringReader(template);
            }

            public void close() throws IOException
            {
            }
        };
        process(stringTemplateSource, model, out);
    }

    /**
     * @param templateSource
     * @param xsltModel
     * @param out
     */
    private void process(TemplateSource templateSource, Object model, Writer out)
    {
        if ((model == null) || !XSLTemplateModel.class.isAssignableFrom(model.getClass()))
        {
            throw new IllegalArgumentException("\"model\" must be an XSLTemplateModel object: " + model);
        }

        XSLTemplateModel xsltModel = (XSLTemplateModel) model;
        System.setProperty("org.apache.xalan.extensions.bsf.BSFManager", BSFManager.class.getName());

        Document xslTemplate;
        try
        {
            xslTemplate = XMLUtil.parse(templateSource.getReader(defaultEncoding));
        }
        catch (IOException ex)
        {
            throw new TemplateException(MSG_UNABLE_TO_READ_TEMPLATE, new Object[] { ex.getMessage() }, ex);
        }
        catch (SAXException sax)
        {
            throw new TemplateException(MSG_UNABLE_TO_PARSE_TEMPLATE, new Object[] { sax.getMessage() }, sax);
        }
        finally
        {
            try
            {
                templateSource.close();
            }
            catch (IOException ex)
            {
                // There's little to be done here. Log it and carry on
                log.warn("Error while trying to close template stream", ex);
            }
        }

        List<String> scriptIds = addScripts(xsltModel, xslTemplate);
        addParameters(xsltModel, xslTemplate);

        final LinkedList<TransformerException> errors = new LinkedList<TransformerException>();
        final ErrorListener errorListener = new ErrorListener()
        {
            public void error(final TransformerException te) throws TransformerException
            {
                log.debug("error " + te.getMessageAndLocation());
                errors.add(te);
            }

            public void fatalError(final TransformerException te) throws TransformerException
            {
                log.debug("fatalError " + te.getMessageAndLocation());
                throw te;
            }

            public void warning(final TransformerException te) throws TransformerException
            {
                log.debug("warning " + te.getMessageAndLocation());
                errors.add(te);
            }
        };

        final TemplateSource resourceSource = templateSource;
        final URIResolver uriResolver = new URIResolver()
        {
            public Source resolve(final String href, String base) throws TransformerException
            {
                if (log.isDebugEnabled())
                {
                    log.debug("request to resolve href " + href + " using base " + base);
                }
                InputStream in = null;
                try
                {
                    in = resourceSource.getResource(href);
                    if (in == null)
                    {
                        throw new TransformerException("unable to resolve href " + href);
                    }

                    Document d = XMLUtil.parse(in);
                    if (log.isDebugEnabled())
                    {
                        log.debug("loaded " + XMLUtil.toString(d));
                    }
                    return new DOMSource(d);
                }
                catch (TransformerException ex)
                {
                    throw ex;
                }
                catch (Exception e)
                {
                    throw new TransformerException("unable to load " + href, e);
                }
            }
        };

        Source xmlSource = this.getXMLSource(xsltModel);

        Transformer t = null;
        try
        {
            final TransformerFactory tf = TransformerFactory.newInstance();
            tf.setErrorListener(errorListener);
            tf.setURIResolver(uriResolver);

            if (log.isDebugEnabled())
            {
                log.debug("xslTemplate: \n" + XMLUtil.toString(xslTemplate));
            }

            t = tf.newTransformer(new DOMSource(xslTemplate));

            if (errors.size() != 0)
            {
                final StringBuilder msg = new StringBuilder("errors encountered creating tranformer ... \n");
                for (TransformerException te : errors)
                {
                    msg.append(te.getMessageAndLocation()).append("\n");
                }
                throw new TemplateException(msg.toString());
            }

            t.setErrorListener(errorListener);
            t.setURIResolver(uriResolver);
            t.setParameter("versionParam", "2.0");
        }
        catch (TransformerConfigurationException tce)
        {
            log.error(tce);
            throw new TemplateException(tce.getMessage(), tce);
        }

        try
        {
            t.transform(xmlSource, new StreamResult(out));
        }
        catch (TransformerException te)
        {
            log.error(te.getMessageAndLocation());
            throw new TemplateException(te.getMessageAndLocation(), te);
        }
        catch (Exception e)
        {
            log.error("unexpected error " + e);
            throw new TemplateException(e.getMessage(), e);
        }
        finally
        {
            //Clear out any scripts that were created for this transform
            if (!scriptIds.isEmpty())
            {
                XSLTProcessorMethodInvoker.removeMethods(scriptIds);
            }
        }

        if (errors.size() != 0)
        {
            final StringBuilder msg = new StringBuilder("errors encountered during transformation ... \n");
            for (TransformerException te : errors)
            {
                msg.append(te.getMessageAndLocation()).append("\n");
            }
            throw new TemplateException(msg.toString());
        }
    }

    /**
     * Adds a script element to the xsl which makes static methods on this object available to the xsl tempalte.
     * 
     * @param xslTemplate
     *            the xsl template
     */
    protected List<String> addScripts(final XSLTemplateModel xsltModel, final Document xslTemplate)
    {
        final Map<QName, List<Map.Entry<QName, Object>>> methods = new HashMap<QName, List<Map.Entry<QName, Object>>>();
        for (final Map.Entry<QName, Object> entry : xsltModel.entrySet())
        {
            if (entry.getValue() instanceof TemplateProcessorMethod)
            {
                final String prefix = QName.splitPrefixedQName(entry.getKey().toPrefixString())[0];
                final QName qn = QName.createQName(entry.getKey().getNamespaceURI(), prefix);
                if (!methods.containsKey(qn))
                {
                    methods.put(qn, new LinkedList<Map.Entry<QName, Object>>());
                }
                methods.get(qn).add(entry);
            }
        }

        final Element docEl = xslTemplate.getDocumentElement();
        final String XALAN_NS = Constants.S_BUILTIN_EXTENSIONS_URL;
        final String XALAN_NS_PREFIX = "xalan";
        docEl.setAttribute("xmlns:" + XALAN_NS_PREFIX, XALAN_NS);

        final Set<String> excludePrefixes = new HashSet<String>();
        if (docEl.hasAttribute("exclude-result-prefixes"))
        {
            excludePrefixes.addAll(Arrays.asList(docEl.getAttribute("exclude-result-prefixes").split(" ")));
        }
        excludePrefixes.add(XALAN_NS_PREFIX);

        final List<String> result = new LinkedList<String>();
        for (QName ns : methods.keySet())
        {
            final String prefix = ns.getLocalName();
            docEl.setAttribute("xmlns:" + prefix, ns.getNamespaceURI());
            excludePrefixes.add(prefix);

            final Element compEl = xslTemplate.createElementNS(XALAN_NS, XALAN_NS_PREFIX + ":component");
            compEl.setAttribute("prefix", prefix);
            docEl.appendChild(compEl);
            String functionNames = null;
            final Element scriptEl = xslTemplate.createElementNS(XALAN_NS, XALAN_NS_PREFIX + ":script");
            scriptEl.setAttribute("lang", "javascript");
            final StringBuilder js = new StringBuilder("var _xsltp_invoke = java.lang.Class.forName('"
                    + XSLTProcessorMethodInvoker.class.getName() + "').newInstance();\n"
                    + "function _xsltp_to_java_array(js_array) {\n"
                    + "var java_array = java.lang.reflect.Array.newInstance(java.lang.Object, js_array.length);\n"
                    + "for (var i = 0; i < js_array.length; i++) { java_array[i] = js_array[i]; }\n"
                    + "return java_array; }\n");
            for (final Map.Entry<QName, Object> entry : methods.get(ns))
            {
                if (functionNames == null)
                {
                    functionNames = entry.getKey().getLocalName();
                }
                else
                {
                    functionNames += " " + entry.getKey().getLocalName();
                }
                final String id = entry.getKey().getLocalName() + entry.getValue().hashCode();
                js.append("function " + entry.getKey().getLocalName() + "() { return _xsltp_invoke.invokeMethod('" + id
                        + "', _xsltp_to_java_array(arguments)); }\n");
                XSLTProcessorMethodInvoker.addMethod(id, (TemplateProcessorMethod) entry.getValue());
                result.add(id);
            }
            log.debug("generated JavaScript bindings:\n" + js);
            scriptEl.appendChild(xslTemplate.createTextNode(js.toString()));
            compEl.setAttribute("functions", functionNames);
            compEl.appendChild(scriptEl);
        }
        docEl.setAttribute("exclude-result-prefixes", StringUtils.join(excludePrefixes
                .toArray(new String[excludePrefixes.size()]), " "));
        return result;
    }

    /**
     * Adds the specified parameters to the xsl template as variables within the alfresco namespace.
     * 
     * @param xsltModel
     *            the variables to place within the xsl template
     * @param xslTemplate
     *            the xsl template
     */
    protected void addParameters(final XSLTemplateModel xsltModel, final Document xslTemplate)
    {
        final Element docEl = xslTemplate.getDocumentElement();
        final String XSL_NS = docEl.getNamespaceURI();
        final String XSL_NS_PREFIX = docEl.getPrefix();

        for (Map.Entry<QName, Object> e : xsltModel.entrySet())
        {
            if (ROOT_NAMESPACE.equals(e.getKey()))
            {
                continue;
            }
            final Element el = xslTemplate.createElementNS(XSL_NS, XSL_NS_PREFIX + ":variable");
            el.setAttribute("name", e.getKey().toPrefixString());
            final Object o = e.getValue();
            if (o instanceof String || o instanceof Number || o instanceof Boolean)
            {
                el.appendChild(xslTemplate.createTextNode(o.toString()));
                // ALF-15413. Add the variables at the end of the list of children
                docEl.insertBefore(el, null);
            }
        }
    }

    protected Source getXMLSource(final Map<QName, Object> model)
    {
        if (!model.containsKey(ROOT_NAMESPACE))
        {
            return null;
        }
        final Object o = model.get(ROOT_NAMESPACE);
        if (!(o instanceof Document))
        {
            throw new IllegalArgumentException("expected root namespace object to be a  " + Document.class.getName()
                    + ".  found a " + o.getClass().getName());
        }
        return new DOMSource((Document) o);
    }

	@Override
	public void process(String template, Object model, Writer out, Locale locale) 
	{
        if (template.indexOf(StoreRef.URI_FILLER) != -1)
        {
    		// If template is a node ref, ignore locale
        	process(template, model, out);
        }
	    else {
	    	//Otherwise try and locate a locale specific resource.
            TemplateSource templateSource = null;
            int lastDot = template.lastIndexOf('.');
            String prefix = lastDot == -1 ? template : template.substring(0, lastDot);
            String suffix = lastDot == -1 ? "" : template.substring(lastDot);
            String localeName = LOCALE_SEPARATOR + locale.toString();
            StringBuffer buf = new StringBuffer(template.length() + localeName.length());
            buf.append(prefix);
			for (;;)
            {
                buf.setLength(prefix.length());
                String path = buf.append(localeName).append(suffix).toString();
                try
                {
                	templateSource = (TemplateSource) templateLoader.findTemplateSource(path);
                }
                catch (IOException ex)
                {
                    throw new TemplateException(MSG_UNABLE_TO_READ_TEMPLATE, new Object[] { ex.getMessage() }, ex);
                }
                if (templateSource != null)
                {
                    break;
                }
                int lastUnderscore = localeName.lastIndexOf('_');
                if (lastUnderscore == -1)
                {
                    break;
                }
                localeName = localeName.substring(0, lastUnderscore);
            }
	        if (templateSource == null)
	        {
	            throw new TemplateException(MSG_ERROR_NO_TEMPLATE, new Object[] {template});
	        }
	        process(templateSource, model, out);
	    }
	}

}
