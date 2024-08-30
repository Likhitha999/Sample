import groovy.json.JsonSlurper
import com.sap.gateway.ip.core.customdev.util.Message;

def Message processData(Message message) {
    def body = message.getBody(java.lang.String) as String;
    def map = message.getHeaders();
    def sap_msg = map.get("sap-message");
    def sap_ext_id = map.get('template-ext-id');
    if(sap_msg == null){
        if(sap_ext_id == null){
            message.setProperty("UpdateFSM", "false"); 
        }else{
            message.setProperty("UpdateFSM", "true");
            message.setBody('''{"externalId":"'''+sap_ext_id+'''"}''');
        }
    }else{
        message.setProperty("UpdateFSM", "true");
        def jsonSlurper = new JsonSlurper();
        def content = jsonSlurper.parseText(sap_msg);
        if (content.severity == 'error'){
            message.setProperty("ErrorType", "S4Error");
            message.setProperty('http.ResponseBody', body);
            message.setProperty('http.StatusText', content.message);
        }else{
            message.setBody('''{"externalId":"'''+sap_ext_id+'''"}''');
        }
    }
    return message;
}
