//////////////////////////////////////////////////
// JIST (Java In Simulation Time) Project
// Timestamp: <Mobility.java Sun 2005/03/13 11:02:59 barr rimbase.rimonbarr.com>
//

// Copyright (C) 2004 by Cornell University
// All rights reserved.
// Refer to LICENSE for terms and conditions of use.

// Includes extensions by Ulm University
// - implemented missing constructor of Mobility.RandomWaypoint

package jist.swans.field;

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
	// @author Elmar Schoch >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Implemented functionality
	// throw new RuntimeException("not implemented");
        // parse config string of the form
        // <pause-time>,<precicion>,<minspeed>,<maxspeed>
    	String[] data = config.split(":");
    	init(bounds,Integer.parseInt(data[0]) * Constants.SECOND ,Float.parseFloat(data[1]),
                   Float.parseFloat(data[2]), Float.parseFloat(data[3]));
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
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
          // reached waypoint
          JistAPI.sleep(pauseTime);
          rwi.waypoint = new Location.Location2D(
              (float)(BORDER + (bounds.getX()-2*BORDER)*Constants.random.nextFloat()),
              (float)(BORDER + (bounds.getY()-2*BORDER)*Constants.random.nextFloat()));
          if(Main.ASSERT) Util.assertion(rwi.waypoint.inside(bounds));
          float speed = minspeed + (maxspeed-minspeed) * (float)Constants.betarnd(2, 2);
          float dist = loc.distance(rwi.waypoint);
          rwi.steps = (int)StrictMath.max(StrictMath.floor(dist / precision),1);
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
      double randomAngle = 2*StrictMath.PI*Constants.random.nextDouble();
      double r = fixedRadius + Constants.random.nextDouble()*randomRadius;
      double x = r * StrictMath.cos(randomAngle), y = r * StrictMath.sin(randomAngle);
      double lx = loc.getX()+x, ly = loc.getY()+y;
      // bounds check and reflect
      if(lx<0) lx=-lx;
      if(ly<0) ly=-ly;
      if(lx>bounds.getX()) lx = bounds.getX()-lx;
      if(ly>bounds.getY()) ly = bounds.getY()-ly;
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
  
  public static class UniformeInfo implements MobilityInfo
  {
  	public double direcao;
  	public double distancia;
  	public int passos;
  	public double tempoPasso;
  	public double velocidade;
  	
  	public UniformeInfo(double velocityMin,double velocityMax,double mu,int precision){
  		direcao = 2*Math.PI*Constants.random.nextDouble();
  		velocidade =  velocityMin + (velocityMax -velocityMin)*Constants.random.nextDouble(); // speedmin+(speedmax - speedmin)*rand
  		distancia = Constants.exprnd(mu);
  		passos = (int)Math.max(Math.floor(distancia / precision),1);
  		float time = (float)(distancia/velocidade);
  		tempoPasso = (time/passos);
  	}
  }
  public static class Uniforme implements Mobility
  {

  	private double vMax,vMin,mu;
  	private int precision;
  	private Location.Location2D limites;
  	
  	public Uniforme(Location.Location2D bounds,String config){
  		this.limites = bounds;
  		String ksConfigOptions [];
  		ksConfigOptions= config.split(":");
  		vMin = Double.parseDouble(ksConfigOptions[0]);
  		vMax = Double.parseDouble(ksConfigOptions[1]);
  		mu = Double.parseDouble(ksConfigOptions[2]);
  		precision =Integer.parseInt(ksConfigOptions[3]);
  		
  	}

  	public MobilityInfo init(FieldInterface f, Integer id, Location loc) {

  		return new UniformeInfo(vMin,vMax,mu,precision);
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
  			/* equaçao da reta;
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
  			uinfo =  new UniformeInfo(vMin,vMax,mu,precision);

  			  
  		
  		}
  	}
  }



} // interface Mobility

// todo: other mobility models
