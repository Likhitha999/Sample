import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.xml.*;
import java.io.*;
 
def Message processData(Message message) 
{
    def headers = message.getHeaders();
    def cookie = headers.get("Set-Cookie");
    StringBuffer bufferedCookie = new StringBuffer();
    for (Object item : cookie) 
    {
        bufferedCookie.append(item + "; ");      
    }
    message.setHeader("Cookie", bufferedCookie.toString());
    
    def messageLog = messageLogFactory.getMessageLog(message);
    if(messageLog != null)
    {
        messageLog.setStringProperty("Logging_Cookie", bufferedCookie.toString());
    }
    return message;
}