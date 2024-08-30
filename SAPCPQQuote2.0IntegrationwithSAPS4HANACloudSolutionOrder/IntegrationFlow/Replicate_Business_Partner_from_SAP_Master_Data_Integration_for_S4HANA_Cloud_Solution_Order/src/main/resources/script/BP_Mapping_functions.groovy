import com.sap.it.api.mapping.*;

def String getProperty(String propertyName, MappingContext context) {
    def propertyValue = context.getProperty(propertyName);
    return propertyValue;
}

def String getHeader(String headerName, MappingContext context) {
    def headerValue = context.getHeader(headerName);
    return headerValue;
}

def String getBankAccount(int orderNumber, MappingContext context) {
	String bankAcounts = context.getProperty("BankAccountList");
	String bankAccount = '';
	if (bankAcounts != null && bankAcounts.length() != 0) {
		String[] splitedBankAccounts = bankAcounts.tokenize(",");
		if(splitedBankAccounts.size() >= orderNumber){
			bankAccount = splitedBankAccounts[orderNumber - 1];
		}
	}
	return bankAccount;
}

def String getTaxNumber(int orderNumber, MappingContext context) {
	String taxNumbers = context.getProperty("TaxNumberList");
	String taxNumber = '';
	if (taxNumbers != null && taxNumbers.length() != 0) {
		String[] splitedTaxNumbers = taxNumbers.tokenize(",");
		if(splitedTaxNumbers.size() >= orderNumber){
			String[] splitedTaxNumber = splitedTaxNumbers[orderNumber - 1].tokenize("|");
			taxNumber = splitedTaxNumber[0];
		}
	}
	return taxNumber;
}
