import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.util.HashMap;

def Message processData(Message message) {
  def body = message.getBody(java.lang.String) as String;
  def jsonParser = new JsonSlurper();
  def jsonObject = jsonParser.parseText(body);
  Set <String> set = new HashSet <String> ();

  jsonObject.each {
    set.add(JsonOutput.toJson(it));
  }

  String newJson = "[ ]";
  def newJsonObject = jsonParser.parseText(newJson);

  for (String s: set) {
    jsonObject = jsonParser.parseText(s);
    newJsonObject.add(jsonObject);
  }

  message.setBody(JsonOutput.toJson(newJsonObject));
  message.setProperty("RequestPayload", JsonOutput.toJson(newJsonObject));
  
  return message;
}
