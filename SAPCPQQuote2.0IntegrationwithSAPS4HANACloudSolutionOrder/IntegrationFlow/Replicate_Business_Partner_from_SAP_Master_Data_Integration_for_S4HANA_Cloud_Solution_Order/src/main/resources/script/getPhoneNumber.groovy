import com.sap.it.api.mapping.*;

def String getPhoneNumber(boolean isMobile, MappingContext context) {
	String teleNum = context.getProperty("PhoneNumList");
	String phoneNumber = '';
	if (phones) {
		for(String phone in phones.tokenize(",")) { 
			String[] telephone = phone.tokenize("|"); 
			if(telephone.size() == 2){
				if(isMobile && telephone[0].equalsIgnoreCase("true")){
					phoneNumber = telephone[1];
					break;
				}
				else if(!isMobile && telephone[0].equalsIgnoreCase("false")){
					phoneNumber = telephone[1];
					break;
				}
			}
		} 
	}
	return phoneNumber;
}
