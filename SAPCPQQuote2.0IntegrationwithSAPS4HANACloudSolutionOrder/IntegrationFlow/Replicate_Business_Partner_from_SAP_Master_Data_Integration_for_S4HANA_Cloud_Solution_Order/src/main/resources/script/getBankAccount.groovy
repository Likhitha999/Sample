import com.sap.it.api.mapping.*;

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
