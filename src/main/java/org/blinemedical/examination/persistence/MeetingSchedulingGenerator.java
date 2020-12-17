package org.blinemedical.examination.persistence;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.blinemedical.examination.app.ExaminationApp;
import org.blinemedical.examination.domain.Attendance;
import org.blinemedical.examination.domain.Day;
import org.blinemedical.examination.domain.Meeting;
import org.blinemedical.examination.domain.MeetingAssignment;
import org.blinemedical.examination.domain.MeetingConstraintConfiguration;
import org.blinemedical.examination.domain.MeetingSchedule;
import org.blinemedical.examination.domain.Person;
import org.blinemedical.examination.domain.Room;
import org.blinemedical.examination.domain.TimeGrain;
import org.optaplanner.examples.common.app.CommonApp;
import org.optaplanner.examples.common.persistence.AbstractSolutionImporter;
import org.optaplanner.examples.common.persistence.generator.StringDataGenerator;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;

public class MeetingSchedulingGenerator {

    private static final Logger logger = LogManager.getLogger(MeetingSchedulingGenerator.class);

    public static void main(String[] args) {
        MeetingSchedulingGenerator generator = new MeetingSchedulingGenerator();
        generator.writeMeetingSchedule(50, 5, 4);
        generator.writeMeetingSchedule(100, 5, 4);
        generator.writeMeetingSchedule(200, 5, 4);
        generator.writeMeetingSchedule(400, 5, 4);
        generator.writeMeetingSchedule(800, 5, 4);
    }

    private final int[] durationInGrainsOptions = {
        1, // 15 mins
        2, // 30 mins
        3, // 45 mins
        4, // 1 hour
        6, // 90 mins
        8, // 2 hours
        16, // 4 hours
    };

    private final int[] startingMinuteOfDayOptions = {
        8 * 60, // 08:00
        8 * 60 + 15, // 08:15
        8 * 60 + 30, // 08:30
        8 * 60 + 45, // 08:45
        9 * 60, // 09:00
        9 * 60 + 15, // 09:15
        9 * 60 + 30, // 09:30
        9 * 60 + 45, // 09:45
        10 * 60, // 10:00
        10 * 60 + 15, // 10:15
        10 * 60 + 30, // 10:30
        10 * 60 + 45, // 10:45
        11 * 60, // 11:00
        11 * 60 + 15, // 11:15
        11 * 60 + 30, // 11:30
        11 * 60 + 45, // 11:45
        13 * 60, // 13:00
        13 * 60 + 15, // 13:15
        13 * 60 + 30, // 13:30
        13 * 60 + 45, // 13:45
        14 * 60, // 14:00
        14 * 60 + 15, // 14:15
        14 * 60 + 30, // 14:30
        14 * 60 + 45, // 14:45
        15 * 60, // 15:00
        15 * 60 + 15, // 15:15
        15 * 60 + 30, // 15:30
        15 * 60 + 45, // 15:45
        16 * 60, // 16:00
        16 * 60 + 15, // 16:15
        16 * 60 + 30, // 16:30
        16 * 60 + 45, // 16:45
        17 * 60, // 17:00
        17 * 60 + 15, // 17:15
        17 * 60 + 30, // 17:30
        17 * 60 + 45, // 17:45
    };

    private final StringDataGenerator fullNameGenerator = StringDataGenerator.buildFullNames();

    protected final SolutionFileIO<MeetingSchedule> solutionFileIO;
    protected final File outputDir;

    protected Random random;

    public MeetingSchedulingGenerator() {
        solutionFileIO = new MeetingSchedulingXlsxFileIO();
        outputDir = new File(CommonApp.determineDataDir(ExaminationApp.DATA_DIR_NAME), "unsolved");
    }

