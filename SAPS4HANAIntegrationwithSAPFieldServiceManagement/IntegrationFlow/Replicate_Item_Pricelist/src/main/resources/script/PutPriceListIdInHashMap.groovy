import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def Message processData(Message message)
{
    def body = message.getBody(java.lang.String) as String;
    def jsonParser = new JsonSlurper();
    def jsonObject = jsonParser.parseText(body);
    Map<String,String> priceListidList = new HashMap<>();
    def anyPriceListCreated = false;
    
    jsonObject.each {  
        if(it.status == 201)
        {
            priceListidList.put(it.priceList.code, it.priceList.id);
            anyPriceListCreated = true;
        }
    };
    
    message.setProperty("PricelistIds",priceListidList);
    message.setProperty("anyPriceListCreated",anyPriceListCreated);
    return message;
}
