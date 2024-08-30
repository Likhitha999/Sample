import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.xml.XmlUtil;
import groovy.util.slurpersupport.NodeChild;
import com.sap.it.api.ITApiFactory;
import com.sap.it.api.mapping.ValueMappingApi;

def Message processData(Message message){
    def body = message.getBody(java.lang.String);
    def query = new XmlSlurper().parseText(body);
    def valueMapApi = ITApiFactory.getApi(ValueMappingApi.class, null);
    def organizationExist = false;
    def map = message.getProperties();
    def ignoreCategory = map.get("IgnoreCategory");
    query.BusinessPartnerSUITEReplicateRequestMessage.each{
        if(ignoreCategory.toLowerCase().equals("false")){
            if(it.BusinessPartner.CategoryCode.text()=="2") {
                def isNodeOrganization = false;
                it.BusinessPartner.Role.each {
                    def fromValueMap = valueMapApi.getMappedValue('S4OPRole', 'RoleCode', it.RoleCode.text(), 'FSMPartner', 'PartnerType');
                    if (fromValueMap != null){
                        if(fromValueMap.toLowerCase().equals("bp")){
                            isNodeOrganization = true;
                            organizationExist = true;
                        }
                    }    
                }
                if(!isNodeOrganization)
                    it.replaceNode {};
            }
            else
                it.replaceNode {};
        }
        
        if(ignoreCategory.toLowerCase().equals("true")){
            def isNodeOrganization = false;
            it.BusinessPartner.Role.each {
                def fromValueMap = valueMapApi.getMappedValue('S4OPRole', 'RoleCode', it.RoleCode.text(), 'FSMPartner', 'PartnerType');
                if (fromValueMap != null){
                    if(fromValueMap.toLowerCase().equals("bp")){
                        isNodeOrganization = true;
                        organizationExist = true;
                    }
                }    
            }
            if(!isNodeOrganization)
                it.replaceNode {};
        }
    }
    
  if(organizationExist)
    message.setProperty("OrganizationExists","exists");
  def valid_data = XmlUtil.serialize(query);
  message.setBody(valid_data);
  return message;
}