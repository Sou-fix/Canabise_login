package org.starloco.locos.exchange;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.starloco.locos.kernel.Console;
import org.starloco.locos.kernel.Logging;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

class ExchangeHandler implements IoHandler
{

  @Override @SuppressWarnings("deprecation")
  public void exceptionCaught(IoSession arg0, Throwable arg1) throws Exception
  {
	  try {
    this.setLogged(arg0,"exceptionCaught : "+arg1.getMessage());
    Console.instance.write("eSession "+arg0.getId()+" exception : "+arg1.getCause()+" : "+arg1.getMessage());
   
    ExchangeClient client=(ExchangeClient)arg0.getAttachment();
    client.getServer().setState(0);
	  }
	    catch(Exception e)
	    {
	      e.printStackTrace();
	    }
  }

  @Override @SuppressWarnings("deprecation")
  public void messageReceived(IoSession arg0, Object arg1) throws Exception
  {try {
    String string=new String(((IoBuffer)arg1).array());
    Console.instance.write("eSession "+arg0.getId()+" < "+string);

    ExchangeClient client=(ExchangeClient)arg0.getAttachment();
    String message=bufferToString(arg1);

    if(client.getServer()!=null)
      Logging.getInstance().write("Game","messageReceived de "+client.getServer().getId()+" : "+message);
    else
    {
      InetSocketAddress socketAddress=(InetSocketAddress)arg0.getRemoteAddress();
      InetAddress inetAddress=socketAddress.getAddress();
      String IP=inetAddress.getHostAddress();
      Logging.getInstance().write("Game","messageReceived de "+IP+" : "+message);
    }
    client.parser(message);
  }
  catch(Exception e)
  {
    e.printStackTrace();
  }
  }

  public void messageSent(IoSession arg0, Object arg1) throws Exception
  {
	  try {
    String message=bufferToString(arg1);
    this.setLogged(arg0,"messageSent : "+message);
    Console.instance.write("eSession "+arg0.getId()+" > "+message);
	  }
	    catch(Exception e)
	    {
	      e.printStackTrace();
	    }
  }

  @Override
  public void inputClosed(IoSession ioSession) throws Exception
  {
    ioSession.close(true);
  }

  @Override
  public void sessionClosed(IoSession arg0) throws Exception
  {
    Console.instance.write("eSession "+arg0.getId()+" closed");
    if(arg0.getAttribute("client") instanceof ExchangeClient)
    {
      ExchangeClient client=(ExchangeClient)arg0.getAttribute("client");
      client.getServer().setState(0);
    }
    this.setLogged(arg0,"sessionClosed");
  }

  @Override @SuppressWarnings("deprecation")
  public void sessionCreated(IoSession arg0) throws Exception
  {
	  try {
    Console.instance.write("eSession "+arg0.getId()+" created");
    arg0.setAttachment(new ExchangeClient(arg0));

    IoBuffer ioBuffer=IoBuffer.allocate(2048);
    ioBuffer.put("SK?".getBytes());
    ioBuffer.flip();
    arg0.write(ioBuffer);

    this.setLogged(arg0,"sessionCreated");
	  }
	    catch(Exception e)
	    {
	      e.printStackTrace();
	    }
  }

  @Override
  public void sessionIdle(IoSession arg0, IdleStatus arg1) throws Exception
  {
    this.setLogged(arg0,"sessionIdle");
    Console.instance.write("eSession "+arg0.getId()+" idle");
  }

  @Override
  public void sessionOpened(IoSession arg0) throws Exception
  {
    this.setLogged(arg0,"sessionOpened");
  }

  String bufferToString(Object o)
  {
    IoBuffer actualBuffer=(IoBuffer)o;
    IoBuffer buffer=IoBuffer.allocate(actualBuffer.capacity());
    buffer.put((IoBuffer)o);
    buffer.flip();

    CharsetDecoder cd=Charset.forName("UTF-8").newDecoder();

    try
    {
      return buffer.getString(cd);
    }
    catch(CharacterCodingException e)
    {
      e.printStackTrace();
    }
    return "undefined";
  }
  
  @SuppressWarnings("deprecation")
  private void setLogged(IoSession arg0, String msg)
  {
	  try {
    ExchangeClient client=(ExchangeClient)arg0.getAttachment();
    Logging.getInstance().write("Game",msg+" -> "+client.getServer().getId());
	  }
	    catch(Exception e)
	    {
	      e.printStackTrace();
	    }
  }
}
