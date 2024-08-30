import com.sap.it.api.mapping.*;

/*MappingContext parameter to read or set headers and properties
*/

def String getExchangeProperty(String name, MappingContext context){
	 String value = context.getProperty(name);
	 return value;
}