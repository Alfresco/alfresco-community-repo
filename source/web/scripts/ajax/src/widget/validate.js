dojo.provide("dojo.widget.validate");

dojo.require("dojo.widget.*");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.widget.Manager");
dojo.require("dojo.widget.Parse");
dojo.require("dojo.xml.Parse");
dojo.require("dojo.lang.array");
dojo.require("dojo.lang.common");

dojo.require("dojo.validate.common");
dojo.require("dojo.validate.datetime");
dojo.require("dojo.validate.check");
dojo.require("dojo.validate.web");
dojo.require("dojo.validate.us");

dojo.require("dojo.i18n.common");
dojo.requireLocalization("dojo.widget", "validate");

dojo.widget.manager.registerWidgetPackage("dojo.widget.validate");

/*
  ****** Textbox ******

  This widget is a generic textbox field.
  Serves as a base class to derive more specialized functionality in subclasses.
  Has the following properties that can be specified as attributes in the markup.

  @attr id         The textbox id attribute.
  @attr className  The textbox class attribute.
  @attr name       The textbox name attribute.
  @attr value      The textbox value attribute.
  @attr trim       Removes leading and trailing whitespace if true.  Default is false.
  @attr uppercase  Converts all characters to uppercase if true.  Default is false.
  @attr lowercase  Converts all characters to lowercase if true.  Default is false.
  @attr ucFirst    Converts the first character of each word to uppercase if true.
  @attr lowercase  Removes all characters that are not digits if true.  Default is false.
*/
dojo.widget.defineWidget(
	"dojo.widget.validate.Textbox",
	dojo.widget.HtmlWidget,
	{
		// default values for new subclass properties
		className: "",
		name: "",
		value: "",
		type: "",
		trim: false,
		uppercase: false,
		lowercase: false,
		ucFirst: false,
		digit: false,
		htmlfloat: "none",

		templatePath: dojo.uri.dojoUri("src/widget/templates/Textbox.html"),
	
		// our DOM nodes
		textbox: null,
	
		// Apply various filters to textbox value
		filter: function() { 
			if (this.trim) {
				this.textbox.value = this.textbox.value.replace(/(^\s*|\s*$)/g, "");
			} 
			if (this.uppercase) {
				this.textbox.value = this.textbox.value.toUpperCase();
			} 
			if (this.lowercase) {
				this.textbox.value = this.textbox.value.toLowerCase();
			} 
			if (this.ucFirst) {
				this.textbox.value = this.textbox.value.replace(/\b\w+\b/g, 
					function(word) { return word.substring(0,1).toUpperCase() + word.substring(1).toLowerCase(); });
			} 
			if (this.digit) {
				this.textbox.value = this.textbox.value.replace(/\D/g, "");
			} 
		},
	
		// event handlers, you can over-ride these in your own subclasses
		onfocus: function() {},
		onblur: function() { this.filter(); },
	
		// All functions below are called by create from dojo.widget.Widget
		mixInProperties: function(localProperties, frag) {
			dojo.widget.validate.Textbox.superclass.mixInProperties.apply(this, arguments);
			if ( localProperties["class"] ) { 
				this.className = localProperties["class"];
			}
		},
	
		fillInTemplate: function() {
			// apply any filters to initial value
			this.filter();

			// set table to be inlined (technique varies by browser)
			if(dojo.render.html.ie){ dojo.html.addClass(this.domNode, "ie"); }
			if(dojo.render.html.moz){ dojo.html.addClass(this.domNode, "moz"); }
			if(dojo.render.html.opera){ dojo.html.addClass(this.domNode, "opera"); }
			if(dojo.render.html.safari){ dojo.html.addClass(this.domNode, "safari"); }
		}
	
	}
);

