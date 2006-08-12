/**
 * Slider Widget.
 * 
 * The slider widget comes in three forms:
 *  1. Base Slider widget which supports movement in x and y dimensions
 *  2. Vertical Slider (SliderVertical) widget which supports movement
 *     only in the y dimension.
 *  3. Horizontal Slider (SliderHorizontal) widget which supports movement
 *     only in the x dimension.
 *
 * The key objects in the widget are:
 *  - a container div which displays a bar in the background (Slider object)
 *  - a handle inside the container div, which represents the value
 *    (sliderHandle DOM node)
 *  - the object which moves the handle (_handleMove is of type 
 *    SliderDragMoveSource)
 *
 * The values for the slider are calculated by grouping pixels together, 
 * based on the number of values to be represented by the slider.
 * The number of pixels in a group is called the _valueSize
 *  e.g. if slider is 150 pixels long, and is representing the values
 *       0,1,...10 then pixels are grouped into lots of 15 (_valueSize), where:
 *         value 0 maps to pixels  0 -  7
 *               1                 8 - 22
 *               2                23 - 37 etc.
 * The accuracy of the slider is limited to the number of pixels
 * (i.e tiles > pixels will result in the slider not being able to
 *  represent some values).
 *
 * Technical Notes:
 *  - 3 widgets exist because the framework caches the template in
 *    dojo.widget.fillFromTemplateCache (which ignores the changed URI)
 *
 * References (aka sources of inspiration):
 *  - http://dojotoolkit.org/docs/fast_widget_authoring.html
 *  - http://dojotoolkit.org/docs/dojo_event_system.html
 * 
 * $Id: $
 */

// tell the package system what functionality is provided in this module (file)
// (note that the package system works on modules, not the classes)
dojo.provide("dojo.widget.Slider");

// load dependencies
dojo.require("dojo.event.*");
dojo.require("dojo.dnd.*");
// dojo.dnd.* doesn't include this package, because it's not in __package__.js
dojo.require("dojo.dnd.HtmlDragMove");
dojo.require("dojo.widget.*");
dojo.require("dojo.html.layout");


/**
 * Define the two dimensional slider widget class.
 */
