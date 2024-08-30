import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;

def Message processData(Message message) {
       map = message.getProperties();
       SolutionOrderID = map.get("SOLOID");
       ExceptionMessage = map.get("CamelExceptionCaught");
       MaximumRetries = map.get("MaximumRetries");
       ExceptionProcessName = map.get("ExceptionProcessName");
       Notification = map.get("EventPayload");
       def messageLog = messageLogFactory.getMessageLog(message);
       if(messageLog != null){
         def loggingmessage = "Retry failed after "+ MaximumRetries + " Retries for Solution Order: " + SolutionOrderID + " in step " + ExceptionProcessName + " with error " + ExceptionMessage + ". Notification is stored in Cloud Integratin data store for 30 days"
         messageLog.addAttachmentAsString("Error during processing: ", loggingmessage, "text/plain")
         messageLog.addAttachmentAsString("Event Payload", Notification, "text/plain");
       }
       return message;
}
