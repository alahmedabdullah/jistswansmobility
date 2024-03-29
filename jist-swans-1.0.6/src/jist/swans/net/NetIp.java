//////////////////////////////////////////////////
// JIST (Java In Simulation Time) Project
// Timestamp: <NetIp.java Tue 2004/04/20 10:12:52 barr pompom.cs.cornell.edu>
//

// Copyright (C) 2004 by Cornell University
// All rights reserved.
// Refer to LICENSE for terms and conditions of use.

package jist.swans.net;

import java.io.IOException;

import guiTrace.*;
import guiTrace.Trace;
import jist.swans.mac.MacAddress;
import jist.swans.mac.MacInterface;
import jist.swans.mac.MacLoop;

import jist.swans.route.RouteInterface;

import jist.swans.misc.Message;
import jist.swans.misc.Mapper;
import jist.swans.misc.Util;

import jist.swans.Constants;
import jist.swans.Main;

import jist.runtime.JistAPI;

import org.apache.log4j.*;

/**
 * IPv4 implementation based on RFC 791. Performs protocol
 * multiplexing, and prioritized packet queuing, but no
 * RED or packet fragmentation.
 *
 * @author Rimon Barr &lt;barr+jist@cs.cornell.edu&gt;
 * @version $Id: NetIp.java,v 1.45 2004/11/03 23:53:22 barr Exp $
 * @since SWANS1.0
 */

public class NetIp implements NetInterface
{
  /**
   * IP logger.
   */
  public static final Logger log = Logger.getLogger(NetIp.class.getName());

  /**
   * Information about each network interface.
   */
  public static class NicInfo
  {
    /** mac entity. */
    public MacInterface mac;
    /** outgoing packet queue. */
    public MessageQueue q;
    /** whether link layer is busy. */
    public boolean busy;
  }

  //////////////////////////////////////////////////
  // constants
  //

  /**
   * Packet fragmentation threshold.
   */
  public static final int THRESHOLD_FRAGMENT = 2048;

  /**
   * Maximum packet queue length.
   */
  public static final int MAX_QUEUE_LENGTH = 1000;

  //////////////////////////////////////////////////
  // locals
  //

  // entity hookup
  /** self-referencing proxy entity. */
  protected NetInterface self;

  /** local network address. */
  protected NetAddress localAddr;

  /** routing protocol. */
  protected RouteInterface routing;

  /** protocol number mapping. */
  protected Mapper protocolMap;

  /** protocol handlers. */
  protected NetHandler[] protocolHandlers;

  /** network interfaces. */
  protected NicInfo[] nics;

  /** packet loss models. */
  protected PacketLoss incomingLoss, outgoingLoss;
  
  /** trace object for this Network Interface. */
  
  protected Trace trace;

  //////////////////////////////////////////////////
  // initialization 
  //

  /**
   * Initialize IP implementation with given address and protocol mapping.
   *
   * @param addr local network address
   * @param protocolMap protocol number mapping
   * @param in incoming packet loss model
   * @param out outgoing packet loss model
   */
  public NetIp(NetAddress addr, Mapper protocolMap, PacketLoss in, PacketLoss out)
  {
	  
	
	  
		  guiTrace.Trace trace = null;
	  	  
	  	  //Wireless Range of mobile or static radios in this simulation environment
	  	  int wirelessRangeX = 410; //this value can be changed. NEEDS IMPROVEMENT
	  	  int wirelessRangeY = 410;
	  		try {
	  		    trace = new guiTrace.JavisTrace("genericTrace.nam");
	  		    
	  		    trace.writePreamble(wirelessRangeX,wirelessRangeY);
	  		    //trace.writePostamble();
	  		    
	  		    System.out.println("Create :TraceFile! -> OK!");
	  		}
	  		catch (IOException e) {
	  		    System.out.println("Could not create TraceFile!");
	  		}
	  	    //---------- Javis guiTrace ---------//
	  	  new NetIp(addr,protocolMap,in,out,trace);
  }
  
  
  
