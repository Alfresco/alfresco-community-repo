/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

alfresco.xforms.constants.XFORMS_CSS_SELECT_QUERY = "input:enabled,area:tabindex,button:tabindex,object:tabindex,select:tabindex,textarea:tabindex";

alfresco.xforms.constants.XFORMS_FOCUSABLE_ELEMENTS = ["INPUT", "AREA", "BUTTON", "OBJECT", "SELECT", "TEXTAREA"];

/**
 * This plugin introduces a possibility of navigation into and from TinyMCE RTE.<br />
 * <br />
 * Supported options:<br />
 * - backward_element_classes - break-separated CSS class names of element which should be navigated on Shift + Tab;<br />
 * - forward_element_classes - break-separated CSS class names of element which should be navigated on Tab;<br />
 * - editor_condition - statement of the next form: &lt;attribute_name>=&lt;value> which allows to identify position between toolbars and editor areas.
 *   String values MUST BE wrapped into quotes;<br />
 * - form_scope_id - id of an element which contains all elements that should participate in tab navigating
 * 
 * @author Dmitry Velichkevich
 */
(function()
{
   var DOM = tinymce.DOM, Event = tinymce.dom.Event, each = tinymce.each, explode = tinymce.explode;

   tinymce.create('tinymce.plugins.XFormsTabFocusPlugin',
   {
      init: function (ed, url)
      {
         function tabCancel (ed, e)
         {
            var code = (e.which) ? (e.wich) : (e.keyCode);
            if (9 === code)
            {
               return Event.cancel(e);
            }
         };

         function tabHandler (ed, e)
         {
            function find (options)
            {
               var edIndex = -1;

               var query = alfresco.xforms.constants.XFORMS_CSS_SELECT_QUERY;
               if ((null != options.editorCondition) && (options.editorCondition.length > 0))
               {
                  query += ",*[" + options.editorCondition + "]";
               }
               var forward = ((null != options.forwardClasses) && (options.forwardClasses.length > 0)) ? (explode(options.forwardClasses, " ")) : (null);
               var backward = ((null != options.backwardClasses) && (options.backwardClasses.length > 0)) ? (explode(options.backwardClasses, " ")) : (null);

               function extendQuery(elements)
               {
                  each(elements, function(element)
                  {
                     query += ',a[class~="' + element + '"]';
                     return true;
                  });
               };

               if (forward)
               {
                  extendQuery(forward);
               }
               if (backward)
               {
                  extendQuery(backward);
               }

               var el = ((null != options.formScopeId) && (options.formScopeId.length > 0)) ? (DOM.select(query, document.getElementById(options.formScopeId))) : (DOM.select(query));

               if (el)
               {
                  var ed = DOM.select('*[' + options.editorCondition + ']');
                  each(el, function(element, index)
                  {
                    if (ed.toString() == element.toString())
                    {
                       edIndex = index;
                       return false;
                    }

                    return true;
                  });

                  function containsClasses(classes, element)
                  {
                     if (null == element)
                     {
                        return false;
                     }

                     for (var i = 0; i < classes.length; i++)
                     {
                        if (!DOM.hasClass(element, classes[i]))
                        {
                           return false;
                        }
                     }

                     return true;
                  };

                  for (var i = (edIndex + options.direction); (options.direction > 0) ? (i < el.length) : (i >= 0); i += options.direction)
                  {
                     if (!el[i].hidden && ("none" != el[i].style.display) && (el[i].tabIndex >= 0))
                     {
                        if ((("A" == el[i].tagName) && containsClasses((options.direction > 0) ? (forward) : (backward), el[i])) || (-1 != alfresco.xforms.constants.XFORMS_FOCUSABLE_ELEMENTS.indexOf(el[i].tagName)))
                        {
                           return el[i];
                        }
                     }
                  }
               }

               return null;
            };

            var code = (e.which) ? (e.which) : (e.keyCode);

            if (9 === code)
            {
               var direction = (e.shiftKey) ? (-1) : (1);

               var options = 
               {
                  "direction": direction,
                  "formScopeId": ed.getParam("form_scope_id"),
                  "editorCondition": ed.getParam("editor_condition"),
                  "forwardClasses": ed.getParam("forward_element_classes"),
                  "backwardClasses": ed.getParam("backward_element_classes")
               };

               var el = find(options);

               if (el)
               {
                  var editor = ed;
                  if (ed = tinymce.EditorManager.get(el.id || el.name))
                  {
                     ed.focus();
                  }
                  else
                  {
                    window.setTimeout(function ()
                    {
                       window.focus();

                       var focusHandler = editor.getParam("pre_focus_changed_handler");
                       if ((null != focusHandler) && ("function" == typeof(focusHandler)))
                       {
                          focusHandler.call(focusHandler, direction);
                       }

                       el.focus();
                    }, 10);
                  }

                  return Event.cancel(e);
               }
            }

            return true;
         };

         ed.onKeyUp.add(tabCancel);

         if (tinymce.isGecko)
         {
            ed.onKeyPress.add(tabHandler);
            ed.onKeyDown.add(tabCancel);
         }
         else
         {
            ed.onKeyDown.add(tabHandler);
         }
      },

      getInfo: function ()
      {
         var result =
         {
            "longname": "XFormsTabFocus",
            "author": "Dmitry Velichkevich",
            "infourl": "http://alfresco.com/",
            "version": tinymce.majorVersion + "." + tinymce.minorVersion
         };
         return result;
      }
   });

   tinymce.PluginManager.add("xformstabfocus", tinymce.plugins.XFormsTabFocusPlugin);
})();
