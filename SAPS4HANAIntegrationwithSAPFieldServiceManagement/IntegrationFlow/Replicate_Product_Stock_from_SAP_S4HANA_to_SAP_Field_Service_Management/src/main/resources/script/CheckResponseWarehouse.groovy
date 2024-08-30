import groovy.json.JsonSlurper
import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.xml.XmlUtil

def Message processData(Message message) {
    def body = message.getBody(java.lang.String) as String;
    def jsonSlurper = new JsonSlurper();
    def content = jsonSlurper.parseText(body);
    def map = message.getProperties()
    def sourcemsg = map.get("sourceMessage")
    
    
    content.each{
        if (it.status >= 400 && it.status < 600) {
            //handle locked messages -- START
            if (it.status != 409){
                message.setProperty("ErrorType", "FSMError");
            }else{
                if(it.externalId){                    
                    message.setProperty("lockedwarehouse",'Y')
                    message.setProperty("lockedmessages",getWarehousemsg(it.externalId,sourcemsg))
                }
            }
            //handle locked messages -- END
            message.setProperty('http.StatusCode', it.status)
            message.setProperty('http.ResponseBody', body)
            if (it.status == 500) {
                message.setProperty('http.StatusText', it.message);
            } else {
                message.setProperty('http.StatusText', it.ex.message);
            }
        }
    }    
    
    return message;
}

def getWarehousemsg(externalId,sourceWH) {
    def root = new XmlParser().parseText("<Warehouse></Warehouse>")
    def parsesrcmsg = new XmlParser().parseText(sourceWH);
    parsesrcmsg.data.each { wh ->
        if (wh.externalId.text() == externalId)
            root.append(wh);
    }
    return XmlUtil.serialize(root)

}