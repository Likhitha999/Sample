import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.xml.XmlUtil;
import groovy.util.slurpersupport.NodeChild;
import com.sap.it.api.ITApiFactory;
import com.sap.it.api.mapping.ValueMappingApi;

def Message processData(Message message){
    def body = message.getBody(java.lang.String);
    def query = new XmlSlurper().parseText(body);
    def valueMapApi = ITApiFactory.getApi(ValueMappingApi.class, null);
    def employeeExist = false;
    def map = message.getProperties();
    def ignoreCategory = map.get("IgnoreCategory");
    query.BusinessPartnerSUITEReplicateRequestMessage.each{
        if(ignoreCategory.toLowerCase().equals("false")){
            if(it.BusinessPartner.CategoryCode.text()=="1") {
                def isNodeEmployee = false;
                it.BusinessPartner.Role.each {
                    def fromValueMap = valueMapApi.getMappedValue('S4OPRole', 'RoleCode', it.RoleCode.text(), 'FSMPartner', 'PartnerType')
                    if(fromValueMap != null){
                        if(fromValueMap.toLowerCase().equals("employee")){
                            isNodeEmployee = true;
                            employeeExist = true;
                        }
                    }
                }
                if(!isNodeEmployee)
                    it.replaceNode {};
            }
            else
                it.replaceNode {};
        }
    
        if(ignoreCategory.toLowerCase().equals("true")){
            def isNodeEmployee = false;
            it.BusinessPartner.Role.each {
                def fromValueMap = valueMapApi.getMappedValue('S4OPRole', 'RoleCode', it.RoleCode.text(), 'FSMPartner', 'PartnerType');
                if (fromValueMap != null){
                    if(fromValueMap.toLowerCase().equals("employee")){
                        isNodeEmployee = true;
                        employeeExist = true;
                    }
                }    
            }
            if(!isNodeEmployee)
                it.replaceNode {};
        }
    }
    if(employeeExist)
        message.setProperty("EmployeeExists","exists");
    def valid_data = XmlUtil.serialize(query);
    message.setBody(valid_data);
    return message;
}