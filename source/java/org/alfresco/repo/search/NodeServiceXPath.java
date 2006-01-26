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
package org.alfresco.repo.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.util.ISO9075;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.BaseXPath;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.FunctionContext;
import org.jaxen.JaxenException;
import org.jaxen.Navigator;
import org.jaxen.SimpleFunctionContext;
import org.jaxen.SimpleVariableContext;
import org.jaxen.function.BooleanFunction;
import org.jaxen.function.CeilingFunction;
import org.jaxen.function.ConcatFunction;
import org.jaxen.function.ContainsFunction;
import org.jaxen.function.CountFunction;
import org.jaxen.function.FalseFunction;
import org.jaxen.function.FloorFunction;
import org.jaxen.function.IdFunction;
import org.jaxen.function.LangFunction;
import org.jaxen.function.LastFunction;
import org.jaxen.function.LocalNameFunction;
import org.jaxen.function.NameFunction;
import org.jaxen.function.NamespaceUriFunction;
import org.jaxen.function.NormalizeSpaceFunction;
import org.jaxen.function.NotFunction;
import org.jaxen.function.NumberFunction;
import org.jaxen.function.PositionFunction;
import org.jaxen.function.RoundFunction;
import org.jaxen.function.StartsWithFunction;
import org.jaxen.function.StringFunction;
import org.jaxen.function.StringLengthFunction;
import org.jaxen.function.SubstringAfterFunction;
import org.jaxen.function.SubstringBeforeFunction;
import org.jaxen.function.SubstringFunction;
import org.jaxen.function.SumFunction;
import org.jaxen.function.TranslateFunction;
import org.jaxen.function.TrueFunction;
import org.jaxen.function.ext.EndsWithFunction;
import org.jaxen.function.ext.EvaluateFunction;
import org.jaxen.function.ext.LowerFunction;
import org.jaxen.function.ext.MatrixConcatFunction;
import org.jaxen.function.ext.UpperFunction;
import org.jaxen.function.xslt.DocumentFunction;

/**
 * Represents an xpath statement that resolves against a
 * <code>NodeService</code>
 * 
 * @author Andy Hind
 */
public class NodeServiceXPath extends BaseXPath
{
    private static final long serialVersionUID = 3834032441789592882L;

    private static String JCR_URI = "http://www.jcp.org/jcr/1.0";
    
    private static Log logger = LogFactory.getLog(NodeServiceXPath.class);

    /**
     * 
     * @param xpath
     *            the xpath statement
     * @param documentNavigator
     *            the navigator that will allow the xpath to be resolved
     * @param paramDefs
     *            parameters to resolve variables required by xpath
     * @throws JaxenException
     */
    public NodeServiceXPath(String xpath, DocumentNavigator documentNavigator, QueryParameterDefinition[] paramDefs)
            throws JaxenException
    {
        super(xpath, documentNavigator);

        if (logger.isDebugEnabled())
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Created XPath: \n")
              .append("   XPath: ").append(xpath).append("\n")
              .append("   Parameters: \n");
            for (int i = 0; paramDefs != null && i < paramDefs.length; i++)
            {
                sb.append("      Parameter: \n")
                  .append("         name: ").append(paramDefs[i].getQName()).append("\n")
                  .append("         value: ").append(paramDefs[i].getDefault()).append("\n");
            }
            logger.debug(sb.toString());
        }
        
