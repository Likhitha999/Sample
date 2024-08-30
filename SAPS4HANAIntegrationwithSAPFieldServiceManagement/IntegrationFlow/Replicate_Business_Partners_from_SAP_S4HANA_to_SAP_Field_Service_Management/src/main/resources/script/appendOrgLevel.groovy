import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.xml.XmlUtil
import groovy.util.*

def Message processData(Message message) {
            def BPPayload = message.getProperty('BPPayload');
        def bpBody = new XmlSlurper().parseText(BPPayload);
        bpBody.BP.each { row ->
        def orgLevelString = '<orgLevelIds></orgLevelIds>'
        def newNode = new XmlSlurper().parseText(orgLevelString)
        row.appendNode(newNode)
        }
        message.setBody(XmlUtil.serialize(bpBody))
    return message
}
