package org.blinemedical.examination.domain;

import java.util.List;

public class Meeting extends AbstractPersistable {

    private List<Person> speakerList;
    private boolean entireGroupMeeting;
    /**
     * Multiply by {@link TimeGrain#GRAIN_LENGTH_IN_MINUTES} to get duration in minutes.
     */
    private int durationInGrains;

    private List<RequiredAttendance> requiredAttendanceList;
    private List<PreferredAttendance> preferredAttendanceList;

    public List<Person> getSpeakerList() {
        return speakerList;
    }

    public void setSpeakerList(List<Person> speakerList) {
        this.speakerList = speakerList;
    }

    public boolean isEntireGroupMeeting() {
        return entireGroupMeeting;
    }

    public void setEntireGroupMeeting(boolean entireGroupMeeting) {
        this.entireGroupMeeting = entireGroupMeeting;
    }

    public int getDurationInGrains() {
        return durationInGrains;
    }

    public void setDurationInGrains(int durationInGrains) {
        this.durationInGrains = durationInGrains;
    }

    public List<RequiredAttendance> getRequiredAttendanceList() {
        return requiredAttendanceList;
    }

    public void setRequiredAttendanceList(List<RequiredAttendance> requiredAttendanceList) {
        this.requiredAttendanceList = requiredAttendanceList;
    }

    public List<PreferredAttendance> getPreferredAttendanceList() {
        return preferredAttendanceList;
    }

    public void setPreferredAttendanceList(List<PreferredAttendance> preferredAttendanceList) {
        this.preferredAttendanceList = preferredAttendanceList;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public int getRequiredCapacity() {
        return requiredAttendanceList.size() + preferredAttendanceList.size();
    }

    public String getDurationString() {
        return (durationInGrains * TimeGrain.GRAIN_LENGTH_IN_MINUTES) + " minutes";
    }
}
