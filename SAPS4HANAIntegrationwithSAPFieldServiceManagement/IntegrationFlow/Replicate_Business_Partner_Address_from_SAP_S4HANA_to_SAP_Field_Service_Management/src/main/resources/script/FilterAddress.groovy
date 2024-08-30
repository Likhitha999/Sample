import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.xml.XmlUtil;
def Message processData(Message message){
 def body = message.getBody(java.lang.String);
 def query = new XmlSlurper().parseText(body);
 boolean flag=false
 query.BusinessPartnerSUITEReplicateRequestMessage.each{
     if(it.BusinessPartner.AddressInformation.text()=="")
         it.replaceNode {};
     else
       flag=true
  } 
 if(flag==false)
 message.setProperty("flag","false")
 def valid_data = XmlUtil.serialize(query);
 message.setBody(valid_data);
 return message;
}