dojo.require("dojo.widget.DebugConsole");
dojo.require("dojo.widget.Button");
dojo.require("dojo.widget.validate");
dojo.require("dojo.widget.ComboBox");
dojo.require("dojo.widget.Checkbox");
dojo.require("dojo.widget.Editor");
dojo.require("dojo.widget.Spinner");
dojo.require("dojo.html.style");
dojo.hostenv.writeIncludes();
dojo.addOnLoad(xforms_init);

var bindings = {};
var xform = null;
function xforms_init()
{
  dojo.io.bind({
    url: xforms_url,
	mimetype: "text/xml",
	load: function(type, data, evt)
      {
	xform = data.documentElement;
	var model = xform.getElementsByTagName("model");
	load_bindings(model[0]);
	for (var i in bindings)
	{
	  dojo.debug("bindings["+i+"]="+bindings[i].id+", parent = "+ (bindings[i].parent ? bindings[i].parent.id : 'null'));
	}
	
	var body = xform.getElementsByTagName("body");
	load_body(body[0], [ document.getElementById("alf-ui") ]);
      },
	error: function(type, e)
      {
	alert("error " + type + " e = " + e);
      }
    });
}

function get_instance()
{
  var model = xform.getElementsByTagName("model")[0];
  return model.getElementsByTagName("instance")[0];
}

function load_bindings(bind, parent)
{
  dojo.lang.forEach(bind.childNodes, function(b)
  {
    if (b.nodeName.toLowerCase() == "xforms:bind")
    {
      var id = b.getAttribute("id");
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
    dojo.debug(o + " nn " + o.nodeName);
    switch (o.nodeName.toLowerCase())
    {
    case "xforms:group":
      var table = document.createElement("table");
      table.setAttribute("style", "width:100%");
      ui_element_stack[ui_element_stack.length - 1].appendChild(table);
      ui_element_stack.push(table);
      load_body(o, ui_element_stack);
      ui_element_stack.pop();
      break;
//    case "xforms:label":
//      var label = document.createElement("span");
//      label.appendChild(document.createTextNode(dojo.dom.textContent(o)));
//      ui_element_stack.last().appendChild(label);
//      break;
    case "xforms:textarea":
      var row = document.createElement("tr");
      ui_element_stack[ui_element_stack.length - 1].appendChild(row);
      var label = get_label_node(o);
      var cell = document.createElement("td");
      row.appendChild(cell);
      if (label)
        cell.appendChild(document.createTextNode(dojo.dom.textContent(label)));
      cell = document.createElement("td");
      row.appendChild(cell);
      var nodeRef = document.createElement("div");
      nodeRef.setAttribute("style", "height: 200px");
      cell.appendChild(nodeRef);
      var w = dojo.widget.createWidget("Editor", { items: ["|", "bold", "italic", "underline", "strikethrough", "|", "colorGroup", "|", "createLink", "insertImage" ] }, nodeRef);
      break;
    case "xforms:input":
      var binding = bindings[o.getAttribute("xforms:bind")];
      var row = document.createElement("tr");
      ui_element_stack[ui_element_stack.length - 1].appendChild(row);
      var label = get_label_node(o);
      var cell = document.createElement("td");
      row.appendChild(cell);
      var required = binding.required == "true()";
      if (label)
      {
	if (required)
	{
	  var req = document.createElement("img");
	  req.setAttribute("src", contextPath + "/images/icons/required_field.gif");
	  req.setAttribute("style", "margin:5px");
	  cell.appendChild(req);
	}
        cell.appendChild(document.createTextNode(dojo.dom.textContent(label)));
      }
      cell = document.createElement("td");
      row.appendChild(cell);
      var nodeRef = document.createElement("div");
      cell.appendChild(nodeRef);
      var value = get_initial_value(binding);
      switch (binding.type)
      {
      case "date":
	var dateTextBoxDiv = document.createElement("div");
	nodeRef.appendChild(dateTextBoxDiv);
	var dateTextBox = dojo.widget.createWidget("DateTextBox", { format: "YYYY-MM-DD", value: value }, dateTextBoxDiv);
	dateTextBox.onfocus = function(o) { 
	  dateTextBox.hide(); dojo.debug("hiding " + o); 
	  dateTextBox.picker.show();

	};
	var datePickerDiv = document.createElement("div");
	nodeRef.appendChild(datePickerDiv);
	dateTextBox.picker = dojo.widget.createWidget("DatePicker", { isHidden: true, value : value }, datePickerDiv);
	dateTextBox.picker.hide();
	dojo.event.connect(dateTextBox.picker,
			   "onSetDate", 
			   function(event)
			   {
			     dateTextBox.picker.hide();
			     dateTextBox.show();
			     dateTextBox.setValue(dojo.widget.DatePicker.util.toRfcDate(dateTextBox.picker.date));
			   });
	break;
      case "integer":
      case "positiveInteger":
      case "negativeInteger":
	var w = dojo.widget.createWidget("SpinnerIntegerTextBox", { value: value }, nodeRef);
	break;
      case "double":
	var w = dojo.widget.createWidget("SpinnerRealNumberTextBox", { value: value }, nodeRef);
	break;
      case "string":
      default:
	var w = dojo.widget.createWidget("ValidationTextBox", { required: required, value: value }, nodeRef);
      }
      break;
    case "xforms:select1":
      var binding = bindings[o.getAttribute("xforms:bind")];
      var row = document.createElement("tr");
      ui_element_stack[ui_element_stack.length - 1].appendChild(row);
      var cell = document.createElement("td");
      row.appendChild(cell);
      var label = get_label_node(o);
      if (label)
        cell.appendChild(document.createTextNode(dojo.dom.textContent(label)));
      var nodeRef = document.createElement("div");
      cell = document.createElement("td");
      row.appendChild(cell);
      var nodeRef = document.createElement("div");
      cell.appendChild(nodeRef);
      var values = get_select_values(o);
      for (var i in values)
      {
	dojo.debug("values["+ i + "] = " + values[i].id + ", " + values[i].label + ", " + values[i].value);
      }
      if (binding.type == "boolean")
      {
        var w = dojo.widget.createWidget("CheckBox", { value: "value" }, nodeRef);
      }
      else if (values.length <= 5)
      {
	for (var i in values)
	{
	  var radio = document.createElement("input");
	  radio.setAttribute("name", o.getAttribute("id"));
	  radio.setAttribute("type", "radio");
	  radio.setAttribute("value", values[i].value);
	  nodeRef.appendChild(radio);
	  nodeRef.appendChild(document.createTextNode(values[i].label));
	}
      }
      else
      {
        var combobox = document.createElement("select");
	nodeRef.appendChild(combobox);
	for (var i in values)
	{
	  var option = document.createElement("option");
	  option.appendChild(document.createTextNode(values[i].label));
	  option.setAttribute("value", values[i].value);
	  combobox.appendChild(option);
	}
      }
      break;
    default:
      load_body(o, ui_element_stack);
    }
  });
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

function get_initial_value(binding)
{
  var b = binding;
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
    node = node.getElementsByTagName(element_name)[0];
    dojo.debug("got node " + node.nodeName);
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
