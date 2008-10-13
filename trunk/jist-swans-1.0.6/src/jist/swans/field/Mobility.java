
//JIST (Java In Simulation Time) Project
//Timestamp: <Mobility.java Sun 2005/03/13 11:02:59 barr rimbase.rimonbarr.com>


//Copyright (C) 2004 by Cornell University
//All rights reserved.
//Refer to LICENSE for terms and conditions of use.

package jist.swans.field;


import java.util.ArrayList;
import java.util.Iterator;

import jist.swans.field.Field.RadioData;
import jist.swans.misc.Location;
import jist.swans.misc.Util;
import jist.swans.misc.Location.Location2D;
import jist.swans.Constants;
import jist.swans.Main;

import jist.runtime.JistAPI;

/** 
 * Interface of all mobility models.
 *
 * @author Rimon Barr &lt;barr+jist@cs.cornell.edu&gt;
 * @version $Id: Mobility.java,v 1.22 2005/03/13 16:11:54 barr Exp $
 * @since SWANS1.0
 */
public interface Mobility
{

	/**
	 * Initiate mobility; initialize mobility data structures.
	 *
	 * @param f field entity
	 * @param id node identifier
	 * @param loc node location
	 * @return mobility information object
	 */
	MobilityInfo init(FieldInterface f, Integer id, Location loc);

	/**
	 * Schedule next movement. This method will again be called after every
	 * movement on the field.
	 *
	 * @param f field entity
	 * @param id radio identifier
	 * @param loc destination of move
	 * @param info mobility information object
	 */
	void next(FieldInterface f, Integer id, Location loc, MobilityInfo info);


	//////////////////////////////////////////////////
	// mobility information
	//

	/**
	 * Interface of algorithm-specific mobility information objects.
	 *
	 * @author Rimon Barr &lt;barr+jist@cs.cornell.edu&gt;
	 * @since SWANS1.0
	 */

	public static interface MobilityInfo
	{
		/** The null MobilityInfo object. */
		MobilityInfo NULL = new MobilityInfo()
		{
		};
	}

	//////////////////////////////////////////////////
	// uniform mobility model
	//
	public static class UniformCircularInfo implements MobilityInfo
	{

		public double velocity;
		public double direction;
		public double distance;
		private double m1;
		public UniformCircularInfo(){

		}
		public UniformCircularInfo(double k1,double k2,double n){
			direction = 2*Math.PI*Constants.random.nextDouble();
			velocity = k1/Math.sqrt(n);  
			m1 = k2/n;
			distance = Constants.exprnd(m1);
			// System.out.println("costrutor");
		}
		public void renew(){
			distance = Constants.exprnd(m1);
			direction = 2*Math.PI*Constants.random.nextDouble();
		}


		public UniformCircularInfo clone(){
			UniformCircularInfo info = new UniformCircularInfo();
			info.direction = this.direction;
			info.distance=this.distance;
			info.velocity = this.velocity;
			return info;

		}



	} // class: BoundlessSimulationAreaInfo 

	public static class UniformCircular implements Mobility
	{

		private double k1,k2;
		private int n;
		private double R;

		public UniformCircular(){

		}

		public UniformCircular(Location bounds, String config,int n)
		{

			String ksConfigOptions [];
			ksConfigOptions= config.split(":");
			R =bounds.getX();
			k1 = Double.parseDouble(ksConfigOptions[0]);
			k2 =Double.parseDouble(ksConfigOptions[1]);
			this.n = n;

		}



		public MobilityInfo init(FieldInterface f, Integer id, Location loc) {	
			//  System.out.println("cheguei"+k1+" - "+k2+" - " +n );
			return new UniformCircularInfo(k1,k2,n);
		}
		private double getAngulo(double x, double y){
			double a = Math.atan(y/x);

			if ((x<0 & y>0) | (x<0 & y<0))
				a = Math.PI + a;
			else if (x>0 & y<0)
				a = 2*Math.PI + a;
			return a;
		}

		private double refletir(double aAnterior, double rAnterior,double aUltimo,double xUltimo,double yUltimo){
			double teta,anguloRefletido;
			if(aAnterior>aUltimo){
				teta = aAnterior-aUltimo;
				anguloRefletido = aUltimo - teta;
			}else{
				teta =aUltimo - aAnterior;
				anguloRefletido = aUltimo + teta;
			}
			double xRefletido = rAnterior*Math.cos(anguloRefletido);
			double yRefletido = rAnterior*Math.sin(anguloRefletido);

			double x = xRefletido - xUltimo;
			double y = yRefletido - yUltimo;

			return getAngulo(x, y);
		}
		public void next(FieldInterface f, Integer id, Location loc, MobilityInfo info) {
//			if(id>5){


			UniformCircularInfo uinfo = (UniformCircularInfo)info;
			//  System.out.println("next|"+id+"|"+uinfo.distance+"");


			double anteLocX = loc.getX()-R;
			double anteLocY = loc.getY()-R;
			double anteLocA = getAngulo(anteLocX, anteLocY);
			double anteLocR = anteLocX/Math.cos(anteLocA);
			double anteLocD = uinfo.direction;

			double nextLocX = anteLocX + uinfo.velocity*Math.cos(uinfo.direction);
			double nextLocY = anteLocY + uinfo.velocity*Math.sin(uinfo.direction);
			double nextLocA =getAngulo(nextLocX, nextLocY);
			double nextLocR = nextLocX/Math.cos(nextLocA);
			double nextLocD = uinfo.direction;

			uinfo.distance -=uinfo.velocity ;
			while(nextLocR>R){
				//  System.out.println("teste");
				double d=Math.sqrt(Math.pow(anteLocX-nextLocX,2)+Math.pow(anteLocY-nextLocY,2));
				double da= anteLocR;;
				double  d0 = nextLocR;
				double  a = d;
				double  b = Math.pow(d0,2) - Math.pow(da,2) - Math.pow(d,2);
				double  c = Math.pow(da,2)*d - Math.pow(R,2)*d;
				double  delta = Math.pow(b,2) - 4*a*c;
				double  d_ = Math.abs((-b + Math.sqrt(delta))/(2*a));

				double UltimoLocX = anteLocX + d_*Math.cos(anteLocD);
				double UltimoLocY = anteLocY + d_*Math.sin(anteLocD);
				double UltimoLocA =getAngulo(UltimoLocX, UltimoLocY);
				double UltimoLocR = UltimoLocX/Math.cos(UltimoLocA);
				double UltimoLocD = refletir( anteLocA, anteLocR,UltimoLocA,UltimoLocX,UltimoLocY);

				double dist = Math.sqrt(Math.pow(nextLocX - UltimoLocX, 2) + Math.pow(nextLocY - UltimoLocY, 2));
				nextLocX = UltimoLocX + dist*Math.cos(UltimoLocD) ;
				nextLocY = UltimoLocY + dist*Math.sin(UltimoLocD) ;
				nextLocA = getAngulo(nextLocX, nextLocY);
				nextLocR = nextLocX/Math.cos(nextLocA);
				nextLocD = UltimoLocD;

				anteLocX = UltimoLocX;
				anteLocY = UltimoLocY;
				anteLocA= UltimoLocA;
				anteLocR = UltimoLocR;
				anteLocD = UltimoLocD;


			}

			uinfo.direction = nextLocD;
			JistAPI.sleep(1*Constants.SECOND);


			f.moveRadio(id,new Location.Location2D((float)(nextLocX+R),(float)(nextLocY+R)));
			if(uinfo.distance<0)
				uinfo.renew();

		}
	}

