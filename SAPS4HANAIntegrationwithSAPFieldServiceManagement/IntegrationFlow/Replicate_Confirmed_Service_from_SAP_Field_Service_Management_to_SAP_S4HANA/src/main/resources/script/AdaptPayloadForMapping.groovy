import com.sap.gateway.ip.core.customdev.util.Message
import java.util.HashMap
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def Message processData(Message message) {
    def body = message.getBody(String.class)
    def jsonSlurper = new JsonSlurper()
    def bodyJson = jsonSlurper.parseText(body) as Map
    
    // get external ids from message header
    def serviceOrder = message.getHeaders().get("ServiceOrder") ?: ''
    def serviceOrderItem = message.getHeaders().get("ServiceOrderItem") ?: ''
    def serviceOrderBundleItem = message.getHeaders().get("ServiceOrderBundleItem") ?: ''
    def serviceOrderParentItem = message.getHeaders().get("ParentServiceOrderItem") ?: ''
    def eventType = message.getHeaders().get("EventType") ?: ''

    //Set property ServiceOrderID
    message.setProperty("ServiceOrderId", serviceOrder)
    message.setProperty("ServiceOrderItemId", serviceOrderItem)
    
    //Set property ActivityId
    message.setProperty("ActivityId", bodyJson.data.serviceCall.activity.unifiedIdentifier.id)
    
    //Adapt payload
    bodyJson = bodyJson.data.serviceCall.activity
    bodyJson = JsonOutput.toJson(bodyJson)
    body = '{"activity" : ' + bodyJson + '}'
    message.setProperty("ActivityPayload",body)
    message.setBody(body)
    
    return message 
}