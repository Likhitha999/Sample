import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def Message processData(Message message) {
  def body = message.getBody(java.lang.String) as String;
  body = body.replace("{\"@nil\":\"true\"}","null");
  def jsonParser = new JsonSlurper();
  def jsonObject = jsonParser.parseText(body); 
  def String newJson = "[ ]";
  def newJsonObject = jsonParser.parseText(newJson);
  jsonObject.MultiAddress.Address.each { Multiadd->
    Multiadd.each{ Singleadd->
        newJsonObject.add(Singleadd);
    }
  }
  message.setBody(JsonOutput.toJson(newJsonObject));
  message.setProperty("RequestPayload", JsonOutput.toJson(newJsonObject));
  return message;
}
