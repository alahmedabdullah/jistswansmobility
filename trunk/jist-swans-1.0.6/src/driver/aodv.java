
//JIST (Java In Simulation Time) Project
//Timestamp: <aodvsim.java Tue 2004/04/06 11:57:32 barr pompom.cs.cornell.edu>


//Copyright (C) 2004 by Cornell University
//All rights reserved.
//Refer to LICENSE for terms and conditions of use.

package driver;

import guiTrace.JavisTrace;
import jist.swans.field.Field;
import jist.swans.field.Mobility;
import jist.swans.field.Placement;
import jist.swans.field.Fading;
import jist.swans.field.Spatial;
import jist.swans.field.PathLoss;
import jist.swans.radio.RadioNoise;
import jist.swans.radio.RadioNoiseImprovedIndep;
import jist.swans.radio.RadioInfo;
import jist.swans.mac.MacAddress;
import jist.swans.mac.MacDumb;
import jist.swans.net.NetAddress;
import jist.swans.net.NetMessage;
import jist.swans.net.NetIp;
import jist.swans.net.PacketLoss;
import jist.swans.trans.TransUdp;
import jist.swans.route.RouteInterface;
import jist.swans.route.RouteAodv;
import jist.swans.misc.MessageBytes;
import jist.swans.misc.Util;
import jist.swans.misc.Mapper;
import jist.swans.misc.Location;
import jist.swans.misc.Message;
import jist.swans.Constants;

import jist.runtime.JistAPI;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Random;
import java.util.Vector;


/**
 * AODV simulation.  Derived from bordercast
 * 
 * @author Clifton
 *
 */
public class aodv
{

	/** Default port number to send and receive packets. */
	private static final int PORT = 3001;
	
	
	
	private static PrintStream statsfile;

	/** Routing protocol to use. */
	private static int protocol = Constants.NET_PROTOCOL_AODV;
	/** Number of nodes. */
	private static int nodes = 50;
	/** Field dimensions (in meters). */
	private static Location.Location2D field = new Location.Location2D(1500, 300);
	/** Node mobility model. */
	private static String mobilityString="waypoint";
	private static int mobilityModel = Constants.MOBILITY_WAYPOINT;
	/** Node mobility options. */
	private static String mobilityOpts = "0:1:1:20";
	/** Packet loss options. */
	private static String lossOpts = "0.2";
	/** Number of messages sent per minute per node. */
	private static double sendRate = 4*60.0;
	/** Start of sending (seconds). */
	private static int startTime = 10;
	/** Number of seconds to send messages. */
	private static int duration = 900;
	/** Number of seconds after messages stop sending to end simulation. */
	private static int resolutionTime = 10; 
	/** Random seed. */
	private static long seed = System.currentTimeMillis();
	/** SNR limite ambiente noise */
	private static double limiteSNR = 10;


	public static void addNode( int i, Vector routers, RouteAodv.AodvStats stats,
			Field field, Placement place, RadioInfo.RadioInfoShared radioInfo, Mapper protMap,
			PacketLoss inLoss, PacketLoss outLoss)
	{
		// radio
		RadioNoiseImprovedIndep r = new RadioNoiseImprovedIndep(i, radioInfo);
		r.setThresholdSNR(limiteSNR);
		RadioNoise radio =r; 

		// mac
		MacDumb mac = new MacDumb(new MacAddress(i), radio.getRadioInfo());

		// network
		final NetAddress address = new NetAddress(i);
		NetIp net = new NetIp(address, protMap, inLoss, outLoss, field.getTrace());

		// routing
		RouteInterface route = null;
		switch(protocol)
		{
		case Constants.NET_PROTOCOL_AODV:
			RouteAodv aodv = new RouteAodv(address);
			aodv.setNetEntity(net.getProxy());
			aodv.getProxy().start();      
			route = aodv.getProxy();
			routers.add(aodv);
			// statistics
			aodv.setStats(stats);
			break;
		default:
			throw new RuntimeException("invalid routing protocol");
		}

		// transport
		TransUdp udp = new TransUdp();

		// placement
		Location location = place.getNextLocation();
		field.addRadio(radio.getRadioInfo(), radio.getProxy(), location);
		field.startMobility(radio.getRadioInfo().getUnique().getID());

		// node entity hookup
		radio.setFieldEntity(field.getProxy());
		radio.setMacEntity(mac.getProxy());
		byte intId = net.addInterface(mac.getProxy());
		net.setRouting(route);
		mac.setRadioEntity(radio.getProxy());
		mac.setNetEntity(net.getProxy(), intId);
		udp.setNetEntity(net.getProxy());
		net.setProtocolHandler(Constants.NET_PROTOCOL_UDP, udp.getProxy());
		net.setProtocolHandler(protocol, route);
	}  //method: addNode