/*
  ****** ValidationTextbox ******

  A subclass of Textbox.
  Over-ride isValid in subclasses to perform specific kinds of validation.
  Has several new properties that can be specified as attributes in the markup.

  @attr type          		Basic input tag type declaration.
  @attr size          		Basic input tag size declaration.
  @attr type          		Basic input tag maxlength declaration.	
  @attr required          	Can be true or false, default is false.
  @attr validColor        	The color textbox is highlighted for valid input. Default is #cfc.
  @attr invalidColor      	The color textbox is highlighted for invalid input. Default is #fcc.
  @attr invalidClass		Class used to format displayed text in page if necessary to override default class
  @attr invalidMessage    	The message to display if value is invalid.
  @attr missingMessage    	The message to display if value is missing.
  @attr missingClass		Override default class used for missing input data
  @attr listenOnKeyPress	Updates messages on each key press.  Default is true.
  @attr promptMessage		Will not issue invalid message if field is populated with default user-prompt text
*/
dojo.widget.defineWidget(
	"dojo.widget.validate.ValidationTextbox",
	dojo.widget.validate.Textbox,
	function() {
		// this property isn't a primitive and needs to be created on a per-item basis.
		this.flags = {};
	},
	{
		// default values for new subclass properties
		required: false,
		validColor: "#cfc",
		invalidColor: "#fcc",
		rangeClass: "range",
		invalidClass: "invalid",
		missingClass: "missing",
		size: "",
		maxlength: "",
		promptMessage: "",
		invalidMessage: "",
		missingMessage: "",
		rangeMessage: "",
		listenOnKeyPress: true,
		htmlfloat: "none",
		lastCheckedValue: null,
	
		templatePath: dojo.uri.dojoUri("src/widget/templates/ValidationTextbox.html"),
	
		// new DOM nodes
		invalidSpan: null,
		missingSpan: null,
		rangeSpan: null,
	
		getValue: function() {
			return this.textbox.value;
		},
	
		setValue: function(value) {
			this.textbox.value = value;
			this.update();
		},
	
		// Need to over-ride with your own validation code in subclasses
		isValid: function() { return true; },
	
		// Need to over-ride with your own validation code in subclasses
		isInRange: function() { return true; },
	
		// Returns true if value is all whitespace
		isEmpty: function() { 
			return ( /^\s*$/.test(this.textbox.value) );
		},
	
		// Returns true if value is required and it is all whitespace.
		isMissing: function() { 
			return ( this.required && this.isEmpty() );
		},
	
		// Called oninit, onblur, and onkeypress.
		// Show missing or invalid messages if appropriate, and highlight textbox field.
		update: function() {
			this.lastCheckedValue = this.textbox.value;
			this.missingSpan.style.display = "none";
			this.invalidSpan.style.display = "none";
			this.rangeSpan.style.display = "none";
	
			var empty = this.isEmpty();
			var valid = true;
			if(this.promptMessage != this.textbox.value){ 
				valid = this.isValid(); 
			}
			var missing = this.isMissing();
	
			// Display at most one error message
			if(missing){
				this.missingSpan.style.display = "";
			}else if( !empty && !valid ){
				this.invalidSpan.style.display = "";
			}else if( !empty && !this.isInRange() ){
				this.rangeSpan.style.display = "";
			}
			this.highlight();
		},
	
		// Called oninit, and onblur.
		highlight: function() {
			// highlight textbox background 
			if ( this.isEmpty() ) {
				this.textbox.style.backgroundColor = "";
			}else if ( this.isValid() && this.isInRange() ){
				this.textbox.style.backgroundColor = this.validColor;
			}else if( this.textbox.value != this.promptMessage){ 
				this.textbox.style.backgroundColor = this.invalidColor;
			}
		},
	
		onfocus: function() {
			if ( !this.listenOnKeyPress) {
			    this.textbox.style.backgroundColor = "";
			}
		},
	
		onblur: function() { 
			this.filter();
			this.update(); 
		},
	
		onkeyup: function(){ 
			if(this.listenOnKeyPress){ 
				//this.filter();  trim is problem if you have to type two words
				this.update(); 
			}else if (this.textbox.value != this.lastCheckedValue){
			    this.textbox.style.backgroundColor = "";
			}
		},

		postMixInProperties: function(localProperties, frag) {
			dojo.widget.validate.ValidationTextbox.superclass.postMixInProperties.apply(this, arguments);
			this.messages = dojo.i18n.getLocalization("dojo.widget", "validate");
			dojo.lang.forEach(["invalidMessage", "missingMessage", "rangeMessage"], function(prop) {
				if(this[prop]){ this.messages[prop] = this[prop]; }
			}, this);
		},
	
		// FIXME: why are there to fillInTemplate methods defined here?
		fillInTemplate: function() {
			dojo.widget.validate.ValidationTextbox.superclass.fillInTemplate.apply(this, arguments);

			// Attach isMissing and isValid methods to the textbox.
			// We may use them later in connection with a submit button widget.
			// TODO: this is unorthodox; it seems better to do it another way -- Bill
			this.textbox.isValid = function() { this.isValid.call(this); };
			this.textbox.isMissing = function() { this.isMissing.call(this); };
			this.textbox.isInRange = function() { this.isInRange.call(this); };
			this.update(); 
		}
	}
);


