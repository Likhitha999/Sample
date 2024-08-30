import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

def Message processData(Message message) {
    def map = message.getProperties();
    def mssg = "[{"
    def contactList = map.get("Deleted_relationships");
    def i;
    for(i=0; i<contactList.size()-1; i++) {
        mssg = """${mssg}"externalId":"${contactList[i]}","object":null},{""";
    }
    mssg = """${mssg}"externalId":"${contactList[i]}","object":null}]""";

    message.setBody(JsonOutput.toJson(new JsonSlurper().parseText(mssg)));
    return message;
}