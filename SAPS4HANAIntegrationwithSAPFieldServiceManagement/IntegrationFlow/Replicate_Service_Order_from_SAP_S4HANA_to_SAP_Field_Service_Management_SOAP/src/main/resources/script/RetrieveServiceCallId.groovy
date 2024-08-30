import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.util.XmlParser;
import groovy.json.JsonSlurper;
import groovy.*

def Message processData(Message message) {
    def body = message.getBody(String.class)
    def object = new JsonSlurper().parseText(body)
    def serviceCallId = object?.data[0]?.serviceCall?.id ?: ""

    message.setProperty("ServiceCallID", serviceCallId)

    return message
}