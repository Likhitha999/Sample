import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import groovy.json.*;
def Message processData(Message message) {
    
    //Get Properties
    def savedPayload = message.getProperty('OriginalPayload')
	def InputPayload = new XmlParser().parseText(savedPayload)

   //Define arrays
    salesOrgs = [:]
    plants = [:]
    
    List relvOrgs    = new ArrayList();
    List relvOrgList = new ArrayList();
    List sdSalesOrgs = new ArrayList();
    
    List plantInfo = new ArrayList();
    List plantList = new ArrayList();
    List plantTmp = new ArrayList();
	
	//Check existancy of ProductMDMReplicateRequestMessage
    if(InputPayload.ProductMDMReplicateRequestMessage.isEmpty()== false ){
	
	InputPayload.ProductMDMReplicateRequestMessage.Product.each{ this_prod ->

        this_prod.SalesSpecification.each{ this_sorg ->
            if(this_sorg.SalesOrganizationID?.text()){
                relvOrgs.add(this_sorg.SalesOrganizationID?.text())
                sdSalesOrgs.add(this_sorg.SalesOrganizationID.text())
            }
        }
        
        if(sdSalesOrgs){
            salesOrgs.put(this_prod.ProductInternalID.text().replaceFirst("^0*", ""), sdSalesOrgs.unique());
            sdSalesOrgs = new ArrayList();
        }
        else {
            this_prod.Plant.each{ this_plant ->
                plantInfo.add(this_plant.PlantID.text())
                plantTmp.add(this_plant.PlantID.text())
            }
			if(plantTmp){
				plants.put(this_prod.ProductInternalID.text().replaceFirst("^0*", ""), plantTmp.unique());
				plantTmp = new ArrayList();
			}	
        }
    }
	
	int i;
	
	def ODataQuery_Plants = ""
	def ODataQuery_SOrg = ""
    relvOrgList = relvOrgs.unique();
    plantList = plantInfo.unique();
    
   if(relvOrgList.isEmpty()== false ){
        for(i=0; i<relvOrgList.size(); i++) {
            if(i == relvOrgList.size()-1){
                ODataQuery_SOrg = """${ODataQuery_SOrg}SalesOrganization eq '${relvOrgList[i]}'""";
            }else{
                ODataQuery_SOrg = """${ODataQuery_SOrg}SalesOrganization eq '${relvOrgList[i]}' \n""";
            }
        }
       
    } 
    
	if(plantList.isEmpty()== false ){
        for(i=0; i<plantList.size(); i++) {
            if(i == plantList.size()-1){
                ODataQuery_Plants = """${ODataQuery_Plants}Plant eq '${plantList[i]}'""";
            }else{
                ODataQuery_Plants = """${ODataQuery_Plants}Plant eq '${plantList[i]}' \n""";	
            }
        }
       
    } 
	
    
    message.setProperty("salesOrgs", JsonOutput.toJson(salesOrgs));
    message.setProperty("plants", JsonOutput.toJson(plants));
	message.setProperty("ODataQuery_SOrg", ODataQuery_SOrg.toString());
	message.setProperty("ODataQuery_Plants", ODataQuery_Plants.toString());
	
}
 	
return message;
}
