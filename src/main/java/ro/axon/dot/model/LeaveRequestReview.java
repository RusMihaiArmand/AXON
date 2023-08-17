package ro.axon.dot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestReview {
    private String leaveRequestStatus;
    private Long version;
    private String rejectReason;
}