  /**
   * Initialize IP implementation with given address and protocol mapping.
   *
   * @param addr local network address
   * @param protocolMap protocol number mapping
   * @param in incoming packet loss model
   * @param out outgoing packet loss model
   * @param tr trace file of field
   */
  public NetIp(NetAddress addr, Mapper protocolMap, PacketLoss in, PacketLoss out, Trace tr)
  {
    // proxy entity
    this.self = (NetInterface)JistAPI.proxy(this, NetInterface.class);
    // local address
    setAddress(addr);
    // protocol number mapping
    this.protocolMap = protocolMap;
    // protocol handlers
    this.protocolHandlers = new NetHandler[protocolMap.getLimit()];
    // network interfaces
    this.nics = new NicInfo[0];
    // packet loss
    this.incomingLoss = in;
    this.outgoingLoss = out;
    
    this.trace = tr;
    // add loopback mac:
    //   therefore, loopback = 0, Constants.NET_INTERFACE_LOOPBACK
    //              next     = 1, Constants.NET_INTERFACE_DEFAULT
    MacLoop loopback = new MacLoop();
    byte netid = addInterface(loopback.getProxy());
    if(Main.ASSERT) Util.assertion(netid==Constants.NET_INTERFACE_LOOPBACK);
    loopback.setNetEntity(getProxy(), netid);
  }

  //////////////////////////////////////////////////
  // entity hookup
  //

  /**
   * Return self-referencing proxy entity.
   *
   * @return self-referencing proxy entity
   */
  public NetInterface getProxy()
  {
    return this.self;
  }

  //////////////////////////////////////////////////
  // address
  //

  /**
   * Set local network address.
   *
   * @param addr local network address
   */
  public void setAddress(NetAddress addr)
  {
    if(Main.ASSERT) Util.assertion(addr!=null);
    this.localAddr = addr;
  }

  /**
   * Whether packet is for local consumption.
   *
   * @param msg packet to inspect
   * @return whether packet is for local consumption
   */
  private boolean isForMe(NetMessage.Ip msg)
  {
    NetAddress addr = msg.getDst();
    return NetAddress.ANY.equals(addr)
      || NetAddress.LOCAL.equals(addr)
      || localAddr.equals(addr);
  }

  //////////////////////////////////////////////////
  // routing, protocols, interfaces
  //

  /**
   * Set routing implementation.
   *
   * @param routingEntity routing entity
   */
  public void setRouting(RouteInterface routingEntity)
  {
    if(!JistAPI.isEntity(routingEntity)) throw new IllegalArgumentException("expected entity");
    this.routing = routingEntity;
  }

  /**
   * Add network interface with default queue.
   *
   * @param macEntity link layer entity
   * @return network interface identifier
   */
  public byte addInterface(MacInterface macEntity)
  {
	  MessageQueue.NoDropMessageQueue mq = new MessageQueue.NoDropMessageQueue(Constants.NET_PRIORITY_NUM, MAX_QUEUE_LENGTH);
	  
	  return addInterface(macEntity, mq);
  }

  /**
   * Add network interface.
   *
   * @param macEntity link layer entity
   * @return network interface identifier
   */
  public byte addInterface(MacInterface macEntity, MessageQueue q)
  {
    if(!JistAPI.isEntity(macEntity)) throw new IllegalArgumentException("expected entity");
    // create new nicinfo
    NicInfo ni = new NicInfo();
    ni.mac = macEntity;
    ni.q = q;
    ni.q.setTrace(this.trace);
    ni.q.setLocalAddr(this.getAddress());
    ni.busy = false;
    // store
    NicInfo[] nics2 = new NicInfo[nics.length+1];
    System.arraycopy(nics, 0, nics2, 0, nics.length);
    nics2[nics.length] = ni;
    nics = nics2;
    // return interface id
    return (byte)(nics.length-1);
  }

  /**
   * Set network protocol handler.
   *
   * @param protocolId protocol identifier
   * @param handler protocol handler
   */
  public void setProtocolHandler(int protocolId, NetHandler handler)
  {
    protocolHandlers[protocolMap.getMap(protocolId)] = handler;
  }

  /**
   * Return network protocol handler.
   *
   * @param protocolId protocol identifier
   * @return procotol handler
   */
  private NetHandler getProtocolHandler(int protocolId)
  {
    return protocolHandlers[protocolMap.getMap(protocolId)];
  }

