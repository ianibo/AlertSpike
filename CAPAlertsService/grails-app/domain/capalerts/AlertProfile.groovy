package capalerts

class AlertProfile {

  String name
  String shortcode
  String shapeType
  String shapeCoordinates
  String radius

  String subscriptionUrl
  String languageOnly
  String highPriorityOnly
  String officialOnly
  String xPathFilterId
  String xPathFilter
  String areaFilterId

  Date dateCreated
  Date lastUpdated

  static constraints = {
    radius nullable:true, blank:false
    lastUpdated nullable:true, blank:false
    subscriptionUrl nullable:true, blank:false
    languageOnly nullable:true, blank:false
    highPriorityOnly nullable:true, blank:false
    officialOnly nullable:true, blank:false
    xPathFilterId nullable:true, blank:false
    xPathFilter nullable:true, blank:false
    areaFilterId nullable:true, blank:false
  }
}
