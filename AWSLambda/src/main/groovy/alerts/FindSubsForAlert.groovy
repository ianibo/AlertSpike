package alerts

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import groovyx.net.http.*
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import groovyx.net.http.*
import org.apache.http.entity.mime.*
import org.apache.http.entity.mime.content.*
import java.nio.charset.Charset
import static groovy.json.JsonOutput.*
import groovy.json.JsonSlurper
import java.text.SimpleDateFormat
import java.net.InetAddress;


class FindSubsForAlert {

    public static final String ES_HOST = 'localhost';

    // public static void handler(InputStream inputStream, OutputStream outputStream, Context context) {
    // Our lambda function handler
    public Map myHandler(data, Context context) {

        // LambdaLogger logger = context.getLogger();
        // logger.log("received : " + myCount);
        def result = null;

        // Input should be a JSON document with shapeType, shapeCoordinates and optionally radius if shapeType is circle
        if ( data.shapeType == null )
          result = [ status:'ERROR', code:-1, message:'No shapeType in input json'  ]
        else if ( data.shapeCoordinates == null )
          result = [ status:'ERROR', code:-2, message:'No shapeCoordinates in input json' ]
        else
           result = findMatchingSubscriptions(data.shapeType, data.shapeCoordinates, data.radius)

        result
    }

    def findMatchingSubscriptions(shapeType, shapeCoordinates, radius) {

      def es = new RESTClient("http://localhost:9200")
      def res = null;

      if ( shapeType.equalsIgnoreCase('polygon') ) {
        res = es.post(path:"/alertssubscriptions/_search",
                   requestContentType: JSON,
                   body:[
                     "from":0,
                     "size":1000,
                     "query":[
                       "bool": [
                         "must": [
                           "match_all": [:]
                         ],
                         "filter": [
                             "geo_shape": [
                               "subshape": [
                                 "shape": [
                                   "type": "polygon",
                                   "coordinates" : shapeCoordinates
                                 ],
                                 relation:'intersects'
                               ]
                             ]
                           ]
                         ]
                       ],
                       "sort":[
                         "recid":[order:'asc']
                       ]
                   ])
      }
      else {
        res = es.post(path:"/alertssubscriptions/_search",
                   requestContentType: JSON,
                   body:[
                     "from":0,
                     "size":1000,
                     "query":[
                       "bool": [
                         "must": [
                           "match_all": [:]
                         ],
                         "filter": [
                             "geo_shape": [
                               "subshape": [
                                 "shape": [
                                   "type": "circle",
                                   "coordinates" : shapeCoordinates,
                                   "radius" : radius
                                 ],
                                 relation:'intersects'
                               ]
                             ]
                           ]
                         ]
                       ],
                       "sort":[
                         "recid":[order:'asc']
                       ]
                   ])
      }

      println("res: ${res}");

      return [res:res]
    }
}
