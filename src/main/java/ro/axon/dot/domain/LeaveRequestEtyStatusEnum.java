package ro.axon.dot.domain;

public enum LeaveRequestEtyStatusEnum {
    PENDING, APPROVED, REJECTED;

    public static LeaveRequestEtyStatusEnum findEnumValue(String name) {
        LeaveRequestEtyStatusEnum result = null;
        for (LeaveRequestEtyStatusEnum status : values()) {
            if (status.name().equalsIgnoreCase(name)) {
                result = status;
                break;
            }
        }
        return result;
    }
}