	public static class UniformRectagularInfo implements MobilityInfo
	{
		public double direction;
		public double distance;
		public double distanceCurr;
		public int steps;
		public int stepsCurr;
		public double stepTime;
		public double velocity;
		public double velocityMax;
		public double velocityMin;
		private double m1;
		public UniformRectagularInfo(){

		}
		public UniformRectagularInfo(double Vmin,double Vmax,double k2,int steps,int n){
			velocityMin =Vmin;
			velocityMax = Vmax;
			this.steps=steps;
			direction = 2*Math.PI*Constants.random.nextDouble();
			velocity =  velocityMin + (velocityMax -velocityMin)*Constants.random.nextDouble(); // speedmin+(speedmax - speedmin)*rand
			
			m1 = k2/n;
			distance = Constants.exprnd(m1);
			distanceCurr = distance;
			
			double timeTotal = distance/velocity;
			this.stepTime =timeTotal/steps;
			stepsCurr = steps;
			
			
		}
		public void renew(){
			direction = 2*Math.PI*Constants.random.nextDouble();
			distance = Constants.exprnd(m1);
			distanceCurr = distance;
			velocity =  velocityMin + (velocityMax -velocityMin)*Constants.random.nextDouble();
			double timeTotal = distance/velocity;
			this.stepTime = (timeTotal/steps);
			stepsCurr = steps;
		}


	}
	public static class UniformRectagular implements Mobility
	{

		private double Vmax,Vmin,k2;
		private int n,steps;
		private Location.Location2D bounds;
		public UniformRectagular(Location.Location2D bounds,String config, int n){
			String ksConfigOptions [];
			ksConfigOptions= config.split(":");
			Vmin = Double.parseDouble(ksConfigOptions[0]);
			Vmax = Double.parseDouble(ksConfigOptions[1]);
			k2 =Double.parseDouble(ksConfigOptions[2]);
			steps =Integer.parseInt(ksConfigOptions[3]);
			this.n = n;
			this.bounds = bounds;
		}

		public MobilityInfo init(FieldInterface f, Integer id, Location loc) {

			return new UniformRectagularInfo(Vmin,Vmax,k2,steps,n);
		}

		public void next(FieldInterface f, Integer id, Location loc, MobilityInfo info) {
			UniformRectagularInfo uinfo = (UniformRectagularInfo)info;
			//Location locAnterior = loc;
			double stepDist = uinfo.velocity*uinfo.stepTime;
			double newX = loc.getX()+ stepDist*Math.cos(uinfo.direction);
			double newY = loc.getY()+ stepDist*Math.sin(uinfo.direction);
			while(newX<0 || newX>bounds.getX() || newY<0 || newY>bounds.getY()){
				double deltaXExt = newX-loc.getX();
				double deltaYExt = newY-loc.getY();
				double a = deltaYExt/deltaXExt;
				double b =  deltaYExt - (a*deltaXExt);
				/* equaçao da reta;
		        y = ax + b
	            x = (y - b)/a
				 */
				Location2D lastPoint = null,reflexPoint=null;
				double deltaXInt, deltaYInt; 

				if(newY<0){
					lastPoint = new Location2D((float)((0 - b)/a),0); 
					reflexPoint = new Location2D((float)newX,(float)(-1*newY)); 
					newY = -1*newY;
					//////new direction after reflection
					if(uinfo.direction>3*Math.PI/2 && uinfo.direction<2*Math.PI){

						deltaXInt = reflexPoint.getX() - lastPoint.getX();
						deltaYInt = reflexPoint.getY() - lastPoint.getY();; 
						uinfo.direction =  Math.atan(deltaYInt/deltaXInt);
					}
					if(uinfo.direction>Math.PI && uinfo.direction<3*Math.PI/2){

						deltaXInt = reflexPoint.getX() - lastPoint.getX();
						deltaYInt = reflexPoint.getY() - lastPoint.getY();; 
						uinfo.direction = Math.PI + Math.atan(deltaYInt/deltaXInt);
					}


				}else if(newY>bounds.getY()){
					lastPoint = new Location2D((float)((bounds.getY() - b)/a),bounds.getY()); 
					reflexPoint = new Location2D((float)newX,(float)(2*bounds.getY() - newY)); 
					newY = 2*bounds.getY() - newY;
					//////new direction after reflection
					if(uinfo.direction>0 && uinfo.direction<Math.PI/2){

						deltaXInt = reflexPoint.getX() - lastPoint.getX();
						deltaYInt = reflexPoint.getY() - lastPoint.getY();; 
						uinfo.direction =  2*Math.PI + Math.atan(deltaYInt/deltaXInt);
					}
					if(uinfo.direction>Math.PI/2 && uinfo.direction<3*Math.PI){
						deltaXInt = reflexPoint.getX() - lastPoint.getX();
						deltaYInt = reflexPoint.getY() - lastPoint.getY();; 
						uinfo.direction = Math.PI + Math.atan(deltaYInt/deltaXInt);
					}
				}
				else if(newX<0){
					lastPoint = new Location2D(0,(float)(a*0 + b)); 
					reflexPoint = new Location2D((float)(-1*newX),(float)newY); 
					newX = -1*newX;
					//////new direction after reflection
					if(uinfo.direction>Math.PI && uinfo.direction<3*Math.PI/2){

						deltaXInt = reflexPoint.getX() - lastPoint.getX();
						deltaYInt = reflexPoint.getY() - lastPoint.getY();; 
						uinfo.direction = 2*Math.PI + Math.atan(deltaYInt/deltaXInt);
					}
					if(uinfo.direction>Math.PI/2 && uinfo.direction<Math.PI){

						deltaXInt = reflexPoint.getX() - lastPoint.getX();
						deltaYInt = reflexPoint.getY() - lastPoint.getY();; 
						uinfo.direction = Math.atan(deltaYInt/deltaXInt);
					}

				}else{
					lastPoint = new Location2D(bounds.getX(),(float)(a*bounds.getX() + b)); 
					reflexPoint = new Location2D((float)(2*bounds.getX() - newX),(float)newY); 
					newX = 2*bounds.getX() - newX;
					//////new direction after reflection
					if(uinfo.direction>0 && uinfo.direction<Math.PI/2){

						deltaXInt = reflexPoint.getX() - lastPoint.getX();
						deltaYInt = reflexPoint.getY() - lastPoint.getY();; 
						uinfo.direction = Math.PI + Math.atan(deltaYInt/deltaXInt);
					}
					if(uinfo.direction>3*Math.PI/2 && uinfo.direction<2*Math.PI){

						deltaXInt = reflexPoint.getX() - lastPoint.getX();
						deltaYInt = reflexPoint.getY() - lastPoint.getY();; 
						uinfo.direction = Math.PI  + Math.atan(deltaYInt/deltaXInt);
					}


				}


			}
			uinfo.stepsCurr--;
			Location2D newLoc = new Location2D((float)newX,(float)newY);
//			if(id==1 )
//		  {
//				System.err.println((long)(uinfo.stepTime*Constants.SECOND));
//		  }
////			long t=(long)uinfo.stepTime*Constants.SECOND;
////			if(t!=0)
			JistAPI.sleep((long)(uinfo.stepTime*Constants.SECOND));
			
			f.moveRadio(id,newLoc);
			if(uinfo.stepsCurr<=0){
				uinfo.renew();
			}

		}

	}

