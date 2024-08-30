import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.xml.XmlUtil
import groovy.util.*

def Message processData(Message message) {
		def productPayload = message.getProperty('productPayload')
		def prodBody = new XmlSlurper().parseText(productPayload)
        message.setBody(XmlUtil.serialize(prodBody))
    return message
}