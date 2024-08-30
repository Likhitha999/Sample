import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.xml.XmlUtil;

def Message processData(Message message) {
    def body = message.getBody(java.lang.String) as String;
    def query = new XmlSlurper().parseText(body);
    List<String> list = new ArrayList<String>();
    def insertOrChangeExists = false;
    query.BusinessPartnerRelationshipSUITEReplicateRequestMessage.each{
        if(it.BusinessPartnerRelationship.@actionCode.text() == "03")
        {
            list.add(it.BusinessPartnerRelationship.RelationshipBusinessPartnerInternalID.text());
            it.replaceNode {};
        }else{
            insertOrChangeExists = true
        }
    }
    message.setProperty("Insert_or_Change_Exists",insertOrChangeExists.toString());
    message.setProperty("Deleted_relationships", list);
    message.setProperty("Delete_relationship_Count", list.size());
    def valid_data = XmlUtil.serialize(query);
    if(valid_data.contains("?xml")){
        int i = valid_data.indexOf('>');
        valid_data = valid_data.substring(i+1);
    }
    message.setBody(valid_data);
    return message;
}