	public static class NomadicCircularInfo implements MobilityInfo
	{
		public double direction;
		public double distance;
		public double velocity;
		public int tipo = 3;
		private Location loc;
		public ArrayList<Integer> nodesInternos=new ArrayList<Integer>();;
		private double m1;

		public NomadicCircularInfo(double k1,double k2,int n)
		{
			m1 = k2/n;
			distance = Constants.exprnd(m1);
			velocity = k1/Math.sqrt(n);

		}

		public void renew(){
			direction = 2*Math.PI*Constants.random.nextDouble();
			distance = Constants.exprnd(m1);
		}

	}

	public static class NomadicCircular implements Mobility{

		private static int qtdpointReference;
		private static int jaqtdpointReference=0;
		private NomadicCircularInfo []heads ;
		private int pauseTime;
		private double raioComunity;
		private double k2,k1,R;
		private int n;

		public NomadicCircular(Location.Location2D bounds,String config, int n){
			String ksConfigOptions [];
			ksConfigOptions= config.split(":");
			k1 = Double.parseDouble(ksConfigOptions[0]);
			k2 =Double.parseDouble(ksConfigOptions[1]);
			this.n = n;
			R = bounds.getX();
			this.qtdpointReference = Integer.parseInt(ksConfigOptions[2]);
			heads = new NomadicCircularInfo[qtdpointReference];
			this.raioComunity = Double.parseDouble(ksConfigOptions[3]);
			this.pauseTime = Integer.parseInt(ksConfigOptions[4]);;  
		} 


		public MobilityInfo init(FieldInterface f, Integer id, Location loc) {
			NomadicCircularInfo info;



			if(jaqtdpointReference<qtdpointReference){

				info = new NomadicCircularInfo(k1,k2,n);
				info.tipo = 1;
				info.loc = loc;
				heads[jaqtdpointReference] = info;
				jaqtdpointReference++;
			}
			else{
				info = new NomadicCircularInfo(k1,k2,n);
				for (NomadicCircularInfo i : heads) {
					if(inside(i.loc.getX(),i.loc.getY(),loc.getX(),loc.getY(),raioComunity))
					{
						i.nodesInternos.add(id);
						info.tipo = 2;
					}

				}
			}


			return info;
		}
		private double getAngulo(double x, double y){
			double a = Math.atan(y/x);

			if ((x<0 & y>0) | (x<0 & y<0))
				a = Math.PI + a;
			else if (x>0 & y<0)
				a = 2*Math.PI + a;
			return a;
		}

		private double refletir(double aAnterior, double rAnterior,double aUltimo,double xUltimo,double yUltimo){
			double teta,anguloRefletido;
			if(aAnterior>aUltimo){
				teta = aAnterior-aUltimo;
				anguloRefletido = aUltimo - teta;
			}else{
				teta =aUltimo - aAnterior;
				anguloRefletido = aUltimo + teta;
			}
			double xRefletido = rAnterior*Math.cos(anguloRefletido);
			double yRefletido = rAnterior*Math.sin(anguloRefletido);

			double x = xRefletido - xUltimo;
			double y = yRefletido - yUltimo;

			return getAngulo(x, y);
		}
		public boolean inside(double xHead,double yHead,double xNode,double yNode, double raio){
			xNode = xNode- xHead;
			yNode = yNode- yHead;
			double aNode = getAngulo(xNode, yNode);
			double rNode = xNode/Math.cos(aNode);
			if (rNode <= raio)
				return true;
			else
				return false;

		}
		public void next(FieldInterface f, Integer id, Location loc, MobilityInfo info) {

			NomadicCircularInfo noinfo = (NomadicCircularInfo) info;

			if(id>=1 && id<=qtdpointReference){
				// movimente o grupo correspondente;
				JistAPI.sleep(pauseTime*Constants.SECOND);
				Location oldLoc = loc;
				Location newLoc = moveRadio(f, id, loc, noinfo);
				double deltax =  newLoc.getX() - oldLoc.getX();
				double deltay =  newLoc.getY() - oldLoc.getY(); 
				noinfo.loc = newLoc;
				for (Iterator iter = noinfo.nodesInternos.iterator(); iter.hasNext();) {
					Integer nointerno = (Integer) iter.next();
					f.moveRadioOff(nointerno, new Location.Location2D((float)deltax,(float)deltay));

				}
			}else{
				JistAPI.sleep(1*Constants.SECOND);
				moveRadio(f, id, loc, noinfo);

			}

		}

