package org.blinemedical.examination.domain;

import org.apache.commons.lang3.text.WordUtils;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.entity.PlanningPin;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity()
public class MeetingAssignment extends AbstractPersistable {

    private Meeting meeting;
    private boolean pinned;

    // Planning variables: changes during planning, between score calculations.
    private TimeGrain startingTimeGrain;
    private Room room;

    public MeetingAssignment() {
    }

    public MeetingAssignment(Meeting meeting, TimeGrain startingTimeGrain, Room room) {
        this.meeting = meeting;
        this.startingTimeGrain = startingTimeGrain;
        this.room = room;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }

    @PlanningPin
    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    @PlanningVariable(valueRangeProviderRefs = {"timeGrainRange"}, nullable = true)
    public TimeGrain getStartingTimeGrain() {
        return startingTimeGrain;
    }

    public void setStartingTimeGrain(TimeGrain startingTimeGrain) {
        this.startingTimeGrain = startingTimeGrain;
    }

    @PlanningVariable(valueRangeProviderRefs = {"roomRange"}, nullable = true)
    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public int calculateOverlap(MeetingAssignment other) {
        if (startingTimeGrain == null || other.getStartingTimeGrain() == null) {
            return 0;
        }
        int start = startingTimeGrain.getGrainIndex();
        int end = start + meeting.getDurationInGrains();
        int otherStart = other.startingTimeGrain.getGrainIndex();
        int otherEnd = otherStart + other.meeting.getDurationInGrains();

        if (end < otherStart) {
            return 0;
        } else if (otherEnd < start) {
            return 0;
        }
        return Math.min(end, otherEnd) - Math.max(start, otherStart);
    }

    public Integer getLastTimeGrainIndex() {
        if (startingTimeGrain == null) {
            return null;
        }
        return startingTimeGrain.getGrainIndex() + meeting.getDurationInGrains() - 1;
    }

    public String getStartingDateTimeString() {
        if (startingTimeGrain == null) {
            return null;
        }
        return startingTimeGrain.getDateTimeString();
    }

    public String getLabel() {
        int wrapLength = Math.max(8 * meeting.getDurationInGrains(), 16);
        return "<html><center>" + WordUtils.wrap(meeting.toString(), wrapLength, "<br/>", false)
            + "</center></html>";
    }
}
