/*
 * Graph.java
 *
 * OpenCOMJ is a flexible component model for reconfigurable reflection developed at Lancaster University.
 * Copyright (C) 2005 Musbah Sagar
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

/**
 * This class creates a visual display of the current component configuration inside a particular
 * instance of the OpenCOM runtime. Notably, we use the full powers of structural reflection
 * to create the displayed images.
 *
 * @author  Musbah Sagar (Oxford Brookes University) and Paul Grace
 * @version 1.3.2
 */

//Modified by Barry Porter, May 2006, to add remote visualization method hooks

package OpenCOM;
import java.awt.*;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.lang.*;
import java.util.*;

/**
 * Display Frame for graph visualisation.
 * Author: Musbah Sagar
 */
public class Graph extends Frame{
    /* Panel */
    public GraphPanel panel = new GraphPanel();
    /* Reference to the OpenCOM runtime */
    private IOpenCOM pIOpenCOM;
    public String frameworkName;
    
    public Vector<Graph> subWindowList;
    
    private boolean localSnapshotMode;
  
    /* Create an instance of the graph - note the OpenCOM runtime
     * reference is passed programatically
     */
    public Graph(IOpenCOM pIOpenCOM, String FrameworkTitle){
        try{
            localSnapshotMode = true;
            jbInit(FrameworkTitle);
            this.pIOpenCOM = pIOpenCOM;
        }catch(Exception e){
            e.printStackTrace();
        }
    }
  
    //create an instance without the OCJ runtime, used in remote visualisation
    public Graph(String FrameworkTitle)
       {
        try{
            localSnapshotMode = false;
            subWindowList = new Vector<Graph>();
            jbInit(FrameworkTitle);
        }catch(Exception e){
            e.printStackTrace();
        }
       }
    
    /* 
     * Initialise the panel
     */
    private void jbInit(String FrameworkTitle) throws Exception{
        this.setTitle(FrameworkTitle);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {hide();}});
        this.setSize(new Dimension(600, 500));
        this.add(panel);
        panel.setSize(getSize());
        panel.start();
    }
   
   //methods called from the RemoteVis server
   public void componentCreated(String componentName, String[] interfaces, String[] receptacles, String inFramework)
      {
      System.out.println("Component " + componentName + " added inside framework " + inFramework);
      
      if (componentName != null)
         {
         if (inFramework != null)
            {
            Graph frameworkGraph = findFrameworkGraph(inFramework);
            
            frameworkGraph.panel.addComponent(componentName, interfaces, receptacles);
            }
            else
            panel.addComponent(componentName, interfaces, receptacles);
         }
      }
   
   public Graph findFrameworkGraph(String name)
      {
      for (int i = 0; i < subWindowList.size(); i++)
         {
         if (subWindowList.elementAt(i).frameworkName.equals(name))
            {
            return subWindowList.elementAt(i);
            }
            else if (subWindowList.elementAt(i).findFrameworkGraph(name) != null)
            {
            return subWindowList.elementAt(i).findFrameworkGraph(name);
            }
         }
      
      return null;
      }
   
   public void frameworkCreated(String componentName, String[] interfaces, String[] receptacles, String inFramework)
      {
      System.out.println("Framework " + componentName + " added inside framework " + inFramework);
      
      if (componentName != null)
         {
         //create a new window for the framework's components
         // - maintain the IP address/port info from this window
         String sourceAddress = getTitle().substring(getTitle().indexOf(" on "), getTitle().length());
         Graph ng = new Graph(componentName + sourceAddress);
         ng.frameworkName = componentName;
         
         if (inFramework != null)
            {
            //perform a recursive search for the framework window
            Graph frameworkGraph = findFrameworkGraph(inFramework);
            
            frameworkGraph.panel.addFramework(componentName, interfaces, receptacles);
            frameworkGraph.subWindowList.addElement(ng);
            }
            else
            {
            panel.addFramework(componentName, interfaces, receptacles);
            subWindowList.addElement(ng);
            }
         }
      }
   
   public void componentDeleted(String componentName, String inFramework)
      {
      if (inFramework != null)
         {
         //perform a recursive search for the framework window
         Graph frameworkGraph = findFrameworkGraph(inFramework);
         
         frameworkGraph.panel.removeComponent(componentName);
         }
         else
         {
         panel.removeComponent(componentName);
         }
      }

   public void connectionMade(String sourceComponentName, String sinkComponentName, String interfaceName, String inFramework)
      {
      if ((sourceComponentName != null) && (sinkComponentName != null) && (interfaceName != null))
         {
         if (inFramework != null)
            {
            Graph frameworkGraph = findFrameworkGraph(inFramework);
            
            frameworkGraph.panel.addLink(sourceComponentName, sinkComponentName, interfaceName);
            }
            else
            panel.addLink(sourceComponentName, sinkComponentName, interfaceName);
         }
      }
   
   public void connectionBroken(String sourceComponentName, String sinkComponentName, String interfaceName, String inFramework)
      {
      if ((sourceComponentName != null) && (sinkComponentName != null) && (interfaceName != null))
         {
         if (inFramework != null)
            {
            Graph frameworkGraph = findFrameworkGraph(inFramework);
            
            frameworkGraph.panel.removeLink(sourceComponentName, sinkComponentName, interfaceName);
            }
            else
            panel.removeLink(sourceComponentName, sinkComponentName, interfaceName);
         }
      }
   
   public void interfaceExposed(String componentName, String interfaceName, String inFramework)
      {
      //find the framework, then the component, and add the interface to it
      if (inFramework != null)
         {
         Graph frameworkGraph = findFrameworkGraph(inFramework);
         
         Component c = frameworkGraph.panel.findComponent(componentName);
         c.addInterface(interfaceName);
         }
         else
         {
         Component c = panel.findComponent(componentName);
         c.addInterface(interfaceName);
         }
      }
   
   public void receptacleExposed(String componentName, String receptacleName, String inFramework)
      {
      
      }

