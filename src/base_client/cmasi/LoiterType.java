// ===============================================================================
// Authors: AFRL/RQQA
// Organization: Air Force Research Laboratory, Aerospace Systems Directorate, Power and Control Division
// 
// Copyright (c) 2017 Government of the United State of America, as represented by
// the Secretary of the Air Force.  No copyright is claimed in the United States under
// Title 17, U.S. Code.  All Other Rights Reserved.
// ===============================================================================

// This file was auto-created by LmcpGen. Modifications will be overwritten.

package base_client.cmasi;


import base_client.avtas.lmcp.LMCPUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**  Describes loiter types */
public enum LoiterType {

    /**  vehicle default  */
    VehicleDefault(0),
    /**  circular  */
    Circular(1),
    /**  racetrack  */
    Racetrack(2),
    /**  figure eight */
    FigureEight(3),
    /**  Hovering. */
    Hover(4);


    private final int val;

    /** creates a new enum of the specified value */
    LoiterType(int val) {
        this.val = val;
    }

    /** returns the set value for this enum */
    public int getValue() {
        return val;
    }

    /** packs this enum into the LMCP buffer */
    public void pack(OutputStream out) throws IOException { LMCPUtil.putInt32(out, getValue()); }

    /** creates an enum for the value in the LMCP buffer */
    public static LoiterType unpack(InputStream in) throws IOException{
        return getEnum( LMCPUtil.getInt32(in) );
    }

    /** returns a new instance of this enum that matches the passed value (null if value is not known) */
    public static LoiterType getEnum(int val) {
        switch(val) {
            case 0 : return VehicleDefault;
            case 1 : return Circular;
            case 2 : return Racetrack;
            case 3 : return FigureEight;
            case 4 : return Hover;
            default: return VehicleDefault;

        }
    }
}
