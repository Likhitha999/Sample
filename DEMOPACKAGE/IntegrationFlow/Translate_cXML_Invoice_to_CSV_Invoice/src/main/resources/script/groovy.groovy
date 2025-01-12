import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
def Message processData(Message message) {
  //Body
  def body = message.getBody();
  body =
  """
  ------WebKitFormBoundary7MA4YWxkTrZu0gW
  Content - Disposition: form - data;
  name = "raw.directory"

  dmol - logs - repo
    -- -- --WebKitFormBoundary7MA4YWxkTrZu0gW
  Content - Disposition: form - data;
  name = "raw.asset1"
  Content - Type: application / vnd.openxmlformats - officedocument.spreadsheetml.sheet

  ${body}
  -- -- --WebKitFormBoundary7MA4YWxkTrZu0gW
  Content - Disposition: form - data;
  name = "raw.asset1.filename"

  invoice
  -- -- --WebKitFormBoundary7MA4YWxkTrZu0gW--
  """

  message.setBody(body);
  return message;
}