/**
 * Display Panel for graph visualisation.
 * Author: Musbah Sagar
 */
class GraphPanel extends Panel implements Runnable, MouseListener, MouseMotionListener {
    
    // List of components in the current panel - MAX 100 (Frameworks should ensure this isn't exceeded
    Component components[] = new Component[100];
    Component pick;
    int nComponents; // number of components

    // List of connectection in the current panel - MAX 200
    Link Links[] = new Link[200];
    int nLinks; // number of links

    Thread relaxer;
    boolean random;
    boolean pickfixed;
    Image offscreen,image,image1;
    Dimension offscreensize;
    Graphics offgraphics;
    
    //vector to hold components that have been removed from the configuration, to animate their departure
    Vector<Component> removedComponents;
    
    //number specifying how long to keep trying to position components for (goes up to MAX_POSITION_TIME)
    // - reset this to zero to re-do positioning for a while
    int positioningCounter = 0;
    int MAX_POSITION_TIME = 100;

    GraphPanel() {
       addMouseListener(this);
       nComponents = 0;
       nLinks = 0;
       removedComponents = new Vector();
       }
  
    /**
     * Find a component in the panel based upon its name
     */
    Component findComponent(String lbl){
        for (int i = 0 ; i < nComponents ; i++) {
                  Component n = components[i];
             if(lbl.equals(n.lbl))return n;
        } 
        
        System.out.println("Returning null for findComponent: " + lbl);
        return null;
   }
   
    /**
     * Add a component to the panel
     */
   Component addComponent(String lbl, IUnknown pComp) {
        Dimension d = getSize();
        double x= (d.width/2);   
        double y= (d.height/2);  
        Component c = new Component(x,y,lbl,0, pComp);
        components[nComponents++] = c;// <========== CHECK FOR OUT OF NODE NUMBER = 100
        return c;
   }
   
   Component addFramework(String lbl, IUnknown pMeta) {
     Dimension d = getSize();
     double x= (d.width/2);   
	   double y= (d.height/2);  
     Component c = new Component(x,y,lbl,1, pMeta);
	   components[nComponents++] = c;// <========== CHECK FOR OUT OF NODE NUMBER = 100
	   return c;
   }
   
   //string-only versions
   synchronized Component addComponent(String lbl, String[] interfaces, String[] receptacles) {
        Dimension d = getSize();
        double x= (d.width/2);   
        double y= (d.height/2);  
        Component c = new Component(x,y,lbl,0, interfaces, receptacles);
        components[nComponents] = c;// <========== CHECK FOR OUT OF NODE NUMBER = 100
        nComponents++;
        
	      //re-set positioning timout
	      positioningCounter = 0;
        
        return c;
   }
   
   synchronized Component addFramework(String lbl, String[] interfaces, String[] receptacles) {
     Dimension d = getSize();
     double x= (d.width/2);   
	   double y= (d.height/2);  
     Component c = new Component(x,y,lbl,1, interfaces, receptacles);
	   components[nComponents] = c;// <========== CHECK FOR OUT OF NODE NUMBER = 100
	   nComponents++;
	   
	   //re-set positioning timout
	   positioningCounter = 0;
	   
	   return c;
   }

    
    public synchronized void removeComponent(String lbl) {
     Component c=null;
     int indx=0;
     for (int i = 0 ; i < nComponents ; i++) {
	      Component n = components[i];
         if(lbl.equals(n.lbl)){
          indx=i;
          break;
         }
      }
      
      c = components[indx];
      removedComponents.addElement(components[indx]);
      components[indx].setComponentLeaving();
      // Replace the deleted node with the last in the array
      components[indx]=components[--nComponents];
      components[nComponents]=null;
      
      //remove all the links to/from that component
      for (int i = nLinks - 1; i >= 0; i--)
         {
         if ((Links[i].from == c) || (Links[i].to == c))
            {
            Links[i] = Links[nLinks - 1];
            Links[nLinks - 1] = null;
            nLinks--;
            }
         }
     }


     public synchronized void addLink(String from,String to, String iid) {
     addLink(findComponent(from),findComponent(to), iid);
     }
     
     public synchronized void addLink(Component from,Component to, String iid) {
    	 Link e = new Link(from,to,iid);
	     Links[nLinks] = e;
	     nLinks++;
      }
   