		private NomadicCircularInfo getHeadReference(Integer id){
			for (int i = 0; i < heads.length; i++) {
				NomadicCircularInfo element = heads[i];
				if (element.nodesInternos.contains(id))
					return element;
			}
			return null;
		}

		private Location2D moveRadio(FieldInterface f, Integer id, Location loc, NomadicCircularInfo uinfo) {
			double xnode = loc.getX()-this.R;
			double ynode = loc.getY()-this.R;
			double R = this.R;
			double xHead = 0;
			double yHead =0;
			NomadicCircularInfo headinfo = getHeadReference(id);
			
			if (uinfo.tipo == 1){
				R -=raioComunity;

			}
			if(uinfo.tipo==2 && headinfo!=null){
				xHead = headinfo.loc.getX();
				yHead = headinfo.loc.getY();				
				xnode = loc.getX()-headinfo.loc.getX();
				ynode = loc.getY()-headinfo.loc.getY();
				R = raioComunity;
			}


			double anteLocX = xnode;
			double anteLocY = ynode;
			double anteLocA = getAngulo(anteLocX, anteLocY);
			double anteLocR = anteLocX/Math.cos(anteLocA);
			double anteLocD = uinfo.direction;

			double nextLocX = anteLocX + uinfo.velocity*Math.cos(uinfo.direction);
			double nextLocY = anteLocY + uinfo.velocity*Math.sin(uinfo.direction);
			double nextLocA =getAngulo(nextLocX, nextLocY);
			double nextLocR = nextLocX/Math.cos(nextLocA);
			double nextLocD = uinfo.direction;

			uinfo.distance -=uinfo.velocity ;
			while(nextLocR>R){
				//  System.out.println("teste");
				double d=Math.sqrt(Math.pow(anteLocX-nextLocX,2)+Math.pow(anteLocY-nextLocY,2));
				double da= anteLocR;;
				double  d0 = nextLocR;
				double  a = d;
				double  b = Math.pow(d0,2) - Math.pow(da,2) - Math.pow(d,2);
				double  c = Math.pow(da,2)*d - Math.pow(R,2)*d;
				double  delta = Math.pow(b,2) - 4*a*c;
				double  d_ = Math.abs((-b + Math.sqrt(delta))/(2*a));

				double UltimoLocX = anteLocX + d_*Math.cos(anteLocD);
				double UltimoLocY = anteLocY + d_*Math.sin(anteLocD);
				double UltimoLocA =getAngulo(UltimoLocX, UltimoLocY);
				double UltimoLocR = UltimoLocX/Math.cos(UltimoLocA);
				double UltimoLocD = refletir( anteLocA, anteLocR,UltimoLocA,UltimoLocX,UltimoLocY);

				double dist = Math.sqrt(Math.pow(nextLocX - UltimoLocX, 2) + Math.pow(nextLocY - UltimoLocY, 2));
				nextLocX = UltimoLocX + dist*Math.cos(UltimoLocD) ;
				nextLocY = UltimoLocY + dist*Math.sin(UltimoLocD) ;
				nextLocA = getAngulo(nextLocX, nextLocY);
				nextLocR = nextLocX/Math.cos(nextLocA);
				nextLocD = UltimoLocD;

				anteLocX = UltimoLocX;
				anteLocY = UltimoLocY;
				anteLocA= UltimoLocA;
				anteLocR = UltimoLocR;
				anteLocD = UltimoLocD;


			}

			uinfo.direction = nextLocD;
			Location2D newLoc = new Location2D((float)(nextLocX+xHead+this.R),(float)(nextLocY+yHead+this.R));
		
			f.moveRadio(id,newLoc);
			if(uinfo.distance<0){
				uinfo.renew();
			}

			return newLoc;

		}
	}
	public static class NomadicRectgularInfo implements MobilityInfo
	{
		public double direction;
		public double distance;
		public double velocity;
		public Location loc;
		public boolean ishead=false;
		public int tipo = 3;
		public ArrayList<Integer> nodesInternos=new ArrayList<Integer>();;
		private double m1;

		public NomadicRectgularInfo(double k1,double k2,int n,boolean ishead){
			direction = 2*Math.PI*Constants.random.nextDouble();

			m1 = k2/n;
			this.ishead = ishead;
			distance = Constants.exprnd(m1);
			if(this.ishead)
				velocity = 10*k1/Math.sqrt(n);
			else
				velocity = 0.1*k1/Math.sqrt(n);
		}
		public NomadicRectgularInfo(){

		}
		public void renew(){
			direction = 2*Math.PI*Constants.random.nextDouble();
			distance = Constants.exprnd(m1);
		}
		public NomadicRectgularInfo clone(){
			NomadicRectgularInfo info = new NomadicRectgularInfo();
			info.direction = this.direction;
			info.distance = this.distance;
			info.velocity = this.velocity;
			info.m1 = this.m1;
			return info;

		}

	}


	public static class NomadicRectgular implements Mobility{

		private static int qtdpointReference;
		private static int jaqtdpointReference=0;
		private NomadicRectgularInfo []heads ;
		private int pauseTime;
		private double k1,k2;
		private int n;
		private double raioComunity;
		private Location.Location2D bounds;



		public NomadicRectgular(Location.Location2D bounds,String config, int n){
			String ksConfigOptions [];
			ksConfigOptions= config.split(":");
			k1 = Double.parseDouble(ksConfigOptions[0]);
			k2 =Double.parseDouble(ksConfigOptions[1]);
			this.n = n;
			this.bounds = bounds;
			this.qtdpointReference = Integer.parseInt(ksConfigOptions[2]);
			heads = new NomadicRectgularInfo[qtdpointReference];
			this.raioComunity = Double.parseDouble(ksConfigOptions[3]);
			this.pauseTime = Integer.parseInt(ksConfigOptions[4]);;  
		} 
		public MobilityInfo init(FieldInterface f, Integer id, Location loc) {

			NomadicRectgularInfo info;



			if(jaqtdpointReference<qtdpointReference){

				info = new NomadicRectgularInfo(k1,k2,n,true);
				info.loc = loc;
				info.tipo = 1;
				heads[jaqtdpointReference] = info;
				jaqtdpointReference++;
			}
			else{
				info = new NomadicRectgularInfo(k1,k2,n,false);
				for (NomadicRectgularInfo i : heads) {
					if(loc.inside(i.loc, new Location.Location2D((float)(i.loc.getX()+raioComunity),(float)(i.loc.getY()+raioComunity))))
					{
						i.nodesInternos.add(id);
						info.tipo = 2;
					}

				}
			}


			return info;
		}

