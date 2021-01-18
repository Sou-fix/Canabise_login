package org.starloco.locos.tool.packetfilter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.starloco.locos.kernel.Logging;



public class IpCheck {
	//Configuration de sécurité
	//Realm
	private final static int GAME_PACKET_PER_SECOND =70;//On autorise au maximum X packets par ip par secondes
	private final static int GAME_PACKET_VERIF = 3;//On fait notre vérification sur X secondes
	private final static int Game_conx_max = 8;// Max co ig
	private final static int Time_ban = 120;// time ban 
	
	private final static String[] GAME_PACKET_TO_IGNORE = {};

	private static Map<Integer,IpCheck.IpInstance> _instances = new ConcurrentHashMap<Integer, IpCheck.IpInstance>();
	private static int index = 0;
	
	public static class IpInstance
	{
		private String _ip;
		private boolean _banned = false;
		//Connexion au org.icore.realm
		private int _Realm_con;
		private int _Realm_packet;
		//Pakcet par seconde sur le org.icore.game server
		private long _PacketGame_ref = 0;
		private int _PacketGame_count = 0;
		private long time_ban = 0;
		private int _game_co = 0;
		private long _time_vote = 0;
		//Nombre de connexions actives
		
		public void set_time_vote(long _time_vote) {
			this._time_vote = _time_vote;
		}

		public long get_time_vote() {
			return _time_vote;
		}
		
		public IpInstance(String ip)
		{
			_ip = ip;
			_Realm_con=0;
			_Realm_packet=0;
			_game_co = 0;
			_time_vote  = 0;
		}
		public String getIp()
		{
			return _ip;
		}
		public void newGamemConnection()
		{
			_game_co++;
		}
		public void delGameConnection()
		{
			_game_co--;
		}
		public boolean newRealMConnexion()
		{
			if(_ip == "127.0.0.1") return true;
			boolean r = true;
			if(_banned)
			{
				if(time_ban != 0){
				if (time_ban < (long) (System.currentTimeMillis()/1000)){
					_banned = false;
					_Realm_con = 1;	
					IpCheck.addToLog("RM deban l'ip : "+_ip);
					return true;
				}
				return false;
				}
			}
			if(_game_co >= Game_conx_max){
				IpCheck.addToLog("Nember max de co "+_game_co+" : "+_ip);
				return false;	
				}
			if(_Realm_con == 1) {
				if(_Realm_packet == 0){
					IpCheck.addToLog("RM Ban définitif 2eme conx sans packet pour l'ip : "+_ip);
					_banned = true;
					time_ban = ((long) (System.currentTimeMillis()/1000) + Time_ban);
					return false;	
				}}
			if(_Realm_con == 0) {
			_Realm_con = 1;
			}
			return r;
		}
		public boolean newGameConnexion()
		{
			if(_ip == "127.0.0.1") return true;
			boolean r = true;
			if(_banned)
			{
				return false;
			}
			if(_Realm_con == 0 ||_Realm_packet == 0)
			{
				IpCheck.addToLog("Game Ban définitif conx port game sans port realm  pour l'ip : "+_ip);
				_banned = true;
				time_ban = ((long) (System.currentTimeMillis()/1000) + Time_ban);
				return false;
			}
			if(_game_co >= Game_conx_max){
				IpCheck.addToLog("Nember max de co "+_game_co+" : "+_ip);
			return false;	
			}
            this.newGamemConnection();
			return r;
		}
		public boolean Realmchekpacket()
		{
			_Realm_packet = 1;
			return false;
		}
		public boolean RealmcdeBan()
		{
			_banned = false;
			_Realm_con = 0;
			return false;
		}
		public boolean RealmcBan()
		{
			_banned = true;
			IpCheck.addToLog("RM Ban définitif pas crai clint pour l'ip : "+_ip);
			time_ban = ((long) (System.currentTimeMillis()/1000) + Time_ban);
			return false;
		}
		public boolean newGamePacket(String packet)
		{
			//if(IpCheck.isIgnorepacket(packet)) return true;
			boolean r = true;
			long cur_t = (long) System.currentTimeMillis()/1000;
			if(_banned)
			{
				return false;
			}
			int lim = GAME_PACKET_VERIF * GAME_PACKET_PER_SECOND;
			if(cur_t - _PacketGame_ref > GAME_PACKET_VERIF)
			{
				_PacketGame_ref = cur_t;
				_PacketGame_count = 0;
			}
			_PacketGame_count++;
			if(_PacketGame_count >= lim)
			{
				IpCheck.addToLog("GamePacket ip au dessus de la limite : "+_ip);
				_PacketGame_count=0;
				r = false;
			}
			
			return r;
		}
	}
	
	public static boolean isIgnorepacket(String packet)
	{
		for(String p : GAME_PACKET_TO_IGNORE) if(p.equals(packet)) return true;
		return false;
	}
	
