import com.sap.gateway.ip.core.customdev.util.Message;

def Message processData(Message message) {

	def messageLog = messageLogFactory.getMessageLog(message);
	if(messageLog != null){

		def invoice_number = message.getHeaders().get("INVOICE_ID");
		if(invoice_number!=null){
			messageLog.addCustomHeaderProperty("invoice", invoice_number);
        }
	}
	return message;
}
