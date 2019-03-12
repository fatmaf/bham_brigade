// ===============================================================================
// Authors: AFRL/RQQA
// Organization: Air Force Research Laboratory, Aerospace Systems Directorate, Power and Control Division
// 
// Copyright (c) 2017 Government of the United State of America, as represented by
// the Secretary of the Air Force.  No copyright is claimed in the United States under
// Title 17, U.S. Code.  All Other Rights Reserved.
// ===============================================================================

// This file was auto-created by LmcpGen. Modifications will be overwritten.

package base_client.cmasi.searchai;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import base_client.cmasi.Location3D;
import base_client.avtas.lmcp.LMCPObject;
import base_client.avtas.lmcp.LMCPUtil;

/**
 Issued by an aircaft that detects a hazard zone with a sensor 
*/
public class HazardZoneDetection extends LMCPObject {
    
    public static final int LMCP_TYPE = 2;

    public static final String SERIES_NAME = "SEARCHAI";
    /** Series Name turned into a long for quick comparisons. */
    public static final long SERIES_NAME_ID = 6000273900112986441L;
    public static final int SERIES_VERSION = 4;


    private static final String TYPE_NAME = "HazardZoneDetection";

    private static final String FULL_LMCP_TYPE_NAME = "afrl.cmasi.searchai.HazardZoneDetection";

    /**  Approximate location of detected Zone. Zero altitude denotes ground level (Units: None)*/
    @LmcpType("Location3D")
    protected Location3D DetectedLocation = new Location3D();
    /**  Sensor payload ID of sensor performing the detection (Units: None)*/
    @LmcpType("uint32")
    protected long SensorPayloadID = 0L;
    /**  ID of the entity (aircraft) that is making the detection (Units: None)*/
    @LmcpType("uint32")
    protected long DetectingEnitiyID = 0L;
    /**  Type of the hazard zone detected (Units: None)*/
    @LmcpType("HazardType")
    protected HazardType DetectedHazardZoneType = HazardType.Undefined;

    
    public HazardZoneDetection() {
    }

    public HazardZoneDetection(Location3D DetectedLocation, long SensorPayloadID, long DetectingEnitiyID, HazardType DetectedHazardZoneType){
        this.DetectedLocation = DetectedLocation;
        this.SensorPayloadID = SensorPayloadID;
        this.DetectingEnitiyID = DetectingEnitiyID;
        this.DetectedHazardZoneType = DetectedHazardZoneType;
    }


    public HazardZoneDetection clone() {
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream( calcSize() );
            pack(bos);
            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
            HazardZoneDetection newObj = new HazardZoneDetection();
            newObj.unpack(bis);
            return newObj;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
     
    /**  Approximate location of detected Zone. Zero altitude denotes ground level (Units: None)*/
    public Location3D getDetectedLocation() { return DetectedLocation; }

    /**  Approximate location of detected Zone. Zero altitude denotes ground level (Units: None)*/
    public HazardZoneDetection setDetectedLocation( Location3D val ) {
        DetectedLocation = val;
        return this;
    }

    /**  Sensor payload ID of sensor performing the detection (Units: None)*/
    public long getSensorPayloadID() { return SensorPayloadID; }

    /**  Sensor payload ID of sensor performing the detection (Units: None)*/
    public HazardZoneDetection setSensorPayloadID( long val ) {
        SensorPayloadID = val;
        return this;
    }

    /**  ID of the entity (aircraft) that is making the detection (Units: None)*/
    public long getDetectingEnitiyID() { return DetectingEnitiyID; }

    /**  ID of the entity (aircraft) that is making the detection (Units: None)*/
    public HazardZoneDetection setDetectingEnitiyID( long val ) {
        DetectingEnitiyID = val;
        return this;
    }

    /**  Type of the hazard zone detected (Units: None)*/
    public HazardType getDetectedHazardZoneType() { return DetectedHazardZoneType; }

    /**  Type of the hazard zone detected (Units: None)*/
    public HazardZoneDetection setDetectedHazardZoneType( HazardType val ) {
        DetectedHazardZoneType = val;
        return this;
    }



    public int calcSize() {
        int size = super.calcSize();  
        size += 12; // accounts for primitive types
        size += LMCPUtil.sizeOf(DetectedLocation);

        return size;
    }

    public void unpack(InputStream in) throws IOException {
            DetectedLocation = (Location3D) LMCPUtil.getObject(in);
        SensorPayloadID = LMCPUtil.getUint32(in);

        DetectingEnitiyID = LMCPUtil.getUint32(in);

        DetectedHazardZoneType = HazardType.unpack( in );


    }

    public void pack(OutputStream out) throws IOException {
        LMCPUtil.putObject(out, DetectedLocation);
        LMCPUtil.putUint32(out, SensorPayloadID);
        LMCPUtil.putUint32(out, DetectingEnitiyID);
        DetectedHazardZoneType.pack(out);

    }

    public int getLMCPType() { return LMCP_TYPE; }

    public String getLMCPSeriesName() { return SERIES_NAME; }

    public long getLMCPSeriesNameAsLong() { return SERIES_NAME_ID; }

    public int getLMCPSeriesVersion() { return SERIES_VERSION; };

    public String getLMCPTypeName() { return TYPE_NAME; }

    public String getFullLMCPTypeName() { return FULL_LMCP_TYPE_NAME; }

    public String toString() {
        return toXML("");
    }

    public String toXML(String ws) {
        StringBuffer buf = new StringBuffer();
        buf.append( ws + "<HazardZoneDetection Series=\"SEARCHAI\">\n");
        if (DetectedLocation!= null){
           buf.append( ws + "  <DetectedLocation>\n");
           buf.append( ( DetectedLocation.toXML(ws + "    ")) + "\n");
           buf.append( ws + "  </DetectedLocation>\n");
        }
        buf.append( ws + "  <SensorPayloadID>" + String.valueOf(SensorPayloadID) + "</SensorPayloadID>\n");
        buf.append( ws + "  <DetectingEnitiyID>" + String.valueOf(DetectingEnitiyID) + "</DetectingEnitiyID>\n");
        buf.append( ws + "  <DetectedHazardZoneType>" + String.valueOf(DetectedHazardZoneType) + "</DetectedHazardZoneType>\n");
        buf.append( ws + "</HazardZoneDetection>");

        return buf.toString();
    }

    public boolean equals(Object anotherObj) {
        if ( anotherObj == this ) return true;
        if ( anotherObj == null ) return false;
        if ( anotherObj.getClass() != this.getClass() ) return false;
        HazardZoneDetection o = (HazardZoneDetection) anotherObj;
        if (DetectedLocation == null && o.DetectedLocation != null) return false;
        if ( DetectedLocation!= null && !DetectedLocation.equals(o.DetectedLocation)) return false;
        if (SensorPayloadID != o.SensorPayloadID) return false;
        if (DetectingEnitiyID != o.DetectingEnitiyID) return false;
        if (DetectedHazardZoneType != o.DetectedHazardZoneType) return false;

        return true;
    }

    public int hashCode() {
        int hash = 0;
        hash += 31 * (int)SensorPayloadID;
        hash += 31 * (int)DetectingEnitiyID;

        return hash + super.hashCode();
    }
    
}
