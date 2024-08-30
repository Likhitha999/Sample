import com.sap.gateway.ip.core.customdev.util.Message
import java.util.HashMap
import groovy.json.JsonSlurper
import groovy.json.JsonOutput


def Message processData(Message message) { 
    def body = message.getBody(String.class)
    def jsonSlurper = new JsonSlurper()
    def object = jsonSlurper.parseText(body)
    def bItemContactPersonPresent = (message.getProperty("ItemContactPersonPresent") == 'true')

    // remove contact tag
    object.root.activities.each{
        if (!it.contact || it.contact?.isEmpty() || it.contact?.externalId?.isEmpty() || it.contact?.externalId == null ){
           if (bItemContactPersonPresent) {          
              it.contact = null             
           } else {
              it.remove("contact")
           }
        } 
    }

    message.setBody(JsonOutput.toJson(object.root))
    message.setProperty("RequestPayload", JsonOutput.toJson(object.root))
    return message
}
