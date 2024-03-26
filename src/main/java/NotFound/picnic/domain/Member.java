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
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column
    private String password;

    @Column
    private String name;

    @Column
    private String nickname;

    @Column
    private String email;

    @Column
    private String phone;

    @Column
    private int role;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @ToString.Exclude
    private List<Schedule> scheduleList;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @ToString.Exclude
    private List<Approval> approvalList;

}
