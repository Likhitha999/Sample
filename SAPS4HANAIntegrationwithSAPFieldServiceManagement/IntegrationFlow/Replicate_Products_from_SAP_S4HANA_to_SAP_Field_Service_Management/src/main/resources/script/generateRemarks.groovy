import com.sap.it.api.mapping.*;

def String generateRemarks(String input,String language,String RemarkType){
	def query = new XmlSlurper().parseText(input);
	String output="";
	query.Text.each{
        if(it.LanguageCode.text() == language && it.TypeCode.text().toLowerCase() == RemarkType){
            if(it.SAPScriptLine.size()>0){
                int i;
                for(i=0;i<it.SAPScriptLine.size();i++){
                    if(!(it.SAPScriptLine[i].SAPScriptLineText.equals(""))){
                        output = output +it.SAPScriptLine[i].SAPScriptLineText+"\n"
                    }
                }
                if(!(it.SAPScriptLine[i].SAPScriptLineText.equals(""))){
                    output = output +it.SAPScriptLine[i].SAPScriptLineText
                }
            }
        }
    }
    if(!(output == "")){
        return output; 
    }
}