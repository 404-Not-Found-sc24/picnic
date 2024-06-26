package NotFound.picnic.domain;

import NotFound.picnic.enums.State;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Approval {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long approvalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(10) DEFAULT 'APPLIED'")
    private State state;

    @Column
    private String date;

    @Column
    private String name;

    @Column
    private String address;

    @Column
    private String detail;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column
    private String division;

    @Column
    private String phone;

    @Column
    private String content;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "member_id")
    @ToString.Exclude
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "schedule_id")
    @ToString.Exclude
    private Schedule schedule;

    @OneToMany(mappedBy = "approval", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @ToString.Exclude
    private List<ApprovalImage> approvalImageList;
}
