package dev.dwmir.lib.api;
/** Frame-rate independent wheel motion, bounded at both ends. */
public final class SmoothScroll {
 private double value,target;private long last=System.nanoTime();
 public void jump(double value){this.value=this.target=Math.max(0,value);last=System.nanoTime();}
 public void by(double amount,double max){target=Math.clamp(target+amount,0,Math.max(0,max));}
 public double update(double max){long now=System.nanoTime();double seconds=Math.clamp((now-last)/1e9,0,.1);last=now;target=Math.clamp(target,0,Math.max(0,max));value=Math.clamp(approach(value,target,seconds),0,Math.max(0,max));if(Math.abs(target-value)<.05)value=target;return value;}
 public double target(){return target;}
 public static double approach(double value,double target,double seconds){return value+(target-value)*(-Math.expm1(-11*Math.max(0,seconds)));}
}
