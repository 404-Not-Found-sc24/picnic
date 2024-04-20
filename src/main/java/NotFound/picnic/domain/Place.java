package NotFound.picnic.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long placeId;

    @Column
    private String date;

    @Column
    private String time;

    @OneToMany(mappedBy = "place", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @ToString.Exclude
    private List<Diary> diaryList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "schedule_id")
    @ToString.Exclude
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "location_id")
    @ToString.Exclude
    private Location location;
}
