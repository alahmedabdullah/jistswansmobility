
//JIST (Java In Simulation Time) Project
//Timestamp: <Mobility.java Sun 2005/03/13 11:02:59 barr rimbase.rimonbarr.com>


//Copyright (C) 2004 by Cornell University
//All rights reserved.
//Refer to LICENSE for terms and conditions of use.

package jist.swans.field;


import java.util.ArrayList;
import java.util.Iterator;

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
public static class GaussMarkovInfo implements MobilityInfo{
	public double velocidade;
	public double velocidadeMedia;
	public double direcao;
	public double direcaoMedia;
	
	public GaussMarkovInfo(double speed,double speed_medio,double direction,double direction_medio){
		this.velocidade = speed;
		this.direcao = direction;
		this.direcaoMedia = direction_medio;
		this.velocidadeMedia = speed_medio;	
	}
}

public static class GaussMarkov implements Mobility{

	private double direcaoMediaInicial;
	private double velocidadeMediaInicial;
	private double borda;
	private double vMax;
	private double vMin;
	private double alpha;
	private double tempoPausa = 1;
	private Location limites;

	
	public GaussMarkov(Location bounds, String config){
		this.limites = bounds;
		String GaussMarkovConfigOptions [];
		GaussMarkovConfigOptions= config.split(":");
		vMin = Double.parseDouble(GaussMarkovConfigOptions[0]);
		vMax =Double.parseDouble(GaussMarkovConfigOptions[1]);
		velocidadeMediaInicial = Double.parseDouble(GaussMarkovConfigOptions[2]);
		direcaoMediaInicial =Double.parseDouble(GaussMarkovConfigOptions[3]);
		alpha =Double.parseDouble(GaussMarkovConfigOptions[4]);
		borda =Double.parseDouble(GaussMarkovConfigOptions[5]);
		
	}
	public MobilityInfo init(FieldInterface f, Integer id, Location loc) {
		double speed_inicial = vMin + (vMax-vMin)*Constants.random.nextDouble();
		double direction_inicial = 2*Math.PI*Constants.random.nextDouble();		
		return new GaussMarkovInfo(speed_inicial,velocidadeMediaInicial,direction_inicial,direcaoMediaInicial);
	}

	public void next(FieldInterface f, Integer id, Location loc, MobilityInfo info) {
		double x = loc.getX();
		double y = loc.getY();
		double limiteXinf = borda*limites.getX();
		double limiteYinf = borda*limites.getY();
		double limiteXsup = limites.getX() - borda*limites.getX();
		double limiteYsup = limites.getY() - borda*limites.getY();
		
		GaussMarkovInfo gminfo = (GaussMarkovInfo)info;
		double direction_medio = gminfo.direcaoMedia;
		double X=limites.getX(),Y=limites.getY();
		if (x > 0 && x < limiteXinf && y > 0 && y < limiteYinf)
			direction_medio = Math.PI/4;
		else if (x > limiteXinf && x < limiteXsup && x > 0 && y < limiteYinf)
			direction_medio = Math.PI/2;
		else if (x > limiteXsup && x < X && y > 0 && y < limiteYinf)
			direction_medio = 3*Math.PI/4;
		else if (x > limiteXsup && x < X && y > limiteYinf  && y < limiteYsup)
			direction_medio = Math.PI;
		else if (x > limiteXsup && x < X && y > limiteYsup  && y < Y)
			direction_medio = 5*Math.PI/4;
		else if (x > limiteXinf && x < limiteXsup && y > limiteYsup  && y < Y)
			direction_medio = 3*Math.PI/2;
		else if (x > 0 && x < limiteXinf && y > limiteYsup  && y < Y)
			direction_medio =7*Math.PI/4;
		else if (x > 0 && x < limiteXinf && y > limiteYinf  && y< limiteYsup)
			direction_medio = 0;
		else
			direction_medio = gminfo.direcaoMedia;
		gminfo.direcaoMedia = direction_medio;
		double speed_old = gminfo.velocidade;
        double direction_old = gminfo.direcao;
        double s_ = gminfo.velocidadeMedia;
        double d_ = gminfo.direcaoMedia;
        double speed_new = alpha*speed_old + (1-alpha)*s_ + Math.sqrt(Math.pow(1-alpha,2))*Constants.random.nextGaussian();
        double direction_new =alpha*direction_old + (1-alpha)*d_ + Math.sqrt(Math.pow(1-direction_old,2))*Constants.random.nextGaussian();
        
        double x_old = loc.getX();
        double y_old = loc.getY();
        double s_old = gminfo.velocidade;
        double d_old = gminfo.direcao;
        double x_new = x_old + s_old*Math.cos(d_old);
        double y_new = y_old + s_old*Math.sin(d_old);
        gminfo.direcao = direction_new;
        gminfo.velocidade =speed_new;
        JistAPI.sleep((long)(tempoPausa*Constants.SECOND));	        
        Location newloc = new Location.Location2D((float)x_new,(float)y_new);
        if(newloc.inside(new Location.Location2D(0,0) ,new Location.Location2D(limites.getX(),limites.getY())))
        	f.moveRadio(id, newloc);
	}

}

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

