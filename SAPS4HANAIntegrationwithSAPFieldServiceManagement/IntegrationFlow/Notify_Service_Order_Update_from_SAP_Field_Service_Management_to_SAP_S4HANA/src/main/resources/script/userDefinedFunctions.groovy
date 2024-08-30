import com.sap.it.api.mapping.*;

def String getProperty(String propertyName,  MappingContext context){
    def property = context.getProperty(propertyName);
    if (property == null){
        return propertyName;
    } else {
        return property;
    }
}