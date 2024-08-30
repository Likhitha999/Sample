import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.xml.XmlUtil
import groovy.util.*

def Message processData(Message message) {
    def body = message.getBody(java.lang.String)
    def orgBody = new XmlSlurper().parseText(body)
    def orgMap = [:]
    def productPayload = message.getProperty('productPayload')  
    def prodBody = new XmlSlurper().parseText(productPayload)
    def salesOrgsData = message.getProperty('salesOrgs') 
    def salesOrgs = new JsonSlurper().parseText(salesOrgsData)
    def plantData =  message.getProperty('plants')
    def plants = new JsonSlurper().parseText(plantData)
    List OrgList = new ArrayList();
   
    // Build a map variable to store the mapping between SD Sales Org in the 
    // payload with CRM Sales org 
    if(orgBody.SrvcMgmtFSMOrgUnitMapping_Type !='') {
        orgBody.SrvcMgmtFSMOrgUnitMapping_Type.each { it ->

            if (orgMap.get(it.SalesOrganization.text()) == null) {

                orgMap.put(it.SalesOrganization.text(), [it.SrvcMgmtOrgUnitID.text()])
            } else {
                def existingOrgs = orgMap.get(it.SalesOrganization.text());
                existingOrgs.add(it.SrvcMgmtOrgUnitID.text());
                orgMap.put(it.SalesOrganization.text(), existingOrgs.unique())
        }
    }
   
    
    // In case of plant assigned to a Product we check if the plant is not assigned
    // to a sales org then we can get the relevant orgs from S4 based on plant
    if (plants){   
        plants.each { this_plants ->
          def plnt = plants[this_plants.key]
          OrgList = new ArrayList();

          for (int i = 0; i < plnt.size(); i++) {              
            if(orgBody.SrvcMgmtFSMOrgUnitMapping_Type !='') {
              orgBody.SrvcMgmtFSMOrgUnitMapping_Type.each { this_orgBody -> 
              if("${plnt[i]}"==this_orgBody.Plant.text()){
                  OrgList.add(this_orgBody.SalesOrganization.text())
                } 
              }
              salesOrgs.put(this_plants.key, OrgList.unique())
            }
          }
        }  
    }
    
    // Assign the org levels to the payload 
    prodBody.PROD.each { this_prod ->
        
         def orgLevels = salesOrgs[this_prod.externalId]
        
        if (orgLevels != null) {
            orgLevels.each { this_orgLevel ->
                def salesOrg = orgMap.get(this_orgLevel)
                for (int i = 0; i < salesOrg?.size(); i++) {
                    def orgLevelString = "<orgLevelIds><externalId>${salesOrg[i].replace("O ", "")}</externalId></orgLevelIds>";

                    def newNode = new XmlSlurper().parseText(orgLevelString)
                    this_prod.appendNode(newNode)

                }
            }
        }
    }
    message.setBody(XmlUtil.serialize(prodBody))
    
    } else {
        message.setBody(XmlUtil.serialize(prodBody))        
    }
    return message
   }
