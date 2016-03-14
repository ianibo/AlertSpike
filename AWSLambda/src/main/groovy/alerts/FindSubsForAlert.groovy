package alerts

import com.amazonaws.services.lambda.runtime.Context

class FindSubsForAlert {

    // Our lambda function handler
    String myHandler(data, Context context) {
        context.logger.log "received in groovy: $data" "Hello ${data}"
    }

}