dojo.widget.defineWidget (
	"dojo.widget.Slider",
	dojo.widget.HtmlWidget,
	{
		// useful properties (specified as attributes in the html tag)
		// minimum value to be represented by slider in the horizontal direction
		minimumX: 0,
		// minimum value to be represented by slider in the vertical direction
		minimumY: 0,
		// maximum value to be represented by slider in the horizontal direction
		maximumX: 10,
		// maximum value to be represented by slider in the vertical direction
		maximumY: 10,
		// can values be changed on the x (horizontal) axis?
		// number of values to be represented by slider in the horizontal direction
		// =0 means no snapping
		snapValuesX: 0,
		// number of values to be represented by slider in the vertical direction
		// =0 means no snapping
		snapValuesY: 0,
		// should the handle snap to the grid or remain where it was dragged to?
		// FIXME: snapToGrid=false is logically in conflict with setting snapValuesX and snapValuesY
		_snapToGrid: true,
		// can values be changed on the x (horizontal) axis?
		isEnableX: true,
		// can values be changed on the y (vertical) axis?
		isEnableY: true,
		// value size (pixels) in the x dimension
		_valueSizeX: 0.0,
		// value size (pixels) in the y dimension
		_valueSizeY: 0.0,
		// left most edge of constraining container (pixels) in the X dimension
		_minX: 0,
		// top most edge of constraining container (pixels) in the Y dimension
		_minY: 0,
		// constrained slider size (pixels) in the x dimension
		_constraintWidth: 0,
		// constrained slider size (pixels) in the y dimension
		_constraintHeight: 0,
		// progress image right clip value (pixels) in the X dimension
		_clipLeft: 0,
		// progress image left clip value (pixels) in the X dimension
		_clipRight: 0,
		// progress image top clip value (pixels) in the Y dimension
		_clipTop: 0,
		// progress image bottom clip value (pixels) in the Y dimension
		_clipBottom: 0,
		// half the size of the slider handle (pixels) in the X dimension
		_clipXdelta: 0,
		// half the size of the slider handle (pixels) in the Y dimension
		_clipYdelta: 0,
		// initial value in the x dimension
		initialValueX: 0,
		// initial value in the y dimension
		initialValueY: 0,
		// values decrease in the X dimension
		flipX: false,
		// values decrease in the Y dimension
		flipY: false,

		// do we allow the user to click on the slider to set the position?
		// (note: dojo's infrastructor will convert attribute to a boolean)
		clickSelect: true,
		// should the value change while you are dragging, or just after drag finishes?
		activeDrag: false,

		templateCssPath: dojo.uri.dojoUri ("src/widget/templates/Slider.css"),
		templatePath: dojo.uri.dojoUri ("src/widget/templates/Slider.html"),

		// our DOM nodes
		sliderHandleNode: null,
		constrainingContainerNode: null,
		sliderBackgroundNode: null,
		progressBackgroundNode: null,
		topButtonNode: null,
		leftButtonNode: null,
		rightButtonNode: null,
		bottomButtonNode: null,
		focusNode: null,

		// private attributes
		// This is set to true when a drag is started, so that it is not confused
		// with a click
		isDragInProgress: false,

		// default user style attributes
		widgetStyle: "",
		buttonStyleX: "",
		buttonStyleY: "",
		bottomButtonSrc: dojo.uri.dojoUri("src/widget/templates/images/slider_down_arrow.png"),
		topButtonSrc: dojo.uri.dojoUri("src/widget/templates/images/slider_up_arrow.png"),
		leftButtonSrc: dojo.uri.dojoUri("src/widget/templates/images/slider_left_arrow.png"),
		rightButtonSrc: dojo.uri.dojoUri("src/widget/templates/images/slider_right_arrow.png"),
		backgroundSrc: dojo.uri.dojoUri("src/widget/templates/images/blank.gif"),
		progressBackgroundSrc: dojo.uri.dojoUri("src/widget/templates/images/blank.gif"),
		backgroundSize: "width:200px;height:200px;",
		backgroundStyle: "",
		handleStyle: "",
		handleSrc: dojo.uri.dojoUri("src/widget/templates/images/slider-button.png"),
		showButtons: true,
		_eventCount: 0,
		_typamaticTimer: null,
		_typamaticFunction: null,
		defaultTimeout: 500,
		timeoutChangeRate: 0.90,
		_currentTimeout: this.defaultTimeout,

		// does the keyboard related stuff
		_handleKeyEvents: function(evt){
			var k = dojo.event.browser.keys;
			var keyCode = evt.keyCode;

			switch(keyCode){
				case k.KEY_LEFT_ARROW:
					dojo.event.browser.stopEvent(evt);
					this._leftButtonPressed(evt);
					return;
				case k.KEY_RIGHT_ARROW:
					dojo.event.browser.stopEvent(evt);
					this._rightButtonPressed(evt);
					return;
				case k.KEY_DOWN_ARROW:
					dojo.event.browser.stopEvent(evt);
					this._bottomButtonPressed(evt);
					return;
				case k.KEY_UP_ARROW:
					dojo.event.browser.stopEvent(evt);
					this._topButtonPressed(evt);
					return;
			}
			this._eventCount++;

		},

		_onKeyDown: function(evt){
			// IE needs to stop keyDown others need to stop keyPress
			if(!document.createEvent){ // only IE
				this._handleKeyEvents(evt);
			}
		},

		_onKeyPress: function(evt){
			if(document.createEvent){ // never IE
				this._handleKeyEvents(evt);
			}
		},

		_pressButton: function(buttonNode){
			buttonNode.className = buttonNode.className.replace("Outset","Inset");
		},

		_releaseButton: function(buttonNode){
			buttonNode.className = buttonNode.className.replace("Inset","Outset");
		},

		_buttonPressed: function(evt, buttonNode){
			this._setFocus();
			if(typeof evt == "object"){
				if(this._typamaticTimer != null){
					if(this._typamaticNode == buttonNode){
						return;
					}
					clearTimeout(this._typamaticTimer);
				}
				this._buttonReleased(null);
				this._eventCount++;
				this._typamaticTimer = null;
				this._currentTimeout = this.defaultTimeout;
				dojo.event.browser.stopEvent(evt);
			}else if (evt != this._eventCount){
				this._buttonReleased(null);
				return false;
			}
			if (buttonNode == this.leftButtonNode && this.isEnableX) {
				this._snapX(dojo.html.getPixelValue (this.sliderHandleNode,"left") - this._valueSizeX, this._handleMove );
			}
			else if (buttonNode == this.rightButtonNode && this.isEnableX) {
				this._snapX(dojo.html.getPixelValue (this.sliderHandleNode,"left") + this._valueSizeX, this._handleMove );
			}
			else if (buttonNode == this.topButtonNode && this.isEnableY) {
				this._snapY(dojo.html.getPixelValue (this.sliderHandleNode,"top") - this._valueSizeY, this._handleMove );
			}
			else if (buttonNode == this.bottomButtonNode && this.isEnableY) {
				this._snapY(dojo.html.getPixelValue (this.sliderHandleNode,"top") + this._valueSizeY, this._handleMove );
			}
			else {
				return false;
			}
			this._pressButton(buttonNode);
			this.notifyListeners();
			this._typamaticNode = buttonNode;
			this._typamaticTimer = dojo.lang.setTimeout(this, "_buttonPressed", this._currentTimeout, this._eventCount, buttonNode);
			this._currentTimeout = Math.round(this._currentTimeout * this.timeoutChangeRate);
			return false;
		},

		_bottomButtonPressed: function(evt){
			return this._buttonPressed(evt,this.bottomButtonNode);
		},

		// IE sends these events when rapid clicking, mimic an extra single click
		_bottomButtonDoubleClicked: function(evt){
			var rc = this._bottomButtonPressed(evt);
			dojo.lang.setTimeout( this, "_buttonReleased", 50, null);
			return rc;
		},

		_topButtonPressed: function(evt){
			return this._buttonPressed(evt,this.topButtonNode);
		},

		// IE sends these events when rapid clicking, mimic an extra single click
		_topButtonDoubleClicked: function(evt){
			var rc = this._topButtonPressed(evt);
			dojo.lang.setTimeout( this, "_buttonReleased", 50, null);
			return rc;
		},

		_leftButtonPressed: function(evt) {
			return this._buttonPressed(evt,this.leftButtonNode);
		},

		// IE sends these events when rapid clicking, mimic an extra single click
		_leftButtonDoubleClicked: function(evt){
			var rc = this._leftButtonPressed(evt);
			dojo.lang.setTimeout( this, "_buttonReleased", 50, null);
			return rc;
		},

		_rightButtonPressed: function(evt) {
			return this._buttonPressed(evt,this.rightButtonNode);
		},

		// IE sends these events when rapid clicking, mimic an extra single click
		_rightButtonDoubleClicked: function(evt){
			var rc = this._rightButtonPressed(evt);
			dojo.lang.setTimeout( this, "_buttonReleased", 50, null);
			return rc;
		},

		_buttonReleased: function(evt){
			if(typeof evt == "object" && evt != null && typeof evt.keyCode != "undefined" && evt.keyCode != null){
				var keyCode = evt.keyCode;
				var k = dojo.event.browser.keys;

				switch(keyCode){
					case k.KEY_LEFT_ARROW:
					case k.KEY_RIGHT_ARROW:
					case k.KEY_DOWN_ARROW:
					case k.KEY_UP_ARROW:
						dojo.event.browser.stopEvent(evt);
						break;
				}
			}
			this._releaseButton(this.topButtonNode);
			this._releaseButton(this.bottomButtonNode);
			this._releaseButton(this.leftButtonNode);
			this._releaseButton(this.rightButtonNode);
			this._eventCount++;
			if(this._typamaticTimer != null){
				clearTimeout(this._typamaticTimer);
			}
			this._typamaticTimer = null;
			this._currentTimeout = this.defaultTimeout;
		},

		_mouseWheeled: function(evt) {
			var scrollAmount = 0;
			if(typeof evt.wheelDelta == 'number'){ // IE
				scrollAmount = evt.wheelDelta;
			}else if (typeof evt.detail == 'number'){ // Mozilla+Firefox
				scrollAmount = -evt.detail;
			}
			if (this.isEnableY) {
				if(scrollAmount > 0){
					this._topButtonPressed(evt);
					this._buttonReleased(evt);
				}else if (scrollAmount < 0){
					this._bottomButtonPressed(evt);
					this._buttonReleased(evt);
				}
			} else if (this.isEnableX) {
				if(scrollAmount > 0){
					this._rightButtonPressed(evt);
					this._buttonReleased(evt);
				}else if (scrollAmount < 0){
					this._leftButtonPressed(evt);
					this._buttonReleased(evt);
				}
			}
		},

		_discardEvent: function(evt) {
			dojo.event.browser.stopEvent(evt);
		},

		_setFocus: function(){
			if (this.focusNode.focus) {
				this.focusNode.focus();
			}
		},

		// remove comments from the node
		_removeComments: function(parent){
			var children = parent.childNodes;
			for (var i = children.length-1; i >= 0; i--) {
				var aChild = children.item(i);
				if (aChild  != null) {
					switch(aChild.nodeType){
						case 1: // recurse
							this._removeComments(aChild);
							break;
						case 8: // comment
							parent.removeChild(aChild);
					}
				}
			}
		},

		// This function is called when the template is loaded
		fillInTemplate: function (args, frag) 
		{
			this._removeComments(this.domNode);
			var source = this.getFragNodeRef(frag);
			dojo.html.copyStyle(this.domNode, source);
			// the user's style for the widget might include border and padding
			// unfortunately, border isn't supported for inline elements
			// so I get to fake everyone out by setting the border and padding
			// of the outer table cells
			var padding = this.domNode.style.padding;
			if (dojo.lang.isString(padding) && padding != "" && padding != "0px" && padding != "0px 0px 0px 0px") {
				this.topBorderNode.style.padding = 
					this.bottomBorderNode.style.padding = padding;
				this.topBorderNode.style.paddingBottom = "0px";
				this.bottomBorderNode.style.paddingTop = "0px";
				this.rightBorderNode.style.paddingRight = this.domNode.style.paddingRight;
				this.leftBorderNode.style.paddingLeft= this.domNode.style.paddingLeft;
				this.domNode.style.padding = "0px 0px 0px 0px";
			}
			var borderWidth = this.domNode.style.borderWidth;
			if (dojo.lang.isString(borderWidth) && borderWidth != "" && borderWidth != "0px" && borderWidth != "0px 0px 0px 0px") {
				this.topBorderNode.style.borderStyle = 
					this.rightBorderNode.style.borderStyle = 
					this.bottomBorderNode.style.borderStyle = 
					this.leftBorderNode.style.borderStyle = 
						this.domNode.style.borderStyle;
				this.topBorderNode.style.borderColor = 
					this.rightBorderNode.style.borderColor = 
					this.bottomBorderNode.style.borderColor = 
					this.leftBorderNode.style.borderColor = 
						this.domNode.style.borderColor;
				this.topBorderNode.style.borderWidth = 
					this.bottomBorderNode.style.borderWidth = borderWidth;
				this.topBorderNode.style.borderBottomWidth = "0px";
				this.bottomBorderNode.style.borderTopWidth = "0px";
				this.rightBorderNode.style.borderRightWidth = this.domNode.style.borderRightWidth;
				this.leftBorderNode.style.borderLeftWidth = this.domNode.style.borderLeftWidth;
				this.domNode.style.borderWidth = "0px 0px 0px 0px";
			}

			// dojo.debug ("fillInTemplate - className = " + this.domNode.className);

			// setup drag-n-drop for the sliderHandle
			this._handleMove = new dojo.widget._SliderDragMoveSource (this.sliderHandleNode);
			this._handleMove.setParent (this);

			if (this.clickSelect) {
				dojo.event.connect (this.constrainingContainerNode, "onmousedown", this, "onClick");
			} 

			if (this.isEnableX) {
				this.setValueX (!isNaN(this.initialValueX) ? this.initialValueX : (!isNaN(this.minimumX) ? this.minimumX : 0));
			}
			if (!this.isEnableX || !this.showButtons) {
				this.rightButtonNode.style.width = "1px"; // allow the border to show
				this.rightButtonNode.style.visibility = "hidden";
				this.leftButtonNode.style.width = "1px"; // allow the border to show
				this.leftButtonNode.style.visibility = "hidden";
			}
			if (this.isEnableY) {
				this.setValueY (!isNaN(this.initialValueY) ? this.initialValueY : (!isNaN(this.minimumY) ? this.minimumY : 0));
			}
			if (!this.isEnableY || !this.showButtons) {
				this.bottomButtonNode.style.width = "1px"; // allow the border to show
				this.bottomButtonNode.style.visibility = "hidden";
				this.topButtonNode.style.width = "1px"; // allow the border to show
				this.topButtonNode.style.visibility = "hidden";
			}
			if(this.focusNode.addEventListener){
				// dojo.event.connect() doesn't seem to work with DOMMouseScroll
				this.focusNode.addEventListener('DOMMouseScroll', dojo.lang.hitch(this, "_mouseWheeled"), false); // Mozilla + Firefox + Netscape
			}
		},

		// move the X value to the closest allowable value
		_snapX: function (x,_handleMove) {
			if (x < 0) { x = 0; }
			else if (x > this._constraintWidth) { x = this._constraintWidth; }
			else {
				var selectedValue = Math.round (x / this._valueSizeX);
				x = Math.round (selectedValue * this._valueSizeX);
			}
			this.sliderHandleNode.style.left = x + "px";
			if (this.flipX) {
				this._clipLeft = x + this._clipXdelta;
			} else {
				this._clipRight = x + this._clipXdelta;
			}
			this.progressBackgroundNode.style.clip = "rect("+this._clipTop+"px,"+this._clipRight+"px,"+this._clipBottom+"px,"+this._clipLeft+"px)";
		},

		// compute _valueSizeX & _constraintWidth & default snapValuesX
		_calc_valueSizeX: function () {
			var constrainingCtrBox = dojo.html.getContentBox(this.constrainingContainerNode);
			var sliderHandleBox = dojo.html.getContentBox(this.sliderHandleNode);
			if (constrainingCtrBox.width <= 0 || sliderHandleBox.width <= 0) { 
				return false; 
			}

			this._constraintWidth = constrainingCtrBox.width 
				+ dojo.html.getPadding(this.constrainingContainerNode).width
				- sliderHandleBox.width;

			if (this.flipX) {
				this._clipLeft = this._clipRight = constrainingCtrBox.width;
			} else {
				this._clipLeft = this._clipRight = 0;
			}
			this._clipXdelta = sliderHandleBox.width >> 1;
			if (!this.isEnableY) {
				this._clipTop = 0;
				this._clipBottom = constrainingCtrBox.height;
			}

			if (this._constraintWidth <= 0) { return false; }
			if (this.snapValuesX == 0) {
				this.snapValuesX = this._constraintWidth + 1;
			}

			this._valueSizeX = this._constraintWidth / (this.snapValuesX - 1);
			return true;
		},

		// Move the handle (in the X dimension) to the specified value
		setValueX: function (value) {
			if (0.0 == this._valueSizeX) {
				if (this._calc_valueSizeX () == false) {
					dojo.lang.setTimeout(this, "setValueX", 100, value);
					return;
				}
			}
			if (isNaN(value)) {
				value = 0;
			}
			if (value > this.maximumX) {
				value = this.maximumX;
			}
			else if (value < this.minimumX) {
				value = this.minimumX;
			}
			var pixelPercent = (value-this.minimumX) / (this.maximumX-this.minimumX);
			if (this.flipX) {
				pixelPercent = 1.0 - pixelPercent;
			}
			this._snapX (pixelPercent * this._constraintWidth, this._handleMove);
			this.notifyListeners();
		},


		// Get the number of the value that matches the position of the handle
		getValueX: function () {
			var pixelPercent = dojo.html.getPixelValue (this.sliderHandleNode,"left") / this._constraintWidth;
			if (this.flipX) {
				pixelPercent = 1.0 - pixelPercent;
			}
			return Math.round (pixelPercent * (this.snapValuesX-1)) * ((this.maximumX-this.minimumX) / (this.snapValuesX-1)) + this.minimumX;
		},

		// move the Y value to the closest allowable value
		_snapY: function (y,_handleMove) {
			if (y < 0) { y = 0; }
			else if (y > this._constraintHeight) { y = this._constraintHeight; }
			else {
				var selectedValue = Math.round (y / this._valueSizeY);
				y = Math.round (selectedValue * this._valueSizeY);
			}
			this.sliderHandleNode.style.top = y + "px";
			if (this.flipY) {
				this._clipTop = y + this._clipYdelta;
			} else {
				this._clipBottom = y + this._clipYdelta;
			}
			this.progressBackgroundNode.style.clip = "rect("+this._clipTop+"px,"+this._clipRight+"px,"+this._clipBottom+"px,"+this._clipLeft+"px)";
		},
		// compute _valueSizeY & _constraintHeight & default snapValuesY
		_calc_valueSizeY: function () {
			var constrainingCtrBox = dojo.html.getContentBox(this.constrainingContainerNode);
			var sliderHandleBox = dojo.html.getContentBox(this.sliderHandleNode);
			if (constrainingCtrBox.height <= 0 || sliderHandleBox.height <= 0) { 
				return false; 
			}

			this._constraintHeight = constrainingCtrBox.height
				+ dojo.html.getPadding(this.constrainingContainerNode).height
				- sliderHandleBox.height;

			if (this.flipY) {
				this._clipTop = this._clipBottom = constrainingCtrBox.height;
			} else {
				this._clipTop = this._clipBottom = 0;
			}
			this._clipYdelta = sliderHandleBox.height >> 1;
			if (!this.isEnableX) {
				this._clipLeft = 0;
				this._clipRight = constrainingCtrBox.width;
			}

			if (this._constraintHeight <= 0) { return false; }
			if (this.snapValuesY == 0) {
				this.snapValuesY = this._constraintHeight + 1;
			}

			this._valueSizeY = this._constraintHeight / (this.snapValuesY - 1);
			return true;
		},

		// set the slider to a particular value
		setValueY: function (value) {
			if (0.0 == this._valueSizeY) {
				if (this._calc_valueSizeY () == false) {
					dojo.lang.setTimeout(this, "setValueY", 100, value);
					return;
				}
			}
			if (isNaN(value)) {
				value = 0;
			}
			if (value > this.maximumY) {
				value = this.maximumY;
			}
			else if (value < this.minimumY) {
				value = this.minimumY;
			}
			var pixelPercent = (value-this.minimumY) / (this.maximumY-this.minimumY);
			if (this.flipY) {
				pixelPercent = 1.0 - pixelPercent;
			}
			this._snapY (pixelPercent * this._constraintHeight, this._handleMove);
			this.notifyListeners();
		},

		// Get the number of the value that the matches the position of the handle
		getValueY: function () {
			var pixelPercent = dojo.html.getPixelValue (this.sliderHandleNode,"top") / this._constraintHeight;
			if (this.flipY) {
				pixelPercent = 1.0 - pixelPercent;
			}
			return Math.round (pixelPercent * (this.snapValuesY-1)) * ((this.maximumY-this.minimumY) / (this.snapValuesY-1)) + this.minimumY;
		},

		// set the position of the handle
		onClick: function (evt) {
			if (this.isDragInProgress) {
				return;
			}

			var offset = dojo.html.getScroll().offset;
			var parent = dojo.html.getAbsolutePosition(this.constrainingContainerNode, true);
			var content = dojo.html.getContentBox(this._handleMove.domNode);			
			if (this.isEnableX) {
				var x = offset.x + evt.clientX - parent.x - (content.width >> 1);
				this._snapX(x, this._handleMove);
			}
			if (this.isEnableY) {
				var y = offset.y + evt.clientY - parent.y - (content.height >> 1);
				this._snapY(y, this._handleMove);
			}
			this.notifyListeners();
		},

		notifyListeners: function() {
			this.onValueChanged(this.getValueX(), this.getValueY());
		},

		onValueChanged: function(x, y){
		}
	}
);


