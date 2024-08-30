import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import groovy.xml.XmlUtil;

def Message processData(Message message) {
  def body = message.getBody(java.lang.String)
  def query = new XmlSlurper().parseText(body)
  def map = message.getProperties();
  def priceList = map.get("PricelistIds");

  query.ItemPriceList.each {
    it.priceList = priceList[it.priceList];
  }

  def valid_data = XmlUtil.serialize(query);
  message.setBody(valid_data);
  return message;
}
