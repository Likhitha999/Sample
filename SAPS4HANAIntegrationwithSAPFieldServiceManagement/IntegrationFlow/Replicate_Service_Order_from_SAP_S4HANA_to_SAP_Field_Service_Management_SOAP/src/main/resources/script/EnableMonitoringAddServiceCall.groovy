import com.sap.gateway.ip.core.customdev.util.Message
import java.util.HashMap
// Version 1.0
// add ServiceCall to custom log headers
def Message processData(Message message) { 
    def serviceCall = message.getProperty('ServiceCallCode')

    if (serviceCall != null && serviceCall != '') {
        // add ServiceCall (id) to message headers and custom log headers
        def messageLog = messageLogFactory.getMessageLog(message)
        if (messageLog != null) {
            message.setHeader("ServiceCall", serviceCall)
            messageLog.addCustomHeaderProperty("ServiceCall", serviceCall)
        }
    }
    return message
}