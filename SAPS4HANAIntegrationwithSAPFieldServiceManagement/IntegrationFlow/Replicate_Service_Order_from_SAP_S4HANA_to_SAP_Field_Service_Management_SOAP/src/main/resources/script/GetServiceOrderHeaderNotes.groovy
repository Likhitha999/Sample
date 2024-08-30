import com.sap.it.api.mapping.*;
import com.sap.it.api.mapping.MappingContext;
import com.sap.it.api.ITApiFactory;
import com.sap.it.api.ITApi;
import com.sap.it.api.mapping.ValueMappingApi;
import groovy.util.XmlParser;

def String GetServiceOrderHeaderNotes( String texttype, MappingContext context ){
    def noteType       = context.getProperty('ServiceOrderHNoteType') ?: 'S002'       
    def resolutionType = context.getProperty('ServiceOrderHProbDescType') ?: 'S001'   
    def noteLanguage   = context.getProperty('ServiceOrderNoteLang') ?: 'en'
    def sourcePayload  = context.getProperty('OriginalPayload')
    def result
    
    if ( sourcePayload != null && sourcePayload != "" ) {
        def sourcePayloadXml = new XmlParser().parseText(sourcePayload)
    
        if ( texttype == 'Remarks' ){                              
            sourcePayloadXml.ServiceOrder.TextCollection.Text.find{ 
                if( it.TypeCode.text() == noteType ){
                    if (it.ContentText[0].@languageCode.equalsIgnoreCase(noteLanguage)) {
                            result = it.ContentText.text()
                    }
                    if(result != null){
                        return
                    }
                }
            }
        } else {                                               
                sourcePayloadXml.ServiceOrder.TextCollection.Text.find{ 
                if( it.TypeCode.text() == resolutionType ){
                    if (it.ContentText[0].@languageCode.equalsIgnoreCase(noteLanguage)) {
                        result = it.ContentText.text()
                    }
                    if(result != null){
                        return
                    }
                }
            }
        }
    }
    return result
} 