import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def Message processData(Message message)
{
    def body = message.getBody(java.lang.String) as String;
    def jsonParser = new JsonSlurper();
    
    body = body.replace("{\"@nil\":\"true\"}","null");
    def ownerArr = new JsonSlurper().parseText('[]')
    
    def jsonObject = jsonParser.parseText(body);
    jsonObject.Warehouse.data.each{ item ->
        item.owners.each{
            if(it != ""){
                ownerArr << [externalId: it]
            }
        }
        item.owners = ownerArr
        ownerArr = new JsonSlurper().parseText('[]')
    }

    message.setBody(JsonOutput.toJson(jsonObject.Warehouse.data));
    message.setProperty("RequestPayload"+message.getProperty("CamelSplitIndex"), JsonOutput.toJson(jsonObject.Warehouse.data));
    
    return message;
}