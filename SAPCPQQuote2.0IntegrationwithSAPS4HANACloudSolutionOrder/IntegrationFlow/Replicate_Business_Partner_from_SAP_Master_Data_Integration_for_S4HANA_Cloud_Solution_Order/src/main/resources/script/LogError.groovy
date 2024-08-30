import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import java.text.SimpleDateFormat;

def Message LogError(Message message) {
    def map = message.getProperties();
    def messageLog = messageLogFactory.getMessageLog(message);
    def errorMessage = map.get("CPQ_ErrorMessages");
    
    def xmlSlurper = new XmlSlurper();
	def root = xmlSlurper.parseText(map.get("BPMappedPayload"));
    def log = "ExternalId: " + root.ExternalId + ", PartnerId: " + root.PartnerId + ", Id: " + root.Id + ", Message: "; 
    
    Date date = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
    // get an exception java class instance
    def ex = map.get("CamelExceptionCaught");
    if(ex!=null && ex.getClass().getCanonicalName().equals("org.apache.camel.component.ahc.AhcOperationFailedException")) {
        errorMessage = errorMessage + log + ex.getResponseBody();
        
        def logTitle = "Error Message " + sdf.format(date); 
        
        messageLog.addAttachmentAsString(logTitle, errorMessage, "text/plain");
        
        message.setProperty("CPQ_ErrorMessages", errorMessage);
    }
    
    return message;
}

def Message AttachCPQErrorMessages(Message message) {
    def map = message.getProperties();
    def errorMessage = map.get("CPQ_ErrorMessages");
    
    if(errorMessage != null){
        def messageLog = messageLogFactory.getMessageLog(message);
        messageLog.addAttachmentAsString("CPQ_ErrorMessages", errorMessage, "text/plain");
    }
    
    return message;
}