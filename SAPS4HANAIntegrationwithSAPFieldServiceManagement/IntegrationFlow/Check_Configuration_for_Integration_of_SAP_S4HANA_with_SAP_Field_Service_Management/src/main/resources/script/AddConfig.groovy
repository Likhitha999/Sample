import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) { 

    def body = message.getBody(java.lang.String) as String
message.setProperty("body", body)
    String iFlowId = message.getProperty("iFlowId")
    String iFlowName = message.getProperty("iFlowName")
    Map iFlowConfig = [:]

    try{
        def xmlConfig = new XmlSlurper().parseText(body)  
        if (xmlConfig != null) {
            xmlConfig.entry.each { configEntry ->
                String description = configEntry.content.properties?.ParameterKey
                String value = configEntry.content.properties?.ParameterValue
                iFlowConfig.put(description, value ?: '')
            }
        }
    } catch(Exception ex0) { /* ignored */ }

    iFlowConfig.put('__NAME__', iFlowName ?: '') // store iflow name
    Map packageConfig = message.getProperty("packageConfig")
    if (packageConfig == null) { 
        packageConfig = [:]
    }
    packageConfig.put(iFlowId, iFlowConfig);
    message.setProperty("packageConfig", packageConfig);

    return message
}