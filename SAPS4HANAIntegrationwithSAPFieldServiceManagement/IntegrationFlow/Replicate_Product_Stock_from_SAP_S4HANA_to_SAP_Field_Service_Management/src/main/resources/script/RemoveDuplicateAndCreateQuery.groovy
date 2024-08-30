import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.xml.*

def Message processData(Message message) {
    int i = 0
    List duplicates = [];
    def Set <String> set = new HashSet <String> ();

// Add a <root> Node tag so that later duplicates can be deleted
    String body = '''<root></root>''' 
    String fragment = message.getBody(java.lang.String)
    def root     = new XmlParser( ).parseText( body )
    def fragmentNode = new XmlParser( ).parseText( fragment )

    root.append(fragmentNode)

// Delete Duplicate nodes
    root.each { this_root ->
        this_root.data.each { this_wrh ->  
        // get the duplicate nodes index      
            if(set.contains(this_wrh.externalId.text())){
                duplicates.add(i)  
            }            
            i = i + 1
            set.add(this_wrh.externalId.text()); 
        }

        // delete duplicate node using the index value
        if(duplicates){
            duplicates.eachWithIndex { this_dupl, index ->
                if(index == 0){
                this_root.data[this_dupl].replaceNode{}  
                } else {
                this_root.data[this_dupl - index].replaceNode{}  
                }
            }            
        }        
    }

// Prepare the query for get HTTP call to FSM to check if warehouse already exists
    String msg = """{"query":"SELECT wh.externalId from Warehouse wh WHERE wh.externalId IN (""";
    i = 0;
    for(i=0; i<set.size()-1; i++) {
        msg = """${msg}'${set[i]}',""";
    }
    msg = """${msg}'${set[i]}')"}""";
    
    message.setBody(JsonOutput.toJson(new JsonSlurper().parseText(msg)));
    message.setProperty("WarehouseMapped_Original", XmlUtil.serialize(root))

    return message;

}