/*
  ****** IntegerTextbox ******

  A subclass of ValidationTextbox.
  Over-rides isValid/isInRange to test for integer input.
  Has 4 new properties that can be specified as attributes in the markup.

  @attr signed     The leading plus-or-minus sign. Can be true or false, default is either.
  @attr separator  The character used as the thousands separator.  Default is no separator.
  @attr min  Minimum signed value.  Default is -Infinity
  @attr max  Maximum signed value.  Default is +Infinity
*/
dojo.widget.defineWidget(
	"dojo.widget.validate.IntegerTextbox",
	dojo.widget.validate.ValidationTextbox,
	{
		mixInProperties: function(localProperties, frag) {
			// First initialize properties in super-class.
			dojo.widget.validate.IntegerTextbox.superclass.mixInProperties.apply(this, arguments);
	
			// Get properties from markup attributes, and assign to flags object.
			if((localProperties.signed == "true")||
				(localProperties.signed == "always")){
				this.flags.signed = true;
			}else if((localProperties.signed == "false")||
					(localProperties.signed == "never")){
				this.flags.signed = false;
				this.flags.min = 0;
			}else{
				this.flags.signed = [ true, false ]; // optional
			}
			if(localProperties.separator){ 
				this.flags.separator = localProperties.separator;
			}
			if(localProperties.min){ 
				this.flags.min = parseInt(localProperties.min);
			}
			if(localProperties.max){ 
				this.flags.max = parseInt(localProperties.max);
			}
		},

		// Over-ride for integer validation
		isValid: function() { 
			return dojo.validate.isInteger(this.textbox.value, this.flags);
		},
		isInRange: function() { 
			return dojo.validate.isInRange(this.textbox.value, this.flags);
		}
	}
);

/*
  ****** RealNumberTextbox ******

  A subclass that extends IntegerTextbox.
  Over-rides isValid/isInRange to test for real number input.
  Has 5 new properties that can be specified as attributes in the markup.

  @attr places    The exact number of decimal places.  If omitted, it's unlimited and optional.
  @attr exponent  Can be true or false.  If omitted the exponential part is optional.
  @attr eSigned   Is the exponent signed?  Can be true or false, if omitted the sign is optional.
  @attr min  Minimum signed value.  Default is -Infinity
  @attr max  Maximum signed value.  Default is +Infinity
*/

dojo.widget.defineWidget(
	"dojo.widget.validate.RealNumberTextbox",
	dojo.widget.validate.IntegerTextbox,
	{
		mixInProperties: function(localProperties, frag) {
			// First initialize properties in super-class.
			dojo.widget.validate.RealNumberTextbox.superclass.mixInProperties.apply(this, arguments);
	
			// Get properties from markup attributes, and assign to flags object.
			if ( localProperties.places ) { 
				this.flags.places = Number( localProperties.places );
			}
			if((localProperties.exponent == "true")||
				(localProperties.exponent == "always")){
				this.flags.exponent = true;
			}else if((localProperties.exponent == "false")||(localProperties.exponent == "never")){
				this.flags.exponent = false;
			}else{
				this.flags.exponent = [ true, false ]; // optional
			}
			if((localProperties.esigned == "true")||(localProperties.esigned == "always")){
				this.flags.eSigned = true;
			}else if((localProperties.esigned == "false")||(localProperties.esigned == "never")){
				this.flags.eSigned = false;
			}else{
				this.flags.eSigned = [ true, false ]; // optional
			}
			if(localProperties.min){ 
				this.flags.min = parseFloat(localProperties.min);
			}
			if(localProperties.max){ 
				this.flags.max = parseFloat(localProperties.max);
			}
		},

		// Over-ride for real number validation
		isValid: function() { 
			return dojo.validate.isRealNumber(this.textbox.value, this.flags);
		},
		isInRange: function() { 
			return dojo.validate.isInRange(this.textbox.value, this.flags);
		}

	}
);

