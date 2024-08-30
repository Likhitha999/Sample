import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import groovy.util.XmlSlurper
import groovy.Xml.*
import groovy.XmlSlurper.*
import groovy.json.*

def Message processData(Message message) {
    def body = message.getBody(String.class)
    def serviceConfirmation = new XmlSlurper().parseText(body)
    
    // get confirmation id
    def confirmationId = serviceConfirmation.A_ServiceConfirmationType.ServiceConfirmation as String
    
    //construct payload
    def FSMPayload = []

    serviceConfirmation.A_ServiceConfirmationType.to_Item.A_ServiceConfirmationItemType.each{ it ->
        def fsmConfId = it.FSMServiceConfirmationItem as String
        
        //for old Service Confirmation API, FSMServiceConfirmation does not exist -> do not add to payload
        if (fsmConfId != ""){
            def s4ConfId = confirmationId + "/" + it.ServiceConfirmationItem as String
            def object = new JsonBuilder()
            object { "id" (fsmConfId)
                     "confirmationitem" (s4ConfId) }

            FSMPayload.add(object)
        }
    }

    message.setProperty("FSMRequestPayload", new JsonBuilder(FSMPayload).content.toString())
    
    return message;
        
}