import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    def index     = message.getProperty("loopIndex") as Number
    def iFlowList = message.getProperty("iFlowList") as List
    def entry     = iFlowList[index];

    message.setProperty("loopIndex",index + 1)
    message.setProperty("iFlowId",entry.Id)
    message.setProperty("iFlowName",entry.Name)
    message.setProperty("iFlowVersion",entry.Version)
    message.setBody(entry)

    return message
}