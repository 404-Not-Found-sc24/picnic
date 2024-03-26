package NotFound.picnic.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.parameters.P;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long locationId;


    @OneToMany(mappedBy = "location", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @ToString.Exclude
    private List<LocationImage> locationImageList;

    @OneToMany(mappedBy = "location", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @ToString.Exclude
    private List<Place> placeList;
}
