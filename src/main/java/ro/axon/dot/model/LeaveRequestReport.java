package ro.axon.dot.model;


import java.util.List;
import lombok.Data;

@Data
public class LeaveRequestReport {
    private List<LeaveRequestReportItem> items;
}
