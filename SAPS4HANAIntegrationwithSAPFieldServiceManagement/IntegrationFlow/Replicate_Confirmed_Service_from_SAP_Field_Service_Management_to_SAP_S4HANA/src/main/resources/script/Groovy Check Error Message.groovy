import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import groovy.util.XmlSlurper;
import groovy.Xml.*
import groovy.XmlSlurper.*

def Message processData(Message message) {
    def confirmationid = message.getProperty("ServiceConfirmationId") as String
    
    if ( confirmationid == '0000000000' || confirmationid == '0' || confirmationid == null || confirmationid == "" ){
        def payload = message.getProperty("sapmsg")
        def errorexist = '' as String
        
        if ( confirmationid == '0000000000' || confirmationid == '0' ){
            throw new Exception("The integration flow message won’t be processed further. Create Service Confirmation Failed");
        }
        else{
           sapmsg = new XmlSlurper().parseText( payload )
           def errormsg = sapmsg.'**'.findAll { it.name() == 'severity' }
           errormsg.each{ e ->
               if (e.text() == 'error'){
                   errorexist = 'X'
               }
           } 
        }
        
    
        
        if (errorexist == 'X'){
            String outxml = groovy.xml.XmlUtil.serialize( sapmsg );
            throw new Exception(outxml);
        }
    }
    
    return message;
        
}