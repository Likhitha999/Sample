import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def Message processData(Message message)
{
    def body = message.getBody(java.lang.String) as String;
    body = body.replace("{\"@nil\":\"true\"}","null");
    def jsonParser = new JsonSlurper();
    
    def jsonObject = jsonParser.parseText(body);
    
    jsonObject["PROD"].each {
        if(it["nameTranslations"] == "")
            it["nameTranslations"] = null;
    }
    
    message.setBody(JsonOutput.toJson(jsonObject["PROD"]));
    message.setProperty("RequestPayload", JsonOutput.toJson(jsonObject["PROD"]));
    
    return message;
}