     public synchronized void addLink(Component from,Component to, int len, String iid) {
    	 Link e = new Link(from,to,len, iid);
	     Links[nLinks] = e;
	     nLinks++;
      }
    
      
     public void removeLink(String from, String to, String iid)
        {
        //removeLink(findComponent(from), findComponent(to), iid);
        }
      
     public synchronized void removeLink(Component from, Component to, String iid) {
        /*int index = -1;
        
        for (int i = 0; i < nLinks; i++)
         {
         if ((Links[i].from == from) && (Links[i].to == to) && (Links[i].Label.equals(iid)))
            {
            index = i;
            break;
            }
         }
       
       if (index != -1)
         {
         Links[index] = Links[nLinks - 1];
         Links[nLinks - 1] = null;
         nLinks--;
         }*/
      }

    public void run() {
        //Thread me = Thread.currentThread();
        while(true){//me==Thread.currentThread()){
            
	   try {
                if(positioningCounter < MAX_POSITION_TIME){
                    Thread.sleep(100);
                    positioningCounter++;
                    relax();
                    if (random && (Math.random() < 0.03)) {
		      // Pick a random component
                        Component n = components[(int)(Math.random() * nComponents)]; 
                        if (!n.fixed) {
		          n.x += 100*Math.random() - 50;
		          n.y += 100*Math.random() - 50;
		        }
                    }
                }
                else{
                    Thread.sleep(100);
                    repaint();
                }
            } catch (InterruptedException e) {}
	}
    }
   public synchronized void update(Graphics g) {
    Dimension d = getSize();
	
	   if ((offscreen == null) || (d.width != offscreensize.width) || (d.height != offscreensize.height)) {
	       offscreen = createImage(d.width, d.height);
	       offscreensize = d;
	         if (offgraphics != null)offgraphics.dispose();
	          offgraphics = offscreen.getGraphics();
	          offgraphics.setFont(getFont());
	    }
	    
    // Draw the off screen background with Gradient Paint
    Graphics2D g2d = (Graphics2D)offgraphics;
    Color startColor =  new Color(94, 100,158);
    Color endColor = new Color(255, 235, 250);
    GradientPaint gradient = new GradientPaint(0, 0, startColor, d.width, d.height, endColor);
    g2d.setPaint(gradient);
    offgraphics.fillRect(0, 0, d.width, d.height);
    
        // Draw Links
      for (int i = 0 ; i < nLinks ; i++)
            Links[i].paint(offgraphics);
    
     // Draw components
    for (int i = 0 ; i < nComponents ; i++)
      components[i].paint(offgraphics,offgraphics.getFontMetrics());
    
    
    for (int i = removedComponents.size() - 1; i >= 0 ; i--)
      {
      if (! removedComponents.elementAt(i).hasAnimationFinished())
         {
         removedComponents.elementAt(i).paint(offgraphics,offgraphics.getFontMetrics());
         }
         else
         removedComponents.removeElementAt(i);
      }
    

    
   
    
    // Draw off screen
    g.drawImage(offscreen, 0, 0, null);
}


synchronized void relax() {
	for (int i = 0 ; i < nLinks ; i++) {
	    Link e = Links[i];
	    
	    //null check...there's a problem sometimes when a to/from component is put in a different window, so the link can't be drawn
	    if ((e.to != null) && (e.from != null))
	      {
   	    double vx = e.to.x - e.from.x;
   	    double vy = e.to.y - e.from.y;
   	    double len = Math.sqrt(vx * vx + vy * vy);
               len = (len == 0) ? .0001 : len;
   	    double f = (Links[i].len - len) / (len * 3);
   	    double dx = f * vx;
   	    double dy = f * vy;
   
   	    if (e.to != null)
   	      {
   	      e.to.dx += dx;
   	      e.to.dy += dy;
            }
         
         if (e.from != null)
            {
   	      e.from.dx += -dx;
   	      e.from.dy += -dy;
            }
         }
	}

	
	for (int i = 0 ; i < nComponents ; i++) {
	    Component n1 = components[i];
	    
	    double dx = 0;
	    double dy = 0;

	    for (int j = 0 ; j < nComponents ; j++) {
		if (i == j) {
		    continue;
		}
		Component n2 = components[j];
		double vx = n1.x - n2.x;
		double vy = n1.y - n2.y;
		double len = vx * vx + vy * vy;
		if (len == 0) {
		    dx += Math.random();
		    dy += Math.random();
		} else if (len < 500*250) {
		    dx += vx / len;
		    dy += vy / len;
		}
	    }
	    double dlen = dx * dx + dy * dy;
	    if (dlen > 0) {
		dlen = Math.sqrt(dlen) / 2;
		n1.dx += dx / dlen*1.5;
		n1.dy += dy / dlen*1.5;
	    }
	}

	Dimension d = getSize();

	for (int i = 0 ; i < nComponents ; i++) {
	    Component n = components[i];
	    if (!n.fixed) {
		n.x += Math.max(-5, Math.min(5, n.dx));
		n.y += Math.max(-5, Math.min(5, n.dy));
            }
            if (n.x < n.w/2) {
                n.x = n.w/2;
            } else if (n.x > d.width-n.w/2) {
                n.x = d.width-n.w/2;
            }
            if (n.y < n.h/2) {
                n.y =  n.h/2;
            } else if (n.y > d.height- n.h/2) {
                n.y = d.height- n.h/2;
            }
	    n.dx /= 2;
	    n.dy /= 2;
	}
	repaint();
}
//===========================================
//  Event Handling
//===========================================
//
// mouseClicked
//
public void mouseClicked(MouseEvent e) {    

    if (e.getClickCount() == 2){}
    else
        return;
    
    	int x = e.getX();
	int y = e.getY();
        for (int i = 0 ; i < nComponents ; i++) {
            Component n = components[i];
            if(n.type==1){
                boolean xRange = ((x>=n.x)&&(x<(n.x+n.w)));
                boolean yRange = ((y<=n.y)&&(y>(n.y-n.h)));
                if (xRange&&yRange) {
                    // Framework --> New Graph
                    if (localSnapshotMode)
                       visualise(n.myMeta, n.lbl);
                       else
                       {
                       //find the framework window and show it
                       System.out.println("User clicked on a framework; searching for matching window");
                        for (int k = 0; k < subWindowList.size(); k++)
                           {
                           if (subWindowList.elementAt(k).frameworkName.indexOf(n.lbl) != -1)
                              {
                              System.out.println("...found matching window");
                              subWindowList.elementAt(k).setVisible(true);
                              break;
                              }
                           }
                       }
                }
            }
        }
   
}

public void visualise(ICFMetaInterface pMeta, String label){
        Graph g = new Graph(pIOpenCOM, label);
        g.setVisible(true);

        Vector<IUnknown> pComps  = new Vector<IUnknown>();
        int noComps = pMeta.get_internal_components(pComps);
  
        // Add compoenent to the graph
        for(int i=0;i<noComps;i++){
            IUnknown pComp = pComps.get(i);
            String name = pMeta.getComponentName(pComp);
            //String name = iOpenCOM.getComponentName(pComp);
            // If its not primitive -- ignore
            
            // Detect if its a framework
            ICFMetaInterface iFrameworkMeta = (ICFMetaInterface) pComp.QueryInterface("OpenCOM.ICFMetaInterface");
            if(iFrameworkMeta==null){
                g.panel.addComponent(name, pComp);
            }
            //else add framework
            else{
                g.panel.addFramework(name, pComp);
            }
        }
  
        for(int i=0;i<noComps;i++){
            IUnknown pComp = pComps.get(i);

            Vector<OCM_RecpMetaInfo_t> ppRecps = new Vector<OCM_RecpMetaInfo_t>();
            IMetaInterface pMetaIntf =  (IMetaInterface) pComp.QueryInterface("OpenCOM.IMetaInterface");
            int noRecps = pMetaIntf.enumRecps(ppRecps);
            for (int j=0; j<noRecps; j++){
                OCM_RecpMetaInfo_t temp = ppRecps.elementAt(j);
                Vector<Long> Recplist = new Vector<Long>();
                IMetaArchitecture pMetaArch = (IMetaArchitecture) pIOpenCOM.QueryInterface("OpenCOM.IMetaArchitecture");
                int noConns = pMetaArch.enumConnsFromRecp(pComp, temp.iid, Recplist);
                for(int k=0;k<noConns;k++){
                    OCM_ConnInfo_t TempConnInfo = pIOpenCOM.getConnectionInfo(Recplist.get(k).longValue());
                    // If they are both in this domain --> Connect them
                    boolean source = false;
                    boolean sink = false;
                    for(int c = 0; c<noComps;c++){
                        String name = pIOpenCOM.getComponentName(pComps.get(c));
                        if(name.equalsIgnoreCase(TempConnInfo.sourceComponentName))
                            source=true;
                        if(name.equalsIgnoreCase(TempConnInfo.sinkComponentName))
                            sink=true;
                    }
                    if(source&&sink)
                        g.panel.addLink(TempConnInfo.sourceComponentName, TempConnInfo.sinkComponentName, TempConnInfo.interfaceType);
                }
            }     
        }
    }
//
// mousePressed
//
public void mousePressed(MouseEvent e) {
	
	addMouseMotionListener(this);
	double bestdist = Double.MAX_VALUE;
	int x = e.getX();
	int y = e.getY();
	     
   for (int i = 0 ; i < nComponents ; i++) {
	  Component n = components[i];
    if(n!=null){
        double dist = (n.x - x) * (n.x - x) + (n.y - y) * (n.y - y);
        if (dist < bestdist) {
        pick = n;
        bestdist = dist;
        }
    }
  }
   if (pick != null)
      {
   	pickfixed = pick.fixed;
   	pick.fixed = true;
   	pick.mouseOver=false;
   	pick.x = x;
   	pick.y = y;
   	repaint();
	   }
	e.consume();
}
//
// mouseReleased
//
public void mouseReleased(MouseEvent e) {
	  removeMouseMotionListener(this);
        if (pick != null) {
            pick.x = e.getX();
            pick.y = e.getY();
            pick.fixed = pickfixed;
            pick = null;
        }
	repaint();
	e.consume();
}
//
// mouseEntered
//
public void mouseEntered(MouseEvent e) {   addMouseMotionListener(this); }
//
// mouseExited
//
public void mouseExited(MouseEvent e) {  removeMouseMotionListener(this);  }
//
// mouseDragged
//
public void mouseDragged(MouseEvent e) {
	pick.x = e.getX();
	pick.y = e.getY();
	repaint();
	e.consume();
}
//
// mouseMoved
//
public void mouseMoved(MouseEvent e) { 
  
 	int x = e.getX();
	int y = e.getY();
	
  for (int i = 0 ; i < nComponents ; i++) {
	  Component n = components[i];
    if(n!=null){
        int nx=(int)n.x-n.w/2;
        int ny=(int)n.y-n.h/2;
        if(x>=nx&&x<=(nx+n.w)&&y>=ny&&y<=(ny+n.h)){
            n.mouseOver=true;
            n.mouseX=x;
            n.mouseY=y;
        }
        else {
              n.mouseOver=false;
              n.mouseX=x;
              n.mouseY=y;
        }
   }
  }

	e.consume();
}
//
// start
//
public void start() {
	relaxer = new Thread(this);
	relaxer.start();
}
//
// stop
//
public void stop() {
	relaxer = null;
}

}// End class