		public void next(FieldInterface f, Integer id, Location loc, MobilityInfo info) {


			NomadicRectgularInfo noinfo = (NomadicRectgularInfo) info;

			if(id>=1 && id<=qtdpointReference){
				// movimente o grupo correspondente;
				JistAPI.sleep(pauseTime*Constants.SECOND);
				Location oldLoc = loc;
				Location newLoc = moveRadio(f, id, loc, noinfo);
				double deltax =  newLoc.getX() - oldLoc.getX();
				double deltay = newLoc.getY() -  oldLoc.getY(); 
				noinfo.loc = newLoc;
				for (Iterator iter = noinfo.nodesInternos.iterator(); iter.hasNext();) {
					Integer nointerno = (Integer) iter.next();
					f.moveRadioOff(nointerno, new Location.Location2D((float)deltax,(float)deltay));

				}
			}else{
				JistAPI.sleep(1*Constants.SECOND);
				moveRadio(f, id, loc, noinfo);

			}


			//moveRadio(f, id, loc, noinfo);






		}
		private NomadicRectgularInfo getHeadReference(Integer id){
			for (int i = 0; i < heads.length; i++) {
				NomadicRectgularInfo element = heads[i];
				if (element.nodesInternos.contains(id))
					return element;
			}
			return null;
		}
		private Location2D moveRadio(FieldInterface f, Integer id, Location loc, NomadicRectgularInfo uinfo) {
			NomadicRectgularInfo headinfo = getHeadReference(id);
			double xnode = loc.getX();
			double ynode = loc.getY();
			double X = bounds.getX();
			double Y = bounds.getY();
			if (uinfo.tipo == 1){
				X = bounds.getX()-raioComunity;
				Y = bounds.getY()-raioComunity;
			}
			if(X<0 || Y<0)
				throw new Error("Largura do raio do grupo deve ser menor!");
			double X_mov_rel = 0;
			double Y_mov_rel = 0;
			if(headinfo!=null){
				xnode = loc.getX()-headinfo.loc.getX();
				ynode = loc.getY()-headinfo.loc.getY();
				X = raioComunity;
				Y = raioComunity;
				X_mov_rel = headinfo.loc.getX();
				Y_mov_rel = headinfo.loc.getY();
			}

			double newX = xnode + uinfo.velocity*Math.cos(uinfo.direction);
			double newY = ynode + uinfo.velocity*Math.sin(uinfo.direction);
			while(newX<0 || newX>X || newY<0 || newY>Y){
				double deltaXExt = newX-xnode;
				double deltaYExt = newY-ynode;
				double a = deltaYExt/deltaXExt;
				double b =  deltaYExt - (a*deltaXExt);
				/* equaçao da reta;
		    y = ax + b
		    x = (y - b)/a
				 */
				Location2D lastPoint = null,reflexPoint=null;
				double deltaXInt, deltaYInt; 

				if(newY<0){
					lastPoint = new Location2D((float)((0 - b)/a),0); 
					reflexPoint = new Location2D((float)newX,(float)(-1*newY)); 
					newY = -1*newY;
					//////new direction after reflection
					if(uinfo.direction>3*Math.PI/2 && uinfo.direction<2*Math.PI){

						deltaXInt = reflexPoint.getX() - lastPoint.getX();
						deltaYInt = reflexPoint.getY() - lastPoint.getY();; 
						uinfo.direction =  Math.atan(deltaYInt/deltaXInt);
					}
					if(uinfo.direction>Math.PI && uinfo.direction<3*Math.PI/2){

						deltaXInt = reflexPoint.getX() - lastPoint.getX();
						deltaYInt = reflexPoint.getY() - lastPoint.getY();; 
						uinfo.direction = Math.PI + Math.atan(deltaYInt/deltaXInt);
					}


				}else if(newY>Y){
					lastPoint = new Location2D((float)((Y - b)/a),(float)Y); 
					reflexPoint = new Location2D((float)newX,(float)(2*Y - newY)); 
					newY = 2*Y - newY;
					//////new direction after reflection
					if(uinfo.direction>0 && uinfo.direction<Math.PI/2){

						deltaXInt = reflexPoint.getX() - lastPoint.getX();
						deltaYInt = reflexPoint.getY() - lastPoint.getY();; 
						uinfo.direction =  2*Math.PI + Math.atan(deltaYInt/deltaXInt);
					}
					if(uinfo.direction>Math.PI/2 && uinfo.direction<3*Math.PI){
						deltaXInt = reflexPoint.getX() - lastPoint.getX();
						deltaYInt = reflexPoint.getY() - lastPoint.getY();; 
						uinfo.direction = Math.PI + Math.atan(deltaYInt/deltaXInt);
					}
				}
				else if(newX<0){
					lastPoint = new Location2D(0,(float)(a*0 + b)); 
					reflexPoint = new Location2D((float)(-1*newX),(float)newY); 
					newX = -1*newX;
					//////new direction after reflection
					if(uinfo.direction>Math.PI && uinfo.direction<3*Math.PI/2){

						deltaXInt = reflexPoint.getX() - lastPoint.getX();
						deltaYInt = reflexPoint.getY() - lastPoint.getY();; 
						uinfo.direction = 2*Math.PI + Math.atan(deltaYInt/deltaXInt);
					}
					if(uinfo.direction>Math.PI/2 && uinfo.direction<Math.PI){

						deltaXInt = reflexPoint.getX() - lastPoint.getX();
						deltaYInt = reflexPoint.getY() - lastPoint.getY();; 
						uinfo.direction = Math.atan(deltaYInt/deltaXInt);
					}

				}else{
					lastPoint = new Location2D((float)X,(float)(a*X + b)); 
					reflexPoint = new Location2D((float)(2*X - newX),(float)newY); 
					newX = 2*X - newX;
					//////new direction after reflection
					if(uinfo.direction>0 && uinfo.direction<Math.PI/2){

						deltaXInt = reflexPoint.getX() - lastPoint.getX();
						deltaYInt = reflexPoint.getY() - lastPoint.getY();; 
						uinfo.direction = Math.PI + Math.atan(deltaYInt/deltaXInt);
					}
					if(uinfo.direction>3*Math.PI/2 && uinfo.direction<2*Math.PI){

						deltaXInt = reflexPoint.getX() - lastPoint.getX();
						deltaYInt = reflexPoint.getY() - lastPoint.getY();; 
						uinfo.direction = Math.PI  + Math.atan(deltaYInt/deltaXInt);
					}


				}


			}
			uinfo.distance -= uinfo.velocity;
			Location2D newLoc = new Location2D((float)(newX+X_mov_rel),(float)(newY+Y_mov_rel));
			/*if(id==1 )
  {
		  System.out.println(id+"\t"+newLoc.getX()+"\t"+newLoc.getY());

  }*/

			f.moveRadio(id,newLoc);
			if(uinfo.distance<0){
				uinfo.renew();
			}

			return newLoc;
		}
	}
	//////////////////////////////////////////////////
	// static mobility model
	//


