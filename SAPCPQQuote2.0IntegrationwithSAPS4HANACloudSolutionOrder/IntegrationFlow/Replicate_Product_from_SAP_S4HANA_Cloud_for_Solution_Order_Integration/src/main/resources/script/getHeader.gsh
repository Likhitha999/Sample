import com.sap.it.api.mapping.*

def String getProperty(String propertyName, MappingContext context)

{
  String PropertyValue= context.getProperty(propertyName);

  PropertyValue= PropertyValue.toString();

  return PropertyValue;

}