/* ------------------------------------------------------------------------- */


/**
 * Define the horizontal slider widget class.
 */
dojo.widget.defineWidget (
	"dojo.widget.SliderHorizontal",
	dojo.widget.Slider,
	{
		widgetType: "SliderHorizontal",

		isEnableX: true,
		isEnableY: false,
		initialValue: "",
		snapValues: "",
		minimum: "",
		maximum: "",
		buttonStyle: "",
		backgroundSize: "height:10px;width:200px;",
		backgroundSrc: dojo.uri.dojoUri("src/widget/templates/images/slider-bg.gif"),
		flip: false,

		postMixInProperties: function(){
			if (!isNaN(parseFloat(this.initialValue))) {
				this.initialValueX = parseFloat(this.initialValue);
			}
			if (!isNaN(parseFloat(this.minimum))) {
				this.minimumX = parseFloat(this.minimum);
			}
			if (!isNaN(parseFloat(this.maximum))) {
				this.maximumX = parseFloat(this.maximum);
			}
			if (!isNaN(parseInt(this.snapValues))) {
				this.snapValuesX = parseInt(this.snapValues);
			}
			if (dojo.lang.isString(this.buttonStyle) && this.buttonStyle != "") {
				this.buttonStyleX = this.buttonStyle;
			}
			if (dojo.lang.isBoolean(this.flip)) {
				this.flipX = this.flip;
			}
		},

		notifyListeners: function() {
			this.onValueChanged(this.getValueX());
		},

		// wrapper for getValueX
		getValue: function () {
			return this.getValueX ();
		},

		// wrapper for setValueX
		setValue: function (value) {
			this.setValueX (value);
		},

		onValueChanged: function(value){
		}
	}
);


