package NotFound.picnic.tour;

import NotFound.picnic.repository.LocationRepository;
import NotFound.picnic.domain.Location;
import org.springframework.web.bind.annotation.*;



import java.util.List;

@RestController
@RequestMapping("/tour/LocationSearch")
public class LocationSearch {

    private final LocationRepository locationRepository;

    public LocationSearch(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @GetMapping("/byname/{name}/accurately")
    public List<Location> searchLocationByname(@PathVariable String name) {
        return locationRepository.findLocationByname(name);
    }
    
    @GetMapping("/byname/{name}")
    public List<Location> searchLocationBynameContaining(@PathVariable String name) {
        return locationRepository.findBynameContaining(name);
    }

    @GetMapping("/byaddress/accurately/{address}")
    public List<Location> searchLocationByaddress(@PathVariable String address) {
        return locationRepository.findLocationByaddress(address);
    }
    
    @GetMapping("/byaddress/{address}")
    public List<Location> searchLocationByaddressContaining(@PathVariable String address) {
        return locationRepository.findByaddressContaining(address);
    }

    

}