	/**
	 * Static (noop) mobility model.
	 *
	 * @author Rimon Barr &lt;barr+jist@cs.cornell.edu&gt;
	 * @since SWANS1.0
	 */

	public static class Static implements Mobility
	{
		//////////////////////////////////////////////////
		// Mobility interface
		//

		/** {@inheritDoc} */
		public MobilityInfo init(FieldInterface f, Integer id, Location loc)
		{
			return null;
		}

		/** {@inheritDoc} */
		public void next(FieldInterface f, Integer id, Location loc, MobilityInfo info)
		{
		}

	} // class Static


	//////////////////////////////////////////////////
	// random waypoint mobility model
	//

	/**
	 * Random waypoint state object.
	 *
	 * @author Rimon Barr &lt;barr+jist@cs.cornell.edu&gt;
	 * @since SWANS1.0
	 */
	public static class RandomWaypointInfo implements MobilityInfo
	{
		/** number of steps remaining to waypoint. */
		public int steps;

		/** duration of each step. */
		public long stepTime;

		/** waypoint. */
		public Location waypoint;
	}

	/**
	 * Random waypoint mobility model.
	 *
	 * @author Rimon Barr &lt;barr+jist@cs.cornell.edu&gt;
	 * @since SWANS1.0
	 */
	public static class RandomWaypoint implements Mobility
	{
		/** thickness of border (for float calculations). */
		public static final float BORDER = (float)0.0005;

		/** Movement boundaries. */
		private Location.Location2D bounds;

		/** Waypoint pause time. */
		private long pauseTime;

		/** Step granularity. */
		private float precision;

		/** Minimum movement speed. */
		private float minspeed; 

		/** Maximum movement speed. */
		private float maxspeed;

		/**
		 * Initialize random waypoint mobility model.
		 *
		 * @param bounds boundaries of movement
		 * @param pauseTime waypoint pause time
		 * @param precision step granularity
		 * @param minspeed minimum speed
		 * @param maxspeed maximum speed
		 */
		public RandomWaypoint(Location.Location2D bounds, long pauseTime, 
				float precision, float minspeed, float maxspeed)
		{
			init(bounds, pauseTime, precision, minspeed, maxspeed);
		}

		/**
		 * Initialize random waypoint mobility model.
		 *
		 * @param bounds boundaries of movement
		 * @param config configuration string
		 */
		public RandomWaypoint(Location.Location2D bounds, String config)
		{
			//START --- Added by Emre Atsan

			String wayPointConfigOptions [];
			wayPointConfigOptions= config.split(":");

			//DEBUG//
			/*for(int i=0; i<4;i++)
    	{
    		System.out.println(wayPointConfigOptions[i]);
    	}
			 */	
			//END-DEBUG//


			init(bounds,Long.parseLong(wayPointConfigOptions[0]), Float.parseFloat(wayPointConfigOptions[1]),Float.parseFloat(wayPointConfigOptions[2]),Float.parseFloat(wayPointConfigOptions[3]));

			// throw new RuntimeException("not implemented");

			//END -- Added by Emre Atsan
		}

		/**
		 * Initialize random waypoint mobility model.
		 *
		 * @param bounds boundaries of movement
		 * @param pauseTime waypoint pause time (in ticks)
		 * @param precision step granularity
		 * @param minspeed minimum speed
		 * @param maxspeed maximum speed
		 */
		private void init(Location.Location2D bounds, long pauseTime, 
				float precision, float minspeed, float maxspeed)
		{
			this.bounds = bounds;
			this.pauseTime = pauseTime;
			this.precision = precision;
			this.minspeed = minspeed;
			this.maxspeed = maxspeed;
		}

		//////////////////////////////////////////////////
		// Mobility interface
		//

		/** {@inheritDoc} */
		public MobilityInfo init(FieldInterface f, Integer id, Location loc)
		{
			return new RandomWaypointInfo();
		}

		/** {@inheritDoc} */
		public void next(FieldInterface f, Integer id, Location loc, MobilityInfo info)
		{
			if(Main.ASSERT) Util.assertion(loc.inside(bounds));
			try
			{
				RandomWaypointInfo rwi = (RandomWaypointInfo)info;
				if(rwi.steps==0)
				{
					//START -- Added By Emre Atsan
					//System.out.println("Node ID:"+id+" position: "+loc.toString());

					//END -- Added by Emre Atsan


					// reached waypoint
					JistAPI.sleep(pauseTime);
					rwi.waypoint = new Location.Location2D(
							(float)(BORDER + (bounds.getX()-2*BORDER)*Constants.random.nextFloat()),
							(float)(BORDER + (bounds.getY()-2*BORDER)*Constants.random.nextFloat()));
					if(Main.ASSERT) Util.assertion(rwi.waypoint.inside(bounds));
					float speed = minspeed + (maxspeed-minspeed) * Constants.random.nextFloat();
					float dist = loc.distance(rwi.waypoint);
					rwi.steps = (int)Math.max(Math.floor(dist / precision),1);
					if(Main.ASSERT) Util.assertion(rwi.steps>0);
					float time = dist / speed;
					rwi.stepTime = (long)(time*Constants.SECOND/rwi.steps);
				}
				// take step
				JistAPI.sleep(rwi.stepTime);
				Location step = loc.step(rwi.waypoint, rwi.steps--);
				f.moveRadioOff(id, step);

			}
			catch(ClassCastException e) 
			{
				// different mobility model installed
			}
		}

	} // class RandomWaypoint


	//////////////////////////////////////////////////
	// Teleport mobility model
	//

	/**
	 * Teleport mobility model: pick a random location and teleport to it,
	 * then pause for some time and repeat.
	 *
	 * @author Rimon Barr &lt;barr+jist@cs.cornell.edu&gt;
	 * @since SWANS1.0
	 */
	public static class Teleport implements Mobility
	{
		/** Movement boundaries. */
		private Location.Location2D bounds;

