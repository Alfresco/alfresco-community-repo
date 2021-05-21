var today = new Date();

var tomorrow = new Date();
tomorrow.setDate(today.getDate() + 1);

var lastSunday = new Date();
lastSunday.setDate(today.getDate() - today.getDay());

var sunday = new Date();
sunday.setDate(lastSunday.getDate() + 7);

var nextSunday = new Date();
nextSunday.setDate(sunday.getDate() + 7);

var future = new Date();
future.setYear(9999);

model.tomorrow = tomorrow;
model.lastSunday = lastSunday;
model.sunday = sunday;
model.nextSunday = nextSunday;
model.future = future;