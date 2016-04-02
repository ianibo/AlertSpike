var http = require('http');
var querystring = require('querystring');

/**
 * See https://nodejs.org/api/http.html#http_http_request_options_callback
 *
 * This handler accepts an event containing a GeoJson shape definition. Normally [tho not constrained so] this will
 * be a polygon or a circle. The handler will return a list of all subscriptions intersecting that alert profile.
 * Example events include
 *
 *  shape : {
 *    "type": "polygon",
 *    "coordinates" : [ [ [-109.5297,40.4554], [-109.5298,40.4556], [-109.5299,40.4556], [-109.5299,40.4554], [-109.5297,40.4554] ] ]
 *  }
 *
 *  shape : {
 *    "type": "circle",
 *    "coordinates" : [-109.5288,40.4555],
 *    "radius" : "1000m"
 *  }
 *
 */
exports.handler = function(event, context) {

    var postData = JSON.stringify({
                     "from":0,
                     "size":1000,
                     "query":{
                       "bool": {
                         "must": {
                           "match_all": {}
                         },
                         "filter": {
                             "geo_shape": {
                               "subshape": {
                                 "shape": event.shape,
                                 relation:'intersects'
                               }
                             }
                           }
                         }
                       },
                       "sort":{
                         "recid":{order:'asc'}
                       }

    });

    console.log("Send query %s",postData);

    var options = {
      hostname: 'ce.semweb.co',
      port: 80,
      json: true,
      body: postData,
      path: '/es/alertssubscriptions/_search',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': postData.length
      }
    };


    var req = http.request(options, function(res) {
        var body = '';
        console.log('Status:', res.statusCode);
        console.log('Headers:', JSON.stringify(res.headers));
        res.setEncoding('utf8');
        res.on('data', function(chunk) {
            body += chunk;
        });

        res.on('end', function() {
            console.log('Successfully processed HTTPS response');
            // If we know it's JSON, parse it
            if (res.headers['content-type'] === 'application/json') {
                body = JSON.parse(body);
            }
            context.succeed(body);
        });
    });

    req.on('error', context.fail);

    req.write(postData);
    req.end();
};
