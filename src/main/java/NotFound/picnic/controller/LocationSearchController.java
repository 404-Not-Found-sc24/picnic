package NotFound.picnic.controller;

import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import NotFound.picnic.service.LocationSearchService;
import NotFound.picnic.dto.LocationSearchDto;
import NotFound.picnic.domain.Location;
import java.security.Principal;



@RestController
@RequiredArgsConstructor
@RequestMapping("/tour/locations")
public class LocationSearchController {
	
private final LocationSearchService locationSearchService;
    
    @GetMapping
    public ResponseEntity<List<Location>> searchLocations(@ModelAttribute LocationSearchDto searchDto, Principal principal) {
        List<Location> locations = locationSearchService.LocationSearch(searchDto, principal);
        return ResponseEntity.ok().body(locations);
    }

}
