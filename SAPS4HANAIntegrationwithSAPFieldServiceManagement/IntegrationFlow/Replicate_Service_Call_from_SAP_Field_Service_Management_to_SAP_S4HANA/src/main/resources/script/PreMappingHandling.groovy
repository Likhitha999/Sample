import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import com.sap.it.api.mapping.ValueMappingApi;
import com.sap.it.api.ITApiFactory;
import groovy.xml.XmlUtil

// process message
def Message processData(Message message) {
    def body = message.getBody(String.class)
    def jsonSlurper = new JsonSlurper()
    def object = jsonSlurper.parseText(body)
    def textNode = "";
    def service = ITApiFactory.getApi(ValueMappingApi.class, null);
    
    def textTypeRemarks = message.getProperty('ServiceOrderNoteType') ?: 'S002'
    def textTypeResolution = message.getProperty('ServiceOrderProbDescType') ?: 'S001'       
    def language = message.getProperty('ServiceOrderNoteLang') ?: 'EN'
    
    message.setProperty("FSMServiceCall", object.data.code);
    message.setProperty("ServiceCallId", object.data.id);
    
    if (object.data.type != null) {
        ServiceOrderType = service.getMappedValue("ServiceCallFSM", "Type", object.data.type, "ServiceOrderS4HANA", "TransactionType") ?: null;
    } else {
        ServiceOrderType = service.getMappedValue("ServiceCallFSM", "Type", "<null>", "ServiceOrderS4HANA", "TransactionType") ?: null;
    }
    
    if (ServiceOrderType != null) {
        message.setProperty("ServiceOrderType", ServiceOrderType);
    } else {
        throw new Exception("The integration flow message won’t be processed further. No ServiceOrderType could be determined by ValueMapping");
    }

    if (object.data.equipments != null && object.data.equipments[0] != null){
        def equipment_node = object.data?.equipments[0]
        def ref_object = equipment_node?.externalId
        def ref_object_category = equipment_node?.objectCategory
        if (ref_object) {
            if (ref_object_category == null || ref_object_category == 'EQ') {
                message.setProperty("Equipment", ref_object)
                message.setProperty("FunctionalLocation", "")
            } else if (ref_object_category == 'FLOC') {
                message.setProperty("Equipment", "")
                message.setProperty("FunctionalLocation", ref_object)
            }
        }
    }

    if (object.data.earliestStartDateTime != null){
        message.setProperty("RequestedStartDate", object.data.earliestStartDateTime);
    }

    if (object.data.dueDateTime != null){
        message.setProperty("RequestedEndDate", object.data.dueDateTime);
    }
    
    if (object.data.subject != null) {
        message.setProperty("Description", groovy.xml.XmlUtil.escapeXml(object.data.subject.take(40)));
    }
    
    if (object.data.businessPartner && object.data.businessPartner.externalId != null) {
        message.setProperty("SoldToParty", object.data.businessPartner.externalId);
    }
    
    if (object.data.responsibles != [] && object.data.responsibles != null) {
        message.setProperty("PersonResponsible", object.data.responsibles[0].externalId);
    }
    
    if (object.data.contact && object.data.contact.externalId != null) {
        message.setProperty("ContactPerson", object.data.contact.externalId);
    }
    
    if (object.data.priority != null) {
        def prioMapped = service.getMappedValue("ServiceCall", "Priority", object.data.priority, "ServiceOrder", "ServiceDocumentPriority") ?: "";
        message.setProperty("Priority", prioMapped);
    }
    
    if (object.data.remarks != null || object.data.resolution != null) {
        remarks = object.data.remarks ?: "";
        resolution = object.data.resolution ?: "";
    
        //* Build Payload for Text Node 
        if (remarks != "" && textTypeRemarks != null && language != null) {
            textNode = textNode + '''<A_ServiceOrderTextType>'''
            textNode = textNode + '''<ServiceOrder></ServiceOrder>'''
            textNode = textNode + '''<Language>'''     + language  + '''</Language>'''
            textNode = textNode + '''<LongTextID>'''   + textTypeRemarks + '''</LongTextID>'''
            textNode = textNode + '''<LongText>'''     + groovy.xml.XmlUtil.escapeXml(remarks) + '''</LongText>'''
            textNode = textNode + '''</A_ServiceOrderTextType>'''        
        }
        
        if (resolution != "" && textTypeResolution != null && language != null) {
            textNode = textNode + '''<A_ServiceOrderTextType>'''
            textNode = textNode + '''<ServiceOrder></ServiceOrder>'''
            textNode = textNode + '''<Language>'''     + language  + '''</Language>'''
            textNode = textNode + '''<LongTextID>'''   + textTypeResolution + '''</LongTextID>'''
            textNode = textNode + '''<LongText>'''     + groovy.xml.XmlUtil.escapeXml(resolution) + '''</LongText>'''
            textNode = textNode + '''</A_ServiceOrderTextType>'''        
        }
    
        message.setProperty("TextNode", textNode);
    }
    
    return message;
}
