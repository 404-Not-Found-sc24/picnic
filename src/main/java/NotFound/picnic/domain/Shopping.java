package NotFound.picnic.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Shopping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shoppingId;

    @Column
    private String time;

    @Column
    private String offDate;

    @Column
    private String parking;

    @Column
    private String babycar;

    @Column
    private String pet;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "location_id")
    @ToString.Exclude
    private Location location;
}
