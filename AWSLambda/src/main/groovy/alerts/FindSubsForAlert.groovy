package alerts

import com.amazonaws.services.lambda.runtime.Context

class FindSubsForAlert {

    public static final String ES_HOST = 'localhost';

    // Cache the ES connection so we can reuse if this lambda is called again in this instance
    def es = null;

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
      checkESConnection()
      
      if ( shapeType.equalsIgnoreCase('polygon') ) {
        search_future = esclient.search {
          indices 'alertssubscriptions'
          types 'alertsubscription'
          source {
            from = result.offset
            size = result.max
            query {
              bool {
                must {
                  query_string (query: '*')
                }
                filter {
                  geo_shape {
                    subShape {
                      shape {
                        type = "polygon"
                        coordinates = shapeCoordinates
                      }
                      relation = "intersects"
                    }
                  }
                }
              }
            }
            sort = [
              recid : [order : "asc"]
            ]
          }
        }
      }
      else {
        search_future = esclient.search {
          indices 'alertssubscriptions'
          types 'alertsubscription'
          source {
            from = result.offset
            size = result.max
            query {
              bool {
                must {
                  query_string (query: '*')
                }
                filter {
                  geo_shape {
                    areas.alertShape {
                      shape {
                        type = "circle"
                        coordinates = shapeCoordinates // result.alert.shapeCoordinates
                        radius =  radius
                      }
                      relation = "intersects"
                    }
                  }
                }
              }
            }
            sort = [
              recid : [order : "asc"]
            ]
          }
        }
      }

      def search_response =  search_future.get()
      // result.hitcount = search_response.hits.totalHits
      // result.hits = []
      // search_response.hits.hits.each { hit ->
      // // log.debug("Adding hit ${hit}");
      // result.hits.add(hit.source)
      // }

      return [:]
    }

    def checkESConnection() {
      if ( es == null ) {

        Settings settings = Settings.settingsBuilder()
                       .put("client.transport.sniff", true)
                       .put("cluster.name", "elasticsearch")
                       .build();
        es = TransportClient.builder().settings(settings).build();
        // add transport addresses
        es.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ES_HOST), 9300 as int))
      }
    }

}
