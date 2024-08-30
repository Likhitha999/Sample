import com.sap.gateway.ip.core.customdev.util.Message
import java.util.HashMap

def Message processData(Message message) {
    def body = message.getBody()
    
    try {
        def sapmsg = new XmlSlurper().parseText( body )
        // search for lock message in error details and set http code
        def errormsg = sapmsg.'**'.findAll { it.name() == 'code' }
        errormsg.find{ code ->
            if (code.text() == 'CRM_CONFIRM_SRV/013' ) {
                message.setHeader("CamelHttpResponseCode", 423)
                return true
            }
        } 
    } catch(Exception ex) { /* ignore */ }
    
    return message
}
