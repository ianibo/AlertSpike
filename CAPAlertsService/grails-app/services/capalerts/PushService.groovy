package capalerts

import grails.transaction.Transactional


import org.elasticsearch.groovy.*
import org.hibernate.ScrollMode
import java.nio.charset.Charset
import java.util.GregorianCalendar
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.flush.FlushRequest
import org.elasticsearch.client.Client
import org.elasticsearch.action.admin.indices.create.*
import groovy.json.JsonSlurper

@Transactional
class PushService {

  static transactional = false
  def grailsApplication
  def ESWrapperService


  def pushPendingRecords() {
    log.debug("pushPendingRecords()");
    pushActivity(ESWrapperService.client,capalerts.AlertProfile.class,'PushSubscriptions','alertssubscriptions', 'subscription') { alert_profile ->
      log.debug("Pushing ${alert_profile}");
      def result = [:]
      // result.status = org.status?.value
      result.recid = "${alert_profile.id}:capalerts.AlertProfile".toString()
      result.name = alert_profile.name
      result.shortcode = alert_profile.shortcode
      def js =  new JsonSlurper()
      result.subshape = [
          type:alert_profile.shapeType,
          coordinates:js.parseText(alert_profile.shapeCoordinates)
      ]

      if ( alert_profile.shapeType == 'circle' ) {
        result.subshape.radius = alert_profile.radius
      }

      result

    }
  }

  def pushActivity(esclient, domain, activity, es_index, rectype, recgen_closure) {

    def count = 0;
    try {
      log.debug("pushActivity - ${domain.name} ${activity}");

      def latest_ft_record = PushCursor.findByDomainClassNameAndActivity(domain.name,activity)

      log.debug("result of findByDomain: ${latest_ft_record}");
      if ( !latest_ft_record) {
        latest_ft_record=new PushCursor(domainClassName:domain.name,activity:activity,lastTimestamp:0)
      }

      log.debug("update ${activity} ${domain.name} since ${latest_ft_record.lastTimestamp}");
      def total = 0;
      Date from = new Date(latest_ft_record.lastTimestamp);
      // def qry = domain.findAllByLastUpdatedGreaterThan(from,[sort:'lastUpdated']);

      def c = domain.createCriteria()
      c.setReadOnly(true)
      c.setCacheable(false)
      c.setFetchSize(Integer.MIN_VALUE);

      c.buildCriteria{
          or {
            gt('lastUpdated', from)
            and {
              gt('dateCreated', from)
              isNull('lastUpdated')
            }
          }
          order("lastUpdated", "asc")
      }

      def results = c.scroll(ScrollMode.FORWARD_ONLY)

      log.debug("Query completed.. processing rows...");

      while (results.next()) {
        Object r = results.get(0);

        def idx_record = recgen_closure(r)

        log.debug("Index: ${idx_record}");

        def future;
        if(idx_record['recid'] == null) {
          log.error("******** Record without an ID: ${idx_record} Obj:${r} ******** ")
          continue
        }

        try {
          if ( idx_record?.status?.toLowerCase() == 'deleted' ) {
            future = esclient.delete {
              index es_index
              type rectype
              id idx_record['_id']
            }.actionGet()
          }
          else {
            future = esclient.index {
              index es_index
              type 'alertsubscription'
              id idx_record['recid']
              source idx_record
            }.actionGet()
          }
        }
        catch ( Exception e ) {
          e.printStackTrace()
        }

        latest_ft_record.lastTimestamp = r.lastUpdated?.getTime()

        count++
        total++
        if ( count > 100 ) {
          count = 0;
          log.debug("processed ${++total} records (${domain.name})");
          latest_ft_record.save(flush:true);
        }
      }
      results.close();

      log.debug("Processed ${total} records for ${domain.name}");

      // update timestamp
      latest_ft_record.save(flush:true);
    }
    catch ( Exception e ) {
      log.error("Problem with FT index",e);
    }
    finally {
      log.debug("Completed processing on ${domain.name} - saved ${count} records");
    }
  }

}