    private void writeMeetingSchedule(int meetingListSize, int roomListSize, int durationInGrains) {
        int timeGrainListSize =
            meetingListSize * durationInGrainsOptions[durationInGrainsOptions.length - 1]
                / roomListSize;
        String fileName = determineFileName(meetingListSize, timeGrainListSize, roomListSize);
        File outputFile = new File(outputDir,
            fileName + "." + solutionFileIO.getOutputFileExtension());
        MeetingSchedule meetingSchedule = createMeetingSchedule(fileName, meetingListSize,
            timeGrainListSize, roomListSize, durationInGrains);
        solutionFileIO.write(meetingSchedule, outputFile);
        logger.info("Saved: {}", outputFile);
    }

    private String determineFileName(int meetingListSize, int timeGrainListSize, int roomListSize) {
        return meetingListSize + "meetings-" + timeGrainListSize + "timegrains-" + roomListSize
            + "rooms";
    }

    public MeetingSchedule createMeetingSchedule(String fileName, int meetingListSize,
        int timeGrainListSize,
        int roomListSize, int durationInGrains) {
        random = new Random(37);
        MeetingSchedule meetingSchedule = new MeetingSchedule();
        meetingSchedule.setId(0L);
        MeetingConstraintConfiguration constraintConfiguration = new MeetingConstraintConfiguration();
        constraintConfiguration.setId(0L);
        meetingSchedule.setConstraintConfiguration(constraintConfiguration);

        createMeetingListAndAttendanceList(meetingSchedule, meetingListSize, durationInGrains);
        createTimeGrainList(meetingSchedule, timeGrainListSize);
        createRoomList(meetingSchedule, roomListSize);
        createPersonList(meetingSchedule);
        linkAttendanceListToPersons(meetingSchedule);
        createMeetingAssignmentList(meetingSchedule);

        BigInteger possibleSolutionSize = BigInteger
            .valueOf((long) timeGrainListSize * roomListSize)
            .pow(meetingSchedule.getMeetingAssignmentList().size());
        logger.info(
            "MeetingSchedule {} has {} meetings, {} timeGrains and {} rooms with a search space of {}.",
            fileName,
            meetingListSize,
            timeGrainListSize,
            roomListSize,
            AbstractSolutionImporter.getFlooredPossibleSolutionSize(possibleSolutionSize));
        return meetingSchedule;
    }

    private void createMeetingListAndAttendanceList(MeetingSchedule meetingSchedule,
        int meetingListSize, int durationInGrains) {
        List<Meeting> meetingList = new ArrayList<>(meetingListSize);
        List<Attendance> globalAttendanceList = new ArrayList<>();
        long attendanceId = 0L;
        for (int i = 0; i < meetingListSize; i++) {
            Meeting meeting = new Meeting();
            meeting.setId((long) i);
            meeting.setDurationInGrains(durationInGrains);

            int requiredAttendanceListSize = 2;
            List<Attendance> requiredAttendanceList = new ArrayList<>(
                requiredAttendanceListSize);
            for (int j = 0; j < requiredAttendanceListSize; j++) {
                Attendance attendance = new Attendance();
                attendance.setId(attendanceId);
                attendanceId++;
                attendance.setMeeting(meeting);
                // person is filled in later
                requiredAttendanceList.add(attendance);
                globalAttendanceList.add(attendance);
            }
            meeting.setRequiredAttendanceList(requiredAttendanceList);

            logger.trace("Created meeting with durationInGrains ({}),"
                    + " requiredAttendanceListSize ({}).",
                durationInGrains,
                requiredAttendanceListSize);
            meetingList.add(meeting);
        }
        meetingSchedule.setMeetingList(meetingList);
        meetingSchedule.setAttendanceList(globalAttendanceList);
    }