  //////////////////////////////////////////////////
  // NetInterface implementation
  //

  /** {@inheritDoc} */
  public NetAddress getAddress() throws JistAPI.Continuation
  {
    return localAddr;
  }

  /** {@inheritDoc} */
  public void receive(Message msg, MacAddress lastHop, byte macId, boolean promisc)
  {
    if(msg==null) throw new NullPointerException();
    NetMessage.Ip ipmsg = (NetMessage.Ip)msg;
   
    //INCOMING PACKET LOSS- EMRE ATSAN
    if(incomingLoss.shouldDrop(ipmsg)) 
    {
    	  String s = lastHop.toString();
      	  //String s = ipmsg.getSrc().getIP().getHostName().substring(6);
        	String srcid = s;
        	String destid = localAddr.getIP().getHostName();
        	
        	if (destid.equalsIgnoreCase("255.255.255.255"))
        	{
        		destid ="-1";
        	}
        	else
        	{
        		destid = destid.substring(6);
        	}
        	//System.out.println("Incoming Drop at link from: "+srcid+" to "+destid);
        	Short packettype = ipmsg.getProtocol();
        	Integer packetSize = ipmsg.getSize();
        	Short packetid = ipmsg.getId();
            //GUI-SUPPORT for SWANS
          	try {
          		trace.handleEvent(new LinkDropTraceEvent(srcid, destid,packettype,packetSize,packetid));
          	}catch (IOException e) {
          		// TODO Auto-generated catch block
          		e.printStackTrace();
          	}
          	//END- GUI SUPPRT for SWANS
      	  
    	return;
    }
    
    if(log.isInfoEnabled())
    {
      log.info("receive t="+JistAPI.getTime()+" from="+lastHop+" on="+macId+" data="+msg);
    }
    if(routing!=null) routing.peek(ipmsg, lastHop);
    
    if(isForMe(ipmsg))
    {
    	String s = ipmsg.getSrc().getIP().getHostName().substring(6);
    	//String s = lastHop.toString();
    	//System.out.println("Last Hop: "+s+" Packet Source: "+ ipmsg.getSrc().getIP().getHostName().substring(6));
    	//System.out.println();
    	String srcid = s;
    	String destid = localAddr.getIP().getHostName();
    	
    	//System.out.println("Receive Destination: "+destid);
    	if (destid.equalsIgnoreCase("255.255.255.255"))
    	{
    		destid ="-1";
    	}
    	else
      	{
      		destid = destid.substring(6);
      	}
    	Short packettype = ipmsg.getProtocol();
    	Integer packetSize = ipmsg.getSize();
    	Short packetid = ipmsg.getId();
      
    	//GUI-SUPPORT for SWANS
      	try {
      		trace.handleEvent(new ReceiveTraceEvent(srcid, destid,packettype,packetSize,packetid));
      	}catch (IOException e) {
      		// TODO Auto-generated catch block
      		e.printStackTrace();
      	}
      	//END- GUI SUPPRT for SWANS
    	
      JistAPI.sleep(Constants.NET_DELAY);
      getProtocolHandler(ipmsg.getProtocol()).receive(ipmsg.getPayload(), 
          ipmsg.getSrc(), lastHop, macId, ipmsg.getDst(), 
          ipmsg.getPriority(), ipmsg.getTTL());
     
    }
    else
    {
      if(ipmsg.getTTL()>0)
      {
  
        if(ipmsg.isFrozen()) ipmsg = ipmsg.copy();
  
        ipmsg.decTTL();
        sendIp(ipmsg);
      }
      else
      {
    	  //DROP packet because of TTL
    	  String s = lastHop.toString();
      	  //String s = ipmsg.getSrc().getIP().getHostName().substring(6);
        	String srcid = s;
        	String destid = localAddr.getIP().getHostName();
        	//System.out.println("Incoming Drop (because of TTL=0) at: "+destid);
        	if (destid.equalsIgnoreCase("255.255.255.255"))
        	{
        		destid ="-1";
        	}
        	else
        	{
        		destid = destid.substring(6);
        	}
        	
        	Short packettype = ipmsg.getProtocol();
        	Integer packetSize = ipmsg.getSize();
        	Short packetid = ipmsg.getId();
            //GUI-SUPPORT for SWANS
          	try {
          		trace.handleEvent(new LinkDropTraceEvent(srcid, destid,packettype,packetSize,packetid));
          	}catch (IOException e) {
          		// TODO Auto-generated catch block
          		e.printStackTrace();
          	}
          	//END- GUI SUPPRT for SWANS
      }
    }
  }