		/** Waypoint pause time. */
		private long pauseTime;

		/**
		 * Initialize teleport mobility model.
		 *
		 * @param bounds boundaries of movement
		 * @param pauseTime waypoint pause time (in ticks)
		 */
		public Teleport(Location.Location2D bounds, long pauseTime)
		{
			this.bounds = bounds;
			this.pauseTime = pauseTime;
		}

		/** {@inheritDoc} */
		public MobilityInfo init(FieldInterface f, Integer id, Location loc)
		{
			if(pauseTime==0) return null;
			return MobilityInfo.NULL;
		}

		/** {@inheritDoc} */
		public void next(FieldInterface f, Integer id, Location loc, MobilityInfo info)
		{
			if(pauseTime>0)
			{
				//START -- Added By Emre Atsan
				System.out.println("Node ID:"+id+" position: "+loc.toString());
				System.out.println((JistAPI.getTimeString()));  
				//END -- Added by Emre Atsan


				JistAPI.sleep(pauseTime);
				loc = new Location.Location2D(
						(float)bounds.getX()*Constants.random.nextFloat(),
						(float)bounds.getY()*Constants.random.nextFloat());
				f.moveRadio(id, loc);
			}
		}

	} // class: Teleport


	/**
	 * Random Walk mobility model: pick a direction, walk a certain distance in
	 * that direction, with some fixed and random component, reflecting off walls
	 * as necessary, then pause for some time and repeat.
	 *
	 * @author Rimon Barr &lt;barr+jist@cs.cornell.edu&gt;
	 * @since SWANS1.0
	 */
	public static class RandomWalk implements Mobility
	{
		/** fixed component of step size. */
		private double fixedRadius;
		/** random component of step size. */
		private double randomRadius;
		/** time wait between steps. */
		private long pauseTime;
		/** field boundaries. */
		private Location.Location2D bounds;

		/**
		 * Create and initialize new random walk object.
		 *
		 * @param bounds field boundaries
		 * @param fixedRadius fixed component of step size
		 * @param randomRadius random component of step size
		 * @param pauseTime time wait between steps
		 */
		public RandomWalk(Location.Location2D bounds, double fixedRadius, double randomRadius, long pauseTime)
		{
			init(bounds, fixedRadius, randomRadius, pauseTime);
		}

		/**
		 * Create an initialize a new random walk object.
		 *
		 * @param bounds field boundaries
		 * @param config configuration string: "fixed,random,time(in seconds)"
		 */
		public RandomWalk(Location.Location2D bounds, String config)
		{
			String[] data = config.split(",");
			if(data.length!=3)
			{
				throw new RuntimeException("expected format: fixedradius,randomradius,pausetime(in seconds)");
			}
			double fixedRadius = Double.parseDouble(data[0]);
			double randomRadius = Double.parseDouble(data[1]);
			long pauseTime = Long.parseLong(data[2])*Constants.SECOND;
			init(bounds, fixedRadius, randomRadius, pauseTime);
		}

		/**
		 * Initialize random walk object.
		 *
		 * @param bounds field boundaries
		 * @param fixedRadius fixed component of step size
		 * @param randomRadius random component of step size
		 * @param pauseTime time wait between steps
		 */
		private void init(Location.Location2D bounds, double fixedRadius, double randomRadius, long pauseTime)
		{
			if(fixedRadius+randomRadius>bounds.getX() || fixedRadius+randomRadius>bounds.getY())
			{
				throw new RuntimeException("maximum step size can not be larger than field dimensions");
			}
			this.bounds = bounds;
			this.fixedRadius = fixedRadius;
			this.randomRadius = randomRadius;
			this.pauseTime = pauseTime;
		}

		//////////////////////////////////////////////////
		// mobility interface
		//

		/** {@inheritDoc} */
		public MobilityInfo init(FieldInterface f, Integer id, Location loc)
		{
			if(pauseTime==0) return null;
			return MobilityInfo.NULL;
		}

		/** {@inheritDoc} */
		public void next(FieldInterface f, Integer id, Location loc, MobilityInfo info)
		{
			// compute new random position with fixedRadius+randomRadius() distance
			double randomAngle = 2*Math.PI*Constants.random.nextDouble();
			double r = fixedRadius + Constants.random.nextDouble()*randomRadius;
			double x = r * Math.cos(randomAngle), y = r * Math.sin(randomAngle);
			double lx = loc.getX()+x, ly = loc.getY()+y;
			// bounds check and reflect
			if(lx<0) lx=-lx;
			if(ly<0) ly=-ly;
			if(lx>bounds.getX()) lx = bounds.getX()-(lx-bounds.getX());
			if(ly>bounds.getY()) ly = bounds.getY()-(ly-bounds.getY());
			// move
			if(pauseTime>0)
			{
				JistAPI.sleep(pauseTime);
				Location l = new Location.Location2D((float)lx, (float)ly);
				//System.out.println("move at t="+JistAPI.getTime()+" to="+l);
				f.moveRadio(id, l);
			}
		}

		/** {@inheritDoc} */
		public String toString()
		{
			return "RandomWalk(r="+fixedRadius+"+"+randomRadius+",p="+pauseTime+")";
		}

	} // class: RandomWalk


	public static class RandomDirectionInfo implements MobilityInfo
	{
		//random direction selected between [0,2pi] uniformly.
		public double direction = 2*Math.PI*Constants.random.nextDouble();


	} // class: RandomDirectionInfo 

	public static class RandomDirection implements Mobility
	{
		private long pauseTime;

		private float constantVelocity;

		private Location.Location2D bounds;


		public RandomDirection(Location.Location2D bounds, String config)
		{
			//START --- Added by Emre Atsan

			String directionConfigOptions [];
			directionConfigOptions= config.split(":");

			init(bounds,Float.parseFloat(directionConfigOptions[0]),Long.parseLong(directionConfigOptions[1]));

			// throw new RuntimeException("not implemented");

			//END -- Added by Emre Atsan
		}

		public RandomDirection(Location.Location2D bounds, float constantVelocity, long pauseTime )
		{
			init(bounds,constantVelocity,pauseTime);
		}


		private void init(Location.Location2D bounds, float constantVelocity, long pauseTime) {

			if(constantVelocity > bounds.getX() || constantVelocity > bounds.getY())
			{
				throw new RuntimeException("Speed (m/sec) cannot be larger than simulation area size!");
			}
			else
			{
				this.bounds = bounds;
				this.constantVelocity = constantVelocity;
				this.pauseTime = pauseTime*Constants.SECOND;

			}
		}