/* ------------------------------------------------------------------------- */


/**
 * Define the vertical slider widget class.
 */
dojo.widget.defineWidget (
	"dojo.widget.SliderVertical",
	dojo.widget.Slider,
	{
		widgetType: "SliderVertical",

		isEnableX: false,
		isEnableY: true,
		initialValue: "",
		snapValues: "",
		minimum: "",
		maximum: "",
		buttonStyle: "",
		backgroundSize: "width:10px;height:200px;",
		backgroundSrc: dojo.uri.dojoUri("src/widget/templates/images/slider-bg-vert.gif"),
		flip: false,

		postMixInProperties: function(){
			if (!isNaN(parseFloat(this.initialValue))) {
				this.initialValueY = parseFloat(this.initialValue);
			}
			if (!isNaN(parseFloat(this.minimum))) {
				this.minimumY = parseFloat(this.minimum);
			}
			if (!isNaN(parseFloat(this.maximum))) {
				this.maximumY = parseFloat(this.maximum);
			}
			if (!isNaN(parseInt(this.snapValues))) {
				this.snapValuesY = parseInt(this.snapValues);
			}
			if (dojo.lang.isString(this.buttonStyle) && this.buttonStyle != "") {
				this.buttonStyleY = this.buttonStyle;
			}
			if (dojo.lang.isBoolean(this.flip)) {
				this.flipY = this.flip;
			}
		},

		notifyListeners: function() {
			this.onValueChanged(this.getValueY());
		},

		// wrapper for getValueY
		getValue: function () {
			return this.getValueY ();
		},

		// wrapper for setValueY
		setValue: function (value) {
			this.setValueY (value);
		},

		onValueChanged: function(value){
		}
	}
);


