package capalerts

import grails.converters.JSON

class ProfileController {

  def index() { 
    def result = [:]
    result.alerts = AlertProfile.executeQuery('select ap from AlertProfile as ap where ap.name like ?',['%'], [max: 10, offset: 0]);
    withFormat {
      json { render result as JSON }
      html result
    }
  }

  def feed() {

    def result = [:]

    if ( params.id ) {
      result.alert = AlertProfile.get(params.id)
      if ( result.alert) {
      }
      else {
        result.message = "Unable to locate profile with id "+params.id;
      }
      render result as JSON
    }
    else {
    }

  }

}