/*
  ****** CurrencyTextbox ******

  A subclass that extends IntegerTextbox.
  Over-rides isValid/isInRange to test if input denotes a monetary value .
  Has 5 new properties that can be specified as attributes in the markup.

  @attr fractional      The decimal places (e.g. for cents).  Can be true or false, optional if omitted.
  @attr symbol     A currency symbol such as Yen "???", Pound "???", or the Euro "???". Default is "$".
  @attr separator  Default is "," instead of no separator as in IntegerTextbox.
  @attr min  Minimum signed value.  Default is -Infinity
  @attr max  Maximum signed value.  Default is +Infinity
*/
dojo.widget.defineWidget(
	"dojo.widget.validate.CurrencyTextbox",
	dojo.widget.validate.IntegerTextbox,
	{
		mixInProperties: function(localProperties, frag) {
			// First initialize properties in super-class.
			dojo.widget.validate.CurrencyTextbox.superclass.mixInProperties.apply(this, arguments);
	
			// Get properties from markup attributes, and assign to flags object.
			if ( localProperties.fractional ) { 
				this.flags.fractional = ( localProperties.fractional == "true" );
			} else if ( localProperties.cents ) {
				dojo.deprecated("dojo.widget.validate.IntegerTextbox", "use fractional attr instead of cents", "0.5");
				this.flags.fractional = ( localProperties.cents == "true" );
			}
			if ( localProperties.symbol ) { 
				this.flags.symbol = localProperties.symbol;
			}
			if(localProperties.min){ 
				this.flags.min = parseFloat(localProperties.min);
			}
			if(localProperties.max){ 
				this.flags.max = parseFloat(localProperties.max);
			}
		},

		// Over-ride for currency validation
		isValid: function() { 
			return dojo.validate.isCurrency(this.textbox.value, this.flags);
		},
		isInRange: function() { 
			return dojo.validate.isInRange(this.textbox.value, this.flags);
		}

	}
);

/*
  ****** IpAddressTextbox ******

  A subclass of ValidationTextbox.
  Over-rides isValid to test for IP addresses.
  Can specify formats for ipv4 or ipv6 as attributes in the markup.

  @attr allowDottedDecimal  true or false, default is true.
  @attr allowDottedHex      true or false, default is true.
  @attr allowDottedOctal    true or false, default is true.
  @attr allowDecimal        true or false, default is true.
  @attr allowHex            true or false, default is true.
  @attr allowIPv6           true or false, default is true.
  @attr allowHybrid         true or false, default is true.
*/
dojo.widget.defineWidget(
	"dojo.widget.validate.IpAddressTextbox",
	dojo.widget.validate.ValidationTextbox,
	{
		mixInProperties: function(localProperties, frag) {
			// First initialize properties in super-class.
			dojo.widget.validate.IpAddressTextbox.superclass.mixInProperties.apply(this, arguments);
	
			// Get properties from markup attributes, and assign to flags object.
			if ( localProperties.allowdotteddecimal ) { 
				this.flags.allowDottedDecimal = ( localProperties.allowdotteddecimal == "true" );
			}
			if ( localProperties.allowdottedhex ) { 
				this.flags.allowDottedHex = ( localProperties.allowdottedhex == "true" );
			}
			if ( localProperties.allowdottedoctal ) { 
				this.flags.allowDottedOctal = ( localProperties.allowdottedoctal == "true" );
			}
			if ( localProperties.allowdecimal ) { 
				this.flags.allowDecimal = ( localProperties.allowdecimal == "true" );
			}
			if ( localProperties.allowhex ) { 
				this.flags.allowHex = ( localProperties.allowhex == "true" );
			}
			if ( localProperties.allowipv6 ) { 
				this.flags.allowIPv6 = ( localProperties.allowipv6 == "true" );
			}
			if ( localProperties.allowhybrid ) { 
				this.flags.allowHybrid = ( localProperties.allowhybrid == "true" );
			}
		},

		// Over-ride for IP address validation
		isValid: function() { 
			return dojo.validate.isIpAddress(this.textbox.value, this.flags);
		}
	}
);

