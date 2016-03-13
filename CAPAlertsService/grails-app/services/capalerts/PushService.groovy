package capalerts

import grails.transaction.Transactional


import org.elasticsearch.node.Node
import static org.elasticsearch.node.NodeBuilder.nodeBuilder
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.groovy.*
import org.elasticsearch.common.transport.InetSocketTransportAddress

@Transactional
class PushService {

  static transactional = false
  def grailsApplication
  def ESWrapperService


  def pushPendingRecords() {
  }

  def pushActivity(esclient, domain, activity, recgen_closure) {

    def count = 0;
    try {
      log.debug("pushActivity - ${domain.name} ${activity}");

      def latest_ft_record = PushCursor.findByDomainClassNameAndActivity(domain.name,activity)

      log.debug("result of findByDomain: ${latest_ft_record}");
      if ( !latest_ft_record) {
        latest_ft_record=new PushCursor(domainClassName:domain.name,activity:activity,lastTimestamp:0)
      }

      log.debug("updatei ${activity} ${domain.name} since ${latest_ft_record.lastTimestamp}");
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
        def future;
        if(idx_record['_id'] == null) {
          log.error("******** Record without an ID: ${idx_record} Obj:${r} ******** ")
          continue
        }
        if ( idx_record?.status?.toLowerCase() == 'deleted' ) {
            future = esclient.delete {
              index es_index
              type domain.name
              id idx_record['_id']
            }.actionGet()
        }
        else {
          future = esclient.index {
            index es_index
            type domain.name
            id idx_record['_id']
            source idx_record
          }.actionGet()
        }

        latest_ft_record.lastTimestamp = r.lastUpdated?.getTime()

        count++
        total++
        if ( count > 100 ) {
          count = 0;
          log.debug("processed ${++total} records (${domain.name})");
          latest_ft_record.save(flush:true);
          cleanUpGorm();
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