	public static IpCheck.IpInstance getInstance(String ip)
	{
		ip = ip.trim();
		if(ip.isEmpty()) return null;
		for(IpCheck.IpInstance ii : _instances.values())
		{
			if(ii.getIp().equalsIgnoreCase(ip))
			{
				return ii;
			}
		}
		return null;
	}
	public static void resetip()
	{
		_instances.clear();;
		return;
	}
	public static IpCheck.IpInstance addInstance(String ip)
	{
		ip = ip.trim();
		if(ip.isEmpty()) return null;
		IpCheck.IpInstance ii = new IpInstance(ip);
		_instances.put(index, ii);
		index++;
		return ii;
	}
	public static boolean canRealMConnect(String ip)
	{
		ip = ip.trim();
		if(ip.isEmpty()) return false;
		IpCheck.IpInstance ii = getInstance(ip);
		if(ii == null) ii = addInstance(ip);
		if(ii == null)
		{
			addToLog("RM: Une erreur s'est produite lors de l'ajout de l'ip : "+ip);
			return false;
		}
		return ii.newRealMConnexion();
	}
	public static boolean Setpacket(String ip)
	{
		IpCheck.IpInstance ii = getInstance(ip);
		return ii.Realmchekpacket();
	}
	public static boolean deBan(String ip)
	{
		IpCheck.IpInstance ii = getInstance(ip);
		return ii.RealmcdeBan();
	}
	public static boolean Ban(String ip)
	{
		IpCheck.IpInstance ii = getInstance(ip);
		return ii.RealmcBan();
	}
	public static boolean canGameConnect(String ip)
	{
		ip = ip.trim();
		if(ip.isEmpty()) return false;
		IpCheck.IpInstance ii = getInstance(ip);
		if(ii == null) ii = addInstance(ip);
		if(ii == null)
		{
			addToLog("GAME: Une erreur s'est produite lors de l'ajout de l'ip : "+ip);
			return false;
		}
		return ii.newGameConnexion();
	}
	public static boolean onGamePacket(String ip, String packet)
	{
		ip = ip.trim();
		if(ip.isEmpty()) return false;
		IpCheck.IpInstance ii = getInstance(ip);
		if(ii == null) ii = addInstance(ip);
		if(ii == null)
		{
			addToLog("GamePacket: Une erreur s'est produite lors de l'ajout de l'ip : "+ip);
			return false;
		}
		return ii.newGamePacket(packet);
	}
	public static long get_vote(String ip)
	{
		ip = ip.trim();
		if(ip.isEmpty()) return 0;
		IpCheck.IpInstance ii = getInstance(ip);
		if(ii == null) ii = addInstance(ip);
		if(ii == null)
		{
			addToLog("GamePacket: Une erreur s'est produite lors de l'ajout de l'ip : "+ip);
			return 0;
		}
		return ii.get_time_vote();
	}
	public static void set_vote(String ip, long time)
	{
		ip = ip.trim();
		if(ip.isEmpty()) return;
		IpCheck.IpInstance ii = getInstance(ip);
		if(ii == null) ii = addInstance(ip);
		if(ii == null)
		{
			addToLog("GamePacket: Une erreur s'est produite lors de l'ajout de l'ip : "+ip);
			return ;
		}
		ii.set_time_vote(time);
	}
	public static void newGameConnection(String ip)
	{
		ip = ip.trim();
		if(ip.isEmpty()) return;
		IpCheck.IpInstance ii = getInstance(ip);
		if(ii == null) ii = addInstance(ip);
		if(ii == null)
		{
			addToLog("Game: Une erreur s'est produite lors de l'ajout de l'ip : "+ip);
			return;
		}
		ii.newGamemConnection();
	}
	public static void delGameConnection(String ip)
	{
		ip = ip.trim();
		if(ip.isEmpty()) return;
		IpCheck.IpInstance ii = getInstance(ip);
		if(ii == null) ii = addInstance(ip);
		if(ii == null)
		{
			addToLog("Game: Une erreur s'est produite lors de l'ajout de l'ip : "+ip);
			return;
		}
		ii.delGameConnection();
	}
	public static void newRealmConnection(String ip)
	{
		ip = ip.trim();
		if(ip.isEmpty()) return;
		IpCheck.IpInstance ii = getInstance(ip);
		if(ii == null) ii = addInstance(ip);
		if(ii == null)
		{
			addToLog("Realm: Une erreur s'est produite lors de l'ajout de l'ip : "+ip);
			return;
		}
	}
	
	public static void delRealmConnection(String ip)
	{
		ip = ip.trim();
		if(ip.isEmpty()) return;
		IpCheck.IpInstance ii = getInstance(ip);
		if(ii == null) ii = addInstance(ip);
		if(ii == null)
		{
			addToLog("Realm: Une erreur s'est produite lors de l'ajout de l'ip : "+ip);
			return;
		}
	}
	
	public synchronized static void addToLog(String str)
	{
		Logging.getInstance().write("DDOS",str);
	}
}
