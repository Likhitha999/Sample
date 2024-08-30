import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper

// sanity check JSON message before API call
def Message processData(Message message) { 
    def body = message.getBody(String.class)
    def jsonSlurper = new JsonSlurper()
    def servicecall = jsonSlurper.parseText(body)
    def map = message.getProperties()
    def exceptionMsg = ''

    // check and collect exception messages
    if (map.get("ServiceOrderID") == '') {
        exceptionMsg += "The integration flow message won’t be processed further. ServiceOrderID empty or missing.\n"
    }
    
    if (exceptionMsg !='') {
        throw new Exception(exceptionMsg)
    }
    
    return message
}