public static class UniformeInfo implements MobilityInfo
{
	public double direcao;
	public double distancia;
	public int passos;
	public double tempoPasso;
	public double velocidade;
	
	public UniformeInfo(double velocityMin,double velocityMax,double mu,int steps){
		direcao = 2*Math.PI*Constants.random.nextDouble();
		velocidade =  velocityMin + (velocityMax -velocityMin)*Constants.random.nextDouble(); // speedmin+(speedmax - speedmin)*rand
		distancia = Constants.exprnd(mu);
		passos = (int)Math.max(Math.floor(distancia / 500/*precision*/),1);
		float time = (float)(distancia/velocidade);
		tempoPasso = (time/passos);
	}
}
public static class Uniforme implements Mobility
{

	private double vMax,vMin,mu;
	private int passos;
	private Location.Location2D limites;
	
	public Uniforme(Location.Location2D bounds,String config){
		this.limites = bounds;
		String ksConfigOptions [];
		ksConfigOptions= config.split(":");
		vMin = Double.parseDouble(ksConfigOptions[0]);
		vMax = Double.parseDouble(ksConfigOptions[1]);
		mu = Double.parseDouble(ksConfigOptions[2]);
		passos =Integer.parseInt(ksConfigOptions[3]);
		
	}

	public MobilityInfo init(FieldInterface f, Integer id, Location loc) {

		return new UniformeInfo(vMin,vMax,mu,passos);
	}

