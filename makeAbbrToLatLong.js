var fs = require('fs');
var data = JSON.parse(fs.readFileSync('json.txt'));

for (var i = 0; i < data.station.length; i++) {
  var station = data.station[i];
  console.log('.put("' + station.abbr.toLowerCase() + '", new LatLng(' + station.gtfs_latitude + ", " + station.gtfs_longitude + '))');
}
