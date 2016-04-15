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
package org.alfresco.repo.admin.patch.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionModel;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.GUID;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.helpers.AttributesImpl;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Updates a XML import file to be compatable with the current version of the repository.
 * 
 * @author royw
 */
public class ImportFileUpdater 
{
	/** Indent size **/
	private static int INDENT_SIZE = 2;
	
	/** The destination export version number **/
	private static String EXPORT_VERSION = "1.4.0";
    
    /** Default encoding **/
    private static String DEFAULT_ENCODING = "UTF-8";
    
    /** File encoding */
    private String fileEncoding = DEFAULT_ENCODING;
	
	/** Element names **/
	private static String NAME_EXPORTER_VERSION = "exporterVersion";
	private static String NAME_RULE = "rule";
	
	/** The current import version number **/
	private String version;
	private boolean shownWarning = false;
    
    /**
     * Set the file encoding.
     * 
     * @param fileEncoding  the file encoding
     */
    public void setFileEncoding(String fileEncoding)
    {
        this.fileEncoding = fileEncoding;
    }

	/**
	 * Updates the passed import file into the equivalent 1.4 format.
	 * 
	 * @param source		the source import file
	 * @param destination	the destination import file
	 */
	public void updateImportFile(String source, String destination)
	{
		XmlPullParser reader = getReader(source);
		XMLWriter writer = getWriter(destination);
		this.shownWarning = false;
		
		try
		{
			// Start the documentation
			writer.startDocument();
			
			// Start reading the document
			int eventType = reader.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) 
	        {
	            if (eventType == XmlPullParser.START_TAG) 
				{
	            	ImportFileUpdater.this.outputCurrentElement(reader, writer, new OutputChildren());
				} 
				eventType = reader.next();
	        }
			
			// End and close the document
			writer.endDocument();
			writer.close();
		}
		catch (Exception exception)
		{
			throw new AlfrescoRuntimeException("Unable to update import file.", exception);
		}
		
	}

	/**
	 * Get the reader for the source import file
	 * 
	 * @param source	the source import file
	 * @return			the XML pull parser used to read the file
	 */
	private XmlPullParser getReader(String source)
	{
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
			factory.setNamespaceAware(true);
	    
            InputStream inputStream = new FileInputStream(source);
            Reader inputReader = new InputStreamReader(inputStream, this.fileEncoding);
            
			XmlPullParser xpp = factory.newPullParser();
			xpp.setInput(new BufferedReader(inputReader));
			
			return xpp;
		}
		catch (XmlPullParserException exception)
		{
			throw new AlfrescoRuntimeException("Unable to update import file.", exception);
		}
		catch (FileNotFoundException fileNotFound)
		{
			throw new AlfrescoRuntimeException("The source file could not be loaded.", fileNotFound);
		}
        catch (UnsupportedEncodingException exception)
        {
            throw new AlfrescoRuntimeException("Unsupported encoding", exception);
        }
	}
	
	/**
	 * Get the writer for the import file
	 * 
	 * @param destination	the destination XML import file
	 * @return				the XML writer
	 */
	private XMLWriter getWriter(String destination)
	{
		try
		{
			 // Define output format
	        OutputFormat format = OutputFormat.createPrettyPrint();
	        format.setNewLineAfterDeclaration(false);
	        format.setIndentSize(INDENT_SIZE);
	        format.setEncoding(this.fileEncoding);
	
	        return new XMLWriter(new FileOutputStream(destination), format);
		}
        catch (Exception exception)
        {
        	throw new AlfrescoRuntimeException("Unable to create XML writer.", exception);
        }
	}
	
	private void outputCurrentElement(XmlPullParser reader, XMLWriter writer, Work work)
	throws Exception
	{
		outputCurrentElement(reader, writer, work, true);
	}
	
	private void outputCurrentElement(XmlPullParser reader, XMLWriter writer, Work work, boolean checkForCallbacks)
		throws Exception
	{
		if (checkForCallbacks == false || checkForCallbacks(reader, writer) == false)
		{
			//	Get the name details of the element
			String name = reader.getName();
			String namespace = reader.getNamespace();
			String prefix = reader.getPrefix();
							
			// Sort out namespaces
			Map<String, String> nss = new HashMap<String, String>();
			int nsStart = reader.getNamespaceCount(reader.getDepth()-1);
		    int nsEnd = reader.getNamespaceCount(reader.getDepth());
		    for (int i = nsStart; i < nsEnd; i++) 
		    {
		    	String nsPrefix = reader.getNamespacePrefix(i);
		        String ns = reader.getNamespaceUri(i);
		        nss.put(nsPrefix, ns);   
		    }		
			
			// Sort out attributes
			AttributesImpl attributes = new AttributesImpl();
			for (int i = 0; i < reader.getAttributeCount(); i++) 
			{
				String attributeName = reader.getAttributeName(i);
				String attributeNamespace = reader.getAttributeNamespace(i);
				String attributePrefix = reader.getAttributePrefix(i);
				String attributeType = reader.getAttributeType(i);
				String attributeValue = reader.getAttributeValue(i);
				
				attributes.addAttribute(attributeNamespace, attributeName, attributePrefix+":"+attributeName, attributeType, attributeValue);			
			}		
			
			// Start the namespace prefixes
			for (Map.Entry<String, String> entry : nss.entrySet()) 
			{
				writer.startPrefixMapping(entry.getKey(), entry.getValue());
			}
			
			// Write the start of the element
			writer.startElement(namespace, name, prefix+":"+name, attributes);
			
			// Do the work
			work.doWork(reader, writer);
			
			// Write the end of the element
			writer.endElement(namespace, name, prefix+":"+name);
			
			// End the namespace prefixes
			for (String nsPrefix : nss.keySet()) 
			{
				writer.endPrefixMapping(nsPrefix);
			}
		}
	}
	
	private boolean checkForCallbacks(XmlPullParser reader, XMLWriter writer) 
		throws Exception
	{
		boolean result = false;
		if (reader.getName().equals(NAME_EXPORTER_VERSION) == true)
		{
			new ImportVersionLabelCallback().doCallback(reader, writer);
			result = true;
		}
		else if (reader.getName().equals(NAME_RULE) == true)
		{
			if (this.shownWarning == false && this.version == null)
			{
				System.out.println("WARNING:  No version information has been found in this import file.  It will be presumed it has been exported from 1.3");
				this.shownWarning = true;
			}
			if (this.version == null || this.version.startsWith("1.3") == true || this.version.startsWith("1.2") == true)
			{
				new RuleCallback().doCallback(reader, writer);
				result = true;
			}
			else
			{
				throw new RuntimeException("Import files of version " + this.version + " are not supported by this tool.");
			}
		}
		return result;
	}

	private interface Work
	{
		void doWork(XmlPullParser reader, XMLWriter writer)
			throws Exception;
	}
	
	private class OutputChildren implements Work
	{
		public void doWork(XmlPullParser reader, XMLWriter writer)
			throws Exception
		{
			// Deal with the contents of the tag
			int eventType = reader.getEventType();
			while (eventType != XmlPullParser.END_TAG) 
	        {
				eventType = reader.next();
				if (eventType == XmlPullParser.START_TAG) 
				{
					ImportFileUpdater.this.outputCurrentElement(reader, writer, new OutputChildren());
				}
				else if (eventType == XmlPullParser.TEXT)
				{
					// Write the text to the output file
					writer.write(reader.getText());
				}
	        }			
		}		
	}
	
	@SuppressWarnings("unused")
	private class IgnoreChildren implements Work
	{
		public void doWork(XmlPullParser reader, XMLWriter writer)
			throws Exception
		{
			int eventType = reader.getEventType();
			while (eventType != XmlPullParser.END_TAG) 
	        {
				eventType = reader.next();
				if (eventType == XmlPullParser.START_TAG) 
				{
					doWork(reader, writer);
				}
	        }			
		}		
	}
	
	private interface ImportUpdaterCallback
	{
		void doCallback(XmlPullParser reader, XMLWriter writer)
			throws Exception;
	}
	
	private class ImportVersionLabelCallback implements ImportUpdaterCallback
	{
		public void doCallback(XmlPullParser reader, XMLWriter writer) 
			throws Exception
		{			
			ImportFileUpdater.this.outputCurrentElement(reader, writer, 
				new Work()
				{
					public void doWork(XmlPullParser reader, XMLWriter writer) throws Exception 
					{
						reader.next();
						ImportFileUpdater.this.version = reader.getText();
						writer.write(EXPORT_VERSION);
						reader.next();
					}			
				}, false);
		}		
	}
	
	private class RuleCallback implements ImportUpdaterCallback
	{
		public void doCallback(XmlPullParser reader, XMLWriter writer) 
			throws Exception 
		{
			// Get the name details of the element
			String name = reader.getName();
			String namespace = reader.getNamespace();
			String prefix = reader.getPrefix();
			
			// Rename the child assoc appropriately
			AttributesImpl attributes = new AttributesImpl();
			String attributeName = reader.getAttributeName(0);
			String attributeNamespace = reader.getAttributeNamespace(0);
			String attributePrefix = reader.getAttributePrefix(0);
			String attributeType = reader.getAttributeType(0);
			String attributeValue = reader.getAttributeValue(0) + GUID.generate();			
			attributes.addAttribute(attributeNamespace, attributeName, attributePrefix+":"+attributeName, attributeType, attributeValue);
			
			// Output the rules element
			writer.startElement(namespace, name, prefix+":"+name, attributes);
			
			int eventType = reader.getEventType();
			while (eventType != XmlPullParser.END_TAG) 
	        {
				eventType = reader.next();
				if (eventType == XmlPullParser.START_TAG)
				{
					String childName = reader.getName();
					if (childName.equals("aspects") == true)
					{
						ImportFileUpdater.this.outputCurrentElement(reader, writer, 
								new Work()
								{
									public void doWork(XmlPullParser reader, XMLWriter writer) throws Exception 
									{										
										// Add titled aspect
										writer.startElement(
												ContentModel.ASPECT_TITLED.getNamespaceURI(), 
												ContentModel.ASPECT_TITLED.getLocalName(), 
												NamespaceService.CONTENT_MODEL_PREFIX + ":" + ContentModel.ASPECT_TITLED.getLocalName(), 
												new AttributesImpl());
										writer.endElement(
												ContentModel.ASPECT_TITLED.getNamespaceURI(), 
												ContentModel.ASPECT_TITLED.getLocalName(), 
												NamespaceService.CONTENT_MODEL_PREFIX + ":" + ContentModel.ASPECT_TITLED.getLocalName());
										
										// Read the rest of the elements and output
										int eventType = reader.getEventType();
										while (eventType != XmlPullParser.END_TAG) 
								        {
											eventType = reader.next();
											if (eventType == XmlPullParser.START_TAG)
											{
												ImportFileUpdater.this.outputCurrentElement(reader, writer, new OutputChildren());
											}
								        }										
									}
							
								}, false);
					}
					else if (childName.equals("properties") == true)
					{
						ImportFileUpdater.this.outputCurrentElement(reader, writer, 
								new Work()
								{
									public void doWork(XmlPullParser reader, XMLWriter writer) throws Exception 
									{
										int eventType = reader.getEventType();
										while (eventType != XmlPullParser.END_TAG) 
								        {
											eventType = reader.next();
											if (eventType == XmlPullParser.START_TAG)
											{
												String propName = reader.getName();
												if (propName.equals("actionDescription") == true)
												{
													writer.startElement(
															ContentModel.PROP_DESCRIPTION.getNamespaceURI(),
															ContentModel.PROP_DESCRIPTION.getLocalName(),
															NamespaceService.CONTENT_MODEL_PREFIX + ":" + ContentModel.PROP_DESCRIPTION.getLocalName(),
															new AttributesImpl());
													
													// Output the value within
													new OutputChildren().doWork(reader, writer);
													
													writer.endElement(
															ContentModel.PROP_DESCRIPTION.getNamespaceURI(),
															ContentModel.PROP_DESCRIPTION.getLocalName(),
															NamespaceService.CONTENT_MODEL_PREFIX + ":" + ContentModel.PROP_DESCRIPTION.getLocalName());
													eventType = reader.next();
													
												}
												else if (propName.equals("actionTitle") == true)
												{
													writer.startElement(
															ContentModel.PROP_TITLE.getNamespaceURI(),
															ContentModel.PROP_TITLE.getLocalName(),
															NamespaceService.CONTENT_MODEL_PREFIX + ":" + ContentModel.PROP_TITLE.getLocalName(),
															new AttributesImpl());
													
													// Output the value within
													new OutputChildren().doWork(reader, writer);
													
													writer.endElement(
															ContentModel.PROP_TITLE.getNamespaceURI(),
															ContentModel.PROP_TITLE.getLocalName(),
															NamespaceService.CONTENT_MODEL_PREFIX + ":" + ContentModel.PROP_TITLE.getLocalName());
													eventType = reader.next();
												}
												else if (propName.equals("executeAsynchronously") == true)
												{
													writer.startElement(
															RuleModel.PROP_EXECUTE_ASYNC.getNamespaceURI(),
															RuleModel.PROP_EXECUTE_ASYNC.getLocalName(),
															RuleModel.RULE_MODEL_PREFIX + ":" + RuleModel.PROP_EXECUTE_ASYNC.getLocalName(),
															new AttributesImpl());
													
													// Output the value within
													new OutputChildren().doWork(reader, writer);
													
													writer.endElement(
															RuleModel.PROP_EXECUTE_ASYNC.getNamespaceURI(),
															RuleModel.PROP_EXECUTE_ASYNC.getLocalName(),
															RuleModel.RULE_MODEL_PREFIX + ":" + RuleModel.PROP_EXECUTE_ASYNC.getLocalName());
													eventType = reader.next();
												}
												else if (propName.equals("ruleType") == true)
												{
													ImportFileUpdater.this.outputCurrentElement(reader, writer, 
														new Work()
														{
															public void doWork(XmlPullParser reader, XMLWriter writer) throws Exception 
															{
																// Output the elements that contain a multi values property
																writer.startElement(NamespaceService.REPOSITORY_VIEW_1_0_URI, "values", "view:values", new AttributesImpl());
																writer.startElement(NamespaceService.REPOSITORY_VIEW_1_0_URI, "value", "view:value", new AttributesImpl());
																
																//	Output the value within
																new OutputChildren().doWork(reader, writer);
																
																// End the multi values elements
																writer.endElement(NamespaceService.REPOSITORY_VIEW_PREFIX, "value", "view:value");
																writer.endElement(NamespaceService.REPOSITORY_VIEW_PREFIX, "values", "view:values");
															}
														}, false);
												}
												else if (propName.equals("definitionName") == true)
												{
													// Skip past next end
													while (eventType != XmlPullParser.END_TAG) 
											        {
														eventType = reader.next();
											        }
													eventType = reader.next();
												}
												else
												{
													ImportFileUpdater.this.outputCurrentElement(reader, writer, new OutputChildren());
												}								
											}
								        }
										
										// Output value for the disabled property
										writer.startElement(
												RuleModel.PROP_DISABLED.getNamespaceURI(),
												RuleModel.PROP_DISABLED.getLocalName(),
												RuleModel.RULE_MODEL_PREFIX + ":" + RuleModel.PROP_DISABLED.getLocalName(),
												new AttributesImpl());
										writer.write("false");
										writer.endElement(
												RuleModel.PROP_DISABLED.getNamespaceURI(),
												RuleModel.PROP_DISABLED.getLocalName(),
												RuleModel.RULE_MODEL_PREFIX + ":" + RuleModel.PROP_DISABLED.getLocalName());
									}
								}, false);						
					}
					else if (childName.equals("associations") == true)
					{
						ImportFileUpdater.this.outputCurrentElement(reader, writer, 
							new Work()
							{
								public void doWork(XmlPullParser reader, XMLWriter writer) throws Exception 
								{
									// <rule:action>
									writer.startElement(
											RuleModel.ASSOC_ACTION.getNamespaceURI(),
											RuleModel.ASSOC_ACTION.getLocalName(),
											RuleModel.RULE_MODEL_PREFIX + ":" + RuleModel.ASSOC_ACTION.getLocalName(),
											new AttributesImpl());
									
									// <act:compositeaction view:childName="rule:action">
									AttributesImpl attributes = new AttributesImpl();
									attributes.addAttribute(NamespaceService.REPOSITORY_VIEW_1_0_URI, "childName", "view:childName", null, "rule:action");
									writer.startElement(
											ActionModel.TYPE_COMPOSITE_ACTION.getNamespaceURI(),
											ActionModel.TYPE_COMPOSITE_ACTION.getLocalName(),
											ActionModel.ACTION_MODEL_PREFIX+ ":" + ActionModel.TYPE_COMPOSITE_ACTION.getLocalName(),
											attributes);								
									
									// <view:properties>
									writer.startElement(
											NamespaceService.REPOSITORY_VIEW_1_0_URI,
											"properties",
											"view:properties",
											new AttributesImpl());
									
									// <act:definitionName>composite-action</definitionName>
									writer.startElement(
											ActionModel.PROP_DEFINITION_NAME.getNamespaceURI(),
											ActionModel.PROP_DEFINITION_NAME.getLocalName(),
											ActionModel.ACTION_MODEL_PREFIX + ":" + ActionModel.PROP_DEFINITION_NAME.getLocalName(),
											new AttributesImpl());
									writer.write("composite-action");
									writer.endElement(
											ActionModel.PROP_DEFINITION_NAME.getNamespaceURI(),
											ActionModel.PROP_DEFINITION_NAME.getLocalName(),
											ActionModel.ACTION_MODEL_PREFIX + ":" + ActionModel.PROP_DEFINITION_NAME.getLocalName());
									
									// </view:properties>
									writer.endElement(
											NamespaceService.REPOSITORY_VIEW_1_0_URI,
											"properties",
											"view:properties");
									
									// <view:association>
									writer.startElement(
											NamespaceService.REPOSITORY_VIEW_1_0_URI,
											"associations",
											"view:associations",
											new AttributesImpl());
									
									// Output the association details
									new OutputChildren().doWork(reader, writer);
									
									// </view:association>
									writer.endElement(
											NamespaceService.REPOSITORY_VIEW_1_0_URI,
											"associations",
											"view:associations");
									
									// </act:compositeaction>
									writer.endElement(
											ActionModel.TYPE_COMPOSITE_ACTION.getNamespaceURI(),
											ActionModel.TYPE_COMPOSITE_ACTION.getLocalName(),
											ActionModel.ACTION_MODEL_PREFIX+ ":" + ActionModel.TYPE_COMPOSITE_ACTION.getLocalName());
									
									// </rule:action>
									writer.endElement(
											RuleModel.ASSOC_ACTION.getNamespaceURI(),
											RuleModel.ASSOC_ACTION.getLocalName(),
											RuleModel.RULE_MODEL_PREFIX + ":" + RuleModel.ASSOC_ACTION.getLocalName());
								}
							}, false);
					}
					else
					{
						// Output anything else that might be hanging araound
						ImportFileUpdater.this.outputCurrentElement(reader, writer, new OutputChildren());
					}
				}
	        }
			
			// End the rules element
			writer.endElement(namespace, name, prefix+":"+name);
		}		
	}
	
	public static void main(String[] args) 
	{
		if (args.length == 2)
		{
			ImportFileUpdater util = new ImportFileUpdater();
			util.updateImportFile(args[0], args[1]);
		}
        else if (args.length == 3)
        {
            ImportFileUpdater util = new ImportFileUpdater();
            util.setFileEncoding(args[2]);
            util.updateImportFile(args[0], args[1]);
        }
		else
		{
			System.out.println(" ImportFileUpdater <source> <destination>");
			System.out.println("    source - 1.3 import file name to be updated");
			System.out.println("    destination - name of the generated 1.4 import file");
            System.out.println("    file encoding (optional) - the file encoding, default is UTF-8");
		}
	}

}
