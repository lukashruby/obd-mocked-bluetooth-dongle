/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.cardashboardadapter;

/**
 *
 * @author michael
 */
public enum DataEnum {
    SPEED, RPM;
    
    public static DataEnum fromPid(byte pid){
        return SPEED;
    }
}