    private void createTimeGrainList(MeetingSchedule meetingSchedule, int timeGrainListSize) {
        List<Day> dayList = new ArrayList<>(timeGrainListSize);
        long dayId = 0;
        Day day = null;
        List<TimeGrain> timeGrainList = new ArrayList<>(timeGrainListSize);
        for (int i = 0; i < timeGrainListSize; i++) {
            TimeGrain timeGrain = new TimeGrain();
            timeGrain.setId((long) i);
            timeGrain.setGrainIndex(i);
            int dayOfYear = (i / startingMinuteOfDayOptions.length) + 1;
            if (day == null || day.getDayOfYear() != dayOfYear) {
                day = new Day();
                day.setId(dayId);
                day.setDayOfYear(dayOfYear);
                dayId++;
                dayList.add(day);
            }
            timeGrain.setDay(day);
            int startingMinuteOfDay = startingMinuteOfDayOptions[i
                % startingMinuteOfDayOptions.length];
            timeGrain.setStartingMinuteOfDay(startingMinuteOfDay);
            logger.trace(
                "Created timeGrain with grainIndex ({}), dayOfYear ({}), startingMinuteOfDay ({}).",
                i, dayOfYear, startingMinuteOfDay);
            timeGrainList.add(timeGrain);
        }
        meetingSchedule.setDayList(dayList);
        meetingSchedule.setTimeGrainList(timeGrainList);
    }

    private void createRoomList(MeetingSchedule meetingSchedule, int roomListSize) {
        final int roomsPerFloor = 20;
        List<Room> roomList = new ArrayList<>(roomListSize);
        for (int i = 0; i < roomListSize; i++) {
            Room room = new Room();
            room.setId((long) i);
            String name = "R " + ((i / roomsPerFloor * 100) + (i % roomsPerFloor) + 1);
            room.setName(name);
            int capacity = 2;
            room.setCapacity(capacity);
            logger.trace("Created room with name ({}), capacity ({}).", name, capacity);
            roomList.add(room);
        }
        meetingSchedule.setRoomList(roomList);
    }

    private void createPersonList(MeetingSchedule meetingSchedule) {
        int attendanceListSize = 0;
        for (Meeting meeting : meetingSchedule.getMeetingList()) {
            attendanceListSize += meeting.getRequiredAttendanceList().size();
        }
        int personListSize = attendanceListSize * meetingSchedule.getRoomList().size() * 3
            / (4 * meetingSchedule.getMeetingList().size());
        List<Person> personList = new ArrayList<>(personListSize);
        fullNameGenerator.predictMaximumSizeAndReset(personListSize);
        for (int i = 0; i < personListSize; i++) {
            Person person = new Person();
            person.setId((long) i);
            String fullName = fullNameGenerator.generateNextValue();
            person.setFullName(fullName);
            logger.trace("Created person with fullName ({}).",
                fullName);
            personList.add(person);
        }
        meetingSchedule.setPersonList(personList);
    }

    private void linkAttendanceListToPersons(MeetingSchedule meetingSchedule) {
        for (Meeting meeting : meetingSchedule.getMeetingList()) {
            List<Person> availablePersonList = new ArrayList<>(meetingSchedule.getPersonList());
            int attendanceListSize = meeting.getRequiredAttendanceList().size();
            if (availablePersonList.size() < attendanceListSize) {
                throw new IllegalStateException(
                    "The availablePersonList size (" + availablePersonList.size()
                        + ") is less than the attendanceListSize (" + attendanceListSize + ").");
            }
            for (Attendance requiredAttendance : meeting.getRequiredAttendanceList()) {
                requiredAttendance.setPerson(
                    availablePersonList.remove(random.nextInt(availablePersonList.size())));
            }
        }
    }

    private void createMeetingAssignmentList(MeetingSchedule meetingSchedule) {
        List<Meeting> meetingList = meetingSchedule.getMeetingList();
        List<MeetingAssignment> meetingAssignmentList = new ArrayList<>(meetingList.size());
        for (Meeting meeting : meetingList) {
            MeetingAssignment meetingAssignment = new MeetingAssignment();
            meetingAssignment.setId(meeting.getId());
            meetingAssignment.setMeeting(meeting);
            meetingAssignmentList.add(meetingAssignment);
        }
        meetingSchedule.setMeetingAssignmentList(meetingAssignmentList);
    }

}
