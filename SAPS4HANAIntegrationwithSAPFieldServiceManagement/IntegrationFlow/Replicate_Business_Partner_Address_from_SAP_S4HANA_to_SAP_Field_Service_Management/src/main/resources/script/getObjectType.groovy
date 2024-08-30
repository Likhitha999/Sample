import com.sap.it.api.mapping.*;

def String getObjectType(String objType, MappingContext context){
    String value = context.getHeader("Replicate_Address_For");
    
    if(value.equals("Employee")){
        objType="UNIFIEDPERSON";
    }
    else{
        objType="BUSINESSPARTNER";
    }
    
	return objType; 
}