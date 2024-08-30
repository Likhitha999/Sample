import com.sap.it.api.mapping.*;

def String setTelephoneOrFax(String number, String extension){
       Integer index = number.length()-extension.length();
       String value = number.substring(0, index);
       return value;
}