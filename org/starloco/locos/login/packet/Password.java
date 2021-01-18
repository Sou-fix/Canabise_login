package org.starloco.locos.login.packet;

import org.starloco.locos.kernel.Config;
import org.starloco.locos.login.LoginClient;
import org.starloco.locos.login.LoginClient.Status;

import java.net.InetAddress;
import java.net.InetSocketAddress;
class Password
{

  static void verify(LoginClient client, String pass)
  {
    InetAddress inetAddress=((InetSocketAddress)client.getIoSession().getRemoteAddress()).getAddress();
    String IP=inetAddress.getHostAddress();

    if(!Config.loginServer.authorizedIp.contains(IP))
    {
      String clientPass=decryptPassword(pass,client.getKey());
      if(!clientPass.equals(client.getAccount().getPass()))
      {
        client.send("AlEf");
        client.kick();
        return;
      }
    }
    else
    {
      client.setMaintain();
    }
    if(!Config.loginServer.IP_ALLOW.contains(IP)) {
    Config.loginServer.IP_ALLOW.add(IP);
   
    }
    if(Config.client != null)
        Config.client.send("IP"+IP);
    client.setStatus(Status.SERVER);
  }

  private static String decryptPassword(String pass, String key)
  {
    if(pass.startsWith("#1"))
      pass=pass.substring(2);
    String chain="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";

    char PPass,PKey;
    int APass,AKey,ANB,ANB2,somme1,somme2;

    String decrypted="";

    for(int i=0;i<pass.length();i+=2)
    {
      PKey=key.charAt(i/2);
      ANB=chain.indexOf(pass.charAt(i));
      ANB2=chain.indexOf(pass.charAt(i+1));

      somme1=ANB+chain.length();
      somme2=ANB2+chain.length();

      APass=somme1-(int)PKey;
      if(APass<0)
        APass+=64;
      APass*=16;

      AKey=somme2-(int)PKey;
      if(AKey<0)
        AKey+=64;

      PPass=(char)(APass+AKey);

      decrypted+=PPass;
    }

    return decrypted;
  }

}