/* ------------------------------------------------------------------------- */


/**
 * This class extends the HtmlDragMoveSource class to provide
 * features for the slider handle.
 */
dojo.declare (
	"dojo.widget._SliderDragMoveSource",
	dojo.dnd.HtmlDragMoveSource,
{
	slider: null,

	/** Setup the handle for drag
	 *  Extends dojo.dnd.HtmlDragMoveSource by creating a SliderDragMoveSource */
	onDragStart: function (evt) {
		this.slider.isDragInProgress = true;
		if (this.slider.isEnableX) {
			this.slider._minX = dojo.html.getAbsolutePosition(this.slider.constrainingContainerNode).x;
		}
		if (this.slider.isEnableY) {
			this.slider._minY = dojo.html.getAbsolutePosition(this.slider.constrainingContainerNode).y;
		}

		var dragObj = this.createDragMoveObject ();

		this.slider.notifyListeners();
		return dragObj;
	},

	onDragEnd: function (evt) {
		this.slider.isDragInProgress = false;
		this.slider.notifyListeners();
	},

	createDragMoveObject: function () {
		//dojo.debug ("SliderDragMoveSource#createDragMoveObject - " + this.slider);
		var dragObj = new dojo.widget._SliderDragMoveObject (this.dragObject, this.type);
		dragObj.slider = this.slider;

		// this code copied from dojo.dnd.HtmlDragSource#onDragStart
		if (this.dragClass) { 
			dragObj.dragClass = this.dragClass; 
		}

		return dragObj;
	},


	setParent: function (slider) {
		this.slider = slider;
	}
});


/* ------------------------------------------------------------------------- */


/**
 * This class extends the HtmlDragMoveObject class to provide
 * features for the slider handle.
 */
dojo.declare (
	"dojo.widget._SliderDragMoveObject",
	dojo.dnd.HtmlDragMoveObject,
{
	// reference to dojo.widget.Slider
	slider: null,

	/** Moves the node to follow the mouse.
	 *  Extends functon HtmlDragObject by adding functionality to snap handle
	 *  to a discrete value */
	onDragMove: function (evt) {
		this.updateDragOffset ();

		var offset = dojo.html.getScroll().offset;
		if (this.slider.isEnableX) {
			var x = this.dragOffset.x + evt.pageX - this.slider._minX - offset.x;
			this.slider._snapX(x, this.slider._handleMove);
		}

		if (this.slider.isEnableY) {
			var y = this.dragOffset.y + evt.pageY - this.slider._minY - offset.y;
			this.slider._snapY(y, this.slider._handleMove);
		}
		if(this.slider.activeDrag){
			this.slider.notifyListeners();
		}
	}
});
