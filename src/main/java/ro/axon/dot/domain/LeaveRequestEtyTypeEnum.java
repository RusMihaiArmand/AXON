package ro.axon.dot.domain;

public enum LeaveRequestEtyTypeEnum {
    MEDICAL, VACATION;

    public static LeaveRequestEtyTypeEnum findEnumValue(String name) {
        LeaveRequestEtyTypeEnum result = null;
        for (LeaveRequestEtyTypeEnum type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                result = type;
                break;
            }
        }
        return result;
    }
}