  /** {@inheritDoc} */
  public void send(Message msg, NetAddress dst, 
      short protocol, byte priority, byte ttl) 
  {
    if(msg==null) throw new NullPointerException();
    sendIp(new NetMessage.Ip(msg, localAddr, dst, 
          protocol, priority, ttl));
  }

  /** {@inheritDoc} */
  public void send(NetMessage.Ip msg, int interfaceId, MacAddress nextHop) 
  {
    if(msg==null) throw new NullPointerException();
    
    //PACKET LOSS HAPPENS HERE! EMRE ATSAN
    if(outgoingLoss.shouldDrop(msg)) 
    {
    	String s = localAddr.getIP().getHostName();
    	//String s = ipmsg.getSrc().getIP().getHostName().substring(6);
    	String srcid = s.substring(6);
    	
    	String destid = nextHop.toString();
    	//System.out.println("Outgoing Drop at link from: "+srcid+" to "+destid);
    	
    	//destid = destid.substring(6);
    	
    	
    	Short packettype = msg.getProtocol();
    	Integer packetSize = msg.getSize();
    	Short packetid = msg.getId();
    	//GUI-SUPPORT for SWANS
    	try {
    		trace.handleEvent(new LinkDropTraceEvent(srcid, destid,packettype,packetSize,packetid));
    	}catch (IOException e) {
    		
    		e.printStackTrace();
    	}
    	//END- GUI SUPPRT for SWANS
    	
    	
    	return;
    }
    /*
    if(msg.getSize()>THRESHOLD_FRAGMENT)
    {
      throw new RuntimeException("ip fragmentation not implemented");
    }
    */
    if(log.isDebugEnabled())
    {
      log.debug("queue t="+JistAPI.getTime()+" to="+nextHop+" on="+interfaceId+" data="+msg);
    }
    NicInfo ni = nics[interfaceId];
    ni.q.insert(new QueuedMessage(msg, nextHop), msg.getPriority());
    if(!ni.busy) pump(interfaceId);
  }

  //////////////////////////////////////////////////
  // send/receive
  //

  /**
   * Send an IP packet. Knows how to broadcast, to deal
   * with loopback. Will call routing for all other destinations.
   *
   * @param msg ip packet
   */
  private void sendIp(NetMessage.Ip msg) 
  {
    if (NetAddress.ANY.equals(msg.getDst()))
    {
      // broadcast
      send(msg, Constants.NET_INTERFACE_DEFAULT, MacAddress.ANY);
    }
    else if(NetAddress.LOCAL.equals(msg.getDst()) || localAddr.equals(msg.getDst()))
    {
      // loopback
      send(msg, Constants.NET_INTERFACE_LOOPBACK, MacAddress.LOOP);
    }
    else
    {
      // route and send
      routing.send(msg);
    }
  }

  //////////////////////////////////////////////////
  // send pump
  //

  /** {@inheritDoc} */
  public void pump(int interfaceId)
  {
    NicInfo ni = nics[interfaceId];
    if(ni.q.isEmpty())
    {
      ni.busy = false;
    }
    else
    {
      ni.busy = true;
      QueuedMessage qmsg = ni.q.remove();
      NetMessage.Ip ip = (NetMessage.Ip)qmsg.getPayload();
      ip = ip.freeze(); // immutable once packet leaves node
      if(log.isInfoEnabled())
      {
        log.info("send t="+JistAPI.getTime()+" to="+qmsg.getNextHop()+" data="+ip);
      }
      JistAPI.sleep(Constants.NET_DELAY);
      ni.mac.send(ip, qmsg.getNextHop());
    }
  }

  //////////////////////////////////////////////////
  // display
  //

  /** {@inheritDoc} */
  public String toString()
  {
    return "ip:"+localAddr;
  }

}

