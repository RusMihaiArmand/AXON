package ro.axon.dot.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.axon.dot.domain.entity.LeaveRequestEty;
import ro.axon.dot.mapper.LeaveRequestMapper;
import ro.axon.dot.model.LeaveRequestDetailsList;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import ro.axon.dot.domain.repositories.LeaveRequestRepository;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;

    @Transactional(readOnly = true)
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