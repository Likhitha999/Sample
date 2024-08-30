import com.sap.it.api.mapping.*;

def String removeLeadingZeros(String value){
    String output = value.replaceAll("^0*", "");
    return output;
}