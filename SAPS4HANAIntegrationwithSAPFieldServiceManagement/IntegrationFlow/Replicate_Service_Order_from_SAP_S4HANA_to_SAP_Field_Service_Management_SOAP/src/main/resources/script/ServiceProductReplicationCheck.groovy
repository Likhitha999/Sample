import com.sap.it.api.mapping.*;

def String ServiceProductReplicationCheck(String value, MappingContext context){
    def ReplicationFlag = context.getProperty("ServiceProductReplication")
    if (ReplicationFlag ==~ /(?i)(true|x)/){
        return true
    }else{
        return false
    }
}