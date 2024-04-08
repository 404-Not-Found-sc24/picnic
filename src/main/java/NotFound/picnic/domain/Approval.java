package NotFound.picnic.domain;

import jakarta.persistence.*;
import lombok.*;

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

    @Column
    private int state;

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
}
