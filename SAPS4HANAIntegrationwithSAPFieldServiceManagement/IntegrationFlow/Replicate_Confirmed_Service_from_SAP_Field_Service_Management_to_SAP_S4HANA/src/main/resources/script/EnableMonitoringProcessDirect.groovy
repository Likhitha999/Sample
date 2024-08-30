import com.sap.gateway.ip.core.customdev.util.Message
import java.util.HashMap
// Version 1.0
// get ids from incoming message via ProcessDirect for setting X-Request-ID and custom log headers
def Message processData(Message message) { 
    // add ServiceOrder (id) and ServiceCall (id) to message headers and custom log headers
    def messageLog = messageLogFactory.getMessageLog(message)
    if(messageLog != null) {
        def serviceOrder = message.getHeaders().get("ServiceOrder")
        if(serviceOrder != null && serviceOrder != '') {
            messageLog.addCustomHeaderProperty("ServiceOrder", serviceOrder)
        }
        def serviceCall = message.getHeaders().get("ServiceCall")
        if(serviceCall != null && serviceCall != '') {
            messageLog.addCustomHeaderProperty("ServiceCall", serviceCall)
        }
    }
    // transfer SAP_ApplicationID to X-Request-ID 
    def sapApplicationID = message.getHeaders().get("SAP_ApplicationID")
    if (sapApplicationID != null && sapApplicationID != '') {
        message.setHeader("X-Request-ID", sapApplicationID)   
    }
    return message
}