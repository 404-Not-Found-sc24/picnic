package NotFound.picnic.service;

import NotFound.picnic.domain.Location;
import NotFound.picnic.domain.LocationImage;
import NotFound.picnic.dto.LocationGetDto;
import NotFound.picnic.repository.LocationImageRepostiory;
import NotFound.picnic.repository.LocationRepository;

import NotFound.picnic.domain.City;
import NotFound.picnic.dto.CityGetDto;
import NotFound.picnic.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourService {
    private final LocationRepository locationRepository;
    private final LocationImageRepostiory locationImageRepostiory;
    private final S3Upload s3Upload;

    public List<LocationGetDto> GetLocations(String city, String keyword) throws UnsupportedEncodingException {

        Optional<List<Location>> locations;


        if (keyword == null)
            locations = locationRepository.findAllByCity(city);
        else
            locations = locationRepository.findByCityAndKeyword(city, keyword);

        return locations.map(locationList -> locationList.stream()
                .map(location -> {

                    Optional<LocationImage> image = locationImageRepostiory.findTopByLocation(location);
                    String imageUrl = null;
                    if (image.isPresent())
                        imageUrl = s3Upload.getImageUrl(image.get().getImageUrl());

                    return LocationGetDto.builder()
                            .locationId(location.getLocationId())
                            .name(location.getName())
                            .address(location.getAddress())
                            .latitude(location.getLatitude())
                            .longitude(location.getLongitude())
                            .imageUrl(imageUrl)
                            .build();
                })
                .toList()).orElse(null);

    }

    private final CityRepository cityRepository;
    public List<CityGetDto> GetCities(){
       List<City> cities = cityRepository.findAll();
       return cities.stream()
                .map(city -> CityGetDto.builder()
                        .cityName(city.getName())
                        .imageUrl(city.getImageUrl())
                        .build())
                .collect(Collectors.toList());
        
                
       

    }


}
