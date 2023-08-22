package ro.axon.dot.model;
import lombok.Data;

@Data
public class LeaveRequestReportItem {
    private String firstName;
    private String lastName;
    private int noOfVacationDays;
    private int noOfMedicalDays;
    private LeaveRequestDetailsList leaveRequests;
}
