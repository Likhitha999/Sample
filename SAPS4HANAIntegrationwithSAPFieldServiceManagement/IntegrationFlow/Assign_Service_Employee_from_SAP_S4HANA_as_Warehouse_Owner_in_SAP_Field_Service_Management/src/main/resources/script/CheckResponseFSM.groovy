import groovy.json.JsonSlurper
import com.sap.gateway.ip.core.customdev.util.Message;

def Message processData(Message message) {
    def body = message.getBody(java.lang.String) as String;
    def jsonSlurper = new JsonSlurper();
    def content = jsonSlurper.parseText(body);
    
    content.each{
        if (it.status >= 400 && it.status < 600) {
            message.setProperty("ErrorType", "FSMError");
            message.setProperty(message.getProperty("CamelSplitIndex")+'http.StatusCode', it.status)
            message.setProperty(message.getProperty("CamelSplitIndex")+'http.ResponseBody', body)
            if (it.status == 500) {
                message.setProperty(message.getProperty("CamelSplitIndex")+'http.StatusText', it.message);
            } else {
                message.setProperty(message.getProperty("CamelSplitIndex")+'http.StatusText', it.ex.message);
            }
        }
    }    
    
    return message;
}
