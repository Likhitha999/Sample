import com.sap.it.api.mapping.*;
import com.sap.it.api.mapping.MappingContext;
import com.sap.it.api.ITApiFactory;
import com.sap.it.api.ITApi;
import com.sap.it.api.mapping.ValueMappingApi;
import groovy.util.XmlParser;

//* Get Text Types for the Service Order Header
def String GetServiceOrderHeaderNotes( String texttype, MappingContext context ){

    def noteType         = context.getProperty('ServiceOrderHNoteType') ?: 'S002'       
    def resolutionType   = context.getProperty('ServiceOrderHProbDescType') ?: 'S001'   

if ( texttype == 'Remarks'){                              

	result = noteType 
} else {                                               
    result = resolutionType 
}
    return result

} 

//* Get the language for Service Order Header and Item
def String GetServiceOrderLanguage( String language, MappingContext context ) {
    def noteLanguage     = context.getProperty('ServiceOrderNoteLang') ?: 'EN'
	result = noteLanguage
}

//* Get the Text Type for the Service Order Item
def String GetServiceOrderItemNotes( String texttype, MappingContext context ){
    def itemNoteType         = context.getProperty('ServiceOrderINoteType') ?: '0002'
	result = itemNoteType
}	
