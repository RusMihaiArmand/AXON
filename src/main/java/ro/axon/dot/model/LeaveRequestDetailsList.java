package ro.axon.dot.model;

import lombok.Data;

import java.util.List;

@Data
public class LeaveRequestDetailsList {

    private List<LeaveRequestDetailsListItem> items;
}
