import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;

def Message processData(Message message) {
       map = message.getProperties();
       CPQQuoteID = map.get("CPQQuoteNumber");
       ExceptionMessage = map.get("CamelExceptionCaught");
       ExceptionProcessName = map.get("ExceptionProcessName");
       Notification = map.get("Notification");
       def messageLog = messageLogFactory.getMessageLog(message);
       if(messageLog != null){
         def loggingmessage = "Retry failed after 5 Retries/10 Seconds for CPQ-Quote: " + CPQQuoteID + " in step " + ExceptionProcessName + " with error " + ExceptionMessage + ". Notification is stored in SCI data store for 30 days"
         messageLog.addAttachmentAsString("Error during processing: ", loggingmessage, "text/plain")
         messageLog.addAttachmentAsString("Notification-Content:", Notification, "text/plain");
       }
       return message;
}
