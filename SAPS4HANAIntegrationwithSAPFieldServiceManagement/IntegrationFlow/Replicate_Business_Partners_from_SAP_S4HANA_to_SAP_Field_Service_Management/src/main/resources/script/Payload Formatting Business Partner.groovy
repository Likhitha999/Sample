import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def Message processData(Message message)
{
    def body = message.getBody(java.lang.String) as String;
    def jsonParser = new JsonSlurper();
    
    body = body.replace("{\"@nil\":\"true\"}","null");
    body = body.replaceAll('\\[""\\]','[]');
    def jsonObject = jsonParser.parseText(body);
    jsonObject.BP.each{
        if(it.type == ""){
            it.type = null
        }
    }
    message.setBody(JsonOutput.toJson(jsonObject["BP"])); 
    message.setProperty("RequestPayload", JsonOutput.toJson(jsonObject["BP"]));
    
    return message;
}