package ro.axon.dot.api;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.axon.dot.model.OffDayList;
import ro.axon.dot.service.OffDayService;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class OffDaysApi {

  private final OffDayService offDayService;


  @GetMapping("misc/legally-days-off")
  public ResponseEntity<OffDayList> getDaysOff( @RequestParam("periods") List<String> periods,
      @RequestParam("years") List<String> years) {

    return ResponseEntity.ok(offDayService.getOffDays(periods,years));
  }

}
