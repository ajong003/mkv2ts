package Meta;

public class Track {
    public int id;
    public String type;     // e.g., "HEVC", "E-AC3 (DD+)", "SRT"
    public String streamId; // e.g., "V_MPEGH/ISO/HEVC", "A_AC3", "S_TEXT/UTF8"
    public String lang;     // may be null if tsmuxer didn't print it

    @Override
    public String toString() {
        return id+" " + type +" " +  streamId + " " + lang;
    }
}