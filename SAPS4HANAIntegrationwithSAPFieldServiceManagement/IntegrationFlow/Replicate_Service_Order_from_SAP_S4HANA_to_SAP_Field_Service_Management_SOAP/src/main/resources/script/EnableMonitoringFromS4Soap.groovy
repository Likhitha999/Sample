import com.sap.gateway.ip.core.customdev.util.Message
import java.util.HashMap
import groovy.util.XmlSlurper
// Version 1.2
// get ids from incoming message from S4/SOAP for setting SAP Application ID, X-Request-ID and custom log headers
def Message processData(Message message) { 
    def body = message.getBody(String.class)
    def serviceOrder, serviceOrderObject, serviceCall, serviceCallObject

    // fetch message id as SAP_ApplicationID and set X-Request-ID
    try {
        def xmlBody = new XmlSlurper().parseText(body)  
        if (xmlBody != null) {
            def sapApplicationID = xmlBody.'**'.find { it.name() == 'ID' }.text()
            if (sapApplicationID != null && sapApplicationID != '') {
                message.setHeader("SAP_ApplicationID", sapApplicationID) 
                message.setHeader("X-Request-ID", sapApplicationID) 
            }
            serviceOrderObject = xmlBody.'**'.find { it.name() == 'ServiceOrder' }
            if (serviceOrderObject != null) {
                serviceOrder = serviceOrderObject.ServiceOrder.text()
            }
            serviceCallObject = xmlBody.'**'.find { it.name() == 'FSMServiceCall' }
            if (serviceCallObject != null) {
                serviceCall = serviceCallObject.text()
            }
        }
    } catch(Exception ex0) {
        // don't set headers
    }
    // add ServiceOrder (id) to message headers and custom log headers
    def messageLog = messageLogFactory.getMessageLog(message)
    if (messageLog != null) {
        if (serviceOrder != null && serviceOrder != '') {
            message.setHeader("ServiceOrder", serviceOrder)
            messageLog.addCustomHeaderProperty("ServiceOrder", serviceOrder)
        }
        if (serviceCall != null && serviceCall != '') {
            message.setHeader("ServiceCall", serviceCall)
            messageLog.addCustomHeaderProperty("ServiceCall", serviceCall)
        }
    }
    // add payload to message property
    message.setProperty("OriginalPayload", body)
    return message
}