import com.sap.gateway.ip.core.customdev.util.Message
import java.util.HashMap;
import groovy.json.JsonSlurper;
import groovy.json.JsonOutput;

def Message processData(Message message) { 
 
    def body = message.getBody(java.lang.String) as String
    def errorMsgs = ''
    List iFlowList = []

    try {
        def xmlBody = new XmlSlurper().parseText(body)  
        if (xmlBody != null) {
            def content = xmlBody.entry.each { entry ->
                iFlowList.add(entry.properties)
            };
        };
    } catch(Exception ex0) { /* ignored */ }

    if (iFlowList == []) {
        throw new Exception("Could not read any integration flow properties with URL " + message.getProperty("SAPCloudIntegrationHost")
                            + "/api/v1/IntegrationPackages('" + message.getProperty("PackageID") + "')/IntegrationDesigntimeArtifacts" )
    }
    
    message.setProperty("iFlowList", iFlowList)
    message.setProperty("listSize",iFlowList.size())
    message.setProperty("loopIndex",0)
    return message
}