public class point{
    public double x;
    public double y;
    public String name;
    
    point(double x, double y, String iid){
        this.x=x;
        this.y=y;
        this.name=iid;
    }
}
//###########################################
// Class: Component
//###########################################
public class Component{
    public double x;
    public double y;
    public double dx;
    public double dy;
    public int w;
    public int h;
    int      mouseX;
    int      mouseY;
    
    int interfaceCount;
    int receptacleCount;
    
    boolean fixed;
    boolean mouseOver;
    public String lbl; /* Label */
    int type;
    Vector<String> Interfaces;
    Vector<String> Receptacles;
    ICFMetaInterface myMeta;
    IUnknown pComp;
    
    Vector<point> IntfPoints;
    Vector<point> RecpPoints;
    
    //animated leave stuff:
    boolean componentLeaving;
    boolean animationFinished;
    Color animColour;
    int animFadeColour;
    
Component(double x,double y,String lbl, int type, IUnknown pMeta){
        Interfaces = new Vector<String>();
        Receptacles = new Vector<String>();
        IntfPoints = new Vector<point>();
        RecpPoints = new Vector<point>();
        
	this.x=x;
	this.y=y;
	this.w=lbl.length()*8;
        if(w<100)
            w=100;
	
	this.lbl=lbl;
	mouseX=0;
	mouseY=0;
	mouseOver=false;
        this.type=type;
        this.pComp = pMeta;
        this.myMeta = (ICFMetaInterface) pComp.QueryInterface("OpenCOM.ICFMetaInterface");
        IMetaInterface getFr = (IMetaInterface) pComp.QueryInterface("OpenCOM.IMetaInterface");
        Vector<Class> intfs = new Vector<Class>(); 
        int size = getFr.enumIntfs(intfs);
        int count=0;
        for(int i=0; i<size; i++){
            Interfaces.add(intfs.get(i).toString());
            String intf = Interfaces.get(i);
            StringTokenizer st = new StringTokenizer(intf,".");
            String result=intf;   
            while(st.hasMoreElements()){
              result=st.nextToken();
            }
            if((result.equalsIgnoreCase("IMetaInterface"))||
                (result.equalsIgnoreCase("IUnknown"))||
                (result.equalsIgnoreCase("ILifeCycle"))||
                (result.equalsIgnoreCase("IConnections"))){ 

            }
            else{
                point pIn = new point(x-15 ,y+(5+(count*15)), result);
                IntfPoints.add(pIn);
                count++;
            }
        }
        if(count<=5)
            this.h=80;
        else
            this.h=80+((count-4)*10);
        
        Vector<OCM_RecpMetaInfo_t> recps = new Vector<OCM_RecpMetaInfo_t>(); 
        size = getFr.enumRecps(recps);
        count=0;
        for(int i=0; i<size; i++){
            OCM_RecpMetaInfo_t tmp = recps.get(i);
            Receptacles.add(tmp.iid);
            String recp = Receptacles.get(i);
            StringTokenizer st = new StringTokenizer(recp,".");
            String result=recp;   
            while(st.hasMoreElements()){
                result=st.nextToken();
            }
            if((result.equalsIgnoreCase("IMetaInterface"))||
                (result.equalsIgnoreCase("IUnknown"))||
                (result.equalsIgnoreCase("ILifeCycle"))||
                (result.equalsIgnoreCase("IConnections"))){ 

            }
            else{
                point pRe = new point(x-15 ,y+(5+(count*15)), result);
                RecpPoints.add(pRe);
                count++;
            }
        }
    }
     
Component(double x,double y,String lbl, int type, String[] interfaces, String[] receptacles){
        Interfaces = new Vector<String>();
        Receptacles = new Vector<String>();
        IntfPoints = new Vector<point>();
        RecpPoints = new Vector<point>();
        
        //set-up animation fade-in color
        animFadeColour = 255;
        
	this.x=x;
	this.y=y;
	this.w=lbl.length()*8;
        if(w<100)
            w=100;
	
	this.lbl=lbl;
	mouseX=0;
	mouseY=0;
	mouseOver=false;
        this.type=type;
        
        int count=0;
        for(int i=0; i<interfaces.length; i++){
            Interfaces.add(interfaces[i]);
            String intf = interfaces[i];
            StringTokenizer st = new StringTokenizer(intf,".");
            String result=intf;   
            while(st.hasMoreElements()){
              result=st.nextToken();
            }
            if((result.equalsIgnoreCase("IMetaInterface"))||
                (result.equalsIgnoreCase("IUnknown"))||
                (result.equalsIgnoreCase("ILifeCycle"))||
                (result.equalsIgnoreCase("IConnections"))){ 

            }
            else{
                point pIn = new point(x-15 ,y+(5+(count*15)), result);
                IntfPoints.add(pIn);
                count++;
            }
        }
        interfaceCount = count;
        
        if(count<=5)
            this.h=80;
        else
            this.h=80+((count-4)*10);
        
        count=0;
        for(int i=0; i<receptacles.length; i++){
            Receptacles.add(receptacles[i]);
            String recp = receptacles[i];
            StringTokenizer st = new StringTokenizer(recp,".");
            String result=recp;   
            while(st.hasMoreElements()){
                result=st.nextToken();
            }
            if((result.equalsIgnoreCase("IMetaInterface"))||
                (result.equalsIgnoreCase("IUnknown"))||
                (result.equalsIgnoreCase("ILifeCycle"))||
                (result.equalsIgnoreCase("IConnections"))){ 

            }
            else{
                point pRe = new point(x-15 ,y+(5+(count*15)), result);
                RecpPoints.add(pRe);
                count++;
            }
        }
        
        receptacleCount = count;
     }

//methods to support dynamic interface and receptacle exposition
public synchronized void addInterface(String interfaceName)
   {
   Interfaces.add(interfaceName);
   
   StringTokenizer st = new StringTokenizer(interfaceName,".");
   String result=interfaceName;   
   while(st.hasMoreElements()){
         result=st.nextToken();
   }
   
   point pIn = new point(x-15 ,y+(5+(interfaceCount*15)), result);
   IntfPoints.add(pIn);
   interfaceCount++;
   }

public synchronized void addReceptacle(String receptacleName)
   {
   Receptacles.add(receptacleName);
   
   StringTokenizer st = new StringTokenizer(receptacleName,".");
   String result=receptacleName;   
   while(st.hasMoreElements()){
         result=st.nextToken();
   }
   
   point pRe = new point(x-15 ,y+(5+(receptacleCount*15)), result);
   RecpPoints.add(pRe);
   receptacleCount++;
   }

public point getPoint(String iid, int type){
    
    if(type==0){
        for(int i=0;i<IntfPoints.size();i++){
            point tmp = IntfPoints.get(i);
            if(tmp.name.equalsIgnoreCase(iid)){
                point ret = new point(x-15,y+(5+(i*15)),iid) ;
                return ret;
            }
        }
      //System.out.println("WARNING: (0) Couldn't find interface point for iid = " + iid + " (my comp label is " + lbl + ")");
    }
    if(type==1){
        for(int i=0;i<RecpPoints.size();i++){
            point tmp = RecpPoints.get(i);
            if(tmp.name.equalsIgnoreCase(iid)){
                point ret = new point(x+w+10,y+(5+(i*15)),iid) ;
                return ret;
            }
        }
     //System.out.println("WARNING: (1) Couldn't find receptacle point for iid = " + iid + " (my comp label is " + lbl + ")");
    }
    return null;
}

//support for animated component departure
public void setComponentLeaving()
   {
   componentLeaving = true;
   animationFinished = false;
   }

public boolean hasAnimationFinished()
   {
   return animationFinished;
   }

public void paint(Graphics g,FontMetrics fm) {
	//w = fm.stringWidth(lbl) + 10;
	//h = fm.getHeight() + 4;
  
	Graphics2D g2d=(Graphics2D) g;
  int x = (int)this.x-w/2;
	int y = (int)this.y-h/2;
	
	int alpha = 255;
	
	if (componentLeaving)
	   {
   	if (animColour == null)
   	   {
           if(type==0)
               g2d.setColor(Color.CYAN);
           else
               g2d.setColor(Color.YELLOW);
         
         animColour = g2d.getColor();
         }
         else
         {
         
         int red = animColour.getRed();
         int blue = animColour.getBlue();
         int green = animColour.getGreen();
         
         alpha = animColour.getAlpha();
         
         if (alpha <= 0)
            animationFinished = true;
         
         alpha -= 20;
         
         if (alpha < 0)
            alpha = 0;
         
         animColour = new Color(red, green, blue, alpha);
         g2d.setColor(animColour);
         }
      }
      else
      {
      
        if(type==0)
            {
            int targetRed = Color.CYAN.getRed();
            int targetGreen = Color.CYAN.getGreen();
            int targetBlue = Color.CYAN.getBlue();
            
            if (animFadeColour > targetRed)
               targetRed = animFadeColour;
            
            if (animFadeColour > targetGreen)
               targetGreen = animFadeColour;
            
            if (animFadeColour > targetBlue)
               targetBlue = animFadeColour;
            
            g2d.setColor(new Color(targetRed, targetGreen, targetBlue));
            
            if (animFadeColour > 0)
               animFadeColour -= 5;
            }
        else
            g2d.setColor(Color.YELLOW);
      }
  
  g2d.setStroke(new BasicStroke(2));
  g.fillRoundRect(x,y,w,h,5,5);
  g2d.setColor(new Color(0, 0, 0, alpha));
  g.drawRoundRect(x,y,w,h,5,5);
  g.drawString(lbl, x+15, y+30 + fm.getAscent());
  
  //for each interface
  int count=0;
  for(int i=0; i<IntfPoints.size();i++){
      point tmp = IntfPoints.get(i);
      String intf = tmp.name;

        g.setColor(new Color(0, 0, 0, alpha));
        g.drawLine(x, y+(10+(count*15)), x-12, y+(10+(count*15)));
        g.setColor(new Color(Color.GRAY.getRed(), Color.GRAY.getGreen(), Color.GRAY.getBlue(), alpha));
   
        g.fillOval((int)x-15,(int)y+(5+(count*15)),10,10);
        g.setColor(new Color(0, 0, 0, alpha));
        g.drawOval((int)x-15,(int)y+(5+(count*15)),10,10);
        
        if(intf.length()<15)
            g.drawString(intf, (int) x-45-(4*intf.length()), (int) y+(13+(count*15)) );
        else if(intf.length()<30)
            g.drawString(intf, (int) x-60-(4*intf.length()), (int) y+(13+(count*15)) );
        else
            g.drawString(intf, (int) x-60-(6*intf.length()), (int) y+(13+(count*15)) );
        count++;
  }
  count = 0;
  for(int i=0; i<RecpPoints.size();i++){
      point tmp = RecpPoints.get(i);
            
      String recp = tmp.name;
        g.setColor(new Color(0, 0, 0, alpha));
        g.drawLine(x+w, y+(10+(count*15)), x+w+15, y+(10+(count*15)));
        g.setColor(new Color(Color.BLUE.getRed(), Color.BLUE.getGreen(), Color.BLUE.getBlue(), alpha));

        g.fillRect((int)x+w+10,(int)y+(5+(count*15)),10,10);
        g.setColor(new Color(0, 0, 0, alpha));
        g.drawRect((int)x+w+10,(int)y+(5+(count*15)),10,10);
        g.drawString(recp, (int) x+w+20, (int) y+(13+(count*15)) );
        count++;
  }
  
   
}


} // End of class Component




