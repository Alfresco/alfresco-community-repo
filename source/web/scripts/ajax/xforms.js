dojo.require("dojo.widget.DebugConsole");
dojo.require("dojo.widget.DatePicker");
dojo.require("dojo.widget.Button");
dojo.require("dojo.widget.validate");
dojo.require("dojo.widget.ComboBox");
dojo.require("dojo.widget.Checkbox");
dojo.require("dojo.widget.Editor");
dojo.require("dojo.widget.Spinner");
dojo.require("dojo.html.style");
dojo.hostenv.writeIncludes();
dojo.addOnLoad(xforms_init);

tinyMCE.init({
  theme : "advanced",
  mode : "exact",
      //	elements : "editor",
  save_callback : "saveContent",
  add_unload_trigger: false,
  add_form_submit_trigger: false,
  theme_advanced_toolbar_location : "top",
  theme_advanced_toolbar_align : "left",
  theme_advanced_buttons1_add : "fontselect,fontsizeselect",
  theme_advanced_buttons2_add : "separator,forecolor,backcolor",
  theme_advanced_disable: "styleselect",
  extended_valid_elements : "a[href|target|name],font[face|size|color|style],span[class|align|style]"
});

//dojo.provide("alfresco.xforms.textarea");
//
//dojo.declare("alfresco.xforms.Widget",
//	     null,
//	     function(node) 
//	     {
//	       this.node = node;
//	     },
//	     id: "",
//	     isRequired: function()
//	     {
//	     },
//	     getInitialValue: function()
//	     {
//	     });
//
//dojo.declae("alfresco.xforms.TextArea",
//	     alfresco.xforms.Widget,
//	     function() { },
//	     
//{
//  alert(element);
//}

var bindings = {};
var xform = null;
function xforms_init()
{
  var req = {
  url: WEBAPP_CONTEXT + "/ajax/invoke/XFormsBean.getXForm",
  content: { },
  mimetype: "text/xml",
  load: function(type, data, evt)
  {
    xform = data.documentElement;
    var model = xform.getElementsByTagName("model");
    load_bindings(model[0]);
    for (var i in bindings)
    {
      dojo.debug("bindings[" + i + "]=" + bindings[i].id + 
		 ", parent = " + (bindings[i].parent ? bindings[i].parent.id : 'null'));
    }
    
    var body = xform.getElementsByTagName("body");
    load_body(body[body.length - 1], [ document.getElementById("alf-ui") ]);
  },
  error: function(type, e)
  {
    alert("error!! " + type + " e = " + e.message);
  }
  };
  dojo.io.bind(req);
}

function get_instance()
{
  var model = xform.getElementsByTagName("model")[0];
  return model.getElementsByTagName("instance")[0];
}

function load_bindings(bind, parent)
{
  dojo.debug("loading bindings for " + bind.nodeName);
  dojo.lang.forEach(bind.childNodes, function(b)
  {
    if (b.nodeName.toLowerCase() == "xforms:bind")
    {
      var id = b.getAttribute("id");
      dojo.debug("loading binding " + id);
      bindings[id] = {
        id: b.getAttribute("id"),
        required: b.getAttribute("xforms:required"),
        nodeset: b.getAttribute("xforms:nodeset"),
        type: b.getAttribute("xforms:type"),
	parent: parent
      }
      load_bindings(b, bindings[id]);
    }
  });
}

