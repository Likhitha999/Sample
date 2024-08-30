import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def Message processData(Message message)
{
    def body = message.getBody(java.lang.String) as String;
    def jsonParser = new JsonSlurper();
    body = body.replace("{\"@nil\":\"true\"}","null");
    def jsonObject = jsonParser.parseText(body);
    message.setBody(JsonOutput.toJson(jsonObject["Person"]));
    message.setProperty("RequestPayload", JsonOutput.toJson(jsonObject["Person"]));
    
    return message;
}