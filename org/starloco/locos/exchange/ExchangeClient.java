package org.starloco.locos.exchange;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.starloco.locos.exchange.packet.PacketHandler;
import org.starloco.locos.object.Server;

public class ExchangeClient
{

  private final IoSession ioSession;
  private Server server;

  public ExchangeClient(IoSession ioSession)
  {
    this.ioSession=ioSession;
  }

  public void send(String s)
  {
	  try {
    IoBuffer ioBuffer=IoBuffer.allocate(20480);
    ioBuffer.put(s.getBytes());

    this.ioSession.write(ioBuffer.flip());
	  }
	    catch(Exception e)
	    {
	      e.printStackTrace();
	    }
  }

  public IoSession getIoSession()
  {
    return ioSession;
  }

  public Server getServer()
  {
    return server;
  }

  public void setServer(Server server)
  {
    this.server=server;
  }

  void parser(String packet)
  {
    PacketHandler.parser(this,packet);
  }

  public void kick()
  {
    this.ioSession.close(true);
  }
}