	private static Field buildField(final Vector routers, final RouteAodv.AodvStats stats)
	{
		// initialize node mobility model
		Mobility mobility = null;
		switch(mobilityModel)
		{
		case Constants.MOBILITY_STATIC:
			mobility = new Mobility.Static();
			break;
		case Constants.MOBILITY_WAYPOINT:
			mobility = new Mobility.RandomWaypoint(field, mobilityOpts);
			break;
		case Constants.MOBILITY_UNIFORM_CIRCLE:
			mobility = new Mobility.UniformCircular(field, mobilityOpts,nodes);
			break;
		case Constants.MOBILITY_UNIFORM_RECT:
			mobility = new Mobility.UniformRectagular(field, mobilityOpts,nodes);
			break;
		case Constants.MOBILITY_UNIFORM_RECT_NOMANDE:
			mobility = new Mobility.NomadicRectgular(field, mobilityOpts,nodes);
			break;
		case Constants.MOBILITY_UNIFORM_CIRCLE_NOMANDE:
			mobility = new Mobility.NomadicCircular(field, mobilityOpts,nodes);
			break;


		default:
			throw new RuntimeException("unknown node mobility model");
		}
		// initialize spatial binning
		Spatial spatial = null;
		spatial = new Spatial.LinearList(field);


		Field field = new Field(spatial, new Fading.Rayleigh(), new PathLoss.TwoRay(), 
				mobility, Constants.PROPAGATION_LIMIT_DEFAULT);
		// initialize shared radio information
		RadioInfo.RadioInfoShared radioInfo = RadioInfo.createShared(
				Constants.FREQUENCY_DEFAULT,
				Constants.BANDWIDTH_DEFAULT,
				Constants.TRANSMIT_DEFAULT,
				Constants.GAIN_DEFAULT,
				Util.fromDB(Constants.SENSITIVITY_DEFAULT),
				Util.fromDB(Constants.THRESHOLD_DEFAULT),
				Constants.TEMPERATURE_DEFAULT,
				Constants.TEMPERATURE_FACTOR_DEFAULT,
				Constants.AMBIENT_NOISE_DEFAULT);

		// initialize shared protocol mapper
		Mapper protMap = new Mapper(new int[] { Constants.NET_PROTOCOL_UDP, protocol, });
		// initialize packet loss models
		PacketLoss outLoss = new PacketLoss.Uniform(Double.parseDouble(lossOpts));
		PacketLoss inLoss = new PacketLoss.Uniform(Double.parseDouble(lossOpts));
		// initialize node placement model
		Placement place =  new Placement.Random(aodv.field);


		//If gui Support enabled, then draw trace. EMRE ATSAN

		//JAVIS -GUI SUPPORT
		JavisTrace.createTraceSetTrace(field,"aodvsim_"+nodes+"_"+mobilityString+"Snr_"+limiteSNR+"_NodeSim");


		FileOutputStream out; // declare a file output object
		// declare a print stream object


		// Create a new file output stream
		// connected to "myfile.txt"
		try {
			out = new FileOutputStream("aodvsim_"+nodes+"_"+mobilityString+"Snr_"+limiteSNR+"_NodeSim_stats_Aodv");
			statsfile = new PrintStream( out );

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		// create each node
		for (int i=1; i<=nodes; i++)
		{
			addNode( i, routers, stats, field, place, radioInfo, protMap, inLoss, outLoss);
		}


		JavisTrace.drawGuiTrace(field);



		// set up message sending events
		JistAPI.sleep(startTime*Constants.SECOND);
		//System.out.println("clear stats at t="+JistAPI.getTimeString());
		stats.clear();//1300/6
		int numTotalMessages = (int)Math.floor(((double)sendRate/60) * nodes * duration);
		long delayInterval = (long)Math.ceil((double)duration * (double)Constants.SECOND / (double)numTotalMessages);
		for(int i=0; i<numTotalMessages; i++)
		{
			//pick random send node
			int srcIdx = Constants.random.nextInt(routers.size());
			int destIdx;
			do
			{
				//pick random dest node
				destIdx = Constants.random.nextInt(routers.size());
			} while (destIdx == srcIdx);
			RouteAodv srcAodv = (RouteAodv)routers.elementAt(srcIdx);
			RouteAodv destAodv = (RouteAodv)routers.elementAt(destIdx);

			Message m = new MessageBytes(new byte[64]);

			TransUdp.UdpMessage udpMsg = new TransUdp.UdpMessage(PORT, PORT, m);

			NetMessage msg = new NetMessage.Ip(udpMsg, srcAodv.getLocalAddr(), destAodv.getLocalAddr(), 
					Constants.NET_PROTOCOL_UDP, Constants.NET_PRIORITY_NORMAL, Constants.TTL_DEFAULT);
			srcAodv.getProxy().send(msg);
			//stats
			if (stats != null)
			{
				stats.netMsgs++;
			}
			JistAPI.sleep(delayInterval);
		}

		return field;
	} // buildField


	/**
	 * Display statistics at end of simulation.
	 *
	 * @param routers vectors to place zrp objects into
	 * @param stats zrp statistics collection object
	 */
	public static void showStats(Field field,Vector routers, RouteAodv.AodvStats stats, Date startTime)
	{
		Date endTime = new Date();
		long elapsedTime = endTime.getTime() - startTime.getTime();
		
		System.err.println("-------------");   
		statsfile.println("-------------");
		System.err.println("Packet stats:");
		statsfile.println("Packet stats:");
		System.err.println("-------------");
		statsfile.println("-------------");

		System.err.println("Rreq packets sent = "+stats.send.rreqPackets);
		statsfile.println("Rreq packets sent = "+stats.send.rreqPackets);
		System.err.println("Rreq packets recv = "+stats.recv.rreqPackets);
		statsfile.println("Rreq packets recv = "+stats.recv.rreqPackets);

		System.err.println("Rrep packets sent = "+stats.send.rrepPackets);
		statsfile.println("Rrep packets sent = "+stats.send.rrepPackets);
		System.err.println("Rrep packets recv = "+stats.recv.rrepPackets);
		statsfile.println("Rrep packets recv = "+stats.recv.rrepPackets);

		System.err.println("Rerr packets sent = "+stats.send.rerrPackets);
		statsfile.println("Rerr packets sent = "+stats.send.rerrPackets);
		System.err.println("Rerr packets recv = "+stats.recv.rerrPackets);
		statsfile.println("Rerr packets recv = "+stats.recv.rerrPackets);

		System.err.println("Hello packets sent = "+stats.send.helloPackets);
		statsfile.println("Hello packets sent = "+stats.send.helloPackets);
		System.err.println("Hello packets recv = "+stats.recv.helloPackets);
		statsfile.println("Hello packets recv = "+stats.recv.helloPackets);

		System.err.println("Total aodv packets sent = "+stats.send.aodvPackets);
		statsfile.println("Total aodv packets sent = "+stats.send.aodvPackets);
		System.err.println("Total aodv packets recv = "+stats.recv.aodvPackets);
		statsfile.println("Total aodv packets recv = "+stats.recv.aodvPackets);


		System.err.println("Non-hello packets sent = "+(stats.send.aodvPackets - stats.send.helloPackets));
		statsfile.println("Non-hello packets sent = "+(stats.send.aodvPackets - stats.send.helloPackets));
		System.err.println("Non-hello packets recv = "+(stats.recv.aodvPackets - stats.recv.helloPackets));
		statsfile.println("Non-hello packets recv = "+(stats.recv.aodvPackets - stats.recv.helloPackets));

		System.err.println("--------------");
		statsfile.println("--------------");
		System.err.println("Overall stats:");
		statsfile.println("Overall stats:");
		System.err.println("--------------");
		statsfile.println("--------------");
		System.err.println("Messages to deliver = "+stats.netMsgs);
		statsfile.println("Messages to deliver = "+stats.netMsgs);
		System.err.println("Route requests      = "+stats.rreqOrig);
		statsfile.println("Route requests      = "+stats.rreqOrig);
		System.err.println("Route replies       = "+stats.rrepOrig);
		statsfile.println("Route replies       = "+stats.rrepOrig);
		System.err.println("Routes added        = "+stats.rreqSucc);
		statsfile.println("Routes added        = "+stats.rreqSucc);

		System.err.println();
		statsfile.println();
		System.gc();
		System.err.println("freemem:  "+Runtime.getRuntime().freeMemory());
		statsfile.println("freemem:  "+Runtime.getRuntime().freeMemory());
		System.err.println("maxmem:   "+Runtime.getRuntime().maxMemory());
		statsfile.println("maxmem:   "+Runtime.getRuntime().maxMemory());
		System.err.println("totalmem: "+Runtime.getRuntime().totalMemory());
		statsfile.println("totalmem: "+Runtime.getRuntime().totalMemory());
		long usedMem = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
		System.err.println("used:     "+usedMem);
		statsfile.println("used:     "+usedMem);

		System.err.println("start time  : "+startTime);
		statsfile.println("start time  : "+startTime);
		System.err.println("end time    : "+endTime);
		statsfile.println("end time    : "+endTime);
		System.err.println("elapsed time: "+elapsedTime);
		statsfile.println("elapsed time: "+elapsedTime);
		System.err.flush();
		statsfile.flush();

		System.out.println(nodes+"\t"
				+stats.send.rreqPackets+"\t"
				+stats.recv.rreqPackets+"\t"
				+stats.send.rrepPackets+"\t"
				+stats.recv.rrepPackets+"\t"
				+stats.send.rerrPackets+"\t"
				+stats.recv.rerrPackets+"\t"
				+stats.send.helloPackets+"\t"
				+stats.recv.helloPackets+"\t"
				+stats.send.aodvPackets+"\t"
				+stats.recv.aodvPackets+"\t"
				+(stats.send.aodvPackets - stats.send.helloPackets)+"\t"
				+(stats.recv.aodvPackets - stats.recv.helloPackets)+"\t"
				+usedMem+"\t"
				+elapsedTime);

		statsfile.println(nodes+"\t"
				+stats.send.rreqPackets+"\t"
				+stats.recv.rreqPackets+"\t"
				+stats.send.rrepPackets+"\t"
				+stats.recv.rrepPackets+"\t"
				+stats.send.rerrPackets+"\t"
				+stats.recv.rerrPackets+"\t"
				+stats.send.helloPackets+"\t"
				+stats.recv.helloPackets+"\t"
				+stats.send.aodvPackets+"\t"
				+stats.recv.aodvPackets+"\t"
				+(stats.send.aodvPackets - stats.send.helloPackets)+"\t"
				+(stats.recv.aodvPackets - stats.recv.helloPackets)+"\t"
				+usedMem+"\t"
				+elapsedTime);

		//clear memory
		routers = null;
		stats = null;

		System.out.println("Average density = " + field.computeDensity()  + "/m^2");
		statsfile.println("Average density = " + field.computeDensity()  + "/m^2");
		System.out.println("Average sensing = " + field.computeAvgConnectivity(true));
		statsfile.println("Average sensing = " + field.computeAvgConnectivity(true));
		System.out.println("Average receive = " + field.computeAvgConnectivity(false));
		statsfile.println("Average receive = " + field.computeAvgConnectivity(false));
		statsfile.close();
	}

	/**
	 * Main entry point.
	 *
	 * @param args command-line arguments 
	 */  
	public static void main(String[] args)
	{

		long endTime = startTime+duration+resolutionTime;
		if(endTime>0)
		{
			JistAPI.endAt(endTime*Constants.SECOND);
		}
		Constants.random = new Random(seed);
		final Vector routers = new Vector();
		final RouteAodv.AodvStats stats = new RouteAodv.AodvStats();
		final Date startTime = new Date();
		final Field field = buildField(routers, stats);



		JistAPI.runAt(new Runnable()
		{
			public void run()
			{
				//WAIT A LITTLE FILE WRITE IS COMPLETE
				long filewait = 15;
				try {
					Thread.sleep(filewait);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


				showStats(field,routers, stats, startTime);
			}
		}, JistAPI.END);



	}

}
