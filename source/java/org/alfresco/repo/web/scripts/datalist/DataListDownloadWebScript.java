/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.datalist;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.web.scripts.DeclarativeSpreadsheetWebScript;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;


/**
 * Data List Download
 * 
 * Exports the contents of a Data List as an Excel file
 * 
 * @author Nick Burch
 */
public class DataListDownloadWebScript extends DeclarativeSpreadsheetWebScript
        implements InitializingBean 
{
    // Logger
    private static final Log logger = LogFactory.getLog(DataListDownloadWebScript.class);
    
    private static final QName DATA_LIST_ITEM_TYPE = 
        QName.createQName(NamespaceService.DATALIST_MODEL_1_0_URI, "dataListItemType");
    
    private NodeService nodeService;
    private SiteService siteService;
    private NamespaceService namespaceService;
    private Map<QName,List<QName>> modelOrder;
    private Map<String,String> rawModelOrder;

    public DataListDownloadWebScript()
    {
      this.filenameBase = "DataListExport";
    }

    /**
     * @param nodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService; 
    }

    /**
     * @param nodeService
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService; 
    }
    
    /**
     * @param namespaceService
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService; 
    }

    public void setModelOrder(Map<String,String> rawModelOrder)
    {
    	this.rawModelOrder = rawModelOrder;
    }
    
    
    @Override
    public void afterPropertiesSet() throws Exception {
       modelOrder = new HashMap<QName, List<QName>>();
       for(String key : rawModelOrder.keySet())
       {
          QName model;
          List<QName> order = new ArrayList<QName>();

          try
          {
             model= QName.createQName(key, namespaceService);
          }
          catch(InvalidQNameException e)
          {
             logger.warn("Skipping invalid model type " + key);
             continue;
          }

          StringTokenizer st = new StringTokenizer(rawModelOrder.get(key), ",");
          while(st.hasMoreTokens())
          {
             order.add( QName.createQName(st.nextToken(), namespaceService) );
          }
          modelOrder.put(model, order);
       }
    }
    
    /**
     * Identify the datalist
     */
    @Override
    protected Object identifyResource(String format, WebScriptRequest req) {
       // Try to find the datalist they requested
       NodeRef list;
       Map<String,String> args = req.getServiceMatch().getTemplateVars();
       if(args.get("store_type") != null)
       {
          list = new NodeRef(
                args.get("store_type"),
                args.get("store_id"),
                args.get("id")
          );
       }
       else
       {
          // Get the site
          SiteInfo site = siteService.getSite(args.get("site"));
          if(site == null)
          {
             throw new WebScriptException(Status.STATUS_NOT_FOUND, "Site not found with supplied name");
          }

          // Now find the data list container with in
          NodeRef container = nodeService.getChildByName(
                site.getNodeRef(),
                ContentModel.ASSOC_CONTAINS,
                args.get("container")
          );
          if(container == null)
          {
             throw new WebScriptException(Status.STATUS_NOT_FOUND, "Container not found within site");
          }

          // Now get the data list itself
          list = nodeService.getChildByName(
                container,
                ContentModel.ASSOC_CONTAINS,
                args.get("list")
          );
       }
       if(list == null || !nodeService.exists(list))
       {
          throw new WebScriptException(Status.STATUS_NOT_FOUND, "The Data List could not be found");
       }

       return list;
    }

    /**
     * We don't have a HTML version
     */
    @Override
    protected boolean allowHtmlFallback() {
       return false;
    }

    /**
     * Fetch the properties, in the requested order, from
     *  the data list definition
     */
    @Override
    protected List<Pair<QName, Boolean>> buildPropertiesForHeader(
          Object resource, String format, WebScriptRequest req) {
       NodeRef list = (NodeRef)resource;
       QName type = buildType(list);

       // Has the user given us rules for what to do
       //  with this type?
       List<QName> props;
       if(modelOrder.containsKey(type))
       {
          props = modelOrder.get(type);
       }
       else
       {
          // We'll have to try to guess it for them
          // For now, just use DataList properties for the type
          TypeDefinition typeDef = dictionaryService.getType(type);
          Map<QName, PropertyDefinition> allProps = typeDef.getProperties();
          props = new ArrayList<QName>();

          for(QName prop : allProps.keySet())
          {
             if(NamespaceService.DATALIST_MODEL_1_0_URI.equals(prop.getNamespaceURI()))
             {
                props.add(prop);
             }
          }
       }

       // Everything is required
       List<Pair<QName, Boolean>> properties = new ArrayList<Pair<QName,Boolean>>();
       for(QName qname : props)
       {
          properties.add(new Pair<QName, Boolean>(qname, true));
       }
       return properties;
    }

    private QName buildType(NodeRef list)
    {
       String typeS = (String)nodeService.getProperty(list, DATA_LIST_ITEM_TYPE);
       if(! typeS.startsWith(NamespaceService.DATALIST_MODEL_PREFIX + ":"))
       {
          throw new WebScriptException(Status.STATUS_NOT_IMPLEMENTED, "Unexpected list type " + typeS);
       }
       QName type = QName.createQName(NamespaceService.DATALIST_MODEL_1_0_URI, typeS.substring(typeS.indexOf(':')+1));
       return type;
    }

    private List<NodeRef> getItems(NodeRef list)
    {
       Set<QName> typeSet = new HashSet<QName>(Arrays.asList(new QName[] { buildType(list) }));

       List<NodeRef> items = new ArrayList<NodeRef>();
       for(ChildAssociationRef ca : nodeService.getChildAssocs(list, typeSet))
       {
          items.add(ca.getChildRef());
       }
       return items;
    }

    @Override
    protected void populateBody(Object resource, CSVPrinter csv,
          List<QName> properties) throws IOException {
       throw new WebScriptException(Status.STATUS_BAD_REQUEST, "CSV not currently supported");
    }

    @Override
    protected void populateBody(Object resource, Workbook workbook,
          Sheet sheet, List<QName> properties) throws IOException {
       NodeRef list = (NodeRef)resource;
       List<NodeRef> items = getItems(list);

       // Our various formats
       DataFormat formatter = workbook.createDataFormat();

       CellStyle styleInt = workbook.createCellStyle();
       styleInt.setDataFormat( formatter.getFormat("0") );
       CellStyle styleDate = workbook.createCellStyle();
       styleDate.setDataFormat( formatter.getFormat("yyyy-mm-dd") );
       CellStyle styleDouble = workbook.createCellStyle();
       styleDouble.setDataFormat( formatter.getFormat("General") );
       CellStyle styleNewLines = workbook.createCellStyle();
       styleNewLines.setWrapText(true);

       // Export the items
       int rowNum = 1, colNum = 0;
       for(NodeRef item : items)
       {
          Row r = sheet.createRow(rowNum);

          colNum = 0;
          for(QName prop : properties)
          {
             Cell c = r.createCell(colNum);

             Serializable val = nodeService.getProperty(item, prop);
             if(val == null)
             {
                // Is it an association, or just missing?
                List<AssociationRef> assocs =  nodeService.getTargetAssocs(item, prop);
                if(assocs.size() > 0)
                {
                   StringBuffer text = new StringBuffer();
                   int lines = 1;

                   for(AssociationRef ref : assocs)
                   {
                      NodeRef child = ref.getTargetRef();
                      QName type = nodeService.getType(child);
                      if(ContentModel.TYPE_PERSON.equals(type))
                      {
                         if(text.length() > 0) {
                            text.append('\n');
                            lines++;
                         }
                         text.append(nodeService.getProperty(
                               child, ContentModel.PROP_USERNAME
                         ));
                      }
                      else if(ContentModel.TYPE_CONTENT.equals(type))
                      {
                         // TODO Link to the content
                         if(text.length() > 0) {
                            text.append('\n');
                            lines++;
                         }
                         text.append(nodeService.getProperty(
                               child, ContentModel.PROP_TITLE
                         ));
                      }
                      else
                      {
                         System.err.println("TODO: handle " + type + " for " + child);
                      }
                   }

                   String v = text.toString();
                   c.setCellValue( v );
                   if(lines > 1) 
                   {
                      c.setCellStyle(styleNewLines);
                      r.setHeightInPoints( lines*sheet.getDefaultRowHeightInPoints() );
                   }
                }
                else
                {
                   // This property isn't set
                   c.setCellType(Cell.CELL_TYPE_BLANK);
                }
             }
             else
             {
                // Regular property, set
                if(val instanceof String)
                {
                   c.setCellValue((String)val);
                }
                else if(val instanceof Date)
                {
                   c.setCellValue((Date)val);
                   c.setCellStyle(styleDate);
                }
                else if(val instanceof Integer || val instanceof Long)
                {
                   double v = 0.0;
                   if(val instanceof Long) v = (double)(Long)val;
                   if(val instanceof Integer) v = (double)(Integer)val;
                   c.setCellValue(v);
                   c.setCellStyle(styleInt);
                }
                else if(val instanceof Float || val instanceof Double)
                {
                   double v = 0.0;
                   if(val instanceof Float) v = (double)(Float)val;
                   if(val instanceof Double) v = (double)(Double)val;
                   c.setCellValue(v);
                   c.setCellStyle(styleDouble);
                }
                else
                {
                   // TODO
                   System.err.println("TODO: handle " + val.getClass().getName() + " - " + val);
                }
             }

             colNum++;
          }

          rowNum++;
       }

       // Sensible column widths please!
       colNum = 0;
       for(QName prop : properties)
       {
          sheet.autoSizeColumn(colNum);
          colNum++;
       }
    }
}
