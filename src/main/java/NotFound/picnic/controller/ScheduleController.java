package NotFound.picnic.controller;

import NotFound.picnic.dto.schedule.*;
import NotFound.picnic.exception.CustomException;
import NotFound.picnic.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/schedule")
public class ScheduleController {
    private final ScheduleService scheduleService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping()
    public ResponseEntity<Long> createSchedule(@RequestBody ScheduleCreateDto scheduleCreateDto, Principal principal) {
        Long message = scheduleService.createSchedule(scheduleCreateDto, principal);
        return ResponseEntity.ok().body(message);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/place/{scheduleId}")
    public ResponseEntity<String> createLocations(@PathVariable(name="scheduleId") Long scheduleId, @RequestBody List<PlaceCreateDto> placeCreateDtoList, Principal principal) {
        String message = scheduleService.createLocations(scheduleId, placeCreateDtoList, principal);
        return ResponseEntity.ok().body(message);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/diary/{placeId}")
    public ResponseEntity<DiaryResponseDto> createDiary(@PathVariable(name="placeId") Long placeId, DiaryCreateDto diaryCreateDto) throws IOException {
        DiaryResponseDto diaryResponseDto = scheduleService.createDiary(placeId, diaryCreateDto);
        return ResponseEntity.ok().body(diaryResponseDto);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/diary/{diaryId}")
    public ResponseEntity<String> updateDiary(@PathVariable(name="diaryId") Long diaryId,DiaryCreateDto diaryCreateDto, Principal principal) throws CustomException{
        String res = scheduleService.UpdateDiary(diaryId, diaryCreateDto, principal);
        return ResponseEntity.ok().body(res);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/diary/{diaryId}")
    public ResponseEntity<String> deleteDiary(@PathVariable(name="diaryId") Long diaryId, Principal principal){
        String res = scheduleService.DeleteDiary(diaryId, principal);
        return ResponseEntity.ok().body(res);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/schedules/{scheduleId}")
    public ResponseEntity<List<SchedulePlaceDiaryGetDto>> getSchedulePlaceDiary(@PathVariable(name="scheduleId") Long scheduleId, Principal principal){
        List<SchedulePlaceDiaryGetDto> schedulePlaceDiaryList = scheduleService.getSchedulePlaceDiary(scheduleId, principal);
        return ResponseEntity.ok().body(schedulePlaceDiaryList);
    }

    @GetMapping("/places/{scheduleId}")
    public ResponseEntity<List<List<PlaceGetDto>>> getPlaces(@PathVariable(name="scheduleId") Long scheduleId) {
        List<List<PlaceGetDto>> placeList = scheduleService.getPlaces(scheduleId);
        return ResponseEntity.ok().body(placeList);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("")
    public ResponseEntity<MyScheduleListDto> getSchedulesInMyPage(Principal principal) {
        MyScheduleListDto myScheduleListDto = scheduleService.GetSchedulesInMyPage(principal);
        return ResponseEntity.ok().body(myScheduleListDto);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<String> deleteSchedule (@PathVariable(name="scheduleId") Long scheduleId, Principal principal) throws IOException {
        String res = scheduleService.deleteSchedule(scheduleId, principal);
        return ResponseEntity.ok().body(res);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/place/{placeId}")
    public ResponseEntity<String> deletePlace(@PathVariable(name="placeId") Long placeId, Principal principal) throws IOException{
        String response = scheduleService.DeletePlace(placeId, principal);
        return ResponseEntity.ok().body(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list")
    public ResponseEntity<List<MyScheduleGetDto>> getSchedules(Principal principal) {
        List<MyScheduleGetDto> scheduleGetDtos = scheduleService.GetSchedules(principal);
        return ResponseEntity.ok().body(scheduleGetDtos);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{scheduleId}")
    public ResponseEntity<String> updateSchedule(@PathVariable(name="scheduleId") Long scheduleId, @RequestBody ScheduleCreateDto scheduleCreateDto) {
        String res = scheduleService.UpdateSchedule(scheduleCreateDto, scheduleId);
        return ResponseEntity.ok().body(res);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/sharing/{scheduleId}")
    public ResponseEntity<String> changeSharing(@PathVariable(name="scheduleId") Long scheduleId, Principal principal) throws CustomException {
        String res = scheduleService.ChangeSharing(scheduleId, principal);
        return ResponseEntity.ok().body(res);
    }
}