/*
  ****** UrlTextbox ******

  A subclass of IpAddressTextbox.
  Over-rides isValid to test for URL's.
  Can specify 5 additional attributes in the markup.

  @attr scheme        Can be true or false.  If omitted the scheme is optional.
  @attr allowIP       Allow an IP address for hostname.  Default is true.
  @attr allowLocal    Allow the host to be "localhost".  Default is false.
  @attr allowCC       Allow 2 letter country code domains.  Default is true.
  @attr allowGeneric  Allow generic domains.  Can be true or false, default is true.
*/
dojo.widget.defineWidget(
	"dojo.widget.validate.UrlTextbox",
	dojo.widget.validate.IpAddressTextbox,
	{
		mixInProperties: function(localProperties, frag) {
			// First initialize properties in super-class.
			dojo.widget.validate.UrlTextbox.superclass.mixInProperties.apply(this, arguments);

			// Get properties from markup attributes, and assign to flags object.
			if ( localProperties.scheme ) { 
				this.flags.scheme = ( localProperties.scheme == "true" );
			}
			if ( localProperties.allowip ) { 
				this.flags.allowIP = ( localProperties.allowip == "true" );
			}
			if ( localProperties.allowlocal ) { 
				this.flags.allowLocal = ( localProperties.allowlocal == "true" );
			}
			if ( localProperties.allowcc ) { 
				this.flags.allowCC = ( localProperties.allowcc == "true" );
			}
			if ( localProperties.allowgeneric ) { 
				this.flags.allowGeneric = ( localProperties.allowgeneric == "true" );
			}
		},

		// Over-ride for URL validation
		isValid: function() { 
			return dojo.validate.isUrl(this.textbox.value, this.flags);
		}
	}
);

/*
  ****** EmailTextbox ******

  A subclass of UrlTextbox.
  Over-rides isValid to test for email addresses.
  Can use all markup attributes/properties of UrlTextbox except scheme.
  One new attribute available in the markup.

  @attr allowCruft  Allow address like <mailto:foo@yahoo.com>.  Default is false.
*/
dojo.widget.defineWidget(
	"dojo.widget.validate.EmailTextbox",
	dojo.widget.validate.UrlTextbox,
	{
		mixInProperties: function(localProperties, frag) {
			// First initialize properties in super-class.
			dojo.widget.validate.EmailTextbox.superclass.mixInProperties.apply(this, arguments);
	
			// Get properties from markup attributes, and assign to flags object.
			if ( localProperties.allowcruft ) { 
				this.flags.allowCruft = ( localProperties.allowcruft == "true" );
			}
		},

		// Over-ride for email address validation
		isValid: function() { 
			return dojo.validate.isEmailAddress(this.textbox.value, this.flags);
		}
	}
);

/*
  ****** EmailListTextbox ******

  A subclass of EmailTextbox.
  Over-rides isValid to test for a list of email addresses.
  Can use all markup attributes/properties of EmailTextbox and ...

  @attr listSeparator  The character used to separate email addresses.  
    Default is ";", ",", "\n" or " ".
*/
dojo.widget.defineWidget(
	"dojo.widget.validate.EmailListTextbox",
	dojo.widget.validate.EmailTextbox,
	{
		mixInProperties: function(localProperties, frag) {
			// First initialize properties in super-class.
			dojo.widget.validate.EmailListTextbox.superclass.mixInProperties.apply(this, arguments);
	
			// Get properties from markup attributes, and assign to flags object.
			if ( localProperties.listseparator ) { 
				this.flags.listSeparator = localProperties.listseparator;
			}
		},

		// Over-ride for email address list validation
		isValid: function() { 
			return dojo.validate.isEmailAddressList(this.textbox.value, this.flags);
		}
	}
);

/*
  ****** DateTextbox ******

  A subclass of ValidationTextbox.
  Over-rides isValid to test if input is in a valid date format.

  @attr format  Described in dojo.validate.js.  Default is  "MM/DD/YYYY".
*/
dojo.widget.defineWidget(
	"dojo.widget.validate.DateTextbox",
	dojo.widget.validate.ValidationTextbox,
	{
		mixInProperties: function(localProperties, frag) {
			// First initialize properties in super-class.
			dojo.widget.validate.DateTextbox.superclass.mixInProperties.apply(this, arguments);
	
			// Get properties from markup attributes, and assign to flags object.
			if ( localProperties.format ) { 
				this.flags.format = localProperties.format;
			}
		},

		// Over-ride for date validation
		isValid: function() { 
			return dojo.validate.isValidDate(this.textbox.value, this.flags.format);
		}
	}
);

