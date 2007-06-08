if (!$defined(Element.getText))
{
   Element.extend(
   {
      getText: function()
      {
         return this.innerText || this.textContent || "";
      }
   });
}


var DatePicker = new Class(
{
   options:
   {
      onShow: Class.empty,
      onHide: Class.empty,
      readOnly: true,
      showToday: true,
      dateFormat: "string",
      monthNamesShort: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],
      monthNamesLong: ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"],
      dayNamesShort: ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"],
      dayNamesLong: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"]
   },
  
   initialize: function(el, options)
   {
      this.element = el;
      this.setOptions(options);

      this.calendar = new Element("div", {"id": "date-picker"}).inject(document.body);
      this.calendar.addEvent("mouseenter", this.onMouseEnter.bind(this));
      this.calendar.addEvent("mouseleave", this.onMouseLeave.bind(this));

      this.wrapper = new Element("div", {"id": "date-wrapper"}).inject(this.calendar);
      
      this.currentDate = null;
      this.isVisible = false;
      this.hasMouse = false;
   
      if (this.options.readOnly)
      {
         el.setProperty("readonly", "readonly");
      }
      el.addEvent("click", this.toggleCalendar.bind(this));
      el.addEvent("blur", this.onBlur.bind(this));
   },
		
   position: function(el)
   {
      var pos = $(el).getPosition();
      this.calendar.setStyles(
      {
         "left": pos.x,
         "top": pos.y + $(el).getCoordinates().height
      });
   },
	
   show: function()
   {
      this.isVisible = true;
      this.calendar.setStyle("visibility", "visible");
      this.fireEvent("onShow", [this.calendar]);
   },

   hide: function()
   {
      this.isVisible = false;
      this.hasMouse = false;
      this.calendar.setStyle("visibility", "hidden");
      this.fireEvent("onHide", [this.calendar]);
   },

   onMouseEnter: function()
   {
      this.hasMouse = true;
   },

   onMouseLeave: function()
   {
      this.hasMouse = false;
      this.onBlur.delay(500, this);
   },

   onBlur: function()
   {
      if (!this.hasMouse)
      {
         this.hide();
      }
   },

   toggleCalendar: function()
   {
      if (this.isVisible)
      {
         // Hide the calendar
         this.hide();
      }
      else
      {
         this.originalDate = this.convert(this.element.value.toString(), "date");
         this.renderCalendar(this.element);
         this.show();
      }
   },

	/* Main calendar render function */
   renderCalendar: function(el, dt)
   {
      this.currentDate = this.convert((($defined(dt)) ? dt : el.value), "date");

      this.position(el);
      
      /** Set up all the dates we need */
      var lastMonth = new Date(this.currentDate).setMonth(this.currentDate.getMonth() - 1); // The previous month to the display date
      var nextMonth = new Date(this.currentDate).setMonth(this.currentDate.getMonth() + 1); // The next month to the display date
      var lastYear = new Date(this.currentDate).setFullYear(this.currentDate.getFullYear() - 1); // The previous year to the display date
      var nextYear = new Date(this.currentDate).setFullYear(this.currentDate.getFullYear() + 1); // The next year to the display date
      
      var firstDay = new Date(this.currentDate); // The first day of the month for the display date
      firstDay.setDate(1);
      if (firstDay.getDay() > 0)
      {
         firstDay.setDate(-firstDay.getDay() + 1);
      }
      
      var currentDay = new Date(firstDay);
      var today = new Date(); // Todays date
      
      /** Clear any previous dom and refill it*/
      this.wrapper.empty();
      
      /** Global vars and initial dom stuff */
      var table, tbody, row, td, highlight;
      table = new Element("table",
      {
         "id": "date-table",
         "class": "date-table"
      });
      tbody = new Element("tbody").injectInside(table)
      
      /** Build the skip month/date controls */
      row = new Element("tr").injectInside(tbody);

      new Element("td",
      {
         "class": "date-monthswitch",
         "events":
         {
            "click" : this.renderCalendar.bind(this, [el, lastMonth])
         }
      }).appendText("<").injectInside(row);

      new Element("td",
      {
         "colSpan": 5, 
         "rowSpan": 2,
         "class" : "date-monthandyear"
      }).appendText(this.options.monthNamesLong[this.currentDate.getMonth()] + " " + this.currentDate.getFullYear()).injectInside(row);

      new Element("td",
      {
         "class": "date-monthswitch",
         "events":
         {
            "click" : this.renderCalendar.bind(this, [el, nextMonth])
         }
      }).appendText(">").injectInside(row);

      row = new Element("tr").injectInside(tbody);

      new Element("td",
      {
         "class": "date-yearswitch",
         "events":
         {
            "click" : this.renderCalendar.bind(this, [el, lastYear])
         }
      }).appendText("<<").injectInside(row);

      new Element("td",
      {
         "class": "date-yearswitch",
         "events":
         {
            "click" : this.renderCalendar.bind(this, [el, nextYear])
         }
      }).appendText(">>").injectInside(row);
      
      /** Push out the day names */
      row = new Element("tr").injectInside(tbody);
      for (i = 0; i < 7; i++)
      { 
         new Element("th").appendText(this.options.dayNamesShort[i].substr(0,2)).injectInside(row);
      }
      
      highlight = this.highlight.bind(this);
      
      /* Populate the dates we can pick */
      while (currentDay.getMonth() == this.currentDate.getMonth() || currentDay.getMonth() == firstDay.getMonth())
      {
         row = new Element("tr").injectInside(tbody);
         for (i = 0; i < 7; i++)
         {
            td = new Element("td").appendText(currentDay.getDate()).injectInside(row);
            td.addClass((currentDay.getDay() == 0 || currentDay.getDay() == 6) ? "date-weekend" : "date-workday");
            if (currentDay.getMonth() != this.currentDate.getMonth())
            {
               td.addClass("date-offmonth");
            }
            else
            {
               td.addClass("date-day");
               td.addEvents(
               {
                  "click": this.selectValue.bindWithEvent(this),
                  "mouseenter": highlight,
                  "mouseleave": highlight
               });
            }
            if (currentDay.getDate() == today.getDate() && currentDay.getMonth() == today.getMonth() && currentDay.getFullYear() == today.getFullYear())
            {
               td.addClass("date-today");
            }
            if (currentDay.getDate() == this.originalDate.getDate()
               && currentDay.getMonth() == this.originalDate.getMonth()
               && currentDay.getFullYear() == this.originalDate.getFullYear())
            {
               td.addClass("date-picked");
            }
            currentDay.setDate(currentDay.getDate() + 1);
         }
      }
      
      /** Add the select today choice */
      if (this.options.showToday)
      {
         row = new Element("tr").injectInside(tbody);
         new Element("td",
         {
            "colSpan": 7, 
            "class" : "date-todayfooter",
            "events":
            {
               "click" : this.renderCalendar.bind(this, [el, today])
            }
         }).appendText("Today: " + this.convert(today, "dd/MMM/yyyy")).injectInside(row);
      }
		
      table.injectInside(this.wrapper);	
   },
   
   highlight: function (ev)
   {
      var e = new Event(ev);
      e.target.toggleClass("date-tdover");
   },
   
   selectValue: function (ev)
   {
      var e = new Event(ev);
      e.stopPropagation();
      var o = $(e.target);
      var pickedDate = this.currentDate.setDate(o.getText());
      this.element.value = this.convert(pickedDate, this.options.dateFormat);
      this.hide();	  
   },

   convert: function(o, format)
   {
      var d = new Date();
      if (o.getFullYear)
      {
         d = o;
      }
      else if ($type(o) == "number")
      {
         d = new Date(o);
      }
      else if ($type(o) == "object")
      {
         d = new Date(o.year, o.month, o.date);
      }
      else if ($type(o) == "string")
      {
         d = new Date(o);
         if ((d.toString() == "Invalid Date") || (d.toString() == "NaN"))
         {
            d = new Date();
         }
      }

      if (format == "date")
      {
         return d;
      }
      else if (format == "object")
      {
         return(
         {
            date: d.getDate(),
            month: d.getMonth(),
            year: d.getFullYear()
         });
      }
      else if (format == "number")
      {
         return d.getTime();
      }
      else if (format == "string")
      {
         return d.getDate() + "/" + (d.getMonth() + 1) + "/" + d.getFullYear();
      }
      
      // Otherwise, assume we've been given a format string for formatDate
      return this.formatDate(d, format);
   },
   
   formatDate: function(dt, format)
   {
      if (!dt.valueOf())
      {
         return '';
      }
      
      window.monthNamesLong = this.options.monthNamesLong;
      window.monthNamesShort = this.options.monthNamesShort;
      window.dayNamesLong = this.options.dayNamesLong;
      window.dayNamesShort = this.options.dayNamesShort;
      
      return format.replace(/(yyyy|yy|y|MMMM|MMM|MM|M|dddd|ddd|dd|d|HH|H|hh|h|mm|m|ss|s|t)/gi, function($1, $2, $3, $4, $5)
      {
         switch ($1)
         {
            case 'yyyy': return dt.getFullYear();
            case 'yy':   return ('0' + (dt.getFullYear()%100)).zeroFill(2);
            case 'y':    return (d.getFullYear()%100);
            case 'MMMM': return window.monthNamesLong[dt.getMonth()];
            case 'MMM':  return window.monthNamesShort[dt.getMonth()];
            case 'MM':   return (dt.getMonth() + 1).zeroFill(2);
            case 'M':    return (dt.getMonth() + 1);
            case 'dddd': return window.dayNamesLong[dt.getDay()];
            case 'ddd':  return window.dayNamesShort[dt.getDay()];
            case 'dd':   return dt.getDate().zeroFill(2);
            case 'd':	 return dt.getDate();
            case 'HH':   return dt.getHours().zeroFill(2);
            case 'H':    return dt.getHours();
            case 'hh':   return ((h = dt.getHours() % 12) ? h : 12).zeroFill(2);
            case 'h':    return ((h = dt.getHours() % 12) ? h : 12);
            case 'mm':   return dt.getMinutes().zeroFill(2);
            case 'm':    return dt.getMinutes();
            case 'ss':   return dt.getSeconds().zeroFill(2);
            case 's':    return dt.getSeconds();
            case 't':	 return dt.getHours() < 12 ? 'A.M.' : 'P.M.';
         }
      });
   }

});

DatePicker.implement(new Events, new Options);

// Used by formatDate function */
String.prototype.zeroFill = function(l)
{
      return '0'.repeat(l - this.length) + this;
}
String.prototype.repeat = function(l)
{
   var s = '', i = 0;
   while (i++ < l)
   {
      s += this;
   }
   return s;
}
Number.prototype.zeroFill = function(l)
{
   return this.toString().zeroFill(l);
}
