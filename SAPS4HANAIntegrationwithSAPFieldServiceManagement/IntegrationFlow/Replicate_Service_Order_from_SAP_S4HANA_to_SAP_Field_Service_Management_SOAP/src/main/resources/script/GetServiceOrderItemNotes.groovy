import com.sap.it.api.mapping.*;
import com.sap.it.api.mapping.MappingContext;
import com.sap.it.api.ITApiFactory;
import com.sap.it.api.ITApi;
import com.sap.it.api.mapping.ValueMappingApi;
import groovy.util.XmlSlurper

def String GetServiceOrderItemNotes(String ItemNum, MappingContext context ){
    def noteType      = context.getProperty("ServiceOrderINoteType") ?: '0002'
    def noteLanguage  = context.getProperty("ServiceOrderNoteLang") ?: 'en'
    def sourcePayload = context.getProperty('OriginalPayload')
    def result

    if ( sourcePayload != null && sourcePayload != "" ) {
        def sourcePayloadXml = new XmlSlurper().parseText(sourcePayload)

        def item = sourcePayloadXml.ServiceOrder.Item.find{ node ->
            node.ServiceOrderItem == ItemNum
        }
     
        if(item.TextCollection) {
            item.TextCollection.Text.each {
                if (it.TypeCode == noteType && it.ContentText[0].@languageCode.toString().equalsIgnoreCase(noteLanguage)) {
                    result = it.ContentText
                }
            }
        }
    }
    return result
}