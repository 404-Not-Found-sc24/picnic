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
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    @Column
    private String name;

    @Column
    private String location;

    @Column
    private String startDate;

    @Column
    private String endDate;

    @Column
    @Builder.Default
    private boolean share = false;

    @OneToMany(mappedBy = "schedule", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Approval> approvalList;

    @OneToMany(mappedBy = "schedule", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @ToString.Exclude
    private List<Place> placeList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "member_id")
    @ToString.Exclude
    private Member member;

}
