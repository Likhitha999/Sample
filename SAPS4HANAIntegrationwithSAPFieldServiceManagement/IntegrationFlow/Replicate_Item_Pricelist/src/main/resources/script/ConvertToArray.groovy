import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def Message processData(Message message)
{
    def body = message.getBody(java.lang.String) as String;
    
    if (body.indexOf('[', 0) == -1 || body.indexOf('[', 0) > 0) 
    {
            body = "[" + body + "]";
            }

    def jsonParser = new JsonSlurper();
    def jsonObject = jsonParser.parseText(body);
 
    message.setBody(JsonOutput.toJson(jsonObject));
    message.setProperty("RequestPayload", JsonOutput.toJson(jsonObject));
    return message;
}
