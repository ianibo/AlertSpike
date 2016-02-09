package capalerts

import grails.converters.JSON
import grails.converters.XML
import grails.plugin.springsecurity.annotation.Secured


class AdminController {


  @Secured(['ROLE_USER','IS_AUTHENTICATED_FULLY'])
  def index() { 
    log.debug("AdminController::index - list current alerts");
    def result = [:]
    result.alerts = AlertProfile.executeQuery('select ap from AlertProfile as ap where ap.name like :profileNameQry',[profileNameQry:'%'], [max: 10, offset: 0]);

    withFormat {
      html result
      json { render result as JSON }
    }
  }
}