        // Add support for parameters
        if (paramDefs != null)
        {
            SimpleVariableContext svc = (SimpleVariableContext) this.getVariableContext();
            for (int i = 0; i < paramDefs.length; i++)
            {
                if (!paramDefs[i].hasDefaultValue())
                {
                    throw new AlfrescoRuntimeException("Parameter must have default value");
                }
                Object value = null;
                if (paramDefs[i].getDataTypeDefinition().getName().equals(DataTypeDefinition.BOOLEAN))
                {
                    value = Boolean.valueOf(paramDefs[i].getDefault());
                }
                else if (paramDefs[i].getDataTypeDefinition().getName().equals(DataTypeDefinition.DOUBLE))
                {
                    value = Double.valueOf(paramDefs[i].getDefault());
                }
                else if (paramDefs[i].getDataTypeDefinition().getName().equals(DataTypeDefinition.FLOAT))
                {
                    value = Float.valueOf(paramDefs[i].getDefault());
                }
                else if (paramDefs[i].getDataTypeDefinition().getName().equals(DataTypeDefinition.INT))
                {
                    value = Integer.valueOf(paramDefs[i].getDefault());
                }
                else if (paramDefs[i].getDataTypeDefinition().getName().equals(DataTypeDefinition.LONG))
                {
                    value = Long.valueOf(paramDefs[i].getDefault());
                }
                else
                {
                    value = paramDefs[i].getDefault();
                }
                svc.setVariableValue(paramDefs[i].getQName().getNamespaceURI(), paramDefs[i].getQName().getLocalName(),
                        value);
            }
        }

