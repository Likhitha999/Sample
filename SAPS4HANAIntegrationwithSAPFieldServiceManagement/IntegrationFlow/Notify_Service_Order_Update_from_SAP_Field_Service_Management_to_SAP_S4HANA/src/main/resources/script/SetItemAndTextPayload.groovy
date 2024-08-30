import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;

    
def Message processData(Message message) {

    def parser = new XmlSlurper();
    def isItemRemarkUpdated    = message.getProperty("isItemRemarkUpdated");
    def UpdateItemPayloadRaw = message.getProperty("itemMappingResultPayload");    
    def isItemRemarkDeleted = false;
    def Language, ServiceOrder;
    def UpdateItemRemarkPayload, LongTextRemarkType, LongTextRemark;
    
    //* Fill Remark 
    if (isItemRemarkUpdated == true) {
        
        UpdateItemRemarkPayload = parser.parseText(UpdateItemPayloadRaw);
        ServiceOrder            = UpdateItemRemarkPayload.A_ServiceOrderItemType.to_Text.A_ServiceOrderItemTextType[0].ServiceOrder 
        Language                = UpdateItemRemarkPayload.A_ServiceOrderItemType.to_Text.A_ServiceOrderItemTextType[0].Language
        Language                = Language.toString()
        LongTextRemarkType  = UpdateItemRemarkPayload.A_ServiceOrderItemType.to_Text.A_ServiceOrderItemTextType[0].LongTextID
        LongTextRemark = UpdateItemRemarkPayload?.A_ServiceOrderItemType?.to_Text?.A_ServiceOrderItemTextType[0]?.LongText?: ""
        if (LongTextRemark == "") {
            isItemRemarkDeleted = true
        } else { 
            isItemRemarkDeleted = false
        }
        
        UpdateItemRemarkPayload = UpdateItemRemarkPayload.A_ServiceOrderItemType.to_Text
        UpdateItemRemarkPayload.A_ServiceOrderTextType.findAll { it.LongTextID != LongTextRemarkType }.replaceNode {}
        UpdateItemRemarkPayload.replaceNode { 'A_ServiceOrderItemText'(it.children()) }

        UpdateItemRemarkPayload = groovy.xml.XmlUtil.serialize( UpdateItemRemarkPayload )
        LongTextRemarkType = LongTextRemarkType.toString()
        message.setProperty("TextLangu", Language)
        message.setProperty("ItemLongTextRemarkType", LongTextRemarkType)
        message.setProperty("updateItemRemarkPayload", UpdateItemRemarkPayload)
        message.setProperty("isItemRemarkDeleted", isItemRemarkDeleted)
    }
    
    return message;
}