	public void next(FieldInterface f, Integer id, Location loc, MobilityInfo info) {
		UniformeInfo uinfo = (UniformeInfo)info;
		double stepDist = uinfo.velocidade*uinfo.tempoPasso;
		double novoX = loc.getX()+ stepDist*Math.cos(uinfo.direcao);
		double novoY = loc.getY()+ stepDist*Math.sin(uinfo.direcao);
		while(novoX<0 || novoX>limites.getX() || novoY<0 || novoY>limites.getY()){
			double deltaXExt = novoX-loc.getX();
			double deltaYExt = novoY-loc.getY();
			double a = deltaYExt/deltaXExt;
			double b =  novoY - (a*novoX);
			/* equa�ao da reta;
	        y = ax + b
            x = (y - b)/a
			 */
			Location2D lastPoint = null,reflexPoint=null;
			double deltaXInt, deltaYInt; 

			if(novoY<0){
				lastPoint = new Location2D((float)((0 - b)/a),0); 
				reflexPoint = new Location2D((float)novoX,(float)(-1*novoY)); 
				novoY = -1*novoY;
				deltaXInt = reflexPoint.getX() - lastPoint.getX();
				deltaYInt = reflexPoint.getY() - lastPoint.getY(); 
				
				if(uinfo.direcao>3*Math.PI/2 && uinfo.direcao<2*Math.PI)					
					uinfo.direcao =  Math.atan(deltaYInt/deltaXInt);
				if(uinfo.direcao>Math.PI && uinfo.direcao<3*Math.PI/2)
					uinfo.direcao = Math.PI + Math.atan(deltaYInt/deltaXInt);

			}else if(novoY>limites.getY()){
				lastPoint = new Location2D((float)((limites.getY() - b)/a),limites.getY()); 
				reflexPoint = new Location2D((float)novoX,(float)(2*limites.getY() - novoY)); 
				novoY = 2*limites.getY() - novoY;
				deltaXInt = reflexPoint.getX() - lastPoint.getX();
				deltaYInt = reflexPoint.getY() - lastPoint.getY();
				
				if(uinfo.direcao>0 && uinfo.direcao<Math.PI/2)
					uinfo.direcao =  2*Math.PI + Math.atan(deltaYInt/deltaXInt);
				if(uinfo.direcao>Math.PI/2 && uinfo.direcao<3*Math.PI)
					uinfo.direcao = Math.PI + Math.atan(deltaYInt/deltaXInt);
				
			}
			else if(novoX<0){
				lastPoint = new Location2D(0,(float)(a*0 + b)); 
				reflexPoint = new Location2D((float)(-1*novoX),(float)novoY); 
				novoX = -1*novoX;
				deltaXInt = reflexPoint.getX() - lastPoint.getX();
				deltaYInt = reflexPoint.getY() - lastPoint.getY(); 
				
				if(uinfo.direcao>Math.PI && uinfo.direcao<3*Math.PI/2)
					uinfo.direcao = 2*Math.PI + Math.atan(deltaYInt/deltaXInt);
				if(uinfo.direcao>Math.PI/2 && uinfo.direcao<Math.PI)
					uinfo.direcao = Math.atan(deltaYInt/deltaXInt);
			}else{
				lastPoint = new Location2D(limites.getX(),(float)(a*limites.getX() + b)); 
				reflexPoint = new Location2D((float)(2*limites.getX() - novoX),(float)novoY); 
				novoX = 2*limites.getX() - novoX;
				deltaXInt = reflexPoint.getX() - lastPoint.getX();
				deltaYInt = reflexPoint.getY() - lastPoint.getY();
			
				if(uinfo.direcao>0 && uinfo.direcao<Math.PI/2)
					uinfo.direcao = Math.PI + Math.atan(deltaYInt/deltaXInt);
				if(uinfo.direcao>3*Math.PI/2 && uinfo.direcao<2*Math.PI)
					uinfo.direcao = Math.PI  + Math.atan(deltaYInt/deltaXInt);
			}
		}
		uinfo.passos--;
		
		/*
 * 
 * 
 * 
 *
		  
		  float dist = loc.distance(rwi.waypoint);
				double steps = (int)Math.max(Math.floor(dist / precision),1);
				float time = dist / speed;
				dstepTime = (long)(time*Constants.SECOND/rwi.steps);
		  
		 */
		
		Location2D newLoc = new Location2D((float)novoX,(float)novoY);
		

		if(uinfo.tempoPasso!=0)
			JistAPI.sleep((long)(uinfo.tempoPasso*Constants.SECOND));		
		f.moveRadio(id,newLoc);
		if(uinfo.passos<=0){
			uinfo =  new UniformeInfo(vMin,vMax,mu,passos);

			  
		
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
public static class GrupoUniformeInfo implements MobilityInfo
{
	public double direcao;
	public double distancia;
	public double velocidade;
	public int passos;
	public double tempoPasso;
	public Location locReferencia;
	public int tipo = 3;
	public ArrayList<Integer> nosInternos=new ArrayList<Integer>();;

	public GrupoUniformeInfo(double Vmin, double Vmax,double mu,int steps){	
		//this.passosExec = steps;
		direcao = 2*Math.PI*Constants.random.nextDouble();
		distancia = Constants.exprnd(mu);
		velocidade = Vmin + (Vmax - Vmin)*Constants.random.nextDouble(); // speedmin+(speedmax - speedmin)*rand
		
		passos = (int)Math.max(Math.floor(distancia / 500/*precision*/),1);
		float time = (float)(distancia/velocidade);
		tempoPasso = (time/passos);
		
		
	}
	public void renew(double Vmin, double Vmax, double mu,int steps){
		direcao = 2*Math.PI*Constants.random.nextDouble();
		distancia = Constants.exprnd(mu);
		velocidade = Vmin + (Vmax - Vmin)*Constants.random.nextDouble(); // speedmin+(speedmax - speedmin)*rand
		passos = (int)Math.max(Math.floor(distancia / 500/*precision*/),1);
		float time = (float)(distancia/velocidade);
		tempoPasso = (time/passos);
		
	}
}


public static class GrupoUniforme implements Mobility{

	private static int qtdpointReference;
	private static int jaqtdpointReference=0;
	private GrupoUniformeInfo []nosReferencias ;
	private static double vMax,vMin;
	private int tempoPausa;
	private int passos;
	private static double mu;
	private double diagonalGrupo;
	private Location.Location2D limites;

	public GrupoUniforme(Location.Location2D bounds,String config){
		String ksConfigOptions [];
		ksConfigOptions= config.split(":");
		vMin = Double.parseDouble(ksConfigOptions[0]);
		vMax = Double.parseDouble(ksConfigOptions[1]);
		mu =Double.parseDouble(ksConfigOptions[2]);
		this.limites = bounds;
		qtdpointReference = Integer.parseInt(ksConfigOptions[3]);
		nosReferencias = new GrupoUniformeInfo[qtdpointReference];
		this.diagonalGrupo = Double.parseDouble(ksConfigOptions[4]);
		this.tempoPausa = Integer.parseInt(ksConfigOptions[5]);
		this.passos = Integer.parseInt(ksConfigOptions[6]);;  
	} 
	public MobilityInfo init(FieldInterface f, Integer id, Location loc) {

		GrupoUniformeInfo info;
		if(jaqtdpointReference<qtdpointReference){
			info = new GrupoUniformeInfo(vMin,vMax,mu,passos);
			info.locReferencia = loc;
			info.tipo = 1;
			nosReferencias[jaqtdpointReference++] = info;
		}
		else{
			info = new GrupoUniformeInfo(vMin,vMax,mu,passos);
			for (GrupoUniformeInfo i : nosReferencias) {
				if(loc.inside(i.locReferencia, new Location.Location2D((float)(i.locReferencia.getX()+diagonalGrupo),(float)(i.locReferencia.getY()+diagonalGrupo))))
				{
					i.nosInternos.add(id);
					info.tipo = 2;
				}
			}
		}
		return info;
	}

	public void next(FieldInterface f, Integer id, Location loc, MobilityInfo info) {
		GrupoUniformeInfo noinfo = (GrupoUniformeInfo) info;
		if(id>=1 && id<=qtdpointReference){
		//	JistAPI.sleep((long)(tempoPausa*Constants.SECOND));
		/*	Location oldLoc = loc;
			Location newLoc = moveNo(f, id, loc, noinfo);
			double deltax =  newLoc.getX() - oldLoc.getX();
			double deltay = newLoc.getY() -  oldLoc.getY(); 
			noinfo.locReferencia = newLoc;
			for (Iterator iter = noinfo.nosInternos.iterator(); iter.hasNext();) {
				Integer nointerno = (Integer) iter.next();
				f.moveRadio(nointerno, newLoc);
				
			}*/
		}else{
			Location l = moveNo(f, id, loc, noinfo);
			//noinfo.locReferencia=l;
			
		}
	}
	private GrupoUniformeInfo getHeadReference(Integer id){
		for (int i = 0; i < nosReferencias.length; i++) {
			GrupoUniformeInfo element = nosReferencias[i];
			if (element.nosInternos.contains(id))
				return element;
		}
		return null;
	}
	private Location2D moveNo(FieldInterface f, Integer id, Location loc, GrupoUniformeInfo uinfo) {
		GrupoUniformeInfo headinfo = getHeadReference(id);
		double xnode = loc.getX();
		double ynode = loc.getY();
		double X = limites.getX();
		double Y = limites.getY();
		if (uinfo.tipo == 1){
			X = limites.getX()-diagonalGrupo;
			Y = limites.getY()-diagonalGrupo;
		}
		double X_mov_rel = 0;
		double Y_mov_rel = 0;
		if(headinfo!=null){
			xnode = loc.getX()-headinfo.locReferencia.getX();
			ynode = loc.getY()-headinfo.locReferencia.getY();
			X = diagonalGrupo;
			Y = diagonalGrupo;
			X_mov_rel = headinfo.locReferencia.getX();
			Y_mov_rel = headinfo.locReferencia.getY();
		}

		double stepDist = uinfo.velocidade*uinfo.tempoPasso;
		double newX = xnode + stepDist*Math.cos(uinfo.direcao);
		double newY = ynode + stepDist*Math.sin(uinfo.direcao);
		while(newX<0 || newX>X || newY<0 || newY>Y){
			double deltaXExt = newX-xnode;
			double deltaYExt = newY-ynode;
			double a = deltaYExt/deltaXExt;
			double b =  newY - (a*newX);
			/* equa�ao da reta;
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
				if(uinfo.direcao>3*Math.PI/2 && uinfo.direcao<2*Math.PI){

					deltaXInt = reflexPoint.getX() - lastPoint.getX();
					deltaYInt = reflexPoint.getY() - lastPoint.getY();; 
					uinfo.direcao =  Math.atan(deltaYInt/deltaXInt);
				}
				if(uinfo.direcao>Math.PI && uinfo.direcao<3*Math.PI/2){

					deltaXInt = reflexPoint.getX() - lastPoint.getX();
					deltaYInt = reflexPoint.getY() - lastPoint.getY();; 
					uinfo.direcao = Math.PI + Math.atan(deltaYInt/deltaXInt);
				}


			}else if(newY>Y){
				lastPoint = new Location2D((float)((Y - b)/a),(float)Y); 
				reflexPoint = new Location2D((float)newX,(float)(2*Y - newY)); 
				newY = 2*Y - newY;
				//////new direction after reflection
				if(uinfo.direcao>0 && uinfo.direcao<Math.PI/2){

					deltaXInt = reflexPoint.getX() - lastPoint.getX();
					deltaYInt = reflexPoint.getY() - lastPoint.getY();; 
					uinfo.direcao =  2*Math.PI + Math.atan(deltaYInt/deltaXInt);
				}
				if(uinfo.direcao>Math.PI/2 && uinfo.direcao<3*Math.PI){
					deltaXInt = reflexPoint.getX() - lastPoint.getX();
					deltaYInt = reflexPoint.getY() - lastPoint.getY();; 
					uinfo.direcao = Math.PI + Math.atan(deltaYInt/deltaXInt);
				}
			}
			else if(newX<0){
				lastPoint = new Location2D(0,(float)(a*0 + b)); 
				reflexPoint = new Location2D((float)(-1*newX),(float)newY); 
				newX = -1*newX;
				//////new direction after reflection
				if(uinfo.direcao>Math.PI && uinfo.direcao<3*Math.PI/2){

					deltaXInt = reflexPoint.getX() - lastPoint.getX();
					deltaYInt = reflexPoint.getY() - lastPoint.getY();; 
					uinfo.direcao = 2*Math.PI + Math.atan(deltaYInt/deltaXInt);
				}
				if(uinfo.direcao>Math.PI/2 && uinfo.direcao<Math.PI){

					deltaXInt = reflexPoint.getX() - lastPoint.getX();
					deltaYInt = reflexPoint.getY() - lastPoint.getY();; 
					uinfo.direcao = Math.atan(deltaYInt/deltaXInt);
				}

			}else{
				lastPoint = new Location2D((float)X,(float)(a*X + b)); 
				reflexPoint = new Location2D((float)(2*X - newX),(float)newY); 
				newX = 2*X - newX;
				//////new direction after reflection
				if(uinfo.direcao>0 && uinfo.direcao<Math.PI/2){

					deltaXInt = reflexPoint.getX() - lastPoint.getX();
					deltaYInt = reflexPoint.getY() - lastPoint.getY();; 
					uinfo.direcao = Math.PI + Math.atan(deltaYInt/deltaXInt);
				}
				if(uinfo.direcao>3*Math.PI/2 && uinfo.direcao<2*Math.PI){

					deltaXInt = reflexPoint.getX() - lastPoint.getX();
					deltaYInt = reflexPoint.getY() - lastPoint.getY();; 
					uinfo.direcao = Math.PI  + Math.atan(deltaYInt/deltaXInt);
				}


			}


		}
		uinfo.passos--;
		//uinfo.distance -= uinfo.velocity;
		Location2D newLoc = new Location2D((float)(newX+X_mov_rel),(float)(newY+Y_mov_rel));
		/*if(id==1 )
  {
		  System.out.println(id+"\t"+newLoc.getX()+"\t"+newLoc.getY());

  }*/

		JistAPI.sleep((long)(uinfo.tempoPasso*Constants.SECOND));
		
		f.moveRadio(id,newLoc);
		if(uinfo.passos<=0){
			uinfo.renew(vMin,vMax,mu,passos);
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
				rwi.steps = (int)Math.max(Math.floor(dist / 500/*precision*/),1);
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
