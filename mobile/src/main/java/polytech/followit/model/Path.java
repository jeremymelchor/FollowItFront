package polytech.followit.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class Path implements Parcelable {

    private ArrayList<Node> listNodes = new ArrayList<>();
    private ArrayList<Instruction> listInstructions = new ArrayList<>();
    private String source = null, destination = null;
    private ArrayList<Beacon> listBeacons = new ArrayList<>();

    public Path() {
    }


    protected Path(Parcel in) {
        source = in.readString();
        destination = in.readString();
        listBeacons = in.createTypedArrayList(Beacon.CREATOR);
    }

    public ArrayList<Node> getListNodes() {
        return listNodes;
    }

    public void setListNodes(ArrayList<Node> listNodes) {
        this.listNodes = listNodes;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public ArrayList<Instruction> getListInstructions() {
        return listInstructions;
    }

    public void setListInstructions(ArrayList<Instruction> listInstructions) {
        this.listInstructions = listInstructions;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public ArrayList<Beacon> getListBeacons() {
        return listBeacons;
    }

    public void setListBeacons(ArrayList<Beacon> listBeacons) {
        this.listBeacons = listBeacons;
    }

    public ArrayList<String> listInstructionsToStringArray() {
        ArrayList<String> result = new ArrayList<>();
        for (Instruction instruction : listInstructions) {
            result.add(instruction.getInstruction());
        }
        return result;
    }

    public boolean isBeaconInside(Beacon beacon) {
        for (Beacon b: listBeacons) {
            if (b.equals(beacon)) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Path{" +
                "listNodes=" + listNodes +
                ", listInstructions=" + listInstructions +
                ", source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", listBeacons=" + listBeacons +
                '}';
    }

    //==============================================================================================
    // Parcelable implementations
    //==============================================================================================

    public static final Creator<Path> CREATOR = new Creator<Path>() {
        @Override
        public Path createFromParcel(Parcel in) {
            return new Path(in);
        }

        @Override
        public Path[] newArray(int size) {
            return new Path[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(source);
        dest.writeString(destination);
        dest.writeTypedList(listBeacons);
    }

}
