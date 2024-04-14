package NotFound.picnic.tour;

import NotFound.picnic.repository.LocationRepository;
import NotFound.picnic.domain.Location;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tour/Locationlist")
public class Locationlist {
	private final LocationRepository locationRepository;
	
	public Locationlist(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }
	
	
    
    @GetMapping("/bycity/{city}")
    public List<Location> searchLocationBycityContaining(@PathVariable String city) {
        return locationRepository.findLocationBycity(city);
    }

}
