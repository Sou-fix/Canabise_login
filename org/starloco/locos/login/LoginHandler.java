package org.starloco.locos.login;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.starloco.locos.kernel.Config;
import org.starloco.locos.kernel.Console;
import org.starloco.locos.tool.packetfilter.IpCheck;

import java.util.Random;

public class LoginHandler implements IoHandler
{

  private static final Random random=new Random();

  public static synchronized void sendToAll(String packet)
  {
    LoginServer login=Config.loginServer;
    if(login==null)
      return;

    for(LoginClient client : login.getClients().values())
    {
      if(client==null)
        continue;
      IoSession ioSession=client.getIoSession();
      if(ioSession!=null&&(ioSession.isConnected()||!ioSession.isClosing()))
        client.send(packet);
    }
  }

  @Override
  public void exceptionCaught(IoSession arg0, Throwable arg1) throws Exception
  {
    Console.instance.write("session "+arg0.getId()+" exception : "+arg1.getMessage());
  }

  @Override
  public void messageReceived(IoSession arg0, Object arg1) throws Exception
  {
    if(arg0.getAttribute("client") instanceof LoginClient)
    {
      LoginClient client=(LoginClient)arg0.getAttribute("client");
      if(client!=null)
      {
        for(String data : ((String)arg1).split("\n"))
        {
          Console.instance.write((client.getAccount()!=null ? client.getAccount().getPseudo() : "")+" session "+arg0.getId()+" : recv < "+data);
          client.parser(data);
        }
      }
    }
  }

  @Override
  public void messageSent(IoSession arg0, Object arg1) throws Exception
  {
    Console.instance.write("session "+arg0.getId()+" : sent > "+arg1.toString());
  }

  @Override
  public void inputClosed(IoSession ioSession) throws Exception
  {
    ioSession.close(true);
  }

  @Override
  public void sessionClosed(IoSession arg0) throws Exception
  {
    Console.instance.write("session "+arg0.getId()+" closed");

    if(arg0.getAttribute("client") instanceof LoginClient)
    {
      LoginClient client=(LoginClient)arg0.getAttribute("client");
      client.getAccount().setState(0);
    }
  }

  @Override
  public void sessionCreated(IoSession arg0) throws Exception
  {
	  if(!IpCheck.canRealMConnect(arg0.getRemoteAddress().toString().substring(1).split(":")[0]))
		{
		  arg0.close(true);
		}
    else
    {
      Console.instance.write("session "+arg0.getId()+" created"+arg0.getRemoteAddress().toString().substring(1).split(":")[0]);
      arg0.setAttribute("client",new LoginClient(arg0,generateKey()));
    }
  }

  @Override
  public void sessionIdle(IoSession arg0, IdleStatus arg1) throws Exception
  {
    Console.instance.write("session "+arg0.getId()+" idle");
  }

  @Override
  public void sessionOpened(IoSession arg0) throws Exception
  {
    Console.instance.write("session "+arg0.getId()+" oppened");
  }

  private String generateKey()
  {
    String alphabet="abcdefghijklmnopqrstuvwxyz";
    StringBuilder hashKey=new StringBuilder();

    for(int i=0;i<32;i++)
      hashKey.append(alphabet.charAt(random.nextInt(alphabet.length())));
    return hashKey.toString();
  }
}
