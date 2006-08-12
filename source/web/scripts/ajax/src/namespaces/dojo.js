dojo.provide("dojo.namespaces.dojo");
dojo.require("dojo.namespace");

(function(){
	//mapping of all widget short names to their full package names
	// This is used for widget autoloading - no dojo.require() is necessary.
	// If you use a widget in markup or create one dynamically, then this
	// mapping is used to find and load any dependencies not already loaded.
	// You should use your own namespace for any custom widgets.
	// For extra widgets you use, dojo.declare() may be used to explicitly load them.
	var map = {
		html: {
			"accordioncontainer": "dojo.widget.AccordionContainer",
			"treerpccontroller": "dojo.widget.TreeRPCController",
			"accordionpane": "dojo.widget.AccordionPane",
			"button": "dojo.widget.Button",
			"chart": "dojo.widget.Chart",
			"checkbox": "dojo.widget.Checkbox",
			"civicrmdatepicker": "dojo.widget.CiviCrmDatePicker",
			"colorpalette": "dojo.widget.ColorPalette",
			"combobox": "dojo.widget.ComboBox",
			"combobutton": "dojo.widget.Button",
			"contentpane": "dojo.widget.ContentPane",
			"contextmenu": "dojo.widget.ContextMenu",
			"datepicker": "dojo.widget.DatePicker",
			"debugconsole": "dojo.widget.DebugConsole",
			"dialog": "dojo.widget.Dialog",
			"docpane": "dojo.widget.DocPane",
			"dropdownbutton": "dojo.widget.Button",
			"dropdowndatepicker": "dojo.widget.DropdownDatePicker",
			"editor2": "dojo.widget.Editor2",
			"editor2toolbar": "dojo.widget.Editor2Toolbar",
			"editor": "dojo.widget.Editor",
			"editortree": "dojo.widget.EditorTree",
			"editortreecontextmenu": "dojo.widget.EditorTreeContextMenu",
			"editortreenode": "dojo.widget.EditorTreeNode",
			"fisheyelist": "dojo.widget.FisheyeList",
			"editortreecontroller": "dojo.widget.EditorTreeController",
			"googlemap": "dojo.widget.GoogleMap",
			"editortreeselector": "dojo.widget.EditorTreeSelector",
			"floatingpane": "dojo.widget.FloatingPane",
			"hslcolorpicker": "dojo.widget.HslColorPicker",
			"inlineeditbox": "dojo.widget.InlineEditBox",
			"layoutcontainer": "dojo.widget.LayoutContainer",
			"linkpane": "dojo.widget.LinkPane",
			"manager": "dojo.widget.Manager",
			"popupcontainer": "dojo.widget.Menu2",
			"popupmenu2": "dojo.widget.Menu2",
			"menuitem2": "dojo.widget.Menu2",
			"menuseparator2": "dojo.widget.Menu2",
			"menubar2": "dojo.widget.Menu2",
			"menubaritem2": "dojo.widget.Menu2",
			"monthlyCalendar": "dojo.widget.MonthlyCalendar",
			"richtext": "dojo.widget.RichText",
			"remotetabcontroller": "dojo.widget.RemoteTabController",
			"resizehandle": "dojo.widget.ResizeHandle",
			"resizabletextarea": "dojo.widget.ResizableTextarea",
			"select": "dojo.widget.Select",
			"slideshow": "dojo.widget.SlideShow",
			"sortabletable": "dojo.widget.SortableTable",
			"splitcontainer": "dojo.widget.SplitContainer",
			"svgbutton": "dojo.widget.SvgButton",
			"tabcontainer": "dojo.widget.TabContainer",
			"taskbar": "dojo.widget.TaskBar",
			"timepicker": "dojo.widget.TimePicker",
			"titlepane": "dojo.widget.TitlePane",
			"toaster": "dojo.widget.Toaster",
			"toggler": "dojo.widget.Toggler",
			"toolbar": "dojo.widget.Toolbar",
			"tooltip": "dojo.widget.Tooltip",
			"tree": "dojo.widget.Tree",
			"treebasiccontroller": "dojo.widget.TreeBasicController",
			"treecontextmenu": "dojo.widget.TreeContextMenu",
			"treeselector": "dojo.widget.TreeSelector",
			"treecontrollerextension": "dojo.widget.TreeControllerExtension",
			"treenode": "dojo.widget.TreeNode",
			"validate": "dojo.widget.validate",
			"treeloadingcontroller": "dojo.widget.TreeLoadingController",
			"widget": "dojo.widget.Widget",
			"wizard": "dojo.widget.Wizard",
			"yahoomap": "dojo.widget.YahooMap"
		},
		svg: {
			"chart": "dojo.widget.svg.Chart",
			"hslcolorpicker": "dojo.widget.svg.HslColorPicker"
		},
		vml: {
			"chart": "dojo.widget.vml.Chart"
		}
	};

	function dojoNamespaceResolver(name, domain){
		if(!domain){ domain="html"; }
		if(!map[domain]){ return null; }
		return map[domain][name];    
	}

	dojo.defineNamespace("dojo", "src", "dojo", dojoNamespaceResolver);

	dojo.addDojoNamespaceMapping = function(shortName, fullName){
		map[shortName]=fullName;    
	};
})();
