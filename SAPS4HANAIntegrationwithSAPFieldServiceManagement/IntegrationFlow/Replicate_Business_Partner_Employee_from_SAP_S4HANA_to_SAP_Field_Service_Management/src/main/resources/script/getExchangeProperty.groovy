import com.sap.it.api.mapping.*;

def String getExchangeProperty(String name, MappingContext context){
	 String value = context.getProperty(name);
	 return value;
}