function load_body(body, ui_element_stack)
{
  dojo.lang.forEach(body.childNodes, function(o)
  {
    dojo.debug("loading " + o + " NN " + o.nodeName);
    switch (o.nodeName.toLowerCase())
    {
    case "xforms:group":
      if (ui_element_stack[ui_element_stack.length - 1].nodeName.toLowerCase() == "table")
      {
	var tr = document.createElement("tr");
	var td = document.createElement("td");
	td.setAttribute("colspan", "3");
	tr.appendChild(td);
	ui_element_stack[ui_element_stack.length - 1].appendChild(tr);
	ui_element_stack.push(td);
      }
      var table = document.createElement("table");
      table.setAttribute("style", "width:100%; border: 0px solid blue;");
      ui_element_stack[ui_element_stack.length - 1].appendChild(table);
      ui_element_stack.push(table);
      load_body(o, ui_element_stack);
      ui_element_stack.pop();
      if (ui_element_stack[ui_element_stack.length - 1].nodeName.toLowerCase() == "td")
	ui_element_stack.pop();
      break;
    case "xforms:textarea":
      //      new alfresco.xforms.textarea(o);
      var row = document.createElement("tr");
      ui_element_stack[ui_element_stack.length - 1].appendChild(row);

      var cell = document.createElement("td");
      row.appendChild(cell);
      add_required_cell(cell, o);

      var cell = document.createElement("td");
      row.appendChild(cell);
      var label = get_label_node(o);
      if (label)
        cell.appendChild(document.createTextNode(dojo.dom.textContent(label)));
      cell = document.createElement("td");
      row.appendChild(cell);
      var nodeRef = document.createElement("div");
      nodeRef.setAttribute("style", "height: 200px; width: 100%; border: solid 1px black;");
      cell.appendChild(nodeRef);
      var id = o.getAttribute("id");
      nodeRef.setAttribute("id", id);
      var initial_value = get_initial_value(o)  || "";
      nodeRef.innerHTML = initial_value;
      tinyMCE.addMCEControl(nodeRef, id);
//      var w = dojo.widget.createWidget("Editor", 
//				       { 
//					   widgetId: id,
//					   focusOnLoad: false,
//					   items: [ "|", "bold", "italic", "underline", "strikethrough", "|", "colorGroup", "|", "createLink", "insertImage" ] 
//				       }, 
//				       nodeRef);
//      dojo.event.connect(w,
//			 "setRichText", 
//			 function(event)
//			 {
//			     dojo.event.connect(w._richText,
//						"onBlur",
//						function()
//						{
//						    setXFormsValue(w.widgetId, 
//								   w._richText.getEditorContent());
//						});
//						
//			 });
//
      break;
    case "xforms:input":
      var id = o.getAttribute("id");
      var row = document.createElement("tr");
      ui_element_stack[ui_element_stack.length - 1].appendChild(row);

      var cell = document.createElement("td");
      row.appendChild(cell);
      add_required_cell(cell, o);

      cell = document.createElement("td");
      row.appendChild(cell);
      var label = get_label_node(o);
      if (label)
      {
        cell.appendChild(document.createTextNode(dojo.dom.textContent(label)));
      }

      cell = document.createElement("td");
      row.appendChild(cell);
      var nodeRef = document.createElement("div");
      cell.appendChild(nodeRef);
      var initial_value = get_initial_value(o);
      switch (get_type(o))
      {
      case "date":
        initial_value = initial_value || dojo.widget.DatePicker.util.toRfcDate();
	var dateTextBoxDiv = document.createElement("div");
	nodeRef.appendChild(dateTextBoxDiv);
	var dateTextBox = dojo.widget.createWidget("DateTextBox", 
						   {
						   widgetId: id,
						   required: is_required(o), 
 						   format: "YYYY-MM-DD", 
						   value: initial_value 
						   }, 
						   dateTextBoxDiv);
	dateTextBox.onfocus = function(o) { 
	  dateTextBox.hide(); dojo.debug("hiding " + o); 
	  dateTextBox.picker.show();

	};
	var datePickerDiv = document.createElement("div");
	nodeRef.appendChild(datePickerDiv);
	dateTextBox.picker = dojo.widget.createWidget("DatePicker", 
						      { 
						      isHidden: true, 
						      value : initial_value 
						      }, 
						      datePickerDiv);
	dateTextBox.picker.hide();
	dojo.event.connect(dateTextBox.picker,
			   "onSetDate", 
			   function(event)
			   {
			     dateTextBox.picker.hide();
			     dateTextBox.show();
			     dateTextBox.setValue(dojo.widget.DatePicker.util.toRfcDate(dateTextBox.picker.date));
			     setXFormsValue(dateTextBox.widgetId, 
					    dateTextBox.getValue());
			   });
	break;
      case "integer":
      case "positiveInteger":
      case "negativeInteger":
        initial_value = initial_value || "";
	var w = dojo.widget.createWidget("SpinnerIntegerTextBox", 
					 { 
					 widgetId: id,
					 required: is_required(o), 
					 value: initial_value 
					 }, 
					 nodeRef);
	var handler = function(event)
	{
	  dojo.debug("value changed " + w.widgetId + 
		     " value " + w.getValue() + 
		     " t " + event.target + 
		     " w " + w + " this " + this);
	  setXFormsValue(w.widgetId, w.getValue());
	}
	dojo.event.connect(w, "adjustValue", handler);
	dojo.event.connect(w, "onkeyup", handler);
	break;
      case "double":
        initial_value = initial_value || "0";
	var w = dojo.widget.createWidget("SpinnerRealNumberTextBox", 
					 { 
					 widgetId: id,
					 required: is_required(o), 
					 value: initial_value 
					 }, 
					 nodeRef);
	var handler = function(event)
	{
	  dojo.debug("value changed " + w.widgetId + 
		     " value " + w.getValue() + 
		     " t " + event.target + 
		     " w " + w + " this " + this);
	  setXFormsValue(w.widgetId, w.getValue());
	}
	dojo.event.connect(w, "adjustValue", handler);
	dojo.event.connect(w, "onkeyup", handler);
	break;
      case "string":
      default:
        initial_value = initial_value || "";
	var w = dojo.widget.createWidget("ValidationTextBox", 
					 {
					 widgetId: id,
					 required: is_required(o), 
					 value: initial_value 
					 }, 
					 nodeRef);
	dojo.event.connect(w,
			   "onkeyup", 
			   function(event)
			   {
			     dojo.debug("value changed " + w.widgetId + " value " + w.getValue() + " t " + event.target + " w " + w + " this " + this);
			     setXFormsValue(w.widgetId, w.getValue());
			   });

      }
      break;
    case "xforms:select1":
      var row = document.createElement("tr");
      ui_element_stack[ui_element_stack.length - 1].appendChild(row);
      var cell = document.createElement("td");
      row.appendChild(cell);
      add_required_cell(cell, o);

      var cell = document.createElement("td");
      row.appendChild(cell);
      var label = get_label_node(o);
      if (label)
        cell.appendChild(document.createTextNode(dojo.dom.textContent(label)));
      cell = document.createElement("td");
      row.appendChild(cell);
      var nodeRef = document.createElement("div");
      cell.appendChild(nodeRef);
      var values = get_select_values(o);
      for (var i in values)
      {
	dojo.debug("values["+ i + "] = " + values[i].id + ", " + values[i].label + ", " + values[i].value);
      }
      var initial_value = get_initial_value(o);
      if (get_type(o) == "boolean")
      {
        initial_value = initial_value || false;
        var w = dojo.widget.createWidget("CheckBox", 
					 { 
					   widgetId: o.getAttribute('id'),
					   checked: initial_value 
					 },
					 nodeRef);
	dojo.event.connect(w,
			   "onClick",
			   function(event)
			   {
			     setXFormsValue(w.widgetId, w.checked);
			   });

      }
      else if (values.length <= 5)
      {
	for (var i in values)
	{
	  var radio = document.createElement("input");
	  radio.setAttribute("id", o.getAttribute("id"));
	  radio.setAttribute("name", o.getAttribute("id"));
	  radio.setAttribute("type", "radio");
	  radio.setAttribute("value", values[i].value);
	  if (values[i].value == initial_value)
	    radio.setAttribute("checked", "true");
	  radio.onclick = function(event) 
	  { 
	      setXFormsValue(this.getAttribute("id"),
			     this.value);
	  }
	  nodeRef.appendChild(radio);
	  nodeRef.appendChild(document.createTextNode(values[i].label));
	}
      }
      else
      {
        var combobox = document.createElement("select");
	combobox.setAttribute("id", o.getAttribute("id"));
	nodeRef.appendChild(combobox);
	for (var i in values)
	{
	  var option = document.createElement("option");
	  option.appendChild(document.createTextNode(values[i].label));
	  option.setAttribute("value", values[i].value);
	  if (values[i].value == initial_value)
	    option.setAttribute("selected", "true");
	  combobox.appendChild(option);
	}
	combobox.onchange = function(event) 
        { 
	    setXFormsValue(this.getAttribute("id"),
			   this.options[this.selectedIndex].value);
	}
      }
      break;
    case "xforms:submit":
      var id = o.getAttribute("id");
      var row = document.createElement("tr");
      ui_element_stack[ui_element_stack.length - 1].appendChild(row);
      var cell = document.createElement("td");
      cell.setAttribute("colspan", "3");
      row.appendChild(cell);
      var nodeRef = document.createElement("div");
      cell.appendChild(nodeRef);
      var w = dojo.widget.createWidget("Button", 
				       {
				       widgetId: id,
				       caption: "submit" 
				       }, 
				       nodeRef);
      w.hide();
      document.submitTrigger = w;
      document.submitTrigger.done = false;
      w.onClick = function()
      {
	fireAction(w.widgetId);
      };
      break;
    case "xforms:repeat":
      dojo.debug("repeat unimplemented");
      break;
    default:
      load_body(o, ui_element_stack);
    }
  });
}

