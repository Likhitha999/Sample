import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def Message processData(Message message)
{
    def body = message.getBody(java.lang.String) as String;
    def jsonParser = new JsonSlurper();
    def jsonObject = jsonParser.parseText(body);
    
    if (jsonObject.PROD.getClass() == java.util.ArrayList) {
        message.setBody(JsonOutput.toJson(jsonObject["PROD"]));
    } else {
        body = "[" + body  + "]";
        jsonObject = jsonParser.parseText(body);
        message.setBody(JsonOutput.toJson(jsonObject["PROD"]));
    }

    return message;
}