//###########################################
// Class: Link
//###########################################
public class Link {

 public Component from;
 public Component to;
 public double len; // Lenght of the Link
 final Color arcColor1 =   Color.black;
 final Color arcColor2 =   Color.pink;
 final Color arcColor3 =   Color.red;
 public String Label;

public Link(Component from, Component to, String iid){
    
 this(from,to,140,iid);
 
 }    
 
 public Link(Component from, Component to, int len, String iid){
	this.from=from;
	this.to=to;
	this.len=len;
        this.Label = iid;
        StringTokenizer st = new StringTokenizer(Label,".");
      String result=Label;   
      while(st.hasMoreElements()){
          result=st.nextToken();
      }
      Label=result;
      
     // System.out.println("New Link constructor, this.from = " + this.from + ", this.to = " + this.to + ", Label = " + Label);
 }    
 
 public void paint(Graphics g) {
   try{
     point fr = from.getPoint(Label, 1);
     
     //if (fr == null)
      //System.out.println("WARNING: fr is null");
     
	 double wf = from.w;
	 double hf = from.h;
   double xf = fr.x-wf-wf/2+10;
	 double yf = fr.y-hf/2-10;
   
         point cTo = to.getPoint(Label, 0);
   
     //if (cTo == null)
      //System.out.println("WARNING: cTo is null");
   
   double wt = to.w;
	 double ht = to.h;
	 double xt = cTo.x-wt/2;
	 double yt = cTo.y-ht/2-10;
   
     //if (g == null)
      //System.out.println("WARNING: g is null");
   
   g.setColor(arcColor1);
   
   double mixX=10.0;
   double x1,x2,x3,x4,x5,x6,x7,x8;
   double y1,y2,y3,y4,y5,y6,y7,y8;
   
   ArrayList al=new ArrayList();

   // from
   x1=xf+wf;
   y1=yf+(hf*0.2);
   // to
   x8=xt;
   y8=yt+(ht*0.2);
   
   // distance
   double distance=Math.sqrt(((x8-x1)*(x8-x1))+((y8-y1)*(y8-y1)));
   // Intermediate
   x2=0;
   if(x8>x1) x2=x1+(distance*0.1);
    else x2=x1+mixX;
   y2=y1;
   x7=0;
   if(x8>x1)x7=x8-(distance*0.1);
    else x7=x8-mixX;
   y7=y8;

   // Initilize the path string to empty
   String path = "";
   
   path += x1+","+y1+":";
   path += x2+","+y2+":";
   
   if(x7<x2&&y7>y2){
	  x3=x2;
	  y3=y2+(hf*0.9);
	  x6=x7;
	  y6=y7-(hf*0.3);
	  double w1=(wf*1.2);
	  double w2=(wt*1.2);
    path += x3+","+y3+":";
	    if(y6<y3)if(Math.abs(x6-x3)>(w1+w2)){
		    x4=x3-w1;
		    y4=y3;
		    x5=x6+w2;
		    y5=y6;
		     if(x5>x4)x5=x4;
		   path += x4+","+y4+":";
		   path += x5+","+y5+":";
	     }else {
		     y6=y7+(hf*0.9);
		     y3=y6;
         path += x3+","+y3+":";
 		     }
      path += x6+","+y6+":";
	  }
    
    if(x7<x2&&y7<y2){
	   x3=x2;
	   y3=y2-(hf*0.3);
	   x6=x7;
	   y6=y7+(hf*0.9);
 	   double w1=(wf*1.2);
	   double w2=(wt*1.2);
     path += x3+","+y3+":";
       if(y6>y3)if(Math.abs(x6-x3)>(w1+w2)){
		    x4=x3-w1;
		    y4=y3;
		    x5=x6+w2;
		    y5=y6;
		     if(x5>x4)x5=x4;
        path += x4+","+y4+":";
        path += x5+","+y5+":";
	     }else {
		     y6=y7-(hf*0.3);
		     y3=y6;
         path += x3+","+y3+":";
 		     }
	  path += x6+","+y6+":";
    }
  
   path += x7+","+y7+":";
   path += x8+","+y8;
   
   int[] xPoints=new int[8];
   int[] yPoints=new int[8];
   int count = 0;
   String[] pairs = path.split(":");
   for(int i=0;i<pairs.length;i++){
    String[] xy = pairs[i].split(",");
    int x=(int)Double.parseDouble(xy[0]);
    int y=(int)Double.parseDouble(xy[1]);
    xPoints[count]=x;
    yPoints[count]=y;
    count++;
   }
   
   g.setColor(Color.WHITE);
   g.drawPolyline(xPoints,yPoints,count);
   }
   catch(Exception e){
       //System.out.println("To= "+pIOpenCOM.getComponentName(to.pComp)+" From "+pIOpenCOM.getComponentName(from.pComp)+" Label= "+Label);
       //e.printStackTrace();
   }
 }
}// End class Link


