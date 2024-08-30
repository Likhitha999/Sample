import groovy.json.JsonSlurper
import com.sap.gateway.ip.core.customdev.util.Message;

def Message processData(Message message) {
    def body = message.getBody(java.lang.String) as String;
    def jsonSlurper = new JsonSlurper();
    def content = jsonSlurper.parseText(body);
    
    content.each{
        if (it.status >= 400 && it.status < 600) {
            message.setProperty("ErrorType", "FSMError");
            message.setProperty('http.StatusCode', it.status)
            message.setProperty('http.ResponseBody', body)
            if (it.status == 500) {
                message.setProperty('http.StatusText', it.message);
            } else {
                message.setProperty('http.StatusText', it.ex.message);
            }
        }
    }    
    
    return message;
}
