var http = require('http');
var querystring = require('querystring');

/**
 * See https://nodejs.org/api/http.html#http_http_request_options_callback
 */
exports.handler = function(event, context) {

    var shapeCoordinates = [[[]]];

    var postData = querystring.stringify({
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

    var options = {
      hostname: 'ce.semweb.co',
      port: 80,
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