//###########################################
// Class: SmartLine, used to calculate the 
//                   intersectoin point between
//                   a line and a rectangle
// Source: SVG Unleashed
//###########################################
public class SmartLine extends Line2D.Double{

    private Rectangle2D rectangle;
    private double x;
    private double y;

    public SmartLine() {
        super();
    }
    
    public void setRect(int cx, int cy, int width, int height) { 
      rectangle = new Rectangle2D.Double(cx, cy, width, height);
      calculateRectangleIntersection();
    }
    
    public double getX(){
     return x;
    }
    
    public double getY(){
     return y;
    }
    
    public double getXDistance() { 
        return getP1().getX() - getP2().getX();
    }

    public double getYDistance() {
        return getP1().getY() - getP2().getY();
    }

    private double getYValue() {
        return (rectangle.getHeight() / 2) * ((rectangle.getWidth() / 2) / getXDistance());
    }

    private double getXValue() {
        return (rectangle.getWidth() / 2) * ((rectangle.getHeight() / 2) / getYDistance());
    }

    private void calculateRectangleIntersection() {

        double yValue = (getYDistance())  * ((rectangle.getWidth() / 2) / getXDistance());
        double xValue = (getXDistance())  * ((rectangle.getHeight() / 2) / getYDistance());

        boolean eastside  = (Math.abs(yValue) < rectangle.getHeight() / 2)  && (getXDistance() >= 0);
        boolean westside  = (Math.abs(yValue) < rectangle.getHeight() / 2)  && (getXDistance() < 0);
        boolean northside = (Math.abs(xValue) < rectangle.getWidth()  / 2)  && (getYDistance() < 0);
        boolean southside = (Math.abs(xValue) < rectangle.getWidth()  / 2)  && (getYDistance() >= 0);

        if (westside){
            x = rectangle.getMinX();
            y = rectangle.getCenterY() - yValue;
        }

        if (eastside) { // right
            x = rectangle.getMaxX();
            y = rectangle.getCenterY() + yValue;
        }

        if (northside) { // top
            x = rectangle.getCenterX() - xValue;
            y = rectangle.getMinY();
        }

        if (southside) { // bottom
            x = rectangle.getCenterX() + xValue;
            y = rectangle.getMaxY();
        }

    }
}// End class SmartLine


}