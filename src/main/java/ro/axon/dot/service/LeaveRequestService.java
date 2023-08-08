package ro.axon.dot.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.axon.dot.domain.LeaveRequestEty;
import ro.axon.dot.domain.LeaveRequestRepository;
import ro.axon.dot.mapper.LeaveRequestMapper;
import ro.axon.dot.model.LeaveRequestDetailsList;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;

    public LeaveRequestDetailsList getLeaveRequestsDetailsSorted(BooleanExpression query) {
        var leaveRequestDetailsList = new LeaveRequestDetailsList();
        Comparator<LeaveRequestEty> leaveRequestStatusComparator = Comparator.comparing(LeaveRequestEty::getStatus);
        Comparator<LeaveRequestEty> leaveRequestCrtTmsComparator = Comparator.comparing(LeaveRequestEty::getCrtTms);
        Iterable<LeaveRequestEty> filteredRepo = leaveRequestRepository.findAll(query);
        leaveRequestDetailsList.setItems(StreamSupport.stream(filteredRepo.spliterator(), false).filter(Objects::nonNull).sorted(leaveRequestStatusComparator.thenComparing(leaveRequestCrtTmsComparator))
                .map(LeaveRequestMapper.INSTANCE::mapLeaveRequestEtyToLeaveRequestDto).collect(Collectors.toList()));
        return leaveRequestDetailsList;
    }

}