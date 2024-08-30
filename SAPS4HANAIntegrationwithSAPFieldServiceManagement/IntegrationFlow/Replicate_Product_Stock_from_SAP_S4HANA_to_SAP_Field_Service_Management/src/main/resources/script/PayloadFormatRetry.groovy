import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def Message processData(Message message)
{
    def body = message.getBody(java.lang.String) as String;
    def jsonParser = new JsonSlurper();
    def map = message.getProperties()
    Integer delay = map.get("DelayMilliSeconds") ?: 0

    body = body.replace("{\"@nil\":\"true\"}","null");

    def jsonObject = jsonParser.parseText(body);
    message.setBody(JsonOutput.toJson(jsonObject["data"])); 
    message.setProperty("RequestPayloadWarehouse", JsonOutput.toJson(jsonObject["data"]));
    
    sleep(delay);
    def loopcounter = message.getProperty("CamelLoopIndex")
    
    if(loopcounter == 4)
        message.setProperty("WareHouseLocked",'Y');
    
    return message;
}