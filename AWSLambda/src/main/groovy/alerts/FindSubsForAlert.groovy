package alerts

import com.amazonaws.services.lambda.runtime.Context

class FindSubsForAlert {

    public static final String ES_HOST = 'localhost';

    // Our lambda function handler
    Map myHandler(data, Context context) {

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
                                 ]
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
                                 ]
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

      def search_response = search_future.get()
      // result.hitcount = search_response.hits.totalHits
      // result.hits = []
      // search_response.hits.hits.each { hit ->
      // // log.debug("Adding hit ${hit}");
      // result.hits.add(hit.source)
      // }

      return [:]
    }
}
