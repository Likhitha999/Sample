import com.sap.gateway.ip.core.customdev.util.Message
import java.util.HashMap
import groovy.json.JsonSlurper
// Version 1.1
// get ids from incoming message from FSM API Attachment for setting SAP Application ID and custom log headers
def Message processData(Message message) { 
    def body = message.getBody(String.class)
    def jsonSlurper = new JsonSlurper()
    def jsonResult = jsonSlurper.parseText(body)
    
    // fetch X-Request-ID as SAP_ApplicationID
    def sapApplicationID = message.getHeaders().get("X-Request-ID")
    if (sapApplicationID != null && sapApplicationID != '') {
        message.setHeader("SAP_ApplicationID", sapApplicationID) 
    }
    // add ServiceOrder (id) and ServiceCall (id) to message headers and custom log headers
    def messageLog = messageLogFactory.getMessageLog(message)
    if(messageLog != null) {
        def serviceOrder, serviceCall, activityID
        // get ServiceOrder
        try {
            serviceOrder = jsonResult['data']['serviceCall']['externalId'] as String
        } catch(Exception ex1) { /* not found */ }
        if (serviceOrder == null) {
            try {
                activityID = jsonResult['data']['activity']['externalId'] as String
                if(activityID != null && activityID != '') {
                    serviceOrder = activityID.split('/')[0]
                }
            } catch(Exception ex2) { /* not found */ }
        }
        if(serviceOrder != null && serviceOrder != '') {
            message.setHeader("ServiceOrder", serviceOrder)
            messageLog.addCustomHeaderProperty("ServiceOrder", serviceOrder)
        }
        // get ServiceCall
        try {
            serviceCall = jsonResult['data']['serviceCall']['code'] as String
        } catch(Exception ex3) { /* not found */ }
        if(serviceCall != null && serviceCall != '') {
            message.setHeader("ServiceCall", serviceCall)
            messageLog.addCustomHeaderProperty("ServiceCall", serviceCall)
        }
    }
    return message
}