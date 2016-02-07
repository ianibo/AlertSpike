package capalerts

import grails.converters.JSON

class ProfileController {

  def ESWrapperService

  def index() { 
    log.debug("ProfileController::index - list current alerts");
    def result = [:]
    result.alerts = AlertProfile.executeQuery('select ap from AlertProfile as ap where ap.name like ?',['%'], [max: 10, offset: 0]);

    withFormat {
      html result
      json { render result as JSON }
    }
  }

  def feed2() {
    log.debug("ProfileController::feed(${params.id})");


    def result = [:]
    result.offset = 0;
    result.max = 10;

    def esclient = ESWrapperService.getClient();

    if ( ( params.id ) && ( esclient ) ) {
      result.alert = AlertProfile.get(params.id)
      if ( result.alert ) {
        log.debug("Got feed ${result.alert.id} ${result.alert.name} ${result.alert.shapeType} ${result.alert.shapeCoordinates} ${result.alert.radius}");

        def query_str="*"

        try {

          def search_future = esclient.search {
                       indices 'alerts'
                       types 'alert'
                       source {
                         from = result.offset
                         size = result.max
                         query {
                           query_string (query: '*')
                         }
                       }
                     }

          def search_response =  search_future.get()
          log.debug("Got response: ${search_response}");
          result.hits = []
          search_response.hits.hits.each { hit ->
            log.debug("Adding hit ${hit}");
            result.hits.add(hit.source)
          }
        }
        catch ( Exception e ) {
          log.error("Error processing search", e);
        }

      }
      else {
        result.message = "Unable to locate profile with id "+params.id;
      }
    }
    else {
    }

    withFormat {
      json { render result as JSON }
      html result
    }

  }

  def feed() {
    log.debug("ProfileController::feed(${params.id})");


    def result = [:]
    result.offset = 0;
    result.max = 10;

    def esclient = ESWrapperService.getClient();

    if ( ( params.id ) && ( esclient ) ) {
      result.alert = AlertProfile.get(params.id)
      if ( result.alert ) {
        log.debug("Got feed ${result.alert.id} ${result.alert.name} ${result.alert.shapeType} ${result.alert.shapeCoordinates} ${result.alert.radius}");

        def query_str="*"

        try {

          def search_future = esclient.search {
                       indices 'alerts'
                       types 'alert'
                       source {
                         from = result.offset
                         size = result.max
                         // query {
                         //   query_string (query: query_str)
                         // }

                         query {
                           bool : {
                             must : {
                               match_all : {}
                             }
                             filter : {
                               nested : {
                                 geo_shape : {
                                   'areas.alertShape' : {
                                     shape : {
                                       type : "polygon"
                                       coordinates : result.alert.shapeCoordinates
                                     }
                                     relation : "intersects"
                                   }
                                 }
                               }
                             }
                           }
                         }

                         // facets {
                         //   'Component Type' {
                         //     terms {
                         //       field = grailsApplication.config.globalSearch.typingField
                         //     }
                         //   }
                         // }
                       }
                     }

          def search_response =  search_future.get()
          log.debug("Got response: ${search_response}");
          result.hits = []
          search_response.hits.hits.each { hit ->
            log.debug("Adding hit ${hit}");
            result.hits.add(hit.source)
          }
        }
        catch ( Exception e ) {
          log.error("Error processing search", e);
        }

      }
      else {
        result.message = "Unable to locate profile with id "+params.id;
      }
    }
    else {
    }

    withFormat {
      json { render result as JSON }
      html result
    }

  }

}