/*
  ****** TimeTextbox ******

  A subclass of ValidationTextbox.
  Over-rides isValid to test if input is in a valid time format.

  @attr format    Described in dojo.validate.js.  Default is  "h:mm:ss t".
  @attr amSymbol  The symbol used for AM.  Default is "AM" or "am".
  @attr pmSymbol  The symbol used for PM.  Default is "PM" or "pm".
*/
dojo.widget.defineWidget(
	"dojo.widget.validate.TimeTextbox",
	dojo.widget.validate.ValidationTextbox,
	{
		mixInProperties: function(localProperties, frag) {
			// First initialize properties in super-class.
			dojo.widget.validate.TimeTextbox.superclass.mixInProperties.apply(this, arguments);
	
			// Get properties from markup attributes, and assign to flags object.
			if ( localProperties.format ) { 
				this.flags.format = localProperties.format;
			}
			if ( localProperties.amsymbol ) { 
				this.flags.amSymbol = localProperties.amsymbol;
			}
			if ( localProperties.pmsymbol ) { 
				this.flags.pmSymbol = localProperties.pmsymbol;
			}
		},

		// Over-ride for time validation
		isValid: function() { 
			return dojo.validate.isValidTime(this.textbox.value, this.flags);
		}
	}
);

/*
  ****** UsStateTextbox ******

  A subclass of ValidationTextbox.
  Over-rides isValid to test if input is a US state abbr.

  @attr allowTerritories  Allow Guam, Puerto Rico, etc.  Default is true.
  @attr allowMilitary     Allow military 'states', e.g. Armed Forces Europe (AE). Default is true.
*/
dojo.widget.defineWidget(
	"dojo.widget.validate.UsStateTextbox",
	dojo.widget.validate.ValidationTextbox,
	{
		mixInProperties: function(localProperties, frag) {
			// Initialize properties in super-class.
			dojo.widget.validate.UsStateTextbox.superclass.mixInProperties.apply(this, arguments);

			// Get properties from markup attributes, and assign to flags object.
			if ( localProperties.allowterritories ) { 
				this.flags.allowTerritories = ( localProperties.allowterritories == "true" );
			}
			if ( localProperties.allowmilitary ) { 
				this.flags.allowMilitary = ( localProperties.allowmilitary == "true" );
			}
		},

		isValid: function() { 
			return dojo.validate.us.isState(this.textbox.value, this.flags);
		}
	}
);

/*
  ****** UsZipTextbox ******

  A subclass of ValidationTextbox.
  Over-rides isValid to test if input is a US zip code.
  Validates zip-5 and zip-5 plus 4.
*/
dojo.widget.defineWidget(
	"dojo.widget.validate.UsZipTextbox",
	dojo.widget.validate.ValidationTextbox,
	{
		isValid: function() { 
			return dojo.validate.us.isZipCode(this.textbox.value);
		}
	}
);

/*
  ****** UsSocialSecurityNumberTextbox ******

  A subclass of ValidationTextbox.
  Over-rides isValid to test if input is a US Social Security Number.
*/
dojo.widget.defineWidget(
	"dojo.widget.validate.UsSocialSecurityNumberTextbox",
	dojo.widget.validate.ValidationTextbox,
	{
		isValid: function() { 
			return dojo.validate.us.isSocialSecurityNumber(this.textbox.value);
		}
	}
);

/*
  ****** UsPhoneNumberTextbox ******

  A subclass of ValidationTextbox.
  Over-rides isValid to test if input is a 10-digit US phone number, an extension is optional.
*/
dojo.widget.defineWidget(
	"dojo.widget.validate.UsPhoneNumberTextbox",
	dojo.widget.validate.ValidationTextbox,
	{
		isValid: function() { 
			return dojo.validate.us.isPhoneNumber(this.textbox.value);
		}
	}
);
