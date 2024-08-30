import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.xml.XmlUtil;
import groovy.util.slurpersupport.NodeChild;
def Message processData(Message message){
 def body = message.getBody(java.lang.String);
 def query = new XmlSlurper().parseText(body);
 query.BusinessPartnerRelationshipSUITEReplicateRequestMessage.each{
     if(it.BusinessPartnerRelationship.RoleCode.text()=="BUR001") 
            message.setProperty("ContactRelationship","exists");
     else
       it.replaceNode {};
  } 
 def valid_data = XmlUtil.serialize(query);
 if(valid_data.contains("?xml")){
	int i = valid_data.indexOf('>');
	valid_data = valid_data.substring(i+1);
  }
 message.setBody(valid_data);
 return message;
}