		public MobilityInfo init(FieldInterface f, Integer id, Location loc) {
			if(pauseTime==0) return null;
			return new RandomDirectionInfo();
		}

		public void next(FieldInterface f, Integer id, Location loc, MobilityInfo info) {

			if(Main.ASSERT) Util.assertion(loc.inside(bounds));
			try
			{
				RandomDirectionInfo rdi = (RandomDirectionInfo)info;
				double nodeAngle = rdi.direction;

				double x = constantVelocity * Math.cos(nodeAngle), y = constantVelocity * Math.sin(nodeAngle);
				double lx = loc.getX()+x, ly = loc.getY()+y;
				boolean sleptBefore =false;
				// bounds check and reflect
				if(lx<0) 
				{
					lx=-lx;
					//Update Node Movement Angle after reflection from the bound.
					rdi.direction = Math.PI-nodeAngle;
					JistAPI.sleep(pauseTime);
					sleptBefore=true;
				}
				else if(lx>bounds.getX()) 
				{
					lx = bounds.getX()-(lx-bounds.getX());
					rdi.direction = Math.PI-nodeAngle;
					JistAPI.sleep(pauseTime);
					sleptBefore=true;
				}

				if(ly<0) 
				{
					ly=-ly;
					rdi.direction = -nodeAngle;
					if(!sleptBefore)
					{
						JistAPI.sleep(pauseTime);
					}
				}
				else if(ly>bounds.getY())
				{
					ly = bounds.getY()-(ly-bounds.getY());
					rdi.direction = -nodeAngle;

					if(!sleptBefore)
					{
						JistAPI.sleep(pauseTime);
					}
				}

				//Sleep for one second in every step of the movement.
				JistAPI.sleep(1*Constants.SECOND);

				Location l = new Location.Location2D((float)lx, (float)ly);

				if(Main.ASSERT) Util.assertion(l.inside(bounds));


				if(id ==new Integer(1))
				{
					System.out.println(id+"\t"+l.getX()+"\t"+l.getY());
				} 
				f.moveRadio(id, l);

			}
			catch(ClassCastException e) 
			{
				// different mobility model installed
			}

		}

		public String toString()
		{
			return "RandomDirection(speed="+constantVelocity+" ,p="+pauseTime+")";
		}  

	} // class: RandomDirection

	public static class BoundlessSimulationAreaInfo implements MobilityInfo
	{
		//velocity of the mobile node
		public double velocity;
		public double direction;

		public BoundlessSimulationAreaInfo(float velocity, double direction)
		{
			this.velocity = velocity;
			this.direction= direction;
		}

	} // class: BoundlessSimulationAreaInfo 

	public static class BoundlessSimulationArea implements Mobility
	{
		private Location.Location2D bounds;

		private double vMax;
		private double aMax;
		private double deltaT;
		private double maxAngularChange;

		public BoundlessSimulationArea(Location.Location2D bounds,double vMax, double aMax,double deltaT,double maxAngularChange)
		{
			init(bounds,vMax,aMax,deltaT,maxAngularChange);

		}

		public BoundlessSimulationArea(Location2D bounds, String config) {
			// TODO Auto-generated constructor stub
			String directionConfigOptions [];
			directionConfigOptions= config.split(":");

			init(bounds,Double.parseDouble(directionConfigOptions[0]),Double.parseDouble(directionConfigOptions[1]),Double.parseDouble(directionConfigOptions[2]),Double.parseDouble(directionConfigOptions[3]));

		}

		private void init(Location.Location2D bounds,double vMax, double aMax,double deltaT,double maxAngularChange)
		{
			this.aMax = aMax;
			this.vMax = vMax;
			this.deltaT = deltaT;
			this.maxAngularChange = maxAngularChange*Math.PI;
			this.bounds = bounds;
		}

		public MobilityInfo init(FieldInterface f, Integer id, Location loc) {

			return new BoundlessSimulationAreaInfo(0,0);
		}

		public void next(FieldInterface f, Integer id, Location loc, MobilityInfo info) {

			if(Main.ASSERT) Util.assertion(loc.inside(bounds));

			try
			{
				BoundlessSimulationAreaInfo bsai = (BoundlessSimulationAreaInfo)info;

				double currentVelocity = bsai.velocity;
				double currentDirection = bsai.direction;

				//change in the velocity which is uniformly distributed between [-aMax*deltaT,aMax*deltaT]
				double deltaV = ((Constants.random.nextDouble()*2.0*aMax)-aMax)*deltaT;
				//change in the direction which is uniformly distributed between [-MaxAngularChange*deltaT,maxAngularChange*deltaT]
				double changeInDirection = ((Constants.random.nextDouble()*2.0*maxAngularChange)-maxAngularChange)*deltaT;

				double nextVelocity = Math.min(Math.max(currentVelocity+deltaV,0.0),vMax);
				double nextDirection = currentDirection + changeInDirection;

				//coordinates of calculated next location.
				double lx = (loc.getX() + currentVelocity*Math.cos(currentDirection));
				double ly = (loc.getY() + currentVelocity*Math.sin(currentDirection));

				//update the MobilityInfo data for the next step calculations.
				bsai.velocity = nextVelocity;
				bsai.direction = nextDirection;

				// bounds check and wrap-around
				if(lx<0) 
				{
					lx = bounds.getX()+lx;
				}
				else if(lx>bounds.getX()) 
				{
					lx=lx - bounds.getX();
				}

				if(ly<0) 
				{
					ly = bounds.getY()+ly;
				}
				else if(ly>bounds.getY())
				{
					ly=ly - bounds.getY();
				}

				//Sleep for one second in every step of the movement.
				JistAPI.sleep((long)deltaT*Constants.SECOND);

				Location l = new Location.Location2D((float)lx, (float)ly);

				if(Main.ASSERT) Util.assertion(l.inside(bounds));

				// if(id==1)
					//{System.out.println(id+"\t"+l.getX()+"\t"+l.getY());}

				f.moveRadio(id, l);

			}
			catch(ClassCastException e) 
			{
				// different mobility model installed
			}

		}

		public String toString()
		{
			return "BoundlessSimulationArea(Max. Velocity="+vMax+" ,Max.Accelaration="+aMax+" ,deltaT="+deltaT+" ,Max. Angular Change in direction (per sec.)"+maxAngularChange+")";
		}  

	}//class: BoundlessSimulationArea

} // interface Mobility

//todo: other mobility models