function get_type(o)
{
  var binding = bindings[o.getAttribute("xforms:bind")];
  return binding.type;
}

function is_required(o)
{
  var binding = bindings[o.getAttribute("xforms:bind")];
  var required = binding.required == "true()";
  return required;
}

function add_required_cell(cell, element)
{
  if (is_required(element))
  {
    var req = document.createElement("img");
    req.setAttribute("src", WEBAPP_CONTEXT + "/images/icons/required_field.gif");
    req.setAttribute("style", "margin:5px");
    cell.appendChild(req);
  }
//  else
//    cell.appendChild(document.createTextNode("&nbsp;"));
}

function get_label_node(o)
{
  var labels = o.getElementsByTagName("label");
  for (var i = 0; i < labels.length; i++)
  {
    dojo.debug("parent " + labels[i].parentNode.nodeName + 
	       " o " + o.nodeName);
    if (labels[i].parentNode == o)
      return labels[i];
  }
  return null;
}

function get_initial_value(o)
{
  var b = bindings[o.getAttribute("xforms:bind")];
  var a = [];
  do
  {
    a.push(b);
    b = b.parent
  }
  while (b);
  var node = get_instance();
  for (var i = a.length - 1; i >= 0; i--)
  {
    var element_name = (a[i].nodeset.match(/^\//)
			? a[i].nodeset.replace(/^\/(.+)/, "$1")
			: a[i].nodeset);
    dojo.debug("locating " + a[i].nodeset + "(" + element_name + ")" +
	       " in " + node.nodeName);
    if (element_name.match(/^@/))
      return node.getAttribute(a[i].nodeset.replace(/^@(.+)/, "$1"));
    else if (element_name == '.')
      break;
    node = node.getElementsByTagName(element_name)[0];
    if (node)
	dojo.debug("got node " + node.nodeName);
    else
	return null;
  }
  return dojo.dom.textContent(node);
}
    
function get_select_values(o)
{
  var values = o.getElementsByTagName("item");
  var result = [];
  for (var v in values)
  {
    if (values[v].getElementsByTagName)
    {
      var label = values[v].getElementsByTagName("label")[0];
      var value = values[v].getElementsByTagName("value")[0];
      result.push({ 
	id: value.getAttribute("id"), 
        label: dojo.dom.textContent(label),
        value: dojo.dom.textContent(value)
      });
    }
  }
  return result;
}

function fireAction(id)
{
  var req = {
  url: WEBAPP_CONTEXT + "/ajax/invoke/XFormsBean.fireAction",
  content: { id: id },
  mimetype: "text/xml",
  load: function(type, data, evt)
  {
      document.submitTrigger.done = true;
      document.submitTrigger.currentButton.click();
      document.submitTrigger.currentButton = null;
  },
  error: function(type, e)
  {
    alert("error!! " + type + " e = " + e.message);
  }
  };
  dojo.io.bind(req);
}

function setXFormsValue(id, value)
{
  var req = {
  url: WEBAPP_CONTEXT + "/ajax/invoke/XFormsBean.setXFormsValue",
  content: { id: id, value: value },
  mimetype: "text/xml",
  load: function(type, data, evt)
  {
  },
  error: function(type, e)
  {
    alert("error!! " + type + " e = " + e.message);
  }
  };
  dojo.io.bind(req);
}

function addSubmitHandlerToButton(b)
{
  var baseOnClick = b.onclick;
  b.onclick = function(event)
  {
//    alert("submitting xform from " + b.getAttribute("id") + 
//	  " b " + b +
//	  " this " + this );
    if (!document.submitTrigger.done)
    {
      //      alert("not done, resubmitting");
      tinyMCE.triggerSave();
      document.submitTrigger.currentButton = this;
      document.submitTrigger.buttonClick(); 
      return false;
    }
    else
    {
      //     alert("done - doing base click");
      return baseOnClick(event);
    }
  }
}

function saveContent(id, content)
{
  setXFormsValue(id, content);
}
