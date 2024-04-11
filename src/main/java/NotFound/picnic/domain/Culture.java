package NotFound.picnic.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Culture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cultureId;

    @Column
    private String time;

    @Column
    private String offDate;

    @Column
    private String fee;

    @Column
    private String discount;

    @Column
    private String parking;

    @Column
    private String babycar;

    @Column
    private String pet;

    @Column
    private String detail;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "location_id")
    @ToString.Exclude
    private Location location;
}
