import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.*;

def Message processData(Message message) {
    def savedPayload = message.getProperty('OriginalPayload')
    def InputPayload = new XmlParser().parseText(savedPayload)
    salesOrgs = [:]
    List relvOrgs    = new ArrayList();
    List relvOrgList = new ArrayList();
    List sdSalesOrgs = new ArrayList();
    
//  Get the Sales Orgs from the SOAP Payload  
    InputPayload.BusinessPartnerSUITEReplicateRequestMessage.BusinessPartner.each{ bp ->        
        bp.Customer.SalesArrangement.each{ 
     
           if(it.SalesOrganisationID.text()){
                relvOrgs.add(it.SalesOrganisationID.text())
                sdSalesOrgs.add(it.SalesOrganisationID.text())
            } 
        } 
        if(sdSalesOrgs){
            salesOrgs.put(bp.InternalID.text(), sdSalesOrgs.unique())
       		sdSalesOrgs = new ArrayList();
        }       
   }
   
// Form the Filter Query parameter
   int i;
   def ODataQuery = ""
   relvOrgList = relvOrgs.unique();
   
   for(i=0; i<relvOrgList.size(); i++) {
       if(i == relvOrgList.size()-1){
           ODataQuery = """${ODataQuery}SalesOrganization eq '${relvOrgList[i]}'""";
       }else{
           ODataQuery = """${ODataQuery}SalesOrganization eq '${relvOrgList[i]}' \n"""; 
       }
    } 
    
    message.setProperty("Query", ODataQuery);
    message.setProperty("salesOrgs", JsonOutput.toJson(salesOrgs));
    message.setBody(ODataQuery.toString());
   
    return message;
}