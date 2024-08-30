import com.sap.gateway.ip.core.customdev.util.Message
import groovy.util.XmlParser
import groovy.xml.XmlUtil
import groovy.json.JsonOutput

// determine main partner / person responsible from the original payload
// and modify the FSM payload to keep only the main responsible 
def Message processData(Message message) {
    def body = message.getBody(String.class)
    def root = new XmlParser().parseText(body)
    def bCustMgmtPartnerIsMainPartnerPresent = (message.getProperty("CustMgmtPartnerIsMainPartnerPresent") == 'true')
    def main_responsibles = []
    def responsible

    if (bCustMgmtPartnerIsMainPartnerPresent) {
        def original_body = message.getProperty("OriginalPayload")
        def original_root = new XmlParser().parseText(original_body)
        // find main responsibles and add them to list main_responsibles
        // there should only be one 
        original_root.'**'.each{ node ->
            if (node.name() == 'CustMgmtPartnerIsMainPartner' && node.value()[0] == 'true'){
                def node_parent = node.parent() // is PersonResponsible
                println(node_parent.name())
                try {
                    responsible = node_parent.'**'.find { it.name() == 'PersonResponsible' }.value()[0]
                } catch(Exception ex0) { /* not found */ }
                if (responsible != null && responsible.value() != null) {
                    main_responsibles.add(responsible.value()[0])
                }
            }
        }
        message.setProperty("MainResponsibles", main_responsibles.toString())
    }

    // the FSM payload could still contain multiple responsibles from the original
    if (main_responsibles.size() > 0) {
        // modify FSM payload and keep only the main responsible
        def keep = 0
        root.'**'.each{ node ->
            if (node.name() == 'responsibles'){
                try {
                    node_external_id = node.value()[0]
                    external_id = node_external_id.value()[0].trim()
                    println(main_responsibles[0])
                    if (!(external_id == main_responsibles[0]) || keep > 0) {
                        node.replaceNode{}
                    } else {
                        keep = keep + 1 // make sure to keep only one
                    }
                } catch(Exception ex2) { /* ignore */ }
            }
        }
    } else {
        //  modify FSM payload and keep only first responsible
        def idx = 0
        root.'**'.each{ node ->
            if (node.name() == 'responsibles'){
                if (idx > 0) {
                    node.replaceNode{}
                }
                idx = idx + 1
            }
        }
    }

    // return modified payload
    String outxml = groovy.xml.XmlUtil.serialize( root )
    message.setBody(outxml)
    return message
}