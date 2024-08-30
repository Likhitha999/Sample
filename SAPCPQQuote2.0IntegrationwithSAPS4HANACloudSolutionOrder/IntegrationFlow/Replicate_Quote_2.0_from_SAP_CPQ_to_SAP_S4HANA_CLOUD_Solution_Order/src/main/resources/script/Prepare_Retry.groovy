import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;

def Message processData(Message message) {
       map = message.getProperties();
       mapHeaders = message.getHeaders();
       def RetryCount = mapHeaders.get("RetryCount")
       if ( RetryCount != null)
       {
            RetryCountInt = RetryCount as int 
            RetryCountInt = RetryCountInt + 1
            RetryCountString = RetryCountInt as String
        }
       else
       {
            RetryCount = 1
            RetryCountString = RetryCount as String
       }
       CPQQuoteID = map.get("CPQQuoteNumber");
       ExceptionMessage = map.get("CamelExceptionCaught");
       ExceptionProcessName = map.get("ExceptionProcessName");
       Notification = map.get("EventPayload");
       def messageLog = messageLogFactory.getMessageLog(message);
       if(messageLog != null){
         def loggingmessage = "CPQ Quote processing failed for CPQ-Quote: " + CPQQuoteID + " in step " + ExceptionProcessName + " with error " + ExceptionMessage + ". Processing will be retried."
         messageLog.addAttachmentAsString("Iflow Error", loggingmessage, "text/plain");
         messageLog.addAttachmentAsString("Event Payload", Notification, "text/plain");
       }
       message.setHeader("RetryCount", RetryCountString)
       message.setBody(Notification)
       return message;
}
