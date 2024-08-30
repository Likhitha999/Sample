import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.xml.XmlUtil
import groovy.util.*


def Message processData(Message message) {
    def body = message.getBody(java.lang.String)
    def orgBody = new XmlSlurper().parseText(body)
    def orgMap = [:] // Use a map literal instead of HashMap constructor
    if(orgBody.SrvcMgmtFSMOrgUnitMapping_Type !='')
    {
        orgBody.SrvcMgmtFSMOrgUnitMapping_Type.each { it ->
    
            if(orgMap.get(it.SalesOrganization.text()) == null)
            {
                
                orgMap.put(it.SalesOrganization.text(), [it.SrvcMgmtOrgUnitID.text()])
            }
            else {
                def existingOrgs = orgMap.get(it.SalesOrganization.text());
                existingOrgs.add(it.SrvcMgmtOrgUnitID.text());
                orgMap.put(it.SalesOrganization.text(), existingOrgs.unique())
    
            }
        }

    def BPPayload = message.getProperty('BPPayload');
    def salesOrgsData = message.getProperty('salesOrgs');
    def bpBody = new XmlSlurper().parseText(BPPayload);
    def salesOrgs = new JsonSlurper().parseText(salesOrgsData);
    
//  Add OrgLevels to the existing payload    
    bpBody.BP.each { row ->
        def orgLevels = salesOrgs[row.externalId]
        if (orgLevels != null) {
            orgLevels.each { orgLevel ->
                def salesOrg = orgMap.get(orgLevel)
                if(salesOrg){
                    for (int i = 0; i < salesOrg.size(); i++) {
                       def orgLevelString = "<orgLevelIds><externalId>${salesOrg[i].replace("O ", "")}</externalId></orgLevelIds>";
                       
                       def newNode = new XmlSlurper().parseText(orgLevelString)
                        row.appendNode(newNode)
                    }              
                }
            }
         
        }
    }
    message.setBody(XmlUtil.serialize(bpBody))
    }
    else {
        def BPPayload = message.getProperty('BPPayload');
        def bpBody = new XmlSlurper().parseText(BPPayload);
        bpBody.BP.each { row ->
        def orgLevelString = '<orgLevelIds></orgLevelIds>'
        def newNode = new XmlSlurper().parseText(orgLevelString)
        row.appendNode(newNode)
        }
        message.setBody(XmlUtil.serialize(bpBody))
        
    }
    return message
}