        for (String prefix : documentNavigator.getNamespacePrefixResolver().getPrefixes())
        {
            addNamespace(prefix, documentNavigator.getNamespacePrefixResolver().getNamespaceURI(prefix));
        }
    }
    
    /**
     * Jaxen has some magic with its IdentitySet, which means that we can get different results
     * depending on whether we cache {@link ChildAssociationRef } instances or not.
     * <p>
     * So, duplicates are eliminated here before the results are returned.
     */
    @SuppressWarnings("unchecked")
    @Override
    public List selectNodes(Object arg0) throws JaxenException
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Selecting using XPath: \n" +
                    "   XPath: " + this + "\n" +
                    "   starting at: " + arg0);
        }
        
        List<Object> resultsWithDuplicates = super.selectNodes(arg0);
        
        Set<Object> set = new HashSet<Object>(resultsWithDuplicates);
        
        // now return as a list again
        List<Object> results = resultsWithDuplicates;
        results.clear();
        results.addAll(set);
        
        // done
        return results;
    }

    public static class FirstFunction implements Function
    {

        public Object call(Context context, List args) throws FunctionCallException
        {
            if (args.size() == 0)
            {
                return evaluate(context);
            }

            throw new FunctionCallException("first() requires no arguments.");
        }

        public static Double evaluate(Context context)
        {
            return new Double(1);
        }
    }

    /**
     * A boolean function to determine if a node type is a subtype of another
     * type
     */
    static class SubTypeOf implements Function
    {
        public Object call(Context context, List args) throws FunctionCallException
        {
            if (args.size() != 1)
            {
                throw new FunctionCallException("subtypeOf() requires one argument: subtypeOf(QName typeQName)");
            }
            return evaluate(context.getNodeSet(), args.get(0), context.getNavigator());
        }

        public Object evaluate(List nodes, Object qnameObj, Navigator nav)
        {
            if (nodes.size() != 1)
            {
                return false;
            }
            // resolve the qname of the type we are checking for
            String qnameStr = StringFunction.evaluate(qnameObj, nav);
            if (qnameStr.equals("*"))
            {
                return true;
            }
            QName typeQName;

            if (qnameStr.startsWith("{"))
            {
                typeQName = QName.createQName(qnameStr);
            }
            else
            {
                typeQName = QName.createQName(qnameStr, ((DocumentNavigator) nav).getNamespacePrefixResolver());
            }
            // resolve the noderef
            NodeRef nodeRef = null;
            if (nav.isElement(nodes.get(0)))
            {
                nodeRef = ((ChildAssociationRef) nodes.get(0)).getChildRef();
            }
            else if (nav.isAttribute(nodes.get(0)))
            {
                nodeRef = ((DocumentNavigator.Property) nodes.get(0)).parent;
            }

            DocumentNavigator dNav = (DocumentNavigator) nav;
            boolean result = dNav.isSubtypeOf(nodeRef, typeQName);
            return result;
        }
    }

    static class Deref implements Function
    {

        public Object call(Context context, List args) throws FunctionCallException
        {
            if (args.size() == 2)
            {
                return evaluate(args.get(0), args.get(1), context.getNavigator());
            }

            throw new FunctionCallException("deref() requires two arguments.");
        }

        public Object evaluate(Object attributeName, Object pattern, Navigator nav)
        {
            List<Object> answer = new ArrayList<Object>();
            String attributeValue = StringFunction.evaluate(attributeName, nav);
            String patternValue = StringFunction.evaluate(pattern, nav);

            // TODO: Ignore the pattern for now
            // Should do a type pattern test
            if ((attributeValue != null) && (attributeValue.length() > 0))
            {
                DocumentNavigator dNav = (DocumentNavigator) nav;
                NodeRef nodeRef = new NodeRef(attributeValue);
                if (patternValue.equals("*"))
                {
                    answer.add(dNav.getNode(nodeRef));
                }
                else
                {
                    QNamePattern qNamePattern = new JCRPatternMatch(patternValue, dNav.getNamespacePrefixResolver());
                    answer.addAll(dNav.getNode(nodeRef, qNamePattern));
                }

            }
            return answer;

        }
    }

    /**
     * A boolean function to determine if a node property matches a pattern
     * and/or the node text matches the pattern.
     * <p>
     * The default is JSR170 compliant. The optional boolean allows searching
     * only against the property value itself.
     * <p>
     * The search is always case-insensitive.
     * 
     * @author Derek Hulley
     */
    static class Like implements Function
    {
        public Object call(Context context, List args) throws FunctionCallException
        {
            if (args.size() < 2 || args.size() > 3)
            {
                throw new FunctionCallException("like() usage: like(@attr, 'pattern' [, includeFTS]) \n"
                        + " - includeFTS can be 'true' or 'false' \n"
                        + " - search is case-insensitive");
            }
            // default includeFTS to true
            return evaluate(context.getNodeSet(), args.get(0), args.get(1), args.size() == 2 ? Boolean.toString(true)
                    : args.get(2), context.getNavigator());
        }

        public Object evaluate(List nodes, Object obj, Object patternObj, Object includeFtsObj, Navigator nav)
        {
            Object attribute = null;
            if (obj instanceof List)
            {
                List list = (List) obj;
                if (list.isEmpty())
                {
                    return false;
                }
                // do not recurse: only first list should unwrap
                attribute = list.get(0);
            }
            if ((attribute == null) || !nav.isAttribute(attribute))
            {
                return false;
            }
            if (nodes.size() != 1)
            {
                return false;
            }
            if (!nav.isElement(nodes.get(0)))
            {
                return false;
            }
            ChildAssociationRef car = (ChildAssociationRef) nodes.get(0);
            String pattern = StringFunction.evaluate(patternObj, nav);
            boolean includeFts = BooleanFunction.evaluate(includeFtsObj, nav);
            QName qname = QName.createQName(nav.getAttributeNamespaceUri(attribute), ISO9075.decode(nav
                    .getAttributeName(attribute)));

            DocumentNavigator dNav = (DocumentNavigator) nav;
            // JSR 170 includes full text matches
            return dNav.like(car.getChildRef(), qname, pattern, includeFts);

        }
    }

    static class Contains implements Function
    {

        public Object call(Context context, List args) throws FunctionCallException
        {
            if (args.size() != 1)
            {
                throw new FunctionCallException("contains() usage: contains('pattern')");
            }
            return evaluate(context.getNodeSet(), args.get(0), context.getNavigator());
        }

        public Object evaluate(List nodes, Object pattern, Navigator nav)
        {
            if (nodes.size() != 1)
            {
                return false;
            }
            QName qname = null;
            NodeRef nodeRef = null;
            if (nav.isElement(nodes.get(0)))
            {
                qname = null; // should use all attributes and full text index
                nodeRef = ((ChildAssociationRef) nodes.get(0)).getChildRef();
            }
            else if (nav.isAttribute(nodes.get(0)))
            {
                qname = QName.createQName(
                        nav.getAttributeNamespaceUri(nodes.get(0)),
                        ISO9075.decode(nav.getAttributeName(nodes.get(0))));
                nodeRef = ((DocumentNavigator.Property) nodes.get(0)).parent;
            }

            String patternValue = StringFunction.evaluate(pattern, nav);
            DocumentNavigator dNav = (DocumentNavigator) nav;

            return dNav.contains(nodeRef, qname, patternValue, SearchParameters.OR);

        }
    }

    static class JCRContains implements Function
    {

        public Object call(Context context, List args) throws FunctionCallException
        {
            if (args.size() == 2)
            {
                if (context.getNavigator().isAttribute(context.getNodeSet().get(0)))
                {
                    throw new FunctionCallException("jcr:contains() does not apply to an attribute context.");
                }
                return evaluate(context.getNodeSet(), args.get(0), args.get(1), context.getNavigator());
            }

            throw new FunctionCallException("contains() requires two argument.");
        }

        public Object evaluate(List nodes, Object identifier, Object pattern, Navigator nav)
        {
            if (nodes.size() != 1)
            {
                return false;
            }

            QName qname = null;
            NodeRef nodeRef = null;

            Object target = identifier;

            if (identifier instanceof List)
            {
                List list = (List) identifier;
                if (list.isEmpty())
                {
                    return false;
                }
                // do not recurse: only first list should unwrap
                target = list.get(0);
            }

            if (nav.isElement(target))
            {
                qname = null; // should use all attributes and full text index
                nodeRef = ((ChildAssociationRef) target).getChildRef();
            }
            else if (nav.isAttribute(target))
            {
                qname = QName.createQName(
                        nav.getAttributeNamespaceUri(target),
                        ISO9075.decode(nav.getAttributeName(target)));
                nodeRef = ((DocumentNavigator.Property) target).parent;
            }

            String patternValue = StringFunction.evaluate(pattern, nav);
            DocumentNavigator dNav = (DocumentNavigator) nav;

            return dNav.contains(nodeRef, qname, patternValue, SearchParameters.AND);

        }
    }

    static class Score implements Function
    {
        private Double one = new Double(1);

        public Object call(Context context, List args) throws FunctionCallException
        {
            return evaluate(context.getNodeSet(), context.getNavigator());
        }

        public Object evaluate(List nodes, Navigator nav)
        {
            return one;

        }
    }

    protected FunctionContext createFunctionContext()
    {
        return XPathFunctionContext.getInstance();
    }

    public static class XPathFunctionContext extends SimpleFunctionContext
    {
        /**
         * Singleton implementation.
         */
        private static class Singleton
        {
            /**
             * Singleton instance.
             */
            private static XPathFunctionContext instance = new XPathFunctionContext();
        }

        /**
         * Retrieve the singleton instance.
         * 
         * @return the singleton instance
         */
        public static FunctionContext getInstance()
        {
            return Singleton.instance;
        }

        /**
         * Construct.
         * 
         * <p>
         * Construct with all core XPath functions registered.
         * </p>
         */
        public XPathFunctionContext()
        {
            // XXX could this be a HotSpot????
            registerFunction("", // namespace URI
                    "boolean", new BooleanFunction());

            registerFunction("", // namespace URI
                    "ceiling", new CeilingFunction());

            registerFunction("", // namespace URI
                    "concat", new ConcatFunction());

            registerFunction("", // namespace URI
                    "contains", new ContainsFunction());

            registerFunction("", // namespace URI
                    "count", new CountFunction());

            registerFunction("", // namespace URI
                    "document", new DocumentFunction());

            registerFunction("", // namespace URI
                    "false", new FalseFunction());

            registerFunction("", // namespace URI
                    "floor", new FloorFunction());

            registerFunction("", // namespace URI
                    "id", new IdFunction());

            registerFunction("", // namespace URI
                    "lang", new LangFunction());

            registerFunction("", // namespace URI
                    "last", new LastFunction());

            registerFunction("", // namespace URI
                    "local-name", new LocalNameFunction());

            registerFunction("", // namespace URI
                    "name", new NameFunction());

            registerFunction("", // namespace URI
                    "namespace-uri", new NamespaceUriFunction());

            registerFunction("", // namespace URI
                    "normalize-space", new NormalizeSpaceFunction());

            registerFunction("", // namespace URI
                    "not", new NotFunction());

            registerFunction("", // namespace URI
                    "number", new NumberFunction());

            registerFunction("", // namespace URI
                    "position", new PositionFunction());

            registerFunction("", // namespace URI
                    "round", new RoundFunction());

            registerFunction("", // namespace URI
                    "starts-with", new StartsWithFunction());

            registerFunction("", // namespace URI
                    "string", new StringFunction());

            registerFunction("", // namespace URI
                    "string-length", new StringLengthFunction());

            registerFunction("", // namespace URI
                    "substring-after", new SubstringAfterFunction());

            registerFunction("", // namespace URI
                    "substring-before", new SubstringBeforeFunction());

            registerFunction("", // namespace URI
                    "substring", new SubstringFunction());

            registerFunction("", // namespace URI
                    "sum", new SumFunction());

            registerFunction("", // namespace URI
                    "true", new TrueFunction());

            registerFunction("", // namespace URI
                    "translate", new TranslateFunction());

            // register extension functions
            // extension functions should go into a namespace, but which one?
            // for now, keep them in default namespace to not break any code

            registerFunction("", // namespace URI
                    "matrix-concat", new MatrixConcatFunction());

            registerFunction("", // namespace URI
                    "evaluate", new EvaluateFunction());

            registerFunction("", // namespace URI
                    "lower-case", new LowerFunction());

            registerFunction("", // namespace URI
                    "upper-case", new UpperFunction());

            registerFunction("", // namespace URI
                    "ends-with", new EndsWithFunction());

            registerFunction("", "subtypeOf", new SubTypeOf());
            registerFunction("", "deref", new Deref());
            registerFunction("", "like", new Like());
            registerFunction("", "contains", new Contains());

            registerFunction("", "first", new FirstFunction());

            // 170 functions

            registerFunction(JCR_URI, "like", new Like());
            registerFunction(JCR_URI, "score", new Score());
            registerFunction(JCR_URI, "contains", new JCRContains());
            registerFunction(JCR_URI, "deref", new Deref());

        }
    }

    public static class JCRPatternMatch implements QNamePattern
    {
        private List<String> searches = new ArrayList<String>();

        private NamespacePrefixResolver resolver;

        /**
         * Construct
         * 
         * @param pattern
         *            JCR Pattern
         * @param resolver
         *            Namespace Prefix Resolver
         */
        public JCRPatternMatch(String pattern, NamespacePrefixResolver resolver)
        {
            // TODO: Check for valid pattern

            // Convert to regular expression
            String regexPattern = pattern.replaceAll("\\*", ".*");

            // Split into independent search strings
            StringTokenizer tokenizer = new StringTokenizer(regexPattern, "|", false);
            while (tokenizer.hasMoreTokens())
            {
                String disjunct = tokenizer.nextToken().trim();
                this.searches.add(disjunct);
            }

            this.resolver = resolver;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.alfresco.service.namespace.QNamePattern#isMatch(org.alfresco.service.namespace.QName)
         */
        public boolean isMatch(QName qname)
        {
            String prefixedName = qname.toPrefixString(resolver);
            for (String search : searches)
            {
                if (prefixedName.matches(search))
                {
                    return true;
                }
            }
            return false;
        }

    }

}
