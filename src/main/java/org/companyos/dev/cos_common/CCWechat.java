package org.companyos.dev.cos_common;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;


public class CCWechat {
  public static JSONObject getUserInfomationByCode(String appId, String appSecret, String code) {
    CCReturn<String> ret = CCHttpClient.get(
        "https://api.weixin.qq.com/sns/oauth2/access_token?",
        "utf-8",
        new BasicNameValuePair[] {
            new BasicNameValuePair( "appid" , appId),
            new BasicNameValuePair( "secret" , appSecret),
            new BasicNameValuePair( "code" , code),
            new BasicNameValuePair( "grant_type" , "authorization_code"),
        });

    if (!ret.isSuccess())
      return null;

    JSONObject retJSON = new JSONObject(ret.getV());

    if (retJSON == null)
      return null;

    if (retJSON.has("errcode") && retJSON.getInt("errcode") != 0) {
      return retJSON;
    }

    String accessToken = retJSON.getString("access_token");
    String openID = retJSON.getString("openid");

    if (accessToken == null ||  openID == null)
      return null;

    ret = CCHttpClient.get(
        "https://api.weixin.qq.com/sns/userinfo?",
        "utf-8",
        new BasicNameValuePair[] {
            new BasicNameValuePair( "access_token" , accessToken),
            new BasicNameValuePair( "openid" , openID),
            new BasicNameValuePair( "lang" , "zh_CN")
        });

    if (!ret.isSuccess())
      return null;

    return  new JSONObject(ret.getV());
  }
}