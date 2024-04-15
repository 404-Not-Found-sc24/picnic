package NotFound.picnic.controller;

import NotFound.picnic.dto.PlaceCreateDto;

import NotFound.picnic.dto.RecordCreateDto;
import NotFound.picnic.repository.RecordRepository;
import NotFound.picnic.service.RecordService;
 
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/schedule/record")
public class RecordController {
	private final RecordService recordService;
	
	@PreAuthorize("isAuthenticated()")
    @PostMapping()
	public ResponseEntity<String> createRecord(@RequestBody RecordCreateDto recordCreateDto, Principal principal){
		String message = recordService.createRecord(recordCreateDto, principal);
		return ResponseEntity.ok().body(message);
	}

}
