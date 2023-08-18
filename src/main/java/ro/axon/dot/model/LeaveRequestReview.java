package ro.axon.dot.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.axon.dot.validation.RejectionReasonRequired;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RejectionReasonRequired
public class LeaveRequestReview {

    @NotEmpty(message = "Leave request type cannot be empty or null.")
    @Pattern(regexp = "APPROVAL|REJECTION", message = "Invalid leave request review type.")
    private String type;

    @NotNull(message = "Version cannot be null.")
    private Long v;

    @Size(max = 255, message = "Reject reason cannot exceed 255 characters.")
    private String rejectionReason;
}
