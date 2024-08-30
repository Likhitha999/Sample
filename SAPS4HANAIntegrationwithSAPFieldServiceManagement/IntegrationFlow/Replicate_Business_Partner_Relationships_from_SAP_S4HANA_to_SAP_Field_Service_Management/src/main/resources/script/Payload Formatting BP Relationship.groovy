import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def Message processData(Message message)
{
    def body = message.getBody(java.lang.String) as String;
    body = body.replace("{\"@nil\":\"true\"}","null");
    def jsonParser = new JsonSlurper();
    def jsonObject = jsonParser.parseText(body);
    
    message.setBody(JsonOutput.toJson(jsonObject["CP"]));
    message.setProperty("RequestPayload", JsonOutput.toJson(jsonObject["CP"]));
    
    return message;
}