package org.alfresco.web.bean.generator;

/**
 * Generates a component to represent a separator using the HTML <code>&lt;hr/&gt;</code> element.
 * 
 * @author gavinc
 */
public class SeparatorGenerator extends HtmlSeparatorGenerator
{
   public SeparatorGenerator()
   {
      // For the standard separator just show a <hr/> element
      
      this.html = "<div style='margin-top: 6px; margin-bottom: 6px;'><hr/></div>";
   }
}