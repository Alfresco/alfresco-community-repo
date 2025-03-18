/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.action.scheduled;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import org.springframework.extensions.surf.util.ISO8601DateFormat;

import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.Duration;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * A factory implementation to build suitable models for the freemarker templating language.
 * 
 * @author Andy Hind
 */
public class FreeMarkerWithLuceneExtensionsModelFactory implements TemplateActionModelFactory
{
    /* Service registry */
    private ServiceRegistry serviceRegistry;

    /**
     * Default constructor.
     */
    public FreeMarkerWithLuceneExtensionsModelFactory()
    {
        super();
    }

    // IOC

    /**
     * Set the service registry
     * 
     * @param serviceRegistry
     *            ServiceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Get the non-contextual model.
     * 
     * This defines:
     * <ol>
     * <li>dates: date, today, yesterday, tomorrow
     * <li>functions: luceneDateRange, selectSingleNode
     * </ol>
     */
    public Map<String, Object> getModel()
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        HashMap<String, Object> model = new HashMap<String, Object>();

        model.put("date", new Date());

        Date today = cal.getTime();
        model.put("today", today);

        model.put("yesterday", Duration.add(today, new Duration("-P1D")));

        model.put("tomorrow", Duration.add(today, new Duration("P1D")));

        model.put("luceneDateRange", new LuceneDateRangeFunction());

        model.put("selectSingleNode", new QueryForSingleNodeFunction());

        return model;
    }

    /**
     * Defines a non-contextual nod model + the contextual node
     */
    public Map<String, Object> getModel(NodeRef nodeRef)
    {
        Map<String, Object> model = getModel();

        TemplateNode companyRootNode = new TemplateNode(nodeRef, serviceRegistry, null);
        model.put("node", companyRootNode);

        return model;
    }

    /**
     * Function to find a single node by query
     * 
     * @author Andy Hind
     */
    private class QueryForSingleNodeFunction implements TemplateMethodModelEx
    {
        @SuppressWarnings("rawtypes")
        public Object exec(List args) throws TemplateModelException
        {
            if (args.size() == 3)
            {
                Object arg0 = args.get(0);
                Object arg1 = args.get(1);
                Object arg2 = args.get(2);
                StoreRef storeRef;
                String language;
                String query;

                if (arg0 instanceof TemplateScalarModel)
                {
                    storeRef = new StoreRef(((TemplateScalarModel) arg0).getAsString());
                }
                else
                {
                    throw new TemplateModelException("Invalid store string");
                }

                if (arg1 instanceof TemplateScalarModel)
                {
                    language = ((TemplateScalarModel) arg1).getAsString();
                }
                else
                {
                    throw new TemplateModelException("Invalid language string");
                }

                if (arg2 instanceof TemplateScalarModel)
                {
                    query = ((TemplateScalarModel) arg2).getAsString();
                }
                else
                {
                    throw new TemplateModelException("Invalid query string");
                }

                SearchParameters sp = new SearchParameters();
                sp.addStore(storeRef);
                sp.setLanguage(language);
                sp.setQuery(query);

                ResultSet results = null;

                try
                {
                    results = serviceRegistry.getSearchService().query(sp);

                    if (results.length() == 0)
                    {
                        throw new TemplateModelException("No nodes selected");
                    }

                    else if (results.length() == 1)
                    {
                        return results.getNodeRef(0).toString();
                    }
                    else
                    {
                        throw new TemplateModelException("More than one node selected");
                    }
                }
                finally
                {
                    if (results != null)
                    {
                        results.close();
                    }
                }
            }
            else
            {
                throw new TemplateModelException("Incorrect arguments");
            }
        }
    }

    /**
     * Function to generate the date range portion of a lucene query
     * 
     * @author Andy Hind
     */
    private static class LuceneDateRangeFunction implements TemplateMethodModelEx
    {
        @SuppressWarnings("rawtypes")
        public Object exec(List args) throws TemplateModelException
        {
            if (args.size() == 2)
            {

                Object arg0 = args.get(0);
                Object arg1 = args.get(1);

                Date startDate = null;
                Date endDate = null;

                if (arg0 instanceof TemplateDateModel)
                {
                    startDate = (Date) ((TemplateDateModel) arg0).getAsDate();
                }
                else if (arg0 instanceof TemplateScalarModel)
                {
                    String startDateString = ((TemplateScalarModel) arg0).getAsString();
                    startDate = ISO8601DateFormat.parse(startDateString);
                }
                else
                {
                    throw new TemplateModelException("Invalid date entry");
                }

                if (arg1 instanceof TemplateDateModel)
                {
                    endDate = (Date) ((TemplateDateModel) arg0).getAsDate();
                }
                else if (arg1 instanceof TemplateScalarModel)
                {

                    String valueString = ((TemplateScalarModel) arg1).getAsString();
                    try
                    {
                        Duration duration = new Duration(valueString);
                        endDate = Duration.add(startDate, duration);
                    }
                    catch (Exception e)
                    {
                        endDate = ISO8601DateFormat.parse(valueString);
                    }
                }
                else
                {
                    throw new TemplateModelException("Invalid date entry");
                }

                if (startDate.compareTo(endDate) > 0)
                {
                    Date temp = startDate;
                    startDate = endDate;
                    endDate = temp;
                }

                StringBuilder builder = new StringBuilder();
                builder.append("[");
                builder.append(DefaultTypeConverter.INSTANCE.convert(String.class, startDate));
                builder.append(" TO ");
                builder.append(DefaultTypeConverter.INSTANCE.convert(String.class, endDate));
                builder.append("]");

                return builder.toString();

            }
            else
            {
                throw new TemplateModelException("Invalid date entry");
            }

        }

    }

    /**
     * The name of the template engine for which this model applies. In this case, "freemarker".
     */
    public String getTemplateEngine()
    {
        return "freemarker";
    }

}
