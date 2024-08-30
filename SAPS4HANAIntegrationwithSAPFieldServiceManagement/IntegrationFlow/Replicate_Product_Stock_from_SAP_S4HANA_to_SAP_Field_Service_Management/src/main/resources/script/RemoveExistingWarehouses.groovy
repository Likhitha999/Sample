import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.xml.XmlUtil;
def Message processData(Message message) {
    //Body 
    def body = message.getBody(java.lang.String) as String;
    def jsonParser = new JsonSlurper();
    def jsonObject = jsonParser.parseText(body);
       
    WarehouseMappedPayload= message.getProperty("WarehouseMapped_Original");
    def query = new XmlSlurper().parseText(WarehouseMappedPayload);
    
    def wh=""
    jsonObject.data.each{
        wh = it.wh.externalId
        query.children().findAll {it.externalId == wh}.replaceNode {}
    }
    
    message.setBody(XmlUtil.serialize(query));
    return message;
}